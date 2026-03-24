package de.creditreform.crefoteam.cte.tesun.sftp_uploads;

import com.google.inject.Module;
import com.google.inject.Provider;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.exports_collector.TesunClientExportedCrefosModule;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestResults;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportConfigurationException;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;
import de.creditreform.crefoteam.cte.tesun.util.TesunConfigInfoUtils;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFallCollectSftpUploads extends AbstractTesunClientJob {
    private final static Logger logger = LoggerFactory.getLogger(TestFallCollectSftpUploads.class);
    private final static int SFTP_UPLOAD_COLLECT_TRIES = 2;
    public static final String COMMAND = "UserTask COLLECT-SFTP";
    public static final String DESCRIPTION = "Sammeln der SFTP-Uploads fuer die uebergebenen Kunden aus den SFTP-Verzeichnissen";

    private String sftpUploadsPath;
    private TesunConfigInfo tesunConfigInfo;
    private final Map<String, TestCustomer> selectedCustomersMap;
    private final TestSupportClientKonstanten.TEST_PHASE testPhase;

    public TestFallCollectSftpUploads(Map<String, TestCustomer> selectedCustomersMap, TestSupportClientKonstanten.TEST_PHASE testPhase, TesunClientJobListener tesunClientJobListener) {
        super(COMMAND, DESCRIPTION, tesunClientJobListener);
        this.selectedCustomersMap = selectedCustomersMap;
        this.testPhase = testPhase;
    }

    @Override
    public void init(EnvironmentConfig envConfig) throws Exception {
        // sftpUploadsPath = TesunUtilites.checkAndCreateDirectory(envConfig.getSftpUploadsFile(), true);
        sftpUploadsPath = TesunUtilites.checkAndCreateDirectoryX(envConfig.getSftpUploadsRoot(testPhase));
        tesunConfigInfo = initTesunConfig(envConfig);
    }

    @Override
    public Module getGuiceModule(Charset charset, Provider<TestSupportMutableState> mutableStateProvider) {
        return new TesunClientExportedCrefosModule(charset, mutableStateProvider);
    }

    @Override
    public JOB_RESULT call() throws Exception {
        printHeader(Level.INFO, COMMAND, testPhase);
        for (Map.Entry<String, TestCustomer> testCustomerEntry : selectedCustomersMap.entrySet()) {
            String key = testCustomerEntry.getKey();
            TestCustomer testCustomer = testCustomerEntry.getValue();
            testCustomer.addTestResultsForCommand(COMMAND);
            logger.info("TestFallCollectSftpUploads#call() für PathElement {}", key);
            TesunUtilites.checkAndCreateDirectory(testCustomer.getSftpUploadsDir(), true);
            SftpUploadsAdapter sftpUploadsAdapter = getSftpUploadsAdapterInstance(key);
            try {
                String strInfo;
                List<PathElement> pathElementList = listSftpUploadPathElementsWithRetry(sftpUploadsAdapter, testCustomer);
                File sftpUploadsDir = new File(sftpUploadsPath, key.toLowerCase());
                for (PathElement sftpUploadPathElement : pathElementList) {
                    strInfo = String.format("\n\t\tLese die SFTP-Upload-Datei '%s'...", sftpUploadPathElement.getSymbolicPath());
                    notifyTesunClientJobListener(Level.INFO, strInfo);
                    notifyTesunClientJobListener(Level.INFO, ".");
                    ByteArrayOutputStream byteArrayOutputStream = sftpUploadsAdapter.retrieveFileContent(sftpUploadPathElement);
                    File theFile = new File(sftpUploadsDir, sftpUploadPathElement.getName());
                    FileUtils.writeByteArrayToFile(theFile, byteArrayOutputStream.toByteArray());
                }
            } catch (Exception ex) {
                TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(ex.getMessage());
                testCustomer.addResultInfo(COMMAND, resultInfo);
                notifyTesunClientJobListener(Level.ERROR, "\n" + ex.getMessage());
            }
            TimelineLogger.end("SFTP-Collect", "SFTP-Collect für " + key + " beendet.");
        }
        printFooter(Level.INFO, COMMAND, testPhase);
        return JOB_RESULT.OK;
    }

    private List<PathElement> listSftpUploadPathElementsWithRetry(SftpUploadsAdapter sftpUploadsAdapter, TestCustomer testCustomer) throws Exception {
        List<PathElement> pathElementList = new ArrayList<>();
        int nTries = 0;
        for (; ; ) {
            String strInfo = String.format("\nErmittle SFTP-Upload des Kunden '%s'", testCustomer + "' (" + (nTries + 1) + ".Versuch)...");
            notifyTesunClientJobListener(Level.INFO, strInfo);
            pathElementList = sftpUploadsAdapter.listSftpUploadPathElements(testCustomer);
            if (!pathElementList.isEmpty()) {
                return pathElementList;
            }
            if (++nTries < SFTP_UPLOAD_COLLECT_TRIES) {
                Thread.sleep(1000);
            } else {
                return pathElementList;
            }
        }
    }

    protected SftpUploadsAdapter getSftpUploadsAdapterInstance(String customerKey) {
        SftpUploadAdapterConfig uploadAdapterConfig = new SftpUploadAdapterConfig(tesunConfigInfo, customerKey);
        return new SftpUploadsAdapter(uploadAdapterConfig, tesunClientJobListener);
    }

    public TesunConfigInfo initTesunConfig(EnvironmentConfig environmentConfig) {
        try {
            TesunConfigInfo tesunConfigInfo;
            boolean isLocal = environmentConfig.useLocalExports();
            if (isLocal) {
                File localExportsFile = new File(environmentConfig.getTestOutputsRoot(), "SFTP-UPLOADS");
                tesunConfigInfo = TesunConfigInfoUtils.buildTesunConfigInfoFromDir(localExportsFile.getAbsolutePath());
            } else {
                RestInvokerConfig restInvokerConfig = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0); // TODO !!!!!
                TesunRestService tesunRestService = new TesunRestService(restInvokerConfig, tesunClientJobListener);
                tesunConfigInfo = tesunRestService.getTesunConfigInfo();
            }
            return tesunConfigInfo;
        } catch (Exception ex) {
            notifyTesunClientJobListener(Level.ERROR, ex.getMessage());
            throw new TestSupportConfigurationException(ex.getMessage());
        }
    }
}
