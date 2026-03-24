package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.tesun.testutil.TesunTestSetupUtil;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CollectExportsIntegrationTestBase  {
    protected static Logger logger   = LoggerFactory.getLogger(CollectExportsIntegrationTestBase.class);

    protected static TesunTestSetupUtil setupUtil;
    protected static Map<String, TestCustomer> customerTestInfoMap;

    @BeforeClass
    public static void setUp() {
        try {
            SystemOutAppender.INFO().installIntoRootLogger();
            setupUtil = new TesunTestSetupUtil();
            setupUtil.setUp();
            setupUtil.configureLog4JProperties();
            customerTestInfoMap = setupUtil.readTestCustomersMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

}
