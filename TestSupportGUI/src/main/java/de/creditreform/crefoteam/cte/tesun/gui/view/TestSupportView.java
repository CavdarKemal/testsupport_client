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
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
        this.viewTestResults = getViewTestResults();
        componentsToOnOff = new ArrayList<>();
        componentsToOnOff.add(getComboBoxTestSource());
        componentsToOnOff.add(getCheckBoxUseOnlyTestCLZs());
        componentsToOnOff.add(getComboBoxITSQRevision());
        componentsToOnOff.add(getComboBoxActivitiHost());
        componentsToOnOff.add(getComboBoxRestServicesHost());
        componentsToOnOff.add(getComboBoxBatchGUIHost());
        componentsToOnOff.add(getComboBoxImpCycleHost());
        componentsToOnOff.add(getComboBoxInsoHost());
        componentsToOnOff.add(getComboBoxInsoBackEndHost());
        componentsToOnOff.add(getComboBoxTestPhase());
        componentsToOnOff.add(getButtonManageJVMs());
        componentsToOnOff.add(getButtonStopUserTasksThread());
        componentsToOnOff.add(getButtonStartTestJob());
        componentsToOnOff.add(getButtonStartProcess());
        componentsToOnOff.add(getComboBoxTestJobs());
        componentsToOnOff.add(getTextFieldJobParams());
        componentsToOnOff.add(getComboBoxTestType());
        componentsToOnOff.add(getComboBoxEnvironment());       // Fix: was added after disable
        componentsToOnOff.add(getButtonRefreshEnvironment());  // Fix: was added after disable
        componentsToOnOff.add(getCheckBoxDemoMode());          // Fix: was added after disable
        componentsToOnOff.add(getCheckBoxUploadSynthetics());  // Fix: was added after disable
        enableComponentsToOnOff(false);

        getLabelFachwertConfig().setVisible(false);
        getRadioButtonFWConfigNewest().setVisible(false);
        getRadioButtonFWConfigLikePRE().setVisible(false);
        getLabelExportFormat().setVisible(false);
        getRadioButtonExportFormatNewest().setVisible(false);
        getRadioButtonExportFormatLikePRE().setVisible(false);

        getSplitPaneMain().setDividerLocation(500);
        getCheckBoxScrollToEnd().setSelected(true);
        getCheckBoxDemoMode().setSelected(true); // CLAUDE_MODE
        getCheckBoxDemoMode().setEnabled(false); // CLAUDE_MODE

        initEnvironmentsComboBox();
        initITSQRevisionsComboBox();
        initTestSourcesComboBox();
        initTestTypesComboBox();
        initTestJobsCombo();
        initHostsFields();
        initTestPhasesComboBox();

        initForEnvironment();

        getCheckBoxUseOnlyTestCLZs().setSelected(currentEnvironment.isLastUseOnlyTestClz());
        getCheckBoxUploadSynthetics().setSelected(currentEnvironment.isLastUploadSynthetics());

        initListeners();
        activitiController = new ActivitiProcessController(this, this::initCustomers);
    }

    private void initListeners() {
        getTabbedPaneMonitor().addChangeListener(this::doTabChangeEvent);
        getScrollPanelProcessImage().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                doResize();
            }
        });
        getCheckBoxUseOnlyTestCLZs().addActionListener(e -> currentEnvironment.setLastUseOnlyTestClz(getCheckBoxUseOnlyTestCLZs().isSelected()));
        getCheckBoxUploadSynthetics().addActionListener(e -> currentEnvironment.setLastUploadSynthetics(getCheckBoxUploadSynthetics().isSelected()));
        getComboBoxEnvironment().addActionListener(e -> doChangeEnvironment());
        getComboBoxActivitiHost().addActionListener(e -> doChangeComboBoxesHost());
        getComboBoxImpCycleHost().addActionListener(e -> doChangeComboBoxesHost());
        getComboBoxRestServicesHost().addActionListener(e -> doChangeComboBoxesHost());
        getComboBoxTestSource().addActionListener(e -> doChangeTestResources());
        getComboBoxITSQRevision().addActionListener(e -> doChangeITSQRevision());
        getComboBoxTestType().addActionListener(e -> doChangeTestType());
        getComboBoxTestJobs().addActionListener(e -> doChangeTestJob());
        getButtonRefreshEnvironment().addActionListener(e -> initForEnvironment());
        getButtonManageJVMs().addActionListener(e -> doManageJVMs());
        getButtonStartProcess().addActionListener(e -> startActivitiProcess());
        getButtonStopUserTasksThread().addActionListener(e -> stopActivitiProcess());
        getButtonStartTestJob().addActionListener(e -> {
            TestJobsComboBoxItem testJobsComboBoxItem = (TestJobsComboBoxItem) getComboBoxTestJobs().getSelectedItem();
            startUserTaskRunnable(testJobsComboBoxItem);
        });
        getButtonClearLOGPanel().addActionListener(e -> getTextAreaTaskListenerInfo().setText(""));
    }

    private void stopActivitiProcess() {
        activitiController.stop();
        getButtonStopUserTasksThread().setEnabled(false);
    }

    protected void startActivitiProcess() {
        // Reset test customer results
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = getViewcustomersSelection().getActiveTestCustomersMapMap();
        activeTestCustomersMapMap.keySet().forEach(testPhase -> activeTestCustomersMapMap.get(testPhase).values().forEach(TestCustomer::emptyTestResultsMapForCommands));

        // Sync part on EDT: check Activiti status, deploy or ask to continue
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
            // Fix: enableComponentsToOnOff no longer throws — safe to call in finally.
            enableComponentsToOnOff(true);
        }

        // Capture GUI state on EDT before handing off to worker thread.
        // Fix: previously setTaskVariablesMap() was called from the worker thread,
        // reading Swing component state from a non-EDT thread.
        final boolean isDemoMode = getCheckBoxDemoMode().isSelected();
        final Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomers =
                getViewcustomersSelection().getActiveTestCustomersMapMap();
        final CteActivitiTask finalTask = cteActivitiTask;

        new Thread(() -> {
            // Fix: all Swing calls dispatched to EDT.
            SwingUtilities.invokeLater(() -> {
                GUIStaticUtils.setWaitCursor(TestSupportView.this, true);
                enableComponentsToOnOff(false);
                getButtonStopUserTasksThread().setEnabled(true);
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
            List<TestSupportClientKonstanten.TEST_TYPES> testTypesList = currentEnvironment.getTestTypes();
            DefaultComboBoxModel testTypesModel = new DefaultComboBoxModel();
            for (TestSupportClientKonstanten.TEST_TYPES testType : testTypesList) {
                testTypesModel.addElement(testType);
            }
            getComboBoxTestType().setModel(testTypesModel);
            getComboBoxTestType().setSelectedItem(currentEnvironment.getLastTestType());
            getViewcustomersSelection().setTestCustomersTableModelMap(new HashMap<>());
        } catch (PropertiesException ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Test-Typen!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void initTestPhasesComboBox() {
        getComboBoxTestPhase().setModel(new DefaultComboBoxModel());
        getComboBoxTestPhase().addItem(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        getComboBoxTestPhase().addItem(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        getComboBoxTestPhase().setSelectedItem(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
    }

    private void initTestJobsCombo() {
        List<TestJobsComboBoxItem> testJobsList = new ArrayList<>();
        // testJobsList.add(new TestJobsComboBoxItem("1:Extend Pseudo-Crefos", Collections.singletonList("UserTaskExtendsArchivBestandCrefos")));
        testJobsList.add(new TestJobsComboBoxItem("2:Generate Pseudo-Crefos", Collections.singletonList("UserTaskGeneratePseudoCrefos")));
        testJobsList.add(new TestJobsComboBoxItem("3:Systemeinstellungen setzen", Collections.singletonList("UserTaskPrepareTestSystem")));
        testJobsList.add(new TestJobsComboBoxItem("4:Upload nach STAGING", Collections.singletonList("UserTaskStartUploads")));

        testJobsList.add(new TestJobsComboBoxItem("6:Beteiligten-Import starten", Arrays.asList("UserTaskStartBeteiligtenImport", "UserTaskWaitForBeteiligtenImport")));
        testJobsList.add(new TestJobsComboBoxItem("8:ENTG-Berechnung starten", Arrays.asList("UserTaskStartEntgBerechnung", "UserTaskWaitForEntgBerechnung")));
        testJobsList.add(new TestJobsComboBoxItem("10:BTLG-Aktualisierung starten", Arrays.asList("UserTaskStartBtlgAktualisierung", "UserTaskWaitForBtlgAktualisierung")));
        testJobsList.add(new TestJobsComboBoxItem("12:CT-Import starten", Arrays.asList("UserTaskStartCtImport", "UserTaskWaitForCtImport")));
        testJobsList.add(new TestJobsComboBoxItem("13:Import-Cycle starten", Arrays.asList("UserTaskStartImports", "UserTaskWaitForCtImport")));

        testJobsList.add(new TestJobsComboBoxItem("14:Exports starten", Collections.singletonList("UserTaskStartExports")));

        testJobsList.add(new TestJobsComboBoxItem("16:Collect starten", Collections.singletonList("UserTaskStartCollect"), TestSupportClientKonstanten.LAST_COMPLETITION_TIME));
        testJobsList.add(new TestJobsComboBoxItem("17:Collects überprüfen", Collections.singletonList("UserTaskCheckCollects")));

        testJobsList.add(new TestJobsComboBoxItem("18:SFTP-Uploads starten", Collections.singletonList("UserTaskStartSftpUploads")));
        testJobsList.add(new TestJobsComboBoxItem("19:SFTP-Uploads prüfen", Collections.singletonList("UserTaskCheckSftpUploads"), TestSupportClientKonstanten.LAST_COMPLETITION_TIME));

        testJobsList.add(new TestJobsComboBoxItem("20:Restore Collects(Exports)", Collections.singletonList("UserTaskStartRestore")));
        testJobsList.add(new TestJobsComboBoxItem("21:REF/COLLECT-Checks astarten", Collections.singletonList("UserTaskCheckRefExports")));
        testJobsList.add(new TestJobsComboBoxItem("22:Export-Protokoll-Checks starten", Collections.singletonList("UserTaskCheckExportProtokoll"), TestSupportClientKonstanten.LAST_COMPLETITION_TIME));

        testJobsList.add(new TestJobsComboBoxItem("25:System-Einstellungen Restaurieren", Collections.singletonList("UserTaskRestoreTestSystem")));

        testJobsList.add(new TestJobsComboBoxItem("30:BIC Jobs testen", Collections.singletonList("UserTaskBicJobs"), TestSupportClientKonstanten.UPLOAD_EMPTY_PAYLOAD));

        testJobsList.add(new TestJobsComboBoxItem("40:CrefoAnalyseErgebnisse sammeln", Collections.singletonList("UserTaskCrefoAnalyseErgebnisse")));
        testJobsList.add(new TestJobsComboBoxItem("41:Fachwertkonfig sammeln", Collections.singletonList("UserTaskCollectFWConfigs")));
        testJobsList.add(new TestJobsComboBoxItem("42:Erneute Lieferung beantragen", Collections.singletonList("UserTaskErneuteLieferungBeantragen")));
        testJobsList.sort(Comparator.naturalOrder());
        DefaultComboBoxModel<TestJobsComboBoxItem> testJobsModel = new DefaultComboBoxModel(testJobsList.toArray());
        getComboBoxTestJobs().setModel(testJobsModel);
        doChangeTestJob();
    }

    private void initHostsFields() {
        try {
            getComboBoxActivitiHost().setModel(new DefaultComboBoxModel());
            currentEnvironment.getRestServiceConfigsForActiviti().forEach(restInvokerConfig -> getComboBoxActivitiHost().addItem(new RestInvokerConfigCbItem(restInvokerConfig.getServiceURL(), restInvokerConfig)));

            getComboBoxRestServicesHost().setModel(new DefaultComboBoxModel());
            currentEnvironment.getRestServiceConfigsForMasterkonsole().forEach(restInvokerConfig -> getComboBoxRestServicesHost().addItem(new RestInvokerConfigCbItem(restInvokerConfig.getServiceURL(), restInvokerConfig)));

            getComboBoxBatchGUIHost().setModel(new DefaultComboBoxModel());
            currentEnvironment.getRestServiceConfigsForBatchGUI().forEach(restInvokerConfig -> getComboBoxBatchGUIHost().addItem(new RestInvokerConfigCbItem(restInvokerConfig.getServiceURL(), restInvokerConfig)));

            getComboBoxImpCycleHost().setModel(new DefaultComboBoxModel());
            currentEnvironment.getRestServiceConfigsForJvmImpCycle().forEach(restInvokerConfig -> getComboBoxImpCycleHost().addItem(new RestInvokerConfigCbItem(restInvokerConfig.getServiceURL(), restInvokerConfig)));

            getComboBoxInsoHost().setModel(new DefaultComboBoxModel());
            currentEnvironment.getRestServiceConfigsForJvmInso().forEach(restInvokerConfig -> getComboBoxInsoHost().addItem(new RestInvokerConfigCbItem(restInvokerConfig.getServiceURL(), restInvokerConfig)));

            getComboBoxInsoBackEndHost().setModel(new DefaultComboBoxModel());
            currentEnvironment.getRestServiceConfigsForJvmInsoBackend().forEach(restInvokerConfig -> getComboBoxInsoBackEndHost().addItem(new RestInvokerConfigCbItem(restInvokerConfig.getServiceURL(), restInvokerConfig)));
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Hosts!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void initTestSourcesComboBox() {
        try {
            ActionListener[] actionListeners = disableCbListeners(getComboBoxTestSource());
            getComboBoxTestSource().setModel(new DefaultComboBoxModel());
            List<String> testSetSources = currentEnvironment.getTestSetSources();
            testSetSources.forEach(testSetSource -> {
                getComboBoxTestSource().addItem(testSetSource);
            });
            getComboBoxTestSource().setSelectedItem(currentEnvironment.getLastTestSource());
            enableCbListeners(getComboBoxTestSource(), actionListeners);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Test-Sourcen!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void initITSQRevisionsComboBox() {
        try {
            ActionListener[] actionListeners = disableCbListeners(getComboBoxITSQRevision());
            List<String> itsqRevisionsList = currentEnvironment.getItsqRevisions();
            getComboBoxITSQRevision().setModel(new DefaultComboBoxModel());
            itsqRevisionsList.forEach(itsqRevision -> {
                getComboBoxITSQRevision().addItem(itsqRevision);
            });
            String lastItsqRevision = currentEnvironment.getLastItsqRevision();
            if (!itsqRevisionsList.contains(lastItsqRevision)) {
                getComboBoxITSQRevision().addItem(lastItsqRevision);
            }
            getComboBoxITSQRevision().setSelectedItem(lastItsqRevision);
            enableCbListeners(getComboBoxITSQRevision(), actionListeners);
        } catch (Exception ex) {
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der ITSQ-Revisions!", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void initEnvironmentsComboBox() {
        DefaultComboBoxModel environmentsModel = new DefaultComboBoxModel();
        Map<String, File> environmentsMap = currentEnvironment.getEnvironmentsMap();
        if ((environmentsMap == null) || (environmentsMap.isEmpty())) {
            String exceptionErr = "Es konnten im aktuellen Verzeichnis '" + System.getProperty("user.dir") + "'\nkeine Konfigurationsdateien '{ENE|GEE|ABE}-config.properties' gefunden werden!";
            notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(this, "Konfiguration laden", new RuntimeException(exceptionErr)));
            System.exit(-1);
        }
        Iterator<String> envNamesIterator = environmentsMap.keySet().iterator();
        while (envNamesIterator.hasNext()) {
            String envName = envNamesIterator.next();
            environmentsModel.addElement(envName);
        }
        getComboBoxEnvironment().setModel(environmentsModel);
        getComboBoxEnvironment().setSelectedItem(currentEnvironment.getCurrentEnvName());
    }

    private void doChangeEnvironment() {
        String newEnv = getComboBoxEnvironment().getSelectedItem().toString();
        try {
            EnvironmentConfig environmentConfig = new EnvironmentConfig(newEnv);
            checkEnvionmentLock(environmentConfig, newEnv);
            TestSupportClientKonstanten.TEST_TYPES selectedTestType = (TestSupportClientKonstanten.TEST_TYPES) getComboBoxTestType().getSelectedItem();
            if (!TimelineLogger.configure(environmentConfig.getLogOutputsRootForEnv(getComboBoxEnvironment().getSelectedItem().toString()), (selectedTestType + ".log"), "TimeLine.log")) {
                notifyClientJob(Level.ERROR, "Exception beim Konfigurieren der LOG-Dateien!\n");
            }
            String testSetSource = getComboBoxTestSource().getSelectedItem().toString();
            environmentConfig.setLastTestSource(testSetSource);

            environmentConfig.setLastItsqRevision(getComboBoxITSQRevision().getSelectedItem().toString());
            environmentConfig.setLastTestType((TestSupportClientKonstanten.TEST_TYPES) getComboBoxTestType().getSelectedItem());
            environmentConfig.setLastUseOnlyTestClz(getCheckBoxUseOnlyTestCLZs().isSelected());
            environmentConfig.setLastUploadSynthetics(getCheckBoxUploadSynthetics().isSelected());

            guiFrame.setEnvironmentConfig(environmentConfig);
            currentEnvironment = guiFrame.getEnvironmentConfig();
            initForEnvironment();
        } catch (Exception ex) {
            RuntimeException runtimeException = new RuntimeException(("Exception beim Initialisieren der Umgebung " + newEnv + "!"), ex);
            notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(this, "Initialisierung", runtimeException));
            ActionListener[] actionListeners = disableCbListeners(getComboBoxEnvironment());
            getComboBoxEnvironment().setSelectedItem(currentEnvironment.getCurrentEnvName());
            enableCbListeners(getComboBoxEnvironment(), actionListeners);
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
        TestSupportClientKonstanten.TEST_TYPES selectedTestType = (TestSupportClientKonstanten.TEST_TYPES) getComboBoxTestType().getSelectedItem();
        notifyClientJob(Level.INFO, String.format("\nInitialisiere für die Umgebung %s...", currentEnvironment.getCurrentEnvName()));
        getTextAreaTaskListenerInfo().setText("");
        initHostsFields();
        new Thread(() -> {
            try {
                notifyClientJob(Level.INFO, String.format("\nInitialisiere Test-Resourcen für die Umgebung %s...", getComboBoxEnvironment().getSelectedItem()));
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
        return new TestSupportHelper(currentEnvironment,
                ((RestInvokerConfigCbItem) getComboBoxActivitiHost().getSelectedItem()).getRestInvokerConfig(),
                ((RestInvokerConfigCbItem) getComboBoxRestServicesHost().getSelectedItem()).getRestInvokerConfig(),
                ((RestInvokerConfigCbItem) getComboBoxImpCycleHost().getSelectedItem()).getRestInvokerConfig(),
                TestSupportView.this);
    }

    private void initCustomers() {
        try {
            SwingUtilities.invokeLater(() -> {
                GUIStaticUtils.setWaitCursor(this, true);
                enableComponentsToOnOff(false);
                getTextFieldTestCasesPath().setText("");
                getViewcustomersSelection().setTestCustomersTableModelMap(new HashMap<>());
            });
            checkAndSetTestsSource(getComboBoxTestSource().getSelectedItem().toString());
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
        // CTEWE-1984::
        File sourceDir = new File(currentEnvironment.getTestResourcesRoot(), testSetSource);
        if (sourceDir.exists()) {
            currentEnvironment.setTestResourcesDir(sourceDir);
            String testCasesPath = currentEnvironment.getItsqRefExportsRoot().getAbsolutePath();
            currentEnvironment.setLastTestSource(testSetSource);
            SwingUtilities.invokeLater(() -> getTextFieldTestCasesPath().setText(testCasesPath));
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
        SwingUtilities.invokeLater(() -> getViewcustomersSelection().setTestCustomersTableModelMap(customerTestInfoMapMap));
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
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = getViewcustomersSelection().getActiveTestCustomersMapMap();
        if (activeTestCustomersMapMap.isEmpty()) {
            RuntimeException ex = new RuntimeException("Es sind keine Kunden aktiviert!\nBitte zuerst mindestens einen Kunden mit mindestens einen Terst-Scenario aktivieren.");
            GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Kunden!", ex);
            throw ex;
        }
        Map<String, TestCustomer> selectedCustomersMapPhase1 = activeTestCustomersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Map<String, TestCustomer> selectedCustomersMapPhase2 = activeTestCustomersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        if (getComboBoxTestType().getSelectedItem().equals(TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2)) {
            selectedCustomersMapPhase1.keySet().forEach(customerKey -> {
                TestCustomer testCustomer = selectedCustomersMapPhase2.get(customerKey);
                if (testCustomer == null || !testCustomer.isActivated()) {
                    RuntimeException ex = new RuntimeException("Kunde " + customerKey + " aus der PHASE-1 muss auch für PHASE-2 selektiert werden!");
                    GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren Kunden!", ex);
                    throw ex;
                }
            });
            // Beide Phasen nehmen
            resultMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, selectedCustomersMapPhase1);
            resultMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, selectedCustomersMapPhase2);
        } else {
            // nur Phase 2 checken: mind. ein Kunde muss aktiv sein!
            Iterator<Map.Entry<String, TestCustomer>> iterator = selectedCustomersMapPhase2.entrySet().iterator();
            while (iterator.hasNext()) {
                TestCustomer testCustomer = iterator.next().getValue();
                if (testCustomer.isActivated()) {
                    break;
                }
            }
            if (!iterator.hasNext()) {
                RuntimeException ex = new RuntimeException("PHASE-2 muss mindestens einen aktiven Kunden haben!");
                GUIStaticUtils.showExceptionMessage(this, "Fehler beim Initialisieren der Kunden!", ex);
                throw ex;
            }
            // nur Phase-2 Kunden mitnehmen
            resultMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, selectedCustomersMapPhase2);
        }
        return resultMap;
    }

    private void startUserTaskRunnable(final TestJobsComboBoxItem testJobsComboBoxItem) {
        try {
            GUIStaticUtils.setWaitCursor(this, true);
            enableComponentsToOnOff(false);
            final boolean isDemoMode = getCheckBoxDemoMode().isSelected();
            new Thread(() -> {
                try {
                    Map<String, Object> lastUserTaskVariablesMap = new HashMap<>();
                    for (String jobName : testJobsComboBoxItem.getTestJobNamesList()) {
                        String className = "de.creditreform.crefoteam.cte.tesun.activiti.handlers." + jobName;
                        Class<UserTaskRunnable> userTaskRunnableClass = (Class<UserTaskRunnable>) Class.forName(className);
                        Constructor<UserTaskRunnable> constructor = userTaskRunnableClass.getConstructor(EnvironmentConfig.class, TesunClientJobListener.class);
                        final UserTaskRunnable userTaskRunnable = constructor.newInstance(currentEnvironment, TestSupportView.this);
                        Map<String, Object> taskVariablesMap = setTaskVariablesMap(isDemoMode, getViewcustomersSelection().getActiveTestCustomersMapMap());
                        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MANUEL_USER_TASK, Boolean.TRUE);
                        taskVariablesMap.putAll(lastUserTaskVariablesMap);
                        taskVariablesMap.putAll(testJobsComboBoxItem.getTaskVariablesMap());
                        lastUserTaskVariablesMap = userTaskRunnable.runTask(taskVariablesMap);
                    }
                } catch (Exception ex) {
                    notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(TestSupportView.this, String.format("\nFehler beim Start des User-Tasks %s!", testJobsComboBoxItem.getTestJobNamesList()), ex));
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
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_TYPE, getComboBoxTestType().getSelectedItem());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE, getComboBoxTestPhase().getSelectedItem());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_USE_ONLY_TEST_CLZ, getCheckBoxUseOnlyTestCLZs().isSelected());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_UPLOAD_SYNTH_TEST_CREFOS, Boolean.valueOf(getCheckBoxUploadSynthetics().isSelected()));
        return taskVariablesMap;
    }

    private void doResize() {
        JLabel jLabel = (JLabel) getScrollPanelProcessImage().getViewport().getComponent(0);
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
                String testSetSource = getComboBoxTestSource().getSelectedItem().toString();
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
            TestSupportClientKonstanten.TEST_TYPES selectedTestType = (TestSupportClientKonstanten.TEST_TYPES) getComboBoxTestType().getSelectedItem();
            if (!TimelineLogger.configure(currentEnvironment.getLogOutputsRootForEnv(getComboBoxEnvironment().getSelectedItem().toString()), (selectedTestType + ".log"), "TimeLine.log")) {
                notifyClientJob(Level.ERROR, "Exception beim Konfigurieren der LOG-Dateien!\n");
            }
            notifyClientJob(Level.INFO, String.format("\nInitialisiere für den Test-Typ %s...", selectedTestType.getDescription()));
            initTestJobsCombo();
        } catch (Exception ex) {
            RuntimeException runtimeException = new RuntimeException("Exception beim Wechseln des Test-Typs!", ex);
            notifyClientJob(Level.ERROR, GUIStaticUtils.showExceptionMessage(this, "Initialisierung", runtimeException));
        }
    }

    private void doChangeTestJob() {
        final TestJobsComboBoxItem testJobsComboBoxItem = (TestJobsComboBoxItem) getComboBoxTestJobs().getSelectedItem();
        final Map<String, Object> taskVariablesMap = testJobsComboBoxItem.getTaskVariablesMap();
        final boolean isempty = taskVariablesMap.isEmpty();
        getLabelJobParams().setVisible(!isempty);
        getTextFieldJobParams().setVisible(!isempty);
        getTextFieldJobParams().setEditable(!isempty);
        if (!isempty) {
            String fieldText = testJobsComboBoxItem.getTaskVariablesMapAsFieldText();
            getTextFieldJobParams().setText(fieldText);
            getTextFieldJobParams().addActionListener(e -> testJobsComboBoxItem.setTaskVariables(getTextFieldJobParams().getText()));
            getTextFieldJobParams().addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    testJobsComboBoxItem.setTaskVariables(getTextFieldJobParams().getText());
                }
            });
        }
    }

    private void enableCbListeners(JComboBox comboBox, ActionListener[] actionListeners) {
        for (ActionListener actionListener : actionListeners) {
            comboBox.addActionListener(actionListener);
        }
    }

    private ActionListener[] disableCbListeners(JComboBox comboBox) {
        ActionListener[] actionListeners = comboBox.getActionListeners();
        for (ActionListener actionListener : actionListeners) {
            comboBox.removeActionListener(actionListener);
        }
        return actionListeners;
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
                    getButtonManageJVMs().setEnabled(currentEnvironment.îsAdminFuncsEnabled());
                    Object selected = getComboBoxTestSource().getSelectedItem();
                    getComboBoxITSQRevision().setEnabled(selected != null && selected.equals("ITSQ"));
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

    /**
     * Appends a message to the log console (EDT-safe) and writes it to the file log.
     */
    private void appendToConsole(String message) {
        if (message == null) return;
        final String forFile = message.startsWith("\n") ? message.substring(1) : message;
        TimelineLogger.info(this.getClass(), forFile);
        SwingUtilities.invokeLater(() -> {
            getTextAreaTaskListenerInfo().append(message.replaceAll("\t", "  "));
            if (getCheckBoxScrollToEnd().isSelected()) {
                getTextAreaTaskListenerInfo().setCaretPosition(getTextAreaTaskListenerInfo().getDocument().getLength() - 1);
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
                    JLabel jLabel = (JLabel) getScrollPanelProcessImage().getViewport().getComponent(0);
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
            // Activiti process has ended
            TimelineLogger.info(this.getClass(), "===========    Activiti-Process beendet.    ===========");
            String msg = "\n***********    UserTasks-Thread beendet.    ***********\n===========    Activiti-Process beendet.    ===========\nTest-Results sind im Output-Ordner gespeichert";
            activitiController.stop();
/* CLAUDE_MODE
            Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = getAndCheckActiveCustomers();
            viewTestResults.refreshTestResultsForMap(activeTestCustomersMapMap, true);
*/
            TimelineLogger.info(TestSupportView.class, msg);
            // Fix: dispatch all GUI operations to EDT
            final String finalMsg = msg;
            SwingUtilities.invokeLater(() -> {
                getTextAreaTaskListenerInfo().append(finalMsg.replaceAll("\t", "  "));
                if (getCheckBoxScrollToEnd().isSelected()) {
                    getTextAreaTaskListenerInfo().setCaretPosition(getTextAreaTaskListenerInfo().getDocument().getLength() - 1);
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
                return getComboBoxTestType().getSelectedItem();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_USE_ITSQ_TEST_RESOURCES)) {
                return getComboBoxTestSource().getSelectedItem();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_REF_EXPORTS_PATH)) {
                File tmpFile = new File(getTextFieldTestCasesPath().getText());
                tmpFile = new File(tmpFile.getParentFile(), TestSupportClientKonstanten.REF_EXPORTS);
                return tmpFile.getAbsolutePath();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_TEST_CASES_PATH)) {
                return GUIStaticUtils.chooseDirectory(this, currentEnvironment.getItsqRefExportsRoot().getAbsolutePath(), "Verzeichnis für die Testfälle angeben");
            } else if (askFor.equals(ASK_FOR.ASK_NEW_TEST_CASES_PATH)) {
                return getTextFieldTestCasesPath().getText();
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
                    getTextAreaTaskListenerInfo().append(strLog.replaceAll("\t", "  "));
                    if (getCheckBoxScrollToEnd().isSelected()) {
                        getTextAreaTaskListenerInfo().setCaretPosition(getTextAreaTaskListenerInfo().getDocument().getLength() - 1);
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


    private class RestInvokerConfigCbItem {
        private final String serviceURL;
        private final RestInvokerConfig restInvokerConfig;

        private RestInvokerConfigCbItem(String serviceURL, RestInvokerConfig restInvokerConfig) {
            this.serviceURL = serviceURL;
            this.restInvokerConfig = restInvokerConfig;
        }

        public String getServiceURL() {
            return serviceURL;
        }

        public RestInvokerConfig getRestInvokerConfig() {
            return restInvokerConfig;
        }

        @Override
        public String toString() {
            return serviceURL;
        }
    }
}
