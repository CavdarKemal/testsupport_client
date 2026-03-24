package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.tesun.rest.TesunRestServiceIntegrationTestBase;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class NachverfolgungTest extends TesunRestServiceIntegrationTestBase {

   public NachverfolgungTest() {
      super("");
   }

   @Test
   public void testTrackingExportInfoForBvdOnENE() throws Exception {
//      URL resourceURL = getClass().getResource("/Missing_LU-S.txt");
//      URL resourceURL = getClass().getResource("/Missing_LU-2.txt");
//      URL resourceURL = getClass().getResource("/Missing_LU.txt");
//      URL resourceURL = getClass().getResource("/Missing_MarkusB.txt");
      doTestForEnvAndCustomer("/Missing_LU-1.txt", "ENE", "CEF");
   }

   @Test
   public void testTrackingExportInfoForBvdOnPRE() throws Exception {
//      URL resourceURL = getClass().getResource("/Missing_LU-S.txt");
//      URL resourceURL = getClass().getResource("/Missing_LU-2.txt");
//      URL resourceURL = getClass().getResource("/Missing_LU.txt");
//      URL resourceURL = getClass().getResource("/Missing_MarkusB.txt");
      doTestForEnvAndCustomer("/Missing_LU-1.txt", "PRE", "CEF");
   }


   private void doTestForEnvAndCustomer(String crefosFile, String umgebung, String customer) throws IOException {
      URL resourceURL = getClass().getResource(crefosFile);
      TrackingInfoHandler trackingInfo = new TrackingInfoHandler(umgebung, resourceURL, tesunClientJobListener);
      trackingInfo.dumpTrackingInfo(customer,
         Arrays.asList(
            new TrackingExportInfoDumper(),
            new TrackingRelevanzPruefungInfo(),
            new TrackingErneuteLieferungInfo(),
            new TrackingZeitlicherAblaufInfo(),
            new TrackingImportEventInfo()
         ));
   }

}
