package de.creditreform.crefoteam.cte.tesun.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class TestResultsZipHandler {
    private final CustomerTestResultsParser customerTestResultsParser;

    public TestResultsZipHandler() {
        this.customerTestResultsParser = new CustomerTestResultsParser();
    }

    public static void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        if (!Files.exists(zipFile.getParent())) {
            Files.createDirectory(zipFile.getParent());
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            Path pathEntry = Path.of(sourceDir.getFileName().toString(), sourceDir.relativize(path).toString());// Relativer Pfad in der Zip-Datei.
                            ZipEntry zipEntry = new ZipEntry(pathEntry.toString());
                            zipOutputStream.putNextEntry(zipEntry);
                            Files.copy(path, zipOutputStream);
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Fehler beim Zippen von " + path + ": " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    public void writeTestResultsToFile(Map<String, TestCustomer> testCustomersMap) {
        testCustomersMap.entrySet().forEach(testCustomerEntry -> {
            try {
                TestCustomer testCustomer = testCustomerEntry.getValue();
                testCustomer.dumpResultsToFile();
            } catch (Exception e) {
                throw new RuntimeException("\n!!! Fehler beim Speichern der Test-Results-Datei!n" + e.getMessage());
            }
        });
    }

    public Path unzipRecursive(Path testResultsZipFile) throws IOException {
        Path otuputPath = testResultsZipFile.resolveSibling(testResultsZipFile.getFileName() + "-UNZIPPED");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(testResultsZipFile.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Pfad auflösen und normalisieren
                Path resolvedPath = otuputPath.resolve(entry.getName()).normalize();
                // Zip-Slip Schutz: Prüfen, ob der Pfad noch im Zielverzeichnis liegt
                if (!resolvedPath.startsWith(otuputPath)) {
                    throw new IOException("Entry is outside of target dir: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zis, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
        return otuputPath;
    }

    public Map<String, TestCustomer> initalizeTestCustomersMapFromDir(Path unzippedPath, String subDirName) throws Exception {
        Map<String, TestCustomer> testCustomerMap = null;
        Path resultsFilePath = unzippedPath.resolve(TestSupportClientKonstanten.CHECKED).resolve(subDirName).resolve("TestResults.txt");
        if (resultsFilePath.toFile().exists()) {
            testCustomerMap = customerTestResultsParser.parseTestResultsFile(resultsFilePath.toFile());
        }
        return testCustomerMap;
    }

}
