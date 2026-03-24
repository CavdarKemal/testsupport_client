package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import java.io.InputStream;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorRecursive;
import org.junit.Assert;
import org.junit.Test;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorIF;

public class XmlMatcherFindFirstTest extends ExportedZipsSearcherBaseTest
{

  @Test public void testXmlStreamProcessorWithXmlMatcherFindFirst() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    XmlMatcherFindFirst xmlMatcherFindFirst = new XmlMatcherFindFirst(new XmlMatcherSearchCriteria( new MatcherParameterTag("bezeichnung"), "Bezeichnung der Adresse" ));
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherFindFirst, "bezeichnung.log");
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream reset!
    inputStream.reset();

    xmlMatcherFindFirst = new XmlMatcherFindFirst(new XmlMatcherSearchCriteria( new MatcherParameterTag("bezeichnung"), "Bezeichnung der Firmendaten" ));
    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherFindFirst, "bezeichnung.log");
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream close!
    inputStream.close();
  }

}
