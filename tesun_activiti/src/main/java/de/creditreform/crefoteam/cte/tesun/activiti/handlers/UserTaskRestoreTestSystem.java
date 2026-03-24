package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.log4j.Level;

import java.util.Map;

public class UserTaskRestoreTestSystem extends AbstractUserTaskRunnable {
    public UserTaskRestoreTestSystem(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        RestInvokerConfig restServiceConfigTesun = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0);
        TesunRestService tesunRestService = getTesunRestServiceInstance(restServiceConfigTesun);
        tesunRestService.restoreEnvironmentProperties();
        notifyUserTask(Level.INFO, "\n\t\tDie CLZ-Liste in Master-Console-Properties wieder restauriert.");
        return taskVariablesMap;
    }

    protected TesunRestService getTesunRestServiceInstance(RestInvokerConfig restServiceConfigTesun) throws PropertiesException {
        TesunRestService tesunRestService = new TesunRestService(restServiceConfigTesun, tesunClientJobListener);
        return tesunRestService;
    }

}
