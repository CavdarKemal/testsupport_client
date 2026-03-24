package de.creditreform.crefoteam.cte.tesun.restore;

import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.httpstest.TestClientUtils;
import de.creditreform.crefoteam.cte.tesun.util.*;
import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacementMapping;
import de.creditreform.crefoteam.cte.tesun.util.replacer.Replacer;
import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacerFactory;
import org.apache.log4j.Level;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class TestFallRestoreCrefos extends AbstractTesunClientJob {

    public static final String COMMAND = "UserTask RESTORE-COLLECTS";
    public static final String DESCRIPTION = "invoke generator to create new xml from reference templates";

    private EnvironmentConfig environmentConfig;
    private final Map<String, TestCustomer> activeCustomersMap;
    private final TestSupportClientKonstanten.TEST_PHASE testPhase;

    public TestFallRestoreCrefos(Map<String, TestCustomer> activeCustomersMap, TestSupportClientKonstanten.TEST_PHASE testPhase, TesunClientJobListener tesunClientJobListener) {
        super(COMMAND, DESCRIPTION, tesunClientJobListener);
        this.activeCustomersMap = activeCustomersMap;
        this.testPhase = testPhase;
    }

    @Override
    public void init(EnvironmentConfig envConfig) throws Exception {
        this.environmentConfig = envConfig;
        TesunUtilites.checkAndCreateDirectory(envConfig.getRestoredCollectsRoot(testPhase), true);
    }

    @Override
    public JOB_RESULT call() throws Exception {
        printHeader(Level.INFO, COMMAND, testPhase);
        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "VOR-" + COMMAND, activeCustomersMap);
        final File mappingsFile = new File(environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.fileNameCrefosMapping);
        Map<String, ReplacementMapping> replacementMappingMap0 = TestFallFileUtil.readReplacementMappingFromFile(mappingsFile);
        Map<String, ReplacementMapping> replacementMappingMap = TestFallFileUtil.swapReplacementMapping(replacementMappingMap0);

        // ReplacerFactory erzeugen und eignerVCsMapp schon mal setzen...
        ReplacerFactory replacerFactory = new ReplacerFactory( Replacer.CHARSET_UTF8);

        // Kopien im neuen Verzeichnis anlegen
        notifyTesunClientJobListener(Level.INFO, String.format("\nLege Kopien im neuen Verzeichnis %s an...", environmentConfig.getRestoredCollectsRoot().getAbsolutePath()));
        Replacer replacer = replacerFactory.create(replacementMappingMap, false);

        // COLLECTED der aktiven Kunden ins Origi8nal ändern
        notifyTesunClientJobListener(Level.INFO, String.format("\nIteriere das COLLECTED Verzeichnis für aktive Kunden..."));
        Iterator<Map.Entry<String, TestCustomer>> entryIterator = activeCustomersMap.entrySet().iterator();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, FutureTask> futureTasksMap = new HashMap<>();
        int nCnt = 0;
        while (entryIterator.hasNext()) {
            if (nCnt > 5) {
                Thread.sleep(1000);
                nCnt = 0;
            }
            TestCustomer testCustomer = entryIterator.next().getValue();
            if (testCustomer.isActivated()) {
                nCnt++;
                notifyTesunClientJobListener(Level.INFO, String.format("\n\tIteriere ueber COLLECTED-Verzeichnis für den Kunden '%s'...", testCustomer.getCustomerKey()));
                testCustomer.addTestResultsForCommand(COMMAND);
                testCustomer.refreshCollecteds();
                CustomerOriginGenerator customerOriginGenerator = new CustomerOriginGenerator(testCustomer, replacer, tesunClientJobListener);
                FutureTask<CustomerOriginGenerator> futureTask = new FutureTask(customerOriginGenerator);
                futureTasksMap.put(testCustomer.getCustomerKey(), futureTask);
                executor.execute(futureTask);
            }
        }
        executor.shutdown();
        TesunUtilites.waitForFutureTasks(futureTasksMap, tesunClientJobListener);
        printFooter(Level.INFO, COMMAND, testPhase);
        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "NACH-" + COMMAND, activeCustomersMap);
        return JOB_RESULT.OK;
    }
}
