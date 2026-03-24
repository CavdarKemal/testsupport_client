package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.JobInfo;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.Map;

public abstract class AbstractJvmJobStarter extends AbstractUserTaskRunnable {
    final JobInfo jobInfo;
    final String lastStartDateVariable;

    public AbstractJvmJobStarter(final JobInfo jobInfo, final String lastStartDateVariable, final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
        this.jobInfo = jobInfo;
        this.lastStartDateVariable = lastStartDateVariable;
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Calendar lastStartCal = Calendar.getInstance();
        taskVariablesMap.put(lastStartDateVariable, lastStartCal.getTimeInMillis());
        notifyUserTask(Level.INFO, "\n\tLetzte " + lastStartDateVariable + ": " + lastStartCal.getTime());
        Map<String, JvmInstallation> jvmInstallationsMap = getJvmInstallationsMap();
        doStartJvmJob(jobInfo, jvmInstallationsMap, testPhase);
        return taskVariablesMap;
    }

}