package de.creditreform.crefoteam.cte.tesun.bic_import_ident;

import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpDirectoryEntry;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestServiceIntegrationTestBase;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.junit.Test;

import java.util.Map;

public class BicImportIdentTest extends TesunRestServiceIntegrationTestBase {

    TestSupportClientKonstanten.TEST_PHASE testPhase = TestSupportClientKonstanten.TEST_PHASE.PHASE_2;
    public BicImportIdentTest() {
        super("ENE");
    }

    @Test
    public void testPropertyUploadEmptyPayloadFALSE() throws Exception {
        logger.info("testPropertyUploadEmptyPayloadTRUE() mit uploadEmptyPayload = false");
        doTest(false);
    }

    @Test
    public void testPropertyUploadEmptyPayloadTRUE() throws Exception {
        logger.info("testPropertyUploadEmptyPayloadTRUE() mit uploadEmptyPayload = true");
        doTest(true);
    }

    private void doTest(boolean uploadEmptyPayload) throws Exception {
        // String exportPath = TestFallFileUtil.findRelativePathForCustomer(tesunConfigInfo, "BIC");
        String exportPath = environmentConfig.getDhlExportPrefix(); // "lokale_exporte/bic/export/delta"; //
        // vor dem Test-Export die schon vorhandenen Exporte ermiteln...
        ExportSchecker exportSchecker = new ExportSchecker(environmentConfig.getDhlExportSftpHost(), tesunClientJobListener);
        Map<String, SftpDirectoryEntry> todaysExportsEntries = exportSchecker.readTodaysExports(exportPath);

        TesunRestService tesunRestServiceBIC = new TesunRestService(environmentConfig.getRestServiceConfigForBIC().get(0), tesunClientJobListener);
        BicJobStarter bicJobStarter = new BicJobStarter(tesunRestServiceWLS, tesunRestServiceBIC, tesunClientJobListener);
        bicJobStarter.prepareBicFiles(environmentConfig, testPhase);
        bicJobStarter.doJobCycle(uploadEmptyPayload);

        UploadsChecker uploadsChecker = new UploadsChecker(environmentConfig.getDhlUploadSftpHost(), tesunClientJobListener);
        uploadsChecker.checkUploads(environmentConfig.getVvcToDhlSftpPath(), BicSftpUtil.UPLOAD_FILE_PREFIX, uploadEmptyPayload);

        // prüfe, welche Exporte dazugekommen sind...
        exportSchecker.checkNewExports(todaysExportsEntries, exportPath, 2, true);
    }

}
