package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingimport.TesunImportTrace;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingimport.TesunImportTrackingCrefo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingimport.TesunImportTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.*;
import org.apache.log4j.Level;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class UserTaskWaitForStaging extends AbstractUserTaskRunnable {

    public UserTaskWaitForStaging(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
        notifyUserTask(Level.INFO, "\nWarte bis Crefos der Kunden im Staging-Bereich angekommen sind...");
        ExecutorService executor = Executors.newFixedThreadPool(50);
        Map<String, FutureTask<List<String>>> futureTasksMap = new HashMap<>();
        for (Map.Entry<String, TestCustomer> testCustomerEntry : selectedCustomersMapPhaseX.entrySet()) {
            final TestCustomer testCustomer = testCustomerEntry.getValue();
            for (TestScenario testScenario : testCustomer.getTestScenariosList()) {
                if (testScenario.isActivated()) {
                    List<String> crefosAsStringList = new ArrayList<>();
                    for (TestCrefo testCrefo : testScenario.getTestCrefosAsList()) {
                        crefosAsStringList.add("" + testCrefo.getItsqTestCrefoNr());
                    }
                    StagingInfoForCustomerCallable callable = new StagingInfoForCustomerCallable(environmentConfig, testCustomer, testScenario, crefosAsStringList);
                    callable.setUserTaskThreadListener(tesunClientJobListener);
                    FutureTask<List<String>> futureTask = new FutureTask<>(callable);
                    String taskName = String.format("%s.%s.%s", testCustomer.getCustomerKey(), testScenario.getScenarioName(), crefosAsStringList.toString());
                    futureTasksMap.put(taskName, futureTask);
                    notifyUserTask(Level.INFO, String.format("\n\tErmittle ImportTrackingInfo für %s.%s", testCustomer.getCustomerKey(), testScenario.getScenarioName()));
                    notifyUserTask(Level.INFO, String.format("\n\t\tund der Crefos %s...", crefosAsStringList));
                    executor.execute(futureTask);
                }
            }
        }
        try {
            List<String> timeOutsList = new ArrayList<>();
            for (Map.Entry<String, FutureTask<List<String>>> entry : futureTasksMap.entrySet()) {
                FutureTask<List<String>> futureTask = entry.getValue();
                List<String> crefosAsStringList = futureTask.get();
                if (crefosAsStringList != null && !crefosAsStringList.isEmpty()) {
                    timeOutsList.addAll(crefosAsStringList);
                }
            }
            if (!timeOutsList.isEmpty()) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Folgende Crefos sind nicht im Staging angekommen:\n");
                stringBuffer.append(timeOutsList);
                stringBuffer.append("\nSoll der Prozess dennoch fortgesetzt werden?");
                Boolean retry = (Boolean) askUserTask(TesunClientJobListener.ASK_FOR.ASK_OBJECT_RETRY, stringBuffer.toString());
                if (!retry.booleanValue()) {
                    throw new RuntimeException("Fehler beim Warten auf STAGING für die Crefos:\n" + timeOutsList);
                }
                notifyUserTask(Level.INFO, "\nTrotz TimeOuts bei den Crefos " + stringBuffer + " wird der Prozess fortgesetzt!");
            }
        } finally {
            executor.shutdown();
        }
        testResultsZipHandler.writeTestResultsToFile(selectedCustomersMapPhaseX);
        return taskVariablesMap;
    }

    protected static class StagingInfoForCustomerCallable<E> implements Callable<List<String>> {
        private final TestCustomer testCustomer;
        private final TestScenario testScenario;
        private final List<String> crefosAsStringList;
        private final RestInvokerConfig restServiceConfigWLS;

        private TesunRestService tesunRestService;
        private TesunClientJobListener tesunClientJobListener;

        public StagingInfoForCustomerCallable(EnvironmentConfig environmentConfig, TestCustomer testCustomer, TestScenario testScenario, List<String> crefosAsStringList) throws Exception {
            restServiceConfigWLS = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0);
            this.tesunRestService = new TesunRestService(restServiceConfigWLS, tesunClientJobListener);
            this.testCustomer = testCustomer;
            this.testScenario = testScenario;
            this.crefosAsStringList = crefosAsStringList;
        }

        @Override
        public List<String> call() throws Exception {
            Calendar sixMinutesBeforeNowCal = Calendar.getInstance();
            sixMinutesBeforeNowCal.add(Calendar.MINUTE, -6);
            List<String> crefosListWithError = new ArrayList<>();
            List<String> crefosListOK = new ArrayList<>();
            TesunImportTrackingErgebnis importTrackingInfo = tesunRestService.getImportTrackingInfo(crefosAsStringList);
            List<TesunImportTrackingCrefo> crefoTrackingList = importTrackingInfo.getCrefoTracking();
            for (TesunImportTrackingCrefo crefoTracking : crefoTrackingList) {
                String strTemp = String.format("%s.%s:: %d", testCustomer.getCustomerKey(), testScenario.getScenarioName(), crefoTracking.getCrefo());
                TesunImportTrace importTrace = crefoTracking.getImportTrace();
                if (!importTrace.getStatus().equals("IMPORT_STATUS_NICHT_GELIEFERT")) {
                    if (importTrace.getEingangStaging() != null) {
                        boolean isEingangStagingOldEnough = importTrace.getEingangStaging().before(sixMinutesBeforeNowCal);
                        if (isEingangStagingOldEnough) {
                            crefosListOK.add("" + crefoTracking.getCrefo());
                        } else {
                            crefosListWithError.add(String.format("\n%s: Staging-Eingangsdatum %s is nicht 6 Minuten alt", strTemp, importTrace.getEingangStaging().getTime()));
                        }
                    } else {
                        crefosListWithError.add(String.format("\n%s: %s", strTemp, importTrace.getStatus()));
                    }
                } else {
                    crefosListWithError.add(String.format("\n%s: %s", strTemp, importTrace.getStatus()));
                }
            }
            String strInfo = String.format("\n\tStaging-Eingangsdatum OK für %s.%s", testCustomer.getCustomerKey(), testScenario.getScenarioName());
            notifyListener(Level.INFO, strInfo);
            strInfo = String.format("\n\t\tund der Crefos %s", crefosListOK);
            notifyListener(Level.INFO, strInfo);
            return crefosListWithError;
        }

        public void setUserTaskThreadListener(TesunClientJobListener tesunClientJobListener) {
            this.tesunClientJobListener = tesunClientJobListener;
        }

        protected void notifyListener(Level level, Object notifyObject) {
            if (tesunClientJobListener != null) {
                tesunClientJobListener.notifyClientJob(level, notifyObject);
            }
        }

        protected Object askListener(TesunClientJobListener.ASK_FOR askFor, Object userObject) throws Exception {
            if (tesunClientJobListener != null) {
                return tesunClientJobListener.askClientJob(askFor, userObject);
            }
            return null;
        }

    }

}
