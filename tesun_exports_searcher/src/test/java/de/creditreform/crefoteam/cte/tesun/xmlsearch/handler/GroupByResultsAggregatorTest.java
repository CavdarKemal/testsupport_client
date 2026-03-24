package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IGroupByRow;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test-Klasse für {@link GroupByResultsAggregator}
 */
public class GroupByResultsAggregatorTest {

   protected Map<String, Integer> createCountersMap() {
      Map<String, Integer> countersMap = new HashMap<>();
      countersMap.put("Value-4.0", 0);
      countersMap.put("Value-4.1", 1);
      countersMap.put("Value-4.2", 3);
      countersMap.put("Value-4.3", 7);
      return countersMap;
   }

   protected GroupByResultsAggregator createAggregator() {
      GroupByResultsAggregator cut = new GroupByResultsAggregator();
      cut.addSingle("Value-1").addSingle(null).addSingle("Value-3");
      return cut;
   }

   private void pruefeInhalt(int erwarteteAnzahl, Map<IGroupByRow, Integer> resultMap, String... keyFragments) {
      IGroupByRow mapKey = new GroupByRowDefaultImpl(keyFragments);
      if (erwarteteAnzahl<0) {
         Assert.assertFalse("Key sollte nicht enthalten sein: "+mapKey, resultMap.containsKey(mapKey));
      }
      else {
         Assert.assertTrue("Key fehlt, sollte aber enthalten sein: "+mapKey, resultMap.containsKey(mapKey));
         Integer actual = resultMap.get(mapKey);
         Assert.assertEquals("Anzahl weicht ab für: "+mapKey, erwarteteAnzahl, actual.intValue());
      }
   }

   @Test
   public void testBuildMap() {
      GroupByResultsAggregator cut = createAggregator();
      Map<String, Integer> countersMap = createCountersMap();
      Map<IGroupByRow, Integer> resultMap = cut.setCounters(countersMap);
      Assert.assertEquals("Größe der Ergebnis-Map ungleich Anzahl gezählter Ausprägungen", countersMap.size(), resultMap.size());
      pruefeInhalt(0, resultMap, "Value-1", null, "Value-3", "Value-4.0");
      pruefeInhalt(1, resultMap, "Value-1", null, "Value-3", "Value-4.1");
      pruefeInhalt(3, resultMap, "Value-1", null, "Value-3", "Value-4.2");
      pruefeInhalt(7, resultMap, "Value-1", null, "Value-3", "Value-4.3");
      pruefeInhalt(-1, resultMap, "Value-1b", null, "Value-3", "Value-4.1");
      pruefeInhalt(-1, resultMap, "Value-1", "not-null", "Value-3", "Value-4.1");
   }

   @Test
   public void testJoinMaps() {
      GroupByResultsAggregator cut = createAggregator();
      // Erzeuge die erste Ergebnis-Menge, und zwar eine Kopie davon (!)
      Map<String, Integer> countersMap1 = createCountersMap();
      Map<IGroupByRow, Integer> resultMap1 = new HashMap<>(cut.setCounters(countersMap1));

      Map<String, Integer> countersMap2 = createCountersMap();
      countersMap2.put("Value-X.0", 75);
      Map<IGroupByRow, Integer> resultMap2 = cut.setCounters(countersMap2);

      Map<IGroupByRow, Integer> joinedMap = cut.addCounters(resultMap1);
      Assert.assertEquals("Größe der zusammengeführten Map falsch", Math.max(resultMap1.size(), resultMap2.size()),
                          joinedMap.size());

      pruefeInhalt(0, joinedMap, "Value-1", null, "Value-3", "Value-4.0");
      pruefeInhalt(2, joinedMap, "Value-1", null, "Value-3", "Value-4.1");
      pruefeInhalt(6, joinedMap, "Value-1", null, "Value-3", "Value-4.2");
      pruefeInhalt(14, joinedMap, "Value-1", null, "Value-3", "Value-4.3");
      pruefeInhalt(75, joinedMap, "Value-1", null, "Value-3", "Value-X.0");
      pruefeInhalt(-1, joinedMap, "Value-1b", null, "Value-3", "Value-4.1");
      pruefeInhalt(-1, joinedMap, "Value-1", "not-null", "Value-3", "Value-4.1");

   }

}
