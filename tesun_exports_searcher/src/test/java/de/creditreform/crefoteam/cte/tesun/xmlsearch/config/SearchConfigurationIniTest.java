package de.creditreform.crefoteam.cte.tesun.xmlsearch.config;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.SearchSpecification;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class SearchConfigurationIniTest
{

  private static SearchSpecification createZipSearchData(String name, String[] lines )
  {
    return new SearchSpecification(name, Collections.unmodifiableList(Arrays.asList(lines ) ) );
  }

  private static Map<String, SearchSpecification> refMap = new TreeMap<String, SearchSpecification>()
                                                                  {
                                                                    {
                                                                      put( "TST_BDR_DELTA_2014-10-06_07-00",
                                                                          createZipSearchData( "TST_BDR_DELTA_2014-10-06_07-00", new String []
                                                                          {
                                                                          "SOURCE=src/test/resources/exports/bedirect/export/delta/2014-10-06_07-00",
                                                                          "CREFO_TAGNAME=crefonummer",
                                                                          "SEARCH1=vorname=timmi tammi",
                                                                          "SEARCH2=beteiligt-seit=jahr:2009",
                                                                          "SEARCH3=vorname=Thomas",
                                                                          "SEARCH4=registernummer=1003[0..9]{2}",
                                                                          "SEARCH5=rechtsform.bezeichnung=.*",
                                                                          "SEARCH6=auskunft-kennzeichen.gesperrt.bezeichnung=.*",
                                                                          "SEARCH7=rechtsform.bezeichnung=Limited" } ) );
                                                                    }
                                                                    {
                                                                      put( "TST_BVD_DELTA_2014-10-06_09-04",
                                                                          createZipSearchData(
                                                                              "TST_BVD_DELTA_2014-10-06_09-04",
                                                                              new String []
                                                                              { "SOURCE=src/test/resources/exports/bvd/export/delta/2014-10-06_09-04",
                                                                              "CREFO_TAGNAME=crefonummer",
                                                                              "SEARCH1=branchencode=4520[0..9]",
                                                                              "SEARCH2=firma-register.bezeichnung-rechtsform=Gewerbebetrieb" } ) );
                                                                    }
                                                                    {
                                                                      put( "TST_CEF_DELTA_2014-10-13_07-00",
                                                                          createZipSearchData(
                                                                              "TST_CEF_DELTA_2014-10-13_07-00",
                                                                              new String []
                                                                              { "SOURCE=src/test/resources/exports/cef/export/delta/2014-10-13_07-00",
                                                                              "CREFO_TAGNAME=crefo-number",
                                                                              "SEARCH1=house-number=2[4..9]",
                                                                              "SEARCH2=street=Saarbrückener.*" } ) );
                                                                    }
                                                                    {
                                                                      put( "TST_CTC_DELTA_2014-10-12_07-00",
                                                                          createZipSearchData(
                                                                              "TST_CTC_DELTA_2014-10-12_07-00",
                                                                              new String []
                                                                              { "SOURCE=src/test/resources/exports/ctc/export/delta/2014-10-12_07-00",
                                                                              "CREFO_TAGNAME=crefonummer",
                                                                              "SEARCH1=anzahlTelefonauskuenfte=[2..9]",
                                                                              "SEARCH2=street=adressbereich=0[6..9]",
                                                                              "SEARCH3=house-number=1[4..9]" } ) );
                                                                    }
                                                                    {
                                                                      put( "TST_DRD_DELTA_2014-10-13_03-00",
                                                                          createZipSearchData(
                                                                              "TST_DRD_DELTA_2014-10-13_03-00",
                                                                              new String []
                                                                              { "SOURCE=src/test/resources/exports/drd/export/delta/2014-10-13_03-00",
                                                                              "CREFO_TAGNAME=crefonummer",
                                                                              "SEARCH1=rechtsformKurzname=Gewerbebetrieb",
                                                                              "SEARCH2=street=rechtsformKurzname=OHG" } ) );
                                                                    }
                                                                    {
                                                                      put( "TST_VSD_DELTA_2014-10-14_07-00",
                                                                          createZipSearchData(
                                                                              "TST_VSD_DELTA_2014-10-14_07-00",
                                                                              new String []
                                                                              { "SOURCE=src/test/resources/exports/vsd/export/delta/2014-10-14_07-00",
                                                                              "CREFO_TAGNAME=crefonummer",
                                                                              "SEARCH1=beteiligteneigenschaft=Inhaber",
                                                                              "SEARCH2=bezeichnung-erste-rechtsform=Gewerbebetrieb",
                                                                              "SEARCH3=beteiligt-seit=jahr:20[0..9]{2}" } ) );
                                                                    }
                                                                  };

  private void check( String configFileName ) throws ConfigurationException, URISyntaxException
  {
    URL resourceURL = ExportedZipsSearcherBaseTest.class.getResource( configFileName );
    SearchConfiguration searchConfiguration = SearchConfigurationFactory.createSearchConfiguration( resourceURL.toURI().getPath() );
    Assert.assertNotNull( searchConfiguration );
    Map<String, SearchSpecification> zipSearcDataMap = searchConfiguration.getZipSearcDataMap();
    Assert.assertNotNull( zipSearcDataMap );
    Assert.assertEquals( zipSearcDataMap.size(), 6 );
    Iterator<String> iterator = zipSearcDataMap.keySet().iterator();
    while( iterator.hasNext() )
    {
      String strSection = iterator.next();
      SearchSpecification tstSearchSpecification = zipSearcDataMap.get(strSection );
      Assert.assertNotNull(tstSearchSpecification);

      Assert.assertTrue( refMap.containsKey( strSection ) );
      SearchSpecification refSearchSpecification = refMap.get(strSection );
      Assert.assertNotNull(refSearchSpecification);

      Assert.assertEquals(refSearchSpecification.getName(), tstSearchSpecification.getName() );
      Assert.assertEquals(refSearchSpecification.getCrefoNrTagName(), tstSearchSpecification.getCrefoNrTagName() );
      Assert.assertEquals(refSearchSpecification.getSourceFile(), tstSearchSpecification.getSourceFile() );
      Assert.assertEquals(refSearchSpecification.getSearchCriteriasList().size(), tstSearchSpecification.getSearchCriteriasList().size() );
    }
  }

  @Test public void testGetZipSearcDataMapgetMap() throws ConfigurationException, URISyntaxException
  {
    check( "/TST_SearchItems.properties" );
  }

}
