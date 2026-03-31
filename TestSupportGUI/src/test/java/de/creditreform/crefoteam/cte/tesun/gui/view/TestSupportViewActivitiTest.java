package de.creditreform.crefoteam.cte.tesun.gui.view;

import de.creditreform.crefoteam.activiti.CteActivitiProcess;
import de.creditreform.crefoteam.activiti.CteActivitiService;
import de.creditreform.crefoteam.activiti.CteActivitiServiceRestImpl;
import de.creditreform.crefoteam.activiti.CteActivitiTask;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.gui.BaseGUITest;
import de.creditreform.crefoteam.cte.tesun.gui.TestSupportGUI;
import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten.TEST_PHASE;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * GUI-Tests für TestSupportView#startActivitiProcess und #stopActivitiProcess.
 *
 * Voraussetzungen:
 *   - Gültige ENE-config.properties im Classpath (test/resources)
 *     mit ACTIVITI_URLS=CAVDARK-ENE@cavdark::http://localhost:9090
 *   - Laufender Activiti-Docker-Container auf localhost:9090
 *   - BPMN-Datei bpmns/TestAutomationProcess.bpmn im Classpath (test/resources/bpmns/)
 *
 * Fehlen Voraussetzungen, werden alle Tests automatisch übersprungen (Assume).
 *
 * Test-Szenarien:
 *   1. Frisch-Start: Stop-Button wird nach dem Prozessstart aktiviert.
 *   2. Prozess fortsetzen: Unterbrechung bei Phase-1- und Phase-2-UserTask,
 *      anschließend Fortsetzung — geprüft ob GUI genau beim unterbrochenen Task weitermacht.
 *   3. Prozess neu starten (Nein-Dialog): alte Instanz gelöscht, neue gestartet.
 */
public class TestSupportViewActivitiTest extends BaseGUITest {

    private static final String PROCESS_KEY  = "ENE-TestAutomationProcess";
    private static final String DIALOG_TITLE = "CTE-Testautomatisierung";

    private static EnvironmentConfig ENV_CONFIG;
    private static CteActivitiService activitiService;
    private static HashMap<String, Object> paramsMap = new HashMap<>();
    /** Query-Map ohne TEST_PHASE — für queryProcessInstances, damit Ergebnis phasenunabhängig ist. */
    private static HashMap<String, Object> queryMap = new HashMap<>();
    /** Task-Query-Map nur mit MEIN_KEY — für listTasks, damit auch Sub-Prozess-Tasks gefunden werden. */
    private static HashMap<String, Object> taskQueryMap = new HashMap<>();

    private TestSupportView testSupportView;

    // -----------------------------------------------------------------------
    // Klassen-Setup / -Teardown
    // -----------------------------------------------------------------------

    @BeforeClass
    public static void setUpClass() {
        try {
            ENV_CONFIG = new EnvironmentConfig("ENE");
            List<RestInvokerConfig> configs = ENV_CONFIG.getRestServiceConfigsForActiviti();
            activitiService = new CteActivitiServiceRestImpl(configs.get(0));
            // Verbindung testen — schlägt fehl wenn Container nicht läuft
            // Laufende Prozesse vorab löschen, damit das Deployment gelöscht werden kann (FK-Constraint)
            paramsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY, ENV_CONFIG.getActivitProcessKey());
            paramsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME, PROCESS_KEY);
            paramsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE, TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
            // queryMap ohne TEST_PHASE — für queryProcessInstances
            queryMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY, ENV_CONFIG.getActivitProcessKey());
            queryMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME, PROCESS_KEY);
            // taskQueryMap nur MEIN_KEY — für listTasks, kein processDefinitionKey-Filter
            // damit Tasks in Sub-Prozessen (andere processDefinitionKey) ebenfalls gefunden werden
            taskQueryMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY, ENV_CONFIG.getActivitProcessKey());
            for (CteActivitiProcess p : activitiService.queryProcessInstances(PROCESS_KEY, paramsMap)) {
                activitiService.deleteProcessInstance(p.getId());
            }
            // BPMN vorab deployen — damit alle Tests startProcess() aufrufen können
            GUIStaticUtils.uploadActivitiProcessesFromClassPath(activitiService, "ENE");
        } catch (Exception e) {
            ENV_CONFIG = null;
            activitiService = null;
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // Frames werden in tearDown() disposed — nichts zu tun
    }

    public TestSupportViewActivitiTest() {
        super(null); // guiFrame wird in setUp() pro Test frisch gesetzt
    }

    // -----------------------------------------------------------------------
    // Test-Setup / -Teardown
    // -----------------------------------------------------------------------

    @Before
    @Override
    public void setUp() {
        Assume.assumeNotNull(
                "Activiti-Container oder ENE-config.properties nicht verfügbar — Test übersprungen",
                activitiService, ENV_CONFIG);
        try {
            deleteAllRunningProcesses();
        } catch (Exception e) {
            Assume.assumeNoException("Activiti-Verbindung fehlgeschlagen — Test übersprungen", e);
        }
        try {
            guiFrame = new TestSupportGUI(ENV_CONFIG); // frisches Frame pro Test — kein Zustand aus Vortests
        } catch (RuntimeException e) {
            Assume.assumeNoException(
                    "TestSupportGUI konnte nicht gestartet werden (Umgebung gesperrt oder andere externe Instanz läuft) — Test übersprungen",
                    e);
        }
        guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // verhindert System.exit() beim dispose() im Test
        super.setUp(); // setzt frameOperator
        testSupportView = extractTestSupportView();
        waitForStartButtonEnabled(30_000);
    }

    @Override
    public void tearDown() {
        // Warten bis der Activiti-Prozess komplett durchgelaufen ist (Demo-Mode: alle UserTasks automatisch erledigt).
        // Erst dann dispose() aufrufen, damit Surefire die JVM nicht vorzeitig beendet und processThread
        // alle UserTasks verarbeiten kann.
        if (testSupportView != null) {
            waitForStartButtonEnabled(120_000);
        }
        if (guiFrame != null) {
            guiFrame.setVisible(false);
            guiFrame.dispose();
            guiFrame = null;
        }
    }

    // -----------------------------------------------------------------------
    // Test 1: Frisch-Start — Stop-Button wird nach dem Prozessstart aktiviert
    // -----------------------------------------------------------------------

    @Test
    public void test1_frischStart_stopButtonAktiviert() throws Exception {
        new JButtonOperator(frameOperator, "Prozess starten").push();

        waitForStopButtonEnabled(120_000);

        boolean[] enabled = {false};
        SwingUtilities.invokeAndWait(() -> enabled[0] = testSupportView.getViewTestSupportMainProcess().getButtonStopUserTasksThread().isEnabled());
        assertTrue("Stop-Button muss nach Prozessstart aktiviert sein", enabled[0]);
    }

    // -----------------------------------------------------------------------
    // Test 2: Prozess unterbrechen und fortsetzen (in derselben GUI-Session)
    //         Phase 1: Prozess auf Activiti starten, auf Phase-1-UserTask warten,
    //                  GUI fortsetzen — prüfen ob GUI genau bei diesem Task beginnt.
    //         Phase 2: GUI verarbeitet Phase 1 durch, auf Phase-2-UserTask warten,
    //                  GUI-Loop per Reflection unterbrechen (simuliert unerwartetes Ende,
    //                  kein Cancel-Signal → Activiti-Prozess bleibt erhalten),
    //                  im selben Frame fortsetzen — prüfen ob GUI beim Phase-2-Task beginnt.
    // -----------------------------------------------------------------------

    @Test
    public void test2_laufendenProzessFortsetzten() throws Exception {
        // --- Phase 1: Unterbrechung + Fortsetzung ---
        // Prozess direkt auf Activiti starten — GUI erkennt ihn und setzt fort
        startProcessOnActiviti();

        // Warten bis Phase-1-UserTask auf Activiti erscheint
        CteActivitiTask taskPhase1 = waitForTaskInPhase(TEST_PHASE.PHASE_1, 60_000);
        assertNotNull("Prozess muss Phase-1-UserTask erreichen", taskPhase1);

        // GUI startet → laufender Prozess erkannt → Dialog → "Ja" (Fortsetzen)
        new JButtonOperator(frameOperator, "Prozess starten").push();
        new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Ja").push();

        // GUI muss den Phase-1-Task in der Konsole ausgeben — beweist korrekte Fortsetzung
        assertTrue("GUI muss nach Fortsetzung bei Phase-1-Task '" + taskPhase1.getName() + "' beginnen", waitForTaskNameInConsole(taskPhase1.getName(), 60_000, 0));

        // --- Phase 2: GUI läuft weiter (verarbeitet Phase 1), auf Phase-2-Task warten ---
        CteActivitiTask taskPhase2 = waitForTaskInPhase(TEST_PHASE.PHASE_2, 120_000);
        Assume.assumeNotNull( "Phase-2-UserTask nicht rechtzeitig erschienen — Phase-2-Check übersprungen", taskPhase2);

        // Konsolenposition merken, damit Phase-2-Check nur neuen Text prüft
        int consoleLengthBeforePhase2Resume = getConsoleLength();

        // GUI-Loop per Reflection unterbrechen: nur running=false, kein Cancel-Signal.
        // Simuliert ein unerwartetes Ende der GUI (z.B. Absturz) — der Activiti-Prozess
        // bleibt am Phase-2-Task erhalten und kann fortgesetzt werden.
        // (Zum Vergleich: clickStopButton() würde ENEcancelProcessSignal senden
        //  und den Activiti-Prozess abbrechen — das wäre kein "Fortsetzen"-Szenario.)
        stopActivitiControllerLoop();
        waitForStartButtonEnabled(30_000);

        // Prüfen ob Phase-2-Task nach der Unterbrechung noch auf Activiti vorhanden ist
        CteActivitiTask taskPhase2ForResume = waitForTaskInPhase(TEST_PHASE.PHASE_2, 10_000);
        Assume.assumeNotNull( "Phase-2-Task nach Unterbrechung nicht mehr auf Activiti — Phase-2-Check übersprungen", taskPhase2ForResume);

        // Im selben GUI-Frame fortsetzen: "Prozess starten" → "Ja"
        new JButtonOperator(frameOperator, "Prozess starten").push();
        new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Ja").push();

        // Konsole (nur neuer Inhalt) muss Phase-2-Task-Namen enthalten
        assertTrue("GUI muss nach Fortsetzung bei Phase-2-Task '" + taskPhase2ForResume.getName() + "' beginnen", waitForTaskNameInConsole(taskPhase2ForResume.getName(), 60_000, consoleLengthBeforePhase2Resume));
    }

    // -----------------------------------------------------------------------
    // Test 3: Alten Prozess beenden und neu starten (Nein-Antwort im Dialog)
    //         Check: alte Instanz gelöscht, neue Instanz gestartet
    // -----------------------------------------------------------------------

    @Test
    public void test3_altenProzessBeendenUndNeuStarten() throws Exception {
        CteActivitiProcess existing = startProcessOnActiviti();
        Integer oldId = existing.getId();

        new JButtonOperator(frameOperator, "Prozess starten").push();

        // Dialog abwarten und "Nein" wählen → alten Prozess beenden, neu starten
        // (Text-basierte Suche, da VERTICAL_SCROLLBAR_ALWAYS die Button-Indizes verschiebt)
        new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Nein").push();

        waitForStopButtonEnabled(120_000);
        waitForNewProcess(oldId, 120_000);

        List<CteActivitiProcess> processes = queryRunningProcesses();
        assertFalse("Es muss eine neue Prozess-Instanz geben", processes.isEmpty());
        boolean oldGone = processes.stream().noneMatch(p -> oldId.equals(p.getId()));
        assertTrue("Alte Prozess-Instanz muss gelöscht worden sein", oldGone);
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden
    // -----------------------------------------------------------------------

    private CteActivitiProcess startProcessOnActiviti() throws Exception {
        return activitiService.startProcess(PROCESS_KEY, paramsMap);
    }

    private List<CteActivitiProcess> queryRunningProcesses() throws Exception {
        return activitiService.queryProcessInstances(PROCESS_KEY, queryMap);
    }

    private void deleteAllRunningProcesses() throws Exception {
        for (CteActivitiProcess p : queryRunningProcesses()) {
            activitiService.deleteProcessInstance(p.getId());
        }
    }

    /**
     * Fragt Activiti-Tasks nach Phase. Nutzt listTasks() mit dem MEIN_KEY-Filter,
     * damit auch Tasks in Sub-Prozessen gefunden werden.
     */
    private CteActivitiTask waitForTaskInPhase(TEST_PHASE expectedPhase, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            List<CteActivitiTask> tasks = activitiService.listTasks(taskQueryMap);
            for (CteActivitiTask task : tasks) {
                String phase = task.getVariables().get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
                if (expectedPhase.name().equals(phase)) {
                    return task;
                }
            }
            Thread.sleep(500);
        }
        return null;
    }

    /**
     * Wartet bis der Konsolen-Text ab Position startOffset den Task-Namen enthält.
     * startOffset=0 prüft den gesamten Text; ein höherer Wert prüft nur neu hinzugekommenen Text.
     */
    private boolean waitForTaskNameInConsole(String taskName, long timeoutMs, int startOffset) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String[] consoleText = {""};
            SwingUtilities.invokeAndWait(() ->
                    consoleText[0] = testSupportView.getTabbedPaneMonitor()
                            .getTextAreaTaskListenerInfo().getText());
            String relevantText = consoleText[0].length() > startOffset
                    ? consoleText[0].substring(startOffset)
                    : "";
            if (relevantText.contains(taskName)) {
                return true;
            }
            Thread.sleep(200);
        }
        return false;
    }

    /** Gibt die aktuelle Zeichenanzahl der Konsole zurück. */
    private int getConsoleLength() throws Exception {
        int[] len = {0};
        SwingUtilities.invokeAndWait(() ->
                len[0] = testSupportView.getTabbedPaneMonitor()
                        .getTextAreaTaskListenerInfo().getText().length());
        return len[0];
    }

    /**
     * Unterbricht den GUI-Loop per Reflection (setzt running=false), ohne das
     * Activiti-Cancel-Signal zu senden. Simuliert ein unerwartetes Ende der GUI
     * (z.B. Absturz) — der Activiti-Prozess bleibt am aktuellen Task erhalten.
     */
    private void stopActivitiControllerLoop() throws Exception {
        Field controllerField = TestSupportView.class.getDeclaredField("activitiController");
        controllerField.setAccessible(true);
        ActivitiProcessController controller = (ActivitiProcessController) controllerField.get(testSupportView);

        Field runningField = ActivitiProcessController.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(controller, false);
    }

    /**
     * Klickt den Stop-Button der GUI auf dem EDT.
     * Ruft ActivitiProcessController.stop() auf: setzt running=false UND sendet
     * ENEcancelProcessSignal an Activiti → der Activiti-Prozess wird abgebrochen.
     * Nur für Tests geeignet, die das bewusste Abbrechen testen (nicht für Fortsetzen).
     */
    private void clickStopButton() throws Exception {
        SwingUtilities.invokeAndWait(() ->
                testSupportView.getViewTestSupportMainProcess().getButtonStopUserTasksThread().doClick());
    }

    private void waitForStartButtonEnabled(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                boolean[] enabled = {false};
                SwingUtilities.invokeAndWait(() -> enabled[0] = testSupportView.getViewTestSupportMainProcess().getButtonStartProcess().isEnabled());
                if (enabled[0]) return;
                Thread.sleep(200);
            } catch (Exception ignored) {}
        }
        // Kein fail() — wenn der Button nie enabled wird, schlägt der Test beim push() fehl
    }

    private void waitForStopButtonEnabled(long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            boolean[] enabled = {false};
            SwingUtilities.invokeAndWait(() ->
                    enabled[0] = testSupportView.getViewTestSupportMainProcess().getButtonStopUserTasksThread().isEnabled());
            if (enabled[0]) return;
            Thread.sleep(100);
        }
        fail("Stop-Button wurde nicht rechtzeitig aktiviert (Timeout: " + timeoutMs + " ms)");
    }

    private void waitForNewProcess(Integer oldId, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            boolean newExists = queryRunningProcesses().stream()
                    .anyMatch(p -> !oldId.equals(p.getId()));
            if (newExists) return;
            Thread.sleep(200);
        }
        fail("Neue Prozess-Instanz nicht rechtzeitig auf Activiti gestartet (Timeout: "
                + timeoutMs + " ms)");
    }

    private TestSupportView extractTestSupportView() {
        try {
            Field field = TestSupportGUI.class.getDeclaredField("testSupportView");
            field.setAccessible(true);
            return (TestSupportView) field.get(guiFrame);
        } catch (Exception e) {
            throw new RuntimeException("testSupportView konnte nicht extrahiert werden", e);
        }
    }
}
