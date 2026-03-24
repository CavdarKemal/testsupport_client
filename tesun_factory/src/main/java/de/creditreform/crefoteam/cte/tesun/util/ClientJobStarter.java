package de.creditreform.crefoteam.cte.tesun.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import de.creditreform.crefoteam.cte.tesun.JobExecutionException;
import de.creditreform.crefoteam.cte.tesun.TesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJob.JOB_RESULT;
import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Utility-Klasse für den Start der Instanzen von {@link TesunClientJob}
 * User: ralf
 * Date: 13.02.14
 * Time: 10:59
 */
public class ClientJobStarter
{
  public static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private final TesunClientJob clientJob;

  public ClientJobStarter(TesunClientJob clientJob) {
    this.clientJob = clientJob;
  }

  public JOB_RESULT startJob(EnvironmentConfig environmentConfig)
  {
    try
    {
      clientJob.init( environmentConfig );
      final Module module = clientJob.getGuiceModule( DEFAULT_CHARSET, new TestSupportMutableStateProvider( environmentConfig ) );
      if( module != null )
      {
        Injector injector = Guice.createInjector( module );
        injector.injectMembers( clientJob );
      }
      return clientJob.call();
    }
    catch (TestSupportConfigurationException e)
    {
      StringBuilder strError = new StringBuilder(e.getMessage());
      if(e.getCause() != null)
      {
        strError.append( "\n" ).append( e.getCause().getMessage() );
      }
      forceInitLogger().error( strError.toString() );
      return JOB_RESULT.ERROR.setUserObject( new JobExecutionException( strError.toString() ) );
    }
    catch (Throwable ex)
    {
      String errMsg = TesunUtilites.buildExceptionMessage( ex, 10 );
      forceInitLogger().error( errMsg );
      return JOB_RESULT.ERROR.setUserObject( new JobExecutionException( errMsg ) );
    }
  }

  protected Logger forceInitLogger()
  {
    SystemOutAppender.INFO().installIntoRootLogger();
    return LoggerFactory.getLogger( ClientJobStarter.class );
  }

}
