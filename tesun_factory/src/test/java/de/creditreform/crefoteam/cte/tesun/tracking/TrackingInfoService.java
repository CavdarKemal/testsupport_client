package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.CteCrefoTrackingErgebnis;
import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.RestInvokerApache4;
import de.creditreform.crefoteam.cte.rest.RestInvokerResponse;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import org.apache.log4j.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class TrackingInfoService {

   Map<String, String> envToUrlMap = new HashMap<String, String>() {{
      put("ENE", "http://rhsctem015.ecofis.de:7051");
      put("GEE", "http://jvm02.gee.verband.creditreform.de:7051");
      put("ABE", "http://jvm02.abe.verband.creditreform.de:7051");
      put("PRE", "http://jvm02.pre.verband.creditreform.de:7051");
   }};
   private final RestInvoker restServiceInvoker;
   private final TesunClientJobListener tesunClientJobListener;

   protected TrackingInfoService(String umgebung, TesunClientJobListener tesunClientJobListener) {
      this.tesunClientJobListener = tesunClientJobListener;
      restServiceInvoker = new RestInvokerApache4(envToUrlMap.get(umgebung), "", "");
   }

   // http://jvm02.pre.verband.creditreform.de:7051/backend/crefotracking/9370023875
   public CteCrefoTrackingErgebnis getCrefoTrackingErgebnis(Long crefo) {
      final String SERVICE_URL = "/backend/crefotracking/" + crefo;
      try {
         restServiceInvoker.init(10000);
         restServiceInvoker.appendPath(SERVICE_URL);
         tesunClientJobListener.notifyClientJob(Level.INFO, String.format("AbstractTrackingInfo#getExportTrackingInfo:: %s", restServiceInvoker.buildURI()));
         RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
         String responseBody = response.getResponseBody();
         Unmarshaller unmarshaller = JAXBContext.newInstance(CteCrefoTrackingErgebnis.class.getPackage().getName()).createUnmarshaller();
         CteCrefoTrackingErgebnis cteCrefoTrackingErgebnis = (CteCrefoTrackingErgebnis) unmarshaller.unmarshal(new StringReader(responseBody));
         return cteCrefoTrackingErgebnis;
      } catch (Exception ex) {
         throw new RuntimeException("getExportTrackingInfo()", ex);
      } finally {
         restServiceInvoker.close();
      }
   }

}
