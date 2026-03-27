package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.jobexecution.TesunJobexecutionInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Level;

public abstract class AbstractUserTaskWaiter extends AbstractUserTaskRunnable {
    private static final int MAX_RETRIES = 5;
    final String processIdentifier;
    final String lastStartDateVariable;

    public AbstractUserTaskWaiter(final String processIdentifier, final String lastStartDateVariable, final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
        this.processIdentifier = processIdentifier;
        this.lastStartDateVariable = lastStartDateVariable;
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Calendar jobStartedCal = extractCalendarFromMap(taskVariablesMap, lastStartDateVariable);
        doWaitForFinish(jobStartedCal);
        return taskVariablesMap;
    }

    protected void doWaitForFinish(Calendar jobStartedCal) throws Exception {
        TesunRestService tesunRestService = getTesunRestService();
        int numRetries = 0;
        long millisForImportCycleTimeOut = environmentConfig.getMillisForImportCycleTimeOut(); // ????????????
        long sleepTimeMillis = environmentConfig.getMillisForJobStatusQuerySleepTime();
        while (true) {
            try {
                waitForJob(tesunRestService, jobStartedCal, millisForImportCycleTimeOut, sleepTimeMillis);
                break;
            } catch (Exception ex) {
                if (++numRetries > MAX_RETRIES) {
                    throw ex;
                }
                if (!askForRetryUserTask(this.getClass().getSimpleName(), ex)) {
                    throw ex;
                }
            }
        }
    }

    private void waitForJob(TesunRestService tesunRestService, Calendar jobStartedCal, long millisForTimeOut, long sleepTimeMillis) throws Exception {
        String strGestartetCal = TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS.format(jobStartedCal.getTime());
        long currentTimeMillis = System.currentTimeMillis();
        notifyUserTask(Level.INFO, "\n\tWarte auf Beendigung des Prozesses '" + processIdentifier + "...");
        while (System.currentTimeMillis() < (currentTimeMillis + millisForTimeOut)) {
            TesunJobexecutionInfo lastJobExecutionInfo = tesunRestService.getTesunJobExecutionInfo(processIdentifier);
            String jobStatus = lastJobExecutionInfo.getJobStatus();
            if (jobStatus == null) {
                throw new RuntimeException("Der Status des Prozesses '" + processIdentifier + "' konnte nicht ermittelt werden!");
            }
                Calendar lastCompletitionDate = lastJobExecutionInfo.getLastCompletitionDate();
                if (lastCompletitionDate != null) {
                    String strlastCompletitionDate = TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS.format(lastCompletitionDate.getTime());
                notifyUserTask(Level.INFO, "\n\t\t'Check ob lastCompletitionDate '" + strlastCompletitionDate + "' nach " + strGestartetCal + " ist...");
                boolean isExportCompletitionAfterExportStart = TesunDateUtils.isSameOrAfter(lastCompletitionDate, jobStartedCal);
                    if (isExportCompletitionAfterExportStart) {
                    if (jobStatus.equals("COMPLETED")) {
                        String msg = "Process '" + processIdentifier + "' wurde beendet.";
                        notifyUserTask(Level.INFO, "\n" + msg);
                        TimelineLogger.end(processIdentifier, msg);
                        return;
                    }
                    else {
                        throw new TimeoutException("Der Prozess '" + processIdentifier + "' wurde mit dem Status " + jobStatus + " abgebrochen!");
                    }
                }
                // notifyUserTask(Level.INFO, "\n\t\t\t  lastCompletitionDate " + strDateTime + ", " + strImportsGestartetCal + " passt nicht, weiter...");
            }
            notifyUserTask(Level.INFO, ".");
            Thread.sleep(sleepTimeMillis);
        }
        throw new TimeoutException("TimeOut beim Prozess '" + processIdentifier + "'!");
    }

}
