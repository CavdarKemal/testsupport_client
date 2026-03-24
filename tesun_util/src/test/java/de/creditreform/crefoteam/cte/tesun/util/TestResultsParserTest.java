package de.creditreform.crefoteam.cte.tesun.util;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Map;

public class TestResultsParserTest {
    private File testResultsFile;
    private TestResultsParser testResultsParser;

    @Before
    public void setUp() throws Exception {
        EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");
        File testRscRootDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
        environmentConfig.setTestResourcesDir(testRscRootDir);

        String testResultsFileName = "/TESTS/TEST-OUTPUTS/CHECKED/testresults.txt";
        URL resource = this.getClass().getResource(testResultsFileName);
        testResultsFile = new File(resource.getFile());
        testResultsParser = new TestResultsParser(testResultsFile);
    }

    @Test
    public void testParseTerstResults() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, TestCustomer> activeTestCustomersMap = testResultsParser.parseTestResultsFile();
        activeTestCustomersMap.entrySet().forEach(testCustomerEntry -> {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            testCustomer.dumpResults(stringBuilder, "\n");
        });
        File theFile = new File(testResultsFile.getParentFile(), "TestResultsX.txt");
        FileUtils.writeStringToFile(theFile, stringBuilder.toString(), false);
    }

}
