package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingexport.TesunExportTrace;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingexport.TesunExportTrackingCrefo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingexport.TesunExportTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.*;
import java.util.Calendar;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserTaskCheckExportProtokoll extends AbstractUserTaskRunnable {

    public UserTaskCheckExportProtokoll(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        if (!environmentConfig.isCheckExportProtokollEnabled()) {
            notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase) + " wird übersprungen, da deaktiviert!");
            return taskVariablesMap;
        }
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        try {
            Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
            Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
            // check, ob bei allen TestCustomers JobStartetAt gesetzt ist
            checkLastJobStartForCustomers(selectedCustomersMapPhaseX);
            Map<String, List<Long>> crefosWithoutExportProtMap = checkExportProtokoll(selectedCustomersMapPhaseX);
            if (!crefosWithoutExportProtMap.isEmpty()) {
                final String strErr = "\nFehlende Export-Protokoll-Infos!\n" + TesunUtilites.formatMap(crefosWithoutExportProtMap, 15);
                throw new RuntimeException(strErr);
            }
            testResultsZipHandler.writeTestResultsToFile(selectedCustomersMapPhaseX);
            return taskVariablesMap;
        } catch (Exception ex) {
            notifyUserTask(Level.ERROR, "\nException beim Check des Export-Protokolls!\n" + ex.getMessage() != null ? ex.getMessage() : ex.getCause().getMessage());
            return taskVariablesMap;
        }
    }

    private Map<String, List<Long>> checkExportProtokoll(Map<String, TestCustomer> selectedCustomersMap) throws Exception {
        TesunRestService tesunRestService = new TesunRestService(environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), tesunClientJobListener);
        Map<String, List<Long>> crefosWithoutExportProtMap = new HashMap<>();
        final File exportProResultsFile = new File(environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.fileNameExportProtokollResults);
        StringBuilder exportProResultsSB = new StringBuilder();
        notifyUserTask(Level.INFO, "\nExport-Protokoll-Infos");
        exportProResultsSB.append("\nExport-Protokoll-Infos\n===============================================");
        for (Map.Entry<String, TestCustomer> testCustomerEntry : selectedCustomersMap.entrySet()) {
            final TestCustomer testCustomer = testCustomerEntry.getValue();
            notifyUserTask(Level.INFO, "\n\tKunde: " + testCustomer.getCustomerName());

            // Betrachte Export-Protokolle ab ein Tag vorher als LastJobStartetAt des Kunden
            Calendar fromCal = testCustomer.getLastJobStartetAt();
            fromCal.add(Calendar.HOUR_OF_DAY, -1);

            exportProResultsSB.append("\n\tKunde: ").append(testCustomer.getCustomerName()).append("[").append(testCustomer.getCustomerKey()).append("]");
            exportProResultsSB.append("\n\t---------------------------------------");
            testCustomer.getTestScenariosMap().entrySet().forEach(testScenarioEntry -> {
                TestScenario testScenario = testScenarioEntry.getValue();
                List<TestCrefo> testCrefosList = testScenario.getTestCrefosAsList();
                if (!testCrefosList.isEmpty()) {
                    // notifyUserTask(Level.INFO, "\n\t\tScenario: " + testScenario.getScenarioName());
                    exportProResultsSB.append("\n\t\tScenario: ").append(testScenario.getScenarioName());
                    final TesunExportTrackingErgebnis exportTrackingInfo = tesunRestService.getExportTrackingInfo(testCrefosList, fromCal.getTime(), null);
                    final List<TesunExportTrackingCrefo> tesunExportTrackingCrefoList = exportTrackingInfo.getCrefoTracking();
                    Map<Long, TesunExportTrace> crefoToTesunExportTraceMap = filterForCustomer(tesunExportTrackingCrefoList, testCustomer);
                    crefoToTesunExportTraceMap.keySet().forEach(theCrefo -> {
                        TesunExportTrace tesunExportTrace = crefoToTesunExportTraceMap.get(theCrefo);
                        exportProResultsSB.append("\n\t\t\tCrefo: ").append(theCrefo);
                        exportProResultsSB.append(", Status: ").append(tesunExportTrace.getStatus());
                        exportProResultsSB.append(", Export-Datum: ").append(TesunDateUtils.formatCalendar(tesunExportTrace.getExportDatum()));
                        if (tesunExportTrace.getLoeschDatum() != null) {
                            exportProResultsSB.append(", Lösch-Datum: ").append(TesunDateUtils.formatCalendar(tesunExportTrace.getLoeschDatum()));
                        }
                    });
                }
            });
        }
        FileUtils.writeStringToFile(exportProResultsFile, exportProResultsSB.toString());
        notifyUserTask(Level.INFO, "\nDas Ergebnis der Export-Protokoll-Ptüfung wurde in die Datei\n\t" + exportProResultsFile.getAbsolutePath() + "\ngespeichert.");
        return crefosWithoutExportProtMap;
    }

    private Map<Long, TesunExportTrace> filterForCustomer(List<TesunExportTrackingCrefo> tesunExportTrackingCrefoList, TestCustomer testCustomer) {
        Map<Long, TesunExportTrace> crefoToTesunExportTraceMap = new HashMap<>();
        tesunExportTrackingCrefoList.forEach(tesunExportTrackingCrefo -> {
            long crefo = tesunExportTrackingCrefo.getCrefo();
            List<TesunExportTrace> tesunExportTraceList = tesunExportTrackingCrefo.getLetzteExporte();
            for (TesunExportTrace tesunExportTrace : tesunExportTraceList) {
                if (tesunExportTrace.getExportTyp().startsWith(testCustomer.getCustomerKey())) {
                    crefoToTesunExportTraceMap.put(crefo, tesunExportTrace);
                    break;
                }
            }
        });
        return crefoToTesunExportTraceMap;
    }

}
