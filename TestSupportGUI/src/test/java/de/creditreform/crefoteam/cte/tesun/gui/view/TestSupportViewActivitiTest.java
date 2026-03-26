package de.creditreform.crefoteam.cte.tesun.gui.view;

import de.creditreform.crefoteam.activiti.CteActivitiProcess;
import de.creditreform.crefoteam.activiti.CteActivitiService;
import de.creditreform.crefoteam.activiti.CteActivitiServiceRestImpl;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.gui.BaseGUITest;
import de.creditreform.crefoteam.cte.tesun.gui.TestSupportGUI;
import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
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
 *   2. Prozess fortsetzen (Ja-Dialog): selbe Prozess-Instanz bleibt aktiv.
 *   3. Prozess neu starten (Nein-Dialog): alte Instanz gelöscht, neue gestartet.
 */
public class TestSupportViewActivitiTest extends BaseGUITest {

    private static final String PROCESS_KEY  = "ENE-TestAutomationProcess";
    private static final String DIALOG_TITLE = "CTE-Testautomatisierung";

    private static EnvironmentConfig ENV_CONFIG;
    private static CteActivitiService activitiService;

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
            for (CteActivitiProcess p : activitiService.queryProcessInstances(PROCESS_KEY, new HashMap<>())) {
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
        guiFrame = new TestSupportGUI(ENV_CONFIG); // frisches Frame pro Test — kein Zustand aus Vortests
        super.setUp(); // setzt frameOperator
        testSupportView = extractTestSupportView();
        waitForStartButtonEnabled(30_000);
    }

    @Override
    public void tearDown() {
        try {
            deleteAllRunningProcesses();
        } catch (Exception ignored) {}
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
        SwingUtilities.invokeAndWait(() ->
                enabled[0] = testSupportView.getButtonStopUserTasksThread().isEnabled());
        assertTrue("Stop-Button muss nach Prozessstart aktiviert sein", enabled[0]);
    }

    // -----------------------------------------------------------------------
    // Test 2: Laufenden Prozess fortsetzen (Ja-Antwort im Dialog)
    //         Check: selbe Prozess-Instanz bleibt aktiv auf Activiti
    // -----------------------------------------------------------------------

    @Test
    public void test2_laufendenProzessFortsetzten() throws Exception {
        CteActivitiProcess existing = startProcessOnActiviti();
        Integer existingId = existing.getId();

        new JButtonOperator(frameOperator, "Prozess starten").push();

        // Dialog 1 "Soll der Prozess fortgesetzt ... werden?" — "Ja"
        // Hinweis: showConfirmDialog verwendet JScrollPane mit VERTICAL_SCROLLBAR_ALWAYS,
        // dadurch enthält die Dialog-Komponenten-Hierarchie BasicArrowButton-Instanzen des
        // Scrollbars vor den eigentlichen Optionen-Buttons → Index-basierte Suche unzuverlässig,
        // stattdessen nach Button-Text suchen.
        new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Ja").push();
        // Dialog 2 "Verzeichnis TEST_OUTPUTS aktualisiert?" aus prepareStart() — "Ja"
        new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Ja").push();

        waitForStopButtonEnabled(120_000);

        // Selbe Instanz muss noch auf Activiti aktiv sein
        List<CteActivitiProcess> processes = queryRunningProcesses();
        boolean sameActive = processes.stream().anyMatch(p -> existingId.equals(p.getId()));
        assertTrue("Selbe Prozess-Instanz muss im Fortsetzen-Modus noch aktiv sein", sameActive);
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
        Map<String, Object> vars = new HashMap<>();
        vars.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY, ENV_CONFIG.getActivitProcessKey());
        vars.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME, PROCESS_KEY);
        vars.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE, TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        return activitiService.startProcess(PROCESS_KEY, vars);
    }

    private List<CteActivitiProcess> queryRunningProcesses() throws Exception {
        return activitiService.queryProcessInstances(PROCESS_KEY, new HashMap<>());
    }

    private void deleteAllRunningProcesses() throws Exception {
        for (CteActivitiProcess p : queryRunningProcesses()) {
            activitiService.deleteProcessInstance(p.getId());
        }
    }

    private void waitForStartButtonEnabled(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                boolean[] enabled = {false};
                SwingUtilities.invokeAndWait(() ->
                        enabled[0] = testSupportView.getButtonStartProcess().isEnabled());
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
                    enabled[0] = testSupportView.getButtonStopUserTasksThread().isEnabled());
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
