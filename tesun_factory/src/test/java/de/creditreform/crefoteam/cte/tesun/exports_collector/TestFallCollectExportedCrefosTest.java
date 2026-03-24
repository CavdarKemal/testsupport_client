package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.tesun.TestFallTestBase;
import de.creditreform.crefoteam.cte.tesun.util.ClientJobStarter;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.junit.Test;

import java.util.Map;

public class TestFallCollectExportedCrefosTest extends TestFallTestBase {
    @Test
    public void testForCustomersList() throws Exception {
        Map<String, TestCustomer> selectedCustomersMapPhaseX = setupUtil.readTestCustomersMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        doTestForCustomers(selectedCustomersMapPhaseX);
    }

    private void doTestForCustomers(Map<String, TestCustomer> customerTestInfoMap) throws Exception {
        setupUtil.configureLog4JProperties();
        TestFallCollectExportedCrefos clientJob = new TestFallCollectExportedCrefos(customerTestInfoMap, TestSupportClientKonstanten.TEST_PHASE.PHASE_1, setupUtil.getTesunClientJobListener());
        ClientJobStarter jobStarter = new ClientJobStarter(clientJob);
        startJobTest(jobStarter);
    }

}
