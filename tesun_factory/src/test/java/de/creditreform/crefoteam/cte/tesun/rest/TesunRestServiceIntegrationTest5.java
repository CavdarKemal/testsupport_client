package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.dsgvoinfo.TesunDsgvoStatusCrefoErgebnisse;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.dsgvoinfo.TesunDsgvoStatusCrefoXml;
import de.creditreform.crefoteam.cte_cta.statistik.xmlbinding.CtaStatistik;
import de.creditreform.crefoteam.cteinsoexporttesun.insoxmlbinding.TesunInsoAktuellerStand;
import de.creditreform.crefoteam.cteinsoexporttesun.insoxmlbinding.TesunInsoSnippet;
import de.creditreform.cte.inso.monitor.xmlbinding.PgpKey;
import de.creditreform.cte.inso.monitor.xmlbinding.XmlKunde;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

public class TesunRestServiceIntegrationTest5 extends TesunRestServiceIntegrationTestBase {

    public TesunRestServiceIntegrationTest5() {
        super("ENE");
    }

    @Test
    public void testCtaStatistik() {
        CtaStatistik ctaStatistik = new CtaStatistik();
        ctaStatistik.setCrefonummer(BigInteger.valueOf(4120562759L));
        ctaStatistik.setAnzahlAuftraegeNachtragspflichtig(BigInteger.valueOf(11));
        ctaStatistik.setAnzahlAuskuenfte(BigInteger.valueOf(22));
        ctaStatistik.setDatumLetzteAuskunft(Calendar.getInstance());
        String responseBody = tesunRestServiceWLS.createCtaStatistikForCrefo(ctaStatistik);
        Assert.assertTrue(responseBody.isEmpty());
    }

    @Test
    public void testReadreadInsoProductForCrefo() {
        // http://rhsctem016.ecofis.de:7079/backend/pre-product/by-crefo/{crefo}
        // WADL: http://rhsctem016.ecofis.de:7079/application.wadl
        Long crefoNr = 5170107860L;
        TesunInsoAktuellerStand tesunInsoAktuellerStand = tesunRestServiceJvmInso.readInsoProductForCrefo(crefoNr);
        Assert.assertEquals(crefoNr.toString(), tesunInsoAktuellerStand.getCrefo());
        List<TesunInsoSnippet> tesunInsoSnippetList = tesunInsoAktuellerStand.getTesunInsoSnippet();
        Assert.assertEquals(2, tesunInsoSnippetList.size());
        TesunInsoSnippet tesunInsoSnippet = tesunInsoSnippetList.get(0);
        Assert.assertEquals("XML_PERSON", tesunInsoSnippet.getSnippetContentType());
        Assert.assertEquals("P", tesunInsoSnippet.getSnippetMatchcode());
        Assert.assertEquals("56281", tesunInsoSnippet.getSnippetPlz());
        Assert.assertTrue(tesunInsoSnippet.getSnippetXmldaten().contains("<p1-angaben-zur-person"));
        Assert.assertTrue(tesunInsoSnippet.getSnippetXmldaten().contains("<personendaten>"));
        Assert.assertTrue(tesunInsoSnippet.getSnippetXmldaten().contains("<crefonummer>5170107860</crefonummer>"));

        // Firmen-Crefo
        crefoNr = 5170026106L;
        tesunInsoAktuellerStand = tesunRestServiceJvmInso.readInsoProductForCrefo(crefoNr);
        Assert.assertEquals(crefoNr.toString(), tesunInsoAktuellerStand.getCrefo());
        tesunInsoSnippetList = tesunInsoAktuellerStand.getTesunInsoSnippet();
        Assert.assertEquals(2, tesunInsoSnippetList.size());
        tesunInsoSnippet = tesunInsoSnippetList.get(0);
        Assert.assertEquals("XML_FIRMA", tesunInsoSnippet.getSnippetContentType());
        Assert.assertEquals("F", tesunInsoSnippet.getSnippetMatchcode());
        Assert.assertEquals("55430", tesunInsoSnippet.getSnippetPlz());
        Assert.assertTrue(tesunInsoSnippet.getSnippetXmldaten().contains("<p1-angaben-zur-firma"));
        Assert.assertTrue(tesunInsoSnippet.getSnippetXmldaten().contains("<firmendaten>"));
        Assert.assertTrue(tesunInsoSnippet.getSnippetXmldaten().contains("<crefonummer>5170026106</crefonummer>"));
    }

    @Test
    public void testInsoKunde() {
        XmlKunde xmlKunde = tesunRestServiceJvmInsoBackend.readInsoKunde("Test-Tool");
        Assert.assertNotNull(xmlKunde);
        Assert.assertEquals("Firmenname wurde geändert!", "Test-Tool-Company", xmlKunde.getFirmenName());
        Assert.assertEquals("Kunden-Kürzel wurde geändert!", "Test-Tool", xmlKunde.getKundenKuerzel());
        Assert.assertEquals("Bemerkung wurde geändert!", "Kunde für das Test-Tool. Bitte nicht verändern!", xmlKunde.getBemerkung());
        Assert.assertEquals("Mitgliedsnummer wurde geändert!", 123456774L, xmlKunde.getMitgliedsNummer().longValue());
        Assert.assertEquals("Crefonummer wurde geändert!", 4125000040L, xmlKunde.getCrefonummer());
        Assert.assertNotNull("Produktiv-Seit wurde geändert!", xmlKunde.getProduktivSeit());
        Assert.assertNull("Produktiv-Bis wurde geändert!", xmlKunde.getPrduktivBis());
        Assert.assertTrue("Aktiv wurde geändert!", xmlKunde.isAktiv());
        Assert.assertFalse("LoeschKennzeichen wurde geändert!", xmlKunde.isLoeschKennzeichen());
        List<PgpKey> pgpKeyList = xmlKunde.getPgpKeyList();
        Assert.assertEquals("PGP-Keys wurde geändert!", 1, pgpKeyList.size());
        Assert.assertEquals("PGP-Key wurde geändert!", "zew_pgp_public_key.asc", pgpKeyList.get(0).getFileName());
    }

    @Test
    public void testSetDsgVoStatus() {
        // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/dsgvo/update?crefo=4120704598&sperren=true
        long crefoNr = 4120562759L;
        Boolean expectedDsgVoStatus = Boolean.TRUE;

        TesunDsgvoStatusCrefoErgebnisse dsgvoStatusCrefoErgebnisse = tesunRestServiceWLS.setDsgVoSperre(crefoNr, expectedDsgVoStatus);
        Assert.assertNotNull(dsgvoStatusCrefoErgebnisse);

        List<TesunDsgvoStatusCrefoXml> dsgvoStatusCrefoXmlsList = dsgvoStatusCrefoErgebnisse.getTesunDsgvoStatusCrefoXml();
        Assert.assertFalse(dsgvoStatusCrefoXmlsList.isEmpty());
        checkTesunDsgvoStatusCrefoXml(dsgvoStatusCrefoXmlsList.get(0), crefoNr, expectedDsgVoStatus, "DSGVO_GESPERRT");

        expectedDsgVoStatus = !expectedDsgVoStatus;
        TesunDsgvoStatusCrefoErgebnisse dsgvoStatusCrefoErgebnisse2 = tesunRestServiceWLS.setDsgVoSperre(crefoNr, expectedDsgVoStatus);
        Assert.assertNotNull(dsgvoStatusCrefoErgebnisse2);
        List<TesunDsgvoStatusCrefoXml> dsgvoStatusCrefoXmlsList2 = dsgvoStatusCrefoErgebnisse2.getTesunDsgvoStatusCrefoXml();
        checkTesunDsgvoStatusCrefoXml(dsgvoStatusCrefoXmlsList2.get(0), crefoNr, expectedDsgVoStatus, "DSGVO_KEINE_SPERRE");
    }

    private void checkTesunDsgvoStatusCrefoXml(TesunDsgvoStatusCrefoXml dsgvoStatusCrefoXml, long crefoNr, Boolean expectedDsgVoStatus, String expectedDsgvoStatusStr) {
        Assert.assertEquals(crefoNr, dsgvoStatusCrefoXml.getCrefonummer());
        Assert.assertEquals(expectedDsgVoStatus, dsgvoStatusCrefoXml.isGesperrt());
        Assert.assertEquals(expectedDsgvoStatusStr, dsgvoStatusCrefoXml.getStatusString());
        Assert.assertNotNull(dsgvoStatusCrefoXml.getGueltigAbIncl());
        Assert.assertNull(dsgvoStatusCrefoXml.getGueltigBisExcl());
    }

}
