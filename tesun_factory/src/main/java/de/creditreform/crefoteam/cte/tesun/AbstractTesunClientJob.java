package de.creditreform.crefoteam.cte.tesun;

import com.google.inject.Module;
import com.google.inject.Provider;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.tesun.configuration.TesunClientModule;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.*;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;

public abstract class AbstractTesunClientJob implements TesunClientJob {
   protected Logger logger;
   protected static String JOB_STARTED_HEADER = "\n*************  User-Task '%s' wurde für die Phase %s gestartert.  *************\n";
   protected static String JOB_ENDED_HEADER =  "\n-------------  User-Task '%s' wurde für die Phase %s abgeschlossen.  -------------\n";
   private final String command;
   private final String description;
   protected final TesunClientJobListener tesunClientJobListener;

   protected AbstractTesunClientJob(final String command, final String description, TesunClientJobListener tesunClientJobListener) {
      this.command = command;
      this.description = description;
      this.tesunClientJobListener = tesunClientJobListener;
      logger = LoggerFactory.getLogger(this.getClass());
   }

   protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
      if (tesunClientJobListener != null) {
         tesunClientJobListener.notifyClientJob(level, notifyInfo);
      }
   }

   protected void printHeader(Level level, String command, TestSupportClientKonstanten.TEST_PHASE testPhase) {
      notifyTesunClientJobListener( level, String.format(JOB_STARTED_HEADER, command, testPhase) );
   }

   protected void printFooter(Level level, String command, TestSupportClientKonstanten.TEST_PHASE testPhase) {
      notifyTesunClientJobListener( level, String.format(JOB_ENDED_HEADER, command, testPhase) );
   }

   public TesunConfigInfo initTesunConfig(EnvironmentConfig environmentConfig) throws Exception {
      try {
         TesunConfigInfo tesunConfigInfo;
         boolean isLocal = environmentConfig.useLocalExports();
         if (isLocal) {
            File localExportsFile = new File(environmentConfig.getTestOutputsRoot(), "EXPORTS");
            tesunConfigInfo = TesunConfigInfoUtils.buildTesunConfigInfoFromDir(localExportsFile.getAbsolutePath());
         } else {
            TesunRestService tesunRestService = new TesunRestService(environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), tesunClientJobListener);
            tesunConfigInfo = tesunRestService.getTesunConfigInfo();
         }
         return tesunConfigInfo;
      } catch (Exception ex) {
         notifyTesunClientJobListener(Level.ERROR, ex.getMessage());
         throw new TestSupportConfigurationException(ex.getMessage());
      }
   }

   @Override
   public String getJobCommandName() {
      return command;
   }

   @Override
   public String getJobCommandDescription() {
      return description;
   }

   @Override
   public Module getGuiceModule(Charset charset, Provider<TestSupportMutableState> mutableStateProvider) {
      return new TesunClientModule(charset, mutableStateProvider);
   }
}
