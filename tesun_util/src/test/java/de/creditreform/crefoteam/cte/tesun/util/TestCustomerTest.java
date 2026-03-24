package de.creditreform.crefoteam.cte.tesun.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class TestCustomerTest
{
  protected static final EnvironmentConfig environmentConfig   = new EnvironmentConfig("ENE");

  @Before
  public void setUp() throws Exception {
    File testRscRootDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
    environmentConfig.setTestResourcesDir(testRscRootDir);
  }

  @Test
  public void testAddTestScenario() throws Exception
  {
    String customerName = "vsd";
    TestCustomer cut = new TestCustomer( customerName, environmentConfig.getTestResourcesDir(), environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
    TestScenario testScenario = new TestScenario( cut, "Relevanz_Positiv", null);
    
    cut.addTestScenario( testScenario );
    List<TestScenario> testScenariosList = cut.getTestScenariosList();
    Assert.assertNotNull( testScenariosList );
    Assert.assertEquals(1, testScenariosList.size());
    
    customerName = "rtn";
    cut = new TestCustomer( customerName, environmentConfig.getTestResourcesDir(), environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
    TestScenario testScenario1 = new TestScenario( cut, "Relevanz_Negativ", null);
    cut.addTestScenario( testScenario1 );
    testScenariosList = cut.getTestScenariosList();
    Assert.assertNotNull( testScenariosList );
    Assert.assertEquals(1, testScenariosList.size() );

    TestScenario testScenario2 = new TestScenario( cut, "Relevanz_Positiv", null);
    cut.addTestScenario( testScenario2 );
    testScenariosList = cut.getTestScenariosList();
    Assert.assertEquals(2, testScenariosList.size());
  }
}
