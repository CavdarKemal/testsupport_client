package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;

public abstract class AbstractUserTaskCopyTest extends UserTaskTestBase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    protected void checkOutputDir(String inputPathExt, String outputPathExt, String extension) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = TestSupportClientKonstanten.TEST_PHASE.PHASE_1;
        Map<String, TestCustomer> testCustomerMap = getTestCustomerMap(testPhase, false);

        String inputsPath = environmentConfig.getTestResourcesDir().getAbsolutePath() + File.separator + inputPathExt;
        String outputsPath = environmentConfig.getTestOutputsRoot().getAbsolutePath() + File.separator + outputPathExt;
        for (String customerKey : testCustomerMap.keySet()) {
            TestCustomer testCustomer = testCustomerMap.get(customerKey);
            List<TestScenario> testScenariosList = testCustomer.getTestScenariosList();
            for (TestScenario testScenario : testScenariosList) {
                compareContents(testScenario, inputsPath.replaceAll("\\\\", "/"), outputsPath.replaceAll("\\\\", "/"), extension);
            }
        }
    }

    private void compareContents(TestScenario testScenario, final String inputsPath, final String outputsPath, final String extension) {
        final File inputsScenarioPath = testScenario.getItsqRefExportsPropsFile().getParentFile();
        File[] filesList = inputsScenarioPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(extension);
            }
        });
        for (File theFile : filesList) {
            String outputFilePath = theFile.getPath().replace(inputsPath, outputsPath);
            File outputFile = new File(outputFilePath);
            Assert.assertTrue(outputFile.exists());
        }
    }
}
