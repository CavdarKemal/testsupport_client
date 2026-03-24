package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExportsCollector {

    private static final Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}");

    protected String collectDirPath;
    protected final EnvironmentConfig environmentConfig;
    protected final ExportsAdapterFactory exportsAdapterFactory;
    protected final TesunClientJobListener tesunClientJobListener;

    public ExportsCollector(EnvironmentConfig environmentConfig, ExportsAdapterFactory exportsAdapterFactory, TesunClientJobListener tesunClientJobListener) throws Exception {
        this.environmentConfig = environmentConfig;
        this.tesunClientJobListener = tesunClientJobListener;
        this.exportsAdapterFactory = exportsAdapterFactory;
        File collectFile = environmentConfig.getCollectsRoot();
        collectDirPath = collectFile.getAbsolutePath();
        if (!collectFile.exists()) {
            String strErr = String.format("Das Verzeichnis %s existiert nicht!", collectDirPath);
            notifyTesunClientJobListener(Level.ERROR, strErr);
            throw new TestSupportConfigurationException("\n" + strErr);
        }
    }

    public void collectExportsForCustomer(TestCustomer testCustomer) {
        String strInfo = String.format("\nSammle die Exporte für den Kunden '%s'...", testCustomer);
        notifyTesunClientJobListener(Level.INFO, "\n" + strInfo);
        ExportsAdapter exportsAdapter = exportsAdapterFactory.findExportsAdapter(testCustomer.getCustomerKey());
        if (exportsAdapter == null) {
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo("Kein passender ExportsAdapter für den Kunden '" + testCustomer.getCustomerKey() + "' gefunden!");
            testCustomer.addResultInfo("", resultInfo);
        }
        // lese die ZIP-Dateien aus dem jüngsten Export des Kunden, die den CLZ's der Test-Cefos des Kunden passen
        PathElement joungestPathElement = exportsAdapter.findRelatedPathElement();
        if (joungestPathElement == null) {
            return;
        }
        tesunClientJobListener.notifyClientJob(Level.INFO, "\n\tDas Jüngste Export-Verzeichnis nach '" + TesunDateUtils.formatCalendar(testCustomer.getLastJobStartetAt()) + "' ist: " + joungestPathElement.getSymbolicPath());
        List<PathElement> pathElementList = exportsAdapter.listZipPathElements(joungestPathElement, testCustomer.getAllTestCrefos(true, false));
        Map<String, TestScenario> testScenariosMap = testCustomer.getTestScenariosMap();
        for (Map.Entry<String, TestScenario> testScenarioEntry : testScenariosMap.entrySet()) {
            TestScenario testScenario = testScenarioEntry.getValue();
            if (testScenario.isActivated()) {
                try {
                    collectExportsForScenario(testScenario, exportsAdapter, pathElementList);
                } catch (TimeoutException ex) {
                    break;
                } catch (Exception ex) {
                    String errorStr = "  Exception beim Collect für den Kunden '" + testCustomer.getCustomerKey() + ":" + testScenario.getScenarioName() + "'!\n" + ex.getMessage();
                    TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                    testScenario.addResultInfo(TestFallCollectExportedCrefos.COMMAND, resultInfo);
                    notifyTesunClientJobListener(Level.INFO, "\n\t" + errorStr);
                }
            }
        }
    }

    private void collectExportsForScenario(TestScenario testScenario, ExportsAdapter exportsAdapter, List<PathElement> pathElementList) throws Exception {
        String strInfo = String.format("\n\tSammle die Exporte für das Test-Scenario '%s:%s'...", testScenario.getTestCustomer().getCustomerKey(), testScenario.getScenarioName());
        notifyTesunClientJobListener(Level.INFO, strInfo);
        // Die Verzeichnis-Struktur für COLLECT schon mal anlegen...
        exportsAdapter.createCollectDirStruct(testScenario.getCollectedsFile());
        // die *.properties Datei auch noch kopieren...
        exportsAdapter.copyPropsFile(testScenario);
        Map<String, List<File>> symbolicLinkToFilesListMap = exportsAdapter.extractAndSaveExportsForTestScenario(testScenario, pathElementList);
        if (!symbolicLinkToFilesListMap.isEmpty()) {
            appendToProkollFile(testScenario, symbolicLinkToFilesListMap);
        } else {
            strInfo = String.format("    Für das Test-Scenario '%s:%s' wurden KEINE Exports gefunden!", testScenario.getTestCustomer().getCustomerKey(), testScenario.getScenarioName());
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(strInfo);
            testScenario.addResultInfo(TestFallCollectExportedCrefos.COMMAND, resultInfo);
            notifyTesunClientJobListener(Level.INFO, "\n\t" + strInfo);
        }
    }

    protected void appendToProkollFile(TestScenario testScenario, Map<String, List<File>> symbolicLinkToFilesListMap) throws Exception {
        Iterator<String> keysIterator = symbolicLinkToFilesListMap.keySet().iterator();
        while (keysIterator.hasNext()) {
            String symbolicPath = keysIterator.next();
            Matcher matcher = datePattern.matcher(symbolicPath);
            if (!matcher.find()) {
                return;
            }
            File testFallFile = testScenario.getCollectedsFile().getParentFile();
            testFallFile = new File(testFallFile, testScenario.getCusomerKey().toUpperCase() + TestSupportClientKonstanten.ADDITIONAL_INFO_FILENAME_POSTFIX);
            Map<String, Tupel> additionalProkollInfosMap = getAdditionalProkollInfosMap(testFallFile);
            String strDate = matcher.group(0).replaceAll("-", ".");
            //'delta_YYYY.MM.DD_HH.MM.exportprotokoll.txt'
            final File protokollFile = new File(testScenario.getCollectedsFile(), String.format("delta_%s.exportprotokoll.txt", strDate));
            List<File> exportXmlFiles = symbolicLinkToFilesListMap.get(symbolicPath);
            for (File xmlFile : exportXmlFiles) {
                final String[] split = xmlFile.getName().split("-");
                if (split.length < 2) {
                    continue;
                }
                if (split[1].contains("ProduktAuftrag")) {
                    continue;
                }
                matcher = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(split[1]);
                if (matcher.find()) {
                    String strCrefo = matcher.group(0);
                    String testName = split[0];
                    String strData = String.format("%s\t%s", testName, strCrefo);
                    final Tupel tupel = additionalProkollInfosMap.get(testName);
                    if (tupel != null) {
                        strData += "\t" + tupel.getElement1();
                        strData += "\t" + tupel.getElement2();
                    } else {
                        strData += " !!! Für den Testfall '";
                        strData += testName;
                        strData += "' wurde keine gültige Beschreibung in der Datei '";
                        strData += TesunUtilites.shortPath(testFallFile, 60);
                        strData += "' angegeben!!!";
                    }
                    strData += "\n";
                    FileUtils.writeStringToFile(protokollFile, strData, true);
                }
            }
        }
    }

    protected Map<String, Tupel> getAdditionalProkollInfosMap(File testFaelleFile) throws IOException {
        Map<String, Tupel> additionalProkollInfosMap = new HashMap<>();
        if (testFaelleFile.exists()) {
            final List<String> stringList = FileUtils.readLines(testFaelleFile, "UTF-8");
            for (String strLine : stringList) {
                final String[] split = strLine.split("\t");
                if (split.length == 3) {
                    final String key = split[0];
                    if (key.startsWith("n") || key.startsWith("p") || key.startsWith("x")) {
                        additionalProkollInfosMap.put(key, new Tupel(split[1], split[2]));
                    }
                }
            }
        }
        return additionalProkollInfosMap;
    }

    protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyInfo);
        }
    }
}
