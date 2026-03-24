package de.creditreform.crefoteam.cte.tesun.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class TestScenarioTest {
    protected static final EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");

    @Before
    public void setUp() throws Exception {
        File testRscRootDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
        environmentConfig.setTestResourcesDir(testRscRootDir);
    }

    @Test
    public void testGetTestCrefosList() throws Exception {
        String customerName = "bvd";
        TestCustomer testCustomer = new TestCustomer(customerName, environmentConfig.getTestResourcesDir(), environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        TestScenario cut = new TestScenario(testCustomer, "Relevanz_Negativ", null);
        List<TestCrefo> testCrefosList = cut.getTestCrefosAsList();
        Assert.assertNotNull(testCrefosList);
        Assert.assertEquals(1, testCrefosList.size());
        TestCrefo testCrefo0 = testCrefosList.get(0);
        Assert.assertNotNull(testCrefo0);
        Assert.assertEquals(4112002442L, testCrefo0.getItsqTestCrefoNr().longValue());
        Assert.assertNotNull(testCrefo0.getTestFallInfo());
        Assert.assertTrue(testCrefo0.isActivated());
        Assert.assertFalse(testCrefo0.isShouldBeExported());
        Assert.assertFalse(testCrefo0.isExported());

    }

}
