package de.creditreform.crefoteam.cte.tesun.relevanz;

import de.creditreform.crefoteam.cte.tesun.rest.TesunRestServiceIntegrationTestBase;
import org.junit.Test;

import java.net.URL;

public class RelevanzPruefTest extends TesunRestServiceIntegrationTestBase {

   public RelevanzPruefTest() {
      super("ENE");
   }

   @Test
   public void test_getCrefoAnaylseInfoForCtcOnPRE() throws Exception {
      URL resourceURL = getClass().getResource("/HR_PPCrefo_CTErelevant_20220214.txt");
      AbstractRelevanzInfo relevanzInfo = new CtcRelevanzInfo("PRE", resourceURL, tesunClientJobListener);
      relevanzInfo.dumpRelevanzMonitoring();
   }

   @Test
   public void test_getCrefoAnaylseInfoForBvdOnPRE() throws Exception {
      URL resourceURL = getClass().getResource("/Missing_LU-1.txt");
//      URL resourceURL = getClass().getResource("/Missing_MarkusB.txt");
      AbstractRelevanzInfo relevanzInfo = new BvdRelevanzInfo("PRE", resourceURL, tesunClientJobListener);
      relevanzInfo.dumpRelevanzMonitoring();
   }

   @Test
   public void test_getCrefoAnaylseInfoForBvdOnABE() throws Exception {
      URL resourceURL = getClass().getResource("/ABE-INSORELEVANZPRUEFERGEBNIS.tsv");
      AbstractRelevanzInfo relevanzInfo = new BvdRelevanzInfo("ABE", resourceURL, tesunClientJobListener);
      relevanzInfo.dumpRelevanzMonitoring();
   }

}
