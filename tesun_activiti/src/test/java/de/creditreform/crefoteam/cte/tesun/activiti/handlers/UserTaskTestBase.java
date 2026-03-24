package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.SystemInfo;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by CavdarK on 19.07.2017.
 */
public abstract class UserTaskTestBase implements TesunClientJobListener {

    protected final static Logger LOGGER = LoggerFactory.getLogger(UserTaskTestBase.class);
    protected static final EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");

    private Map<String, TestCustomer> testCustomerMap = new HashMap<>();
    protected List<String> notifyList = new ArrayList<>();
    private SystemOutAppender systemOutAppender;
    protected TestSupportClientKonstanten.TEST_PHASE testPhase;

    @Before
    public void setUp() throws Exception {
        systemOutAppender = SystemOutAppender.INFO().installIntoRootLogger();
        notifyList.clear();
        File testRscRootDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
        environmentConfig.setTestResourcesDir(testRscRootDir);
    }

    @After
    public void tearDown() throws Exception {
        systemOutAppender.removeFromRootLogger();
    }

    @Override
    public void notifyClientJob(Level level, Object notifyObject) {
        String strInfo = notifyObject.toString();
        notifyList.add(strInfo);
        LOGGER.info(strInfo);
    }

    @Override
    public Object askClientJob(ASK_FOR askFor, Object userObject) {
        if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_REF_EXPORTS_PATH)) {
            String testInputsPath = environmentConfig.getTestResourcesDir().getAbsolutePath();
            return new File(testInputsPath, "NEU_SOLL").getPath();
        }
        if (TesunClientJobListener.ASK_FOR.ASK_OBJECT_RETRY.equals(askFor)) {
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    public Map<String, TestCustomer> getTestCustomerMap(TestSupportClientKonstanten.TEST_PHASE newTestPhase, boolean extendWithInfo) throws Exception {
        if(this.testPhase != newTestPhase) {
            this.testPhase = newTestPhase;
            testCustomerMap = new TreeMap<>();
        }
        if(testCustomerMap.isEmpty()) {
            testCustomerMap = environmentConfig.getCustomerTestInfoMap(testPhase);
            if(extendWithInfo) {
                TesunRestService tesunRestServiceWLS = new TesunRestService(this.environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), this);
                SystemInfo systemInfo = tesunRestServiceWLS.getSystemPropertiesInfo();
                testCustomerMap.entrySet().forEach((Map.Entry<String, TestCustomer> testCustomerEntry) -> {
                    try {
                        TestCustomer testCustomer = testCustomerEntry.getValue();
                        tesunRestServiceWLS.extendTestCustomerProperiesInfos(testCustomer, systemInfo);
                    } catch (Exception ex) {
                        Assert.fail(ex.getMessage());
                    }
                });
            }
        }
        return testCustomerMap;
    }

    public void setTestCustomerMap(Map<String, TestCustomer> testCustomerMap) {
        this.testCustomerMap = testCustomerMap;
    }

    protected void checkExpectedNotifies(List<String> notifyList, String[] expectedNotifies) {
        LOGGER.info("Actual Notify-List");
        for (String notify : notifyList) {
            LOGGER.info(notify);
        }
        for (String expectedNotify : expectedNotifies) {
            boolean found = false;
            for (String notify : notifyList) {
                if (notify.contains(expectedNotify)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Erwartete Nachricht '" + expectedNotify + "' nicht in der Liste!", found);
        }
    }

}
