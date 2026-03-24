package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorIF;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorRecursive;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XmlMatcherLogicAndTest extends ExportedZipsSearcherBaseTest
{

  @Test public void testXmlStreamProcessorWithXmlMatcherLogicAnd_NoHints() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    List<XmlMatcher> xmlMatchersList = new ArrayList<>();
    
    // kein Kriterium, das passt, also kine Treffer!
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("non-existent-tag"), "123456789" ) );
    XmlMatcherLogicAnd xmlMatcherLogicAnd = new XmlMatcherLogicAnd( xmlMatchersList );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicAnd, "non-existent-tag");
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertFalse( isMatch );

    inputStream.reset();

    // ein passendes, ein nicht passendes Kriterium, also kine Treffer!
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("plz"), "48488" ) );
    xmlMatcherLogicAnd = new XmlMatcherLogicAnd( xmlMatchersList );
    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicAnd, "plz48488");
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertFalse( isMatch );

    // Stream close!
    inputStream.close();
  }

  @Test public void testXmlStreamProcessorWithXmlMatcherLogicAnd_Hint1() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    List<XmlMatcher> xmlMatchersList = new ArrayList<>();
    
    // zwei passende Kriterien, also kine Treffer!
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("plz"), "48488" ) );
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("crefonummer"), "1234567890" ) );
    XmlMatcherLogicAnd xmlMatcherLogicAnd = new XmlMatcherLogicAnd( xmlMatchersList );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicAnd, "crefonummer.log");
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream close!
    inputStream.close();
  }

  @Test public void testXmlStreamProcessorWithXmlMatcherLogicAnd_Hint2() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    List<XmlMatcher> xmlMatchersList = new ArrayList<>();
    
    // zwei passende Kriterien, also kine Treffer!
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("plz"), "48488" ) );
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("crefonummer"), "1234567890" ) );
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("gruendung"), "jahr:2009" ) );
    XmlMatcherLogicAnd xmlMatcherLogicAnd = new XmlMatcherLogicAnd( xmlMatchersList );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicAnd, "crefonummer.log");
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream close!
    inputStream.close();
  }

}
