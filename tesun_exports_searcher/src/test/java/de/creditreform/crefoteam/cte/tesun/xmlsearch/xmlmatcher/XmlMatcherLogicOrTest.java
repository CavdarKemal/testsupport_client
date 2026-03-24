package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorIF;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorRecursive;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XmlMatcherLogicOrTest extends ExportedZipsSearcherBaseTest
{

  @Test public void testXmlStreamProcessorWithXmlMatcherLogicOr_NoHints() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    List<XmlMatcher> xmlMatchersList = new ArrayList<>();
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("non-existent-tag"), "123456789" ) );
    XmlMatcherLogicOr xmlMatcherLogicOr = new XmlMatcherLogicOr( xmlMatchersList );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicOr, "non-existent-tag.log" );
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertFalse( isMatch );// kein Kriterium, das passt, also kine Treffer!

    // Stream close!
    inputStream.close();
  }

  @Test public void testXmlStreamProcessorWithXmlMatcherLogicOr_SomeHints() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    List<XmlMatcher> xmlMatchersList = new ArrayList<>();
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("non-existent-tag"), "123456789" ) );
    xmlMatchersList.add( new XmlMatcherSearchCriteria( new MatcherParameterTag("plz"), "48488" ) );
    XmlMatcherLogicOr xmlMatcherLogicOr = new XmlMatcherLogicOr( xmlMatchersList );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicOr, "non-existent-tag.log" );
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );// ein Kriterium, das passt, also Treffer!

    // Stream close!
    inputStream.close();
  }

}
