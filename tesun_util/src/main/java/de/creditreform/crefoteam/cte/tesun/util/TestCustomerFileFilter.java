package de.creditreform.crefoteam.cte.tesun.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestCustomerFileFilter implements FileFilter {
    private final Map<String, TestCustomer> activeCustomersMap;
    private final boolean forRefExports;

    public TestCustomerFileFilter(Map<String, TestCustomer> activeCustomersMap, boolean forRefExports) {
        this.activeCustomersMap = activeCustomersMap;
        this.forRefExports = forRefExports;
    }

    @Override
    public boolean accept(File theFile) {
        String theFilesAbsolutePath = theFile.getAbsolutePath();
        for (Map.Entry<String, TestCustomer> entry : activeCustomersMap.entrySet()) {
            TestCustomer testCustomer = entry.getValue();
            String customersAbsolutePath = testCustomer.getItsqRefExportsDir().getAbsolutePath();
            if (theFilesAbsolutePath.equals(customersAbsolutePath)) {
                return true; // E:\Projekte\CTE\testsupport_client\TestSupportGUI\X-TESTS\ENE\LOCAL\TEST-CASES\crm
            } else if (theFilesAbsolutePath.startsWith(customersAbsolutePath)) {
                if (theFile.isDirectory()) {
                    return checkActiveTestScenarios(theFile, testCustomer.getTestScenariosList());
                } else {
                    boolean isOK = false;
                    if (theFilesAbsolutePath.endsWith(".properties")) {
                        isOK = checkActiveTestScenarios(theFile.getParentFile(), testCustomer.getTestScenariosList());
                    } else if (theFilesAbsolutePath.contains(TestSupportClientKonstanten.ADDITIONAL_BIC_FILENAME_PREFIX)) {
                        isOK = true;
                    } else if (theFilesAbsolutePath.endsWith(TestSupportClientKonstanten.ADDITIONAL_INFO_FILENAME_POSTFIX)) {
                        isOK = true;
                    } else if (theFilesAbsolutePath.matches(TestSupportClientKonstanten.CREFO_XML_FILE_MATCHER) || theFilesAbsolutePath.matches(TestSupportClientKonstanten.CREFO_TXT_FILE_MATCHER)) {
                        isOK = checkActiveTesctCrefos(theFile, testCustomer.getTestScenariosList());
                    }
                    return isOK;
                }
            }
        }
        return false;
    }

    private boolean checkActiveTesctCrefos(File theFile, List<TestScenario> testScenariosList) {
        List<TestCrefo> testCrefoList = new ArrayList<>();
        TestScenario testScenario = findScenarioFor(theFile.getParentFile(), testScenariosList);
        if (testScenario != null && testScenario.isActivated()) {
            Map<String, TestCrefo> testFallNameToTestCrefoMap = testScenario.getTestFallNameToTestCrefoMap();
            testFallNameToTestCrefoMap.entrySet().forEach(testCrefoEntry -> {
                TestCrefo testCrefo = testCrefoEntry.getValue();
                if (theFile.getName().contains(testCrefo.getItsqTestCrefoNr() + ".")) {
                    testCrefoList.add(testCrefo);
                }
            });
        }
        return !testCrefoList.isEmpty();
    }

    private TestScenario findScenarioFor(File theFile, List<TestScenario> testScenariosList) {
        for (TestScenario testScenario : testScenariosList) {
            if (theFile.getName().endsWith(testScenario.getScenarioName())) {
                return testScenario;
            }
        }
        return null;
    }

    private boolean checkActiveTestScenarios(File theFile, List<TestScenario> testScenariosList) {
        for (TestScenario testScenario : testScenariosList) {
            if (testScenario.isActivated()) {
                if (theFile.getName().endsWith(testScenario.getScenarioName())) {
                    return true;
                }
            }
        }
        return false;
    }
}

