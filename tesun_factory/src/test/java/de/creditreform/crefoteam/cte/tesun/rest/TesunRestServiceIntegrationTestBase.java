package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleSystemOut;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TesunRestServiceIntegrationTestBase {
    protected static final String STR_GRUNDBESTAND = "Grundbestand";
    protected static Logger logger;

    protected EnvironmentConfig environmentConfig;
    protected TesunRestService tesunRestServiceWLS;
    protected TesunRestService tesunRestServiceJvmInso;
    protected TesunRestService tesunRestServiceJvmInsoBackend;
    protected TesunRestService tesunRestServiceJvmImpCycle;
    protected Map<String, TestCustomer> testCustomerMap;

    protected static TesunClientJobListener tesunClientJobListener = new TesunClientJobListener() {
        @Override
        public void notifyClientJob(Level level, Object notifyObject) {
            System.out.print(notifyObject.toString());
            System.out.flush();
        }

        @Override
        public Object askClientJob(TesunClientJobListener.ASK_FOR askFor, Object userObject) {
            if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CTE_VERSION)) {
                return Integer.valueOf("1917");
            }
            else if (askFor.equals(ASK_FOR.ASK_OBJECT_TEST_TYPE)) {
                return TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2;
            }
            return null;
        }
    };

    @Rule
    public AppenderRuleSystemOut ruleSystemOut = new AppenderRuleSystemOut();

    @BeforeClass
    public static void setUp() throws Exception {
        SystemOutAppender.INFO().installIntoRootLogger();
        logger = LoggerFactory.getLogger(TesunRestServiceIntegrationTestBase.class);
    }

    @After
    public void tearDown() {
        SystemOutAppender.INFO().removeFromRootLogger();
    }

    public TesunRestServiceIntegrationTestBase(String umgebung) {
        logger.info("Test für die Umgebung " + umgebung);
        logger.info("=====================================");
        init(umgebung);
    }

    private void init(String umgebung) {
        try {
            // Kundenliste IMMER zuerst aus der ENE-Konfig
            this.testCustomerMap = initCustomersFromEnv("ENE");
            if (umgebung == null) {
                umgebung = "ENE";
            }
            initForEnvironment(umgebung);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    protected void initForEnvironment(String umgebung) throws Exception {
        environmentConfig = new EnvironmentConfig(umgebung);
        File testRscRootDir = new File(environmentConfig.getTestResourcesRoot(), "TESTS/LOCAL");
        environmentConfig.setTestResourcesDir(testRscRootDir);

        tesunRestServiceWLS = new TesunRestService(environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), tesunClientJobListener);
        tesunRestServiceJvmImpCycle = new TesunRestService(environmentConfig.getRestServiceConfigsForJvmImpCycle().get(0), tesunClientJobListener);
        tesunRestServiceJvmInso = new TesunRestService(environmentConfig.getRestServiceConfigsForJvmInso().get(0), tesunClientJobListener);
        tesunRestServiceJvmInsoBackend = new TesunRestService(environmentConfig.getRestServiceConfigsForJvmInsoBackend().get(0), tesunClientJobListener);
        if (!umgebung.equals("PRE")) {
            SystemInfo systemInfo = tesunRestServiceWLS.getSystemPropertiesInfo();
            testCustomerMap.entrySet().forEach(testCustomerEntry -> {
                try {
                    TestCustomer testCustomer = testCustomerEntry.getValue();
                    tesunRestServiceWLS.extendTestCustomerProperiesInfos(testCustomer, systemInfo);
                } catch (Exception ex) {
                    Assert.fail(ex.getMessage());
                }
            });
        }
    }

    private Map<String, TestCustomer> initCustomersFromEnv(String umgebung) throws Exception {
        environmentConfig = new EnvironmentConfig(umgebung);
        File testRscRootDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
        environmentConfig.setTestResourcesDir(testRscRootDir);
        Map<String, TestCustomer> customerTestInfoMap = environmentConfig.getCustomerTestInfoMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2); // !!!!!!!!!!! TODO
        return customerTestInfoMap;
    }

    protected void dumpProperties(CteEnvironmentProperties cteEnvironmentProperties, String keyFilter, String valueFilter, File propsFile) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        logger.info(String.format("\nProperties mit Key-Filter '%s' und Value-Filter '%s'\n", keyFilter, valueFilter));
        for (CteEnvironmentPropertiesTupel cteEnvironmentPropertiesTupel : cteEnvironmentProperties.getProperties()) {
            Assert.assertTrue(cteEnvironmentPropertiesTupel.getKey().toLowerCase().contains(keyFilter.toLowerCase()));
            Assert.assertTrue(cteEnvironmentPropertiesTupel.getValue().toLowerCase().contains(valueFilter.toLowerCase()));
            String strTemp = cteEnvironmentPropertiesTupel.isDbOverride() ? "[DB]" : "[FLUX]";
            stringBuilder.append(String.format("\n%s%s=%s", strTemp, cteEnvironmentPropertiesTupel.getKey(), cteEnvironmentPropertiesTupel.getValue()));
        }
        logger.info(stringBuilder.toString());
        if (propsFile != null) {
            FileUtils.writeStringToFile(propsFile, stringBuilder.toString());
        }
    }
}
