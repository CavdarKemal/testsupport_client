package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IZipEntryInfo;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test-Klasse für {@link CsvAbgleichMatchInfoListener}
 */
public class CsvAbgleichMatchInfoListenerTest {
   private final TestSupportClientKonstanten.SEARCH_RESULT_TYPE myResultType = TestSupportClientKonstanten.SEARCH_RESULT_TYPE.CREFOS_COUNT;

   @Test
   public void testNoCsvFile() {
      CsvAbgleichMatchInfoListener cut = new CsvAbgleichMatchInfoListener(null, "das-gibt-es-nicht äöü\\ //", myResultType, "target/Results", null);
      Assert.assertFalse( cut.isActive() );
   }

   @Test
   public void testLoadCsvFile() {
      CsvAbgleichMatchInfoListener cut = new CsvAbgleichMatchInfoListener(null, "src/test/resources/test-crefos-zum-abgleich.csv", myResultType, "target/Results", null);
      Assert.assertTrue( cut.isActive() );
      Assert.assertEquals(4, cut.getAnzahlCrefosZumAbgleich());
   }

   @Test
   public void testLoadEmptyCsvFile() {
      CsvAbgleichMatchInfoListener cut = new CsvAbgleichMatchInfoListener(null, "src/test/resources/test-keine-crefos-zum-abgleich.csv", myResultType, "target/Results", null);
      Assert.assertFalse( cut.isActive() );
      Assert.assertEquals(0, cut.getAnzahlCrefosZumAbgleich());
   }

   @Test
   public void testWriteResults()
   throws IOException {
      CsvAbgleichMatchInfoListener cut = new CsvAbgleichMatchInfoListener(null, "target/test-classes/test-crefos-zum-abgleich.csv", myResultType, "target/Results", null);
      Assert.assertTrue( cut.isActive() );
      Assert.assertEquals(4, cut.getAnzahlCrefosZumAbgleich());
      cut.notifyEntryMatched(null, createEntryInfo("9876543210"), null); // nicht in der CSV-Datei
      cut.notifyEntryMatched(null, createEntryInfo("1234567890"), null); // in der CSV-Datei enthalten
      cut.close();
   }

   private IZipEntryInfo createEntryInfo(String crefonummer) {
      IZipEntryInfo zipEntryInfo = EasyMock.createMock(IZipEntryInfo.class);
      EasyMock.expect(zipEntryInfo.getCrefonummer()).andReturn(crefonummer).once();
      EasyMock.replay(zipEntryInfo);
      return zipEntryInfo;
   }

   @Test
   public void testCsvFirstColumn() {
      CsvAbgleichMatchInfoListener cut = new CsvAbgleichMatchInfoListener(null, "target/test-classes/test-crefos-zum-abgleich.csv", myResultType, "target/Results", null);
      Map<String,String> expectedNotNull = new LinkedHashMap<>();
      expectedNotNull.put("1234567890", "keine weiteren Spalten, keine Anführungszeichen");
      expectedNotNull.put("\"1234567890\"", "keine weiteren Spalten, _mit_ Anführungszeichen");
      expectedNotNull.put("1234567890,", "weitere Spalten leer, keine Anführungszeichen");
      expectedNotNull.put("1234567890;", "weitere Spalten leer, keine Anführungszeichen");
      expectedNotNull.put("1234567890,xyz;uvw", "weitere Spalten vorhanden, keine Anführungszeichen");
      expectedNotNull.put("\"1234567890\",xyz;uvw", "weitere Spalten vorhanden, _mit_ Anführungszeichen");
      expectedNotNull.put("\"1234567890\" # Zeilen-Kommentar", "nur eine Spalte vorhanden, Kommentar am Zeilen-Ende");
      pruefeTestfaelle(cut, "1234567890", expectedNotNull);

      Map<String,String> expectedNull = new LinkedHashMap<>();
      expectedNull.put("\"\",xyz;uvw", "weitere Spalten vorhanden, _mit_ Anführungszeichen");
      expectedNull.put("\" ' ' \",xyz;uvw", "weitere Spalten vorhanden, verschachtelte Anführungszeichen");
      expectedNull.put(null, "keine Spalten vorhanden, null");
      expectedNull.put("", "keine Spalten vorhanden, Leerstring");
      expectedNull.put("\" \"", "keine Spalten vorhanden, Leerzeichen in Anführungszeichen");
      expectedNull.put(" ; Kommentar ", "keine Spalten vorhanden, Kommentar");
      expectedNull.put("; Kommentar ", "keine Spalten vorhanden, Kommentar");
      expectedNull.put(" # Kommentar ", "keine Spalten vorhanden, Kommentar");
      expectedNull.put("# Kommentar ", "keine Spalten vorhanden, Kommentar");
      pruefeTestfaelle(cut, null, expectedNull);

   }

   private void pruefeTestfaelle(CsvAbgleichMatchInfoListener cut, String expectedResult, Map<String,String> testFaelle) {
      for (Map.Entry<String,String> e : testFaelle.entrySet()) {
         Assert.assertEquals(e.getValue(), expectedResult, cut.csvFirstColumn(e.getKey()));
      }
   }


}
