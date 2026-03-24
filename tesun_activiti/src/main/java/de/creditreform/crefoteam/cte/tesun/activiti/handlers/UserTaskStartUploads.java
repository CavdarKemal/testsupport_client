package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.uploader.TestFallUploader;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.JobInfo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;
import java.util.Map;
import org.apache.log4j.Level;

public class UserTaskStartUploads extends AbstractUserTaskRunnable {
    public UserTaskStartUploads(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        TestSupportClientKonstanten.TEST_TYPES testType = (TestSupportClientKonstanten.TEST_TYPES) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_TYPE);
        Boolean uploadSyntheticTestFiles = Boolean.valueOf(taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_UPLOAD_SYNTH_TEST_CREFOS).toString());
        doStartTesunClientJob(new TestFallUploader(selectedCustomersMapMap, uploadSyntheticTestFiles, testType, testPhase, tesunClientJobListener));

        Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
        if (testPhase.equals(TestSupportClientKonstanten.TEST_PHASE.PHASE_2)) {
            //Für EH muss noch der Job 'eh.riskNotificationsImport' gestartet werden...
            TestCustomer ehTestCustomer = selectedCustomersMapPhaseX.get("EH");
            if (ehTestCustomer != null) {
                TestCustomer tmpCustomer = TestCustomer.cloneEhForRisksImport(ehTestCustomer); // cloene den TestCustomer, um Jobname zu überschreiben
                notifyUserTask(Level.INFO, "\n\t\tStart den Job '" + tmpCustomer.getExportJobName() + "' für EH...");
                JobInfo jobInfo = new JobInfo(tmpCustomer);
                try {
                    // ermittle die JVM-Installationen
                    Map<String, JvmInstallation> jvmInstallationsMap = getJvmInstallationsMap();
                    doStartJvmJob(jobInfo, jvmInstallationsMap, testPhase);
                } catch (Exception ex) {
                    notifyUserTask(Level.ERROR, String.format("\n!!! Job '%s' konnte nicht gestartet werden!\n", jobInfo.getJobName()) + ex.getMessage());
                }
            }
        }
        testResultsZipHandler.writeTestResultsToFile(selectedCustomersMapPhaseX);
        return taskVariablesMap;
    }
}
