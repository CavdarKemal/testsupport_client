package de.creditreform.crefoteam.cte.tesun.restore;

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
import java.util.Map;
import java.util.concurrent.Callable;

public class CustomerOriginGenerator implements Callable<TestResults> {
    private final TestCustomer testCustomer;
    private final Replacer replacer;
    private final TesunClientJobListener tesunClientJobListener;

    public CustomerOriginGenerator(TestCustomer testCustomer, Replacer replacer, TesunClientJobListener tesunClientJobListener) {
        this.testCustomer = testCustomer;
        this.replacer = replacer;
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
                    String errorStr = "Exception beim Ersetzen der Test-Crtefos für den Kunden '" + testCustomer.getCustomerKey() + ":" + testScenario.getScenarioName() + "'!\n" + ex.getMessage();
                    TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                    testScenario.addResultInfo(TestFallRestoreCrefos.COMMAND, resultInfo);
                }
            }
        });
        return testCustomer.getTestResultsForCommand(TestFallRestoreCrefos.COMMAND);
    }

    private void replaceScenarioFiles(TestScenario testScenario) throws Exception {
        // Properties-File...
        File collectedsPropertiesFile = testScenario.getCollectedsPropsFile();
        File restoredCollectedsPropertiesFile = testScenario.getRestoredCollectsPropsFile();
        replaceFileContent(collectedsPropertiesFile, restoredCollectedsPropertiesFile);
        // Test-Crefos...
        testScenario.getTestCrefosAsList().forEach(testCrefo -> {
            try {
                File collectedXmlFile = testCrefo.getCollectedXmlFile();
                if(collectedXmlFile != null && !collectedXmlFile.getName().startsWith("XML")) { // Snippets?
                    String newRestoredXmlFilePath = collectedXmlFile.getParentFile().getAbsolutePath().replace(TestSupportClientKonstanten.COLLECTED, TestSupportClientKonstanten.RESTORED_COLLECTS);
                    String newRestoredXmlFileName = replacer.replace(null, collectedXmlFile.getName()).toString();
                    File newRestoredXmlFile = new File(newRestoredXmlFilePath, newRestoredXmlFileName);
                        replaceFileContent(collectedXmlFile, newRestoredXmlFile);
                        testCrefo.setRestoredXmlFile(newRestoredXmlFile);
                }
            } catch (IOException ex) {
                TestResults.ResultInfo resultInfo = new TestResults.ResultInfo("Exception beim Restoren der TestCrefo " + testCrefo.getTestFallName() + " " + testCrefo.getCollectedXmlFile().getName() + "\n" + ex.getMessage());
                testScenario.addResultInfo(TestFallRestoreCrefos.COMMAND, resultInfo);
            }
        });
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
        } finally {
            if (inpStream != null) inpStream.close();
            if (outStream != null) outStream.close();
        }
    }

    protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyInfo);
        }
    }
}
