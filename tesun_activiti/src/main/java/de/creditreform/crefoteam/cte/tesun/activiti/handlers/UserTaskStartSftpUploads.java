package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.JobInfo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;
import org.apache.log4j.Level;

import java.util.*;

public class UserTaskStartSftpUploads extends AbstractUserTaskRunnable {

    public UserTaskStartSftpUploads(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        if (!environmentConfig.isSftpUploadEnabled()) {
            notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase) + " wird übersprungen, da deaktiviert!");
            return taskVariablesMap;
        }
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
        // check, ob bei allen TestCustomers JobStartetAt gesetzt ist
        checkLastJobStartForCustomers(selectedCustomersMapPhaseX);

        List<String> uploadJobNameList = new ArrayList<>();
        Map<String, JvmInstallation> jvmInstallationsMap = getJvmInstallationsMap();
        for (Map.Entry<String, TestCustomer> testCustomerEntry : selectedCustomersMapPhaseX.entrySet()) {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            if (testCustomer.isActivated()) {
                final String jobName = testCustomer.getUploadJobName();
                // wurde für die JVM schon ein Export gestartet?
                if (uploadJobNameList.contains(testCustomer.getUploadJobName())) {
                    continue;
                }
                uploadJobNameList.add(testCustomer.getUploadJobName());
                Properties queryParameters = null;
                if (jobName.contains("ika")) {
                    queryParameters = new Properties();
                    queryParameters.setProperty("ikarosclz", "215");
                }
                JobInfo jobInfo = new JobInfo(testCustomer.getJvmName(), jobName, queryParameters, "");
                doStartJvmJob(jobInfo, jvmInstallationsMap, testPhase);
                Thread.sleep(200);
            }
        }
        testResultsZipHandler.writeTestResultsToFile(selectedCustomersMapPhaseX);
        return taskVariablesMap;
    }

}
