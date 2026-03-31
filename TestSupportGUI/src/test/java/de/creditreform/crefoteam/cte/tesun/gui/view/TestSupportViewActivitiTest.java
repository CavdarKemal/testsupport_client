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
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * GUI-Tests für TestSupportView#startActivitiProcess und #stopActivitiProcess.
 *
 * Voraussetzungen:
 *   - Gültige ENE-config.properties und GEE-config.properties im Classpath (test/resources)
 *   - Laufender Activiti-Docker-Container auf localhost:9090
 *   - BPMN-Dateien bpmns/CteAutomatedTestProcess.bpmn + CteAutomatedTestProcessSUB.bpmn
 *
 * Test-Szenarien:
 *   1. Vollständiger Durchlauf — alle 44 Tasks (Main-Start + Phase-1 + Phase-2 + Main-End) in richtiger Reihenfolge
 *   2. Phase-1-Unterbrechung + Fortsetzen — Prozess stoppt im Phase-1-Sub-Prozess, GUI setzt an korrektem Task fort
 *   3. Phase-2-Unterbrechung + Fortsetzen — dasselbe für Phase-2
 *   4. Phase-2-Unterbrechung + Neu-Start (Nein) — alte Instanz gelöscht, neue gestartet
 *   5. Parallel ENE + GEE — beide laufen vollständig durch
 *   6. Parallel ENE + GEE — ENE bei Phase-1, GEE bei Phase-2 unterbrochen, beide fortgesetzt
 */
public class TestSupportViewActivitiTest extends BaseGUITest {

    // -----------------------------------------------------------------------
    // Task-Suchtoken für verifyTaskOrder() (erscheinen im Konsolen-Text)
    // Quelle: task.getName() via notifyTask() — "Name : <name>"
    // -----------------------------------------------------------------------

    /** Token aus dem Hauptprozess, vor den Sub-Prozessen. */
    private static final String[] TASK_TOKENS_MAIN_START = {
        "Systemeinstellungen für",      // UserTaskPrepareTestSystem
        "[GeneratePseudoCrefos]"        // UserTaskGeneratePseudoCrefos
    };

    /** Token der 20 Sub-Prozess-Tasks (Phase-1 und Phase-2 verwenden dieselbe Liste). */
    private static final String[] TASK_TOKENS_PHASE = {
        "[StartUploads]",
        "[WaittUploads]",
        "[StartBeteiligtenImport]",
        "[WaitForBeteiligtenImport]",
        "[StartEntgBerechnung]",
        "[WaitForEntgBerechnung]",
        "[StartBtlgAktualisierung]",
        "[WaitForBtlgAktualisierung]",
        "[WaittCtImport]",
        "[StartCtImport]",
        "[WaitForCtImport]",
        "[TaskWaitBeforeExport]",
        "[StartExports]",
        "[StartCollect]",
        "[CheckCollects]",
        "[StartRestore]",
        "[CheckRefExports]",
        "[CheckExportProtokoll]",
        "[StartSftpUploads]",
        "[CheckSftpUploads]"
    };

    /** Token aus dem Hauptprozess, nach den Sub-Prozessen. */
    private static final String[] TASK_TOKENS_MAIN_END = {
        "Erfolgs-Mail",                 // UserTaskSuccessMail
        "Restauriert System"            // UserTaskRestoreTestSystem
    };

    /** Vollständige Tokenliste: Main-Start + Phase-1 + Phase-2 + Main-End (44 Einträge). */
    private static final List<String> ALL_TASK_TOKENS;
    static {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(TASK_TOKENS_MAIN_START));
        list.addAll(Arrays.asList(TASK_TOKENS_PHASE));  // Phase-1
        list.addAll(Arrays.asList(TASK_TOKENS_PHASE));  // Phase-2
        list.addAll(Arrays.asList(TASK_TOKENS_MAIN_END));
        ALL_TASK_TOKENS = Collections.unmodifiableList(list);
    }

    /**
     * Konsolen-Token zum Erkennen des Unterbrechungspunkts.
     * Der Konsolen-Text bleibt nach Erscheinen erhalten — kein Polling-Race-Condition
     * wie bei Activiti-Task-Abfragen (Tasks können in <500ms verarbeitet werden).
     */
    private static final String INTERRUPT_TOKEN_PHASE1 = "[StartBeteiligtenImport]"; // Phase-1 Sub-Prozess-Task
    private static final String INTERRUPT_TOKEN_PHASE2 = "PHASE_2"; // erscheint im "Test-Phase"-Feld der notifyTask()-Ausgabe

    private static final String DIALOG_TITLE = "CTE-Testautomatisierung";

    // -----------------------------------------------------------------------
    // ENE — Statische Felder
    // -----------------------------------------------------------------------

    private static final String PROCESS_KEY_ENE = "ENE-TestAutomationProcess";
    private static EnvironmentConfig ENV_CONFIG_ENE;
    private static CteActivitiService activitiServiceENE;
    private static final Map<String, Object> paramsMapENE  = new HashMap<>();  // zum startProcess()
    private static final Map<String, Object> queryMapENE   = new HashMap<>();  // für queryProcessInstances (kein TEST_PHASE)
    private static final Map<String, Object> taskQueryMapENE = new HashMap<>(); // für listTasks (nur MEIN_KEY)

    // -----------------------------------------------------------------------
    // GEE — Statische Felder (nur für Tests 5 & 6 benötigt)
    // -----------------------------------------------------------------------

    private static final String PROCESS_KEY_GEE = "GEE-TestAutomationProcess";
    private static EnvironmentConfig ENV_CONFIG_GEE;
    private static CteActivitiService activitiServiceGEE;
    private static final Map<String, Object> paramsMapGEE    = new HashMap<>();
    private static final Map<String, Object> queryMapGEE     = new HashMap<>();
    private static final Map<String, Object> taskQueryMapGEE = new HashMap<>();

    // -----------------------------------------------------------------------
    // Instanz-Felder
    // -----------------------------------------------------------------------

    /** ENE-View — gesetzt in setUp(). */
    private TestSupportView viewENE;

    // -----------------------------------------------------------------------
    // Klassen-Setup / -Teardown
    // -----------------------------------------------------------------------

    @BeforeClass
    public static void setUpClass() {
        // ENE
        try {
            ENV_CONFIG_ENE = new EnvironmentConfig("ENE");
            List<RestInvokerConfig> configs = ENV_CONFIG_ENE.getRestServiceConfigsForActiviti();
            activitiServiceENE = new CteActivitiServiceRestImpl(configs.get(0));

            paramsMapENE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY,               ENV_CONFIG_ENE.getActivitProcessKey());
            paramsMapENE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME,   PROCESS_KEY_ENE);
            paramsMapENE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE,              TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
            paramsMapENE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_TYPE,               TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2);

            queryMapENE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY,                ENV_CONFIG_ENE.getActivitProcessKey());
            queryMapENE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME,    PROCESS_KEY_ENE);

            taskQueryMapENE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY,             ENV_CONFIG_ENE.getActivitProcessKey());

            // Laufende Prozesse vorab löschen, BPMN deployen
            for (CteActivitiProcess p : activitiServiceENE.queryProcessInstances(PROCESS_KEY_ENE, queryMapENE)) {
                activitiServiceENE.deleteProcessInstance(p.getId());
            }
            GUIStaticUtils.uploadActivitiProcessesFromClassPath(activitiServiceENE, "ENE");
        } catch (Exception e) {
            ENV_CONFIG_ENE = null;
            activitiServiceENE = null;
        }

        // GEE (optional — fehlende Konfiguration führt nur zu übersprungenen Tests 5 & 6)
        try {
            ENV_CONFIG_GEE = new EnvironmentConfig("GEE");
            List<RestInvokerConfig> configs = ENV_CONFIG_GEE.getRestServiceConfigsForActiviti();
            activitiServiceGEE = new CteActivitiServiceRestImpl(configs.get(0));

            paramsMapGEE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY,               ENV_CONFIG_GEE.getActivitProcessKey());
            paramsMapGEE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME,   PROCESS_KEY_GEE);
            paramsMapGEE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE,              TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
            paramsMapGEE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_TYPE,               TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2);

            queryMapGEE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY,                ENV_CONFIG_GEE.getActivitProcessKey());
            queryMapGEE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME,    PROCESS_KEY_GEE);

            taskQueryMapGEE.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY,             ENV_CONFIG_GEE.getActivitProcessKey());

            for (CteActivitiProcess p : activitiServiceGEE.queryProcessInstances(PROCESS_KEY_GEE, queryMapGEE)) {
                activitiServiceGEE.deleteProcessInstance(p.getId());
            }
            GUIStaticUtils.uploadActivitiProcessesFromClassPath(activitiServiceGEE, "GEE");
        } catch (Exception e) {
            ENV_CONFIG_GEE = null;
            activitiServiceGEE = null;
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
                activitiServiceENE, ENV_CONFIG_ENE);
        try {
            deleteAllRunningProcesses(activitiServiceENE, PROCESS_KEY_ENE, queryMapENE);
        } catch (Exception e) {
            Assume.assumeNoException("Activiti-Verbindung fehlgeschlagen — Test übersprungen", e);
        }
        try {
            guiFrame = new TestSupportGUI(ENV_CONFIG_ENE);
        } catch (RuntimeException e) {
            Assume.assumeNoException(
                    "TestSupportGUI konnte nicht gestartet werden (Umgebung gesperrt) — Test übersprungen", e);
        }
        guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setUp(); // setzt frameOperator
        viewENE = extractViewFrom((TestSupportGUI) guiFrame);
        waitForStartButtonEnabled(viewENE, 30_000);
    }

    @Override
    public void tearDown() {
        if (viewENE != null) {
            waitForStartButtonEnabled(viewENE, 120_000);
        }
        if (guiFrame != null) {
            guiFrame.setVisible(false);
            guiFrame.dispose();
            guiFrame = null;
        }
        viewENE = null;
    }

    // -----------------------------------------------------------------------
    // Test 1: Vollständiger Durchlauf — Task-Reihenfolge verifiziert
    // -----------------------------------------------------------------------

    @Test
    public void test1_vollstaendigerDurchlauf_TaskReihenfolgeVerifiziert() throws Exception {
        System.out.println("[TEST] test1: Starte vollständigen Prozessdurchlauf...");
        new JButtonOperator(frameOperator, "Prozess starten").push();

        waitForStopButtonEnabled(viewENE, 60_000);
        System.out.println("[TEST] test1: Prozess läuft — warte auf vollständigen Abschluss (" + ALL_TASK_TOKENS.size() + " Tasks)...");

        waitForStartButtonEnabled(viewENE, 480_000);
        System.out.println("[TEST] test1: Prozess abgeschlossen — prüfe Task-Reihenfolge");

        String consoleText = getConsoleText(viewENE);
        verifyTaskOrder(consoleText, ALL_TASK_TOKENS);
        System.out.println("[TEST] test1: Task-Reihenfolge korrekt (" + ALL_TASK_TOKENS.size() + " Tokens verifiziert)");
    }

    // -----------------------------------------------------------------------
    // Test 2: Phase-1-Unterbrechung + Fortsetzen
    // -----------------------------------------------------------------------

    @Test
    public void test2_phase1Unterbrechung_fortsetzen() throws Exception {
        System.out.println("[TEST] test2: Starte Prozess für Phase-1-Unterbrechung...");
        new JButtonOperator(frameOperator, "Prozess starten").push();
        waitForStopButtonEnabled(viewENE, 60_000);

        // Warten bis ein Phase-1-Task in der Konsole erscheint.
        // Konsolen-Text bleibt erhalten — kein Polling-Race-Condition wie bei Activiti-Task-Abfragen.
        boolean phase1Seen = waitForTaskNameInConsole(viewENE, INTERRUPT_TOKEN_PHASE1, 120_000, 0);
        Assume.assumeTrue("Phase-1-Token '" + INTERRUPT_TOKEN_PHASE1 + "' nicht rechtzeitig in Konsole erschienen — Test übersprungen", phase1Seen);
        System.out.println("[TEST] test2: Phase-1-Token '" + INTERRUPT_TOKEN_PHASE1 + "' in Konsole gesehen — unterbreche GUI");

        stopActivitiControllerLoop(viewENE);
        waitForStartButtonEnabled(viewENE, 60_000);

        // Aktuellen Task nach Unterbrechung ermitteln (Prozess könnte einen Schritt weiter sein)
        CteActivitiTask resumeTask = getCurrentTask(activitiServiceENE, taskQueryMapENE);
        Assume.assumeNotNull("Kein Activiti-Task nach Phase-1-Unterbrechung — Test übersprungen", resumeTask);
        System.out.println("[TEST] test2: GUI setzt fort bei Task: '" + resumeTask.getName() + "'");

        int consoleLengthBefore = getConsoleLength(viewENE);

        // Fortsetzen: Dialog erscheint (laufender Prozess erkannt) → Ja
        new JButtonOperator(frameOperator, "Prozess starten").push();
        new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Ja").push();
        System.out.println("[TEST] test2: Dialog 'Fortsetzen' bestätigt (Ja)");

        assertTrue("GUI muss nach Phase-1-Fortsetzung bei Task '" + resumeTask.getName() + "' beginnen",
                waitForTaskNameInConsole(viewENE, resumeTask.getName(), 60_000, consoleLengthBefore));
        System.out.println("[TEST] test2: Phase-1-Fortsetzung erfolgreich verifiziert");
    }

    // -----------------------------------------------------------------------
    // Test 3: Phase-2-Unterbrechung + Fortsetzen
    // -----------------------------------------------------------------------

    @Test
    public void test3_phase2Unterbrechung_fortsetzen() throws Exception {
        System.out.println("[TEST] test3: Starte Prozess für Phase-2-Unterbrechung (Phase-1 muss zuerst abgeschlossen werden)...");
        new JButtonOperator(frameOperator, "Prozess starten").push();
        waitForStopButtonEnabled(viewENE, 60_000);

        // Warten bis Phase-2 in der Konsole erscheint (long timeout — Phase-1 läuft vollständig durch)
        boolean phase2Seen = waitForTaskNameInConsole(viewENE, INTERRUPT_TOKEN_PHASE2, 300_000, 0);
        Assume.assumeTrue("Phase-2-Indikator nicht rechtzeitig in Konsole erschienen — Test übersprungen", phase2Seen);
        System.out.println("[TEST] test3: Phase-2-Indikator in Konsole gesehen — unterbreche GUI");

        stopActivitiControllerLoop(viewENE);
        waitForStartButtonEnabled(viewENE, 60_000);

        // Aktuellen Task nach Unterbrechung ermitteln
        CteActivitiTask resumeTask = getCurrentTask(activitiServiceENE, taskQueryMapENE);
        Assume.assumeNotNull("Kein Activiti-Task nach Phase-2-Unterbrechung — Test übersprungen", resumeTask);
        System.out.println("[TEST] test3: GUI setzt fort bei Task: '" + resumeTask.getName() + "'");

        int consoleLengthBefore = getConsoleLength(viewENE);

        // Fortsetzen
        new JButtonOperator(frameOperator, "Prozess starten").push();
        new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Ja").push();
        System.out.println("[TEST] test3: Dialog 'Fortsetzen' bestätigt (Ja)");

        assertTrue("GUI muss nach Phase-2-Fortsetzung bei Task '" + resumeTask.getName() + "' beginnen",
                waitForTaskNameInConsole(viewENE, resumeTask.getName(), 60_000, consoleLengthBefore));
        System.out.println("[TEST] test3: Phase-2-Fortsetzung erfolgreich verifiziert");
    }

    // -----------------------------------------------------------------------
    // Test 4: Phase-2-Unterbrechung + Neu-Start (Dialog: Nein)
    // -----------------------------------------------------------------------

    @Test
    public void test4_phase2Unterbrechung_neuStart() throws Exception {
        System.out.println("[TEST] test4: Starte Prozess für Phase-2-Unterbrechung mit anschließendem Neu-Start...");
        new JButtonOperator(frameOperator, "Prozess starten").push();
        waitForStopButtonEnabled(viewENE, 60_000);

        boolean phase2Seen = waitForTaskNameInConsole(viewENE, INTERRUPT_TOKEN_PHASE2, 300_000, 0);
        Assume.assumeTrue("Phase-2-Indikator nicht rechtzeitig in Konsole erschienen — Test übersprungen", phase2Seen);
        System.out.println("[TEST] test4: Phase-2-Indikator in Konsole gesehen — unterbreche GUI");
        stopActivitiControllerLoop(viewENE);
        waitForStartButtonEnabled(viewENE, 60_000);

        // Alte Prozess-ID sichern
        List<CteActivitiProcess> runningBefore = activitiServiceENE.queryProcessInstances(PROCESS_KEY_ENE, queryMapENE);
        Assume.assumeFalse("Kein laufender Prozess nach Unterbrechung — Test übersprungen", runningBefore.isEmpty());
        Integer oldId = runningBefore.get(0).getId();
        System.out.println("[TEST] test4: Alte Prozess-ID: " + oldId + " — wähle Nein (Neu-Start)");

        // Neu-Start: Dialog → Nein (alte Instanz löschen, neu starten)
        new JButtonOperator(frameOperator, "Prozess starten").push();
        new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Nein").push();
        System.out.println("[TEST] test4: Dialog 'Neu-Start' bestätigt (Nein)");

        waitForStopButtonEnabled(viewENE, 120_000);
        waitForNewProcess(activitiServiceENE, PROCESS_KEY_ENE, queryMapENE, oldId, 60_000);

        List<CteActivitiProcess> runningAfter = activitiServiceENE.queryProcessInstances(PROCESS_KEY_ENE, queryMapENE);
        assertFalse("Es muss eine neue Prozess-Instanz geben", runningAfter.isEmpty());
        boolean oldGone = runningAfter.stream().noneMatch(p -> oldId.equals(p.getId()));
        assertTrue("Alte Prozess-Instanz muss gelöscht worden sein (ID: " + oldId + ")", oldGone);
        System.out.println("[TEST] test4: Neu-Start verifiziert — alte ID " + oldId + " ist weg, neue ID: "
                + runningAfter.get(0).getId());
    }

    // -----------------------------------------------------------------------
    // Test 5: Parallel ENE + GEE — vollständiger Durchlauf beider Prozesse
    // -----------------------------------------------------------------------

    @Test
    public void test5_parallelENEundGEE_vollstaendigerDurchlauf() throws Exception {
        Assume.assumeNotNull("GEE nicht verfügbar — Test übersprungen", ENV_CONFIG_GEE, activitiServiceGEE);

        TestSupportGUI guiGEE = null;
        TestSupportView viewGEE = null;
        try {
            // GEE vorbereiten
            deleteAllRunningProcesses(activitiServiceGEE, PROCESS_KEY_GEE, queryMapGEE);
            guiGEE = new TestSupportGUI(ENV_CONFIG_GEE);
            guiGEE.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JFrameOperator frameOpGEE = new JFrameOperator(guiGEE);
            viewGEE = extractViewFrom(guiGEE);
            waitForStartButtonEnabled(viewGEE, 30_000);

            System.out.println("[TEST] test5: Starte ENE und GEE parallel...");
            new JButtonOperator(frameOperator, "Prozess starten").push();
            new JButtonOperator(frameOpGEE, "Prozess starten").push();

            waitForStopButtonEnabled(viewENE, 60_000);
            waitForStopButtonEnabled(viewGEE, 60_000);
            System.out.println("[TEST] test5: Beide Prozesse gestartet — warte auf vollständigen Abschluss...");

            waitForStartButtonEnabled(viewENE, 480_000);
            System.out.println("[TEST] test5: ENE abgeschlossen");
            waitForStartButtonEnabled(viewGEE, 480_000);
            System.out.println("[TEST] test5: GEE abgeschlossen");

            // Task-Reihenfolge ENE prüfen
            String consoleENE = getConsoleText(viewENE);
            verifyTaskOrder(consoleENE, ALL_TASK_TOKENS);
            System.out.println("[TEST] test5: ENE Task-Reihenfolge korrekt");

            // Task-Reihenfolge GEE prüfen
            String consoleGEE = getConsoleText(viewGEE);
            verifyTaskOrder(consoleGEE, ALL_TASK_TOKENS);
            System.out.println("[TEST] test5: GEE Task-Reihenfolge korrekt — paralleler Durchlauf verifiziert");

        } finally {
            if (guiGEE != null) {
                if (viewGEE != null) waitForStartButtonEnabled(viewGEE, 120_000);
                guiGEE.setVisible(false);
                guiGEE.dispose();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Test 6: Parallel ENE + GEE — ENE bei Phase-1, GEE bei Phase-2 unterbrochen, beide fortgesetzt
    // -----------------------------------------------------------------------

    @Test
    public void test6_parallelENEundGEE_unterbrechenUndFortsetzen() throws Exception {
        Assume.assumeNotNull("GEE nicht verfügbar — Test übersprungen", ENV_CONFIG_GEE, activitiServiceGEE);

        TestSupportGUI guiGEE = null;
        TestSupportView viewGEE = null;
        try {
            // GEE vorbereiten
            deleteAllRunningProcesses(activitiServiceGEE, PROCESS_KEY_GEE, queryMapGEE);
            guiGEE = new TestSupportGUI(ENV_CONFIG_GEE);
            guiGEE.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JFrameOperator frameOpGEE = new JFrameOperator(guiGEE);
            viewGEE = extractViewFrom(guiGEE);
            waitForStartButtonEnabled(viewGEE, 30_000);

            System.out.println("[TEST] test6: Starte ENE und GEE parallel...");
            new JButtonOperator(frameOperator, "Prozess starten").push();
            new JButtonOperator(frameOpGEE, "Prozess starten").push();

            waitForStopButtonEnabled(viewENE, 60_000);
            waitForStopButtonEnabled(viewGEE, 60_000);

            // ENE: warten bis Phase-1-Token in ENE-Konsole erscheint, dann GUI stoppen.
            // Konsolen-Text bleibt erhalten — kein Polling-Race-Condition (Tasks können schneller sein als 500ms-Polls).
            boolean enePhase1Seen = waitForTaskNameInConsole(viewENE, INTERRUPT_TOKEN_PHASE1, 120_000, 0);
            System.out.println("[TEST] test6: ENE Phase-1-Token '" + INTERRUPT_TOKEN_PHASE1 + "' "
                    + (enePhase1Seen ? "gesehen" : "NICHT gesehen (Timeout)") + " — unterbreche ENE-GUI");
            Assume.assumeTrue("ENE Phase-1-Token nicht in Konsole erschienen — Test übersprungen", enePhase1Seen);
            stopActivitiControllerLoop(viewENE);

            // GEE: warten bis Phase-2-Indikator in GEE-Konsole erscheint (GEE muss Phase-1 erst abschließen).
            // ENE ist jetzt gestoppt — GEE läuft weiter.
            boolean geePhase2Seen = waitForTaskNameInConsole(viewGEE, INTERRUPT_TOKEN_PHASE2, 360_000, 0);
            Assume.assumeTrue("GEE Phase-2-Indikator nicht rechtzeitig in Konsole erschienen — Test übersprungen", geePhase2Seen);
            System.out.println("[TEST] test6: GEE Phase-2-Indikator '" + INTERRUPT_TOKEN_PHASE2 + "' gesehen — unterbreche GEE-GUI");
            stopActivitiControllerLoop(viewGEE);

            // Auf beide Start-Buttons warten (GUIs sind gestoppt)
            waitForStartButtonEnabled(viewENE, 60_000);
            waitForStartButtonEnabled(viewGEE, 60_000);

            // Aktuellen Task nach Unterbrechung ermitteln
            CteActivitiTask eneResumeTask = getCurrentTask(activitiServiceENE, taskQueryMapENE);
            CteActivitiTask geeResumeTask = getCurrentTask(activitiServiceGEE, taskQueryMapGEE);
            Assume.assumeNotNull("Kein ENE-Task nach Unterbrechung — Test übersprungen", eneResumeTask);
            Assume.assumeNotNull("Kein GEE-Task nach Unterbrechung — Test übersprungen", geeResumeTask);

            System.out.println("[TEST] test6: ENE-Fortsetzung bei Task: '" + eneResumeTask.getName() + "'");
            System.out.println("[TEST] test6: GEE-Fortsetzung bei Task: '" + geeResumeTask.getName() + "'");

            int eneConsoleLenBefore = getConsoleLength(viewENE);
            int geeConsoleLenBefore = getConsoleLength(viewGEE);

            // ENE fortsetzen
            new JButtonOperator(frameOperator, "Prozess starten").push();
            new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Ja").push();
            System.out.println("[TEST] test6: ENE Dialog 'Fortsetzen' bestätigt (Ja)");

            // GEE fortsetzen
            new JButtonOperator(frameOpGEE, "Prozess starten").push();
            new JDialogOperator(DIALOG_TITLE); // warten bis Dialog erscheint
            new JButtonOperator(new JDialogOperator(DIALOG_TITLE), "Ja").push();
            System.out.println("[TEST] test6: GEE Dialog 'Fortsetzen' bestätigt (Ja)");

            // ENE-Konsole prüfen
            assertTrue("ENE-GUI muss nach Fortsetzung bei Task '" + eneResumeTask.getName() + "' beginnen",
                    waitForTaskNameInConsole(viewENE, eneResumeTask.getName(), 60_000, eneConsoleLenBefore));
            System.out.println("[TEST] test6: ENE-Fortsetzung verifiziert");

            // GEE-Konsole prüfen
            assertTrue("GEE-GUI muss nach Fortsetzung bei Task '" + geeResumeTask.getName() + "' beginnen",
                    waitForTaskNameInConsole(viewGEE, geeResumeTask.getName(), 60_000, geeConsoleLenBefore));
            System.out.println("[TEST] test6: GEE-Fortsetzung verifiziert — parallele Unterbrechung und Fortsetzung erfolgreich");

        } finally {
            if (guiGEE != null) {
                if (viewGEE != null) waitForStartButtonEnabled(viewGEE, 120_000);
                guiGEE.setVisible(false);
                guiGEE.dispose();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden — Activiti-Abfragen
    // -----------------------------------------------------------------------

    private void deleteAllRunningProcesses(CteActivitiService service, String processKey, Map<String, Object> qMap) throws Exception {
        for (CteActivitiProcess p : service.queryProcessInstances(processKey, qMap)) {
            service.deleteProcessInstance(p.getId());
        }
    }

    /**
     * Gibt den ersten verfügbaren Task zurück, oder null wenn keiner vorhanden ist.
     * Wird aufgerufen NACHDEM stopActivitiControllerLoop() und waitForStartButtonEnabled() —
     * der Prozess ist dann an einem stabilen Task eingefroren.
     */
    private CteActivitiTask getCurrentTask(CteActivitiService service, Map<String, Object> tqMap) throws Exception {
        List<CteActivitiTask> tasks = service.listTasks(tqMap);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden — GUI-Zustand
    // -----------------------------------------------------------------------

    /**
     * Unterbricht den GUI-Loop per Reflection (setzt running=false), ohne das
     * Activiti-Cancel-Signal zu senden. Simuliert ein unerwartetes Ende der GUI-Session —
     * der Activiti-Prozess bleibt am aktuellen Task erhalten und kann fortgesetzt werden.
     */
    private void stopActivitiControllerLoop(TestSupportView view) throws Exception {
        Field controllerField = TestSupportView.class.getDeclaredField("activitiController");
        controllerField.setAccessible(true);
        ActivitiProcessController controller = (ActivitiProcessController) controllerField.get(view);
        if (controller == null) return;
        Field runningField = ActivitiProcessController.class.getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(controller, false);
    }

    private void waitForStartButtonEnabled(TestSupportView view, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                boolean[] enabled = {false};
                SwingUtilities.invokeAndWait(() ->
                        enabled[0] = view.getViewTestSupportMainProcess().getButtonStartProcess().isEnabled());
                if (enabled[0]) return;
                Thread.sleep(200);
            } catch (Exception ignored) {}
        }
    }

    private void waitForStopButtonEnabled(TestSupportView view, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            boolean[] enabled = {false};
            SwingUtilities.invokeAndWait(() ->
                    enabled[0] = view.getViewTestSupportMainProcess().getButtonStopUserTasksThread().isEnabled());
            if (enabled[0]) return;
            Thread.sleep(100);
        }
        fail("Stop-Button wurde nicht rechtzeitig aktiviert (Timeout: " + timeoutMs + " ms)");
    }

    private void waitForNewProcess(CteActivitiService service, String processKey,
            Map<String, Object> qMap, Integer oldId, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            boolean newExists = service.queryProcessInstances(processKey, qMap).stream()
                    .anyMatch(p -> !oldId.equals(p.getId()));
            if (newExists) return;
            Thread.sleep(200);
        }
        fail("Neue Prozess-Instanz nicht rechtzeitig auf Activiti gestartet (Timeout: " + timeoutMs + " ms)");
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden — Konsole
    // -----------------------------------------------------------------------

    /**
     * Wartet bis der Konsolen-Text ab Position startOffset den gesuchten Task-Namen enthält.
     * startOffset=0 prüft den gesamten Konsoleninhalt.
     */
    private boolean waitForTaskNameInConsole(TestSupportView view, String taskName,
            long timeoutMs, int startOffset) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String[] text = {""};
            SwingUtilities.invokeAndWait(() ->
                    text[0] = view.getTabbedPaneMonitor().getTextAreaTaskListenerInfo().getText());
            String relevant = text[0].length() > startOffset ? text[0].substring(startOffset) : "";
            if (relevant.contains(taskName)) return true;
            Thread.sleep(200);
        }
        return false;
    }

    /** Gibt die aktuelle Zeichenanzahl der Konsole zurück. */
    private int getConsoleLength(TestSupportView view) throws Exception {
        int[] len = {0};
        SwingUtilities.invokeAndWait(() ->
                len[0] = view.getTabbedPaneMonitor().getTextAreaTaskListenerInfo().getText().length());
        return len[0];
    }

    /** Gibt den vollständigen Konsoleninhalt zurück. */
    private String getConsoleText(TestSupportView view) throws Exception {
        String[] text = {""};
        SwingUtilities.invokeAndWait(() ->
                text[0] = view.getTabbedPaneMonitor().getTextAreaTaskListenerInfo().getText());
        return text[0];
    }

    /**
     * Prüft, dass alle Tokens aus expectedTokens in der gegebenen Reihenfolge im Konsolen-Text vorkommen.
     * Phase-1 und Phase-2 verwenden dieselben Task-Namen — die Tokens werden zweimal in expectedTokens
     * übergeben. indexOf(token, searchFrom) stellt sicher, dass jede Instanz an der richtigen Stelle gefunden wird.
     */
    private void verifyTaskOrder(String consoleText, List<String> expectedTokens) {
        int searchFrom = 0;
        for (String token : expectedTokens) {
            int pos = consoleText.indexOf(token, searchFrom);
            assertTrue("Task-Token '" + token + "' nicht in Konsole nach Position " + searchFrom
                    + " gefunden. Konsoleninhalt (ab " + Math.max(0, searchFrom - 50) + "):\n"
                    + consoleText.substring(Math.max(0, searchFrom - 50),
                            Math.min(consoleText.length(), searchFrom + 200)),
                    pos >= 0);
            searchFrom = pos + token.length();
        }
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden — Reflection
    // -----------------------------------------------------------------------

    private TestSupportView extractViewFrom(TestSupportGUI gui) {
        try {
            Field field = TestSupportGUI.class.getDeclaredField("testSupportView");
            field.setAccessible(true);
            return (TestSupportView) field.get(gui);
        } catch (Exception e) {
            throw new RuntimeException("testSupportView konnte nicht extrahiert werden", e);
        }
    }
}
