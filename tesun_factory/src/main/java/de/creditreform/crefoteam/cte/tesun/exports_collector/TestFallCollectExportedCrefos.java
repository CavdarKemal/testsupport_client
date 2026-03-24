package de.creditreform.crefoteam.cte.tesun.exports_collector;

import com.google.inject.Module;
import com.google.inject.Provider;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.httpstest.TestClientUtils;
import de.creditreform.crefoteam.cte.tesun.util.*;
import org.apache.log4j.Level;

import java.nio.charset.Charset;
import java.util.Map;

public class TestFallCollectExportedCrefos extends AbstractTesunClientJob {

    public static final String COMMAND = "UserTask COLLECT";
    public static final String DESCRIPTION = "Sammeln der XML-Dateien fuer die uebergebenen Crefos aus den Export-Verzeichnissen";

    TestSupportClientKonstanten.TEST_PHASE testPhase;
    ExportsCollector exportsCollector;
    private EnvironmentConfig envConfig;
    private final Map<String, TestCustomer> activeCustomersMap;

    public TestFallCollectExportedCrefos(Map<String, TestCustomer> activeCustomersMap, TestSupportClientKonstanten.TEST_PHASE testPhase, TesunClientJobListener tesunClientJobListener) {
        super(COMMAND, DESCRIPTION, tesunClientJobListener);
        this.activeCustomersMap = activeCustomersMap;
        this.testPhase = testPhase;
    }

    @Override
    public void init(EnvironmentConfig envConfig) throws Exception {
        this.envConfig = envConfig;
        TesunUtilites.checkAndCreateDirectory(envConfig.getCollectsRoot(testPhase), true);
        TesunConfigInfo tesunConfigInfo = initTesunConfig(envConfig);
        ExportsAdapterFactory exportsAdapterFactory = new ExportsAdapterFactory(tesunConfigInfo, activeCustomersMap, tesunClientJobListener);
        exportsCollector = new ExportsCollector(envConfig, exportsAdapterFactory, tesunClientJobListener);
    }

    @Override
    public Module getGuiceModule(Charset charset, Provider<TestSupportMutableState> mutableStateProvider) {
        return new TesunClientExportedCrefosModule(charset, mutableStateProvider);
    }

    @Override
    public JOB_RESULT call() throws Exception {
        printHeader(Level.INFO, COMMAND, testPhase);
        TesunUtilites.dumpCustomers(envConfig.getLogOutputsRoot(), "VOR-" + COMMAND, activeCustomersMap);
        activeCustomersMap.entrySet().forEach(testCustomerEntry -> {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            if (testCustomer.isActivated()) {
                testCustomer.addTestResultsForCommand(COMMAND);
                testCustomer.refreshPseudoRefExports();
                exportsCollector.collectExportsForCustomer(testCustomer);
            }
        });
        TesunUtilites.dumpCustomers(envConfig.getLogOutputsRoot(), "NACH-" + COMMAND, activeCustomersMap);
        printFooter(Level.INFO, COMMAND, testPhase);
        return JOB_RESULT.OK;
    }

}
