package de.creditreform.crefoteam.cte.tesun.auto;

import de.creditreform.crefoteam.activiti.CteActivitiService;
import de.creditreform.crefoteam.activiti.CteActivitiServiceRestImpl;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.zip.ZipOutputStream;

public class ActivitiTestAutomatisierungTest {
    ScheduledExecutorService executorService;
    EnvironmentConfig environmentConfig;
    CteActivitiService cteActivitiService;

    @Before
    public void setUp() throws Exception {
        environmentConfig = new EnvironmentConfig("ENE");
        executorService = Executors.newScheduledThreadPool(1);
        RestInvokerConfig restServiceActiviti = environmentConfig.getRestServiceConfigsForActiviti().get(0);
        cteActivitiService = new CteActivitiServiceRestImpl(restServiceActiviti);
        URL resource = getClass().getResource("/activiti-tests");
        // testResourcesDir muss explizit gesetzt werden, da ActivitiTestAutomatisierung
        // den WLS-Server für initForEnvironment() benötigt, der im Test nicht verfügbar ist.
        File localDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
        localDir.mkdirs();
        environmentConfig.setTestResourcesDir(localDir);
    }

    @After
    public void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    public void testAddDirToArchive() throws Exception {
        ActivitiTestAutomatisierung cut = new ActivitiTestAutomatisierung(environmentConfig, "LOCAL", "");
        String zipFileName = environmentConfig.getTestResourcesRoot().getAbsolutePath() + "/test_zip.zip";
        FileOutputStream fileOutputStream = new FileOutputStream(zipFileName);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        File srcFile = environmentConfig.getTestResourcesDir();
        cut.notifyClientJob(Level.INFO, String.format("Das Verzeichnis %s wird gezippt...", srcFile.getAbsolutePath()));
        cut.addDirToArchive(zipOutputStream, srcFile.getParentFile(), srcFile);
        File logFile = new File(environmentConfig.getTestResourcesRoot().getParentFile().getParentFile().getAbsolutePath(), "testsupport_client.log");
        if (logFile.exists()) {
            cut.addFileToArchive(zipOutputStream, logFile.getParentFile(), logFile);
        }
        zipOutputStream.close();
    }

    @Test
    public void testZipOutputDirectory() throws Exception {
        ActivitiTestAutomatisierung cut = new ActivitiTestAutomatisierung(environmentConfig, "LOCAL", "");
        Assert.assertEquals(environmentConfig.getTestResourcesDir().getName(), "LOCAL");
        cut.zipOutputDirectory();
    }

    @Test
    public void testSendEmail() throws Exception {
        Assume.assumeTrue("SMTP-Server " + TestSupportClientKonstanten.EMAIL_HOST_NAME + ":" + TestSupportClientKonstanten.EMAIL_PORT + " nicht erreichbar — Test wird übersprungen", isSmtpReachable());
        ActivitiTestAutomatisierung cut = new ActivitiTestAutomatisierung(environmentConfig, "LOCAL", "");
        String emailContent = "Test-Email";
        String attachmentFileName = "src/test/java/de/creditreform/crefoteam/cte/tesun/auto/ActivitiTestAutomatisierungTest.java";
        String emailSubject = "CTE Test-Automatisierung: " + environmentConfig.getCurrentEnvName();
        TesunUtilites.sendEmail(environmentConfig.getActivitiEmailFrom(), environmentConfig.getActivitiFailureEmailTo(), emailSubject, emailContent, attachmentFileName);
    }

    private boolean isSmtpReachable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(TestSupportClientKonstanten.EMAIL_HOST_NAME, TestSupportClientKonstanten.EMAIL_PORT), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
