package de.creditreform.crefoteam.cte.tesun;

import com.google.common.base.Predicate;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;
import de.creditreform.crefoteam.cte.tesun.util.propertyfiles.PathInfo;

import java.util.List;
import java.util.Map;

public class CustomerPredidacte implements Predicate<PathInfo> {
   private final Map<String, TestCustomer> activeCustomersMap;
   private final boolean mustAllCrefosExist;

   public CustomerPredidacte(Map<String, TestCustomer> activeCustomersMap, boolean mustAllCrefosExist) {
      this.activeCustomersMap = activeCustomersMap;
      this.mustAllCrefosExist = mustAllCrefosExist;
   }

   @Override
   public boolean apply(PathInfo pathInfo) {
      if (activeCustomersMap.isEmpty()) {
         return false;
      }
      String customerKey = pathInfo.getCurtomerKey();
      final TestCustomer testCustomer = activeCustomersMap.get(customerKey.toUpperCase());
      if (testCustomer == null) {
         return false;
      }
      List<TestScenario> testScenariosList = testCustomer.getTestScenariosList();
      for (TestScenario testScenario : testScenariosList) {
         if (pathInfo.getRelativeSubDir().endsWith(testScenario.getScenarioName())) {
            return testScenario.isActivated();
         }
      }
      return false;
   }

   public boolean mustAllCrefosExist() {
      return mustAllCrefosExist;
   }

}
