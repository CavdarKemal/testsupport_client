package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import org.apache.log4j.Level;

import java.util.Map;

public class UserTaskFailureMail extends AbstractUserTaskRunnable {
    public UserTaskFailureMail(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        String emailSubject = "CTE Test-Automatisierung: " + environmentConfig.getCurrentEnvName();
        String emailContent = "Fehler beim Ausführen des ACTIVITI-Prozesses!";
        TesunUtilites.sendEmail(environmentConfig.getActivitiEmailFrom(), environmentConfig.getActivitiFailureEmailTo(), emailSubject, emailContent, null);
        return taskVariablesMap;
    }

}
