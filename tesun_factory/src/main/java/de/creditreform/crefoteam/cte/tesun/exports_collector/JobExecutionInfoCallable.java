package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.jobexecution.TesunJobexecutionInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestResults;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import java.util.Calendar;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;

public class JobExecutionInfoCallable<E> implements Callable {
    public static final String COMMAND = "WAIT-FOR-EXPORTS";

    private final TesunRestService tesunRestService;
    private final TestCustomer testCustomer;
    private final TesunClientJobListener tesunClientJobListener;
    private long sleepTimeMillis;
    private long timeOutInMillis;
    private final TestResults testResults;

    public JobExecutionInfoCallable(RestInvokerConfig restServiceConfigTesun, TestCustomer testCustomer, TesunClientJobListener tesunClientJobListener) {
        this.tesunRestService = new TesunRestService(restServiceConfigTesun, tesunClientJobListener);
        this.testCustomer = testCustomer;
        this.tesunClientJobListener = tesunClientJobListener;
        testResults = testCustomer.getTestResultsForCommand(COMMAND);
    }

    public void setTimeOutMillis(long timeOutInMillis) {
        this.timeOutInMillis = timeOutInMillis;
    }

    public void setSleepTimeMillis(long sleepTimeMillis) {
        this.sleepTimeMillis = sleepTimeMillis;
    }

    @Override
    public TesunJobexecutionInfo call() throws Exception {
        int numRetries = 0;
        while (true) {
            try {
                TesunJobexecutionInfo tesunJobexecutionInfo = waitForProcess();
                return tesunJobexecutionInfo;
            } catch (Exception ex) {
                if (++numRetries > 3) {
                    if (!doAskForRetryUserTask(ex)) {
                        String errorStr = "Warten auf den Export '" + testCustomer.getProcessIdentifier() + "' nach " + numRetries + " Versuchen abgebrochen!";
                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                        testCustomer.addResultInfo(COMMAND, resultInfo);
                        break;
                    }
                } else {
                    // bisken warten...
                    Thread.sleep(sleepTimeMillis);
                    // ... und dann nochmal versuchen
                }
            }
        }
        return null;
    }

    protected TesunJobexecutionInfo waitForProcess() throws Exception {
        TesunJobexecutionInfo tesunExecutionInfo = null;
        long currentTimeMillis = System.currentTimeMillis();
        String strInfo = "\n\t\tWarte bis der Export-Job '" + testCustomer.getProcessIdentifier() + "' beendet ist, der um " + TesunDateUtils.formatCalendar(testCustomer.getLastJobStartetAt()) + " gestartet wurde...";
        tesunClientJobListener.notifyClientJob(Level.INFO, strInfo);
        while (System.currentTimeMillis() < (currentTimeMillis + timeOutInMillis)) {
            tesunExecutionInfo = tesunRestService.getTesunJobExecutionInfo(testCustomer.getProcessIdentifier());
            String jobStatus = tesunExecutionInfo.getJobStatus();
            if (jobStatus != null) {
                if (jobStatus.equals("COMPLETED")) {
                    Calendar lastCompletitionDate = tesunExecutionInfo.getLastCompletitionDate();
                    if (lastCompletitionDate != null) {
                        String strOnfo = "\n\t\tFür den Export-Job '" + testCustomer.getProcessIdentifier() + "' wurde COMPLETED-Time " + TesunDateUtils.formatCalendar(lastCompletitionDate) + " ermittelt, prüfe, ob dieser jung genug ist...";
                        tesunClientJobListener.notifyClientJob(Level.INFO, strOnfo);
                        boolean isAfter = TesunDateUtils.isSameOrAfter(lastCompletitionDate, testCustomer.getLastJobStartetAt());
                        if (isAfter) {
                            String msg = "Export-Job '" + testCustomer.getProcessIdentifier() + "' wurde beendet.";
                            TimelineLogger.end(testCustomer.getProcessIdentifier(), msg);
                            tesunClientJobListener.notifyClientJob(Level.INFO, "\n\t\t" + msg);
                            testCustomer.setLastJobStartetAt(lastCompletitionDate);
                            return tesunExecutionInfo;
                        }
                    } else {
                        String errorStr = "\n\t!!! Für den Export-Job '" + testCustomer.getProcessIdentifier() + "' konnte kein lastCompletitionDates ermittelt werden!";
                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                        testCustomer.addResultInfo(COMMAND, resultInfo);
                        tesunClientJobListener.notifyClientJob(Level.ERROR, errorStr);
                        return tesunExecutionInfo;
                    }
                }
            } else {
                String errorStr = "\n\t!!! Für den Export-Job '" + testCustomer.getProcessIdentifier() + "' konnte kein jobStatus ermittelt werden!";
                TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                testCustomer.addResultInfo(COMMAND, resultInfo);
                tesunClientJobListener.notifyClientJob(Level.ERROR, errorStr);
                return tesunExecutionInfo;
            }
            tesunClientJobListener.notifyClientJob(Level.INFO, ".");
            Thread.sleep(sleepTimeMillis);
        }
        // Time-Out!
        String errorStr = "\n\t!!! Time-Out beim Warten auf den Export-Job '" + testCustomer.getProcessIdentifier() + "'!";
        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
        testCustomer.addResultInfo(COMMAND, resultInfo);
        tesunClientJobListener.notifyClientJob(Level.ERROR, errorStr);
        return tesunExecutionInfo;
    }

    protected boolean doAskForRetryUserTask(Exception ex) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Beim Prüfen des Export-Status für den Export '");
        stringBuffer.append(testCustomer.getProcessIdentifier());
        stringBuffer.append("' ist ein Problem aufgetreten:\n\n");
        stringBuffer.append((ex.getMessage() != null) ? ex.getMessage() : ex.getStackTrace().toString());
        stringBuffer.append("\n\nDie Prüfung wiederholen oder abbrechen?");
        try {
            Boolean retry = (Boolean) tesunClientJobListener.askClientJob(TesunClientJobListener.ASK_FOR.ASK_OBJECT_RETRY, stringBuffer.toString());
            return retry.booleanValue();
        } catch (Exception ex1) {
            tesunClientJobListener.notifyClientJob(Level.ERROR, "\n! " + ex1.getClass().getName() + ":" + ex.getMessage());
        }
        return false;
    }

}
