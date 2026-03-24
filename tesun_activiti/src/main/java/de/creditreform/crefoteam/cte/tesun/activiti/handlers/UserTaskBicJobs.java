package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpDirectoryEntry;
import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.bic_import_ident.BicJobStarter;
import de.creditreform.crefoteam.cte.tesun.bic_import_ident.BicSftpUtil;
import de.creditreform.crefoteam.cte.tesun.bic_import_ident.ExportSchecker;
import de.creditreform.crefoteam.cte.tesun.bic_import_ident.UploadsChecker;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UserTaskBicJobs extends AbstractUserTaskRunnable {
    public static final String COMMAND = "BIC-TEST";
    public static final String DESCRIPTION = "Testet die BIC-Jobs";

    protected static Logger logger = LoggerFactory.getLogger(UserTaskBicJobs.class);
    protected TestSupportClientKonstanten.TEST_PHASE testPhase = TestSupportClientKonstanten.TEST_PHASE.PHASE_2;

    public UserTaskBicJobs(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super(environmentConfig, tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Object strPayload = taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_UPLOAD_EMPTY_PAYLOAD);
        if (strPayload == null) {
            throw new RuntimeException("\nWert für UPLOAD_EMPTY_PAYLOAD nicht angegeben!");
        }
        boolean uploadEmptyPayload = strPayload.toString().equals("true");
        TesunRestService tesunRestServiceWLS = new TesunRestService(environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), tesunClientJobListener);
        TesunRestService tesunRestServiceBIC = new TesunRestService(environmentConfig.getRestServiceConfigForBIC().get(0), tesunClientJobListener);
        AbstractTesunClientJob tesunClientJob = new TesunClientBicJob(testPhase, uploadEmptyPayload, tesunRestServiceWLS, tesunRestServiceBIC, tesunClientJobListener);
        tesunClientJob.init(environmentConfig);
        doStartTesunClientJob(tesunClientJob);
        return taskVariablesMap;
    }

    private static class TesunClientBicJob extends AbstractTesunClientJob {
        private final boolean uploadEmptyPayload;
        private final TestSupportClientKonstanten.TEST_PHASE testPhase;
        private EnvironmentConfig environmentConfig;
        private final TesunRestService tesunRestServiceWLS;
        private final TesunRestService tesunRestServiceBIC;

        protected TesunClientBicJob(TestSupportClientKonstanten.TEST_PHASE testPhase, boolean uploadEmptyPayload, TesunRestService tesunRestServiceWLS, TesunRestService tesunRestServiceBIC, TesunClientJobListener tesunClientJobListener) {
            super(COMMAND, DESCRIPTION, tesunClientJobListener);
            this.testPhase = testPhase;
            this.uploadEmptyPayload = uploadEmptyPayload;
            this.tesunRestServiceWLS = tesunRestServiceWLS;
            this.tesunRestServiceBIC = tesunRestServiceBIC;
        }

        @Override
        public void init(EnvironmentConfig envConfig) throws Exception {
            this.environmentConfig = envConfig;
        }

        @Override
        public JOB_RESULT call() {
            try {
//                TesunRestService tesunRestServiceWLS = new TesunRestService(environmentConfig.getRestServiceConfigWLS(), tesunClientJobListener);
//                TesunRestService tesunRestServiceBIC = new TesunRestService(environmentConfig.getRestServiceConfigBIC(), tesunClientJobListener);
                // vor dem Test-Export die schon vorhandenen Exporte ermiteln...
                String exportPath = environmentConfig.getDhlExportPrefix(); // "lokale_exporte/bic/export/delta"; //
                ExportSchecker exportSchecker = new ExportSchecker(environmentConfig.getDhlExportSftpHost(), tesunClientJobListener);
                Map<String, SftpDirectoryEntry> todaysExportsEntries = exportSchecker.readTodaysExports(exportPath);

                BicJobStarter bicJobStarter = new BicJobStarter(tesunRestServiceWLS, tesunRestServiceBIC, tesunClientJobListener);
                bicJobStarter.prepareBicFiles(environmentConfig, testPhase);

                bicJobStarter.doJobCycle("true".equals(uploadEmptyPayload));

                UploadsChecker uploadsChecker = new UploadsChecker(environmentConfig.getDhlUploadSftpHost(), tesunClientJobListener);
                uploadsChecker.checkUploads(environmentConfig.getVvcToDhlSftpPath(), BicSftpUtil.UPLOAD_FILE_PREFIX, "true".equals(uploadEmptyPayload));

                exportSchecker.checkNewExports(todaysExportsEntries, exportPath, 2, true);
            } catch (Throwable e) {
                throw new RuntimeException("\nBIC-Job-Test mit Fehler beendet!\n" + e.getMessage());
            }
            return JOB_RESULT.OK;
        }
    }
}
