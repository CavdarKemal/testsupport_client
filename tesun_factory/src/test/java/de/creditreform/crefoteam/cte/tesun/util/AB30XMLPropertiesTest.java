package de.creditreform.crefoteam.cte.tesun.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;

/**
 * Test-Klasse für AB30XMLProperties
 */
public class AB30XMLPropertiesTest {

   private List<AB30XMLProperties.EH_PROD_AUFTR_TYPE> findByAuftragsArt(Function<AB30XMLProperties.EH_PROD_AUFTR_TYPE, Boolean> fn) {
      List<AB30XMLProperties.EH_PROD_AUFTR_TYPE> byAuftragsArt = new ArrayList<>();
      for (AB30XMLProperties.EH_PROD_AUFTR_TYPE epat : AB30XMLProperties.EH_PROD_AUFTR_TYPE.values()) {
         if (Boolean.TRUE.equals(fn.apply(epat))) {
            byAuftragsArt.add(epat);
            Assert.assertTrue(epat.isUploadErforderlich());
         }
      }
      return byAuftragsArt;
   }

   /**
    * Die Ausprägungen von {@link de.creditreform.crefoteam.cte.tesun.util.AB30XMLProperties.EH_PROD_AUFTR_TYPE} dürfen
    * nur einen eindeutigen Typ aufweisen
    */
   @Test
   public void testUniqueType() {
      List<AB30XMLProperties.EH_PROD_AUFTR_TYPE> alle = new ArrayList<>();
      alle.addAll(findByAuftragsArt(AB30XMLProperties.EH_PROD_AUFTR_TYPE::isAuftragRecherche));
      alle.addAll(findByAuftragsArt(AB30XMLProperties.EH_PROD_AUFTR_TYPE::isAuftragInitialBuyer));
      // Mit Ausnahme von 'KEINE' sollten wir alle Ausprägungen genau ein mal getroffen haben...
      Assert.assertEquals(AB30XMLProperties.EH_PROD_AUFTR_TYPE.values().length-1, alle.size());
   }

   // "# CREFO::[IKA-AUFTR-CLZ],[{BTLG-CREFO;...}],[BEFR|BILANZ|BEIDES],[ABLEHNUNG_FIRMA_FIRMA|ABLEHNUNG_FIRMA_PRIVPERSON|...],[CTA_STATISTIK],[DSGVO_SPERRE],[{Used-By-Customer;...}]"
   private void doTest(String strLine,
                       int version, Long crefoNr, Long auftrNr, List<Long> btlgList,
                       AB30XMLProperties.BILANZEN_TYPE bilanzType, AB30XMLProperties.EH_PROD_AUFTR_TYPE ehProdAuftrType,
                       Boolean mitCtaStatistik, Boolean mitDsgVoSperre,
                       String usedByCustomers, String expectedErrorString) {
      try {
         System.out.println("\nTest mit "
            + "\n\tCrefonummer : " + crefoNr
            + "\n\tAuftragsnummer : " + auftrNr
            + "\n\tBeteiligten : " + btlgList
            + "\n\tBilanz-Typ : " + bilanzType
            + "\n\tEH-ProdAuftr-Typ : " + ehProdAuftrType
            + "\n\tCTA-Statistik : " + mitCtaStatistik
            + "\n\tDSGVO-Sperre : " + mitDsgVoSperre
            + "\n\tKunden : " + usedByCustomers
         );
         AB30XMLProperties ab30XMLProperties = new AB30XMLProperties(strLine, version);
         Assert.assertNull(expectedErrorString);
         Assert.assertEquals(crefoNr, ab30XMLProperties.getCrefoNr());
         Assert.assertTrue(auftrNr != null ? ab30XMLProperties.getAuftragClz().equals(auftrNr) : true);
         Assert.assertTrue(btlgList != null ? ab30XMLProperties.getBtlgCrefosList().containsAll(btlgList) : true);
         Assert.assertEquals(bilanzType, ab30XMLProperties.getBilanzType());
         Assert.assertEquals(ehProdAuftrType, ab30XMLProperties.getEhProduktAuftragType());
         Assert.assertTrue(mitCtaStatistik != null ? ab30XMLProperties.isMitCtaStatistik() : true);
         Assert.assertTrue(mitDsgVoSperre != null ? ab30XMLProperties.isMitDsgVoSperre() : true);
         Assert.assertTrue(usedByCustomers != null ? (usedByCustomers.split(";").length == ab30XMLProperties.getUsedByCustomersList().size()) : true);

      } catch (IllegalArgumentException ex) {
         Assert.assertTrue(ex.getMessage().startsWith(expectedErrorString));
      }
   }

   @Test
   public void testConstructWithDiffLine() {
      // "# CREFO::[{Used-By-Customer;...}],[IKA-AUFTR-CLZ],[{BTLG-CREFO;...}],[BEFR|BILANZ|BEIDES],[ABLEHNUNG_FIRMA_FIRMA|ABLEHNUNG_FIRMA_PRIVPERSON|...],[CTA_STATISTIK],[DSGVO_SPERRE]"
      doTest("", 2, null, null, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, "\nDie Zeile darf nicht leer oder NULL sein!");
      long expectedCrefoNr = 2012006924L;
      doTest("2012006924", 2, expectedCrefoNr, null, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      doTest("2012006924::", 2, expectedCrefoNr, null, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      doTest("2012006924::[]", 2, expectedCrefoNr, null, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      long expectedAuftrNr = 317L;
      doTest("2012006924::[],[317]", 2, expectedCrefoNr, expectedAuftrNr, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      List<Long> expectedBtlgList = Collections.EMPTY_LIST;
      doTest("2012006924::[],[317],[]", 2, expectedCrefoNr, expectedAuftrNr, expectedBtlgList, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      expectedBtlgList = Arrays.asList(2012006925L, 2012006926L);
      doTest("2012006924::[],[317],[2012006925;2012006926]", 2, expectedCrefoNr, expectedAuftrNr, expectedBtlgList, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      doTest("2012006924::[],[317],[2012006925;2012006926],[]", 2, expectedCrefoNr, expectedAuftrNr, expectedBtlgList, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      for (AB30XMLProperties.BILANZEN_TYPE bilanzType : AB30XMLProperties.BILANZEN_TYPE.values()) {
         doTest("2012006924::[],[317],[2012006925;2012006926],[" + bilanzType.name() + "]", 2, expectedCrefoNr, expectedAuftrNr, expectedBtlgList, bilanzType, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      }
      for (AB30XMLProperties.EH_PROD_AUFTR_TYPE ehProdAuftrType : AB30XMLProperties.EH_PROD_AUFTR_TYPE.values()) {
         doTest("2012006924::[],[317],[2012006925;2012006926],[KEINE],[" + ehProdAuftrType + "]", 2, expectedCrefoNr, expectedAuftrNr, expectedBtlgList, AB30XMLProperties.BILANZEN_TYPE.KEINE, ehProdAuftrType, null, null, null, null);
      }
      doTest("2012006924::[],[317],[2012006925;2012006926],[KEINE],[KEINE],[]", 2, expectedCrefoNr, expectedAuftrNr, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, null, null, null, null);
      doTest("2012006924::[],[317],[2012006925;2012006926],[KEINE],[KEINE],[CTA_STATISTIK]", 2, expectedCrefoNr, expectedAuftrNr, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, Boolean.TRUE, null, null, null);
      doTest("2012006924::[],[317],[2012006925;2012006926],[KEINE],[KEINE],[CTA_STATISTIK],[]", 2, expectedCrefoNr, expectedAuftrNr, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, Boolean.TRUE, null, null, null);
      doTest("2012006924::[],[317],[2012006925;2012006926],[KEINE],[KEINE],[CTA_STATISTIK],[DSGVO_SPERRE]", 2, expectedCrefoNr, expectedAuftrNr, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, Boolean.TRUE, Boolean.TRUE, null, null);

      doTest("2012006924::[],[317],[2012006925;2012006926],[KEINE],[KEINE],[CTA_STATISTIK],[DSGVO_SPERRE]", 2, expectedCrefoNr, expectedAuftrNr, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, Boolean.TRUE, Boolean.TRUE, null, null);
      doTest("2012006924::[EH],[317],[2012006925;2012006926],[KEINE],[KEINE],[CTA_STATISTIK],[DSGVO_SPERRE]", 2, expectedCrefoNr, expectedAuftrNr, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, Boolean.TRUE, Boolean.TRUE, "EH", null);
      doTest("2012006924::[EH;CRM],[317],[2012006925;2012006926],[KEINE],[KEINE],[CTA_STATISTIK],[DSGVO_SPERRE]", 2, expectedCrefoNr, expectedAuftrNr, null, AB30XMLProperties.BILANZEN_TYPE.KEINE, AB30XMLProperties.EH_PROD_AUFTR_TYPE.KEINE, Boolean.TRUE, Boolean.TRUE, "EH;CRM", null);
   }

   @Test
   public void testConstructCrefoOnly() {
      String strLine = "2012006924::";
      int version = 1;
      AB30XMLProperties cut = new AB30XMLProperties(strLine, version);
      Assert.assertNotNull(cut);
      Assert.assertEquals("Crefonummer wurde falsch geparst!", 2012006924L, cut.getCrefoNr().longValue());
      Assert.assertNull("Autfrags-Clz wurde falsch geparst!", cut.getAuftragClz());
      TreeSet<Long> entgCrefosList = cut.getBtlgCrefosList();
      Assert.assertTrue("Entscheidungsträger wurden falsch geparst!", entgCrefosList.isEmpty());
      Assert.assertEquals("Bilanz wurde falsch geparst!", AB30XMLProperties.BILANZEN_TYPE.KEINE, cut.getBilanzType());
   }

   @Test
   public void testConstructCrefoAndClz() {
      String strLine = "2012006924:: [345]";
      int version = 1;
      AB30XMLProperties cut = new AB30XMLProperties(strLine, version);
      Assert.assertNotNull(cut);
      Assert.assertEquals("Crefonummer wurde falsch geparst!", 2012006924L, cut.getCrefoNr().longValue());
      Assert.assertEquals("Autfrags-Clz wurde falsch geparst!", 345L, cut.getAuftragClz().longValue());
      TreeSet<Long> entgCrefosList = cut.getBtlgCrefosList();
      Assert.assertTrue("Entscheidungsträger wurden falsch geparst!", entgCrefosList.isEmpty());
      Assert.assertEquals("Bilanz wurde falsch geparst!", AB30XMLProperties.BILANZEN_TYPE.KEINE, cut.getBilanzType());
   }

   @Test
   public void testConstructCrefoAndClzAndBtlgsAndCustomers() {
      // "#             CREFO::     [{Used-By-Customer;...}], [IKA-AUFTR-CLZ],[{BTLG-CREFO;...}],     [BEFR|BILANZ|BEIDES],[ABLEHNUNG_FIRMA_FIRMA|ABLEHNUNG_FIRMA_PRIVPERSON|...],[CTA_STATISTIK],[DSGVO_SPERRE]"
      String strLine = "2012006924::[EH;CRM]  ,               [719],          [2012192856;2012193487],[],                  [],                                                    [],             []";
      int version = 2;
      AB30XMLProperties cut = new AB30XMLProperties(strLine, version);
      Assert.assertNotNull(cut);
      Assert.assertEquals("Crefonummer wurde falsch geparst!", 2012006924L, cut.getCrefoNr().longValue());
      Assert.assertEquals("Autfrags-Clz wurde falsch geparst!", 719L, cut.getAuftragClz().longValue());
      TreeSet<Long> entgCrefosList = cut.getBtlgCrefosList();
      Assert.assertEquals("BTLG/Entscheidungsträger wurden falsch geparst!", 2, entgCrefosList.size());
      Assert.assertTrue("BTLG/Entscheidungsträger wurde falsch geparst!", entgCrefosList.contains(2012192856L));
      Assert.assertTrue("BTLG/Entscheidungsträger wurde falsch geparst!", entgCrefosList.contains(2012193487L));
      Assert.assertEquals("Bilanz wurde falsch geparst!", AB30XMLProperties.BILANZEN_TYPE.KEINE, cut.getBilanzType());
      Assert.assertFalse("CTA_STATISTIK wurde falsch geparst!", cut.isMitCtaStatistik());
      Assert.assertFalse("DSGVO_SPERRE wurde falsch geparst!", cut.isMitDsgVoSperre());
      Assert.assertEquals("Used-By-Customer wurde falsch geparst!", 2, cut.getUsedByCustomersList().size());
   }

   @Test
   public void testConstructCrefoAndClzAndBilanz() {
      String strLine = "2012006924:: [],[719],[2012192856;2012193487],[BILANZ],[],[],[]";
      int version = 2;
      AB30XMLProperties cut = new AB30XMLProperties(strLine, version);
      Assert.assertNotNull(cut);
      Assert.assertEquals("Crefonummer wurde falsch geparst!", 2012006924L, cut.getCrefoNr().longValue());
      Assert.assertEquals("Autfrags-Clz wurde falsch geparst!", 719L, cut.getAuftragClz().longValue());
      TreeSet<Long> entgCrefosList = cut.getBtlgCrefosList();
      Assert.assertEquals("Entscheidungsträger wurden falsch geparst!", 2, entgCrefosList.size());
      Assert.assertTrue("Entscheidungsträger wurde falsch geparst!", entgCrefosList.contains(2012192856L));
      Assert.assertTrue("Entscheidungsträger wurde falsch geparst!", entgCrefosList.contains(2012193487L));
      Assert.assertEquals("Bilanz wurde falsch geparst!", AB30XMLProperties.BILANZEN_TYPE.BILANZ, cut.getBilanzType());
   }
}
