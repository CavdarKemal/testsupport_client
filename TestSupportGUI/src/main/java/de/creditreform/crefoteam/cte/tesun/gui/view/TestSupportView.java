package de.creditreform.crefoteam.cte.tesun.gui.view;

import de.creditreform.crefoteam.activiti.CteActivitiTask;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.activiti.handlers.UserTaskRunnable;
import de.creditreform.crefoteam.cte.tesun.gui.design.TestSupportPanel;
import de.creditreform.crefoteam.cte.tesun.gui.jvm.ManageJvmsDlgView;
import de.creditreform.crefoteam.cte.tesun.gui.model.TestJobsComboBoxItem;
import de.creditreform.crefoteam.cte.tesun.gui.utils.CommandExecutorListener;
import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIFrame;
import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;
import de.creditreform.crefoteam.cte.tesun.gui.utils.TestSupportHelper;
import de.creditreform.crefoteam.cte.tesun.jvm.ManageJvmsDlg;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentLockManager;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.apache.log4j.Level;

public class TestSupportView extends TestSupportPanel implements TesunClientJobListener, CommandExecutorListener {

    private static final String APP_TITLE = "CTE-Testautomatisierung";

    private ActivitiProcessController activitiController;
    private BufferedImage lastProcessImage;
    private final boolean resizeProcessImage = false;
    List<JComponent> componentsToOnOff;

    private final GUIFrame guiFrame;
    private TestSupportHelper testSupportHelper;
    private final TestResultsView viewTestResults;
    private EnvironmentConfig currentEnvironment;

    public TestSupportView(GUIFrame guiFrame) {
        super();
        this.guiFrame = guiFrame;
        currentEnvironment = guiFrame.getEnvironmentConfig();
        this.viewTestResults = getTabbedPaneMonitor().getViewTestResults();

        getViewTestSupportMainControls().init(
                this,
                this::doChangeComboBoxesHost,
                this::initForEnvironment,
                this::doManageJVMs,
                this::doChangeEnvironment);

        getViewTestSupportMainProcess().init(
                this::startActivitiProcess,
                this::stopActivitiProcess,
                this::startSelectedTestJob,
                this::doChangeTestResources,
                this::doChangeITSQRevision,
                this::doChangeTestType,
                () -> currentEnvironment.setLastUseOnlyTestClz(getViewTestSupportMainProcess().isUseOnlyTestCLZs()),
                currentEnvironment);

        componentsToOnOff = new ArrayList<>();
        componentsToOnOff.addAll(getViewTestSupportMainControls().getComponentsToOnOff());
        componentsToOnOff.addAll(getViewTestSupportMainProcess().getComponentsToOnOff());

        enableComponentsToOnOff(false);

        getSplitPaneMain().setDividerLocation(500);
        getTabbedPaneMonitor().getCheckBoxScrollToEnd().setSelected(true);

        initEnvironmentsComboBox();
        initITSQRevisionsComboBox();
        initTestSourcesComboBox();
        initTestTypesComboBox();
        initTestJobsCombo();
        initHostsFields();
        initTestPhasesComboBox();

        initForEnvironment();

        initListeners();
        activitiController = new ActivitiProcessController(this, this::initCustomers);
    }

    private void initListeners() {
        getTabbedPaneMonitor().addChangeListener(this::doTabChangeEvent);
        getTabbedPaneMonitor().getScrollPanelProcessImage().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                doResize();
            }
        });
    }

    private void stopActivitiProcess() {
        activitiController.stop();
        getViewTestSupportMainProcess().setStopButtonEnabled(false);
    }

    private void startSelectedTestJob() {
        startUserTaskRunnable(getViewTestSupportMainProcess().getSelectedTestJob());
    }

    protected void startActivitiProcess() {
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap =
                getViewCustomersSelection().getActiveTestCustomersMapMap();
        activeTestCustomersMapMap.keySet().forEach(testPhase ->
                activeTestCustomersMapMap.get(testPhase).values().forEach(TestCustomer::emptyTestResultsMapForCommands));

        CteActivitiTask cteActivitiTask;
        enableComponentsToOnOff(false);
        GUIStaticUtils.setWaitCursor(TestSupportView.this, true);
        try {
            cteActivitiTask = activitiController.prepareStart(testSupportHelper, currentEnvironment);
        } catch (RequestAbortedException abortEx) {
            return;
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(TestSupportView.this, "Fehler beim Vorbereiten von ACTIVITI-Prozess!", ex);
            return;
        } finally {
            GUIStaticUtils.setWaitCursor(TestSupportView.this, false);
            enableComponentsToOnOff(true);
        }

        // Capture GUI state on EDT before handing off to worker thread.
        final boolean isDemoMode = getViewTestSupportMainProcess().isDemoMode();
        final Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomers =
                getViewCustomersSelection().getActiveTestCustomersMapMap();
        final CteActivitiTask finalTask = cteActivitiTask;

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                GUIStaticUtils.setWaitCursor(TestSupportView.this, true);
                enableComponentsToOnOff(false);
                getViewTestSupportMainProcess().setStopButtonEnabled(true);
            });
            try {
                Map<String, Object> taskVariablesMap = setTaskVariablesMap(isDemoMode, activeCustomers);
                activitiController.runProcess(testSupportHelper, currentEnvironment, taskVariablesMap, activeCustomers, finalTask);
            } catch (Exception ex) {
                notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(TestSupportView.this, "Fehler beim Starten des Activiti-Prozesses!", ex));
                SwingUtilities.invokeLater(() -> {
                    enableComponentsToOnOff(true);
                    GUIStaticUtils.setWaitCursor(TestSupportView.this, false);
                });
            }
        }).start();
    }

    private void doChangeComboBoxesHost() {
        try {
            testSupportHelper = getTestSupportHelper();
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Wechseln des Hosts!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void doTabChangeEvent(ChangeEvent changeEvent) {
        JTabbedPane jTabbedPane = (JTabbedPane) changeEvent.getSource();
        Component selectedComponent = jTabbedPane.getSelectedComponent();
        if (selectedComponent instanceof TestResultsView) {
            Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = getAndCheckActiveCustomers();
            viewTestResults.refreshTestResultsForMap(activeTestCustomersMapMap, false);
        }
    }

    private void initTestTypesComboBox() {
        try {
            getViewTestSupportMainProcess().initTestTypesComboBox(currentEnvironment);
            getViewCustomersSelection().setTestCustomersTableModelMap(new HashMap<>());
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Test-Typen!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void initTestPhasesComboBox() {
        getViewTestSupportMainProcess().initTestPhasesComboBox();
    }

    private void initTestJobsCombo() {
        getViewTestSupportMainProcess().initTestJobsCombo();
    }

    private void initHostsFields() {
        try {
            getViewTestSupportMainControls().initHostsFields(currentEnvironment);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Hosts!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void initTestSourcesComboBox() {
        try {
            getViewTestSupportMainProcess().initTestSourcesComboBox(currentEnvironment);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Test-Sourcen!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void initITSQRevisionsComboBox() {
        try {
            getViewTestSupportMainProcess().initITSQRevisionsComboBox(currentEnvironment);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der ITSQ-Revisions!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void initEnvironmentsComboBox() {
        Map<String, File> environmentsMap = currentEnvironment.getEnvironmentsMap();
        if ((environmentsMap == null) || (environmentsMap.isEmpty())) {
            String exceptionErr = "Es konnten im aktuellen Verzeichnis '" + System.getProperty("user.dir") + "'\nkeine Konfigurationsdateien '{ENE|GEE|ABE}-config.properties' gefunden werden!";
            notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(this, "Konfiguration laden", new RuntimeException(exceptionErr)));
            System.exit(-1);
        }
        getViewTestSupportMainControls().initEnvironmentsComboBox(currentEnvironment);
    }

    private void doChangeEnvironment() {
        String newEnv = getViewTestSupportMainControls().getSelectedEnvironmentName();
        try {
            EnvironmentConfig environmentConfig = new EnvironmentConfig(newEnv);
            checkEnvionmentLock(environmentConfig, newEnv);
            TestSupportClientKonstanten.TEST_TYPES selectedTestType = getViewTestSupportMainProcess().getSelectedTestType();
            if (!TimelineLogger.configure(
                    currentEnvironment.getLogOutputsRootForEnv(getViewTestSupportMainControls().getSelectedEnvironmentName()),
                    (selectedTestType + ".log"), "TimeLine.log")) {
                notifyClientJob(Level.ERROR, "Exception beim Konfigurieren der LOG-Dateien!\n");
            }
            String testSetSource = getViewTestSupportMainProcess().getSelectedTestSource();
            environmentConfig.setLastTestSource(testSetSource);
            environmentConfig.setLastItsqRevision(getViewTestSupportMainProcess().getSelectedITSQRevision());
            environmentConfig.setLastTestType(selectedTestType);
            environmentConfig.setLastUseOnlyTestClz(getViewTestSupportMainProcess().isUseOnlyTestCLZs());
            environmentConfig.setLastUploadSynthetics(getViewTestSupportMainProcess().isUploadSynthetics());
            guiFrame.setEnvironmentConfig(environmentConfig);
            currentEnvironment = guiFrame.getEnvironmentConfig();
            initForEnvironment();
        } catch (Exception ex) {
            RuntimeException runtimeException = new RuntimeException(("Exception beim Initialisieren der Umgebung " + newEnv + "!"), ex);
            notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(this, "Initialisierung", runtimeException));
            getViewTestSupportMainControls().setSelectedEnvironment(currentEnvironment.getCurrentEnvName());
        }
    }

    private static void checkEnvionmentLock(EnvironmentConfig environmentConfig, String newEnv) throws PropertiesException {
        File lockDir = environmentConfig.getLogOutputsRootForEnv(newEnv);
        if (EnvironmentLockManager.isLocked(lockDir)) {
            throw new RuntimeException("Die Umgebung " + environmentConfig.getCurrentEnvName() + " ist gesperrt, da eine andere Instanz in dieser Umgebung läuft.");
        }
        EnvironmentLockManager.releaseLock();
        if (!EnvironmentLockManager.acquireLock(lockDir, environmentConfig.getCurrentEnvName())) {
            throw new RuntimeException("Konnte Lock für die Umgebung " + environmentConfig.getCurrentEnvName() + " nicht erwerben!");
        }
    }

    public void initForEnvironment() {
        enableComponentsToOnOff(false);
        GUIStaticUtils.setWaitCursor(TestSupportView.this, true);
        viewTestResults.setEnvironmentConfig(currentEnvironment);
        notifyClientJob(Level.INFO, String.format("\nInitialisiere für die Umgebung %s...", currentEnvironment.getCurrentEnvName()));
        getTabbedPaneMonitor().getTextAreaTaskListenerInfo().setText("");
        initHostsFields();
        new Thread(() -> {
            try {
                notifyClientJob(Level.INFO, String.format("\nInitialisiere Test-Resourcen für die Umgebung %s...",
                        getViewTestSupportMainControls().getSelectedEnvironmentName()));
                testSupportHelper = getTestSupportHelper();
/* CLAUDE_MODE
                TesunSystemInfo tesunSystemInfo = testSupportHelper.getTesunRestServiceWLS().getTesunSystemInfo();
                String versionsInfoInTitle = String.format("[ %s ] - [ CTE-Version: %s ]", currentEnvironment.getAppVersionsInfo(), tesunSystemInfo.getCteVersion());
                guiFrame.setVersionsInfoInTitle(versionsInfoInTitle);
*/
                initCustomers();
            } catch (Throwable ex) {
                notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(TestSupportView.this, "Konfiguration laden", ex));
            } finally {
                enableComponentsToOnOff(true);
                SwingUtilities.invokeLater(() -> GUIStaticUtils.setWaitCursor(TestSupportView.this, false));
            }
        }).start();
    }

    private TestSupportHelper getTestSupportHelper() throws Exception {
        RestInvokerConfig activitiConfig     = getViewTestSupportMainControls().getSelectedActivitiConfig();
        RestInvokerConfig restServicesConfig = getViewTestSupportMainControls().getSelectedRestServicesConfig();
        RestInvokerConfig impCycleConfig     = getViewTestSupportMainControls().getSelectedImpCycleConfig();
        if (activitiConfig == null || restServicesConfig == null || impCycleConfig == null) {
            return null;
        }
        return new TestSupportHelper(currentEnvironment, activitiConfig, restServicesConfig, impCycleConfig, TestSupportView.this);
    }

    private void initCustomers() {
        try {
            SwingUtilities.invokeLater(() -> {
                GUIStaticUtils.setWaitCursor(this, true);
                enableComponentsToOnOff(false);
                getViewTestSupportMainProcess().setTestCasesPath("");
                getViewCustomersSelection().setTestCustomersTableModelMap(new HashMap<>());
            });
            checkAndSetTestsSource(getViewTestSupportMainProcess().getSelectedTestSource());
            initTestCasesForCustomers();
        } catch (Exception ex) {
            notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(TestSupportView.this, "Quelle für ITSQ-Revision ändern", ex));
        } finally {
            SwingUtilities.invokeLater(() -> {
                GUIStaticUtils.setWaitCursor(this, false);
                enableComponentsToOnOff(true);
            });
        }
    }

    private void checkAndSetTestsSource(String testSetSource) throws Exception {
        if (testSetSource == null) return;
        File sourceDir = new File(currentEnvironment.getTestResourcesRoot(), testSetSource);
        if (sourceDir.exists()) {
            currentEnvironment.setTestResourcesDir(sourceDir);
            String testCasesPath = currentEnvironment.getItsqRefExportsRoot().getAbsolutePath();
            currentEnvironment.setLastTestSource(testSetSource);
            SwingUtilities.invokeLater(() -> getViewTestSupportMainProcess().setTestCasesPath(testCasesPath));
        } else {
            GUIStaticUtils.showExceptionMessage(this, "", new RuntimeException("\nDie selektierte Quelle " + sourceDir + " existiert nicht!\nBitte andere Quelle wählen."));
        }
    }

    private void initTestCasesForCustomers() throws Exception {
        notifyClientJob(Level.INFO, "\n\tLese die Test-Crefos-Konfiguration aus dem ITSQ-Verzeichnis...");
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customerTestInfoMapMap = currentEnvironment.getCustomerTestInfoMapMap();
        notifyClientJob(Level.INFO, "\n\tErmittle TesunConfigInfo für die Kunden...");
        TesunRestService tesunRestServiceWLS = testSupportHelper.getTesunRestServiceWLS();
/* CLAUDE_MODE
        SystemInfo systemInfo = tesunRestServiceWLS.getSystemPropertiesInfo();
*/
        notifyClientJob(Level.INFO, "\n\tErmittle KundenKonfigList für die Kunden...");
        Iterator<TestSupportClientKonstanten.TEST_PHASE> iterator = customerTestInfoMapMap.keySet().iterator();
        while (iterator.hasNext()) {
            TestSupportClientKonstanten.TEST_PHASE testPhase = iterator.next();
            Map<String, TestCustomer> testCustomerMap = customerTestInfoMapMap.get(testPhase);
            notifyClientJob(Level.INFO, "\n" + testCustomerMap.size() + " Kunden sind für den Test in " + testPhase + " ausgewählt.");
            testCustomerMap.entrySet().forEach(testCustomerEntry -> {
                try {
                    TestCustomer testCustomer = testCustomerEntry.getValue();
                    notifyClientJob(Level.INFO, "\n\t\tInitialisiere Testfälle des Kunden für " + testCustomer.getCustomerName() + " aus " + testPhase);
/* CLAUDE_MODE
                    tesunRestServiceWLS.extendTestCustomerProperiesInfos(testCustomer, systemInfo);
*/
                } catch (Exception ex) {
                    notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(TestSupportView.this, "Konfiguration vervollständigen", ex));
                }
            });
            TesunUtilites.dumpCustomers(currentEnvironment.getLogOutputsRoot(), "INIT-" + testPhase.name(), testCustomerMap);
        }
        SwingUtilities.invokeLater(() -> getViewCustomersSelection().setTestCustomersTableModelMap(customerTestInfoMapMap));
    }

    private void doManageJVMs() {
        GUIStaticUtils.setWaitCursor(this, true);
        ManageJvmsDlg theView = new ManageJvmsDlgView(GUIStaticUtils.getParentFrame(this), "Verfügbare JVMs und Jobs", currentEnvironment);
        GUIStaticUtils.warteBisken(1000);
        theView.setVisible(true);
        GUIStaticUtils.setWaitCursor(this, false);
    }

    private Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> getAndCheckActiveCustomers() {
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> resultMap = new HashMap<>();
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap =
                getViewCustomersSelection().getActiveTestCustomersMapMap();
        if (activeTestCustomersMapMap.isEmpty()) {
            RuntimeException ex = new RuntimeException("Es sind keine Kunden aktiviert!\nBitte zuerst mindestens einen Kunden mit mindestens einen Terst-Scenario aktivieren.");
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Kunden!", ex);
            throw ex;
        }
        Map<String, TestCustomer> selectedCustomersMapPhase1 = activeTestCustomersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Map<String, TestCustomer> selectedCustomersMapPhase2 = activeTestCustomersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        if (getViewTestSupportMainProcess().getSelectedTestType().equals(TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2)) {
            selectedCustomersMapPhase1.keySet().forEach(customerKey -> {
                TestCustomer testCustomer = selectedCustomersMapPhase2.get(customerKey);
                if (testCustomer == null || !testCustomer.isActivated()) {
                    RuntimeException ex = new RuntimeException("Kunde " + customerKey + " aus der PHASE-1 muss auch für PHASE-2 selektiert werden!");
                    GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren Kunden!", ex);
                    throw ex;
                }
            });
            resultMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, selectedCustomersMapPhase1);
            resultMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, selectedCustomersMapPhase2);
        } else {
            Iterator<Map.Entry<String, TestCustomer>> it = selectedCustomersMapPhase2.entrySet().iterator();
            while (it.hasNext()) {
                TestCustomer testCustomer = it.next().getValue();
                if (testCustomer.isActivated()) {
                    break;
                }
            }
            if (!it.hasNext()) {
                RuntimeException ex = new RuntimeException("PHASE-2 muss mindestens einen aktiven Kunden haben!");
                GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Kunden!", ex);
                throw ex;
            }
            resultMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, selectedCustomersMapPhase2);
        }
        return resultMap;
    }

    private void startUserTaskRunnable(final TestJobsComboBoxItem testJobsComboBoxItem) {
        try {
            GUIStaticUtils.setWaitCursor(this, true);
            enableComponentsToOnOff(false);
            final boolean isDemoMode = getViewTestSupportMainProcess().isDemoMode();
            new Thread(() -> {
                try {
                    Map<String, Object> lastUserTaskVariablesMap = new HashMap<>();
                    for (String jobName : testJobsComboBoxItem.getTestJobNamesList()) {
                        String className = "de.creditreform.crefoteam.cte.tesun.activiti.handlers." + jobName;
                        Class<UserTaskRunnable> userTaskRunnableClass = (Class<UserTaskRunnable>) Class.forName(className);
                        Constructor<UserTaskRunnable> constructor = userTaskRunnableClass.getConstructor(EnvironmentConfig.class, TesunClientJobListener.class);
                        final UserTaskRunnable userTaskRunnable = constructor.newInstance(currentEnvironment, TestSupportView.this);
                        Map<String, Object> taskVariablesMap = setTaskVariablesMap(isDemoMode, getViewCustomersSelection().getActiveTestCustomersMapMap());
                        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MANUEL_USER_TASK, Boolean.TRUE);
                        taskVariablesMap.putAll(lastUserTaskVariablesMap);
                        taskVariablesMap.putAll(testJobsComboBoxItem.getTaskVariablesMap());
                        lastUserTaskVariablesMap = userTaskRunnable.runTask(taskVariablesMap);
                    }
                } catch (Exception ex) {
                    notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(TestSupportView.this,
                            String.format("\nFehler beim Start des User-Tasks %s!", testJobsComboBoxItem.getTestJobNamesList()), ex));
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        GUIStaticUtils.setWaitCursor(TestSupportView.this, false);
                        enableComponentsToOnOff(true);
                    });
                }
            }).start();
        } catch (Exception ex) {
            notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(this, "UserTask starten!", ex));
            enableComponentsToOnOff(true);
            GUIStaticUtils.setWaitCursor(this, false);
        }
    }

    // Fix: receives pre-captured GUI values — no longer reads Swing state from worker thread.
    private Map<String, Object> setTaskVariablesMap(
            boolean isDemoMode,
            Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomers) throws PropertiesException {
        Map<String, Object> taskVariablesMap = new HashMap<>();
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE, isDemoMode);
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY, currentEnvironment.getActivitProcessKey());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME, currentEnvironment.getActivitiProcessName());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_BTLG_IMPORT, currentEnvironment.getMillisBeforeBtlgImport(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_CT_IMPORT, currentEnvironment.getMillisBeforeCtImport(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_EXPORT, currentEnvironment.getMillisBeforeExports(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_EXPORTS_COLLECT, currentEnvironment.getMillisBeforeCollectExports(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_SFTP_COLLECT, currentEnvironment.getMillisBeforeCollectSftpUploads(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_EMAIL_FROM, currentEnvironment.getActivitiEmailFrom());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_SUCCESS_EMAIL_TO, currentEnvironment.getActivitiSuccessEmailTo());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_FAILURE_EMAIL_TO, currentEnvironment.getActivitiFailureEmailTo());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS, activeCustomers);
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_TYPE, getViewTestSupportMainProcess().getSelectedTestType());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE, getViewTestSupportMainProcess().getSelectedTestPhase());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_USE_ONLY_TEST_CLZ, getViewTestSupportMainProcess().isUseOnlyTestCLZs());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_UPLOAD_SYNTH_TEST_CREFOS, getViewTestSupportMainProcess().isUploadSynthetics());
        return taskVariablesMap;
    }

    private void doResize() {
        JLabel jLabel = (JLabel) getTabbedPaneMonitor().getScrollPanelProcessImage().getViewport().getComponent(0);
        if (resizeProcessImage && (lastProcessImage != null)) {
            try {
                Dimension scaledDimension = testSupportHelper.getScaledDimension(jLabel, lastProcessImage);
                Image resizedImage = lastProcessImage.getScaledInstance((int) scaledDimension.getWidth(), (int) scaledDimension.getHeight(), Image.SCALE_DEFAULT);
                jLabel.setIcon(new ImageIcon(resizedImage));
            } catch (Exception ex) {
                notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(TestSupportView.this, "Fehler!", ex));
            }
        }
    }

    private void doChangeTestResources() {
        new Thread(() -> {
            enableComponentsToOnOff(false);
            SwingUtilities.invokeLater(() -> GUIStaticUtils.setWaitCursor(TestSupportView.this, true));
            try {
                initCustomers();
                String testSetSource = getViewTestSupportMainProcess().getSelectedTestSource();
                currentEnvironment.setLastTestSource(testSetSource);
            } catch (Exception ex) {
                notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(TestSupportView.this, "Quelle für Test-Resourcen ändern", ex));
            } finally {
                SwingUtilities.invokeLater(() -> GUIStaticUtils.setWaitCursor(TestSupportView.this, false));
                enableComponentsToOnOff(true);
            }
        }).start();
    }

    private void doChangeITSQRevision() {
        // Fix: added finally block to always re-enable after disable
        new Thread(() -> {
            enableComponentsToOnOff(false);
            SwingUtilities.invokeLater(() -> GUIStaticUtils.setWaitCursor(TestSupportView.this, true));
            try {
                initCustomers();
            } finally {
                SwingUtilities.invokeLater(() -> GUIStaticUtils.setWaitCursor(TestSupportView.this, false));
                enableComponentsToOnOff(true);
            }
        }).start();
    }

    protected void doChangeTestType() {
        try {
            TestSupportClientKonstanten.TEST_TYPES selectedTestType = getViewTestSupportMainProcess().getSelectedTestType();
            if (!TimelineLogger.configure(
                    currentEnvironment.getLogOutputsRootForEnv(getViewTestSupportMainControls().getSelectedEnvironmentName()),
                    (selectedTestType + ".log"), "TimeLine.log")) {
                notifyClientJob(Level.ERROR, "Exception beim Konfigurieren der LOG-Dateien!\n");
            }
            notifyClientJob(Level.INFO, String.format("\nInitialisiere für den Test-Typ %s...", selectedTestType.getDescription()));
            getViewTestSupportMainProcess().initTestJobsCombo();
        } catch (Exception ex) {
            RuntimeException runtimeException = new RuntimeException("Exception beim Wechseln des Test-Typs!", ex);
            notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(this, "Initialisierung", runtimeException));
        }
    }

    /**
     * Enables or disables all managed components.
     * Fix 1: Thread-safe — dispatches to EDT if called from a worker thread.
     * Fix 2: NPE-safe — null-checks getSelectedItem() before calling equals().
     * Fix 3: No longer throws RuntimeException — logs instead, so finally-blocks
     *         in worker threads are not disrupted by a GUI state error.
     */
    private void enableComponentsToOnOff(boolean enable) {
        Runnable task = () -> {
            for (JComponent component : componentsToOnOff) {
                component.setEnabled(enable);
            }
            if (enable) {
                try {
                    getViewTestSupportMainControls().updateAdminButtonState(currentEnvironment);
                    getViewTestSupportMainProcess().updateITSQRevisionEnabled();
                } catch (Exception ex) {
                    TimelineLogger.error(getClass(), "Fehler beim Aktivieren der GUI-Elemente", ex);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    private String createErrorFilesInfo(List<File> errorTxtFiles) {
        StringBuilder strErrBuilder = new StringBuilder("Folgende Fehlerdateien wurden erstellt:");
        for (File errorTxtFile : errorTxtFiles) {
            strErrBuilder.append("\n\t->" + errorTxtFile.getAbsolutePath());
        }
        strErrBuilder.append("\nBitte überprüfen und OK wenn trotzdem weiter?");
        return strErrBuilder.toString();
    }

    private void appendToConsole(String message) {
        if (message == null) return;
        final String forFile = message.startsWith("\n") ? message.substring(1) : message;
        TimelineLogger.info(this.getClass(), forFile);
        SwingUtilities.invokeLater(() -> {
            getTabbedPaneMonitor().getTextAreaTaskListenerInfo().append(message.replaceAll("\t", "  "));
            if (getTabbedPaneMonitor().getCheckBoxScrollToEnd().isSelected()) {
                getTabbedPaneMonitor().getTextAreaTaskListenerInfo().setCaretPosition(getTabbedPaneMonitor().getTextAreaTaskListenerInfo().getDocument().getLength() - 1);
            }
        });
    }

    /***********************************************************************************************************/
    /***************************************** TesunClientJobListener ******************************************/

    /**
     * Fix: all Swing operations are now safely dispatched to the EDT.
     * This method may be called from any thread (EDT or Activiti worker threads).
     */
    @Override
    public void notifyClientJob(Level level, Object notifyObject) {
        if (notifyObject instanceof CteActivitiTask) {
            CteActivitiTask userTask = (CteActivitiTask) notifyObject;
            Object testPhaseObj = userTask.getVariables().get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
            String msg = String.format(
                    "\n\nUser-Task:\n\tName : %s\n\tTest-Phase : %s\n\tID : %d\n\tProcessDefinition : %s\n\tProcessInstance : %d",
                    userTask.getName(),
                    testPhaseObj != null ? testPhaseObj.toString() : "?",
                    userTask.getId(), userTask.getProcessDefinitionId(), userTask.getProcessInstanceId());
            appendToConsole(msg);

        } else if (notifyObject instanceof InputStream) {
            final InputStream inputStream = (InputStream) notifyObject;
            SwingUtilities.invokeLater(() -> {
                try {
                    JLabel jLabel = (JLabel) getTabbedPaneMonitor().getScrollPanelProcessImage().getViewport().getComponent(0);
                    lastProcessImage = testSupportHelper.refreshProcessImage(inputStream, jLabel, resizeProcessImage);
                } catch (Exception ex) {
                    GUIStaticUtils.showExceptionMessage(this, "Fehler beim Erzeugen des Bitmaps!", ex);
                }
            });

        } else if (notifyObject instanceof String) {
            String msg = (String) notifyObject;
            if (!msg.equals(".")) {
                appendToConsole(msg);
            }

        } else if (notifyObject == null) {
            TimelineLogger.info(this.getClass(), "===========    Activiti-Process beendet.    ===========");
            String msg = "\n***********    UserTasks-Thread beendet.    ***********\n===========    Activiti-Process beendet.    ===========\nTest-Results sind im Output-Ordner gespeichert";
            activitiController.stop();
/* CLAUDE_MODE
            Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = getAndCheckActiveCustomers();
            viewTestResults.refreshTestResultsForMap(activeTestCustomersMapMap, true);
*/
            TimelineLogger.info(TestSupportView.class, msg);
            final String finalMsg = msg;
            SwingUtilities.invokeLater(() -> {
                getTabbedPaneMonitor().getTextAreaTaskListenerInfo().append(finalMsg.replaceAll("\t", "  "));
                if (getTabbedPaneMonitor().getCheckBoxScrollToEnd().isSelected()) {
                    getTabbedPaneMonitor().getTextAreaTaskListenerInfo().setCaretPosition(getTabbedPaneMonitor().getTextAreaTaskListenerInfo().getDocument().getLength() - 1);
                }
                enableComponentsToOnOff(true);
                GUIStaticUtils.setWaitCursor(this, false);
            });

        } else if (notifyObject instanceof Exception) {
            appendToConsole(((Exception) notifyObject).getMessage());

        } else {
            appendToConsole("?! Unbekanntes Notify-Objekt !?");
        }
    }

    @Override
    public Object askClientJob(TesunClientJobListener.ASK_FOR askFor, Object userObject) {
        try {
            if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_RETRY)) {
                return (GUIStaticUtils.showConfirmDialog(this, (userObject.toString() + "\nErneut versuchen?"), APP_TITLE));
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CONTINUE)) {
                return (GUIStaticUtils.showConfirmDialog(this, userObject.toString(), APP_TITLE, JOptionPane.YES_NO_CANCEL_OPTION));
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CTE_VERSION)) {
                return Integer.valueOf(currentEnvironment.getCteVersion());
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_TEST_TYPE)) {
                return getViewTestSupportMainProcess().getSelectedTestType();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_USE_ITSQ_TEST_RESOURCES)) {
                return getViewTestSupportMainProcess().getSelectedTestSource();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_REF_EXPORTS_PATH)) {
                File tmpFile = new File(getViewTestSupportMainProcess().getTestCasesPath());
                tmpFile = new File(tmpFile.getParentFile(), TestSupportClientKonstanten.REF_EXPORTS);
                return tmpFile.getAbsolutePath();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_TEST_CASES_PATH)) {
                return GUIStaticUtils.chooseDirectory(this, currentEnvironment.getItsqRefExportsRoot().getAbsolutePath(), "Verzeichnis für die Testfälle angeben");
            } else if (askFor.equals(ASK_FOR.ASK_NEW_TEST_CASES_PATH)) {
                return getViewTestSupportMainProcess().getTestCasesPath();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CHECK_DOWNLOADS)) {
                String strErr = createErrorFilesInfo((List<File>) userObject);
                return (JOptionPane.showConfirmDialog(this, ("Fehler bei Download!\n" + strErr), APP_TITLE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_WAIT_FOR_TEST_SYSTEM)) {
                return (JOptionPane.showConfirmDialog(this, ("Sind die Testfälle im Test-System abgearbeitet?"), APP_TITLE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_COPY_EXPORTS_TO_INPUTS)) {
                String strInfo = String.format("Bitte die Kunden-Exporte nach \n\t%s\nim jewewiligen Unterverzeichnis kopieren...", currentEnvironment.getItsqRefExportsRoot());
                strInfo += String.format("\nz.B für 'bvd' das Verzeichnis\n'y:/bvd/export/delta/2016-02-23_15-28'\nnach\n'%s/EXPORTS/bvd/export/delta/'\nkopieren.", currentEnvironment.getItsqRefExportsRoot());
                JOptionPane.showMessageDialog(this, strInfo, APP_TITLE, JOptionPane.INFORMATION_MESSAGE);
                return Boolean.TRUE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CREATE_NEW_SOLL)) {
                return (JOptionPane.showConfirmDialog(this, ("Sollen neue SOLL Dateien generiert werden?"), APP_TITLE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_ANAYLSE_CHECKS)) {
                JOptionPane.showMessageDialog(this, "Bitte Check-Ergebnisse prüfen...", APP_TITLE, JOptionPane.INFORMATION_MESSAGE);
                return Boolean.TRUE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_CHECK_COLLECTS)) {
                String strErr = createErrorFilesInfo((List<File>) userObject);
                return (JOptionPane.showConfirmDialog(this, ("Fehler bei Collect!\n" + strErr), APP_TITLE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_EXCEPTION)) {
                Throwable throwable = (Throwable) userObject;
                String errMsg = TesunUtilites.buildExceptionMessage(throwable, 10);
                String s = errMsg != null ? errMsg.replaceAll("\\n", "") : throwable.getClass().getName();
                String strLog = "\n!!!\n\t" + s + "\n!!!\n";
                TimelineLogger.error(this.getClass(), strLog);
                SwingUtilities.invokeLater(() -> {
                    getTabbedPaneMonitor().getTextAreaTaskListenerInfo().append(strLog.replaceAll("\t", "  "));
                    if (getTabbedPaneMonitor().getCheckBoxScrollToEnd().isSelected()) {
                        getTabbedPaneMonitor().getTextAreaTaskListenerInfo().setCaretPosition(getTabbedPaneMonitor().getTextAreaTaskListenerInfo().getDocument().getLength() - 1);
                    }
                });
                return Boolean.TRUE;
            } else {
                throw new PropertiesException("Unbekannte Rückfrage: " + askFor + "!");
            }
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "! Fehler !", ex);
        }
        return null;
    }

    /***************************************** TesunClientJobListener ******************************************/

    /***************************************** CommandExecutorListener *****************************************/
    @Override
    public void progress(String strInfo) {
        notifyClientJob(Level.INFO, strInfo);
    }
    /***************************************** CommandExecutorListener *****************************************/
}
