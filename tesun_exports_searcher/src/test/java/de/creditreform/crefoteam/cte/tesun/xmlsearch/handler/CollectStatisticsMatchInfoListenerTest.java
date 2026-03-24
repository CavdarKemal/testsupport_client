package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IGroupByRow;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IPerEntryStatistics;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IZipEntryInfo;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.CollectStatisticsMatchInfoListener.*;

/**
 * Test-Klasse für {@link CollectStatisticsMatchInfoListener}
 */
public class CollectStatisticsMatchInfoListenerTest {

   private void addResult(CollectStatisticsMatchInfoListener cut, String forFile, int anzahlTreffer, String k1, String... moreKeys) throws Exception {
      // IZipEntryInfo mit der Angabe des Dateinamens
      IZipEntryInfo entryInfo = EasyMock.createMock(IZipEntryInfo.class);
      EasyMock.expect( entryInfo.getZipFileName() ).andReturn(forFile).anyTimes();
      EasyMock.replay(entryInfo);
      // IGroupByRow
      List<String> componentsOfKeyList = new ArrayList<>();
      componentsOfKeyList.add(k1);
      componentsOfKeyList.addAll(Arrays.asList(moreKeys));
      IGroupByRow row = new GroupByRowDefaultImpl(componentsOfKeyList);
      // Map mit den Ergebnissen
      Map<IGroupByRow, Integer> statsMiniMap = new HashMap<>();
      statsMiniMap.put(row, anzahlTreffer);
      // IXmlMatchStatistics als Container für alle Ergebnisse aus einem Zip-Entry
      IPerEntryStatistics matchStatistics = EasyMock.createMock(IPerEntryStatistics.class);
      EasyMock.expect( matchStatistics.getZipEntryStatistics() ).andReturn(statsMiniMap).once();
      EasyMock.replay(matchStatistics);
      // Speichern/Summieren der Treffer
      cut.notifyEntryMatched(null, entryInfo, matchStatistics);
   }

   @Test
   public void testWriteResult() throws Exception {
      CollectStatisticsMatchInfoListener cut = new CollectStatisticsMatchInfoListener(TestSupportClientKonstanten.SEARCH_RESULT_TYPE.CREFOS_COUNT,
                                                                                      "target/Results",
                                                                                      "CollectStatisticsMatchInfoListenerTest");
      addResult(cut, "2234567890.zip", 3, "2010", "2");
      addResult(cut, "1234567890.zip", 1, "2011", "2");
      addResult(cut, "1234567890.zip", 3, "2010", "2");
      addResult(cut, "2234567890.zip", 5, "2010", "2");
      addResult(cut, "1234567890.zip", 3, "2011", "2");
      addResult(cut, "1234567890.zip", 7, "2010", "2");
      cut.close();
      Path expectedPath = Paths.get("./target/Results/CollectStatisticsMatchInfoListenerTest/CREFOS_COUNT/"+CollectStatisticsMatchInfoListener.FILE_NAME_RAW_STATS);
      Assert.assertTrue(expectedPath.toFile().exists());

      List<String> expectedLines = new ArrayList<>(Arrays.asList("1234567890.zip;2010;2;10",
                                                                 "1234567890.zip;2011;2;4",
                                                                 "2234567890.zip;2010;2;8"));

      List<String> actualLines = Files.readAllLines(expectedPath, StandardCharsets.UTF_8 );

      for (String actLine : actualLines) {
         expectedLines.remove(actLine);
      }

      if (!expectedLines.isEmpty()) {
         Assert.fail("Nicht alle Zeilen wurden in der CSV-Datei gefunden: "+expectedLines);
      }
   }

   @Test
   public void testCountryDetection() {
      CollectStatisticsMatchInfoListener cut = new CollectStatisticsMatchInfoListener(TestSupportClientKonstanten.SEARCH_RESULT_TYPE.CREFOS_COUNT,
                                                                                      "target/Results",
                                                                                      "CollectStatisticsMatchInfoListenerTest");
      Assert.assertEquals(CN_DE, cut.getCountryFromFileName("/home/ralf/Documents/Skripte/2019.08.28_BvD_Testlieferung/2019-08-22_11-45-VC937/abCrefo_2000000000.zip") );
      Assert.assertEquals(CN_DE, cut.getCountryFromFileName("/home/ralf/Documents/Skripte/2019.08.28_BvD_Testlieferung/2019-08-22_11-45-VC937/abCrefo_8350000000.zip") );
      Assert.assertEquals(CN_AT, cut.getCountryFromFileName("/home/ralf/Documents/Skripte/2019.08.28_BvD_Testlieferung/2019-08-22_11-45-VC937/abCrefo_9000000000.zip") );
      Assert.assertEquals(CN_AT, cut.getCountryFromFileName("/home/ralf/Documents/Skripte/2019.08.28_BvD_Testlieferung/2019-08-22_11-45-VC937/abCrefo_9150000000.zip") );
      Assert.assertEquals(CN_LU, cut.getCountryFromFileName("/home/ralf/Documents/Skripte/2019.08.28_BvD_Testlieferung/2019-08-22_11-45-VC937/abCrefo_9370000000.zip") );
      Assert.assertEquals("941", cut.getCountryFromFileName("/home/ralf/Documents/Skripte/2019.08.28_BvD_Testlieferung/2019-08-22_11-45-VC937/abCrefo_9410000000.zip") );
   }

}
