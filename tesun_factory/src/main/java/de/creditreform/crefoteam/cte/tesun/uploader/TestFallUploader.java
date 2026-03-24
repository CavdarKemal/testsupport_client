package de.creditreform.crefoteam.cte.tesun.uploader;

import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.AB30MapperUtil;
import de.creditreform.crefoteam.cte.tesun.util.AB30XMLProperties;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestFallFileUtil;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

public class TestFallUploader extends AbstractTesunClientJob {
    public static final String COMMAND = "UserTask UPLOAD-PSEUDO-CREFOS";
    public static final String DESCRIPTION = "invoke upload of generated xml into cte";

    private String uploadPath;
    protected TesunRestService tesunRestServiceWLS;
    protected TesunRestService tesunRestServiceJVMImportC;
    EnvironmentConfig environmentConfig;
    private final Map<String, TestCustomer> activeCustomersMap;
    private final TestSupportClientKonstanten.TEST_TYPES testType;
    private final TestSupportClientKonstanten.TEST_PHASE testPhase;

    @Inject
    private TestSupportMutableState mutableState;
    private final Boolean uploadSyntheticTestFiles;

    public TestFallUploader(Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap, Boolean uploadSyntheticTestFiles, TestSupportClientKonstanten.TEST_TYPES testType, TestSupportClientKonstanten.TEST_PHASE testPhase, TesunClientJobListener tesunClientJobListener) {
        super(COMMAND, DESCRIPTION, tesunClientJobListener);
        this.testType = testType;
        this.testPhase = testPhase;
        this.activeCustomersMap = selectedCustomersMapMap.get(testPhase);
        this.uploadSyntheticTestFiles = uploadSyntheticTestFiles;
    }

    @Override
    public void init(EnvironmentConfig envConfig) throws Exception {
        this.environmentConfig = envConfig;
        uploadPath = TesunUtilites.checkAndCreateDirectory(envConfig.getPseudoArchivBestandsRoot(testPhase), false);
        tesunRestServiceWLS = new TesunRestService(envConfig.getRestServiceConfigsForMasterkonsole().get(0), tesunClientJobListener);
        tesunRestServiceJVMImportC = new TesunRestService(envConfig.getRestServiceConfigsForJvmImpCycle().get(0), tesunClientJobListener);
    }

    @Override
    public JOB_RESULT call() throws Exception {
        printHeader(Level.INFO, COMMAND, testPhase);
        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "VOR-" + COMMAND, activeCustomersMap);
        notifyTesunClientJobListener(Level.INFO, String.format("\nCrefos aus dem Verzeichnis %s werden hochgeladen...", uploadPath));

        File ab30XmlsDir = new File(environmentConfig.getArchivBestandsRoot(), testPhase.getDirName());
        File newPorpsFile = new File(ab30XmlsDir, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        AB30MapperUtil ab30MapperUtil = new AB30MapperUtil(environmentConfig, tesunClientJobListener, false, null);
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap = ab30MapperUtil.initAb30CrefoPropertiesMap(newPorpsFile);
        // filtere für die aktiven Kunden der aktiven Phase
        Map<Long, AB30XMLProperties> filteredAb30XMLPropertiesMapPhaseX = ab30MapperUtil.filterAb30CrefoPropertiesMap("\n", ab30CrefoToPropertiesMap, activeCustomersMap, true);

        CrefosUploader crefosUploader = new CrefosUploader(uploadPath, filteredAb30XMLPropertiesMapPhaseX, tesunRestServiceWLS, tesunRestServiceJVMImportC, tesunClientJobListener);
        activeCustomersMap.entrySet().forEach(testCustomerEntry -> {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            testCustomer.addTestResultsForCommand(COMMAND);
            crefosUploader.uploadCrefos(testCustomer, COMMAND);
            notifyTesunClientJobListener(Level.INFO, ".");
        });
        // synthetische Crefos zum Schluss hochladen, weil die maximale Crefo per REST-Service ermittelt wird!
        // da oben die Test-Crefos schon hochgeladen wurden, starten die synthetischen Crefos on top of the test-crefos
        notifyTesunClientJobListener(Level.INFO, "\nSynthetische Crefos werden hochgeladen...");
        if (uploadSyntheticTestFiles) {
            uploadSyntheticCrefos();
        }
        saveUploadedInfosFile();

        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "NACH-" + COMMAND, activeCustomersMap);
        printFooter(Level.INFO, COMMAND, testPhase);
        return JOB_RESULT.OK;
    }

    private void uploadSyntheticCrefos() throws Exception {
        File dstDir = new File(environmentConfig.getTestOutputsRoot(), "SYNTHETIC_CREFOS");
        List<File> contentFromFolder = TestFallFileUtil.downloadFolderContentFromFolder(TestSupportClientKonstanten.MAPPINGCOVERAGE_TEST_CREFOS, ".xml", dstDir);
        long nextCrefo = mutableState.getNextCrefo();
        for (File syntheCrefoFile : contentFromFolder) {
            if (syntheCrefoFile.getName().endsWith(".xml")) {
                String xmlContent = FileUtils.readFileToString(syntheCrefoFile, StandardCharsets.UTF_8);
                int crefoPos = xmlContent.indexOf("<crefonummer>");
                crefoPos += "<crefonummer>".length();
                StringBuilder stringBuilder = new StringBuilder(xmlContent.substring(0, crefoPos));
                stringBuilder.append(nextCrefo);
                stringBuilder.append(xmlContent.substring(crefoPos + 10));
                FileUtils.writeStringToFile(syntheCrefoFile, stringBuilder.toString());
                tesunRestServiceWLS.uploadCrefo(nextCrefo, syntheCrefoFile, TestSupportClientKonstanten.AB3_0_XSD);
                notifyTesunClientJobListener(Level.INFO, "\n\t\tSynthetische Crefo '" + syntheCrefoFile.getName() + "' wurde als " + nextCrefo + " wurde hochgeladen...");
                nextCrefo++;
            }
        }
    }

    private void saveUploadedInfosFile() throws IOException {
        String strUploadedInfos = "Dateien in diesem Verzeichnis sind hochgeladen.";
        File uploadedInfosFile = new File(uploadPath, TestSupportClientKonstanten.UPLOADED_INFOS_FILENAME);
        FileUtils.writeStringToFile(uploadedInfosFile, strUploadedInfos);
    }

/*
    private void uploadSyntheticCrefosByVisitor() throws Exception {
        // String SYNTHETIC_CREFOS_PATH = "testutils_cte/mappingcoverage/src/main/resources/mappingcoverage-test-crefos";
        String SYNTHETIC_CREFOS_PATH = "archivbestand30_jaxb/mapper_test_helper/src/main/resources/mappingcoverage-test-crefos";
        if (environmentConfig.getTestSetSources().equals("ITSQ")) {
            File itsqDir = new File(environmentConfig.getTestResourcesRoot(), "ITSQ");
            File srcDir = new File(itsqDir, SYNTHETIC_CREFOS_PATH);
            File dstDir = new File(environmentConfig.getTestOutputsRoot(), "SYNTHETIC_CREFOS");
            Long nextCrefo = mutableState.getNextCrefo();
            Path startingDir = Paths.get(srcDir.getAbsolutePath());
            MyFileVisitor<Path> visitor = new MyFileVisitor<>(dstDir, nextCrefo, tesunRestServiceWLS, tesunClientJobListener);
            Files.walkFileTree(startingDir, visitor);
        } else {
            notifyTesunClientJobListener(Level.INFO, "\n--- Übersprungen, weil der Test-Source nicht 'ITSQ' ist!");
        }
    }
*/

/*
    private static class MyFileVisitor<Path> extends SimpleFileVisitor<Path> {
        final File outputDir;
        long syntheticCrefoNr;
        final TesunRestService tesunRestServiceWLS;
        final TesunClientJobListener tesunClientJobListener;

        public MyFileVisitor(File outputDir, Long crefoNr, TesunRestService tesunRestServiceWLS, TesunClientJobListener tesunClientJobListener) {
            this.outputDir = outputDir;
            this.syntheticCrefoNr = crefoNr.longValue();
            this.tesunRestServiceWLS = tesunRestServiceWLS;
            this.tesunClientJobListener = tesunClientJobListener;
        }

        @Override
        public FileVisitResult visitFile(Path thePath, BasicFileAttributes attrs) {
            try {
                if (thePath.toString().endsWith(".xml")) {
                    File xmlFile = new File(thePath.toString());
                    String xmlContent = FileUtils.readFileToString(xmlFile);
                    int crefoPos = xmlContent.indexOf("<crefonummer>");
                    crefoPos += "<crefonummer>".length();
                    StringBuilder stringBuilder = new StringBuilder(xmlContent.substring(0, crefoPos));
                    stringBuilder.append(syntheticCrefoNr);
                    stringBuilder.append(xmlContent.substring(crefoPos + 10));
                    FileUtils.writeStringToFile(new File(outputDir, xmlFile.getName()), stringBuilder.toString());
                    tesunRestServiceWLS.uploadCrefo(syntheticCrefoNr, xmlFile, TestSupportClientKonstanten.AB3_0_XSD);
                    notifyTesunClientJobListener(Level.INFO, "\n\tSynthetische Crefo '" + xmlFile.getName() + "' wurde als " + syntheticCrefoNr + " wurde hochgeladen...");
                    syntheticCrefoNr++;
                }
            } catch (Exception ex) {
                notifyTesunClientJobListener(Level.ERROR, "\n\t!!! Synthetische Crefo " + syntheticCrefoNr + "\n'" + thePath.toString() + "'\nkonnte nicht hochgeladen werden!\n" + ex.getMessage());
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }

        protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
            if (tesunClientJobListener != null) {
                tesunClientJobListener.notifyClientJob(level, notifyInfo);
            }
        }
    }
*/
}
