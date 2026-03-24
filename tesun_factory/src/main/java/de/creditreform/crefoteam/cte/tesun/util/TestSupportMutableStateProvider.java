package de.creditreform.crefoteam.cte.tesun.util;

import com.google.inject.Provider;

/**
 * {@link Provider} für eine Instanz von {@link TestSupportMutableState}.
 * Durch den Einsatz dieser Klasse treten Fehler bzgl. der Status-Information
 * nur dann auf, wenn diese auch vom Client benötigt wird.
 * User: ralf
 * Date: 25.02.14
 * Time: 09:38
 */
public class TestSupportMutableStateProvider implements Provider<TestSupportMutableState> {
   private final EnvironmentConfig environmentConfig;

   public TestSupportMutableStateProvider(EnvironmentConfig environmentConfig) {
      this.environmentConfig = environmentConfig;
   }

   @Override
   public TestSupportMutableState get() {
/*
      File configFile = environmentConfig.getConfigFile();
      String stateFileName = configFile.getAbsolutePath().replace(TestSupportClientKonstanten.PATH_PREFIX_CONFIG, TestSupportClientKonstanten.PATH_PREFIX_STATE);
      return TestSupportMutableStateFile.forFile(configuration.getCharset(), stateFileName);
*/
      return new TestSupportMutableStateDbByREST(environmentConfig);
   }

}
