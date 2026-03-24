package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.JobInfo;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.Map;

public class UserTaskStartImports extends AbstractUserTaskRunnable {

    public UserTaskStartImports(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        // prüfe, ob noch laufende ImportCycle-Job vorliegt und ermittle die letzte Completition-Zeit von "FROM_STAGING_INTO_CTE"
        JobInfo jobInfoForImportCycle = environmentConfig.getJobInfoForImportCycle();
        Calendar lastCompletitionDate = getLastCompletitionDate(jobInfoForImportCycle);
        Map<String, JvmInstallation> jvmInstallationsMap = getJvmInstallationsMap();
        doStartJvmJob(jobInfoForImportCycle, jvmInstallationsMap, testPhase);

        taskVariablesMap.put(TestSupportClientKonstanten.IMPORT_STARTET_AT, lastCompletitionDate.getTimeInMillis());
        return taskVariablesMap;
    }
}