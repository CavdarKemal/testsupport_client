package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import org.junit.Assert;
import org.junit.Test;

import static de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.ThreadedSearchPerZipResult.StateAtExit.*;

/**
 * Test-Klasse für ThreadedSearchPerZipResult
 */
public class ThreadedSearchPerZipResultTest {

   @Test
   public void testAggregateState() {
      pruefeAggregation(SUCCESS, SUCCESS, null);
      pruefeAggregation(SUCCESS, SUCCESS, SUCCESS);
      pruefeAggregation(ABORTED, SUCCESS, ABORTED);
      pruefeAggregation(ABORTED, ABORTED, null);
      pruefeAggregation(ABORTED, ABORTED, SUCCESS);
      pruefeAggregation(ABORTED, ABORTED, ABORTED);
      pruefeAggregation(ERROR, ERROR, null);
      pruefeAggregation(ERROR, ERROR, SUCCESS);
      pruefeAggregation(ERROR, ERROR, ABORTED);
      pruefeAggregation(ERROR, ERROR, ERROR);
   }

   private void pruefeAggregation(ThreadedSearchPerZipResult.StateAtExit expected,
                                  ThreadedSearchPerZipResult.StateAtExit first,
                                  ThreadedSearchPerZipResult.StateAtExit other) {
      ThreadedSearchPerZipResult.StateAtExit actual = first.aggregateState(other);
      Assert.assertEquals(expected, actual);
   }

}
