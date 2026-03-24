package de.creditreform.crefoteam.cte.tesun.downloader;

import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.httpstest.TestClientUtils;
import de.creditreform.crefoteam.cte.tesun.util.*;
import de.creditreform.crefoteam.cte.tesun.util.propertyfiles.PropertyFileLoader;
import org.apache.log4j.Level;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

public class TestFallDownloader extends AbstractTesunClientJob {

    public static final String COMMAND = "UserTask DOWNLOAD";
    public static final String DESCRIPTION = "Startet das Herunterladen der XML-Dateien aus CTE";

    private String testCasesPath;
    private String downloadedsPath;
    private PropertyFileLoaderFunctionDownload downloaderFunction;
    private final Map<String, TestCustomer> activeCustomersMap;
    private final TestSupportClientKonstanten.TEST_PHASE testPhase;
    private EnvironmentConfig environmentConfig;
    @Inject
    private PropertyFileLoader propertyFileLoader;

    public TestFallDownloader(Map<String, TestCustomer> activeCustomersMap, TesunClientJobListener tesunClientJobListener, TestSupportClientKonstanten.TEST_PHASE testPhase) {
        super(COMMAND, DESCRIPTION, tesunClientJobListener);
        this.activeCustomersMap = activeCustomersMap;
        this.testPhase = testPhase;
    }

    @Override
    public void init(EnvironmentConfig envConfig) throws Exception {
        this.environmentConfig = envConfig;
        downloaderFunction = new PropertyFileLoaderFunctionDownload(envConfig, activeCustomersMap, tesunClientJobListener);
        testCasesPath = TesunUtilites.checkAndCreateDirectory(envConfig.getItsqRefExportsRoot(), false);
        downloadedsPath = TesunUtilites.checkAndCreateDirectory(envConfig.getNewTestCasesRoot(), true);
    }

    @Override
    public JOB_RESULT call() throws Exception {
        printHeader(Level.INFO, COMMAND, testPhase);
        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "VOR-" + COMMAND, activeCustomersMap);
        // die Properties aus Test-Inputs nach DOWNLOADED-Verzeichnis kopieren...
        notifyTesunClientJobListener(Level.INFO, "\n\tBereite Tesfälle für aktive Kunden vor...");
        List<File> copiedFilesList = TesunUtilites.handleCustomersFiles(activeCustomersMap, downloadedsPath, new String[]{".properties"}, true);
        if (copiedFilesList.isEmpty()) {
            throw new PropertiesException(String.format("\nEs konnten keine Properties-Dateien nach % kopiert werden!", downloadedsPath));
        }
        notifyTesunClientJobListener(Level.INFO, String.format("\n\tIteriere ueber das Verzeichnis %s...", downloadedsPath));
        propertyFileLoader.iterateCrefos(logger, testCasesPath, downloaderFunction);
        printFooter(Level.INFO, COMMAND, testPhase);
        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "NACH-" + COMMAND, activeCustomersMap);
        return JOB_RESULT.OK;
    }

}
