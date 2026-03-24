package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.*;
import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacementMapping;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class UserTaskErneuteLieferungBeantragen extends AbstractUserTaskRunnable {
    public UserTaskErneuteLieferungBeantragen(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
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
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
        for (Map.Entry<String, TestCustomer> testCustomerEntry : selectedCustomersMapPhaseX.entrySet()) {
            final TestCustomer testCustomer = testCustomerEntry.getValue();
            final List<Long> activeTestCrefos = getTestCrefosFromNachlieferung(testCustomer);
            String processIdentifier = testCustomer.getProcessIdentifier();
            if (processIdentifier == null) {
                notifyUserTask(Level.INFO, String.format("\t!!! für den Kunden %s wurde kein Processidentifier hinterlegt!\nBitte mittels Masterkonsole die Property cteTestclient.%s.exportDir entsprechend setzen.",
                        testCustomer.getCustomerKey(), testCustomer.getCustomerKey().toLowerCase()));
                return taskVariablesMap;
            }
            if (!activeTestCrefos.isEmpty()) {
                notifyUserTask(Level.INFO, String.format("\nFür '%s' werden %d Crefos zur erneuten Lieferung beantragt...", processIdentifier, activeTestCrefos.size()));
                tesunRestService.erneuteLieferungBeantragen(activeTestCrefos, processIdentifier);
                notifyUserTask(Level.INFO, "\tOK");
                if (testCustomer.getCustomerKey().equals("IKA")) {
                    tesunRestService.createIkarosAuftrag(testCustomer);
                }
            } else {
                notifyUserTask(Level.WARN, String.format("\nFür '%s' sind keine Crefos als Testfall angegeben!", processIdentifier));
            }
        }
        String strInfo = "\n\nDie erneute Lieferung wurde für die Crefos der aktiven Kunden beantragt.";
        strInfo += "\nWICHTIG: Vor dem Start eines Tests muss mindestens 6 Minuten abgewartet werden!";
        strInfo += "\n--> Frühstmögliche Startzeit wäre also: ";
        Calendar nowCal = Calendar.getInstance();
        nowCal.add(Calendar.MINUTE, 6);
        strInfo += TesunDateUtils.formatCalendar(nowCal);
        notifyUserTask(Level.INFO, strInfo);
        testResultsZipHandler.writeTestResultsToFile(selectedCustomersMapPhaseX);
        return taskVariablesMap;
    }

    private List<Long> getTestCrefosFromMapping(TestCustomer testCustomer) throws Exception {
        List<Long> mappedCrefosList = new ArrayList<>();
        final File mappingsFile = new File(environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.fileNameCrefosMapping);
        Map<String, ReplacementMapping> replacementMappingMap = TestFallFileUtil.readReplacementMappingFromFile(mappingsFile);
        for (Long origCrefo : testCustomer.getAllTestCrefosAsLongList(true, false)) {
            ReplacementMapping replacementMapping = replacementMappingMap.get(origCrefo.toString());
            mappedCrefosList.add(replacementMapping.getTargetCrefo());
        }
        return mappedCrefosList;
    }

    private List<Long> getTestCrefosFromNachlieferung(TestCustomer testCustomer) throws PropertiesException, IOException {
        List<Long> crefosList = new ArrayList<>();
        String strFileName = testCustomer.getCustomerKey() + "/" + TestSupportClientKonstanten.fileNameCrefosFuerNachlieferung;
        File outputFile = new File(environmentConfig.getTestOutputsRoot(), "Nachlieferung");
        List<String> crefosLines = FileUtils.readLines(new File(outputFile, strFileName));
        for (String strCrefo : crefosLines) {
            crefosList.add(Long.valueOf(strCrefo));
        }
        return crefosList;
    }

}
