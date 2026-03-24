package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import java.util.Map;
import org.apache.log4j.Level;

public class UserTaskExtendsArchivBestandCrefos extends AbstractUserTaskRunnable {
    public UserTaskExtendsArchivBestandCrefos(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
/*
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        File alternateSourceDir = new File("D:\\CTE\\testsupport_client\\TestSupportGUI\\X-TESTS\\ENE\\ITSQ\\tesfaelle_cte\\ARCHIV-BESTAND-PH2");
        final TestFallExtendsArchivBestandCrefos clientJob = new TestFallExtendsArchivBestandCrefos(selectedCustomersMapMap, environmentConfig.mustExtendEntgsFromREST(), alternateSourceDir, tesunClientJobListener);
        doStartTesunClientJob(clientJob);
*/
        return taskVariablesMap;
    }

}
