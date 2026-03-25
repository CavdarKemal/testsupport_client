package de.creditreform.crefoteam.cte.tesun.gui.view;

import de.creditreform.crefoteam.activiti.CteActivitiTask;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.gui.BaseGUITest;
import de.creditreform.crefoteam.cte.tesun.gui.TestSupportGUI;
import de.creditreform.crefoteam.cte.tesun.gui.utils.TestSupportHelper;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.log4j.Level;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.*;

/**
 * GUI-Tests für TestSupportView#startActivitiProcess und #stopActivitiProcess.
 *
 * Da keine Server vorhanden sind (CLAUDE_MODE), wird der ActivitiProcessController
 * durch FakeActivitiProcessController ersetzt — er simuliert das Prozessverhalten
 * ohne Netzwerkzugriff.
 *
 * Voraussetzung: gültige EnvironmentConfig("ENE") mit existierenden Properties-Dateien
 * (gleiche Anforderung wie TestGUITestSupport).
 *
 * Test-Szenarien:
 *   1. Prozess starten und bis zum Ende laufen lassen
 *   2. Prozess starten → bei UserTask stoppen → neu starten → fortsetzen:
 *      Check: runProcess erhält den pausierten Task
 *   3. Prozess starten → bei UserTask stoppen → neu starten → von vorne:
 *      Check: runProcess erhält null (frischer Start, alter Prozess beendet)
 */
public class TestSupportViewActivitiTest extends BaseGUITest {

    /**
     * GUI-Frame wird einmal statisch erstellt. Schlägt EnvironmentConfig fehl
     * (z.B. fehlende ENE-Properties-Datei im Maven-Build), bleibt das Feld null
     * und alle Tests werden via Assume übersprungen — kein Fehler.
     */
    private static final TestSupportGUI GUI_FRAME;
    static {
        TestSupportGUI frame;
        try {
            frame = new TestSupportGUI(new EnvironmentConfig("ENE"));
        } catch (Exception e) {
            frame = null;
        }
        GUI_FRAME = frame;
    }

    private TestSupportView testSupportView;
    private FakeActivitiProcessController fakeController;

    public TestSupportViewActivitiTest() {
        super(GUI_FRAME);
    }

    @Before
    @Override
    public void setUp() {
        Assume.assumeNotNull("EnvironmentConfig('ENE') nicht verfügbar — Test übersprungen", GUI_FRAME);
        super.setUp();
        try {
            testSupportView = extractTestSupportView();
            fakeController = new FakeActivitiProcessController(testSupportView);
            injectController(testSupportView, fakeController);
        } catch (Exception e) {
            throw new RuntimeException("setUp fehlgeschlagen", e);
        }
    }

    // -----------------------------------------------------------------------
    // Test 1: Prozess starten, bis zum Ende laufen lassen
    // -----------------------------------------------------------------------
    @Test
    public void test1_startProcess_waitForEnd() throws Exception {
        fakeController.setPrepareStartResult(null); // null = Frisch-Start, kein laufender Prozess

        clickStart();
        fakeController.waitForRunProcessCalled(3_000);
        assertTrue("runProcess muss aufgerufen worden sein", fakeController.wasRunProcessCalled());
        assertNull("Frisch-Start: finalTask muss null sein", fakeController.getLastRunProcessTask());

        // Prozess-Ende simulieren: löst notifyClientJob(null) aus → GUI aufräumen
        fakeController.simulateProcessEnd();
        fakeController.waitForProcessEndNotified(5_000);

        SwingUtilities.invokeAndWait(() ->
            assertFalse("Stop-Button muss nach Prozessende deaktiviert sein",
                testSupportView.getButtonStopUserTasksThread().isEnabled()));
    }

    // -----------------------------------------------------------------------
    // Test 2: Starten → bei UserTask stoppen → neu starten → fortsetzen
    //         Check: runProcess erhält den pausierten (existierenden) Task
    // -----------------------------------------------------------------------
    @Test
    public void test2_stopAtUserTask_restart_continue() throws Exception {
        CteActivitiTask existingTask = createMock(CteActivitiTask.class);

        // --- Phase 1: Frisch starten ---
        fakeController.setPrepareStartResult(null);
        clickStart();
        fakeController.waitForRunProcessCalled(3_000);

        // --- Phase 2: Bei UserTask stoppen ---
        clickStop();
        fakeController.waitForStopCalled(2_000);

        // --- Phase 3: Neu starten im Continue-Modus ---
        // prepareStart gibt den pausierten Task zurück → Prozess wird fortgesetzt
        fakeController.resetForNextRun(existingTask);

        clickStart();
        fakeController.waitForRunProcessCalled(3_000);

        assertSame("runProcess muss mit dem pausierten Task aufgerufen werden (Continue-Modus)",
            existingTask, fakeController.getLastRunProcessTask());
    }

    // -----------------------------------------------------------------------
    // Test 3: Starten → bei UserTask stoppen → neu starten → von vorne
    //         Check: runProcess erhält null (alter Prozess beendet, frischer Start)
    // -----------------------------------------------------------------------
    @Test
    public void test3_stopAtUserTask_restart_fresh() throws Exception {
        // --- Phase 1: Frisch starten ---
        fakeController.setPrepareStartResult(null);
        clickStart();
        fakeController.waitForRunProcessCalled(3_000);

        // --- Phase 2: Bei UserTask stoppen ---
        clickStop();
        fakeController.waitForStopCalled(2_000);

        // --- Phase 3: Neu starten im Fresh-Modus ---
        // prepareStart gibt null zurück → alter Prozess ist beendet worden, neuer startet
        fakeController.resetForNextRun(null);

        clickStart();
        fakeController.waitForRunProcessCalled(3_000);

        assertNull("runProcess muss mit null aufgerufen werden (Frisch-Start nach Abbruch)",
            fakeController.getLastRunProcessTask());
        assertTrue("runProcess muss beim Neustart aufgerufen worden sein",
            fakeController.wasRunProcessCalled());
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden
    // -----------------------------------------------------------------------

    private void clickStart() throws Exception {
        SwingUtilities.invokeAndWait(() -> testSupportView.getButtonStartProcess().doClick());
    }

    private void clickStop() throws Exception {
        SwingUtilities.invokeAndWait(() -> testSupportView.getButtonStopUserTasksThread().doClick());
    }

    private TestSupportView extractTestSupportView() throws Exception {
        Field field = TestSupportGUI.class.getDeclaredField("testSupportView");
        field.setAccessible(true);
        return (TestSupportView) field.get(guiFrame);
    }

    private static void injectController(TestSupportView view, FakeActivitiProcessController fake)
            throws Exception {
        Field field = TestSupportView.class.getDeclaredField("activitiController");
        field.setAccessible(true);
        field.set(view, fake);
    }

    // -----------------------------------------------------------------------
    // FakeActivitiProcessController
    //
    // Überschreibt alle relevanten Methoden; kein echter Server-Zugriff.
    // prepareStart() gibt einen konfigurierbaren Task zurück (null = Frisch-Start).
    // runProcess() speichert den Task und signalisiert über einen Latch.
    // stop() signalisiert über einen Latch.
    // simulateProcessEnd() ruft notifyClientJob(null) auf dem Callback auf
    //   → löst die GUI-Aufräumlogik in TestSupportView aus.
    // -----------------------------------------------------------------------
    static class FakeActivitiProcessController extends ActivitiProcessController {

        private final TesunClientJobListener viewCallback;

        // Konfiguration für prepareStart (wird vor jedem run gesetzt)
        private volatile CteActivitiTask prepareStartResult;

        // Beobachtbare Ergebnisse
        private volatile boolean runProcessCalled;
        private volatile CteActivitiTask lastRunProcessTask;
        private volatile boolean running;

        // Latches für Synchronisation zwischen Test-Thread und Background-Thread
        private volatile CountDownLatch runProcessLatch = new CountDownLatch(1);
        private volatile CountDownLatch stopLatch       = new CountDownLatch(1);
        private volatile CountDownLatch processEndLatch = new CountDownLatch(1);

        FakeActivitiProcessController(TesunClientJobListener viewCallback) {
            super(viewCallback, () -> {});
            this.viewCallback = viewCallback;
        }

        /** Setzt den Rückgabewert für prepareStart. null = Frisch-Start, non-null = Continue. */
        void setPrepareStartResult(CteActivitiTask task) {
            this.prepareStartResult = task;
        }

        /** Setzt neuen Zustand für den nächsten Lauf (nach stop). */
        void resetForNextRun(CteActivitiTask nextPrepareStartResult) {
            this.prepareStartResult = nextPrepareStartResult;
            this.runProcessCalled = false;
            this.lastRunProcessTask = null;
            this.runProcessLatch = new CountDownLatch(1);
            this.stopLatch       = new CountDownLatch(1);
            // processEndLatch wird nur in Test 1 verwendet, daher kein Reset nötig
        }

        /** Simuliert das Ende des Activiti-Prozesses über den View-Callback. */
        void simulateProcessEnd() {
            running = false;
            // Ruft die echte notifyClientJob(null)-Logik in TestSupportView auf,
            // die enableComponentsToOnOff(true) und Stop-Button-Deaktivierung ausführt.
            viewCallback.notifyClientJob(Level.INFO, null);
            processEndLatch.countDown();
        }

        void waitForRunProcessCalled(long timeoutMs) throws InterruptedException {
            assertTrue("runProcess wurde nicht rechtzeitig aufgerufen (Timeout: " + timeoutMs + "ms)",
                runProcessLatch.await(timeoutMs, TimeUnit.MILLISECONDS));
        }

        void waitForStopCalled(long timeoutMs) throws InterruptedException {
            assertTrue("stop() wurde nicht rechtzeitig aufgerufen (Timeout: " + timeoutMs + "ms)",
                stopLatch.await(timeoutMs, TimeUnit.MILLISECONDS));
        }

        void waitForProcessEndNotified(long timeoutMs) throws InterruptedException {
            assertTrue("simulateProcessEnd() wurde nicht rechtzeitig gemeldet (Timeout: " + timeoutMs + "ms)",
                processEndLatch.await(timeoutMs, TimeUnit.MILLISECONDS));
        }

        boolean wasRunProcessCalled()       { return runProcessCalled; }
        CteActivitiTask getLastRunProcessTask() { return lastRunProcessTask; }

        // ---- Überschriebene Controller-Methoden ----

        @Override
        public CteActivitiTask prepareStart(TestSupportHelper helper, EnvironmentConfig env) {
            // Kein Server-Zugriff: gibt direkt den konfigurierten Wert zurück
            return prepareStartResult;
        }

        @Override
        public void runProcess(TestSupportHelper helper, EnvironmentConfig env,
                Map<String, Object> taskVariablesMap,
                Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap,
                CteActivitiTask cteActivitiTask) {
            this.lastRunProcessTask = cteActivitiTask;
            this.runProcessCalled = true;
            this.running = true;
            this.runProcessLatch.countDown();
            // Kein echter Prozess-Loop — Prozessende wird über simulateProcessEnd() ausgelöst
        }

        @Override
        public void stop() {
            running = false;
            stopLatch.countDown();
            // Mehrfache Aufrufe (z.B. aus notifyClientJob(null)) sind unbedenklich
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }
}
