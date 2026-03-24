package de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.config.SearchConfiguration;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.config.SearchConfigurationFactory;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.ProgressListenerWithLogger;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.SearchSpecification;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.XmlStreamListenerGroup;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.ZipSearcResult;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IZipFileInfo;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IZipSearcResult;
import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportedZipsSearcherBaseTest
{
  protected static Logger                             logger   = LoggerFactory.getLogger( ExportedZipsSearcherBaseTest.class );

  protected Map<String, SearchSpecification> zipSearcDataMap;
  protected File                                      searchResultsDir;
  protected String                                    fileName = "/TST_SearchItems.properties";
  protected XmlStreamListenerGroup                    listenerGroup = new XmlStreamListenerGroup(null, new ProgressListenerWithLogger(logger));

  @Before
  public void setUp() throws ConfigurationException, URISyntaxException
  {
    SystemOutAppender.INFO().installIntoRootLogger();
    searchResultsDir = new File( "target/SearchResults" );
    try
    {
      FileUtils.forceMkdir( searchResultsDir );
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
    if( !fileName.startsWith( "/" ) )
    {
      fileName = "/" + fileName;
    }
    URL resourceURL = ExportedZipsSearcherBaseTest.class.getResource( fileName );
    SearchConfiguration searchConfiguration = SearchConfigurationFactory.createSearchConfiguration( resourceURL.toURI().getPath() );
    zipSearcDataMap = searchConfiguration.getZipSearcDataMap();
    if( zipSearcDataMap == null )
    {
      Assert.fail( "Parameter für den Testaufruf sind falsch!" );
    }
  }

  @After
  public void tearDown()
  {
    SystemOutAppender.INFO().removeFromRootLogger();
    try
    {
      FileUtils.deleteDirectory( searchResultsDir );
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }

  protected InputStream makeInputStream( String xmlFileName )
  {
    InputStream copyContentInputStream = getClass().getResourceAsStream( xmlFileName );
    copyContentInputStream.mark( Integer.MAX_VALUE );
    return copyContentInputStream;
  }

  protected void checkZipSearcResult(IZipSearcResult zipSearcResult, ZipSearcResult expectedZipSearcResult )
  {
    Map<Path, IZipFileInfo> zipFileInfoMap = zipSearcResult.getZipFileInfoMap();
    //    for( ZipFileInfo zipFileInfo : zipFileInfoMap )
    //    {
    //      String zipFileName = zipFileInfo.getZipFileName();
    //      logger.info( zipFileName );
    //      File theFile = new File( zipFileName );
    //      Assert.assertTrue( theFile.getPath() + " existiert nicht!", theFile.exists() );
    //
    //      List<ZipEntryInfo> zipEntryInfoList = zipFileInfo.getZipEntryInfoList();
    //      for( ZipEntryInfo zipEntryInfo : zipEntryInfoList )
    //      {
    //        String zipEntryName = zipEntryInfo.getZipEntryName();
    //        logger.info( "\t" + zipEntryName );
    //        String xmlFileName = zipEntryInfo.getResultFileName();
    //        logger.info( "\t" + xmlFileName );
    //        theFile = new File( xmlFileName );
    //        Assert.assertTrue( theFile.getPath() + " existiert nicht!", theFile.exists() );
    //      }
    //    }
  }

  protected void checkTargetSearchResultsDir( String fileName, String... searchTagValuePais ) throws IOException
  {
    File theFile = new File( fileName );
    Assert.assertTrue( theFile.getPath() + " existiert nicht!", theFile.exists() );
    String xmlContent = "";
    if( !searchTagValuePais[0].isEmpty() )
    {
      xmlContent = FileUtils.readFileToString( theFile );
    }

    for( int i = 0; i < searchTagValuePais.length; i++ )
    {
      String pair = searchTagValuePais[i];
      if( pair.isEmpty() )
      {
        File [] filesList = theFile.listFiles();
        Assert.assertEquals( filesList.length, 0 );
      }
      else
      {
        Assert.assertTrue( xmlContent.contains( pair ) );
      }
    }
  }
  
}
