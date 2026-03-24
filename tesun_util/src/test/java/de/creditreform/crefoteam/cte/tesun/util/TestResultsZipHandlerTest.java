package de.creditreform.crefoteam.cte.tesun.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestResultsZipHandlerTest {

    Path testResultsZipFileIn;
    TestResultsZipHandler testResultsZipHandler = new TestResultsZipHandler();
    EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");

    @Before
    public void setUp() throws Exception {
        File testRscRootDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
        environmentConfig.setTestResourcesDir(testRscRootDir);
        String testResultsFileName = "/TESTS/TEST-OUTPUTS/TestResults.zip";
        URL resource = this.getClass().getResource(testResultsFileName);
        testResultsZipFileIn = Paths.get(resource.toURI());
    }

    @Test
    public void testWriteTestResultsToZipFile() throws Exception {
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> testCustomersMapMap = new HashMap<>();
        Path otuputPath = testResultsZipHandler.unzipRecursive(testResultsZipFileIn);
        for (TestSupportClientKonstanten.TEST_PHASE testPhase : TestSupportClientKonstanten.TEST_PHASE.values()) {
            Map<String, TestCustomer> testCustomersMap = testResultsZipHandler.initalizeTestCustomersMapFromDir(otuputPath, testPhase.getDirName());
            testCustomersMapMap.put(testPhase, testCustomersMap);
        }
        Iterator<TestSupportClientKonstanten.TEST_PHASE> iterator = testCustomersMapMap.keySet().iterator();
        while(iterator.hasNext()) {
            testResultsZipHandler.writeTestResultsToFile(testCustomersMapMap.get(iterator.next()));
        }
    }

    @Test
    public void testReadTestResultsFromZipFile() throws Exception {
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> testCustomersMapMap = new HashMap<>();
        Path otuputPath = testResultsZipHandler.unzipRecursive(testResultsZipFileIn);
        for (TestSupportClientKonstanten.TEST_PHASE testPhase : TestSupportClientKonstanten.TEST_PHASE.values()) {
            Map<String, TestCustomer> testCustomersMap = testResultsZipHandler.initalizeTestCustomersMapFromDir(otuputPath, testPhase.getDirName());
            testCustomersMapMap.put(testPhase, testCustomersMap);
        }
        checkTestCustomerMap(testCustomersMapMap);
    }

    private static void checkTestCustomerMap(Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> testCustomersMapMap) {
        testCustomersMapMap.keySet().forEach(testPhase -> {
            Map<String, TestCustomer> testCustomersMapPhaseX = testCustomersMapMap.get(testPhase);
            testCustomersMapPhaseX.keySet().forEach(customerKey -> {
                TestCustomer testCustomer = testCustomersMapPhaseX.get(customerKey);
                Path testCustomerResultPath = Paths.get(testCustomer.getTestResultsFile().getParent());
                Assert.assertTrue(Files.exists(testCustomerResultPath));
                for (Map.Entry<String, TestScenario> testScenarioEntry : testCustomer.getTestScenariosMap().entrySet()) {
                    TestScenario testScenario = testScenarioEntry.getValue();
                    Path scenariosResultPath = Paths.get(testCustomerResultPath.toFile().getAbsolutePath(), testScenario.getScenarioName());
                    Assert.assertTrue(Files.exists(scenariosResultPath));
                }
            });
        });
    }
}
