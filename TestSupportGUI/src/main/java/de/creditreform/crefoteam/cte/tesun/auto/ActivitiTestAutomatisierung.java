package de.creditreform.crefoteam.cte.tesun.auto;

import de.creditreform.crefoteam.activiti.CteActivitiTask;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.systeminfo.TesunSystemInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.gui.utils.CommandExecutorListener;
import de.creditreform.crefoteam.cte.tesun.gui.view.ActivitiProcessController;
import de.creditreform.crefoteam.cte.tesun.gui.utils.TestCaseFilesFromGit;
import de.creditreform.crefoteam.cte.tesun.gui.utils.TestSupportHelper;
import de.creditreform.crefoteam.cte.tesun.rest.SystemInfo;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestFallFileUtil;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ActivitiTestAutomatisierung implements TesunClientJobListener, CommandExecutorListener {
    private static final Logger LOGGER = Logger.getLogger(ActivitiTestAutomatisierung.class);
    private final EnvironmentConfig environmentConfig;
    private final String currTestSrc;
    private final String currRepoBranch;

    private Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> testCustomerMapMap;

    private ActivitiProcessController activitiController;
    private TestSupportHelper testSupportHelper;
    private TesunRestService tesunRestServiceWLS;
    private FileAppender specificFileAppender;

    private TestCaseFilesFromGit testCaseFilesFromRepo;
    private boolean activitProcessEnded = false;
    private int activitProcessExitCode = 0;

    public ActivitiTestAutomatisierung(EnvironmentConfig environmentConfig, String currTestSrc, String currRepoBranch) {
        this.environmentConfig = environmentConfig;
        this.currTestSrc = currTestSrc;
        this.currRepoBranch = currRepoBranch;
        initForEnvironment();
    }

    private void initForEnvironment() {
        try {
            notifyClientJob(Level.INFO, String.format("\nInitialisiere Test-Resourcen für die Umgebung %s...", environmentConfig.getCurrentEnvName()));
            String appLogFileName = String.format("logs/%s.log", TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2.name());
            String actionLogFileName = String.format("logs/%s.log", "TimeLine.log");
            if (!TimelineLogger.configure(environmentConfig.getLogOutputsRoot(), appLogFileName, actionLogFileName)) {
                notifyClientJob(Level.ERROR, "Exception beim Konfigurieren der LOG-Dateien!\n");
            }
            notifyClientJob(Level.INFO, String.format("\nInitialisiere für die Umgebung %s...", environmentConfig.getCurrentEnvName()));
            environmentConfig.loadEnvironmentConfig(environmentConfig.getCurrentEnvName());
            testSupportHelper = new TestSupportHelper(environmentConfig,
                    environmentConfig.getRestServiceConfigsForActiviti().get(0), // TODO
                    environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), // TODO
                    environmentConfig.getRestServiceConfigsForJvmImpCycle().get(0), // TODO
                    ActivitiTestAutomatisierung.this);
            tesunRestServiceWLS = testSupportHelper.getTesunRestServiceWLS();
            TesunSystemInfo tesunSystemInfo = tesunRestServiceWLS.getTesunSystemInfo();
            environmentConfig.setCteVersion(tesunSystemInfo.getCteVersion());
            File sourceDir;
            if (currTestSrc.equals("LOCAL")) {
                sourceDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
            } else if (currTestSrc.equals("LOCAL-S")) {
                sourceDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL-S");
            } else {
                testCaseFilesFromRepo = new TestCaseFilesFromGit(this);
                File[] files = environmentConfig.getTestResourcesRoot().listFiles();
                if (files != null && files.length > 0) {
                    FileUtils.forceDelete(environmentConfig.getTestResourcesRoot());
                }
                sourceDir = testCaseFilesFromRepo.updateItsqTestPaket(environmentConfig.getTestResourcesRoot(), environmentConfig.getGitReposList(currRepoBranch));
            }
            environmentConfig.setTestResourcesDir(sourceDir);
            testCustomerMapMap = initTestCasesForCustomers();
        } catch (Throwable ex) {
            notifyClientJob(Level.ERROR, "Exception beim Laden der Konfiguration!\n" + ex.getMessage());
        }
    }

    public Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> initTestCasesForCustomers() throws Exception {
        notifyClientJob(Level.INFO, "\n\tLese die Test-Crefos-Konfiguration aus dem ITSQ-Verzeichnis...");
        SystemInfo systemInfo = tesunRestServiceWLS.getSystemPropertiesInfo();
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customerTestInfoMapMap = environmentConfig.getCustomerTestInfoMapMap();
        Iterator<TestSupportClientKonstanten.TEST_PHASE> iterator = customerTestInfoMapMap.keySet().iterator();
        while (iterator.hasNext()) {
            TestSupportClientKonstanten.TEST_PHASE testPhase = iterator.next();
            Map<String, TestCustomer> testCustomerMap = customerTestInfoMapMap.get(testPhase);
            testCustomerMap.entrySet().forEach(testCustomerEntry -> {
                try {
                    TestCustomer testCustomer = testCustomerEntry.getValue();
                    notifyClientJob(Level.INFO, "\n\t\tInitialisiere Testfälle des Kunden für " + testCustomer.getCustomerName() + " aus " + testPhase);
                    tesunRestServiceWLS.extendTestCustomerProperiesInfos(testCustomer, systemInfo);
                } catch (Exception ex) {
                    String notifyObject = "\n" + testCustomerMap.size() + "Exception beim Vervollständigen der Kundenkonfiguration!\n" + ex.getMessage();
                    notifyClientJob(Level.ERROR, notifyObject);
                }
            });
            notifyClientJob(Level.INFO, "\n" + testCustomerMap.size() + " Kunden sind für den Test in der " + testPhase + " ausgewählt\n");
        }
        return customerTestInfoMapMap;
    }

    protected void appendInfo(String strInfo) {
        if (!strInfo.startsWith(".")) {
            LOGGER.info(strInfo.replaceAll("\t", " "));
        }
    }

    public void addFileToArchive(ZipOutputStream zipOutputStream, File parentFile, File srcFile) throws IOException {
        // notifyClientJob(Level.INFO, String.format("\tFüge die Datei %s ins ZIP hinzu..." , files[i].getAbsolutePath()));
        byte[] buffer = new byte[1024];
        FileInputStream fileInputStream = new FileInputStream(srcFile);
        String absolutePath = srcFile.getAbsolutePath();
        String entryName = absolutePath.substring(parentFile.getAbsolutePath().length() + 1);
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        int length;
        while ((length = fileInputStream.read(buffer)) > 0) {
            zipOutputStream.write(buffer, 0, length);
        }
        zipOutputStream.closeEntry();
        fileInputStream.close();
    }

    protected void addDirToArchive(ZipOutputStream zipOutputStream, File parentFile, File srcFile) throws IOException {
        File[] files = srcFile.listFiles();
        if (files == null || files.length < 1) {
            notifyClientJob(Level.WARN, String.format("\t! Das Verzeichnis %s ist leer!", srcFile.getAbsolutePath()));
            return;
        }
        //  notifyClientJob(Level.INFO, String.format("\tFüge das Verzeichnis %s ins ZIP hinzu..." , srcFile.getAbsolutePath()));
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDirToArchive(zipOutputStream, parentFile, files[i]);
                continue;
            }
            addFileToArchive(zipOutputStream, parentFile, files[i]);
        }
    }

    protected String zipOutputDirectory() {
        String zipFileName = null;
        ZipOutputStream zipOutputStream = null;
        try {
            File testOutputsFile = environmentConfig.getTestOutputsRoot();
            String strDateTime = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.format(Calendar.getInstance().getTime()).replaceAll(":", "_");
            zipFileName = String.format("%s%sTEST-OUTPUTS-%s.zip", testOutputsFile.getParent(), File.separator, strDateTime);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFileName);
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            notifyClientJob(Level.INFO, String.format("Das Verzeichnis %s wird gezippt...", testOutputsFile.getAbsolutePath()));
            addDirToArchive(zipOutputStream, testOutputsFile.getParentFile(), testOutputsFile);

            File testInputsFile = environmentConfig.getItsqRefExportsRoot();
            addDirToArchive(zipOutputStream, testInputsFile.getParentFile(), testInputsFile);
            zipOutputStream.close();
        } catch (Exception ex) {
            final String exMsg = ex.getMessage();
            appendInfo(exMsg != null ? exMsg : ex.getClass().getSimpleName());
        } finally {
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return zipFileName;
    }

    private Map<String, Object> setTaskVariablesMap(Boolean isDemoMode) throws PropertiesException {
        Map<String, Object> taskVariablesMap = new HashMap<>();
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE, isDemoMode);
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY, environmentConfig.getActivitProcessKey());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME, environmentConfig.getActivitiProcessName());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_BTLG_IMPORT, environmentConfig.getMillisBeforeBtlgImport(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_CT_IMPORT, environmentConfig.getMillisBeforeCtImport(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_EXPORT, environmentConfig.getMillisBeforeExports(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_EXPORTS_COLLECT, environmentConfig.getMillisBeforeCollectExports(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TIME_BEFORE_SFTP_COLLECT, environmentConfig.getMillisBeforeCollectSftpUploads(isDemoMode));
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_EMAIL_FROM, environmentConfig.getActivitiEmailFrom());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_SUCCESS_EMAIL_TO, environmentConfig.getActivitiSuccessEmailTo());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_FAILURE_EMAIL_TO, environmentConfig.getActivitiFailureEmailTo());
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS, false);
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_TYPE, TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2);
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE, TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_USE_ONLY_TEST_CLZ, true);
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_UPLOAD_SYNTH_TEST_CREFOS, false);

        return taskVariablesMap;
    }

    public void startACTIVITPorocess() throws Exception {
        Map<String, Object> taskVariablesMap = setTaskVariablesMap(false);
        testCaseFilesFromRepo.tagAndPushITSQProject(
                environmentConfig.getTestResourcesDir().getAbsolutePath(),
                environmentConfig.getItsqTagName(TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2.name()));
        activitiController = new ActivitiProcessController(ActivitiTestAutomatisierung.this, () -> {});
        activitiController.runProcess(testSupportHelper, environmentConfig, taskVariablesMap, testCustomerMapMap, null);
        while (!activitProcessEnded) {
            Thread.sleep(100000);
        }
        System.exit(activitProcessExitCode);
    }

    private void endACTIVITIProcess() {
        TimelineLogger.info(ActivitiTestAutomatisierung.class, "===========    Activiti-Process beendet.    ===========");
        appendInfo("===========    Activiti-Process beendet.    ===========");
        try {
            activitiController.stop();
        } catch (Exception ex) {
            appendInfo("Fehler beim Beenden des ActivitiProcessController!\n" + ex.getMessage());
        }
        StringBuilder stringBuilderAll = new StringBuilder();
        testCustomerMapMap.entrySet().forEach(testPhaseMapEntry -> {
            stringBuilderAll.append(TestFallFileUtil.dumAllCustomers(testPhaseMapEntry.getValue()));
        });
        try {
            FileUtils.copyFile(new File(specificFileAppender.getFile()), new File(environmentConfig.getTestOutputsRoot(), specificFileAppender.getFile()));
            FileUtils.writeStringToFile(new File(environmentConfig.getTestOutputsRoot(), "TestResults.txt"), stringBuilderAll.toString());
            appendInfo("\nTest-Results sind im Output-Ordner gespeichert");
        } catch (Exception ex) {
            appendInfo("Fehler beim Speicher der TestResults-Datei!\n" + ex.getMessage());
        }
        String attachmentFileName = zipOutputDirectory();
        appendInfo(String.format("Sende Email an '%s' mit dem Inhalt:\n%s", stringBuilderAll, attachmentFileName));
        try {
            String emailSubject = "CTE Test-Automatisierung: " + environmentConfig.getCurrentEnvName() + "." + TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2.name();
            TesunUtilites.sendEmail(environmentConfig.getActivitiEmailFrom(), environmentConfig.getActivitiFailureEmailTo(), emailSubject, stringBuilderAll.toString(), attachmentFileName);
            activitProcessExitCode = (stringBuilderAll.length() > 0) ? -1 : 0;
        } catch (Exception ex) {
            appendInfo("Fehler beim Senden der Email!\n" + ex.getMessage());
        }
        TimelineLogger.close();
        activitProcessEnded = true;
    }

    /***********************************************************************************************************/
    /****************************************  UserTaskThreadListener  *****************************************/
    /***********************************************************************************************************/
    @Override
    public void notifyClientJob(Level level, Object notifyObject) {
        if (notifyObject == null) {
            // Nachricht über das Ende des Prozesses
            endACTIVITIProcess();
        } else if (notifyObject instanceof CteActivitiTask) {
            // Nachricht über die gerade aktive Activiti-Task
            CteActivitiTask userTask = (CteActivitiTask) notifyObject;
            StringBuilder taskInfo = new StringBuilder();
            taskInfo.append(String.format("\n\nUser-Task: %s", userTask.getName()));
            taskInfo.append(String.format("\n  ID: %d", userTask.getId()));
            taskInfo.append(String.format("\n  ProcessInstance-ID: %d", userTask.getProcessInstanceId()));
            taskInfo.append(String.format("\n  ProcessDefinition-ID: %s", userTask.getProcessDefinitionId()));
            appendInfo(taskInfo.toString());
            TimelineLogger.info(ActivitiTestAutomatisierung.class, taskInfo.toString());
        } else if (notifyObject instanceof SequenceInputStream) {
            // Nachricht mit dem Prozess-Bild, für diese Klasse / diesen Listener nicht interessant
        } else {
            // unbekannte Nachricht
            appendInfo(notifyObject.toString());
        }
    }

    @Override
    public Object askClientJob(ASK_FOR askFor, Object userObject) {
        try {
            if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_RETRY)) {
                appendInfo(userObject.toString() + "\n\tVersuche NICHT erneut, ABBRUCH!");
                return Boolean.FALSE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CTE_VERSION)) {
                return Integer.valueOf(environmentConfig.getCteVersion());
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_REF_EXPORTS_PATH)) {
                File tmpFile = environmentConfig.getItsqRefExportsRoot();
                return tmpFile.getAbsolutePath();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_TEST_CASES_PATH)) {
                File tmpFile = environmentConfig.getItsqRefExportsRoot();
                return tmpFile.getAbsolutePath();
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_WAIT_FOR_TEST_SYSTEM)) {
                return Boolean.TRUE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_COPY_EXPORTS_TO_INPUTS)) {
                return Boolean.TRUE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CREATE_NEW_SOLL)) {
                return Boolean.TRUE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_ANAYLSE_CHECKS)) {
                return Boolean.TRUE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CHECK_DOWNLOADS)) {
                return Boolean.TRUE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_CHECK_COLLECTS)) {
                return Boolean.TRUE;
            } else if (askFor.equals(TesunClientJobListener.ASK_FOR.ASK_OBJECT_EXCEPTION)) {
                return userObject;
            }
            PropertiesException propertiesException = new PropertiesException("Unbekannte Rückfrage: " + askFor + "!");
            throw propertiesException;
        } catch (Exception ex) {

        }
        return null;
    }
    /**-------------------------------------------------------------------------------------------------------**/
    /***********************************************************************************************************/

    /***********************************************************************************************************/
    /****************************************  CommandExecutorListener  ****************************************/
    /***********************************************************************************************************/
    @Override
    public void progress(String strInfo) {
        notifyClientJob(Level.INFO, strInfo);
    }

    /**-------------------------------------------------------------------------------------------------------**/
    /***********************************************************************************************************/

    /***********************************************************************************************************/
    /**************************************      static methods     ********************************************/
    /***********************************************************************************************************/
    private static void intro(String strErr) {
        System.out.println("Start mit folgenden Parametern: -e:<Umgebung> -r:<Test-Root> -b:<Branch>");
        System.out.println("\t-e: " + "ENE oder GEE oder ABE");
        System.out.println("\t-r: " + "ITSQ");
        System.out.println("\t-b: " + "ZWEI_PHASEN");
        throw new RuntimeException(strErr);
    }

    /**
     * Launch-Configurations für ActivitiTestAutomatisierung:
     * Main-Class: de.creditreform.crefoteam.cte.tesun.auto.ActivitiTestAutomatisierung
     * Environment: -Dlog4j.debug=true -Dlog4j.configuration=file:log4j.properties
     * Prog- Args: -e:<Umgebung> -r:<Test-Root> -b:<Branch>
     * z.B.:       -e=ENE -r=ITSQ -b=ZWEI_PHASEN
     * WorkingDir: $MODULE_DIR$
     */
    public static void main(String[] cmdArgs) {
        String currTestSrc = null;
        String currRepoBranch = null;
        try {
            EnvironmentConfig environmentConfig = null;
            // Start mit folgenden Parametern: -e=<Umgebung> -r=<Test-Root> -t=<Test-Typ> -b=<Branch>
            for (String cmdArg : cmdArgs) {
                String[] split = cmdArg.split("[:=]");
                String argName = split[0];
                String argValue = split[1];
                if (argName.equals("e") || argName.equals("-e")) {
                    environmentConfig = new EnvironmentConfig(argValue);
                } else if (cmdArg.startsWith("-r")) {
                    currTestSrc = argValue;
                } else if (cmdArg.startsWith("-b")) {
                    currRepoBranch = argValue;
                } else {
                    intro(String.format("Der Parameter '%s' wird nicht unterstützt!\n", cmdArg));
                }
            }
            if (environmentConfig == null) {
                intro("Der Parameter für Umgebung fehlt!");
            }
            if (currTestSrc == null) {
                intro("Der Parameter für Test-Source fehlt!");
            }
            if (currTestSrc == "ITSQ" && currRepoBranch == null) {
                intro("Der Parameter für Git-Branch fehlt!");
            }
            ActivitiTestAutomatisierung cut = new ActivitiTestAutomatisierung(environmentConfig, currTestSrc, currRepoBranch);
            cut.startACTIVITPorocess();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            printWriter.flush();
            LOGGER.error(writer.toString());
            System.exit(-1);
        }
    }

}
