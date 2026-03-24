package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.jaxbbasics.jaxbbasicscommon.JaxbBasicsPrettyPrinting;
import de.creditreform.crefoteam.cte.jaxbbasics.jaxbutil.CteJaxbBasics;
import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzDecisionMonitoring;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.log4j.Level;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class UserTaskCrefoAnalyseErgebnisse extends AbstractUserTaskRunnable {
    public UserTaskCrefoAnalyseErgebnisse(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        TesunRestService tesunRestService = new TesunRestService(environmentConfig.getRestServiceConfigsForJvmImpCycle().get(0), tesunClientJobListener);
        CteJaxbBasics cteJaxbBasics = new CteJaxbBasics(RelevanzDecisionMonitoring.class.getPackage());
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        Map<String, TestCustomer> selectedCustomersMap = selectedCustomersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);// TODO PHASE-1 ???!!!
        for (Map.Entry<String, TestCustomer> testCustomerEntry : selectedCustomersMap.entrySet()) {
            final TestCustomer testCustomer = testCustomerEntry.getValue();
            final List<Long> crefosList = testCustomer.getAllTestCrefosAsLongList(true, false); // nur die aktive und positive+negative Testfälle!
            if (!crefosList.isEmpty()) {
                notifyUserTask(Level.INFO, String.format("\nFür '%s' werden Crefo-Analyse-Ergebnisse erfragt...", testCustomer.getCustomerKey()));
                String customerKey = testCustomerEntry.getKey();
                List<RelevanzDecisionMonitoring> relevanzDecisionMonitoringsList = tesunRestService.getCrefoAnaylseInfos(customerKey, crefosList);
                if (relevanzDecisionMonitoringsList != null) {
                    for (RelevanzDecisionMonitoring relevanzDecisionMonitoring : relevanzDecisionMonitoringsList) {
                        notifyUserTask(Level.INFO, "\nAnlayse-Ergebnisse für Crefo " + relevanzDecisionMonitoring.getCrefo() + ":");
                        StringWriter stringWriter = new StringWriter();
                        cteJaxbBasics.marshalJSON(JaxbBasicsPrettyPrinting.EXTRA_PRETTY_PRINTING).toWriter(stringWriter, relevanzDecisionMonitoring);
                        notifyUserTask(Level.INFO, "\n" + stringWriter);
                    }
                } else {
                    notifyUserTask(Level.ERROR, "REST-Service '/cte_tesun_service/tesun/crefoanalyse/' nicht verfügbar!");
                }
            } else {
                notifyUserTask(Level.WARN, String.format("\nFür '%s' sind keine Crefos als Testfall angegeben!", testCustomer.getCustomerKey()));
            }
        }
        String strInfo = "\n\nCrefo-Analyse-Ergebnisse wurden für die Crefos der aktiven Kunden ermittelt.";
        notifyUserTask(Level.INFO, strInfo);
        testResultsZipHandler.writeTestResultsToFile(selectedCustomersMap);
        return taskVariablesMap;
    }

}
