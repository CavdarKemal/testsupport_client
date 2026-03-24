package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.clzinfo.TesunClzInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import org.apache.log4j.Level;

public class TestSupportMutableStateDbByREST implements TestSupportMutableState {

   private final EnvironmentConfig environmentConfig;

   public TestSupportMutableStateDbByREST(EnvironmentConfig environmentConfig) {
      this.environmentConfig = environmentConfig;
   }

   @Override
   public Long getNextCrefo() {
      Integer targetClz = null;
      try {
         targetClz = environmentConfig.getTargetClzForDePseudoCrefos();
         TesunRestService tesunRestService = getTesunRestService();
         TesunClzInfo clzInfo = tesunRestService.getClzInfo(targetClz);
         long maxCrefo = clzInfo.getMaxCrefo();
         if(maxCrefo < 4120000501L) {
            // TODO!
            maxCrefo = 4120000500L;
         }
         return  maxCrefo + 1;
      } catch (Exception ex) {
         throw new RuntimeException(String.format("Fehler beim Ermitteln der höchstbenutzten Crefo für %s!", targetClz), ex);
      }
   }

   private TesunRestService getTesunRestService() throws PropertiesException {
      RestInvokerConfig restServiceConfigTesun = environmentConfig.getRestServiceConfigsForMasterkonsole().get(0);
      TesunRestService tesunRestService = new TesunRestService(restServiceConfigTesun, new TesunClientJobListener() {
         @Override
         public void notifyClientJob(Level level, Object notifyObject) {
         }

         @Override
         public Object askClientJob(ASK_FOR askFor, Object userObject) {
            return null;
         }
      });
      return tesunRestService;
   }
}
