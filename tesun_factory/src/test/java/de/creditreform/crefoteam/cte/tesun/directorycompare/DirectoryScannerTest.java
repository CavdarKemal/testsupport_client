package de.creditreform.crefoteam.cte.tesun.directorycompare;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Test-Klasse für {@link DirectoryScanner}
 * User: ralf
 * Date: 11.06.14
 * Time: 13:44
 */
public class DirectoryScannerTest {
  private final String pathPrefix = "./src/test/resources/";
  
    @Test
    public void testScanDirectory() {
        final String pathname = "DirectoryCompareTest/empty/";
        Map<String, DirectoryScanResult> resEmpty = getFileMap(pathname);
        Assert.assertNotNull(resEmpty);
        Assert.assertTrue(resEmpty.isEmpty());

        Map<String, DirectoryScanResult> resDRD11 = getFileMap("DirectoryCompareTest/drd_export_1/");
        checkExpectedKeys(resDRD11,
                          "abCrefo203.2000000_createAndUpdate.zip",
                          "completioninfo.txt");
        checkExpectedFiles(resDRD11,
                           pathPrefix + "DirectoryCompareTest/drd_export_1/20130618_0853_abCrefo203.2000000_createAndUpdate.zip",
                           pathPrefix + "DirectoryCompareTest/drd_export_1/completioninfo.txt");

        Map<String, DirectoryScanResult> resVSH1 = getFileMap("DirectoryCompareTest/vsh_export_1/");
        checkExpectedKeys(resVSH1,
                          "203_CLZ_203_ab_0600000.zip",
                          "203_CLZ_203_ab_2000000.zip",
                          "207_CLZ_207_ab_2000000.zip",
                          "207_CLZ_207_firstOnly.zip",
                          "completioninfo.txt");
        checkExpectedFiles(resVSH1,
                           pathPrefix + "DirectoryCompareTest/vsh_export_1/203/CLZ_203_ab_0600000.zip",
                           pathPrefix + "DirectoryCompareTest/vsh_export_1/203/CLZ_203_ab_2000000.zip",
                           pathPrefix + "DirectoryCompareTest/vsh_export_1/207/CLZ_207_ab_2000000.zip",
                           pathPrefix + "DirectoryCompareTest/vsh_export_1/207/CLZ_207_firstOnly.zip",
                           pathPrefix + "DirectoryCompareTest/vsh_export_1/completioninfo.txt");
    }

    private Map<String, DirectoryScanResult> getFileMap(String pathName) {
        Map<String, DirectoryScanResult> res;
        File dir = new File(pathPrefix + pathName);
        Assert.assertTrue("getestetes Verzeichnis existiert nicht: " + pathName, dir.exists());
        DirectoryScanner cut = new DirectoryScanner();
        res = cut.scanDirectory(dir);
        Assert.assertNotNull("Ergebnis des Scan-laufs sollte nie NULL sein", res);
        return res;
    }

    private void checkExpectedKeys(Map<String, DirectoryScanResult> resMap, String... expectedKeys) {
        List<String> expected = Arrays.asList(expectedKeys);
        Assert.assertEquals("Anzahl gefundener Dateien nicht korrekt", expected.size(), resMap.size());
        Iterator<String> expectedIter = expected.iterator();
        for (String key : resMap.keySet()) {
            Assert.assertEquals("Treffer an dieser Position entspricht nicht dem erwarteten Ergebnis", expectedIter.next(), key);
        }
    }

    private void checkExpectedFiles(Map<String, DirectoryScanResult> resMap, String... expectedFileNames) {
        List<String> expected = Arrays.asList(expectedFileNames);
        Assert.assertEquals("Anzahl gefundener Dateien nicht korrekt", expected.size(), resMap.size());
        Iterator<String> expectedIter = expected.iterator();
        for (DirectoryScanResult dsr : resMap.values()) {
            File expectedFile = new File(expectedIter.next());
            Assert.assertTrue("Bezeichnung einer erwarteten Datei sollte einer physikalischen Datei entsprechen: \n-->"+expectedFile.getPath(),
                              expectedFile.exists());
            Assert.assertEquals("Treffer an dieser Position entspricht nicht dem erwarteten Ergebnis", expectedFile, dsr.getFile());
        }
    }
}
