package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.generate.TestFallGeneratePseudoCrefos;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.log4j.Level;

import java.util.Iterator;
import java.util.Map;

public class UserTaskGeneratePseudoCrefos extends AbstractUserTaskRunnable {
    public UserTaskGeneratePseudoCrefos(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        TestSupportClientKonstanten.TEST_TYPES testType = (TestSupportClientKonstanten.TEST_TYPES) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_TYPE);
        final TestFallGeneratePseudoCrefos clientJob = new TestFallGeneratePseudoCrefos(testType, activeCustomersMapMap, tesunClientJobListener);
        doStartTesunClientJob(clientJob);
        Iterator<Map.Entry<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>> iterator = activeCustomersMapMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> next = iterator.next();
            testResultsZipHandler.writeTestResultsToFile(next.getValue());
        }
        return taskVariablesMap;
    }

}
