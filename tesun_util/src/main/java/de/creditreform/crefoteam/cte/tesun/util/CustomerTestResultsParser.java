package de.creditreform.crefoteam.cte.tesun.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CustomerTestResultsParser {

    public Map<String, TestCustomer> parseTestResultsFile(File testResultsFile) throws Exception {
        Map<String, TestCustomer> testCustomerMap = new TreeMap<>();
        List<String> linesList = FileUtils.readLines(testResultsFile);
        String strLine = getNextLine(linesList);
        String command = "";
        TestCustomer testCustomer = null;
        while (strLine != null ) {
            if (strLine.startsWith("Test-Results für den Kunden")) {
                String customerKey = strLine.split("'")[1];
                testCustomer = new TestCustomer(customerKey, customerKey);
                strLine = checkLineForCustomer(testCustomer, strLine, linesList);
                testCustomerMap.put(customerKey, testCustomer);
            } else if ((strLine != null) && strLine.startsWith("Test-Results für UserTask")) {
                command = strLine.split("'")[1].trim();
                strLine = addTestResultsForCommand(testCustomer, strLine, linesList);
            } else if ((strLine != null) && strLine.startsWith("Test-Results für Test-Scenario")) {
                String scenarioName = strLine.split("'")[1].trim();
                strLine = addTestResultsForScenrio(testCustomer, command, scenarioName, linesList, testResultsFile);
            }
        }
        return testCustomerMap;
    }

    public void parseTestResultsFile(File testResultsFile, TestCustomer testCustomer) throws Exception {
        List<String> linesList = FileUtils.readLines(testResultsFile);
        String strLine = getNextLine(linesList);
        String command = "";
        while ((strLine != null) && !linesList.isEmpty()) {
            if (strLine.startsWith("Test-Results für den Kunden")) {
                strLine = checkLineForCustomer(testCustomer, strLine, linesList);
            } else if ((strLine != null) && strLine.startsWith("Test-Results für UserTask")) {
                command = strLine.split("'")[1].trim();
                strLine = addTestResultsForCommand(testCustomer, strLine, linesList);
            } else if ((strLine != null) && strLine.startsWith("Test-Results für Test-Scenario")) {
                String scenarioName = strLine.split("'")[1].trim();
                strLine = addTestResultsForScenrio(testCustomer, command, scenarioName, linesList, testResultsFile);
            }
        }
    }

    private String getNextLine(List<String> linesList) {
        if (linesList.isEmpty()) {
            return null;
        }
        if (linesList.get(0).isBlank()) {
            linesList.remove(0);
        }
        return linesList.remove(0).trim();
    }

    private String addTestResultsForScenrio(TestCustomer testCustomer, String command, String scenarioName, List<String> linesList, File testResultsFile) throws Exception {
        String strLine = getNextLine(linesList);
        TestScenario testScenario = testCustomer.getTestScenariosMap().get(scenarioName);
        if (testScenario == null) {
            testScenario = new TestScenario(testCustomer, scenarioName);
            testCustomer.addTestScenario(testScenario);
        }
        while ((strLine != null) && !strLine.startsWith("Test-Results für den Kunden") && !strLine.startsWith("Test-Results für UserTask") && !strLine.startsWith("Test-Results für Test-Scenario")) {
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(strLine);
            testScenario.addResultInfo(command, resultInfo);
            if (strLine.startsWith("Unterschiede beim Testfall")) {
                strLine = addTestResultsForDiffInfo(resultInfo, strLine, linesList, testResultsFile);
            } else {
                strLine = getNextLine(linesList);
            }
        }
        return strLine;
    }

    private String addTestResultsForDiffInfo(TestResults.ResultInfo resultInfo, String strLine, List<String> linesList, File testResultsFile) throws Exception {
        TestResults.DiffenrenceInfo diffenrenceInfo = paserseDiffFromErrorString(strLine, testResultsFile);
        if (diffenrenceInfo != null) {
            resultInfo.addDifferences(diffenrenceInfo);
        }
        strLine = getNextLine(linesList);
        return strLine;
    }

    protected TestResults.DiffenrenceInfo paserseDiffFromErrorString(String errorStr, File testResultsFile) throws Exception {
        String strTemp1 = testResultsFile.getAbsolutePath().split(TestSupportClientKonstanten.CHECKED)[0];
        String strTemp2 = errorStr.split(TestSupportClientKonstanten.CHECKED)[1];
        File diffFile = new File(new File(strTemp1, TestSupportClientKonstanten.CHECKED), strTemp2);

        // Unterschiede beim Testfall 'p001_01:4110109457[4123439802]'! Diff-Datei: 'E:\Projekte\CTE\testsupport_client\TestSupportGUI\X-TESTS\GEE\TEST-OUTPUTS\CHECKED\PHASE-1\rtn\Relevanz_Positiv\p001_01-diff.xml'
        String[] splitMain = errorStr.split("'");

        String[] splitTestfall = splitMain[1].split(":"); // "p012_19:7330002775[4123856466]"
        //        1 = "4110109457[4123439802]"
        //        0 = "p001_01"
        String testFallName = splitTestfall[0]; // "p012_19"

        String[] splitCrefos = StringUtils.split(splitTestfall[1], "[]"); // "7330002775[4123856466]"
        // 0 = "7330002775"
        // 1 = "4123856466"

        File xmlFileSrc = buildXmlFileFor(diffFile, testFallName, splitCrefos[0], "REF-EXPORTS"); // E:\Projekte\Branches\testsupport_client\tesun_util\src\test\resources\LOCAL\TEST-OUTPUTS\CHECKED\bdr\Relevanz_Positiv\REF-EXPORTS\p012_19-stammcrefo_7330002775.xml
        File xmlFileDst = buildXmlFileFor(diffFile, testFallName, splitCrefos[0], "RESTORED-COLLECTS"); // E:\Projekte\Branches\testsupport_client\tesun_util\src\test\resources\LOCAL\TEST-OUTPUTS\CHECKED\bdr\Relevanz_Positiv\RESTORED-COLLECTS\p012_19-stammcrefo_7330002775.xml

        List<Difference> differenceList = compareContent(FileUtils.readFileToString(xmlFileSrc), FileUtils.readFileToString(xmlFileDst));
        TestResults.DiffenrenceInfo diffenrenceInfo = new TestResults.DiffenrenceInfo(testFallName, xmlFileSrc, xmlFileDst, diffFile, differenceList);
        return diffenrenceInfo;
    }

    private List<Difference> compareContent(String actualContent, String expectedContent) throws Exception {
        List<Difference> allDifferences = new ArrayList<>();
        if (!expectedContent.isBlank() && !actualContent.isBlank()) {
            DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(expectedContent, actualContent));
            allDifferences = diff.getAllDifferences();
        }
        return allDifferences;
    }

    private File buildXmlFileFor(File diffFile, String testFallName, String strCrefo, String subDirName) {
        File file = new File(diffFile.getParentFile(), subDirName);
        Collection<File> fileCollection = FileUtils.listFiles(file, new String[]{"xml"}, false);
        List<File> collected = fileCollection.stream().filter(xmlFile -> xmlFile.getName().contains(testFallName) && xmlFile.getName().contains(strCrefo)).collect(Collectors.toList());
        if (collected != null && !collected.isEmpty()) {
            return collected.get(0);
        }
        throw new IllegalStateException("Crefo-File nicht gefunden!");
    }

    private String checkLineForCustomer(TestCustomer testCustomer, String strLine, List<String> linesList) {
        String[] split = strLine.split("'");
        String customerKey = split[1].trim();
        if (!testCustomer.getCustomerKey().equalsIgnoreCase(customerKey)) {
            throw new IllegalStateException("Customer-Key '" + customerKey + "' passt nicht dem übergebenen TestCustomer '" + testCustomer + "'!");
        }
        strLine = getNextLine(linesList);
        return strLine;
    }

    private String addTestResultsForCommand(TestCustomer testCustomer, String strLine, List<String> linesList) {
        String command = strLine.split("'")[1].trim();
        testCustomer.addTestResultsForCommand(command);
        strLine = getNextLine(linesList);
        StringBuilder errorBuilder = new StringBuilder();
        while ((strLine != null) && !strLine.startsWith("Test-Results für den Kunden") && !strLine.startsWith("Test-Results für UserTask") && !strLine.startsWith("Test-Results für Test-Scenario")) {
            errorBuilder.append(strLine.trim() + "\n");
            strLine = getNextLine(linesList);
        }
        if (errorBuilder.length() > 0) {
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorBuilder.toString());
            testCustomer.addResultInfo(command, resultInfo);
        }
        return strLine;
    }

}
