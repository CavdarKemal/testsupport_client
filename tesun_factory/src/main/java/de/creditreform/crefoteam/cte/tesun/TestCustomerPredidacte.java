package de.creditreform.crefoteam.cte.tesun;

import com.google.common.base.Predicate;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;

import java.util.List;

public class TestCustomerPredidacte implements Predicate<TestCustomer> {
    private final boolean mustAllCrefosExist;

    public TestCustomerPredidacte(boolean mustAllCrefosExist) {
        this.mustAllCrefosExist = mustAllCrefosExist;
    }

    @Override
    public boolean apply(TestCustomer testCustomer) {
        if (testCustomer == null) {
            return false;
        }
        List<TestScenario> testScenariosList = testCustomer.getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            return testScenario.isActivated();
        }
        return false;
    }

}
