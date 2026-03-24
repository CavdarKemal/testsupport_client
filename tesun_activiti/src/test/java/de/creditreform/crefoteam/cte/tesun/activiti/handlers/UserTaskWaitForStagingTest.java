package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;

import java.util.*;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserTaskWaitForStagingTest extends UserTaskTestBase {
   @Before
   public void setUp() throws Exception {
      super.setUp();
   }

   @Test
   public void testRunTask() throws Exception {
      Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = new TreeMap<>();
      TestSupportClientKonstanten.TEST_PHASE testPhase = TestSupportClientKonstanten.TEST_PHASE.PHASE_1;// TODO PHASE-1 ???!!!
      Map<String, TestCustomer> testCustomerMap = getTestCustomerMap(testPhase, true);
      activeTestCustomersMapMap.put(testPhase, testCustomerMap);// TODO PHASE-1 ???!!!
      // dieser Thread wird gestartet, wartet 5 Sekunden und setzt die Staging-Datümer der Crefos
      // http://localhost:7001/cte_tesun_service/tesun/staging/trigger?crefo=4112000448&crefo=4112000434
      Thread uThread = new Thread(new Runnable() {
         @Override
         public void run() {
            final List<String> strCrefosList = new ArrayList<>();
            for (String customerKey : testCustomerMap.keySet()) {
               TestCustomer testCustomer = testCustomerMap.get(customerKey);
               List<TestScenario> testScenariosList = testCustomer.getTestScenariosList();
               for (TestScenario testScenario : testScenariosList) {
                  List<TestCrefo> testCrefosList = testScenario.getTestCrefosAsList();
                  TestCrefo testCrefo = testCrefosList.get(0);
                  strCrefosList.add(testCrefo.getItsqTestCrefoNr() + "");
               }
            }
            try {
               final RestInvokerConfig restServiceConfigTesun = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0);
               final TesunRestService tesunRestService = new TesunRestService(restServiceConfigTesun, UserTaskWaitForStagingTest.this);
               Thread.sleep(2000);
               tesunRestService.updateDatumStaging(strCrefosList.subList(0, 1));
               Thread.sleep(1000);
               tesunRestService.updateDatumStaging(strCrefosList.subList(1, 3));
               Thread.sleep(1000);
               tesunRestService.updateDatumStaging(strCrefosList.subList(3, 4));
            } catch (Exception ex) {
               Assert.fail(ex.getMessage());
            }
         }
      });
      uThread.start();

      UserTaskWaitForStaging cut = new UserTaskWaitForStaging(environmentConfig, this);
      Map<String, Object> taskParamsMap = new HashMap<>();
      taskParamsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS, activeTestCustomersMapMap);
      cut.runTask(taskParamsMap);

      String[] expectedNotifies = {
         "@UserTaskWaitForStaging",
         "Warte bis Crefos der Kunden im Staging-Bereich angekommen sind...",
         "Ermittle ImportTrackingInfo für BVD.Relevanz_Negativ",
         "Ermittle ImportTrackingInfo für BVD.Relevanz_Positiv",
         "Ermittle ImportTrackingInfo für RTN.Relevanz_Negativ",
         "Ermittle ImportTrackingInfo für RTN.Relevanz_Positiv",
         "Ermittle ImportTrackingInfo für VSD.Relevanz_Negativ",
         "Ermittle ImportTrackingInfo für VSD.Relevanz_Positiv",
         "Staging-Eingangsdatum OK für RTN.Relevanz_Negativ",
         "Staging-Eingangsdatum OK für BVD.Relevanz_Positiv",
         "Staging-Eingangsdatum OK für BVD.Relevanz_Negativ",
         "Staging-Eingangsdatum OK für VSD.Relevanz_Negativ",
         "Staging-Eingangsdatum OK für RTN.Relevanz_Positiv",
         "Staging-Eingangsdatum OK für VSD.Relevanz_Positiv",
      };
      checkExpectedNotifies(notifyList, expectedNotifies);
   }

}
