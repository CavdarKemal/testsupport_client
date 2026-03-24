package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.exports_collector.TestFallCollectExportedCrefos;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.*;
import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacementMapping;
import de.creditreform.crefoteam.cteinsoexporttesun.insoxmlbinding.TesunInsoAktuellerStand;
import de.creditreform.crefoteam.cteinsoexporttesun.insoxmlbinding.TesunInsoSnippet;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UserTaskStartCollect extends AbstractUserTaskRunnable {

    public UserTaskStartCollect(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        // ist ACTIVITI-Prozess oder Manuell-Test?
        checkForWait(taskVariablesMap, TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_EXPORTS_COLLECT);

        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        Map<String, TestCustomer> selectedCustomersMapPhaseX = selectedCustomersMapMap.get(testPhase);
        // check, ob bei allen TestCustomers JobStartetAt gesetzt ist
        checkLastJobStartForCustomers(selectedCustomersMapPhaseX);

        doStartTesunClientJob(new TestFallCollectExportedCrefos(selectedCustomersMapPhaseX, testPhase, tesunClientJobListener));
        collectSnippetsForInso(selectedCustomersMapPhaseX);
        testResultsZipHandler.writeTestResultsToFile(selectedCustomersMapPhaseX);
        return taskVariablesMap;
    }

    private void collectSnippetsForInso(Map<String, TestCustomer> selectedCustomersMap) throws Exception {
        Iterator<String> iterator = selectedCustomersMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.startsWith("INSO")) {
                TestCustomer insoTestCustomer = selectedCustomersMap.get(key);
                collectInsoSnippeds(insoTestCustomer);
            }
        }
    }

    private void collectInsoSnippeds(TestCustomer insomon1TestCustomer) throws Exception {
        // spezielle Coolect-Action für INSO...
        notifyUserTask(Level.INFO, "\n\tSammle INSO-Snippets für '" + insomon1TestCustomer.getCustomerName() + "'...");
        final File mappingsFile = new File(environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.fileNameCrefosMapping);
        Map<String, ReplacementMapping> replacementMappingMap = TestFallFileUtil.readReplacementMappingFromFile(mappingsFile);
        TesunRestService tesunRestService = new TesunRestService(environmentConfig.getRestServiceConfigsForJvmInso().get(0), tesunClientJobListener);
        List<TestScenario> testScenariosList = insomon1TestCustomer.getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            List<TestCrefo> testCrefosList = testScenario.getTestCrefosAsList();
            List<Long> pseudoCrefosList = mapToPseudoCrefos(replacementMappingMap, testCrefosList);
            for (Long crefo : pseudoCrefosList) {
                TesunInsoAktuellerStand tesunInsoAktuellerStand = readreadInsoProductForCrefoWithRetry(tesunRestService, crefo);
                List<TesunInsoSnippet> tesunInsoSnippetList = tesunInsoAktuellerStand.getTesunInsoSnippet();
                for (TesunInsoSnippet tesunInsoSnippet : tesunInsoSnippetList) {
                    final String formattedXMLContent = TesunUtilites.toPrettyString(tesunInsoSnippet.getSnippetXmldaten(), 2);
                    String xmlFileName = tesunInsoSnippet.getSnippetContentType() + "-" + tesunInsoSnippet.getSnippetMatchcode().replaceAll("/", "#") + "-" + crefo + ".xml";
                    File xmlFile = new File(testScenario.getCollectedsFile(), xmlFileName);
                    FileUtils.writeStringToFile(xmlFile, formattedXMLContent, StandardCharsets.UTF_8);
                }
            }
        }
    }

    private TesunInsoAktuellerStand readreadInsoProductForCrefoWithRetry(TesunRestService tesunRestService, Long crefo) throws Exception {
        int numRetries = 0;
        while (true) {
            try {
                TesunInsoAktuellerStand tesunInsoAktuellerStand = tesunRestService.readInsoProductForCrefo(crefo);
                return tesunInsoAktuellerStand;
            } catch (Exception ex) {
                Boolean retry = (Boolean) askUserTask(TesunClientJobListener.ASK_FOR.ASK_OBJECT_RETRY, ex.getMessage());
                if (!retry.booleanValue()) {
                    throw ex;
                }
                if (++numRetries > 3) {
                    throw ex;
                }
            }
        }
    }

    private List<Long> mapToPseudoCrefos(Map<String, ReplacementMapping> replacementMappingMap, List<TestCrefo> testCrefosList) {
        List<Long> resultList = new ArrayList<>();
        for (TestCrefo testCrefo : testCrefosList) {
            ReplacementMapping replacementMapping = replacementMappingMap.get(testCrefo.getItsqTestCrefoNr().toString());
            Long pseudoCrefo = replacementMapping.getTargetCrefo();
            if (pseudoCrefo != null) {
                // nur die p-Fälle übernehmen!
                if (testCrefo.isShouldBeExported()) {
                    resultList.add(pseudoCrefo);
                }
            } else {
                notifyUserTask(Level.ERROR, "\nTest-Crefo [" + testCrefo.getItsqTestCrefoNr() + ":" + testCrefo.getPseudoCrefoNr() + "] wurde in der Crefos-Map nicht gefunden!");
            }
        }
        return resultList;
    }

}
