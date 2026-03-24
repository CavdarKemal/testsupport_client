package de.creditreform.crefoteam.cte.tesun.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class TestCustomer {
    private String customerKey;
    private String jvmName;
    private String customerName;
    private TestSupportClientKonstanten.TEST_PHASE testPhase;
    private Calendar lastJobStartetAt;
    private String exportUrl;
    private String uploadUrl;
    private String exportJobName;
    private String uploadJobName;
    private String processIdentifier;
    private File itsqAB30XmlsDir;
    private File itsqRefExportsDir;
    private File pseudoRefExportsDir;
    private File collectedsDir;
    private File restoredCollectedsDir;
    private File checksDir;
    private File sftpUploadsDir;
    private File testResultsFile;
    private boolean activated = true;
    private String customerPropertyPrefix;
    private String fwAktualisierungsdatum;
    private String pdVersion;

    private Map<String, TestResults> testResultsMapForCommands = new HashMap<>();
    private Map<String, TestScenario> testScenariosMap = new HashMap<>();

    private ArrayList<MutablePair<String, String>> properyPairsList = new ArrayList<>() {
        {
            add(new MutablePair<>("%.vc", ""));
            add(new MutablePair<>("%.exportFormat", ""));
            add(new MutablePair<>("%.exportFormat.branchen", ""));
            add(new MutablePair<>("%.exportFormat.options", ""));
            add(new MutablePair<>("%.extra_xml_features", ""));
            add(new MutablePair<>("%.extra_xml_features.branchen", ""));
            add(new MutablePair<>("VerarbeitungBereich.vc", ""));
            add(new MutablePair<>("beteiligungenImport.beteiligungen_import.vc", ""));
            add(new MutablePair<>("beteiligungenImportDelta.beteiligungen_import.vc", ""));
            add(new MutablePair<>("beteiligungenImportFull.beteiligungen_import.vc", ""));
            add(new MutablePair<>("beteiligungen_import.vc", ""));
            add(new MutablePair<>("ctImportDelta.ctimport.vc", ""));
            add(new MutablePair<>("ctImportFull.ctimport.vc", ""));
            add(new MutablePair<>("ctimport.vc", ""));
            add(new MutablePair<>("deltaImport.ctimport.vc", ""));
            add(new MutablePair<>("importCycle.beteiligungen_import.vc", ""));
            add(new MutablePair<>("importCycle.ctimport.vc", ""));
            add(new MutablePair<>("relevanzMigrationDelta.relevanzmigration.vc", ""));
            add(new MutablePair<>("relevanzMigrationFull.relevanzmigration.vc", ""));
        }
    };

    public TestCustomer(String customerKey, String customerName) {
        this.customerKey = customerKey;
        this.customerName = customerName;
        this.testPhase = TestSupportClientKonstanten.TEST_PHASE.values()[0];
    }

    public TestCustomer(String customerKey, File itsqRoot, File testOutputsFile, TestSupportClientKonstanten.TEST_PHASE testPhase) {
        this.customerKey = customerKey;
        this.customerName = customerKey;
        this.testPhase = testPhase;

        File archivBestandRoot = new File(itsqRoot, TestSupportClientKonstanten.ARCHIV_BESTAND); // "TESTS/LOCAL/ARCHIV-BESTAND"
        this.itsqAB30XmlsDir = new File(archivBestandRoot, testPhase.getDirName()); // "TESTS/LOCAL/ARCHIV-BESTAND/PHASE-X"

        File itsqRefExportsRoot = new File(itsqRoot, TestSupportClientKonstanten.REF_EXPORTS);// "TESTS/LOCAL/RFEF-EXPORTS"
        File itsqRefExportsPhase = new File(itsqRefExportsRoot, testPhase.getDirName());// "TESTS/LOCAL/RFEF-EXPORTS/PHASE-X"
        this.itsqRefExportsDir = new File(itsqRefExportsPhase, customerKey.toLowerCase(Locale.ROOT));// "TESTS/LOCAL/RFEF-EXPORTS/PHASE-X/ABC"

        File pseudoRefExportsPhase = new File(new File(testOutputsFile, TestSupportClientKonstanten.PSEUDO_REF_EXPORTS), testPhase.getDirName());
        this.pseudoRefExportsDir = new File(pseudoRefExportsPhase, customerKey.toLowerCase(Locale.ROOT));

        File checksPhase = new File(new File(testOutputsFile, TestSupportClientKonstanten.CHECKED), testPhase.getDirName());
        this.checksDir = new File(checksPhase, customerKey.toLowerCase(Locale.ROOT));

        File collectedsPhase = new File(new File(testOutputsFile, TestSupportClientKonstanten.COLLECTED), testPhase.getDirName());
        this.collectedsDir = new File(collectedsPhase, customerKey.toLowerCase(Locale.ROOT));

        File restoredCollectedsPhase = new File(new File(testOutputsFile, TestSupportClientKonstanten.RESTORED_COLLECTS), testPhase.getDirName());
        this.restoredCollectedsDir = new File(restoredCollectedsPhase, customerKey.toLowerCase(Locale.ROOT));

        File sftpUploadsPhase = new File(new File(testOutputsFile, TestSupportClientKonstanten.SFTP_UPLOADS), testPhase.getDirName());
        this.sftpUploadsDir = new File(sftpUploadsPhase, customerKey.toLowerCase(Locale.ROOT));
    }

    public TestCustomer(TestCustomer toBeCloned) {
        setCustomerKey(toBeCloned.getCustomerKey());
        setCustomerName(toBeCloned.getCustomerName());
        setTestPhase(toBeCloned.getTestPhase());
        setItsqRefExportsDir(toBeCloned.getItsqRefExportsDir());
        setPseudoRefExportsDir(toBeCloned.getPseudoRefExportsDir());
        setChecksDir(toBeCloned.getChecksDir());
        setCollectedsDir(toBeCloned.getCollectedsDir());
        setRestoredCollectedsDir(toBeCloned.getRestoredCollectedsDir());
        setSftpUploadsDir(toBeCloned.getSftpUploadsDir());
        setExportUrl(toBeCloned.getExportUrl());
        setUploadUrl(toBeCloned.getUploadUrl());

        setJvmName(toBeCloned.getJvmName());
        setExportJobName(toBeCloned.getExportJobName());
        setUploadJobName(toBeCloned.getUploadJobName());
        setActivated(toBeCloned.isActivated());
        setProcessIdentifier(toBeCloned.getProcessIdentifier());
        setLastJobStartetAt(toBeCloned.getLastJobStartetAt());
        getPropertyPairsList().addAll(toBeCloned.getPropertyPairsList());
        for (TestScenario testScenario : toBeCloned.getTestScenariosList()) {
            TestScenario clonedTtestScenario = new TestScenario(testScenario);
            addTestScenario(clonedTtestScenario);
        }
    }

    public static TestCustomer cloneEhForRisksImport(TestCustomer ehTestCustomer) throws Exception {
        TestCustomer tmpCustomer = new TestCustomer(ehTestCustomer);
        tmpCustomer.setExportJobName("eh.riskNotificationsImport");
        return tmpCustomer;
    }

    public static TestCustomer cloneInsoMonitorPhase2(TestCustomer insomon1TestCustomer, String exportJobName, String uploadJobName, String processIdentifier) {
        TestCustomer tmpCustomer = new TestCustomer(insomon1TestCustomer);
        tmpCustomer.setExportUrl(insomon1TestCustomer.getExportUrl());
        tmpCustomer.setUploadUrl(insomon1TestCustomer.getUploadUrl());
        tmpCustomer.setCustomerKey(insomon1TestCustomer.getCustomerKey());
        tmpCustomer.setJvmName(insomon1TestCustomer.getJvmName());
        tmpCustomer.setCustomerName("Insolvenz Monitor Phase 2");
        tmpCustomer.setExportJobName(exportJobName);
        tmpCustomer.setUploadJobName(uploadJobName);
        tmpCustomer.getPropertyPairsList().addAll(insomon1TestCustomer.getPropertyPairsList());
        tmpCustomer.setProcessIdentifier(processIdentifier);
        tmpCustomer.setLastJobStartetAt(insomon1TestCustomer.getLastJobStartetAt());
        return tmpCustomer;
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public TestSupportClientKonstanten.TEST_PHASE getTestPhase() {
        return testPhase;
    }

    public void setTestPhase(TestSupportClientKonstanten.TEST_PHASE testPhase) {
        this.testPhase = testPhase;
    }

    public String getCustomerPropertyPrefix() {
        return customerPropertyPrefix;
    }

    public void setCustomerPropertyPrefix(String customerPropertyPrefix) {
        this.customerPropertyPrefix = customerPropertyPrefix;
    }

    public String getFwAktualisierungsdatum() {
        return fwAktualisierungsdatum;
    }

    public void setFwAktualisierungsdatum(String fwAktualisierungsdatum) {
        this.fwAktualisierungsdatum = fwAktualisierungsdatum;
    }

    public String getPdVersion() {
        return pdVersion;
    }

    public void setPdVersion(String pdVersion) {
        this.pdVersion = pdVersion;
    }

    public MutablePair<String, String> getProperty(String propName) {
        Optional<MutablePair<String, String>> optionalPair = getPropertyPairsList().stream().filter(pair -> {
            return pair.getKey().equals(propName);
        }).findFirst();
        return optionalPair.orElseGet(() -> new MutablePair<>(propName, ""));
    }

    public void setProperty(Pair<String, String> propertyPair) {

    }

    public List<MutablePair<String, String>> getPropertyPairsList() {
        return properyPairsList;
    }

    public String getExportUrl() {
        return exportUrl;
    }

    public void setExportUrl(String exportUrl) {
        this.exportUrl = exportUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getProcessIdentifier() {
        return processIdentifier;
    }

    public void setProcessIdentifier(String processIdentifier) {
        this.processIdentifier = processIdentifier;
    }

    public String getExportJobName() {
        return exportJobName;
    }

    public void setExportJobName(String exportJobName) {
        this.exportJobName = exportJobName;
    }

    public String getUploadJobName() {
        return uploadJobName;
    }

    public void setUploadJobName(String uploadJobName) {
        this.uploadJobName = uploadJobName;
    }

    public File getItsqAB30XmlsDir() {
        return itsqAB30XmlsDir;
    }

    public void setItsqAB30XmlsDir(File itsqAB30XmlsDir) {
        this.itsqAB30XmlsDir = itsqAB30XmlsDir;
    }

    public File getItsqRefExportsDir() {
        return itsqRefExportsDir;
    }

    public void setItsqRefExportsDir(File itsqRefExportsDir) {
        this.itsqRefExportsDir = itsqRefExportsDir;
    }

    public File getPseudoRefExportsDir() {
        return pseudoRefExportsDir;
    }

    public void setPseudoRefExportsDir(File pseudoRefExportsDir) {
        this.pseudoRefExportsDir = pseudoRefExportsDir;
    }

    public File getCollectedsDir() {
        return collectedsDir;
    }

    public void setCollectedsDir(File collectedsDir) {
        this.collectedsDir = collectedsDir;
    }

    public File getRestoredCollectedsDir() {
        return restoredCollectedsDir;
    }

    public void setRestoredCollectedsDir(File restoredCollectedsDir) {
        this.restoredCollectedsDir = restoredCollectedsDir;
    }

    public File getChecksDir() {
        return checksDir;
    }

    public void setChecksDir(File checksDir) {
        this.checksDir = checksDir;
    }

    public File getTestResultsFile() {
        return testResultsFile;
    }

    public void setTestResultsFile(File testResultsFile) {
        this.testResultsFile = testResultsFile;
    }

    public File getSftpUploadsDir() {
        return sftpUploadsDir;
    }

    public void setSftpUploadsDir(File sftpUploadsDir) {
        this.sftpUploadsDir = sftpUploadsDir;
    }

    public Calendar getLastJobStartetAt() {
        return lastJobStartetAt;
    }

    public void setLastJobStartetAt(Calendar lastJobStartetAt) {
        this.lastJobStartetAt = lastJobStartetAt;
    }

    public void addTestScenario(TestScenario testScenario) {
        testScenariosMap.put(testScenario.getScenarioName(), testScenario);
    }

    public Map<String, TestScenario> getTestScenariosMap() {
        return testScenariosMap;
    }

    public List<TestScenario> getTestScenariosList() {
        List<TestScenario> theList = new ArrayList<>(testScenariosMap.values());
        return theList;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated;
    }

    public TestScenario getScenario(String scenarioName) {
        return testScenariosMap.get(scenarioName);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", customerKey, customerName);
    }

    public List<Long> getAllTestCrefosAsLongList(boolean activeOnly, boolean positiveOnly) {
        List<TestCrefo> testCrefosList = getAllTestCrefos(activeOnly, positiveOnly);
        List<Long> crefosList = new ArrayList<>();
        testCrefosList.forEach(testCrefo -> {
            if (!activeOnly || testCrefo.isActivated()) {
                if (!positiveOnly || testCrefo.isShouldBeExported()) {
                    crefosList.add(testCrefo.getItsqTestCrefoNr());
                }
            }
        });
        return crefosList;
    }

    public List<TestCrefo> getAllTestCrefos(boolean activeOnly, boolean positiveOnly) {
        List<TestCrefo> testCrefosList = new ArrayList<>();
        final List<TestScenario> testScenariosList = getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            if (!activeOnly || testScenario.isActivated()) {
                Map<String, TestCrefo> testFallNameToTestCrefoMap = testScenario.getTestFallNameToTestCrefoMap();
                testFallNameToTestCrefoMap.entrySet().forEach(testCrefoEntry -> {
                    TestCrefo testCrefo = testCrefoEntry.getValue();
                    if (!activeOnly || testCrefo.isActivated()) {
                        if (!positiveOnly || testCrefo.isShouldBeExported()) {
                            testCrefosList.add(testCrefo);
                        }
                    }
                });
            }
        }
        return testCrefosList;
    }

    public void refreshCollecteds() {
        final List<TestScenario> testScenariosList = getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            testScenario.refreshCollecteds();
        }
    }

    public void refreshPseudoRefExports() {
        final List<TestScenario> testScenariosList = getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            testScenario.refreshPseudoRefExports();
        }
    }

    public void refreshRestoredCollects() {
        final List<TestScenario> testScenariosList = getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            testScenario.refreshRestoredCollects();
        }
    }

    public TestResults getTestResultsForCommand(String command) {
        TestResults testResults = testResultsMapForCommands.get(command);
        if (testResults == null) {
            testResults = new TestResults(command);
            testResultsMapForCommands.put(command, testResults);
        }
        return testResults;
    }

    public Map<String, TestResults> getTestResultsMapForCommands() {
        return testResultsMapForCommands;
    }

    public void emptyTestResultsMapForCommands() {
        testResultsMapForCommands.entrySet().forEach(testResultsEntry -> {
            testResultsEntry.getValue().getResultInfosList().clear();
        });
    }

    public TestResults addTestResultsForCommand(String command) {
        testResultsMapForCommands.remove(command);
        getTestScenariosMap().entrySet().forEach(testScenarioEntry -> {
            TestScenario testScenario = testScenarioEntry.getValue();
            testScenario.removeResultInfoForCommand(command);
        });
        return getTestResultsForCommand(command);
    }

    public void addResultInfo(String command, TestResults.ResultInfo resultInfo) {
        TestResults testResult = getTestResultsForCommand(command);
        testResult.addResultInfo(resultInfo);
    }

    public void dumpResults(StringBuilder sbForTestCustomer, String prefix) {
        Map<String, TestResults> testResultsMap = getTestResultsMapForCommands();
        if (!testResultsMap.isEmpty()) {
            sbForTestCustomer.append(prefix + "Test-Results für den Kunden '" + getCustomerKey() + "'");
        }
        testResultsMap.entrySet().forEach(resultsKey -> {
            TestResults testResults = resultsKey.getValue();
            StringBuilder sbForCommand = new StringBuilder();
            testResults.dumpResults(sbForCommand, prefix + "\t");
            List<StringBuilder> sbForScenariosList = new ArrayList<>();
            getTestScenariosMap().entrySet().forEach(testScenarioEntry -> {
                TestScenario testScenario = testScenarioEntry.getValue();
                StringBuilder sbForScenario = new StringBuilder();
                testScenario.dumpResults(testResults.getCommand(), sbForScenario, prefix + "\t\t");
                if (sbForScenario.length() > 0) {
                    sbForScenariosList.add(sbForScenario);
                }
            });
            if (sbForCommand.length() > 0 || sbForScenariosList.size() > 0) {
                sbForTestCustomer.append(prefix + "\tTest-Results für UserTask '" + testResults.getCommand() + "'");
                sbForTestCustomer.append(sbForCommand);
            }
            sbForScenariosList.forEach(stringBuilder -> {
                sbForTestCustomer.append(stringBuilder);
            });
        });
    }

    public File dumpResultsToFile() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        dumpResults(stringBuilder, "\n");
        if (stringBuilder.length() > 0) {
            File theFile = new File(getChecksDir(), "TestResults.txt");
            setTestResultsFile(theFile);
            FileUtils.writeStringToFile(theFile, stringBuilder.toString(), false);
            return theFile;
        }
        return null;
    }

    public void clrearTestResults() {
        Map<String, TestResults> testResultsMap = getTestResultsMapForCommands();
        testResultsMap.entrySet().forEach(resultsKey -> {
            TestResults testResults = resultsKey.getValue();
            testResults.getResultInfosList().clear();
            getTestScenariosMap().entrySet().forEach(testScenarioEntry -> {
                TestScenario testScenario = testScenarioEntry.getValue();
                testScenario.clrearTestResults();
            });
        });
    }

    public StringBuilder dumpTestCustomer(String prefix) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix + "Kunde " + customerKey);
        getTestScenariosMap().entrySet().forEach(testScenarioEntry -> {
            TestScenario testScenario = testScenarioEntry.getValue();
            stringBuilder.append(prefix + testScenario.dump("\n\t"));
        });
        return stringBuilder;
    }

}
