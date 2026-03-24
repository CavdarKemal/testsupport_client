package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.CteCrefoTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TestFallFileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class TrackingInfoHandler {
   private final File sourceFile;
   private final String umgebung;
   private final TesunClientJobListener tesunClientJobListener;
   TrackingInfoService trackingInfoService;

   protected TrackingInfoHandler(String umgebung, URL resourceURL, TesunClientJobListener tesunClientJobListener) {
      this.umgebung = umgebung;
      this.sourceFile = new File(resourceURL.getFile());
      this.tesunClientJobListener = tesunClientJobListener;
      trackingInfoService = new TrackingInfoService(umgebung, tesunClientJobListener);
   }

   public void dumpTrackingInfo(String customer, List<AbstractTrackingInfoDumper> trackingInfoDumpersList) throws IOException {
      List<Long> crefosList = TestFallFileUtil.readCrefosFromResourceFile(sourceFile);
      trackingInfoDumpersList.stream().forEach(trackingInfoDumper -> trackingInfoDumper.prepareOutputFile(umgebung, customer, sourceFile, tesunClientJobListener));
      for (Long crefo : crefosList) {
         CteCrefoTrackingErgebnis cteCrefoTrackingErgebnis = trackingInfoService.getCrefoTrackingErgebnis(crefo);
         trackingInfoDumpersList.stream().forEach(trackingInfoDumper -> trackingInfoDumper.dumpCrefoTrackingErgebnisInfo(customer, cteCrefoTrackingErgebnis, tesunClientJobListener));
      }
   }

}
