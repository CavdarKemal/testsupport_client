package de.creditreform.crefoteam.cte.tesun.exports_checker;

import de.creditreform.crefoteam.cte.tesun.TestCustomerPredidacte;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExportContentsComparator {
    private static XmlDiffFormatter diffFormatter = new XmlDiffFormatter();
    private final Logger logger = LoggerFactory.getLogger(ExportContentsComparator.class);
    private final List<String> ignorableXPaths;
    private final TesunClientJobListener tesunClientJobListener;

    public ExportContentsComparator(List<String> ignorableXPaths, TesunClientJobListener tesunClientJobListener) {
        this.ignorableXPaths = ignorableXPaths;
        this.tesunClientJobListener = tesunClientJobListener;
    }

    public void compareFileContents(TestCustomer testCustomer) {
        TestCustomerPredidacte customerPredicate = new TestCustomerPredidacte(true);
        if (customerPredicate.apply(testCustomer)) {
            Iterator<String> scenariosIterator = testCustomer.getTestScenariosMap().keySet().iterator();
            while (scenariosIterator.hasNext()) {
                TestScenario testScenario = testCustomer.getTestScenariosMap().get(scenariosIterator.next());
                logger.info("\tVergleich für Test-Scenario {}...", testScenario.getScenarioName());
                Map<String, TestCrefo> testCrefosMap = testScenario.getTestFallNameToTestCrefoMap();
                testCrefosMap.entrySet().forEach(testCrefoEntry -> {
                    TestCrefo testCrefo = testCrefoEntry.getValue();
                    File restoredXmlFile = testCrefo.getRestoredXmlFile();
                    File itsqRexExportXmlFile = testCrefo.getItsqRexExportXmlFile();
                    if (restoredXmlFile == null && itsqRexExportXmlFile == null) {
                        // beide sind null
                    }
                    else if (restoredXmlFile == null && itsqRexExportXmlFile != null) {
                        String strErr = String.format("Für die Test-Crefo '%s' wurde ein Ref-Export angegeben: '%s', aber kein Export gefunden!",
                                testCrefo, itsqRexExportXmlFile.getName());
                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(strErr);
                        testScenario.addResultInfo(TestFallCheckRefExports.COMMAND, resultInfo);
                        notifyTesunClientJobListener(Level.INFO, "\n" + strErr);
                    } else if (restoredXmlFile != null && itsqRexExportXmlFile == null) {
                        String strErr = String.format("Für die Test-Crefo '%s' wurde ein Export gefunden: '%s', aber kein REF-Export angegeben!",
                                testCrefo, restoredXmlFile.getName());
                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(strErr);
                        testScenario.addResultInfo(TestFallCheckRefExports.COMMAND, resultInfo);
                        notifyTesunClientJobListener(Level.INFO, "\n" + strErr);
                    } else if (restoredXmlFile.getName().compareTo(itsqRexExportXmlFile.getName()) != 0) {
                        // für die Crefo wurde unpassender Export produziert!
                        String strErr = String.format("Für die Test-Crefo '%s' wurde unpassender Export produziert: Erwarted: '%s', Exportiert: '%s'",
                                testCrefo, itsqRexExportXmlFile.getName(), restoredXmlFile.getName());
                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(strErr);
                        testScenario.addResultInfo(TestFallCheckRefExports.COMMAND, resultInfo);
                        notifyTesunClientJobListener(Level.INFO, "\n" + strErr);
                    } else {
                        compareFileContentsForScenario(testScenario, testCrefo, restoredXmlFile, itsqRexExportXmlFile);
                    }
                });
            }
        }
    }

    private void compareFileContentsForScenario(TestScenario testScenario, TestCrefo testCrefo, File restoredXmlFile, File itsqRexExportXmlFile) {
        try {
            TestResults.DiffenrenceInfo diffenrenceInfo = compareFileContents(testCrefo, restoredXmlFile, itsqRexExportXmlFile, testScenario.getCheckedsFile(), ignorableXPaths);
            if (diffenrenceInfo != null) {
                String errorStr = String.format("Unterschiede beim Testfall '%s'! Diff-Datei: '%s'", testCrefo, diffenrenceInfo.getDiffFile().getAbsolutePath() );
                TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                resultInfo.addDifferences(diffenrenceInfo);
                testScenario.addResultInfo(TestFallCheckRefExports.COMMAND, resultInfo);
                notifyTesunClientJobListener(Level.INFO, "\n" + errorStr);
            }
        } catch (SAXException ex) {
            String errorStr = String.format("SAXException beim Vergleich der Crefo-XML '%s' mit '%s': %s!", itsqRexExportXmlFile, restoredXmlFile, ex.getMessage());
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(String.format("Exception '%s'", errorStr));
            testScenario.addResultInfo(TestFallCheckRefExports.COMMAND, resultInfo);
            notifyTesunClientJobListener(Level.INFO, errorStr);
        } catch (IOException ex) {
            String errorStr = String.format("IOException beim Vergleich der Crefo-XML '%s' mit '%s': %s!", itsqRexExportXmlFile, restoredXmlFile, ex.getMessage());
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(String.format("Fehlende REF-Export-Datei '%s'", errorStr));
            testScenario.addResultInfo(TestFallCheckRefExports.COMMAND, resultInfo);
            notifyTesunClientJobListener(Level.INFO, "\n" + errorStr);
        } catch (Exception ex) {
            String errorStr = String.format("Exception beim Vergleich der Crefo-XML '%s' mit '%s': %s!", itsqRexExportXmlFile, restoredXmlFile, ex.getMessage());
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(String.format("Fehlende REF-Export-Datei '%s'", errorStr));
            testScenario.addResultInfo(TestFallCheckRefExports.COMMAND, resultInfo);
            notifyTesunClientJobListener(Level.INFO, "\n" + errorStr);
        }
    }

    protected TestResults.DiffenrenceInfo compareFileContents(TestCrefo testCrefo, File restoredXmlFile, File itsqRefExportXmlFile, File checkedsFile, final List<String> ignorableXPaths) throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        String expectedContent = FileUtils.readFileToString(restoredXmlFile);
        String actualContent = FileUtils.readFileToString(itsqRefExportXmlFile);
        TextFileComparator textFileComparator = new TextFileComparator();
        List<Difference> allDifferences = textFileComparator.compareContent(itsqRefExportXmlFile.getName(), expectedContent, actualContent, ignorableXPaths);
        if (!allDifferences.isEmpty()) {
            File newRefXmlFile = new File(new File(checkedsFile, TestSupportClientKonstanten.REF_EXPORTS), itsqRefExportXmlFile.getName());
            FileUtils.copyFile(itsqRefExportXmlFile, newRefXmlFile);
            File newRstoredCollectXmlFile = new File(new File(checkedsFile, TestSupportClientKonstanten.RESTORED_COLLECTS), restoredXmlFile.getName());
            FileUtils.copyFile(restoredXmlFile, newRstoredCollectXmlFile);
            File diffFile = new File(newRstoredCollectXmlFile.getParentFile().getParentFile(), testCrefo.getTestFallName() + "-diff.xml");
            StringBuilder tmpBuilder = new StringBuilder();
            diffFormatter.appendDifferences(tmpBuilder, "\n\t", allDifferences);
            writeToFile(diffFile, tmpBuilder.toString());
            TestResults.DiffenrenceInfo diffenrenceInfo = new TestResults.DiffenrenceInfo(testCrefo.getTestFallName(), newRefXmlFile, newRstoredCollectXmlFile, diffFile, allDifferences);
            return diffenrenceInfo;
        }
        return null;
    }

    protected void writeToFile(File msgListFile, String strContent) throws IOException {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(msgListFile, false), StandardCharsets.UTF_8));
            bufferedWriter.write(strContent);
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }

    protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyInfo);
        }
    }
}
