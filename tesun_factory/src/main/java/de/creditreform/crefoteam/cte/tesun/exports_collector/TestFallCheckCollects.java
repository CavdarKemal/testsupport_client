package de.creditreform.crefoteam.cte.tesun.exports_collector;

import com.google.inject.Module;
import com.google.inject.Provider;
import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.log4j.Level;

public class TestFallCheckCollects extends AbstractTesunClientJob {

   public static final String COMMAND = "UserTask CHECK-COLLECTS";
   public static final String DESCRIPTION = "Prüfen der Exporte fuer die uebergebenen Crefos aus den Export-Verzeichnissen";

   private EnvironmentConfig envConfig;
   private CollectsChecker collectsChecker;
   private final Map<String, TestCustomer> activeCustomersMap;
   private final TestSupportClientKonstanten.TEST_PHASE testPhase;
   public TestFallCheckCollects(Map<String, TestCustomer> activeCustomersMap, TesunClientJobListener tesunClientJobListener, TestSupportClientKonstanten.TEST_PHASE testPhase) {
      super(COMMAND, DESCRIPTION, tesunClientJobListener);
      this.activeCustomersMap = activeCustomersMap;
       this.testPhase = testPhase;
   }

   @Override
   public void init(EnvironmentConfig envConfig) throws Exception {
      this.envConfig = envConfig;
      collectsChecker = new CollectsChecker(envConfig, tesunClientJobListener);
   }

   @Override
   public Module getGuiceModule(Charset charset, Provider<TestSupportMutableState> mutableStateProvider) {
      return new TesunClientExportedCrefosModule(charset, mutableStateProvider);
   }

   @Override
   public JOB_RESULT call() throws Exception {
      printHeader( Level.INFO, COMMAND, testPhase);
      TesunUtilites.dumpCustomers(envConfig.getLogOutputsRoot(), "VOR-" + COMMAND, activeCustomersMap);
      activeCustomersMap.entrySet().stream().forEach(entry -> {
         TestCustomer testCustomer = entry.getValue();
         if(testCustomer.isActivated()) {
            testCustomer.addTestResultsForCommand(COMMAND);
            testCustomer.refreshCollecteds();
            collectsChecker.checkTestCustomerCollects(testCustomer);
            notifyTesunClientJobListener(Level.INFO, ".");
         }
      });
      TesunUtilites.dumpCustomers(envConfig.getLogOutputsRoot(), "NACH-" + COMMAND, activeCustomersMap);
      printFooter( Level.INFO, COMMAND, testPhase);
      return JOB_RESULT.OK;
   }

}
