package de.creditreform.crefoteam.cte.tesun.bic_import_ident;

import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.apache4.Apache4RestInvokerFactory;
import de.creditreform.crefoteam.cte.rest.interfaces.RestInvokerFactory;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.jobexecution.TesunJobexecutionInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.jvmclient.JvmRestClient;
import de.creditreform.crefoteam.jvmclient.JvmRestClientImpl;
import de.creditreform.crefoteam.jvmclient.domain.JobStartResponse;
import de.creditreform.crefoteam.jvmclient.domain.JvmInstallation;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class BicJobStarter {
    private static final long millisFactor = 50000;
    private static final long millisSleep = 1000;
    private final AtomicBoolean abortFlag = new AtomicBoolean();
    private final TesunRestService tesunRestServiceWLS;
    private final JvmInstallation jvmInstallation;
    private final TesunClientJobListener tesunClientJobListener;
    protected static Logger logger = LoggerFactory.getLogger(BicJobStarter.class);;

    public BicJobStarter(TesunRestService tesunRestServiceWLS, TesunRestService tesunRestServiceBIC, TesunClientJobListener tesunClientJobListener) {
        this.tesunRestServiceWLS = tesunRestServiceWLS;
        this.jvmInstallation = new JvmInstallation();
        this.jvmInstallation.setJvmName("BIC (ENE)");
        String serviceURL = tesunRestServiceBIC.getRestInvokerConfig().getServiceURL();
        if(!serviceURL.startsWith("http")) {
            serviceURL = "http://" + serviceURL;
        }
        jvmInstallation.setJvmUrl(serviceURL);
        this.tesunClientJobListener = tesunClientJobListener;
    }

    public void prepareBicFiles(EnvironmentConfig environmentConfig, TestSupportClientKonstanten.TEST_PHASE testPhase) throws Exception {
        logger.info("Bereite BIC Import Files...");
        BicSftpUtil bicSftpUtil = new BicSftpUtil(environmentConfig.getDhlUploadSftpHost(), tesunClientJobListener);
        bicSftpUtil.deleteUploads(environmentConfig.getDhlToVvcSftpPath(), BicSftpUtil.TODAY_DATE);
        bicSftpUtil.deleteUploads(environmentConfig.getVvcToDhlSftpPath(), BicSftpUtil.UPLOAD_FILE_PREFIX);
        String sftpPath = environmentConfig.getDhlToVvcSftpPath() + "/" + TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM.format(new Date());
        // Env-unabhängig, immer ENE nehmen!
        String absolutePath = environmentConfig.getItsqRefExportsRoot(testPhase).getAbsolutePath();
        absolutePath = absolutePath.replace("ABE", "ENE").replace("GEE", "ENE");
        File bicTestFilesDir = new File(absolutePath, "bic/Default");
        if(!bicTestFilesDir.exists()) {
            throw new IllegalArgumentException("Das Verzeichnis '/LOCAL/REF-EXPORTS/PHASE-2/bic/Default' existiert nicht!");
        }
        bicSftpUtil.copyNewFilesToDhlToVvc(bicTestFilesDir, sftpPath);
    }

    public void doJobCycle(boolean uploadEmptyPayload) throws Exception {
        // erzeuge einen leeren Export...
        tesunClientJobListener.notifyClientJob(Level.INFO, "\nJob 'bic.bicExportDelta' gestartet, um einen leeren Export zu erzeugen.");
        logger.info("Starte BIC-Job 'bic.bicExportDelta' um einen leeren Export zu erzeugen...");
        doStartBicJob(jvmInstallation, "bic.bicExportDelta", "BIC_EXPORT", millisFactor * 1000);

        logger.info("Starte BIC-Job 'bic.bicImportDelta'...");
        doStartBicJob(jvmInstallation, "bic.bicImportDelta", "BIC_IMPORT", millisFactor * 1000);

        logger.info("Starte BIC-Job 'bic.bicIdentDelta'...");
        doStartBicJob(jvmInstallation, "bic.bicIdentDelta", "BIC_IDENT", millisFactor * 1000);

        logger.info("Starte BIC-Job 'bic.bicExportDelta'...");
        doStartBicJob(jvmInstallation, "bic.bicExportDelta", "BIC_EXPORT", millisFactor * 1000);
        // Setze das Property auf false setzen --> transferierte keine leeren Exporte
        if (!String.valueOf(uploadEmptyPayload).equals(setPropertyUploadEmptyPayload(String.valueOf(uploadEmptyPayload)))) {
            throw new RuntimeException("setPropertyUploadEmptyPayload() eliefert nicht " + uploadEmptyPayload + "!");
        }
        // dieser Job-Aufruf erzeugt einen NICHT-leeren Export
        logger.info("Starte BIC-Job 'bic.bicUploadDelta'...");
        doStartBicJob(jvmInstallation, "bic.bicUploadDelta", "BIC_UPLOAD", millisFactor * 1000);
        // Properties in DB restaurieren...
        tesunClientJobListener.notifyClientJob(Level.INFO, "\nProperties in DB restaurieren...");
        tesunRestServiceWLS.restoreEnvironmentProperties();
    }

    private void doStartBicJob(JvmInstallation jvmInstallation, String jobName, String processIdent, long jobCompletedTimeOut) throws InterruptedException {
        Date startJobDate = Calendar.getInstance().getTime();
        RestInvokerFactory restInvokerFactory = new Apache4RestInvokerFactory("", "", 10000);
        try {
            RestInvoker restInvoker = restInvokerFactory.getRestInvoker(jvmInstallation.getJvmUrl());
            JvmRestClient jvmRestClient = new JvmRestClientImpl(restInvoker, abortFlag);
            JobStartResponse jobStartResponse = jvmRestClient.startJob(jobName, new Properties());
            String processID = jobStartResponse.getJobId();
            tesunClientJobListener.notifyClientJob(Level.INFO, "\nJob '" + jobName + "' gestartet.");
            long startTimeMillis = System.currentTimeMillis();
            tesunClientJobListener.notifyClientJob(Level.INFO, "\nWarte auf Beendigung des Jobs '" + jobName + "' ...");
            while ((System.currentTimeMillis() - startTimeMillis) < jobCompletedTimeOut) {
                TesunJobexecutionInfo tesunJobExecutionInfo = tesunRestServiceWLS.getTesunJobExecutionInfo(processIdent);
                Calendar lastCompletitionCal = tesunJobExecutionInfo.getLastCompletitionDate();
                if (lastCompletitionCal != null) {
                    Date lastCompletitionDate = lastCompletitionCal.getTime();
                    if (lastCompletitionDate != null) {
                        String jobStatus = tesunJobExecutionInfo.getJobStatus();
                        if (lastCompletitionDate.after(startJobDate)) {
                            if ("COMPLETED".equals(jobStatus)) {
                                tesunClientJobListener.notifyClientJob(Level.INFO, "\nJob '" + jobName + "' beendet.");
                                return;
                            }
                        }
                    }
                }
                tesunClientJobListener.notifyClientJob(Level.INFO, ".");
                Thread.sleep(millisSleep);
            }
        } finally {
            restInvokerFactory.close();
        }
        throw new RuntimeException("Job '" + jobName + "' wurde nicht beendet!");
    }

    private void doStartBicDualJob(JvmRestClient jvmRestClient, String jobName, String processIdent1, String processIdent2) throws Exception {
        Date startJobDate = Calendar.getInstance().getTime();
        JobStartResponse jobStartResponse = jvmRestClient.startJob(jobName, new Properties());
        String processID = jobStartResponse.getJobId();
        int COMPLETED_TIME_OUT = 30 * 1000;
        long startTimeMillis = System.currentTimeMillis();
        String jobStatusBicImport = "";
        String jobStatusBicIdent = "";
        while ((System.currentTimeMillis() - startTimeMillis) < COMPLETED_TIME_OUT) {
            TesunJobexecutionInfo tesunJobExecutionInfoImport = tesunRestServiceWLS.getTesunJobExecutionInfo(processIdent1);
            Date lastCompletitionDateImport = tesunJobExecutionInfoImport.getLastCompletitionDate().getTime();
            jobStatusBicImport = tesunJobExecutionInfoImport.getJobStatus();
            TesunJobexecutionInfo tesunJobExecutionInfoIdent = tesunRestServiceWLS.getTesunJobExecutionInfo(processIdent2);
            Calendar lastCompletitionDate = tesunJobExecutionInfoIdent.getLastCompletitionDate();
            Date lastCompletitionDateIdent = lastCompletitionDate.getTime();
            if (lastCompletitionDateIdent != null) {
                jobStatusBicIdent = tesunJobExecutionInfoIdent.getJobStatus();
                if (lastCompletitionDateImport.after(startJobDate) && lastCompletitionDateIdent.after(startJobDate)) {
                    if ("COMPLETED".equals(jobStatusBicImport) && "COMPLETED".equals(jobStatusBicIdent)) {
                        return;
                    }
                    Thread.sleep(10);
                }
            }
        }
        throw new RuntimeException("Job '" + jobName + "' wurde nicht beendet!");
    }

    private String setPropertyUploadEmptyPayload(String newValue) {
        // Properties in DB restaurieren...
        tesunRestServiceWLS.restoreEnvironmentProperties();

        // Lese Properties mit Postfix
        String keyFilter = "bicexport.transfer.uploadEmptyPayload";
        String valueFilter = "";
        CteEnvironmentProperties cteEnvironmentProperties = tesunRestServiceWLS.getEnvironmentProperties(keyFilter, valueFilter, true);
        if (1 != cteEnvironmentProperties.getProperties().size()) {
            throw new RuntimeException("Anzahl erwarteter Properties sollte 1 sein!");
        }
        // modifiziere die Property
        CteEnvironmentPropertiesTupel cteEnvironmentPropertiesTupel = cteEnvironmentProperties.getProperties().get(0);
        cteEnvironmentPropertiesTupel.setValue(newValue);
        cteEnvironmentPropertiesTupel.setDbOverride(true);

        // ... und speichere die Props
        tesunRestServiceWLS.setEnvironmentProperties(cteEnvironmentProperties);
        cteEnvironmentProperties = tesunRestServiceWLS.getEnvironmentProperties(keyFilter, valueFilter, true);
        cteEnvironmentPropertiesTupel = cteEnvironmentProperties.getProperties().get(0);
        tesunClientJobListener.notifyClientJob(Level.INFO, "\nSetze das Property 'bicexport.uploadEmptyPayload' auf den Wert " + newValue + " mit DBOverride = true");
        return cteEnvironmentPropertiesTupel.getValue();
    }

}
