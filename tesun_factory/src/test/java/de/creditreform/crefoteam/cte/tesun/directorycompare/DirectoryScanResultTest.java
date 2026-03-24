package de.creditreform.crefoteam.cte.tesun.directorycompare;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test-Klasse für {@link DirectoryScanResult}
 * User: ralf
 * Date: 03.07.14
 * Time: 13:05
 */
public class DirectoryScanResultTest {

    private void assertEquals(String anwendungsFall, String parentDir, String first, String second) {
        DirectoryScanResult r1 = new DirectoryScanResult(new File(parentDir), new File(first));
        DirectoryScanResult r2 = new DirectoryScanResult(new File(parentDir), new File(second));
        Assert.assertEquals("Nicht gleicher Pfad im Anwendungsfall "+anwendungsFall, r1.getComparableFileNameSection(), r2.getComparableFileNameSection());
        Assert.assertEquals("trotz identischer Strings liefert der Comparator nicht 0", 0, r1.compareTo(r2));
    }

    private void assertNotEquals(String anwendungsFall, String parentDir, String first, String second) {
        DirectoryScanResult r1 = new DirectoryScanResult(new File(parentDir), new File(first));
        DirectoryScanResult r2 = new DirectoryScanResult(new File(parentDir), new File(second));
        Assert.assertNotEquals("Pfade sollten im Anwendungsfall "+anwendungsFall+" verschieden sein", r1.getComparableFileNameSection(), r2.getComparableFileNameSection());
        Assert.assertNotEquals("trotz verschiedener Strings liefert der Comparator 0", 0, r1.compareTo(r2));
    }

    @Test
    public void testCompareFilenames() {
        for (ZipNameForExport z : ZipNameForExport.MATCHING_ZIPS) {
            assertEquals(z.getAnwendungsFall(), z.getPfadParent(), z.getNameFirst(), z.getNameSecond());
        }
        for (ZipNameForExport z : ZipNameForExport.NON_MATCHING_ZIPS) {
            assertNotEquals(z.getAnwendungsFall(), z.getPfadParent(), z.getNameFirst(), z.getNameSecond());
        }
    }

    @Test
    public void testPathPortionPresent() {
        File f = new File("./src/test/resources/DirectoryCompareTest/DirectoryCompareIntegrationTest.config.properties");
        File parent1 = new File("./src/test/resources/");
        DirectoryScanResult cut1 = new DirectoryScanResult(parent1, f);
        Assert.assertEquals("DirectoryCompareTest_", cut1.getDirectoryBasedNamePrefix());
        Assert.assertEquals("DirectoryCompareTest_DirectoryCompareIntegrationTest.config.properties", cut1.getIdentifier());

        File parent2 = new File("./src/test/resources");
        DirectoryScanResult cut2 = new DirectoryScanResult(parent2, f);
        Assert.assertEquals("DirectoryCompareTest_", cut2.getDirectoryBasedNamePrefix());
        Assert.assertEquals("DirectoryCompareTest_DirectoryCompareIntegrationTest.config.properties", cut2.getIdentifier());

        File parent3 = new File("./src/test/");
        DirectoryScanResult cut3 = new DirectoryScanResult(parent3, f);
        Assert.assertEquals("resources_DirectoryCompareTest_", cut3.getDirectoryBasedNamePrefix());
        Assert.assertEquals("resources_DirectoryCompareTest_DirectoryCompareIntegrationTest.config.properties", cut3.getIdentifier());

    }

    @Test
    public void testPathPortionEmpty() {
        File f = new File("./src/test/resources/DirectoryCompareTest/DirectoryCompareIntegrationTest.config.properties");
        File parent1 = new File("./src/test/resources/DirectoryCompareTest");
        DirectoryScanResult cut1 = new DirectoryScanResult(parent1, f);
        Assert.assertEquals("", cut1.getDirectoryBasedNamePrefix());
        Assert.assertEquals("DirectoryCompareIntegrationTest.config.properties", cut1.getIdentifier());

        File parent2 = new File("./src/test/resources/DirectoryCompareTest/");
        DirectoryScanResult cut2 = new DirectoryScanResult(parent2, f);
        Assert.assertEquals("", cut2.getDirectoryBasedNamePrefix());
        Assert.assertEquals("DirectoryCompareIntegrationTest.config.properties", cut2.getIdentifier());

    }
}
