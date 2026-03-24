package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorIF;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorLinear;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorRecursive;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test-Klasse für {@link XmlMatcherBilanzStatistik}
 */
public class XmlMatcherBilanzStatistikTest
extends ExportedZipsSearcherBaseTest
{
  protected static final String AB30_TAG_BILANZ = "kapitel-bilanzen.unternehmensbilanz";
  protected static final List<String> AB30_TAGS_STICHTAG_QUELLE = Collections.unmodifiableList(Arrays.asList(AB30_TAG_BILANZ+".stichtag",AB30_TAG_BILANZ+".quelle"));

  @Test public void testXmlStreamProcessorWithXmlMatcherBilanzStatistik() throws Exception
  {
    InputStream inputStream = makeInputStream( "/ab30/firma_8150192939_bestand_v3_0.xml" );

    // Tagname für Crefonummer ist falsch ==> kein Treffer
    XmlMatcherBilanzStatistik xmlMatcher = new XmlMatcherBilanzStatistik(AB30_TAG_BILANZ, AB30_TAGS_STICHTAG_QUELLE);
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorLinear(xmlMatcher, "firma_8150192939_bestand_v3_0.log");
    boolean isMatchLinear = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue("Kein Treffer mit XmlStreamProcessorLinear", isMatchLinear);
    Assert.assertEquals("Im Test-XML sollten 9 Bilanzen enthalten sein", 9, xmlMatcher.getMapZwischenErgebnisse().size());
    xmlMatcher.reset();
    Assert.assertEquals("nach dem reset() sollte die Map mit den Zwischen-Ergebnissen leer sein", 0, xmlMatcher.getMapZwischenErgebnisse().size());
    // Stream reset!
    inputStream.reset();

    xmlMatcher = new XmlMatcherBilanzStatistik(AB30_TAG_BILANZ, AB30_TAGS_STICHTAG_QUELLE);
    //
    xmlStreamProcessor = new XmlStreamProcessorRecursive( xmlMatcher, "test.log" );
    boolean isMatchRecursive = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( "Kein Treffer mit XmlStreamProcessorRecursive", isMatchRecursive );

    // Stream close!
    inputStream.close();
  }

}
