package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.testutil.TesunTestSetupUtil;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class ExportsAdapterTestBase {

    protected static final Logger logger = LoggerFactory.getLogger(ExportsAdapterTestBase.class);
    protected static TesunTestSetupUtil setupUtil;
    protected static Map<String, TestCustomer> customerTestInfoMap;

    @BeforeClass
    public static void setUp() {
        try {
            SystemOutAppender.INFO().installIntoRootLogger();
            setupUtil = new TesunTestSetupUtil();
            setupUtil.setUp();
            setupUtil.configureLog4JProperties();
            customerTestInfoMap = setupUtil.readTestCustomersMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2); //!!! TODO
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    protected static TesunClientJobListener tesunClientJobListener = new TesunClientJobListener() {
        @Override
        public void notifyClientJob(Level level, Object notifyObject) {
            logger.info(notifyObject.toString());
        }

        @Override
        public Object askClientJob(ASK_FOR askFor, Object userObject) {
            return null;
        }
    };

    protected TestScenario fillTestCrefosForTesScenario(List<TestCrefo> testCrefoList, TestCustomer testCustomer) {
        TestScenario testScenario = testCustomer.getTestScenariosMap().get("Relevanz_Positiv");
        testScenario.getTestFallNameToTestCrefoMap().clear();
        testCrefoList.forEach(testCrefo -> {
            if (testCrefo != null) {
                testScenario.getTestFallNameToTestCrefoMap().put(testCrefo.getTestFallName(), testCrefo);
            }
        });
        return testScenario;
    }

    protected ExportsAdapterDefImpl getExportsAdapter(String customerKey) throws Exception {
        ExportsAdapterConfig exportsAdapterConfig = new ExportsAdapterConfig(setupUtil.getTesunConfigInfo(true), customerKey);
        ExportsAdapterDefImpl cut = new ExportsAdapterDefImpl(exportsAdapterConfig, customerTestInfoMap.get(customerKey), tesunClientJobListener);
        Assert.assertNotNull(cut);
        return cut;
    }

}
