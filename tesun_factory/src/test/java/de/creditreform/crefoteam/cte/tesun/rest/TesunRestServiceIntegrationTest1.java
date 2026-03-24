package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigUploadInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.jobexecution.TesunJobexecutionInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.pendingjobs.TesunPendingJob;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.pendingjobs.TesunPendingJobs;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.produktauftrag.TesunProduktAuftrag;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.produktauftrag.TesunProduktAuftragQuerverweis;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.systeminfo.TesunSystemInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingexport.TesunExportTrace;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingexport.TesunExportTrackingCrefo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingexport.TesunExportTrackingErgebnis;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingimport.TesunImportTrace;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingimport.TesunImportTrackingCrefo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingimport.TesunImportTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestFallFileUtil;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class TesunRestServiceIntegrationTest1 extends TesunRestServiceIntegrationTestBase {
    public TesunRestServiceIntegrationTest1() {
        super("ENE");
    }

    @Test
    public void test_getTesunSystemInfo() {
        TesunSystemInfo tesunSystemInfo = tesunRestServiceWLS.getTesunSystemInfo();
        Assert.assertNotNull(tesunSystemInfo);
        // Noch nicht gefüllt!!! Assert.assertNotNull(tesunSystemInfo.getUhrzeitDatenbank());
        Assert.assertNotNull(tesunSystemInfo.getUhrzeitRestservice());
        Assert.assertNotNull(tesunSystemInfo.getUmgebungsKuerzel());
        Assert.assertNotNull(tesunSystemInfo.getUmgebungsKuerzel());
        Assert.assertNotNull(tesunSystemInfo.getCteVersion());
        Assert.assertNotNull(tesunSystemInfo.getBuildVersionCteRest());
    }

    @Test
    public void test_getTesunPendingJobs() throws Exception {
    /* Aufruf-Beispiel
       REST-Client: GET http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/jobs/pending
       curl:        curl -X GET -i 'http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/jobs/pending'
       Response
            <?xml version="1.0" encoding="UTF-8"?>
            <tesun-pending-jobs xmlns="http://crefoteam.creditreform.de/cte/tesun/pendingjobs/2018/08">
              <umgebungsKuerzel>ENE<u/mgebungsKuerzel>
              <jobs>
                <infokeyStart>micExportStartDate</infokeyStart>
                <prozessIdentifier>EXPORT_CTE_TO_MIC</prozessIdentifier>
                <infokeyTodoBlock>micExportToDoBlock</infokeyTodoBlock>
                <anzahlTodoBloecke>2199</anzahlTodoBloecke>
              </jobs>
              <jobs>
                <infokeyStart>ctimportStartDate</infokeyStart>
                <prozessIdentifier>FROM_STAGING_INTO_CTE</prozessIdentifier>
                <infokeyTodoBlock>ctimportToDoBlock</infokeyTodoBlock>
                <anzahlTodoBloecke>0</anzahlTodoBloecke>
              </jobs>
            </tesun-pending-jobs>
    */
        TesunPendingJobs tesunPendingJobs = tesunRestServiceWLS.getTesunPendingJobs();
        Assert.assertNotNull(tesunPendingJobs);
        Assert.assertEquals("ENE", tesunPendingJobs.getUmgebungsKuerzel());
        List<TesunPendingJob> jobsList = tesunPendingJobs.getJobs();
        Assert.assertNotNull(jobsList);
    }

    @Test
    public void test_getLastJobExecutionInfo() {
        final String processIdentfier = "EXPORT_CTE_TO_BVD";
        TesunJobexecutionInfo tesunJobexecutionInfo = tesunRestServiceWLS.getTesunJobExecutionInfo(processIdentfier);
        Assert.assertNotNull(tesunJobexecutionInfo);
        Assert.assertEquals(processIdentfier, tesunJobexecutionInfo.getInfoKey());
        Assert.assertEquals("COMPLETED", tesunJobexecutionInfo.getJobStatus());
        Assert.assertNotNull(tesunJobexecutionInfo.getLastCompletitionDate());
        Assert.assertNotNull(tesunJobexecutionInfo.getLastStartDate());
    }

    @Test
    public void testGetTesunConfigInfoX() {
        TesunConfigInfo tesunConfigInfo = tesunRestServiceWLS.getTesunConfigInfo(environmentConfig, testCustomerMap);
        List<TesunConfigExportInfo> exportPfadeList = tesunConfigInfo.getExportPfade();
        exportPfadeList.stream().forEach(TesunConfigExportInfo -> {
            Assert.assertNotNull(TesunConfigExportInfo.getKundenKuerzel());
            Assert.assertNotNull(testCustomerMap.get(TesunConfigExportInfo.getKundenKuerzel()));
            Assert.assertNotNull(TesunConfigExportInfo.getRelativePath());
            //Assert.assertNotNull(TesunConfigExportInfo.getNamedAs());
            //Assert.assertNotNull(TesunConfigExportInfo.getProcessIdentifier());
        });
        List<TesunConfigUploadInfo> uploadPfadeList = tesunConfigInfo.getUploadPfade();
        uploadPfadeList.stream().forEach(TesunConfigUploadInfo -> {
            Assert.assertNotNull(TesunConfigUploadInfo.getKundenKuerzel());
            Assert.assertNotNull(testCustomerMap.get(TesunConfigUploadInfo.getKundenKuerzel()));
            Assert.assertNotNull(TesunConfigUploadInfo.getCompletePath());
            // Assert.assertNotNull(TesunConfigUploadInfo.getNamedAs());
        });

    }

    @Test
    public void testGetTesunConfigInfo() {
        TesunConfigInfo tesunConfigInfo = tesunRestServiceWLS.getTesunConfigInfo();
        Assert.assertNotNull(tesunConfigInfo);
        List<TesunConfigExportInfo> exportPfadeList = tesunConfigInfo.getExportPfade();
        Assert.assertNotNull(exportPfadeList);
        Assert.assertFalse(exportPfadeList.isEmpty());
        //Assert.assertTrue(exportPfadPrefix.contains( "fileserver.ene.creditreform.de" ));
        //Assert.assertTrue(exportPfadPrefix.contains( "ene" ));
        String umgebungsKuerzel = tesunConfigInfo.getUmgebungsKuerzel();
        Assert.assertNotNull(umgebungsKuerzel);
    }

    @Test
    public void test_getExportTrackingInfo() throws Exception {
      /*
         -- http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/trackingexport?crefo=5270219399&datumVom=2020-03-14 02:40:16
         SELECT * FROM &umg.admin.cte_export_protokoll ep
         where ep.crefonummer IN (5270219399 )
               AND (ep.DATUM_LETZTE_LIEFERUNG > to_date('2020-03-14 02:40:16', 'yyyy-mm-dd HH24:mi:ss' ))
         order by ep.DATUM_LETZTE_LIEFERUNG desc;
      */
        TestCrefo testCrefo = new TestCrefo("TN-00", 5270219399L, "", true, null);
        testCrefo.setPseudoCrefoNr(4112006898L);
        List<TestCrefo> testCrefoList = Arrays.asList(testCrefo);
        Date datumVom = TesunDateUtils.toDate("2020-03-14 02:40:16");
        TesunExportTrackingErgebnis exportTrackingInfo = tesunRestServiceWLS.getExportTrackingInfo(testCrefoList, datumVom, null);
        Assert.assertNotNull(exportTrackingInfo);
        List<TesunExportTrackingCrefo> crefoTrackingList = exportTrackingInfo.getCrefoTracking();
        Assert.assertEquals(testCrefoList.size(), crefoTrackingList.size());
        List<TesunExportTrace> letzteExporte = crefoTrackingList.get(0).getLetzteExporte();

        datumVom = TesunDateUtils.toDate("2020-06-08 02:04:11");
        exportTrackingInfo = tesunRestServiceWLS.getExportTrackingInfo(testCrefoList, datumVom, null);
        Assert.assertNotNull(exportTrackingInfo);
        crefoTrackingList = exportTrackingInfo.getCrefoTracking();
        Assert.assertEquals(testCrefoList.size(), crefoTrackingList.size());
        letzteExporte = crefoTrackingList.get(0).getLetzteExporte();
    }

    @Test
    public void test_getExportTrackingInfoX() throws Exception {
        /*    CREFONUMMER TYP   EXPORT_STATUS  DATUM_ERSTE_LIEFERUNG   DATUM_LETZTE_LIEFERUNG  DATUM_LOESCHUNG   */
        Date datumVom = TesunDateUtils.toDate("2010-10-01 09:46:17");
        TestCrefo testCrefo = new TestCrefo("TN-00", 4110184619L, "", true, null);
        testCrefo.setPseudoCrefoNr(4112006898L);
        List<TestCrefo> testCrefoList = Arrays.asList(testCrefo);
        TesunExportTrackingErgebnis exportTrackingInfo = tesunRestServiceWLS.getExportTrackingInfo(testCrefoList, datumVom, null);
        Assert.assertNotNull(exportTrackingInfo);
        List<TesunExportTrackingCrefo> crefoTrackingList = exportTrackingInfo.getCrefoTracking();
        Assert.assertEquals(testCrefoList.size(), crefoTrackingList.size());
        List<TesunExportTrace> letzteExporte = crefoTrackingList.get(0).getLetzteExporte();
        Assert.assertNotNull(letzteExporte);
        Assert.assertFalse(letzteExporte.isEmpty());
        TesunExportTrace tesunExportTrace = letzteExporte.get(0);
        Assert.assertNotNull(tesunExportTrace);
        String exportTyp = tesunExportTrace.getExportTyp();
        String status = tesunExportTrace.getStatus();
        Calendar exportDatum = tesunExportTrace.getExportDatum();
        Calendar loeschDatum = tesunExportTrace.getLoeschDatum();
        String strExportD = TesunDateUtils.formatCalendar(exportDatum);
        String strLoeschD = (loeschDatum != null) ? TesunDateUtils.formatCalendar(loeschDatum) : "";
        System.out.println(String.format("Crefo: %d, Export-Typ:%s, Status:%s, Export-Datum:%s, Lösch-Datum:%s", testCrefoList.get(0).getPseudoCrefoNr(), exportTyp, status, strExportD, strLoeschD));
    }

    @Test
    public void test_getImportTrackingInfo() throws Exception {
        List<String> crefosAsStringList = Arrays.asList("4112000461", "4112000462", "4112000463", "4112000464");
        TesunImportTrackingErgebnis importTrackingInfo = tesunRestServiceWLS.getImportTrackingInfo(crefosAsStringList);
        Assert.assertNotNull(importTrackingInfo);
        List<TesunImportTrackingCrefo> crefoTrackingList = importTrackingInfo.getCrefoTracking();
        Assert.assertNotNull(crefoTrackingList);
        Assert.assertFalse(crefoTrackingList.isEmpty());
        int nIndex = 0;
        for (String strCrefo : crefosAsStringList) {
            TesunImportTrackingCrefo trackingCrefo = crefoTrackingList.get(nIndex++);
            Assert.assertNotNull(trackingCrefo);
            TesunImportTrace importTrace = trackingCrefo.getImportTrace();
            Assert.assertNotNull(importTrace);
            String status = importTrace.getStatus();
            Calendar eingangBestand = importTrace.getEingangBestand();
            Calendar eingangStaging = importTrace.getEingangStaging();
            Calendar updateInCto = importTrace.getUpdateInCto();
            String strEingangBestandD = TesunDateUtils.formatCalendar(eingangBestand);
            String strEingangStagingD = (eingangStaging != null) ? TesunDateUtils.formatCalendar(eingangStaging) : "";
            String strUpdateInCtoD = (updateInCto != null) ? TesunDateUtils.formatCalendar(updateInCto) : "";
            System.out.println(String.format("Crefo: %s, Status:%s, EingangBestand-Datum:%s, EingangStaging-Datum:%s, UpdateInCto-Datum:%s", strCrefo, status, strEingangBestandD, strEingangStagingD, strUpdateInCtoD));
        }
    }

    @Test
    public void test_downloadCrefo() throws Exception {
        long crefo = 4112000461L;
        BufferedOutputStream bos = null;
        try {
            String crefoXML = tesunRestServiceWLS.downloadCrefo(crefo);
            Assert.assertNotNull(crefoXML);
            String filePath = "target/" + crefo + ".xml";
            FileOutputStream fos = new FileOutputStream(filePath);
            bos = new BufferedOutputStream(fos);
            bos.write(crefoXML.getBytes("UTF-8"));
            bos.flush();
            bos.close();
        } finally {
            IOUtils.closeQuietly(bos);
        }
    }

    @Test
    public void test_uploadSyntheticCrefo() throws Exception {
        List<File> filesList = TestFallFileUtil.downloadFolderContentFromFolder(TestFallFileUtil.FOLDERNAME_SYNTH_TESTCREFOS, ".xml", environmentConfig.getTestOutputsRoot());
        doTheUplodsForSyntheticCrefo(filesList, "WELLFORMED");
        doTheUplodsForSyntheticCrefo(filesList, "PRUEFE_NAMESPACE");
        doTheUplodsForSyntheticCrefo(filesList, "VALIDIERE_XSD_SCHEMA");
    }

    private void doTheUplodsForSyntheticCrefo(List<File> filesList, String validationMode) {
        changeCteCtaValidationMode(validationMode);
        AtomicLong crefoNr = new AtomicLong(4120000001L);
        filesList.forEach(theFile -> {
            try {
                InputStream inputStream = new FileInputStream(theFile);
                String xmlContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                int crefoPos = xmlContent.indexOf("<crefonummer>");
                crefoPos += "<crefonummer>".length();
                StringBuilder stringBuilder = new StringBuilder(xmlContent.substring(0, crefoPos));
                stringBuilder.append(crefoNr.longValue());
                stringBuilder.append(xmlContent.substring(crefoPos + 10));
                FileUtils.writeStringToFile(theFile, stringBuilder.toString());
                logger.info("\nLade die Crefo " + crefoNr + " ('" + theFile + "') hoch...");
                tesunRestServiceWLS.uploadCrefo(crefoNr.getAndIncrement(), theFile, TestSupportClientKonstanten.AB3_0_XSD);
            } catch (Exception ex) {
                throw new RuntimeException("Fehler beim Upload von: " + theFile + "\n" + ex);
            }
        });
    }

    @Test
    public void test_uploadCrefo() throws Exception {
        String rscPath = getClass().getResource("/").toURI().getPath();
        tesunRestServiceWLS.uploadCrefo(4120874032L, new File(rscPath, "4120874032.xml"), TestSupportClientKonstanten.AB3_0_XSD);
    }

    @Test
    public void test_uploadCrefo_With_WELLFORMED() throws Exception {
        changeCteCtaValidationMode("WELLFORMED");
        // Property "cte_cta_validation.mode" = "WELLFORMED", Resource ist AB3.0-XML, Version ist 3.0 ==> Upload OK
        String rscPath = getClass().getResource("/").toURI().getPath();
        File xmlFile = new File(rscPath, "9122016515_BESTAND__v3_0.xml");
        tesunRestServiceWLS.uploadCrefo(9122016515L, xmlFile, TestSupportClientKonstanten.AB3_0_XSD);

        // Default wieder setzen...
        changeCteCtaValidationMode("PRUEFE_NAMESPACE");
    }

    @Test
    public void test_uploadCrefo_With_PRUEFE_NAMESPACE() throws Exception {
        changeCteCtaValidationMode("PRUEFE_NAMESPACE");
        // Property "cte_cta_validation.mode" = "PRUEFE_NAMESPACE", Resource ist AB3.0-XML, Version ist 3.0 ==> Upload OK
        String rscPath = getClass().getResource("/").toURI().getPath();
        File xmlFile = new File(rscPath, "9122016515_BESTAND__v3_0.xml");
        tesunRestServiceWLS.uploadCrefo(9122016515L, xmlFile, TestSupportClientKonstanten.AB3_0_XSD);

        // Default wieder setzen...
        changeCteCtaValidationMode("PRUEFE_NAMESPACE");
    }

    @Test
    public void test_uploadCrefo_With_VALIDIERE_XSD_SCHEMA() throws Exception {
        changeCteCtaValidationMode("VALIDIERE_XSD_SCHEMA");
        // Property "cte_cta_validation.mode" = "VALIDIERE_XSD_SCHEMA", Resource ist AB3.0-XML, Version ist 3.0 ==> Upload OK
        String rscPath = getClass().getResource("/").toURI().getPath();
        File xmlFile = new File(rscPath, "9122016515_BESTAND__v3_0.xml");
        tesunRestServiceWLS.uploadCrefo(9122016515L, xmlFile, TestSupportClientKonstanten.AB3_0_XSD);

        // Default wieder setzen...
        changeCteCtaValidationMode("PRUEFE_NAMESPACE");
    }

    @Test
    public void test_uploadCrefosFromFile() throws Exception {
        File srcDir = new File(getClass().getResource("/").toURI().getPath());
        String rscFileName = getClass().getResource("/crefos.txt").toURI().getPath();
        final List<String> linesList = IOUtils.readLines(new FileInputStream(rscFileName));
        for (String line : linesList) {
            BufferedOutputStream bos = null;
            try {
                Long crefo = Long.parseLong(line);
                File xmlFile = new File(srcDir, crefo + ".xml");
                tesunRestServiceWLS.uploadCrefo(crefo, xmlFile, "http://www.creditreform.de/crefoteam/archivbestandv3_0");
                logger.info("Datei {} hochgeladen.", xmlFile.getAbsolutePath());
            } catch (Exception ex) {
                continue;
            } finally {
                IOUtils.closeQuietly(bos);
            }
        }
    }

    @Test
    public void test_getEhProduktAuftragQuerverweis()  {
        Long randomCrefoNummer = createRandomCrefoNummer();
        final TesunProduktAuftragQuerverweis ehProduktAuftragQuerverweis = tesunRestServiceWLS.getEhProduktAuftragQuerverweis(randomCrefoNummer);
        Assert.assertNotNull(ehProduktAuftragQuerverweis);
        Assert.assertEquals(ehProduktAuftragQuerverweis.getForCrefo(), randomCrefoNummer.longValue());
    }

    @Test
    public void test_createEhProduktAuftrag() throws Exception {
        final TesunProduktAuftrag tesunProduktauftragInfos1 = getEhProduktAuftragsCountFromTo();
        Assert.assertTrue(tesunProduktauftragInfos1.getProduktAuftragsList().size() > -1);

        // einen neuen Auftrag mit FileTransfer X anlegen (abgelehnt)...
        Long randomCrefoNummer = createRandomCrefoNummer();
        String strFileTransferNr = getFileTransferNr(randomCrefoNummer);
        String phase2Dir = "/TESTS/LOCAL/ARCHIV-BESTAND/PHASE-2/";
        String strProdAuftr = getSampleXml(phase2Dir + "EH-ProduktAuftrag-ABLEHNUNG_PRIVPERSON_FIRMA.xml", strFileTransferNr);
        System.out.println("Produktauftrag 'ABLEHNUNG_PRIVPERSON_FIRMA'  mit Crefo: " + randomCrefoNummer + " und FileTransferNr: " + strFileTransferNr);
        String strResult1 = tesunRestServiceWLS.createEhProduktAuftrag(randomCrefoNummer, strProdAuftr);
        Assert.assertNotNull(strResult1);
        // einen zweiten Auftrag mit derselben Crefo und FileTransfer anlegen (erledigt)...
        strProdAuftr = getSampleXml(phase2Dir + "EH-ProduktAuftrag-ERLEDIGUNG_PRIVPERSON_FIRMA.xml", strFileTransferNr);
        System.out.println("Produktauftrag 'ERLEDIGUNG_PRIVPERSON_FIRMA' mit Crefo: " + randomCrefoNummer + " und FileTransferNr: " + strFileTransferNr);
        String strResult2 = tesunRestServiceWLS.createEhProduktAuftrag(randomCrefoNummer, strProdAuftr);
        Assert.assertNotNull(strResult2);
        // da bei beiden dieselbe Crefonummer verwendet, müsste beim zweiten Mal ein Update erfolgt sein!
        Assert.assertEquals(strResult1, strResult2);

        final TesunProduktAuftrag tesunProduktauftragInfos2 = getEhProduktAuftragsCountFromTo();
        Assert.assertEquals(tesunProduktauftragInfos2.getProduktAuftragsList().size(), tesunProduktauftragInfos1.getProduktAuftragsList().size());

        // einen neuen  Auftrag mit FileTransfer Y anlegen (abgelehnt)...
        strFileTransferNr = getFileTransferNr(randomCrefoNummer) + "-Y";
        strProdAuftr = getSampleXml(phase2Dir + "EH-ProduktAuftrag-ABLEHNUNG_FIRMA_PRIVPERSON.xml", strFileTransferNr);
        randomCrefoNummer = createRandomCrefoNummer();
        System.out.println("Produktauftrag 'ABLEHNUNG_FIRMA_PRIVPERSON'  mit Crefo: " + randomCrefoNummer + " und FileTransferNr: " + strFileTransferNr);
        strResult1 = tesunRestServiceWLS.createEhProduktAuftrag(randomCrefoNummer, strProdAuftr);
        Assert.assertNotNull(strResult1);
        // einen zweiten Auftrag mit FileTransfer Z anlegen (erledigt)...
        strFileTransferNr = getFileTransferNr(randomCrefoNummer) + "-Z";
        strProdAuftr = getSampleXml(phase2Dir + "EH-ProduktAuftrag-ERLEDIGUNG_FIRMA_PRIVPERSON.xml", strFileTransferNr);
        randomCrefoNummer = createRandomCrefoNummer();
        System.out.println("Produktauftrag 'ERLEDIGUNG_FIRMA_PRIVPERSON' mit Crefo: " + randomCrefoNummer + " und FileTransferNr: " + strFileTransferNr);
        strResult2 = tesunRestServiceWLS.createEhProduktAuftrag(randomCrefoNummer, strProdAuftr);
        // da bei beiden Aufträge unterschiedliche Crefonummer verwendet haben, müsste beim zweiten Mal ein Create erfolgt sein!
        Assert.assertNotEquals(strResult1, strResult2);

        final TesunProduktAuftrag tesunProduktauftragInfos3 = getEhProduktAuftragsCountFromTo();
        Assert.assertEquals(tesunProduktauftragInfos3.getProduktAuftragsList().size(), tesunProduktauftragInfos1.getProduktAuftragsList().size());
    }

    @Test
    public void testOrderCrefo() throws Exception {
        URL resource = this.getClass().getResource("/Ikaros_Auftraege.txt");
        File file = new File(resource.toURI());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = "";
        int clzIndex = 0;
        String[] testfallNameArray = new String[]{"p01_ika.215", "p02_ika.411", "p03_ika.412", "p04_ika.505", "p05_ika.511", "p06_ika.517", "p07_ika.527"};
        while (null != (line = bufferedReader.readLine())) {
            String autragsKennung = tesunRestServiceJvmImpCycle.orderCrefo(Long.valueOf(line), testfallNameArray[clzIndex]);
            Assert.assertTrue(autragsKennung.contains(line));
            clzIndex = (clzIndex + 1) % testfallNameArray.length;
        }
    }

    private String getFileTransferNr(Long crefoNummer) {
        String fileTransferNr = crefoNummer.toString().substring(0, 3) + "_" + crefoNummer;
        return fileTransferNr;
    }

    private Long createRandomCrefoNummer() {
        Random random = new Random();
        String strTemp = (TestSupportClientKonstanten.TEST_CLZ_412 + Math.abs(random.nextLong())).substring(0, 10);
        final Long aLong = Long.valueOf(strTemp);
        return aLong;
    }

    private TesunProduktAuftrag getEhProduktAuftragsCountFromTo() {
        Calendar theCal = Calendar.getInstance();
        theCal.add(Calendar.YEAR, -1);
        Date datumVom = theCal.getTime();
        theCal = Calendar.getInstance();
        theCal.add(Calendar.YEAR, 1);
        Date datumBis = theCal.getTime();
        return tesunRestServiceWLS.getEhProduktAuftragInfos(datumVom, datumBis);
    }

    private String getSampleXml(String strXmlFilename, String strFileTransferNr) throws IOException, URISyntaxException {
        URL resourceURL = getClass().getResource(strXmlFilename);
        final String xmlContent = FileUtils.readFileToString(new File(resourceURL.toURI().getPath()));
        return xmlContent.replace("<!--FILE_TRANSFER_NUMMER-->0", strFileTransferNr);
    }

    private void changeCteCtaValidationMode(String newMode) {
        tesunRestServiceWLS.restoreEnvironmentProperties();
        CteEnvironmentProperties cteEnvironmentProperties = tesunRestServiceWLS.getEnvironmentProperties("cte_cta_validation.mode", "", true);
        Assert.assertEquals("Es sollte nur ein Property 'cte_cta_validation.mode' existieren!", 1, cteEnvironmentProperties.getProperties().size());
        CteEnvironmentPropertiesTupel cteEnvironmentPropertiesTupel = cteEnvironmentProperties.getProperties().get(0);
        cteEnvironmentPropertiesTupel.setValue(newMode);
        //cteEnvironmentPropertiesTupel.setDbOverride(true);
        tesunRestServiceWLS.setEnvironmentProperties(cteEnvironmentProperties);
    }
    /*
    private void checkCteCtaValidationMode(String expected) {
        CteEnvironmentProperties cteEnvironmentProperties = tesunRestServiceWLS.getEnvironmentProperties("cte_cta_validation", ".mode");
        Assert.assertEquals("Es sollte nur ein Property 'cte_cta_validation.mode' existieren!", 1, cteEnvironmentProperties.getProperties().size());
        CteEnvironmentPropertiesTupel cteEnvironmentPropertiesTupel = cteEnvironmentProperties.getProperties().get(0);
        String actual = cteEnvironmentPropertiesTupel.getValue().toString();
        Assert.assertEquals("Property-Wert nicht wie erwartet!", expected, actual);
    }
    */
}
