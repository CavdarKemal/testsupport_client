package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import java.util.Map;
import org.apache.log4j.Level;

public class UserTaskWaitBeforBeteiligtenImport extends AbstractUserTaskRunnable {
    public UserTaskWaitBeforBeteiligtenImport(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));

        // ist ACTIVITI-Prozess oder Manuell-Test?
        checkForWait(taskVariablesMap, TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_BTLG_IMPORT);

        return taskVariablesMap;
    }

}
