package de.creditreform.crefoteam.cte.tesun.gui;

import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.RestInvokerResponse;
import de.creditreform.crefoteam.cte.rest.apache4.Apache4RestInvokerFactory;
import de.creditreform.crefoteam.cte.rest.interfaces.RestInvokerFactory;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.gui.utils.TestSupportHelper;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.jvmclient.JvmRestClientKonstanten;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TestSupportViewTest implements TesunClientJobListener {

    String testEnvName = "ENE";

/*
   @Test
   public void testInitForEnvironment() throws Exception {
      TestSupportView cut = new TestSupportGUI().getTestSupportView();
      cut.initForEnvironment(testEnvName, TestSupportClientKonstanten.RESOURCES_ROOT_LOCAL, "RepeatableTest"2);

      EnvironmentConfig environmentConfig =  cut.getEnvironmentConfig();
      Map<String, TestCustomer> testCustomerMap = environmentConfig.getCustomerTestInfoMap();
      Assert.assertEquals("Anzahl der Eingelesenen Test-Kunden stimmt nicht!", 3, testCustomerMap.size());

      System.out.println("TestBaseDir:\t\t\t" + environmentConfig.getTestResourcesRoot().getAbsolutePath());
      System.out.println("RepoFile:\t\t\t" + environmentConfig.getRepoFile().getAbsolutePath());
      System.out.println("TestCasesFile:\t\t\t" + environmentConfig.getTestCasesFile().getAbsolutePath());
      System.out.println("PseudoTestCasesFile:\t\t\t" + environmentConfig.getPseudoTestCasesFile().getAbsolutePath());
      System.out.println("TestOutputsFile:\t\t\t" + environmentConfig.getTestOutputsFile().getAbsolutePath());
      System.out.println("CollectsFile:\t\t\t" + environmentConfig.getCollectsFile().getAbsolutePath());
      System.out.println("RefExportsFile:\t\t\t" + environmentConfig.getRefExportsFile().getAbsolutePath());
      System.out.println("PseudoRefExportsFile:\t\t\t" + environmentConfig.getPseudoRefExportsFile().getAbsolutePath());
      System.out.println("RestoredCollectsFile:\t\t\t" + environmentConfig.getRestoredCollectsFile().getAbsolutePath());
      System.out.println("AB30XmlsFile:\t\t\t" + environmentConfig.getArchivBestandsFilePhase2().getAbsolutePath());
   }
*/

    @Test
    public void testSystemPropertyWithRestInvokerFactory() {
        System.setProperty("restInvoker.traceInvokers", "true");
        RestInvokerFactory restInvokerFactory = new Apache4RestInvokerFactory("", "", 10000);
        String URL_1 = "http://rhsctem015.ecofis.de:7052";
        String URL_2 = "http://rhsctem015.ecofis.de:7062";

        RestInvoker restInvoker1 = restInvokerFactory.getRestInvoker(URL_1);
        RestInvoker restInvoker2 = restInvokerFactory.getRestInvoker(URL_1);
        Assert.assertEquals("Instanzen müssten identisch sein!", restInvoker1, restInvoker2);
        RestInvoker restInvoker3 = restInvokerFactory.getRestInvoker(URL_2);
        Assert.assertFalse("Instanzen dürfen nicht identisch sein!", restInvoker1.equals(restInvoker3));
        Assert.assertFalse("Instanzen dürfen nicht identisch sein!", restInvoker2.equals(restInvoker3));
        restInvokerFactory.close();

        RestInvoker restInvoker4 = restInvokerFactory.getRestInvoker(URL_1);
        Assert.assertFalse("Instanzen dürfen nicht identisch sein!", restInvoker1.equals(restInvoker4));
        restInvokerFactory.close();

        RestInvoker restInvoker5 = restInvokerFactory.getRestInvoker(URL_2);
        String pathJobs = JvmRestClientKonstanten.PATH_JOBS;
        restInvoker5.temporaryPath(pathJobs);
        RestInvokerResponse restInvokerResponse = restInvoker5.invokeGetWithRetry(null, 10, 100).expectStatusOK();
        String strJvmInfoJobs = restInvokerResponse.getResponseBody();
        restInvokerFactory.close();
    }

    @Test
    public void testCheckRunningJobs() throws Exception {
        EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");
        TestSupportHelper testSupportHelper = new TestSupportHelper(environmentConfig,
                environmentConfig.getRestServiceConfigsForActiviti().get(0), // TODO
                environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), // TODO
                environmentConfig.getRestServiceConfigsForJvmImpCycle().get(0), // TODO
                this);
        try {
            Map<String, TestCustomer> customerTestInfoMap = environmentConfig.getCustomerTestInfoMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
            String strErr = testSupportHelper.checkRunningJobs(customerTestInfoMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testCheckJvms() throws Exception {
        EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");
        Map<String, TestCustomer> customerTestInfoMap = new HashMap<>();
        TestSupportHelper testSupportHelper = new TestSupportHelper(environmentConfig,
                environmentConfig.getRestServiceConfigsForActiviti().get(0), // TODO
                environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), // TODO
                environmentConfig.getRestServiceConfigsForJvmImpCycle().get(0), // TODO
                this);
        try {
         customerTestInfoMap.put("RTN (ENE)", new TestCustomer("RTN",  new File(""), new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
         customerTestInfoMap.put("BVD (ENE)", new TestCustomer("BVD", new File(""), new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
         customerTestInfoMap.put("VSD (ENE)", new TestCustomer("VSD", new File(""), new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
            String strErr = testSupportHelper.checkJvms(customerTestInfoMap);
            Assert.assertEquals("", strErr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void notifyClientJob(Level level, Object notifyObject) {
        System.out.println(notifyObject.toString());
    }

    @Override
    public Object askClientJob(ASK_FOR askFor, Object userObject) {
        return null;
    }
}
