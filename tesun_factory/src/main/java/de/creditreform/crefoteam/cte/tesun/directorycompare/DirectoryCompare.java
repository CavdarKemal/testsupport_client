package de.creditreform.crefoteam.cte.tesun.directorycompare;

import com.google.inject.Module;
import com.google.inject.Provider;
import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.configuration.TesunClientModule;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;
import org.apache.log4j.Level;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

/**
 * Implementierung von {@link TesunClientJob} für den vergleich zweier
 * Verzeichnisse
 * User: ralf
 * Date: 11.06.14
 * Time: 11:01
 */
public class DirectoryCompare extends AbstractTesunClientJob
{
  public static final String      COMMAND     = "directorycompare";
  public static final String      DESCRIPTION = "compare two directories and content of Zip-Files";
                                              
  @Inject
  private DirectoryCompareFolders folders;
                                  
  @Inject
  private DiffListener            listener;
                                  
  @Inject
  private ZipContentCompare       zipContentCompare;
                                  
  private String                  dirFirst;
  private String                  dirSecond;
  private String                  dirResult;
  private boolean                 extractFirst;
  private boolean                 extractSecond;
                                  
  public DirectoryCompare( TesunClientJobListener tesunClientJobListener )
  {
    super( COMMAND, DESCRIPTION, tesunClientJobListener );
  }
  
  @Override
  public void init(EnvironmentConfig envConfig) throws Exception
  {
    Properties properties = envConfig.getDirectoryCompareProperties();
    String parentPath = properties.getProperty("PROPES_FILE_PATH");

    dirFirst = new File(parentPath, properties.getProperty(TestSupportClientKonstanten.OPT_DIRECTORY_TO_COMPARE_FIRST)).getPath();
    dirSecond = new File(parentPath, properties.getProperty(TestSupportClientKonstanten.OPT_DIRECTORY_TO_COMPARE_SECOND)).getPath();
    dirResult = new File(parentPath, properties.getProperty(TestSupportClientKonstanten.OPT_DIRECTORY_COMPARE_RESULTS)).getPath();
    String propValue = properties.getProperty(TestSupportClientKonstanten.OPT_COMPARE_EXTRACT_FIRST);
    extractFirst = propValue.equals("1") || propValue.equals("true");
    propValue = properties.getProperty(TestSupportClientKonstanten.OPT_COMPARE_EXTRACT_FIRST);
    extractSecond = propValue.equals("1") || propValue.equals("true");
  }
  
  @Override
  public Module getGuiceModule( Charset charset, Provider<TestSupportMutableState> mutableStateProvider )
  {
    return new TesunClientModule( charset, mutableStateProvider );
  }
  
  @Override
  public JOB_RESULT call() throws Exception
  {
    notifyTesunClientJobListener(Level.INFO, String.format( "\nVergleich der Verzeichnisse: \n1. %s\n2 %s", dirFirst, dirSecond));
    // Verzeichnisse prüfen und/oder anlegen
    listener.init( extractFirst, extractSecond );
    folders.init( dirFirst, dirSecond, dirResult );
    try
    {
      // Verzeichnisse durchsuchen
      DirectoryScanner scanner = new DirectoryScanner();
      Map<String, DirectoryScanResult> mapFirst = scanner.scanDirectory( folders.getFirstDir() );
      Map<String, DirectoryScanResult> mapSecond = scanner.scanDirectory( folders.getSecondDir() );
      String currentDirectoryBasedNamePrefix = "/";
      for( Map.Entry<String, DirectoryScanResult> e : mapFirst.entrySet() )
      {
        if( !currentDirectoryBasedNamePrefix.equals( e.getValue().getDirectoryBasedNamePrefix() ) )
        {
          listener.notifyPathChange( e.getValue() );
          currentDirectoryBasedNamePrefix = e.getValue().getDirectoryBasedNamePrefix();
        }
        DirectoryScanResult otherResult = mapSecond.remove( e.getKey() );
        if( otherResult == null )
        {
          listener.firstOnly( e.getValue() );
        }
        else if( otherResult.getFile().getName().endsWith( ".zip" ) )
        {
          zipContentCompare.compareZipFiles( e.getKey(), e.getValue(), otherResult );
        }
        else
        {
          listener.notCompared( e.getValue(), otherResult );
        }
      }
      // in mapSecond verbliebene Dateien existieren nur im zweiten Verzeichnis
      for( DirectoryScanResult o : mapSecond.values() )
      {
        listener.secondOnly( o );
      }
    }
    finally
    {
      listener.close();
      folders.close();
    }
    return JOB_RESULT.OK;
  }
  
}
