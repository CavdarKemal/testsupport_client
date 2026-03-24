package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.exports_collector.ExportsWaiter;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.JobInfo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestResults;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Level;

public class UserTaskStartExports extends AbstractUserTaskRunnable {
    public static final String COMMAND = "UserTask START-EXPORTS";
    TestSupportClientKonstanten.TEST_PHASE testPhase;

    public UserTaskStartExports(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        // ermittle die JVM-Installationen
        Map<String, JvmInstallation> jvmInstallationsMap = getJvmInstallationsMap();
        int nCount = 0;
        Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
        List<String> exportJobNameList = new ArrayList<>();
        Map<String, TestCustomer> subCustomersMap = new HashMap<>();
        ExportsWaiter exportsWaiter = new ExportsWaiter(environmentConfig, tesunClientJobListener);
        // Starte Exports in Paketen...
        Calendar nowCall = Calendar.getInstance();
        for (Map.Entry<String, TestCustomer> testCustomerEntry : selectedCustomersMapPhaseX.entrySet()) {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            if (testCustomer.isActivated()) {
                testCustomer.addTestResultsForCommand(COMMAND);
                // wurde für die JVM schon ein Export gestartet?
                if (exportJobNameList.contains(testCustomer.getExportJobName())) {
                    testCustomer.setLastJobStartetAt(nowCall);
                    continue;
                }
                exportJobNameList.add(testCustomer.getExportJobName());
                JobInfo jobInfo = new JobInfo(testCustomer);
                try {
                    doStartJvmJob(jobInfo, jvmInstallationsMap, testPhase);
                    testCustomer.setLastJobStartetAt(nowCall);
                    subCustomersMap.put(testCustomer.getCustomerKey(), testCustomer);
                    if (++nCount > environmentConfig.getMaxCustomersForTest()) {
                        notifyUserTask(Level.INFO, "\nWarte auf die bisherigen Exporte...\n");
                        exportsWaiter.waitForExports(subCustomersMap);
                        notifyUserTask(Level.INFO, "\n\tBisherige Exports sind fertig, Mache mit den restlichen Exports weiter...\n");
                        nCount = 0;
                        subCustomersMap.clear();
                    } else {
                        Thread.sleep(200);
                    }
                } catch (Exception ex) {
                    String errorStr = "\n!!!Exception beim Start des Export-Jobs '" + testCustomer.getExportJobName() + "'!\n" + ex.getMessage();
                    TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                    testCustomer.addResultInfo(COMMAND, resultInfo);
                    notifyUserTask(Level.ERROR, String.format("\n!!! Export-Job '%s' konnte nicht gestartet werden!\nEs wird mit dem nächsten Kunden fortgesetz.\n", jobInfo.getJobName()) + ex.getMessage());
                }
            }
        }
        // Der Export 'inso.deltaExportPreProduct' wurde oben in der for-schleife gestartet,
        // nun die Phase-2-Eexporte 'inso.deltaExportKundeDaily', 'inso.deltaExportKundeWeekly' und 'inso.deltaExportKundeMonthly' starten...
        startInsoPhase2Exports(exportsWaiter, selectedCustomersMapPhaseX, jvmInstallationsMap);
        testResultsZipHandler.writeTestResultsToFile(selectedCustomersMapPhaseX);
        return taskVariablesMap;
    }

    private void startInsoPhase2Exports(ExportsWaiter exportsWaiter, Map<String, TestCustomer> testCustomerMap, Map<String, JvmInstallation> jvmInstallationsMap) throws Exception {
        String[][] insoPhase2Eexports = {
                {"inso.deltaExportKundeDaily", "inso.deltaUploadKunde", "EXPORT_CTE_TO_INSO_2T"},
                {"inso.deltaExportKundeWeekly", "inso.deltaUploadKunde", "EXPORT_CTE_TO_INSO_2W"},
                {"inso.deltaExportKundeMonthly", "inso.deltaUploadKunde", "EXPORT_CTE_TO_INSO_2M"},
        };
        String customerKey = testCustomerMap.keySet().stream().filter(key -> key.contains("INSO")).collect(Collectors.toList()).get(0);
        TestCustomer insoCustomer = testCustomerMap.get(customerKey);
        if (insoCustomer != null) {
            Map<String, TestCustomer> insoTestCustomerMap = new HashMap<>();
            for (int i = 0; i < insoPhase2Eexports.length; i++) {
                TestCustomer testCustomer = TestCustomer.cloneInsoMonitorPhase2(insoCustomer, insoPhase2Eexports[i][0], insoPhase2Eexports[i][1], insoPhase2Eexports[i][2]);
                testCustomer.setCustomerKey("INSO-TMP-" + i);
                JobInfo jobInfo = new JobInfo(testCustomer);
                try {
                    doStartJvmJob(jobInfo, jvmInstallationsMap, testPhase);
                    testCustomer.setLastJobStartetAt(Calendar.getInstance());
                    notifyUserTask(Level.INFO, String.format("\n\tExport-Job '%s' wurde gestartet.", jobInfo.getJobName()));
                    insoTestCustomerMap.put(testCustomer.getCustomerKey(), testCustomer);
                } catch (Exception ex) {
                    String errorStr = String.format("\n!!! Export-Job '%s' konnte nicht gestartet werden!\nEs wird mit dem nächsten Kunden fortgesetz.\n", jobInfo.getJobName()) + ex.getMessage();
                    TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                    testCustomer.addResultInfo(COMMAND, resultInfo);
                    notifyUserTask(Level.ERROR, errorStr);
                }
            }
            // Export abwarten...
            notifyUserTask(Level.INFO, "\nWarte auf die INSO-Exporte...\n");
            exportsWaiter.waitForExports(insoTestCustomerMap);
        }
    }

}
