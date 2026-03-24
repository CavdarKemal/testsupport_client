package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IGroupByRow;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test-Klasse für {@link GroupByRowDefaultImpl}
 */
public class GroupByRowDefaultImplTest {

   @Test
   public void testEquals() {
      GroupByRowDefaultImpl first = new GroupByRowDefaultImpl("1", null, "3", "4");
      GroupByRowDefaultImpl other = new GroupByRowDefaultImpl("1", null, "3", "4");
      Assert.assertTrue(first.equals(other));
   }

   @Test
   public void testCompare() {
      GroupByRowDefaultImpl first = new GroupByRowDefaultImpl("1", null, "3");
      pruefeVergleich(1, first, "1", null); // other ist kürzer
      pruefeVergleich(-1, first, "1", null, "3", "4"); // other ist länger
      pruefeVergleich(-1, first, "1", null, "4"); // other ist gleich lang, größer
      pruefeVergleich(1, first, "1", null, "2"); // other ist gleich lang, kleiner
      pruefeVergleich(-1, first, "1", "2", "3"); // other ist gleich lang, "2">null
      pruefeVergleich(1, first, null, null, "3"); // other ist gleich lang, null<"1"
   }

   private void pruefeVergleich(int expectedSign, IGroupByRow first, String... otherComponents) {
      GroupByRowDefaultImpl other = new GroupByRowDefaultImpl(otherComponents);
      int cmp = first.compareTo(other);
      int sign = sign(cmp);
      if (sign !=expectedSign) {
         Assert.assertEquals("Abweichung "+first.getComponentsOfKey()+"<->"+other.getComponentsOfKey(), expectedSign, sign);
      }
   }

   private int sign(int value) {
      if (value>0) {
         return 1;
      }
      else if (value==0) {
         return 0;
      }
      else {
         return -1;
      }
   }

}
