package de.creditreform.crefoteam.cte.tesun.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TestResultsParser {
    private final File testResultsFile;

    public TestResultsParser(File testResultsFile) {
        this.testResultsFile = testResultsFile;
    }

    public Map<String, TestCustomer> parseTestResultsFile() throws Exception {
        Map<String, TestCustomer> testCustomersMap = new TreeMap<>();
        List<String> linesList = FileUtils.readLines(testResultsFile);
        String strLine = linesList.remove(0);
        while (!linesList.isEmpty()) {
            if (strLine.contains("Test-Results für den Kunden")) {
                String[] split = strLine.split("'");
                String customerName = split[1].trim();
                TestCustomer testCustomer = new TestCustomer(customerName, customerName);
                testCustomersMap.put(customerName, testCustomer);
                if (linesList.isEmpty()) {
                    throw new IllegalStateException("Datei unerwartet beendet!");
                }
                strLine = linesList.remove(0);
                while (!strLine.contains("Test-Results für den Kunden")) {
                    if (strLine.contains("Test-Results für UserTask")) {
                        String command = strLine.split("'")[1].trim();
                        testCustomer.addTestResultsForCommand(command);
                        if (linesList.isEmpty()) {
                            throw new IllegalStateException("Datei unerwartet beendet!");
                        }
                        strLine = linesList.remove(0);
                        if (!strLine.contains("Test-Results für UserTask") && !strLine.contains("Test-Results für den Kunden") && !strLine.contains("Test-Results für Test-Scenario")) {
                            String errorStr = strLine.trim();
                            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                            testCustomer.addResultInfo(command, resultInfo);
                            if (linesList.isEmpty()) {
                                return testCustomersMap;
                            }
                            strLine = linesList.remove(0);
                        } else {
                            while (!strLine.contains("Test-Results für UserTask") && !strLine.contains("Test-Results für den Kunden")) {
                                if (strLine.contains("Test-Results für Test-Scenario")) {
                                    String scenarioName = strLine.split("'")[1].trim();
                                    TestScenario testScenario = testCustomer.getScenario(scenarioName);
                                    if (testScenario == null) {
                                        testScenario = new TestScenario(testCustomer, scenarioName, null);
                                        testCustomer.addTestScenario(testScenario);
                                    }
                                    if (linesList.isEmpty()) {
                                        throw new IllegalStateException("Datei unerwartet beendet!");
                                    }
                                    strLine = linesList.remove(0);
                                    while (!strLine.contains("Test-Results für Test-Scenario") && !strLine.contains("Test-Results für UserTask") && !strLine.contains("Test-Results für den Kunden")) {
                                        String errorStr = strLine.trim();
                                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                                        if (errorStr.contains("Unterschiede beim Testfall")) {
                                            TestResults.DiffenrenceInfo diffenrenceInfo = paserseDiffFromErrorString(errorStr);
                                            if (diffenrenceInfo != null) {
                                                resultInfo.addDifferences(diffenrenceInfo);
                                            }
                                        }
                                        testScenario.addResultInfo(command, resultInfo);
                                        if (linesList.isEmpty()) {
                                            return testCustomersMap;
                                        }
                                        strLine = linesList.remove(0);
                                    }
                                } else {
                                    if (linesList.isEmpty()) {
                                        return testCustomersMap;
                                    }
                                    strLine = linesList.remove(0);
                                }
                            } // while (!linesList.isEmpty() && !strLine.contains("Test-Results für UserTask") && !strLine.contains("Test-Results für den Kunden"))
                        } // else
                    } // if (strLine.contains("Test-Results für UserTask"))
                } // while (!linesList.isEmpty() && !strLine.contains("Test-Results für den Kunden"))
            } // if (strLine.contains("Test-Results für den Kunden"))
            else {
                if (linesList.isEmpty()) {
                    return testCustomersMap;
                }
                strLine = linesList.remove(0);
            }
        } // while !linesList.isEmpty()
        return testCustomersMap;
    }

    private TestResults.DiffenrenceInfo paserseDiffFromErrorString(String errorStr) throws Exception {
        // Unterschiede beim Testfall 'p012_19:7330002775[4123871290]'! Diff-Datei: 'E:\Projekte\Branches\testsupport_client\TestSupportGUI\X-TESTS\ENE\TEST-OUTPUTS\CHECKED\bdr\Relevanz_Positiv\p012_19-diff.xml'
        String[] split = errorStr.split("'");
        //  0 = "Unterschiede beim Testfall: "
        //  1 = "p012_19:7330002775[4123856466]"
        //  2 = " Diff-Datei: "
        //  3 = "E:\Projekte\Branches\testsupport_client\TestSupportGUI\X-TESTS\ENE\TEST-OUTPUTS\CHECKED\bdr\Relevanz_Positiv\p012_19-diff.xml"

        String[] split1 = split[1].split(":"); // "p012_19:7330002775[4123856466]"
        //  0 = "p012_19"
        //  1 = "7330002775[4123856466]"

        String testFallName = split1[0]; // "p012_19"
        File diffFile = new File(split[3]);

        String[] splitCrefos = StringUtils.split(split1[1], "[]"); // "7330002775[4123856466]"
        // 0 = "7330002775"
        // 1 = "4123856466"

        File xmlFileSrc = buildXmlFileFor(diffFile, testFallName, splitCrefos[0], "REF-EXPORTS"); // E:\Projekte\Branches\testsupport_client\tesun_util\src\test\resources\LOCAL\TEST-OUTPUTS\CHECKED\bdr\Relevanz_Positiv\REF-EXPORTS\p012_19-stammcrefo_7330002775.xml
        File xmlFileDst = buildXmlFileFor(diffFile, testFallName, splitCrefos[0], "RESTORED-COLLECTS"); // E:\Projekte\Branches\testsupport_client\tesun_util\src\test\resources\LOCAL\TEST-OUTPUTS\CHECKED\bdr\Relevanz_Positiv\RESTORED-COLLECTS\p012_19-stammcrefo_7330002775.xml

        List<Difference> differenceList = compareContent(FileUtils.readFileToString(xmlFileSrc), FileUtils.readFileToString(xmlFileDst));
        TestResults.DiffenrenceInfo diffenrenceInfo = new TestResults.DiffenrenceInfo(testFallName, xmlFileSrc, xmlFileDst, diffFile, differenceList);
        return diffenrenceInfo;
    }

    public List<Difference> compareContent(String actualContent, String expectedContent) throws Exception {
        List<Difference> allDifferences = new ArrayList<>();
        if (!expectedContent.isBlank() && !actualContent.isBlank()) {
            DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(expectedContent, actualContent));
            allDifferences = diff.getAllDifferences();
        }
        return allDifferences;
    }


    private static File buildXmlFileFor(File diffFile, String testFallName, String strCrefo, String subDirName) {
        // E:\Projekte\Branches\testsupport_client\tesun_util\src\test\resources\LOCAL\TEST-OUTPUTS\CHECKED\bdr\Relevanz_Positiv\RESTORED-COLLECTS\p010_16-stammcrefo_5410000536.xml
        // E:\Projekte\Branches\testsupport_client\tesun_util\src\test\resources\LOCAL\TEST-OUTPUTS\CHECKED\bdr\Relevanz_Positiv\REF-EXPORTS\p010_16-stammcrefo_5410000536.xml
        File file = new File(diffFile.getParentFile(), subDirName);
        Collection<File> fileCollection = FileUtils.listFiles(file, new String[]{"xml"}, false);
        List<File> collected = fileCollection.stream().filter(xmlFile -> xmlFile.getName().contains(testFallName) && xmlFile.getName().contains(strCrefo)).collect(Collectors.toList());
        if (collected != null && !collected.isEmpty()) {
            return collected.get(0);
        }
        throw new IllegalStateException("Crefo-File nicht gefunden!");
    }

}
