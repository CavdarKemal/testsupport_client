package de.creditreform.crefoteam.cte.tesun.xmlsearch.config;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Test;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;

public class SearchConfigurationFactoryTest
{

  @Test
  public void testCreateSearchConfigurationThrowsException()
  {
    String configFileName = null;
    try
    {
      SearchConfigurationFactory.createSearchConfiguration( configFileName );
      Assert.fail( "IllegalArgumentException expected!" );
    }
    catch (ConfigurationException ex)
    {
      Assert.assertTrue( ex.getMessage().equals( "Konfigurationsdateiname darf nicht NULL oder leer sein!" ) );
    }
    configFileName = "";
    try
    {
      SearchConfigurationFactory.createSearchConfiguration( configFileName );
      Assert.fail( "ConfigurationException expected!" );
    }
    catch (ConfigurationException ex)
    {
      Assert.assertTrue( ex.getMessage().equals( "Konfigurationsdateiname darf nicht NULL oder leer sein!" ) );
    }
    
    configFileName = "non-existent-file.properties";
    try
    {
      SearchConfigurationFactory.createSearchConfiguration( configFileName );
      Assert.fail( "ConfigurationException expected!" );
    }
    catch (ConfigurationException ex)
    {
      Assert.assertTrue( ex.getMessage().equals( "Die Konfigurationsdatei '" + configFileName + "' existiert nicht!" ) );
    }
  }
  
  @Test public void testCreateSearchConfiguration_Ini()
  throws ConfigurationException, URISyntaxException {
    String configFileName = "/TST_SearchItems.properties";
    URL resourceURL = ExportedZipsSearcherBaseTest.class.getResource( configFileName);
    SearchConfiguration searchConfiguration = SearchConfigurationFactory.createSearchConfiguration( resourceURL.toURI().getPath() );
    Assert.assertNotNull( searchConfiguration );
  }
  
  @Test public void testCreateSearchConfiguration_Props()
  throws ConfigurationException, URISyntaxException {
    String configFileName = "/TST_SearchItems.properties";
    URL resourceURL = ExportedZipsSearcherBaseTest.class.getResource( configFileName);
    SearchConfiguration searchConfiguration = SearchConfigurationFactory.createSearchConfiguration( resourceURL.toURI().getPath() );
    Assert.assertNotNull( searchConfiguration );
  }

}
