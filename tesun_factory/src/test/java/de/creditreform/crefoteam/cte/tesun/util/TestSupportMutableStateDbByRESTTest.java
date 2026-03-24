package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.clzinfo.TesunClzInfo;
import de.creditreform.crefoteam.cte.tesun.TestFallTestBase;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import org.apache.log4j.Level;
import org.junit.Test;

public class TestSupportMutableStateDbByRESTTest  extends TestFallTestBase implements TesunClientJobListener {

   @Test
   public void testIt() throws PropertiesException {
      RestInvokerConfig restServiceConfigTesun = setupUtil.getEnvironmentConfig().getRestServiceConfigsForMasterkonsole().get(0);
      TesunRestService tesunRestService = new TesunRestService(restServiceConfigTesun, this);
      TesunClzInfo clzInfo = tesunRestService.getClzInfo(412);
      clzInfo.getNumCrefos();
   }

   @Override
   public void notifyClientJob(Level level, Object notifyObject) {

   }

   @Override
   public Object askClientJob(ASK_FOR askFor, Object userObject) {
      return null;
   }
}


