package de.creditreform.crefoteam.cte.tesun.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TestScenario {
    protected final static Logger logger = LoggerFactory.getLogger(TestScenario.class);

    private boolean activated = true;
    private TestCustomer testCustomer;
    private String scenarioName;

    private File itsqRefExportsFile;
    private File itsqRefExportsPropsFile;

    private File pseudoTestCasesFile;
    private File pseudoRefExportsFile;
    private File pseudoRefExportsPropsFile;

    private File collectedsFile;
    private File collectedsPropsFile;

    private File restoredCollectsFile;
    private File restoredCollectsPropsFile;

    private File checkedsFile;
    private Map<String, TestCrefo> testFallNameToTestCrefoMap = new TreeMap<>();
    private final Map<String, Map<String, TestResults>> testResultsMapForCommandsScenariosMap = new HashMap<>();

    public TestScenario(TestCustomer testCustomer, String scenarioName) {
        this.testCustomer = testCustomer;
        this.scenarioName = scenarioName;
    }

    public TestScenario(TestCustomer testCustomer, String scenarioName, List<File> archivBestandXmlFilesList) throws Exception {
        this.testCustomer = testCustomer;
        this.scenarioName = scenarioName;
        File srcFile = new File(testCustomer.getItsqRefExportsDir(), scenarioName);
        File[] files = srcFile.listFiles(pathname -> pathname.getName().endsWith(".properties"));
        if (files == null) {
            throw new RuntimeException(String.format("Das Test-Scenario '%s' für den Kunden '%s' enthält keine Properties-Dateien!\nDer Pfad ist '%s'!", scenarioName, testCustomer.getCustomerName(), srcFile.getAbsolutePath()));
        }
        if (files.length != 1) {
            throw new RuntimeException(String.format("Das Test-Scenario '%s' für den Kunden '%s' enthält %d Properties-Dateien\nErlaubt ist genau eine Properties-Datei!", scenarioName, testCustomer.getCustomerName(), files.length));
        }

        itsqRefExportsFile = new File(testCustomer.getItsqRefExportsDir(), scenarioName);
        itsqRefExportsPropsFile = new File(itsqRefExportsFile, files[0].getName());
        if(archivBestandXmlFilesList != null) {
            initItsqTestCasesData(archivBestandXmlFilesList);
        }
        extendTestCrefos(itsqRefExportsFile, itsqRefExportsPropsFile, new TestCrefoExtender() {
            @Override
            void fillExtraData(TestCrefo testCrefo, Long itsqCrefoNr, File xmlFile) {
                testCrefo.setItsqRexExportXmlFile(xmlFile);
            }
        });

        pseudoRefExportsFile = new File(testCustomer.getPseudoRefExportsDir(), scenarioName);
        pseudoRefExportsPropsFile = new File(pseudoRefExportsFile, files[0].getName());
        refreshPseudoRefExports();

        collectedsFile = new File(testCustomer.getCollectedsDir(), scenarioName);
        collectedsPropsFile = new File(collectedsFile, files[0].getName());
        refreshCollecteds();

        restoredCollectsFile = new File(testCustomer.getRestoredCollectedsDir(), scenarioName);
        restoredCollectsPropsFile = new File(restoredCollectsFile, files[0].getName());
        refreshRestoredCollects();
        checkedsFile = new File(testCustomer.getChecksDir(), scenarioName);
    }

    public TestScenario(TestScenario toBeCloned) {
        setActivated(toBeCloned.isActivated());
        setScenarioName(toBeCloned.getScenarioName());
        setTestCustomer(toBeCloned.getTestCustomer());

        setItsqRefExportsFile(toBeCloned.getItsqRefExportsFile());
        setItsqRefExportsPropsFile(toBeCloned.getItsqRefExportsPropsFile());

        setCollectedsFile(toBeCloned.getCollectedsFile());
        setCollectedsPropsFile(toBeCloned.getCollectedsPropsFile());

        setRestoredCollectsFile(toBeCloned.getRestoredCollectsFile());
        setRestoredCollectsPropsFile(toBeCloned.getRestoredCollectsPropsFile());

        setPseudoRefExportsFile(toBeCloned.getPseudoRefExportsFile());
        setPseudoRefExportsPropsFile(toBeCloned.getPseudoRefExportsPropsFile());

        setCheckedsFile(toBeCloned.getCheckedsFile());

        testFallNameToTestCrefoMap.putAll(toBeCloned.getTestFallNameToTestCrefoMap());
        testResultsMapForCommandsScenariosMap.putAll(getTestResultsMapForCommandsScenariosMap());

    }

    @Override
    public String toString() {
        return scenarioName + " #" + testFallNameToTestCrefoMap.size();
    }

    public void refreshRestoredCollects() {
        if (restoredCollectsPropsFile.exists()) {
            extendTestCrefos(restoredCollectsFile, restoredCollectsPropsFile, new TestCrefoExtender() {
                @Override
                void fillExtraData(TestCrefo testCrefo, Long itsqCrefoNr, File xmlFile) {
                    testCrefo.setRestoredXmlFile(xmlFile);
                }
            });
        }
    }

    public void refreshCollecteds() {
        if (collectedsPropsFile.exists()) {
            extendTestCrefos(collectedsFile, collectedsPropsFile, new TestCrefoExtender() {
                @Override
                void fillExtraData(TestCrefo testCrefo, Long pseudoCrefoNr, File xmlFile) {
                    testCrefo.setCollectedXmlFile(xmlFile);
                }
            });
        }
    }

    public void refreshPseudoRefExports() {
        if (pseudoRefExportsPropsFile.exists()) {
            extendTestCrefos(pseudoRefExportsFile, pseudoRefExportsPropsFile, new TestCrefoExtender() {
                @Override
                void fillExtraData(TestCrefo testCrefo, Long pseudoCrefoNr, File xmlFile) {
                    testCrefo.setPseudoCrefoNr(pseudoCrefoNr);
                    testCrefo.setPseudoRefExportXmlFile(xmlFile);
                }
            });
        }
    }

    public TestResults getTestResultsForScenario(String command, String scenarioName) {
        Map<String, TestResults> testResultsMapForCommand = testResultsMapForCommandsScenariosMap.get(command);
        if (testResultsMapForCommand == null) {
            testResultsMapForCommand = new HashMap<>();
            testResultsMapForCommandsScenariosMap.put(command, testResultsMapForCommand);
        }
        TestResults testResultsForScenario = testResultsMapForCommand.get(scenarioName);
        if (testResultsForScenario == null) {
            testResultsForScenario = new TestResults(scenarioName);
            testResultsMapForCommand.put(scenarioName, testResultsForScenario);
        }
        return testResultsForScenario;
    }

    public void addResultInfo(String command, TestResults.ResultInfo resultInfo) {
        TestResults testResult = getTestResultsForScenario(command, scenarioName);
        testResult.addResultInfo(resultInfo);
    }

    public void removeResultInfoForCommand(String command) {
        testResultsMapForCommandsScenariosMap.remove(command);
    }

    public Map<String, Map<String, TestResults>> getTestResultsMapForCommandsScenariosMap() {
        return testResultsMapForCommandsScenariosMap;
    }

    public TestCustomer getTestCustomer() {
        return testCustomer;
    }

    public void setTestCustomer(TestCustomer testCustomer) {
        this.testCustomer = testCustomer;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getCusomerKey() {
        return testCustomer.getCustomerKey();
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public List<TestCrefo> getTestCrefosAsList() {
        return new ArrayList(getTestFallNameToTestCrefoMap().values());
    }

    public Map<String, TestCrefo> getTestFallNameToTestCrefoMap() {
        return testFallNameToTestCrefoMap;
    }

    public void setTestFallNameToTestCrefoMap(Map<String, TestCrefo> testFallNameToTestCrefoMap) {
        this.testFallNameToTestCrefoMap = testFallNameToTestCrefoMap;
    }

    /*******************     ItsqRefExports  *************************/
    public File getItsqRefExportsFile() {
        return itsqRefExportsFile;
    }

    public void setItsqRefExportsFile(File itsqRefExportsFile) {
        this.itsqRefExportsFile = itsqRefExportsFile;
    }

    public File getItsqRefExportsPropsFile() {
        return itsqRefExportsPropsFile;
    }

    public void setItsqRefExportsPropsFile(File itsqRefExportsPropsFile) {
        this.itsqRefExportsPropsFile = itsqRefExportsPropsFile;
    }

    /*------------------     ItsqRefExports  ------------------------*/

    /*******************     PseudoRefExports  *************************/
    public File getPseudoRefExportsFile() {
        return pseudoRefExportsFile;
    }

    public void setPseudoRefExportsFile(File pseudoRefExportsFile) {
        this.pseudoRefExportsFile = pseudoRefExportsFile;
    }

    public File getPseudoRefExportsPropsFile() {
        return pseudoRefExportsPropsFile;
    }

    public void setPseudoRefExportsPropsFile(File pseudoRefExportsPropsFile) {
        this.pseudoRefExportsPropsFile = pseudoRefExportsPropsFile;
    }
    /*------------------     PseudoRefExports  ------------------------*/

    /*******************     Collecteds  *************************/
    public File getCollectedsFile() {
        return collectedsFile;
    }

    public void setCollectedsFile(File collectedsFile) {
        this.collectedsFile = collectedsFile;
    }

    public File getCollectedsPropsFile() {
        return collectedsPropsFile;
    }

    public void setCollectedsPropsFile(File collectedsPropsFile) {
        this.collectedsPropsFile = collectedsPropsFile;
    }
    /*------------------     Collecteds  ------------------------*/

    /*******************     RestoredCollects  *************************/
    public File getRestoredCollectsFile() {
        return restoredCollectsFile;
    }

    public void setRestoredCollectsFile(File restoredCollectsFile) {
        this.restoredCollectsFile = restoredCollectsFile;
    }

    public File getRestoredCollectsPropsFile() {
        return restoredCollectsPropsFile;
    }

    public void setRestoredCollectsPropsFile(File restoredCollectsPropsFile) {
        this.restoredCollectsPropsFile = restoredCollectsPropsFile;
    }
    /*------------------     RestoredCollects  ------------------------*/

    /*******************     Checked  *************************/
    public File getCheckedsFile() {
        return checkedsFile;
    }

    public void setCheckedsFile(File checkedsFile) {
        this.checkedsFile = checkedsFile;
    }
    /*------------------     Checked  ------------------------*/

    private void extendTestCrefos(final File theFile, final File thePropsFile, TestCrefoExtender testCrefoExtender) {
        if (!theFile.exists()) {
            return;
        }
        try {
            final Collection<File> allXmlFiles = FileUtils.listFiles(theFile, new String[]{"xml"}, true);
            final List<String> propsFileContent = FileUtils.readLines(thePropsFile);
            propsFileContent.forEach(line -> {
                if (!line.isBlank() && !line.startsWith("#")) {
                    String[] splitEqual = line.split("=");
                    try {
                        String testFallName = splitEqual[0];
                        final String[] splitHash = splitEqual[1].trim().split("#");
                        long crefoNr = Long.parseLong(splitHash[0].trim());
                        TestCrefo testCrefo = testFallNameToTestCrefoMap.get(testFallName);
                        if (testCrefo == null) {
                            String errorStr = "TestCrefo mit dem Namen: " + testFallName + " konnte nicht in der Map gefunden werden!\n\t" + line;
                            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                            addResultInfo("INIT-SCENARIO", resultInfo);
                        }
                        if (testCrefo != null) {
                            File xmlFile = findXmlFileForTestfallAndCrefo(allXmlFiles, testFallName, crefoNr);
                            testCrefoExtender.fillExtraData(testCrefo, crefoNr, xmlFile);
                        }
                    } catch (Exception ex) {
                        String errorStr = "Exception in der Zeile '" + line + "' der Datei '" + thePropsFile.getName() + "':\n" + ex.getMessage();
                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                        addResultInfo("INIT-SCENARIO", resultInfo);
                    }
                }
            });
        } catch (IOException ex) {
            String errorStr = "Exception beim Lesen der Properties-Datei '" + thePropsFile.getAbsolutePath() + "'!\n" + ex.getMessage();
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
            addResultInfo("INIT-SCENARIO", resultInfo);
        }
    }

    protected void initItsqTestCasesData(Collection<File> phaseXmlFiles) {
        try {
            List<String> propsFileContent = FileUtils.readLines(itsqRefExportsPropsFile);
            propsFileContent.forEach(line -> {
                if (!line.isBlank() && !line.startsWith("#")) {
                    String[] splitEqual = line.split("=");
                    try {
                        String testFallName = splitEqual[0];
                        boolean shouldBeExported = !splitEqual[0].trim().startsWith("n");
                        final String[] splitHash = splitEqual[1].trim().split("#");
                        long crefoNr = Long.parseLong(splitHash[0].trim());
                        String testFallInfo = "Norbert's Faulheit:-)'";
                        if (splitHash.length > 1) {
                            testFallInfo = splitHash[1];
                        }
                        File phaseXmlFile = findXmlFileForCrefo(phaseXmlFiles, crefoNr);
                        if (phaseXmlFile == null) {
                            String errorStr = "Für die Crefo " + crefoNr + " wurde keine XML-Datei im ITSQ-PHASE-2 Verzeichnis gefunden!";
                            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                            addResultInfo("INIT-SCENARIO", resultInfo);
                        }
                        TestCrefo testCrefo = testFallNameToTestCrefoMap.get(testFallName);
                        if (testCrefo == null) {
                            testCrefo = new TestCrefo(testFallName, crefoNr, testFallInfo, shouldBeExported, phaseXmlFile);
                            testFallNameToTestCrefoMap.put(testFallName, testCrefo);
                        } else {
                            String errorStr = "Für den Testfall " + testFallName + " wurde kein Eintrag gefunden!";
                            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                            addResultInfo("INIT-SCENARIO", resultInfo);
                        }
                    } catch (Exception ex) {
                        String errorStr = "Exception in der Zeile '" + line + "' der Datei '" + itsqRefExportsPropsFile.getName() + "':\n" + ex.getMessage();
                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                        addResultInfo("INIT-SCENARIO", resultInfo);
                    }
                }
            });
        } catch (IOException ex) {
            String errorStr = "Exception beim Lesen der Properties-Datei '" + itsqRefExportsPropsFile.getAbsolutePath() + "'!\n" + ex.getMessage();
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
            addResultInfo("INIT-SCENARIO", resultInfo);
        }
    }

    private File findXmlFileForCrefo(Collection<File> allXmlFiles, long crefoNr) {
        // TODO was soll dassss?????
        List<File> collect = allXmlFiles.stream().filter(theFile -> theFile.getName().contains(crefoNr + "")).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }

    private File findXmlFileForTestfallAndCrefo(Collection<File> allXmlFiles, String testFallName, long crefoNr) {
        List<File> collect = allXmlFiles.stream().filter(theFile -> theFile.getName().contains(testFallName) && theFile.getName().contains(crefoNr + "")).collect(Collectors.toList());
        return collect.isEmpty() ? null : collect.get(0);
    }

    public void dumpResults(String command, StringBuilder stringBuilder, String prefix) {
        TestResults testResults = getTestResultsForScenario(command, getScenarioName());
        StringBuilder stringBuilderLocal = new StringBuilder();
        testResults.dumpResults(stringBuilderLocal, prefix);
        if (stringBuilderLocal.length() > 0) {
            stringBuilder.append(prefix + "Test-Results für Test-Scenario '" + getScenarioName() + "'");
            stringBuilder.append(stringBuilderLocal);
        }
    }

    public void clrearTestResults() {
        testResultsMapForCommandsScenariosMap.entrySet().forEach(mapEntry -> {
            Map<String, TestResults> testResultsMap = mapEntry.getValue();
            testResultsMap.clear();
        });
    }

    public StringBuilder dump(String prefix) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix + "\tScenario: " + scenarioName);
        stringBuilder.append(prefix + "\t\tTest-Crefos");
        stringBuilder.append(prefix + "\t\t\ttestFallName\titsqTestCrefoNr\tpseudoCrefoNr\titsqPhase2XmlFile\titsqRexExportXmlFile\tpseudoRefExportXmlFile\tcollectedXmlFile\trestoredXmlFile");
        testFallNameToTestCrefoMap.entrySet().forEach(testCrefoEntry -> {
            TestCrefo testCrefo = testCrefoEntry.getValue();
            stringBuilder.append(testCrefo.dump(prefix + "\t\t\t"));
        });
        return stringBuilder;
    }

    private abstract class TestCrefoExtender {
        abstract void fillExtraData(TestCrefo testCrefo, Long crefoNrFromPropsFile, File xmlFile);
    }
}
