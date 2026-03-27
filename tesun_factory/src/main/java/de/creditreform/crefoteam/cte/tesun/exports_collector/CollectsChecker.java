package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.jaxbbasics.jaxbbasicscommon.JaxbBasicsPrettyPrinting;
import de.creditreform.crefoteam.cte.jaxbbasics.jaxbutil.CteJaxbBasics;
import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzDecisionMonitoring;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestResults;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.apache.log4j.Level;

public class CollectsChecker {

    private List<Long> deletedCrefosList;
    private final TesunClientJobListener tesunClientJobListener;
    private final EnvironmentConfig environmentConfig;

    public CollectsChecker(EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        this.environmentConfig = environmentConfig;
        this.tesunClientJobListener = tesunClientJobListener;
    }

    public void checkTestCustomerCollects(TestCustomer testCustomer) {
        deletedCrefosList = null;
        notifyTesunClientJobListener(Level.INFO, "\nPrüfe die COLLECTS des Kunden " + testCustomer.getCustomerKey() + "...");
        Map<String, TestScenario> testScenariosMap = testCustomer.getTestScenariosMap();
        testScenariosMap.entrySet().stream().forEach(entry -> {
            TestScenario testScenario = entry.getValue();
            if (testScenario.isActivated()) {
                try {
                    checkTestScenarioCollects(testScenario);
                } catch (Exception ex) {
                    TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(ex.getMessage());
                    testScenario.addResultInfo(TestFallCheckCollects.COMMAND, resultInfo);
                    notifyTesunClientJobListener(Level.ERROR, "\n"+ex.getMessage());
                }
            }
        });
    }

    public void checkTestScenarioCollects(TestScenario testScenario) throws IOException {
        notifyTesunClientJobListener(Level.INFO, "\n\tPrüfe die COLLECTS des Scenarios '" + testScenario.getScenarioName() + "' für den Kunden '" + testScenario.getTestCustomer().getCustomerKey() + "'...");
        List<File> collectedXmlsFilesList = TesunUtilites.getFilesFromDir(testScenario.getCollectedsFile(), TestSupportClientKonstanten.SUPPORTED_EXPORT_MATCHER);
        List<File> pseudoRefExportsXmlsFilesList = TesunUtilites.getFilesFromDir(testScenario.getPseudoRefExportsFile(), TestSupportClientKonstanten.SUPPORTED_EXPORT_MATCHER);
        deletedCrefosList = null;
        for (TestCrefo testCrefo : testScenario.getTestCrefosAsList()) {
            String strInfo;
            // finde die entsprechende XML-Datei für diese Crefo...
            File xmlFile = findXmlFileForCrefo(collectedXmlsFilesList, testCrefo);
            boolean deletedCrefo = isDeletedCrefo(pseudoRefExportsXmlsFilesList, testCrefo.getPseudoCrefoNr());
            if (testCrefo.getTestFallName().startsWith("p")) {
                // der Testfall war ein Positiv-Test...
                checkPositifTestfall(testScenario, testCrefo, xmlFile, deletedCrefo);
            } else if (testCrefo.getTestFallName().startsWith("n")) {
                // der Testfall war ein Negativ-Test...
                checkNegativTestfals(testScenario, testCrefo, xmlFile, deletedCrefo);
            } else if (testCrefo.getTestFallName().startsWith("x")) {
                // der Testfall war ein X-Negativ-Test...
                checkXFall(testScenario, testCrefo, xmlFile, deletedCrefo);
            }
        }
/*
        if(testResults != null && !testResults.getResultInfosMap().isEmpty()) {
            notifyTesunClientJobListener(Level.ERROR, String.format("\nSammle Analyse-Informationen über die gesammelten Exports für das Test-Scenario %s\n", testScenario.getScenarioName()));
        }
*/
    }

    private void checkXFall(TestScenario testScenario, TestCrefo testCrefo, File xmlFile, boolean deletedCrefo) {
        String strInfo;
        if (xmlFile == null) {
            // ... aber es wurde nicts exportiert --> Fehler
            strInfo = String.format("Für den Testfall %s wurde kein Löschsatz exportiert!", testCrefo);
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(testCrefo.getPseudoCrefoNr(), strInfo);
            testScenario.addResultInfo(TestFallCheckCollects.COMMAND, resultInfo);
            notifyTesunClientJobListener(Level.ERROR, "\n\t\t" + strInfo);
        } else {
            if (deletedCrefo) {
                // ... und es existiert ein Löschsatz-Datei --> OK
                strInfo = String.format("Für den Testfall %s wurde ERWARTUNGSGEMÄß ein Löschsatz '%s' exportiert.", testCrefo, xmlFile.getName());
                notifyTesunClientJobListener(Level.INFO, "\n\t\t" + strInfo);
            } else {
                // ... und es existiert ein Exportsatz-Datei --> Fehler
                strInfo = String.format("Für den Testfall %s wurde UNERWARTETERWEISE ein Exportsatz '%s' exportiert!", testCrefo, xmlFile.getName());
                TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(testCrefo.getPseudoCrefoNr(), strInfo);
                testScenario.addResultInfo(TestFallCheckCollects.COMMAND, resultInfo);
                notifyTesunClientJobListener(Level.ERROR, "\n\t\t" + strInfo);
            }
        }
    }

    private void checkNegativTestfals(TestScenario testScenario, TestCrefo testCrefo, File xmlFile, boolean deletedCrefo) {
        String strInfo;
        if (xmlFile == null) {
            // ... und es wurde auch nicts exportiert --> OK
            strInfo = String.format("Für den Testfall %s wurde ERWARTUNGSGEMÄß nichts exportiert.", testCrefo);
            notifyTesunClientJobListener(Level.INFO, "\n\t\t" + strInfo);
        } else {
            if (deletedCrefo) {
                // ... und es existiert ein Löschsatz-Datei --> OK
                strInfo = String.format("Für den Testfall %s wurde ERWARTUNGSGEMÄß ein Löschsatz '%s' exportiert.", testCrefo, xmlFile.getName());
                notifyTesunClientJobListener(Level.INFO, "\n\t\t" + strInfo);
            } else {
                // ... und es existiert ein Exportsatz-Datei --> Fehler
                strInfo = String.format("Für den Testfall %s wurde UNERWARTETERWEISE ein Exportsatz '%s' exportiert!", testCrefo, xmlFile.getName());
                TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(testCrefo.getPseudoCrefoNr(), strInfo);
                testScenario.addResultInfo(TestFallCheckCollects.COMMAND, resultInfo);
                notifyTesunClientJobListener(Level.ERROR, "\n\t\t" + strInfo);
            }
        }
    }

    private void checkPositifTestfall(TestScenario testScenario, TestCrefo testCrefo, File xmlFile, boolean deletedCrefo) {
        String strInfo;
        if (xmlFile == null) {
            // ... aber es existiert keine passende Datei --> Fehler
            strInfo = String.format("!Testfall %s MÜSSTE exportiert werden!", testCrefo);
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(testCrefo.getPseudoCrefoNr(), strInfo);
            testScenario.addResultInfo(TestFallCheckCollects.COMMAND, resultInfo);
            notifyTesunClientJobListener(Level.ERROR, "\n\t\t" + strInfo);
        } else {
            if (deletedCrefo && !testCrefo.getTestFallName().startsWith("p")) {
                // ... aber es existiert ein Löschsatz-Datei --> Fehler
                strInfo = String.format("!Für den Testfall %s wurde UNERWARTETERWEISE ein Löschsatz '%s' exportiert!", testCrefo, xmlFile.getName());
                TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(testCrefo.getPseudoCrefoNr(), strInfo);
                testScenario.addResultInfo(TestFallCheckCollects.COMMAND, resultInfo);
                notifyTesunClientJobListener(Level.ERROR, "\n\t\t" + strInfo);
            } else {
                // ... und es existiert ein passender Exportsatz-Datei --> OK
                strInfo = String.format("Für den Testfall %s wurde Exportsatz '%s' exportiert.", testCrefo, xmlFile.getName());
                notifyTesunClientJobListener(Level.INFO, "\n\t\t" + strInfo);
            }
        }
    }

    protected void collectCrefoAnaylseInfos(TestScenario testScenario, List<TestResults.ResultInfo> resultInfos) {
        try {
            List<Long> crefosList = resultInfos.stream().map(resultInfo -> resultInfo.getCrefoNummer()).collect(Collectors.toList());
            TesunRestService tesunRestService = createTesunRestServiceInstance();
            List<RelevanzDecisionMonitoring> relevanzDecisionMonitoringsList = tesunRestService.getCrefoAnaylseInfos(testScenario.getCusomerKey(), crefosList);
            CteJaxbBasics cteJaxbBasics = new CteJaxbBasics(RelevanzDecisionMonitoring.class.getPackage());
            int index = 0;
            for (RelevanzDecisionMonitoring relevanzDecisionMonitoring : relevanzDecisionMonitoringsList) {
                TestResults.ResultInfo resultInfo = resultInfos.get(index++);
                StringWriter stringWriter = new StringWriter();
                cteJaxbBasics.marshalJSON(JaxbBasicsPrettyPrinting.EXTRA_PRETTY_PRINTING).toWriter(stringWriter, relevanzDecisionMonitoring);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\nAnalyse-Informationen über Test-Crefo\n");
                stringBuilder.append(stringWriter);
                resultInfo.appendToErrorStr(stringBuilder.toString());
                stringWriter.close();
            }
        } catch (Exception ex) {
            notifyTesunClientJobListener(Level.ERROR, ex.getMessage());
        }
    }

    protected TesunRestService createTesunRestServiceInstance() throws PropertiesException {
        TesunRestService tesunRestService = new TesunRestService(environmentConfig.getRestServiceConfigsForJvmImpCycle().get(0), tesunClientJobListener);
        return tesunRestService;
    }

    protected void writeToFile(File msgListFile, String strContent) throws IOException {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(msgListFile, false), StandardCharsets.UTF_8));
            bufferedWriter.write(strContent);
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }

    protected File findXmlFileForCrefo(List<File> xmlsFilesList, TestCrefo testCrefo) {
        for (File xmlFile : xmlsFilesList) {
            Long pseudoCrefoNr = testCrefo.getPseudoCrefoNr();
            String xmlFileName = xmlFile.getName();
            boolean crefoOK = xmlFileName.contains(pseudoCrefoNr + ".");
            if (crefoOK) {
                if (xmlFileName.contains("loesch") || xmlFileName.contains("delete") || xmlFileName.contains("stopmessage")) {
                    if (testCrefo.getTestFallName().startsWith("x")) {
                        return xmlFile;
                    }
                } else {
                    if (testCrefo.getTestFallName().startsWith("p") || testCrefo.getTestFallName().startsWith("n")) {
                        return xmlFile;
                    }
                }
            }
        }
        return null;
    }

    protected List<Long> getDeletedCrefosList(List<File> xmlsFilesList) {
        if (deletedCrefosList == null) {
            deletedCrefosList = new ArrayList<>();
            for (File xmlFile : xmlsFilesList) {
                String xmlName = xmlFile.getName();
                for (String prefix : TestSupportClientKonstanten.LOESCHSATZ_FILENAMES_PREFIX) {
                    if (xmlName.contains(prefix)) {
                        Matcher matcher = TestSupportClientKonstanten.CREFO_NUMBER_PATTERN.matcher(xmlName);
                        if (matcher.find()) {
                            String crefoNr = matcher.group(0);
                            deletedCrefosList.add(Long.valueOf(crefoNr.substring(0, 10)));
                        }
                    }
                }
            }
        }
        return deletedCrefosList;
    }

    protected boolean isDeletedCrefo(List<File> xmlsFilesList, Long crefo) {
        return getDeletedCrefosList(xmlsFilesList).contains(crefo);
    }

    protected void notifyTesunClientJobListener(Level level, String strInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, strInfo);
        }
    }
}
