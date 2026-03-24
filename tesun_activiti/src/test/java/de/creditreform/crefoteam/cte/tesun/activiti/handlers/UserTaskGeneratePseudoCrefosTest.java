package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class UserTaskGeneratePseudoCrefosTest extends UserTaskTestBase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testRunTask() throws Exception {
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = new TreeMap<>();
        activeTestCustomersMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, getTestCustomerMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, false));
        activeTestCustomersMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, getTestCustomerMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, false));

        UserTaskGeneratePseudoCrefos cut = new UserTaskGeneratePseudoCrefos(environmentConfig, this);
        Map<String, Object> taskParamsMap = new HashMap<>();
        taskParamsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS, activeTestCustomersMapMap);
        cut.runTask(taskParamsMap);

        final File pseudoRefExportsFile = environmentConfig.getPseudoRefExportsRoot(testPhase);
        Assert.assertTrue(new File(pseudoRefExportsFile, "rtn/Relevanz_Negativ/RTN_Negative_TF_Relevanz.properties").exists());
        Assert.assertTrue(new File(pseudoRefExportsFile, "rtn/Relevanz_Positiv/RTN_Positive_TF_Relevanz.properties").exists());

        final File pseudoPhase2ArchivBestandsFile = environmentConfig.getPseudoArchivBestandsRoot(testPhase);
        Assert.assertTrue(new File(pseudoPhase2ArchivBestandsFile, "bilanz.xml").exists());
        Assert.assertTrue(new File(pseudoPhase2ArchivBestandsFile, "befreiung.xml").exists());
        Assert.assertTrue(new File(pseudoPhase2ArchivBestandsFile, "bilanz_befreiung.xml").exists());
        final File testOutputsFile = environmentConfig.getTestOutputsRoot();
        Assert.assertTrue(new File(testOutputsFile, TestSupportClientKonstanten.fileNameCrefosMapping).exists());
    }

}
