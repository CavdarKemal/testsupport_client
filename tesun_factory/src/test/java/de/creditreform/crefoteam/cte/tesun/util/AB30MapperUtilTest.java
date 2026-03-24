package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.tesun.TestFallTestBase;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class AB30MapperUtilTest extends TestFallTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AB30MapperUtilTest.class);

    private AB30MapperUtil cut;
    private File alternateSourceDir;

    @Before
    public void setUp() {
        super.setUp();
        try {
            setupUtil.configureLog4JProperties();
            URI uri = getClass().getResource("/TESTSLOCAL/ALTERNATE-SOURCE").toURI();
            alternateSourceDir = new File(uri);
            cut = new AB30MapperUtil(setupUtil.getEnvironmentConfig(), tesunClientJobListener, false, alternateSourceDir);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testCopyFileFromAlternateDir() {
        final File ab30XmlsDir = setupUtil.getEnvironmentConfig().getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        // Dateien im target löschen, damit #copyFileFromAlternateDir arbeitet
        deleteFilesInDir(ab30XmlsDir);

        // 4102004217 existiert nicht im TEST_PHASE.PHASE_1, muss aus alternativer Source-Verzeichnis werden
        long testCrefo = 4102004217L;
        Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists());
        try {
            cut.copyFileFromAlternateDir("", testCrefo, ab30XmlsDir);
            checkNotifyList(Arrays.asList("Test-Crefo " + testCrefo + " wurde aus alternativer Source-Verzeichnis", "kopiert."), null, null, null);
            Assert.assertTrue(new File(ab30XmlsDir, testCrefo + ".xml").exists());
        } catch (Exception ex) {
            Assert.fail("Unerwartete Exception:\n" + ex.getMessage());
        }

        // 4102004218 existiert nicht im TEST_PHASE.PHASE_1, muss aus alternativer Source-Verzeichnis werden, existiert aber auch dort nicht!
        testCrefo = 4102004218L;
        Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists()); // existiert nirgendswo!
        try {
            cut.copyFileFromAlternateDir("", testCrefo, ab30XmlsDir);
            Assert.fail("Exception erwartet!");
        } catch (Exception ex) {
            Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists()); // existiert nirgendswo, auch nach #copyFileFromAlternateDir
            checkNotifyList(Collections.emptyList(), ex, "Die Crefo existiert nicht im alternativen Verzeichnis", null);
        }
    }

    @Test
    public void testDownloadCrefoAnsSave() {
        final File ab30XmlsDir = setupUtil.getEnvironmentConfig().getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        // Dateien im target löschen, damit #downloadCrefoAnsSave arbeitet
        deleteFilesInDir(ab30XmlsDir);

        // 4110061138 existiert nicht im TEST_PHASE.PHASE_1, muss heruntergeladen werden
        long testCrefo = 4110061138L;
        Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists());
        try {
            cut.downloadCrefoAnsSave("", testCrefo, ab30XmlsDir);
            checkNotifyList(Arrays.asList("Für Test-Crefo " + testCrefo + " wurde AB30-XML-Datei heruntergeladen und", "abgespeichert."), null, null, null);
        } catch (Exception ex) {
            Assert.fail("Unerwartete Exception:\n" + ex.getMessage());
        }

        // 4130061138 existiert nicht im TEST_PHASE.PHASE_1, kann auch nicht heruntergeladen werden!
        testCrefo = 4130061138L;
        Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists());
        try {
            cut.downloadCrefoAnsSave("", testCrefo, ab30XmlsDir);
            Assert.fail("Exception erwartet!");
        } catch (Exception ex) {
            Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists());
            checkNotifyList(Collections.emptyList(), ex, "Fehler beim REST-Service-Aufruf!", null);
        }
    }

    @Test
    public void testDownloadCrefoBtlgOrEntg() {
        final File ab30XmlsDir = setupUtil.getEnvironmentConfig().getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        // Dateien im target löschen, damit #downloadCrefoBtlgOrEntg arbeitet
        deleteFilesInDir(ab30XmlsDir);

        // 4110061138 als Beteiligter existiert nicht im TEST_PHASE.PHASE_1, muss heruntergeladen werden
        long testCrefo = 4110061138L;
        Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists());
        try {
            cut.downloadCrefoAnsSave("", testCrefo, ab30XmlsDir);
            Assert.assertTrue(new File(ab30XmlsDir, testCrefo + ".xml").exists());
            checkNotifyList(Arrays.asList("Für Test-Crefo " + testCrefo + " wurde AB30-XML-Datei heruntergeladen und im Verzeichnis", " abgespeichert."), null, null, null);
        } catch (Exception ex) {
            Assert.fail("Unerwartete Exception:\n" + ex.getMessage());
        }

        // 4100061138 existiert nicht im TEST_PHASE.PHASE_1, kann nicht heruntergeladen werden, existiert auch nicht im alternativen Verzeichnis!
        testCrefo = 4100061138L;
        Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists());
        try {
            cut.downloadCrefoAnsSave("", 4102000238L, ab30XmlsDir);
            Assert.fail("Exception erwartet!");
        } catch (Exception ex) {
            Assert.assertFalse(new File(ab30XmlsDir, testCrefo + ".xml").exists());
            checkNotifyList(Arrays.asList("Download-Versuch für die Test-Crefo 4102000238 gescheitert",
                            "Versuche die Test-Crefo 4102000238 aus alternativem Source",
                            "Die Crefo existiert nicht im alternativen Verzeichnis"),
                    ex, "Fehler beim REST-Service-Aufruf!:: -> Statuscode entspricht nicht dem erwarteten '200' (OK), sondern lautet: 204 (No Content)",
                    "Die Crefo existiert nicht im alternativen Verzeichnis");
        }
    }

    @Test
    public void testInitAb30CrefoPropertiesMapEmpty() throws IOException {
        EnvironmentConfig environmentConfig = setupUtil.getEnvironmentConfig();
        File ab30XmlsDirP1 = environmentConfig.getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        File ab30XmlsDirP2 = environmentConfig.getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);

        // Dateien im target löschen, damit #initAb30CrefoPropertiesMap arbeitet
        deleteFilesInDir(ab30XmlsDirP1);
        deleteFilesInDir(ab30XmlsDirP2);

        // init Map aus archivBestandsPhase1TestCrefosFile, die aber nicht existiert!
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMapP1 = cut.initAb30CrefoPropertiesMap(new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME));
        Assert.assertTrue(ab30CrefoToPropertiesMapP1.isEmpty());

        // init Map aus archivBestandsPhase1TestCrefosFile, die aber nicht existiert!
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMapP2 = cut.initAb30CrefoPropertiesMap(new File(ab30XmlsDirP2, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME));
        Assert.assertTrue(ab30CrefoToPropertiesMapP2.isEmpty());
    }

    @Test
    public void testInitAb30CrefoPropertiesMapNotEmpty() throws IOException {
        EnvironmentConfig environmentConfig = setupUtil.getEnvironmentConfig();
        File ab30XmlsDirP1 = environmentConfig.getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        File archivBestandsPhase1TestCrefosFile = new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        File ab30XmlsDirP2 = environmentConfig.getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        File archivBestandsPhase2TestCrefosFile = new File(ab30XmlsDirP2, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);

        // Dateien im target löschen, damit #initAb30CrefoPropertiesMap arbeitet
        deleteFilesInDir(ab30XmlsDirP1);
        deleteFilesInDir(ab30XmlsDirP2);

        // vorgefertigte "TestCrefos.properties" Datei ablegen und initialisieren
        File srcFilePH1 = new File(alternateSourceDir, "PH1-" + TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        FileUtils.copyFile(srcFilePH1, archivBestandsPhase1TestCrefosFile);
        File srcFilePH2 = new File(alternateSourceDir, "PH2-" + TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        FileUtils.copyFile(srcFilePH2, archivBestandsPhase2TestCrefosFile);

        // init Map aus archivBestandsPhase1TestCrefosFile, die aber nicht existiert!
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMapP1 = cut.initAb30CrefoPropertiesMap(new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME));
        Assert.assertEquals(4, ab30CrefoToPropertiesMapP1.size());

        // init Map aus archivBestandsPhase1TestCrefosFile, die aber nicht existiert!
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMapP2 = cut.initAb30CrefoPropertiesMap(new File(ab30XmlsDirP2, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME));
        Assert.assertEquals(8, ab30CrefoToPropertiesMapP2.size());
    }

    @Test
    public void testHandleBtlgOrEntg() throws Exception {
        EnvironmentConfig environmentConfig = setupUtil.getEnvironmentConfig();
        File ab30XmlsDirP1 = environmentConfig.getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        File archivBestandsPhase1TestCrefosFile = new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        // Dateien im target löschen, damit #copyFileFromAlternateDir arbeitet
        deleteFilesInDir(ab30XmlsDirP1);
        // vorgefertigte "TestCrefos.properties" Datei ablegen und initialisieren
        File srcFilePH1 = new File(alternateSourceDir, "PH1-" + TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        FileUtils.copyFile(srcFilePH1, archivBestandsPhase1TestCrefosFile);

        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap = cut.initAb30CrefoPropertiesMap(new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME));
        AB30XMLProperties ab30XMLProperties = ab30CrefoToPropertiesMap.get(4112000001L);

        // Beteiligter 4112000002 ->  Download-Versuch -> Exception -> Versuche aus alternativem Source --> kopiert
        long btlgCrefo = 4112000002L;
        try {
            cut.handleBtlgOrEntg("", "Beteiligten", btlgCrefo, ab30XmlsDirP1, ab30XMLProperties, ab30CrefoToPropertiesMap);
            checkNotifyList(Arrays.asList("Für den Beteiligten 4112000002 der Test-Crefo 4112000001 existiert keine AB30-XML-Datei",
                            "Versuche den Beteiligten aus der Datenbank herunterzuladen...", "Download-Versuch für die Test-Crefo 4112000002 gescheitert", "Versuche die Test-Crefo 4112000002 aus alternativem Source",
                            "Test-Crefo 4112000002 wurde aus alternativer Source-Verzeichnis", "Beteiligten 4112000002 wurde schon als Mapping-Zeile aufgenommen!"),
                    null, null, null);
        } catch (Exception ex) {
            Assert.fail("Unerwartete Exception:\n" + ex.getMessage());
        }
        // Beteiligter 4202004217 ->  Download-Versuch -> Exception -> Versuche aus alternativem Source -->
        btlgCrefo = 4202004217L;
        try {
            cut.handleBtlgOrEntg("", "Beteiligten", btlgCrefo, ab30XmlsDirP1, ab30XMLProperties, ab30CrefoToPropertiesMap);
            Assert.fail("Exception erwartet!");
        } catch (Exception ex) {
            checkNotifyList(Arrays.asList(
                            "Nehme Beteiligten 4202004217 in die Btlg-Liste auf.",
                            "Für den Beteiligten 4202004217 der Test-Crefo 4112000001 existiert keine AB30-XML-Datei im",
                            "Versuche den Beteiligten aus der Datenbank herunterzuladen",
                            "Download-Versuch für die Test-Crefo 4202004217 gescheitert",
                            "Versuche die Test-Crefo 4202004217 aus alternativem Source",
                            "Fehler beim REST-Service-Aufruf!"),
                    ex, "Fehler beim REST-Service-Aufruf!:: -> Statuscode entspricht nicht dem erwarteten '200' (OK), sondern lautet: 204",
                    "Die Crefo existiert nicht im alternativen Verzeichnis");
        }
    }

    @Test
    public void testHandleBTLGsFromCrefoXMLFile() throws IOException {
        final File ab30XmlsDirP1 = setupUtil.getEnvironmentConfig().getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        // Dateien im target löschen, damit #copyFileFromAlternateDir arbeitet
        deleteFilesInDir(ab30XmlsDirP1);
        // vorgefertigte "TestCrefos.properties" Datei ablegen und initialisieren
        File srcFilePH1 = new File(alternateSourceDir, "PH1-" + TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        File archivBestandsPhase1TestCrefosFile = new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        FileUtils.copyFile(srcFilePH1, archivBestandsPhase1TestCrefosFile);
        File xmlFile1 = new File(alternateSourceDir, "4112000001.xml");
        FileUtils.copyFile(xmlFile1, new File(ab30XmlsDirP1, "4112000001.xml"));

        // "4112000001.xml" enthält <firmenbeteiligter> <crefonummer-beteiligter>4112000008 und 4112000008, diese Beteiligten behandeln...
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap = cut.initAb30CrefoPropertiesMap(new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME));
        AB30XMLProperties ab30XMLProperties = ab30CrefoToPropertiesMap.get(4112000001L);
        try {
            cut.handleBTLGsFromCrefoXMLFile("", ab30XmlsDirP1, ab30XMLProperties, ab30CrefoToPropertiesMap);
            checkNotifyList(Arrays.asList(
                            "Suche Beteiligten für Testfall-Crefo 4112000001 in der XML-Datei",
                            "Nehme Beteiligten 4112000008 in die Btlg-Liste auf.",
                            "Für den Beteiligten 4112000008 der Test-Crefo 4112000001 existiert keine AB30-XML-Datei",
                            "Versuche den Beteiligten aus der Datenbank herunterzuladen...",
                            "Download-Versuch für die Test-Crefo 4112000008 gescheitert",
                            "Versuche die Test-Crefo 4112000008 aus alternativem Sourc",
                            "Test-Crefo 4112000008 wurde aus alternativer Source-Verzeichnis",
                            "Nehme Beteiligten 4112000008 als Mapping-Zeile auf.",
                            "Nehme Beteiligten 4112000009 in die Btlg-Liste auf.",
                            "Für den Beteiligten 4112000009 der Test-Crefo 4112000001 existiert keine AB30-XML-Datei",
                            "Versuche den Beteiligten aus der Datenbank herunterzuladen...",
                            "Download-Versuch für die Test-Crefo 4112000009 gescheitert",
                            "Versuche die Test-Crefo 4112000009 aus alternativem Sourc",
                            "Test-Crefo 4112000009 wurde aus alternativer Source-Verzeichnis",
                            "Nehme Beteiligten 4112000009 als Mapping-Zeile auf."),
                    null, null, null);
        } catch (Exception ex) {
            Assert.fail("Unerwartete Exception:\n" + ex.getMessage());
        }
    }

    @Test
    public void testFilterAb30CrefoPropertiesMap() throws Exception {
        EnvironmentConfig environmentConfig = setupUtil.getEnvironmentConfig();
        File ab30XmlsDirP1 = environmentConfig.getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);

        // vorgefertigte "TestCrefos.properties" Datei ablegen und initialisieren
        File srcFilePH1 = new File(alternateSourceDir, "PH1-" + TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        File archivBestandsPhase1TestCrefosFile = new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        FileUtils.copyFile(srcFilePH1, archivBestandsPhase1TestCrefosFile);

        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap = cut.initAb30CrefoPropertiesMap(new File(ab30XmlsDirP1, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME));
        Map<String, TestCustomer> activeCustomersMap = environmentConfig.getCustomerTestInfoMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Map<Long, AB30XMLProperties> usedAb30CrefoToPropertiesMap = cut.filterAb30CrefoPropertiesMap("\n", ab30CrefoToPropertiesMap, activeCustomersMap, false);
        checkAB30XMLPropertiesMapSizes(ab30XmlsDirP1, usedAb30CrefoToPropertiesMap, 2);
    }

    @Test
    public void testParseCrefoFromXmlContent() throws URISyntaxException, XPathExpressionException {
        URL resourceURL = getClass().getResource("/4120874032.xml");
        File xmlFile = new File(resourceURL.toURI().getPath());
        Map<String, List<Long>> crefoListsMap = cut.parseCrefosFromXmlContent(xmlFile);
        Assert.assertNotNull(crefoListsMap);

        List<Long> crefosList = crefoListsMap.get(AB30MapperUtil.TAG_STEUERUNGSDATEN);
        Assert.assertNotNull(crefosList);
        Assert.assertEquals(1, crefosList.size());
        Assert.assertEquals(4120874032L, crefosList.get(0).longValue());

        List<Long> btlgCrefosList = crefoListsMap.get(AB30MapperUtil.TAG_FIRMENBETEILIGTER);
        Assert.assertNotNull(btlgCrefosList);
        Assert.assertEquals(21, btlgCrefosList.size());
        Assert.assertTrue(btlgCrefosList.contains(2151387012L));
        Assert.assertTrue(btlgCrefosList.contains(2150594299L));

        List<Long> verfBtlgCrefosList = crefoListsMap.get(AB30MapperUtil.TAG_VERFAHRENSBETEILIGTER);
        Assert.assertNotNull(verfBtlgCrefosList);
        Assert.assertEquals(2, verfBtlgCrefosList.size());
        Assert.assertTrue(verfBtlgCrefosList.contains(2010572197L));
        Assert.assertTrue(verfBtlgCrefosList.contains(3152004446L));
    }

    @Test
    public void testXPath() throws URISyntaxException, XPathExpressionException {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        URL resourceURL = getClass().getResource("/4120874032.xml");
        String path = resourceURL.toURI().getPath();
        InputSource xml = new InputSource(path);
        NodeList result0 = (NodeList) xpath.evaluate("//*[local-name() = verfahrensbeteiligter/crefonummer]", xml, XPathConstants.NODESET);
        NodeList result = (NodeList) xpath.evaluate("//*[local-name() = 'crefonummer']", xml, XPathConstants.NODESET);
        for (int i = 0; i < result.getLength(); i++) {
            System.out.println(result.item(i).getTextContent());
        }
    }

    private void checkNotifyList(List<String> expectedNotifyStrings, Exception ex, String expectedEx1Msg, String expectedEx2Msg) {
        if (ex != null) {
            Assert.assertTrue("Exception Message stimmt nicht!", ex.getMessage().contains(expectedEx1Msg));
            if (ex.getCause() != null) {
                Assert.assertTrue("Exception Message stimmt nicht!", ex.getCause().getMessage().contains(expectedEx2Msg));
            }
        }
        for (String expectedNotifyString : expectedNotifyStrings) {
            boolean found = false;
            for (String notify : getNotifyList()) {
                if (notify.contains(expectedNotifyString)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(" Notify '" + expectedNotifyString + "' nicht gefunden!", found);
        }
        notifyList.clear();
    }

    private void checkAB30XMLPropertiesMapSizes(File archivBestandsFile, Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap, int phase1Size) throws IOException {
        Assert.assertNotNull(ab30CrefoToPropertiesMap);
        Assert.assertEquals(phase1Size, ab30CrefoToPropertiesMap.size());
        List<String> stringList = FileUtils.readLines(new File(archivBestandsFile, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME)) ;
        Iterator<Long> iterator = ab30CrefoToPropertiesMap.keySet().iterator();
        while (iterator.hasNext()) {
            Long crefoNr = iterator.next();
            AB30XMLProperties ab30XMLProperties = ab30CrefoToPropertiesMap.get(crefoNr);
            Assert.assertTrue(stringList.contains(ab30XMLProperties.toString()));
        }
    }

}
