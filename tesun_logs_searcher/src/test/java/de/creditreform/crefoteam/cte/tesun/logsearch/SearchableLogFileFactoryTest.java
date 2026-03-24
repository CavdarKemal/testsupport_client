package de.creditreform.crefoteam.cte.tesun.logsearch;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SearchableLogFileFactoryTest
{
  @Before
  public void setUp()
  {
  }

  @After
  public void tearDown()
  {

  }

  @Test
  public void testFactory() throws URISyntaxException
  {
    String logFileName = "";
    try
    {
      SearchableLogFileFactory.createInstanceFor( logFileName );
      Assert.fail( "UnsupportedOperationException expected!" );
    }
    catch (UnsupportedOperationException ex)
    {
      Assert.assertEquals( "Typ der Log-Datei '' unbekannt!", ex.getMessage() );
    }
    catch (FileNotFoundException ex)
    {
      Assert.fail( ex.getMessage() );
    }

    logFileName = "blabla.xyz";
    try
    {
      SearchableLogFileFactory.createInstanceFor( logFileName );
      Assert.fail( "UnsupportedOperationException expected!" );
    }
    catch (UnsupportedOperationException ex)
    {
      Assert.assertEquals( "Typ der Log-Datei 'blabla.xyz' unbekannt!", ex.getMessage() );
    }
    catch (FileNotFoundException ex)
    {
      Assert.fail( ex.getMessage() );
    }

    logFileName = SearchableLogFileFactoryTest.class.getResource( "/all_flux_out.log").toURI().getPath();
    try
    {
      SearchableLogFile logFile = SearchableLogFileFactory.createInstanceFor( logFileName );
      Assert.assertNotNull( logFile );
      Assert.assertTrue( logFile instanceof NormalLogFile );
    }
    catch (UnsupportedOperationException ex)
    {
      Assert.fail( ex.getMessage() );
    }
    catch (FileNotFoundException ex)
    {
      Assert.fail( ex.getMessage() );
    }

    logFileName = SearchableLogFileFactoryTest.class.getResource( "/all_flux_out.log.1.gz").toURI().getPath();
    try
    {
      SearchableLogFile logFile = SearchableLogFileFactory.createInstanceFor( logFileName );
      Assert.assertNotNull( logFile );
      Assert.assertTrue( logFile instanceof ZippedLogFile );
    }
    catch (UnsupportedOperationException ex)
    {
      Assert.fail( ex.getMessage() );
    }
    catch (FileNotFoundException ex)
    {
      Assert.fail( ex.getMessage() );
    }
  }

}
