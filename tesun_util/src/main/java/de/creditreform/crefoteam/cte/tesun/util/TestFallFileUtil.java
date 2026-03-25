package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacementMapping;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class TestFallFileUtil {
    public static final String FOLDERNAME_SYNTH_TESTCREFOS = "mappingcoverage-test-crefos";

    public static List<Path> listFolderContentAsFilenames(String folderName, String extension) throws URISyntaxException, IOException {
        final List<Path> entries = new ArrayList<>();
        // Mit nio2 können wir eine URI ermitteln, auch wenn der Ordner in einer Datei liegt
        URL resourceURL = TestFallFileUtil.class.getClassLoader().getResource(folderName);
        if (resourceURL == null) {
            throw new RuntimeException("Resource '" + folderName + "' existiert im ClassPath nicht!");
        }
        URI resourceURI = resourceURL.toURI();
        if ("jar".equals(resourceURI.getScheme())) {
            // Daten liegen innerhalb einer JAR-Datei, wir erzeugen temporär ein FileSystem auf dem JAR
            try (FileSystem fileSystem = FileSystems.newFileSystem(resourceURI, Collections.emptyMap(), null)) {
                // innerhalb unseres temporären Dateisystems können wir auf den Pfad des Ordners zugreifen
                Files.list(fileSystem.getPath(folderName)).forEach((p) -> entries.add(p));
                //return collectEntries(fileSystem.getPath(folderName));
            }
        } else {
            // Daten liegen _nicht_ in einer JAR-Datei...
            Files.list(Paths.get(resourceURI)).forEach((p) -> entries.add(p));
            //return collectEntries( Paths.get(resourceURI) );
        }
        return entries.stream().filter(s -> {
            return s.getFileName().toString().endsWith(extension);
        }).collect(Collectors.toList());
    }

    public static List<File> downloadFolderContentFromFolder(String folderName, String extension, File outputDir) throws Exception {
        final List<File> entries = new ArrayList<>();
        List<Path> fileNamesList = listFolderContentAsFilenames(folderName, extension);
        fileNamesList.stream().forEach(fileName -> {
            InputStream inputStream = TestFallFileUtil.class.getResourceAsStream("/bpmns/" + fileName);
            try {
                String xmlContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                File theFile = new File(outputDir, new File(fileName.toString()).getName());
                FileUtils.writeStringToFile(theFile, xmlContent);
                entries.add(theFile);
            } catch (Exception ex) {
                throw new RuntimeException("Fehler beim Laden der Resource '" + fileName, ex);
            }
        });
        return entries;
    }

    public static Map<String, ReplacementMapping> readReplacementMappingFromFile(File mappingsFile) throws IOException {
        if (!mappingsFile.exists()) {
            throw new RuntimeException("Mapping-Datei '" + mappingsFile.getAbsolutePath() + "' existiert nicht!");
        }
        List<String> strLines = FileUtils.readLines(mappingsFile);
        Map<String, ReplacementMapping> replacementMappingMap = new TreeMap<>();
        for (String strLine : strLines) {
            String[] split = strLine.split(";");
            if (split.length == 4) {
                ReplacementMapping replacementMapping = new ReplacementMapping(Long.valueOf(split[0]));
                replacementMapping.setTargetCrefo(Long.valueOf(split[1]));
                replacementMapping.setEignerVC(Integer.valueOf(split[2]));
                replacementMapping.setTagNameEignerVC(split[3]);
                replacementMappingMap.put(split[0], replacementMapping);
            }
        }
        return replacementMappingMap;
    }

    public static Map<String, ReplacementMapping> swapReplacementMapping(Map<String, ReplacementMapping> replacementMappingMap0) {
        Map<String, ReplacementMapping> replacementMappingMap = new TreeMap<>();
        replacementMappingMap0.keySet().forEach(strKey -> {
            ReplacementMapping replacementMapping0 = replacementMappingMap0.get(strKey);

            Long newToBeReplacedCrefo = replacementMapping0.getTargetCrefo();
            Long newTargetCrefo = replacementMapping0.getToBeReplacedCrefo();
            ReplacementMapping replacementMapping = new ReplacementMapping(newToBeReplacedCrefo);
            replacementMapping.setToBeReplacedCrefo(newToBeReplacedCrefo);
            replacementMapping.setTargetCrefo(newTargetCrefo);
            replacementMapping.setTagNameEignerVC(replacementMapping0.getTagNameEignerVC());
            replacementMapping.setEignerVC(replacementMapping0.getEignerVC());
            replacementMappingMap.put(newToBeReplacedCrefo.toString(), replacementMapping);
        });
        return replacementMappingMap;
    }

    public static List<Long> readCrefosFromResourceFile(final File sourceFile) throws IOException {
        List<Long> crefosList = new ArrayList<>();
        List<String> strCrefos = FileUtils.readLines(sourceFile);
        for (String strCrefo : strCrefos) {
            try {
                crefosList.add(Long.valueOf(strCrefo));
            } catch (NumberFormatException ex) {
                // ok
            }
        }
        return crefosList;
    }

    public static StringBuilder dumAllCustomers(Map<String, TestCustomer> testCustomerMap) {
        StringBuilder stringBuilderAll = new StringBuilder();
        testCustomerMap.entrySet().forEach(testCustomerEntry -> {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            testCustomer.dumpResults(stringBuilder, "\n");
            if (stringBuilder.length() > 0) {
                stringBuilderAll.append(stringBuilder);
            }
        });
        return stringBuilderAll;
    }
}