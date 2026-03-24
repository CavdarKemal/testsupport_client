package de.creditreform.crefoteam.cte.tesun.generate;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.*;
import de.creditreform.crefoteam.cte.tesun.util.replacer.Replacer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class CustomerPseudoGenerator implements Callable<TestResults> {
    private final Replacer replacer;
    private final TestSupportClientKonstanten.TEST_PHASE testPhase;
    private final TesunClientJobListener tesunClientJobListener;
    private final TestCustomer testCustomer;
    public CustomerPseudoGenerator(TestCustomer testCustomer, TestSupportClientKonstanten.TEST_PHASE testPhase, Replacer replacer, TesunClientJobListener tesunClientJobListener) {
        this.testCustomer = testCustomer;
        this.replacer = replacer;
        this.testPhase = testPhase;
        this.tesunClientJobListener = tesunClientJobListener;
    }

    @Override
    public TestResults call() throws Exception {
        Map<String, TestScenario> testScenariosMap = testCustomer.getTestScenariosMap();
        testScenariosMap.entrySet().stream().forEach(entry -> {
            TestScenario testScenario = entry.getValue();
            if (testScenario.isActivated()) {
                try {
                    replaceScenarioFiles(testScenario);
                    notifyTesunClientJobListener(Level.INFO, ".");
                } catch (Exception ex) {
                    addResultInfo(testScenario, "Exception beim Ersetzen der Test-Crtefos für den Kunden '" + testCustomer.getCustomerKey() + ":" + testScenario.getScenarioName() + "'!\n" + ex.getMessage());
                }
            }
        });
        return testCustomer.getTestResultsForCommand(TestFallGeneratePseudoCrefos.COMMAND);
    }

    private void replaceScenarioFiles(TestScenario testScenario) throws Exception {
        String absolutePath = testScenario.getItsqRefExportsPropsFile().getAbsolutePath().replace(TestSupportClientKonstanten.TEST_PHASE.PHASE_2.getDirName(), testPhase.getDirName());
        File itsqPropertiesFile = new File(absolutePath);

        // Bei REF_EXPORTS gibt es die Properties-Datei...
        absolutePath = testScenario.getPseudoRefExportsPropsFile().getAbsolutePath().replace(TestSupportClientKonstanten.TEST_PHASE.PHASE_2.getDirName(), testPhase.getDirName());
        File pseudoRefExportsPropertiesFile = new File(absolutePath);
        replaceFileContent(itsqPropertiesFile, pseudoRefExportsPropertiesFile);

        // ... und die REF-XML's, replacen...
        List<String> pseudoRefExportsPropertiesFileContent = FileUtils.readLines(pseudoRefExportsPropertiesFile);
        absolutePath = testScenario.getItsqRefExportsFile().getAbsolutePath().replace(TestSupportClientKonstanten.TEST_PHASE.PHASE_2.getDirName(), testPhase.getDirName());
        File srcItsqRefExportsDir = new File(absolutePath);
        Collection<File> itsqXmlFilesList = FileUtils.listFiles(srcItsqRefExportsDir, new String[]{"xml"}, true);
        absolutePath = testScenario.getPseudoRefExportsFile().getAbsolutePath().replace(TestSupportClientKonstanten.TEST_PHASE.PHASE_2.getDirName(), testPhase.getDirName());
        File dstPseudoRefExportsDir = new File(absolutePath);
        pseudoRefExportsPropertiesFileContent.forEach(line -> {
            if (!line.isBlank() && !line.startsWith("#")) {
                String[] splitEqual = line.split("=");
                // lese Crefos aus der Props-File, ermittle die zugehörige XML-Datei aus ITSQ-REF-EXPORTS-Directory und führe Replacements durch...
                try {
                    String testFallName = splitEqual[0];
                    TestCrefo testCrefo = testScenario.getTestFallNameToTestCrefoMap().get(testFallName);
                    if (testCrefo == null) {
                        addResultInfo(testScenario, "Für den Testfall '" + testCrefo + "' wurde kein Test-Crefo ind der Map gefunden!");
                    }
                    else {
                        final String[] splitHash = splitEqual[1].trim().split("#");
                        long crefoNr = Long.parseLong(splitHash[0].trim());
                        testCrefo.setPseudoCrefoNr(crefoNr);
                        File xmlFile = findXmlFileForTestCrefo(testCrefo, itsqXmlFilesList);
                        if (xmlFile != null) {
                            testCrefo.setPseudoRefExportXmlFile(xmlFile);
                            String dstName = getPseudoDstName(xmlFile, pseudoRefExportsPropertiesFileContent);
                            if (dstName == null) {
                                addResultInfo(testScenario, "ITSQ.REF_EXPORTS-Ordner enthält die XML-Datei '" + xmlFile.getName() + "', ohne dass der Testfall '"+ testCrefo +"' in der Properties-Datei existiert!");
                            } else {
                                replaceFileContent(xmlFile, new File(dstPseudoRefExportsDir, dstName));
                            }
                        }
                        else {
                            // ein Negativ-Testfall!
                            if(!testCrefo.getTestFallName().startsWith("n")) {
                                addResultInfo(testScenario, "Für einen NICHT-Negativ-Testfall '" + testCrefo  + "' wurde im REF_EXPORTS keine REF-XML-Datei angegeben!");
                            }
                        }
                    }
                } catch (Exception ex) {
                    addResultInfo(testScenario, "Exception in der Zeile '" + line + "' der Datei '" + pseudoRefExportsPropertiesFile.getName() + "':\n" + ex.getMessage());
                }
            }
        });
    }

    private File findXmlFileForTestCrefo(TestCrefo testCrefo, Collection<File> itsqXmlFilesList) {
        for (File xmlFile : itsqXmlFilesList) {
            String xmlFileName = xmlFile.getName();
            if (xmlFileName.startsWith("XML")) {
                continue; // Snippets überspringen!
            }
            if (xmlFileName.startsWith(testCrefo.getTestFallName()) && xmlFileName.contains(testCrefo.getItsqTestCrefoNr()+"")) {
                return xmlFile;
            }
        }
        return null;
    }

    private String getPseudoDstName(File xmlFile, List<String> pseudoRefExportsPropertiesFileContent) {
        for (String strLine : pseudoRefExportsPropertiesFileContent) {
            if (strLine.isBlank() || strLine.startsWith("#")) {
                continue;
            }
            String[] split = strLine.split("=");
            if (xmlFile.getName().startsWith(split[0])) {
                Long originalCrefoNr = TesunUtilites.extractCrefonummerFromString(xmlFile.getName());
                Long pseudoCrefoNr = TesunUtilites.extractCrefonummerFromString(split[1]);
                return xmlFile.getName().replace("" + originalCrefoNr, "" + pseudoCrefoNr);
            }
        }
        return null;
    }

    private void replaceFileContent(File srcFile, File dstFile) throws IOException {
        FileInputStream inpStream = null;
        FileOutputStream outStream = null;
        try {
            inpStream = new FileInputStream(srcFile);
            dstFile.getParentFile().mkdirs();
            outStream = new FileOutputStream(dstFile);
            // der ursprüngliche Eigner-VC wird unter dem Namen der Source-Datei gespeichert
            replacer.copyAndReplace(srcFile.getName(), inpStream, outStream);
            dstFile.setLastModified(srcFile.lastModified());
            // System.out.println("New-Datei : " + newFile + " mit lastModified-Date: " + TesunDateUtils.DATE_FORMATTER_DD_MM_YYYY_HH_MM_SS.format(oldDate));
        } finally {
            if (inpStream != null) inpStream.close();
            if (outStream != null) outStream.close();
        }
    }

    private void addResultInfo(TestScenario testScenario, String errorStr) {
        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
        testScenario.addResultInfo(TestFallGeneratePseudoCrefos.COMMAND, resultInfo);
    }

    protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyInfo);
        }
    }
}
