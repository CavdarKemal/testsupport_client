package de.creditreform.crefoteam.cte.tesun.relevanz;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzDecisionMonitoring;
import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzMonitoringErgebnis;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.TestFallFileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractRelevanzInfo {

   Map<String, String> envToUrlMap = new HashMap<String, String>() {{
      put("ENE", "http://rhsctem015.ecofis.de:7051");
      put("GEE", "http://jvm02.gee.verband.creditreform.de:7051");
      put("ABE", "http://jvm02.abe.verband.creditreform.de:7051");
      put("PRE", "http://jvm02.pre.verband.creditreform.de:7051");
   }};

   private final String umgebung;
   private final String customerKey;
   private final String header;
   private final URL resourceURL;
   private final RestInvokerConfig restInvokerConfig;
   private final TesunRestService tesunRestService;
   private final TesunClientJobListener tesunClientJobListener;

   protected AbstractRelevanzInfo(String umgebung, String customerKey, String strHead, URL resourceURL, TesunClientJobListener tesunClientJobListener) {
      this.umgebung = umgebung;
      this.customerKey = customerKey;
      this.header = strHead;
      this.resourceURL = resourceURL;
      this.tesunClientJobListener = tesunClientJobListener;
      restInvokerConfig = new RestInvokerConfig(envToUrlMap.get(umgebung), "tesuntestene", "tesuntestene");
      tesunRestService = new TesunRestService(restInvokerConfig, tesunClientJobListener);
   }

   public abstract StringBuilder dumpRelevanzDecisionMonitoring(RelevanzDecisionMonitoring relevanzDecisionMonitoring);

   public String getCustomerKey() {
      return customerKey;
   }

   public String getHeader() {
      return header;
   }

   public void dumpRelevanzMonitoring() throws IOException {
      File outFile = prepareOutputFile();
      File sourceFile = new File(resourceURL.getFile());
      List<Long> crefosList = TestFallFileUtil.readCrefosFromResourceFile(sourceFile);
      if(crefosList.size() > 10) {
         crefosList = crefosList.subList(0, 10);
      }
      String strInfo = String.format("Anlayse gestartet\n\t" +
            "Datum/Zeit:      %s\n\t" +
            "Umgebung:        %s\n\t" +
            "REST-URL:        %s\n\t" +
            "Kunde:           %s\n\t" +
            "Crefo-Liste aus: %s\n\t" +
            "Anzahl Crefos:   %d\n",
         new Date(), umgebung, restInvokerConfig.getServiceURL(), customerKey, resourceURL, crefosList.size());
      tesunClientJobListener.notifyClientJob(Level.INFO, strInfo);
      FileUtils.writeStringToFile(outFile, strInfo, Charset.forName("UTF-8"), false);
      FileUtils.writeStringToFile(outFile, getHeader(), Charset.forName("UTF-8"),true);
      final Collection<List<Long>> result = splitCrefosList(crefosList, 10);
      for (List<Long> chunkList : result) {
         long nanoTimeStart = System.nanoTime();
         tesunClientJobListener.notifyClientJob(Level.INFO, "Verarbeite Liste mit " + chunkList.size() + " Crefos: " + chunkList + "...");
         for (Long crefo : chunkList) {
            RelevanzDecisionMonitoring relevanzDecisionMonitorings = tesunRestService.getCrefoAnaylseInfo(getCustomerKey(), crefo);
            Assert.assertNotNull(relevanzDecisionMonitorings);
            StringBuilder stringBuilder = dumpRelevanzDecisionMonitoring(relevanzDecisionMonitorings);
            FileUtils.writeStringToFile(outFile, stringBuilder.toString(), Charset.forName("UTF-8"),true);
         }
         long nanoTimeEnd = System.nanoTime();
         strInfo = String.format("\tZeitverbrauch: %s ms.\n", ((nanoTimeEnd - nanoTimeStart) / 1000000));
         tesunClientJobListener.notifyClientJob(Level.INFO, strInfo);
      }
   }

   protected StringBuilder dumMonitoringErg(List<RelevanzMonitoringErgebnis> monitoringErgebnisse) {
      StringBuilder stringBuilderMonErg = new StringBuilder();
      stringBuilderMonErg.append("{ ");
      for (RelevanzMonitoringErgebnis relevanzMonitoringErgebnis : monitoringErgebnisse) {
         stringBuilderMonErg.append(relevanzMonitoringErgebnis.getPruefKriterium()).append(":");
         stringBuilderMonErg.append(relevanzMonitoringErgebnis.isRelevant() ? "Ja" : "Nein").append("  ");
      }
      stringBuilderMonErg.append("}");
      return stringBuilderMonErg;
   }

   private Collection<List<Long>> splitCrefosList(List<Long> crefosList, int chunkSize) {
      final AtomicInteger counter = new AtomicInteger();
      final Collection<List<Long>> result = crefosList.stream()
         .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
         .values();
      return result;
   }

   private File prepareOutputFile() {
      URL rscUrl = getClass().getResource("/");
      File outFile = new File(rscUrl.getFile(), "RelevanzDecisionMonitoring-" + umgebung + "-" + customerKey + ".csv");
      outFile.delete();
      tesunClientJobListener.notifyClientJob(Level.INFO, "Ergebnisse werden in derDatei " + outFile.getAbsolutePath() + " gespeichert.");
      return outFile;
   }

}
