package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorIF;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorRecursive;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class XmlMatcherCrefoTest extends ExportedZipsSearcherBaseTest
{

  @Test public void testXmlStreamProcessorWithXmlMatcherCrefo() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    // Tagname für Crefonummer ist falsch ==> kein Treffer
    XmlMatcherCrefo xmlMatcherCrefo = new XmlMatcherCrefo( new MatcherParameterTag("crefonummerX") );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherCrefo, "test.log" );
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertFalse( isMatch );

    // Stream reset!
    inputStream.reset();

    xmlMatcherCrefo = new XmlMatcherCrefo( new MatcherParameterTag("crefonummer") );
    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherCrefo, "test.log" );
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream close!
    inputStream.close();
  }

}
