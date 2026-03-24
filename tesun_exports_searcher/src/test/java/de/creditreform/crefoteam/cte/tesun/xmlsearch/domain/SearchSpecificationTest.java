package de.creditreform.crefoteam.cte.tesun.xmlsearch.domain;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher.XmlMatcher;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher.XmlMatcherLogicNot;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SearchSpecificationTest
{

  @Test public void testConstructWithEmptyData()
  {
    final List<String> linesToParse = new ArrayList<>();

    linesToParse.add( "SOURCE=Blupliops" );
    linesToParse.add( "SEARCH=blip=blup" );

    SearchSpecification searchSpecification = new SearchSpecification("testConstructWithEmptyData", linesToParse );
    Assert.assertNotNull(searchSpecification);
    Assert.assertNotNull(searchSpecification.getSourceFile() );
    Assert.assertFalse(searchSpecification.getSearchCriteriasList().isEmpty() );

    String crefoNrTagName = searchSpecification.getCrefoNrTagName();
    Assert.assertNull( crefoNrTagName );
  }

  @Test public void testConstructWithData()
  {
    final List<String> linesToParse = getSearchSpecification();
    SearchSpecification searchSpecification = new SearchSpecification("testConstructWithData", linesToParse );
    Assert.assertNotNull(searchSpecification);
    Assert.assertEquals(searchSpecification.getSourceFile().getPath().replaceAll("\\\\", "/"), "src/test/resources/exports/bedirect/export/delta/2014-10-06_07-00" );
    List<SearchCriteria> searchCriteriasList = searchSpecification.getSearchCriteriasList();
    Assert.assertEquals( searchCriteriasList.size(), 3 );
    checkEntry( searchCriteriasList.get( 0 ), "beteiligt-seit", "jahr:2009" );
    checkEntry( searchCriteriasList.get( 2 ), "rechtsform.bezeichnung", "Limited" );
    checkEntry( searchCriteriasList.get( 1 ), "vorname", "Thomas" );

    String crefoNrTagName = searchSpecification.getCrefoNrTagName();
    Assert.assertNotNull( crefoNrTagName );
    Assert.assertEquals( crefoNrTagName, "crefonummer" );
  }

  @Test public void testConstructWithInvertedResults()
  {
    checkCreateInvertedResults( "INVERT_RESULTS=true", true );
    checkCreateInvertedResults( "INVERT_RESULTS=false", false );
  }

  protected void checkCreateInvertedResults( String addForInversion, boolean expectInversion )
  {
    final List<String> linesToParse = getSearchSpecification();
    linesToParse.add( addForInversion);
    SearchSpecification searchSpecification = new SearchSpecification("testConstructWithData", linesToParse );
    XmlMatcher generatedMatcher = searchSpecification.getRuntimeSearchSpec().buildXmlMatcherSearch();
    Assert.assertEquals( expectInversion, generatedMatcher instanceof XmlMatcherLogicNot );
  }

  protected List<String> getSearchSpecification()
  {
    final List<String> linesToParse = new ArrayList<>();
    linesToParse.add( "NAME=TST_BDR_DELTA_2014-10-06_07-00" );
    linesToParse.add( "SOURCE=src/test/resources/exports/bedirect/export/delta/2014-10-06_07-00" );
    linesToParse.add( "CREFO_TAGNAME=crefonummer" );
    linesToParse.add( "SEARCH0=beteiligt-seit=jahr:2009" );
    linesToParse.add( "SEARCH1=vorname=Thomas" );
    linesToParse.add( "SEARCH2=rechtsform.bezeichnung=Limited" );
    return linesToParse;
  }

  private void checkException( List<String> linesToParse, String expectedErrStr )
  {
    try
    {
      new SearchSpecification("Dummy", linesToParse );
      Assert.fail( "IllegalArgumentException expected!" );
    }
    catch( IllegalArgumentException ex )
    {
      Assert.assertEquals( ex.getMessage(), expectedErrStr );
    }
  }

  private void checkEntry( SearchCriteria searchCriteria, String expectedPath, String expectedValue )
  {
    Assert.assertNotNull( searchCriteria );
    Assert.assertTrue( searchCriteria.getSearchTag().equals( expectedPath ) );
    Assert.assertTrue( searchCriteria.getSearchValue().equals( expectedValue ) );
  }

}
