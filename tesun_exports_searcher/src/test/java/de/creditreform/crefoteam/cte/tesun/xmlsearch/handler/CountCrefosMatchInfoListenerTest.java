package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IPerEntryStatistics;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IZipEntryInfo;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test-Klasse für {@link CountCrefosMatchInfoListener}
 */
public class CountCrefosMatchInfoListenerTest {

//   @Rule
//   public de.creditreform.crefoteam.cte.testutils_cte.junit4rules.logappenders.AppenderRuleCollecting ruleCollecting

   private IZipEntryInfo createZipEntryInfo(String zipFileName, String zipEntryName) {
      IZipEntryInfo newEntryInfo = EasyMock.createMock(IZipEntryInfo.class);
      EasyMock.expect(newEntryInfo.getZipFileName()).andReturn(zipFileName).anyTimes();
      EasyMock.expect(newEntryInfo.getZipEntryName()).andReturn(zipEntryName).anyTimes();
      EasyMock.replay(newEntryInfo);
      return newEntryInfo;
   }

   private IPerEntryStatistics createMatchStatistics(int anzahlTreffer) {
      IPerEntryStatistics newStats = EasyMock.createMock(IPerEntryStatistics.class);
      EasyMock.expect( newStats.getMatchingCriteriaCount() ).andReturn(anzahlTreffer).anyTimes();
      EasyMock.replay(newStats);
      return newStats;
   }

   private static String onCloseMessage01 = "=== Anzahl der Zip-Entries mit mindestens einem Treffer: 2 ===\n" +
   "=== Statistik der gefundenen Treffer (Zip-Datei,Einträge mit Treffer, Anzahl Treffer in der Datei) ===\n" +
   "file-1,2,2\n" +
   "=== Anzahl der gefundenen Treffer insgesamt: 2";

   private static String onCloseMessage02 = "=== Anzahl der Zip-Entries mit mindestens einem Treffer: 4 ===\n" +
   "=== Statistik der gefundenen Treffer (Zip-Datei,Einträge mit Treffer, Anzahl Treffer in der Datei) ===\n" +
   "file-1,2,2\n" +
   "file-2,2,12\n" +
   "=== Anzahl der gefundenen Treffer insgesamt: 14";

   @Test
   public void testCollectMatches() {
      CountCrefosMatchInfoListener cut = new CountCrefosMatchInfoListener("JUNIT-testCollectMatches");
      IZipEntryInfo firstZipEntry = createZipEntryInfo("file-1", "entry-11");
      cut.notifyEntryMatched(null, firstZipEntry, null);
      cut.notifyEntryMatched(null, createZipEntryInfo("file-1", "entry-12"), null);
      Assert.assertEquals("Anzahl der Zip-Einträge mit einer Zip-Datei nicht korrekt", 2, cut.getAnzahlZipEntries());
      Assert.assertEquals("On-Close Message falsch", onCloseMessage01, cut.buildOnCloseMessage());

      cut.notifyEntryMatched(null, createZipEntryInfo("file-2", "entry-21"), createMatchStatistics(5));
      cut.notifyEntryMatched(null, createZipEntryInfo("file-2", "entry-22"), createMatchStatistics(7));
      Assert.assertEquals("Anzahl der Zip-Einträge mit zwei Zip-Dateien nicht korrekt", 4, cut.getAnzahlZipEntries());
      Assert.assertEquals("On-Close Message falsch mit zweiter Zip-Datei", onCloseMessage02, cut.buildOnCloseMessage());

      cut.notifyEntryMatched(null, firstZipEntry, null);
      Assert.assertEquals("Eine Erkennung doppelter Einträge in einer Zip-Datei sollte nicht erfolgen", 5, cut.getAnzahlZipEntries());

   }

}
