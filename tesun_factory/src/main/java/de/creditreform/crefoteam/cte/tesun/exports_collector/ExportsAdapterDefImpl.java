package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.PathElementProcessorFactory;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.PathElementUtils;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestResults;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.naming.InsufficientResourcesException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;

public class ExportsAdapterDefImpl implements ExportsAdapter {

    protected final ExportsAdapterConfig exportsAdapterConfig;
    protected final TesunClientJobListener tesunClientJobListener;
    protected final PathElementProcessorFactory pathElementProcessorFactory;

    @Override
    public TestCustomer getTestCustomer() {
        return testCustomer;
    }

    protected final TestCustomer testCustomer;
    List<File> testCustomerZipFilesList = new ArrayList<>();

    public ExportsAdapterDefImpl(ExportsAdapterConfig exportsAdapterConfig, TestCustomer testCustomer, TesunClientJobListener tesunClientJobListener) throws InsufficientResourcesException {
        this.exportsAdapterConfig = exportsAdapterConfig;
        this.testCustomer = testCustomer;
        this.tesunClientJobListener = tesunClientJobListener;
        this.pathElementProcessorFactory = new PathElementProcessorFactory(exportsAdapterConfig.getSftpCfgMap());
    }

    @Override
    public String getCustomerKey() {
        return exportsAdapterConfig.getCustomerKey();
    }

    @Override
    public boolean isTestScenarioActive(String scenarioName) {
        List<TestScenario> testScenariosList = testCustomer.getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            if (scenarioName.endsWith(testScenario.getScenarioName())) {
                return testScenario.isActivated();
            }
        }
        return false;
    }


    @Override
    public PathElement findRelatedPathElement() {
        try {
            tesunClientJobListener.notifyClientJob(Level.INFO, "\n\tDurchsuche NFS-Sever '" + exportsAdapterConfig.getExportsHost() + "' nach Exports ab '" + TesunDateUtils.formatCalendar(testCustomer.getLastJobStartetAt()) + "'...");
            PathElementProcessor processorForExportDirs = pathElementProcessorFactory.create(exportsAdapterConfig.getExportsPath());
            List<PathElement> pathElementsList = processorForExportDirs.listFiles(getExportDirsPathElementFilter());
            if (!pathElementsList.isEmpty()) {
                PathElement joungestPathElement = PathElementUtils.findFirstJoungerElement(tesunClientJobListener, pathElementsList, testCustomer.getLastJobStartetAt());
                return joungestPathElement;
            }
        } catch (Exception ex) {
            notifyTesunClientJobListener(Level.INFO, "\n\t\tFehler beim Ermitteln des jüngsten PathElements!\n\t" + ex.getMessage());
        }
        return null;
    }

    @Override
    public List<PathElement> listZipPathElements(PathElement joungestPathElement, List<TestCrefo> testCrefosList) {
        PathElementProcessor pathElementProcessor = pathElementProcessorFactory.create(joungestPathElement);
        List<PathElement> pathElementsList = new ArrayList<>();
        boolean timedOut = checkIfCompletitionInfoExists(joungestPathElement, pathElementProcessor);
        if (!timedOut) {
            pathElementsList = listZipFiles(pathElementProcessor, testCrefosList);
            notifyTesunClientJobListener(Level.INFO, String.format("\n\t\tAnzahl der ZIP-Dateien aus dem Verzeichnis %s ist %d", joungestPathElement.getName(), pathElementsList.size()));
        } else {
            String strInfo = String.format("\n\t\tTime-Out beim warten auf die Datei %s", joungestPathElement.getName());
            notifyTesunClientJobListener(Level.ERROR, strInfo);
        }
        pathElementProcessorFactory.close();
        return pathElementsList;
    }

    @Override
    public Map<String, List<File>> extractAndSaveExportsForTestScenario(TestScenario testScenario, List<PathElement> pathElementList) throws Exception {
        String strInfo = String.format("\n\tFür das Test-Scenario '%s:%s' wurden %d ZIP-Path-Elemente gefunden...", testCustomer.getCustomerKey(), testScenario.getScenarioName(), pathElementList.size());
        notifyTesunClientJobListener(Level.INFO, strInfo);
        Map<String, List<File>> symbolicLinkToFilesListMap = new TreeMap<>();
        for (PathElement zipPathElement : pathElementList) {
            String symbolicPath = zipPathElement.getSymbolicPath();
            strInfo = String.format("\n\t\tExtrahiere passende Crefos für das Test-Scenario '%s:%s' aus der ZIP-Datei\t'%s'...", testCustomer.getCustomerKey(), testScenario.getScenarioName(), zipPathElement.getSymbolicPath());
            notifyTesunClientJobListener(Level.INFO, strInfo);
            File zipFile = retrieveZipFileForZipPathElement(zipPathElement);
            final List<File> fileList = extractAndSaveZipInputStream(testScenario, zipFile);
            symbolicLinkToFilesListMap.put(symbolicPath, fileList);
        }
        return symbolicLinkToFilesListMap;
    }

    protected List<File> extractAndSaveZipInputStream(TestScenario testScenario, File theFile) throws Exception {
        List<File> xmlFiles = new ArrayList<>();
        // ermittle alle ZIP-Entries aus dem Stream
        List<String> zipEntryNamesList = listZipEntryNamesFromZipFile(theFile);
        ZipFile zipFile = new ZipFile(theFile);
        String infoStr;
        List<TestCrefo> testCrefosAsList = testScenario.getTestCrefosAsList();
        List<CollectInfo> collectInfoList = getCollectInfoForTestCrefosList(testCrefosAsList, zipEntryNamesList);
        for (CollectInfo collectInfo : collectInfoList) {
            if (collectInfo.isStatusOK()) {
                String zipEntryName = collectInfo.getZipEntryName();
                TestCrefo testCrefo = collectInfo.getTestCrefo();
                if (zipEntryName != null && testCrefo != null) {
                    ZipEntry zipEntry = zipFile.getEntry(zipEntryName);
                    InputStream zipFileInputStream = zipFile.getInputStream(zipEntry);
                    File xmlFile = new File(testScenario.getCollectedsFile(), testCrefo.getTestFallName() + "-" + zipEntryName);
                    File savedXmlFile = saveByteArrayOutputStream(zipFileInputStream, xmlFile);
                    if (savedXmlFile != null) {
                        testCrefo.setCollectedXmlFile(savedXmlFile);
                        testCrefo.setExported(true);
                        xmlFiles.add(savedXmlFile);
                        infoStr = String.format("\n\t\t\tXML für die Test-Crefo '%s' wurde extrahiert und in '%s' abgespeichert.", testCrefo, TesunUtilites.shortPath(savedXmlFile, 50));
                        notifyTesunClientJobListener(Level.INFO, infoStr);
                    } else {
                        String errorStr = String.format("!!! Für die Test-Crefo '%s' konnte das XML-Entry '%s' NICHT extrahiert werden!", testCrefo, zipEntryName);
                        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(errorStr);
                        testScenario.addResultInfo(TestFallCollectExportedCrefos.COMMAND, resultInfo);
                        notifyTesunClientJobListener(Level.INFO, "\n\t\t" + errorStr);
                    }
                }
            } else {
                TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(collectInfo.getStrInfo());
                testScenario.addResultInfo(TestFallCollectExportedCrefos.COMMAND, resultInfo);
                notifyTesunClientJobListener(Level.INFO, "\n\t\t" + collectInfo.getStrInfo());
            }
        }
        zipFile.close();
        return xmlFiles;
    }

    private static List<String> listZipEntryNamesFromZipFile(File theFile) throws IOException {
        List<String> zipEntryNamesList = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(theFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                zipEntryNamesList.add(zipEntry.getName());
            }
        }
        zipEntryNamesList.sort(Comparator.naturalOrder());
        return zipEntryNamesList;
    }

    private boolean checkIfCompletitionInfoExists(PathElement joungestPathElement, PathElementProcessor pathElementProcessor) {
        long startMillis = System.currentTimeMillis();
        long timeOutMillis = startMillis + 200 * 60 * 6;
        boolean timedOut = false;
        List<PathElement> pathElementList = new ArrayList<>();
        notifyTesunClientJobListener(Level.INFO, String.format("\n\t\tPrüfe Existenz der Datei 'completioninfo.txt' im Verzeichnis %s", joungestPathElement.getName()));
        while (!timedOut && pathElementList.isEmpty()) {
            pathElementList = pathElementProcessor.listFiles(new RegExpPathElementFilter("completioninfo.txt"));
            if (pathElementList.size() == 1) {
                break;
            }
            try {
                Thread.sleep(500);
                timedOut = System.currentTimeMillis() > timeOutMillis;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return timedOut;
    }

    @Override
    public List<PathElement> listZipFiles(PathElementProcessor processorForZipFiles, List<TestCrefo> testCrefosList) {
        PathElementFilter exportZipsPathElementFilter = getExportZipsPathElementFilter(testCrefosList);
        List<PathElement> pathElementsList = processorForZipFiles.listFiles(exportZipsPathElementFilter);
        return pathElementsList;
    }

    @Override
    public List<String> getZipFilePrefixes() {
        return Arrays.asList("abCrefo", "fromCrefo");
    }

    protected List<CollectInfo> getCollectInfoForTestCrefosList(List<TestCrefo> testCrefoList, List<String> zipEntryNamesList) {
        List<CollectInfo> collectInfosList = new ArrayList<>();
        //String strList = zipEntryNamesList.toString().replaceAll("\\[", "\n\t").replaceAll("\\]", "").replaceAll(",", "\n\t");
        //notifyTesunClientJobListener(Level.INFO, "\nPrüfe ZIP-Entry-List " + strList + "\nfür die Testfälle der TestScenario...");
        String infoStr;
        for (String zipEntryName : zipEntryNamesList) {
            // ermittle TestCrefos, die der Crefonummer des ZIP-Entries entsprechen
            List<TestCrefo> testCrefoListX = testCrefoList.stream().filter(testCrefo -> {
                //System.out.println("Checke TestCrefo '" + testCrefo + "' gegen ZIP-Enty '" + zipEntryName + "'...");
                return zipEntryName.contains(testCrefo.getPseudoCrefoNr().toString());
            }).collect(Collectors.toList());
            if (zipEntryName.startsWith("compan") || zipEntryName.startsWith("stamm") || zipEntryName.startsWith("crefo") || zipEntryName.startsWith("betei") || zipEntryName.startsWith("update")) {
                // Zip-Entry entsprciht einem Stamm- oder Beteiligtenexport. Suche die passende TestCrefo
                for (int index = 0; index < testCrefoListX.size(); index++) {
                    TestCrefo testCrefo = testCrefoListX.get(index);
                    // ist die aktielle TestCrefo passend?
                    if (testCrefo.getTestFallName().startsWith("p")) {
                        testCrefo.setCollectedXmlFile(new File(zipEntryName));
                        infoStr = "Für den P-Testfall '" + testCrefo + "' wurde ein erwartungsgemß der Export '" + zipEntryName + "' gefunden!";
                        collectInfosList.add(new CollectInfo(testCrefo, zipEntryName, true, infoStr));
                        // gefunden, aus der original-Liste entfernen und raus
                        testCrefoList.remove(testCrefo);
                    } else if (testCrefo.getTestFallName().startsWith("x")) {
                        infoStr = "Für den X-Testfall '" + testCrefo + "' wurde unerwarteterweise ein Export '" + zipEntryName + "' gefunden!";
                        collectInfosList.add(new CollectInfo(testCrefo, zipEntryName, false, infoStr));
                    } else if (testCrefo.getTestFallName().startsWith("n")) {
                        infoStr = "Für den N-Testfall '" + testCrefo + "' wurde unerwarteterweise der Export '" + zipEntryName + "' gefunden!";
                        collectInfosList.add(new CollectInfo(testCrefo, zipEntryName, false, infoStr));
                    }
                }
            } else if (zipEntryName.startsWith("loesc") || zipEntryName.startsWith("stopm") || zipEntryName.startsWith("delete")) {
                for (int index = 0; index < testCrefoListX.size(); index++) {
                    TestCrefo testCrefo = testCrefoListX.get(index);
                    // ist die aktielle TestCrefo passend?
                    if (testCrefo.getTestFallName().startsWith("x")) {
                        testCrefo.setCollectedXmlFile(new File(zipEntryName));
                        infoStr = "Für den X-Testfall '" + testCrefo + "' wurde ein erwartungsgemß der Export '" + zipEntryName + "' gefunden!";
                        collectInfosList.add(new CollectInfo(testCrefo, zipEntryName, true, infoStr));
                        // gefunden, aus der original-Liste entfernen und raus
                        testCrefoList.remove(testCrefo);
                    } else if (testCrefo.getTestFallName().startsWith("p")) {
                        infoStr = "Für den P-Testfall '" + testCrefo + "' wurde unerwarteterweise ein Export '" + zipEntryName + "' gefunden!";
                        collectInfosList.add(new CollectInfo(testCrefo, zipEntryName, false, infoStr));
                    } else if (testCrefo.getTestFallName().startsWith("n")) {
                        infoStr = "Für den N-Testfall '" + testCrefo + "' wurde unerwarteterweise der Export '" + zipEntryName + "' gefunden!";
                        collectInfosList.add(new CollectInfo(testCrefo, zipEntryName, false, infoStr));
                    }
                }
            } else {
                for (int index = 0; index < testCrefoListX.size(); index++) {
                    TestCrefo testCrefo = testCrefoListX.get(index);
                    if (zipEntryName.startsWith(testCrefo.getPseudoCrefoNr().toString())) {
                        infoStr = "Für den X-Testfall '" + testCrefo + "' wurde ein erwartungsgemß der Export '" + zipEntryName + "' gefunden!";
                        collectInfosList.add(new CollectInfo(testCrefo, zipEntryName, true, infoStr));
                        // gefunden, aus der original-Liste entfernen und raus
                        testCrefoList.remove(testCrefo);
                    }
                }
            }
        }
        // Prüfe, ob für alle P- und X-Testfälle der igendein Export gefunden wurde...
/*
        testCrefoList.forEach(testCrefo -> {
            if (!testCrefo.getTestFallName().startsWith("n") && testCrefo.getCollectedXmlFile() == null) {
                collectInfosList.add(new CollectInfo(testCrefo, null, false, "Für den Testfall '" + testCrefo + "' wurde kein Export gefunden!"));
            }
        });
*/
        return collectInfosList;
    }

    @Override
    public void createCollectDirStruct(File theFile) throws IOException {
        TesunUtilites.checkAndCreateDirectory(theFile, true);
    }

    @Override
    public PathElementFilter getExportZipsPathElementFilter(List<TestCrefo> testCrefosList) {
        return new ExportZipsPathElementFilter(testCrefosList, getZipFilePrefixes());
    }

    @Override
    public PathElementFilter getExportDirsPathElementFilter() {
        return new ExportDirsPathElementFilter();
    }

    @Override
    public void copyPropsFile(TestScenario testScenario) throws IOException {
        File srcFile = testScenario.getPseudoRefExportsPropsFile();
        if (srcFile.exists()) {
            File targetFile = new File(testScenario.getCollectedsFile(), testScenario.getItsqRefExportsPropsFile().getName());
            FileUtils.copyFile(srcFile, targetFile);
        }
    }

    public ByteArrayOutputStream retrieveFileContent(PathElement pathElement) throws Exception {
        PathElementProcessor processor = pathElementProcessorFactory.create(pathElement);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(100 * 1024);
        processor.readFile(baos);
        baos.close();
        pathElementProcessorFactory.close();
        return baos;
    }

    public File retrieveZipFileForZipPathElement(PathElement zipPathElement) throws Exception {
        File outputZipFile = null;
        List<File> fileList = testCustomerZipFilesList.stream().filter(file -> file.getName().equals(zipPathElement.getName())).collect(Collectors.toList());
        if (fileList.size() < 1) {
            outputZipFile = new File(testCustomer.getCollectedsDir(), zipPathElement.getName());
            ByteArrayOutputStream byteArrayOutputStream = retrieveFileContent(zipPathElement);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            FileUtils.copyInputStreamToFile(byteArrayInputStream, outputZipFile);
            testCustomerZipFilesList.add(outputZipFile);
        } else {
            outputZipFile = fileList.get(0);
        }
        return outputZipFile;
    }

    public File saveByteArrayOutputStream(InputStream inputStream, File xmlFile) throws Exception {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, Charset.forName("UTF-8"));
        String theString = writer.toString();
        if (!theString.isEmpty()) {
            final String formattedXMLContent = TesunUtilites.toPrettyString(theString, 2);
            FileUtils.writeStringToFile(xmlFile, formattedXMLContent, Charset.forName("UTF-8"));
            return xmlFile;
        } else {
            return null;
        }
    }

    protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyInfo);
        }
    }

    private static class NoCloseInputStream extends FilterInputStream {
        public NoCloseInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            // intentionally ignored
        }
    }

}
