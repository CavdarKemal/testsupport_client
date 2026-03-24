package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.rest.apache4.Apache4RestInvokerFactory;
import de.creditreform.crefoteam.cte.rest.interfaces.RestInvokerFactory;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.jobexecution.TesunJobexecutionInfo;
import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJob.JOB_RESULT;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.ClientJobStarter;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.JobInfo;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestResultsZipHandler;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import de.creditreform.crefoteam.jvmclient.JvmRestClient;
import de.creditreform.crefoteam.jvmclient.JvmRestClientImpl;
import de.creditreform.crefoteam.jvmclient.domain.JobStartResponse;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Level;

public abstract class AbstractUserTaskRunnable implements UserTaskRunnable {
    private static final int MAX_RETRIES_TO_START_FLUX_JOB = 3;

    protected final EnvironmentConfig environmentConfig;
    protected final TesunClientJobListener tesunClientJobListener;
    protected final TestResultsZipHandler testResultsZipHandler;
    protected boolean canceled = false;
    AtomicBoolean abortFlag = new AtomicBoolean();

    public AbstractUserTaskRunnable(final EnvironmentConfig environmentConfig, final TesunClientJobListener tesunClientJobListener) {
        this.environmentConfig = environmentConfig;
        this.tesunClientJobListener = tesunClientJobListener;
        this.testResultsZipHandler = new TestResultsZipHandler();
    }

    protected void checkLastJobStartForCustomers(Map<String, TestCustomer> selectedCustomersMap ) {
        selectedCustomersMap.entrySet().forEach(testCustomerEntry -> {
            final TestCustomer testCustomer = testCustomerEntry.getValue();
            if(testCustomer.getLastJobStartetAt() == null) {
                throw new RuntimeException(buildNotifyStringForClassName(testCustomer.getTestPhase()) + " wird übersprungen, da für den Kunden die Methode getLastJobStartetAt() null liefert!");
            }
        });
    }

    protected void checkForWait(Map<String, Object> taskVariablesMap, String waitForTime) {
        Object obj = taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_MANUEL_USER_TASK);
        if (obj != null && ((Boolean) obj).booleanValue()) {
            // manuell gestartet durch die GUI
            Calendar jobGestartetCal = extractCalendarFromMap(taskVariablesMap, TestSupportClientKonstanten.LAST_COMPLETITION_TIME);
            Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
            TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
            Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
            selectedCustomersMapPhaseX.entrySet().forEach(testCustomerEntry -> {
                testCustomerEntry.getValue().setLastJobStartetAt(jobGestartetCal);
            });
        } else {
            // durch den ACTIVITI-Prozess gestartet
            long timeBeforeExport = (Long) taskVariablesMap.get(waitForTime);
            waitMillisForUserTask(timeBeforeExport);
        }
    }

    protected Boolean checkDemoMode(Boolean demoMode) {
        if (demoMode) {
            notifyUserTask(Level.INFO, "\nDemo-Mode: UserTak-Start wird simuliert!");
            return true;
        }
        return false;
    }

    // REST-Service aufrufen und JVM-Installationen ermitteln...
    public Map<String, JvmInstallation> getJvmInstallationsMap() throws Exception {
        TesunRestService tesunRestServiceCteBatchGUI = new TesunRestService(environmentConfig.getRestServiceConfigsForBatchGUI().get(0), tesunClientJobListener);
        Map<String, JvmInstallation> jvmInstallationMap = new HashMap<>();
        Map<String, String> jvmNameToUrlMap = tesunRestServiceCteBatchGUI.getJvmInstallationMap();
        for (Map.Entry<String, String> entry : jvmNameToUrlMap.entrySet()) {
            JvmInstallation jvmInstallation = new JvmInstallation();
            jvmInstallation.setJvmName(entry.getKey());
            jvmInstallation.setJvmUrl(entry.getValue());
            jvmInstallationMap.put(jvmInstallation.getJvmName(), jvmInstallation);
        }
        return jvmInstallationMap;
    }

    protected String buildNotifyStringForClassName(TestSupportClientKonstanten.TEST_PHASE testPhase) {
        String strName = this.getClass().getSimpleName();
        return "\n@" + strName + (testPhase != null ? (" für " + testPhase.name()) : "");
    }

    protected ClientJobStarter getClientJobStarter(TesunClientJob tesunClientJob) {
        return new ClientJobStarter(tesunClientJob);
    }

    protected void doStartTesunClientJob(AbstractTesunClientJob clientJob) throws Exception {
        ClientJobStarter jobStarter = getClientJobStarter(clientJob);
        int numRetries = 0;
        while (true) {
            try {
                JOB_RESULT jobResult = jobStarter.startJob(environmentConfig);
                if (jobResult.equals(JOB_RESULT.OK)) {
                    return;
                }
                throw ((Exception) jobResult.getUserObject());
            } catch (Exception ex) {
                if (++numRetries > MAX_RETRIES_TO_START_FLUX_JOB) {
                    notifyUserTask(Level.ERROR, "\n\t" + ex.getMessage());
                }
                notifyUserTask(Level.WARN, "\n\tException beim Starten des Tesun-Jobs '" + clientJob.getJobCommandName() + "'\n" + ex.getMessage());
                if (!askForRetryUserTask(this.getClass().getSimpleName(), ex)) {
                    return;
                }
                notifyUserTask(Level.INFO, "\n\tErneuter Versuch den Tesun-Job '" + clientJob.getJobCommandName() + "' zu starten...");
            }
        }
    }

    protected void doStartJvmJob(JobInfo jobInfo, Map<String, JvmInstallation> jvmInstallationsMap, TestSupportClientKonstanten.TEST_PHASE testPhase) throws Exception {
        notifyUserTask(Level.INFO, String.format("\nVersuche JVM-Job '%s' für Test-Phase '%s' zu starten...", jobInfo.getJobName(), testPhase));
        String jvmName = jobInfo.getJvmName();
        JvmInstallation jvmInstallation = jvmInstallationsMap.get(String.format("%s (%s)", jvmName, environmentConfig.getCurrentEnvName().toUpperCase()));
        if (jvmInstallation == null) {
            throw new RuntimeException(String.format("\n\tDie JVM '%s' existiert auf der Ziel-Umgebung nicht!", jvmName));
        }
        RestInvokerFactory restInvokerFactory = new Apache4RestInvokerFactory("", "", 10000);
        int numRetries = 0;
        TesunRestService tesunRestService = getTesunRestService();

        TesunJobexecutionInfo tesunJobExecutionInfo = null;
        if (!jobInfo.getProcessNamesList().isEmpty()) {
            tesunJobExecutionInfo = tesunRestService.getTesunJobExecutionInfo(jobInfo.getProcessNamesList().get(0));
        }
        while (true) {
            try {
                JvmRestClient jvmRestClient = new JvmRestClientImpl(restInvokerFactory.getRestInvoker(jvmInstallation.getJvmUrl()), abortFlag);
                JobStartResponse jobStartResponse = jvmRestClient.startJob(jobInfo.getJobName(), jobInfo.getQueryParameters());
                if (jobStartResponse.getJobId() != null) {
                    String strInfo = String.format("JVM-Job '%s' für Test-Phase '%s' wurde gestartet. ID: %s, Job-ID : %s", jobInfo.getJobName(), testPhase, jobStartResponse.getId(), jobStartResponse.getJobId());
                    TimelineLogger.start(jobInfo.getJobName(), strInfo);
                    notifyUserTask(Level.INFO, "\n\t" + strInfo);
                    return;
                }
                throw new RuntimeException("\tDer JVM-Job " + jobInfo.getJobName() + " konnte nicht gestartet werden!");
            } catch (Exception ex) {
                // wenn Connection-Probleme, brauche keine weiteren Versuche o.ä.
                if (isConnectionProblem(ex)) {
                    throw ex;
                }
                if (++numRetries > MAX_RETRIES_TO_START_FLUX_JOB) {
                    throw ex;
                }
                if (tesunJobExecutionInfo != null) {
                    // Prüfe, ob der Job doch gestartet werden konnte...
                    if (checkJobStarted(tesunRestService, tesunJobExecutionInfo, jobInfo)) {
                        notifyUserTask(Level.INFO, String.format("\nJVM-Job '%s' wurde DOCH gestartet.", jobInfo.getJobName()));
                        return;
                    }
                }
                notifyUserTask(Level.WARN, "\n\tException beim Starten des JVM-Jobs '" + jobInfo.getJobName() + "'\n" + ex.getMessage());
                if (!askForRetryUserTask(this.getClass().getSimpleName(), ex)) {
                    throw ex;
                }
                notifyUserTask(Level.INFO, "\n\tErneuter Versuch den JVM-Job '" + jobInfo.getJobName() + "' zu starten...");
                Thread.sleep(500); // vor dem nächsten Versuch bisken warten...
            } finally {
                restInvokerFactory.close();
            }
        }
    }

    protected TesunRestService getTesunRestService() throws PropertiesException {
        final RestInvokerConfig restServiceConfigTesun = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0);
        TesunRestService tesunRestService = new TesunRestService(restServiceConfigTesun, tesunClientJobListener);
        return tesunRestService;
    }

    private boolean isConnectionProblem(Throwable ex) {
        if (ex != null) {
            String message = ex.getMessage();
            if (message != null) {
                if (message.contains("refused") || message.contains("connection") || message.contains("timeout") || message.contains("timed out")) {
                    return true;
                }
            }
            return isConnectionProblem(ex.getCause());
        }
        return false;

    }

    private boolean checkJobStarted(TesunRestService tesunRestService, TesunJobexecutionInfo tesunJobExecutionInfo1, JobInfo jobInfo) throws Exception {
        TesunJobexecutionInfo tesunJobExecutionInfo2 = tesunRestService.getTesunJobExecutionInfo(jobInfo.getProcessNamesList().get(0));
        if (tesunJobExecutionInfo2.getLastStartDate() == null) {
            // wurde NIE gestartet
            return false;
        }
        // Start-Date ist neuer als Last-Completition-Date, also neu gestartet, läuft...
        return TesunDateUtils.isSameOrAfter(tesunJobExecutionInfo2.getLastStartDate(), tesunJobExecutionInfo1.getLastCompletitionDate());
        // return tesunJobExecutionInfo2.getLastStartDate().after(tesunJobExecutionInfo1.getLastCompletitionDate());
        // wurde nicht gestartet
    }

    protected Calendar getLastCompletitionDate(Map<String, TestCustomer> selectedCustomersMap) throws Exception {
        List<String> processIdentifiersList = new ArrayList<>();
        for (Map.Entry<String, TestCustomer> entry : selectedCustomersMap.entrySet()) {
            final TestCustomer testCustomer = entry.getValue();
            processIdentifiersList.add(testCustomer.getProcessIdentifier());
        }
        processIdentifiersList.add("BETEILIGUNGEN_IMPORT");
        processIdentifiersList.add("ENTSCHEIDUNGSTRAEGER_BERECHNUNG");
        processIdentifiersList.add("BTLG_UPDATE_TRIGGER");
        processIdentifiersList.add("FROM_STAGING_INTO_CTE");
        return getLastCompletitionDate(processIdentifiersList);
    }

    protected Calendar extractCalendarFromMap(Map<String, Object> taskVariablesMap, String entyName) {
        String strTime = taskVariablesMap.get(entyName).toString();
        Calendar calendar = TesunDateUtils.toCalendar(strTime);
        if (calendar == null) {
            throw new RuntimeException("Die Taskvariable für Zeitangabe fehlt!");
        }
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    protected Calendar getLastCompletitionDate(JobInfo jobInfo) throws Exception {
        List<String> processIdentifiersList = new ArrayList<>();
        for (String processIdentifier : jobInfo.getProcessNamesList()) {
            processIdentifiersList.add(processIdentifier);
        }
        return getLastCompletitionDate(processIdentifiersList);
    }

    protected Calendar getLastCompletitionDate(List<String> processIdentifiersList) throws Exception {
        Calendar lastCompletitionDate = Calendar.getInstance();
        Calendar completitionDate;
        lastCompletitionDate.set(Calendar.YEAR, 1900);
        // notifyUserTask(Level.INFO, "\n--> Suche Last-Completition für Prozesse: " + processIdentifiersList);
        for (String processIdentifier : processIdentifiersList) {
            completitionDate = getLastCompletitionDate(processIdentifier);
            if (TesunDateUtils.isSameOrAfter(completitionDate, lastCompletitionDate)) {
                //if (completitionDate.after(lastCompletitionDate)) {
                lastCompletitionDate = completitionDate;
            }
        }
        String strLastCompletitionDate = TesunDateUtils.formatCalendar(lastCompletitionDate);
        notifyUserTask(Level.INFO, "\n==> Letzte Last-Completition ist: " + strLastCompletitionDate);
        return lastCompletitionDate;
    }

    protected Calendar getLastCompletitionDate(String processIdentifier) throws Exception {
        TesunRestService tesunRestService = getTesunRestService();
        TesunJobexecutionInfo tesunJobexecutionInfo = tesunRestService.getTesunJobExecutionInfo(processIdentifier);
        String jobStatus = tesunJobexecutionInfo.getJobStatus();
        if (!"COMPLETED".equals(jobStatus)) {
            String errMsg = String.format("getLastCompletitionDate():: Der Status des Jobs '%s' ist '%s'; sollte aber 'COMPLETED' sein!", processIdentifier, jobStatus);
            if (jobStatus == null) {
                errMsg += "\nEventuell muss für den Kunden noch Relevanz-Migration-Full und Voll-Export erfolgen.";
            }
            throw new RuntimeException(errMsg);
        }
        Calendar lastCompletitionDate = tesunJobexecutionInfo.getLastCompletitionDate();
        lastCompletitionDate.set(Calendar.SECOND, 0);
        return lastCompletitionDate;
    }

    protected void waitMillisForUserTask(long timeBeforeCtImport) {
        notifyUserTask(Level.INFO, "\n\tWarte die " + timeBeforeCtImport / 1000 + " Sekunden (ab " + new Date() + ") ...");
        long startMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() < (startMillis + timeBeforeCtImport)) {
            notifyUserTask(Level.INFO, ".");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        //notifyUserTask(Level.INFO, "\n\t" + (System.currentTimeMillis()-startMillis) / 1000 + " Sekunden gewartet.");
    }

    protected boolean askForRetryUserTask(String simpleName, Exception ex, String... additionalInfos) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Bei der Ausführung des ACTIVITI-Tasks '");
        stringBuffer.append(simpleName);
        stringBuffer.append("' ist ein Problem aufgetreten:\n\n");
        for (String additionalInfo : additionalInfos) {
            stringBuffer.append(additionalInfo);
            stringBuffer.append("\n");
        }
        stringBuffer.append((ex.getMessage() != null) ? ex.getMessage() : "");
        stringBuffer.append((ex.getCause() != null) ? ex.getCause().getMessage() : "");
        stringBuffer.append("\n\nDie Aktion wiederholen oder den Prozess abbrechen?");
        try {
            Boolean retry = (Boolean) askUserTask(TesunClientJobListener.ASK_FOR.ASK_OBJECT_RETRY, stringBuffer.toString());
            return retry.booleanValue();
        } catch (Exception ex1) {
            notifyUserTask(Level.ERROR, "\n! " + ex1.getClass().getName() + ":" + ex.getMessage());
        }
        return false;
    }

    protected Object askUserTask(TesunClientJobListener.ASK_FOR askFor, Object userObject) throws Exception {
        if (tesunClientJobListener != null) {
            return tesunClientJobListener.askClientJob(askFor, userObject);
        }
        return null;
    }

    protected void notifyUserTask(Level level, Object notifyObject) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyObject);
        }
    }

    /****************       UserTaskRunnable            *************************/
    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        return taskVariablesMap;
    }

}

