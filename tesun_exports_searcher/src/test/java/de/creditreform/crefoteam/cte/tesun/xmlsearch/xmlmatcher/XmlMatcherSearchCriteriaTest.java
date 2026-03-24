package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.XmlSearchCursor;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorIF;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorRecursive;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IPerEntryListener;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;

public class XmlMatcherSearchCriteriaTest extends ExportedZipsSearcherBaseTest
{

  @Test public void testXmlStreamProcessorWithXmlMatcherSearchCriteria() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    // Tagname für ist falsch ==> kein Treffer
    XmlMatcherSearchCriteria xmlMatcherSearchCriteria = new XmlMatcherSearchCriteria(new MatcherParameterTag( "non-existent-tag"), "55555555" );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherSearchCriteria, "non-existent-tag.log" );
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertFalse( isMatch );

    // Stream reset!
    inputStream.reset();

    // Tagname ist ok, wert ok ==> ein Treffer
    xmlMatcherSearchCriteria = new XmlMatcherSearchCriteria( new MatcherParameterTag("crefonummer"), "1234567890" );
    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherSearchCriteria, "crefonummer.log" );
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream reset!
    inputStream.reset();

    // Tagname ist ok, wert ok ==> ein Treffer
    xmlMatcherSearchCriteria = new XmlMatcherSearchCriteria( new MatcherParameterTag("plz"), "48488" );
    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherSearchCriteria, "plz.log" );
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream close!
    inputStream.close();
  }

  @Test public void testXmlStreamProcessorWithEmptyXmlMatcherList() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    try
    {
      new XmlMatcherLogicOr( new ArrayList<>() );
      Assert.fail( "IllegalArgumentException expected!" );
    }
    catch (IllegalArgumentException ex)
    {
      Assert.assertTrue( true );
    }

    try
    {
      new XmlMatcherLogicAnd();
      Assert.fail( "IllegalArgumentException expected!" );
    }
    catch (IllegalArgumentException ex)
    {
      Assert.assertTrue( true );
    }

    // Stream close!
    inputStream.close();
  }

  @Test public void testXmlStreamProcessorWithXmlMatcherGrabEverthing() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    XmlMatcherLogicOr xmlMatcherLogicOr = new XmlMatcherLogicOr( new XmlMatcherGrabEverything(), new XmlMatcherCrefo( new MatcherParameterTag("crefonummer") ) );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicOr, "1234567890.log" );
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream reset!
    inputStream.reset();

    xmlMatcherLogicOr = new XmlMatcherLogicOr( new XmlMatcherGrabEverything(), new XmlMatcherCrefo( new MatcherParameterTag("crefonummer") ) );
    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicOr, "test.log" );
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream close!
    inputStream.close();
  }

  //<gruendung genauigkeit="EXAKT" tag="12" monat="6" jahr="2009"/>
  @Test public void testXmlStreamProcessorWithAttributedXmlMatcher() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    XmlMatcherLogicOr xmlMatcherLogicOr = new XmlMatcherLogicOr( new XmlMatcherGrabEverything(), new XmlMatcherSearchCriteria(new MatcherParameterTag( "gruendung"), "jahr:2009" ) );
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicOr, "test.log" );
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream reset!
    inputStream.reset();

    xmlMatcherLogicOr = new XmlMatcherLogicOr( new XmlMatcherSearchCriteria( new MatcherParameterTag("gruendung"), "jahr:2009" ), new XmlMatcherGrabEverything() );
    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicOr, "test.log" );
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream close!
    inputStream.close();
  }

  static class XmlMatcherGrabEverything implements XmlMatcher
  {
    @Override public XmlMatcherGrabEverything matchCursor(XmlSearchCursor childCursor, IPerEntryListener perEntryListener) throws XMLStreamException
    {
      childCursor.getElemStringValue();
      return this;
    }

    @Override
    public void notifyExitElement(XmlSearchCursor childCursor, IPerEntryListener perEntryListener) throws XMLStreamException {
      // intentionally empty
    }

    @Override public boolean isSatisfied()
    {
      return false;
    }

    @Override public XmlMatcher reset()
    {
      return this;
    }

    @Override
    public void notifyZipEntryCompleted(boolean success, IPerEntryListener perEntryListener) {
      // intentionally empty
    }

  }

  @Test
  public void testInternalMatcherValue() throws XMLStreamException {
    XmlMatcherSearchCriteria.InternalMatcherValue cutExact = new XmlMatcherSearchCriteria.InternalMatcherValue("bla-tag", "exact-match");
    pruefeMatchValue(cutExact, "exact-match", true);
    pruefeMatchValue(cutExact, "no-exact-match", false);
    XmlMatcherSearchCriteria.InternalMatcherValue cutVielleicht = new XmlMatcherSearchCriteria.InternalMatcherValue("vielleicht-tag", "ja|nein");
    pruefeMatchValue(cutVielleicht, "ja", true);
    pruefeMatchValue(cutVielleicht, "nein", true);
    pruefeMatchValue(cutVielleicht, "vielleicht", false);
    pruefeMatchValue(cutVielleicht, "ja|nein", false);

    XmlMatcherSearchCriteria.InternalMatcherValue cutContains = new XmlMatcherSearchCriteria.InternalMatcherValue("contains", "[a-zA-Z-]*ja[a-zA-Z-]*");
    pruefeMatchValue(cutContains, "ja", true);
    pruefeMatchValue(cutContains, "hmmm-ja-vielleicht", true);
    pruefeMatchValue(cutContains, "hmmm-vielleicht", false);

    XmlMatcherSearchCriteria.InternalMatcherValue cutHinterlegung = new XmlMatcherSearchCriteria.InternalMatcherValue("contains", "[a-zA-Z- (]*Hinterlegung[a-zA-Z- )]*");
    pruefeMatchValue(cutHinterlegung, "eBundesanzeiger (Hinterlegung)", true);

  }

  protected void pruefeMatchValue(XmlMatcherSearchCriteria.InternalMatcher cut, String elementValue, boolean expectMatch) throws XMLStreamException {
    XmlSearchCursor matcherContextValue = createMatcherContextValue(elementValue, expectMatch);
    IPerEntryListener perEntryListener = createPerEntryListener(expectMatch);
    Assert.assertEquals(elementValue, expectMatch, cut.isMatch(matcherContextValue, perEntryListener));
    EasyMock.verify(matcherContextValue);
  }

  private IPerEntryListener createPerEntryListener(boolean expectMatch) {
    IPerEntryListener mockListener = EasyMock.createMock(IPerEntryListener.class);
    if (expectMatch) {
      mockListener.addMatchingCriterion(EasyMock.anyString());
      EasyMock.expectLastCall().once();
    }
    EasyMock.replay(mockListener);
    return mockListener;
  }

  private XmlSearchCursor createMatcherContextValue(String elementValue, boolean expectMatch) throws XMLStreamException {
    XmlSearchCursor mock = EasyMock.createMock(XmlSearchCursor.class);
    EasyMock.expect(mock.getElemStringValue()).andReturn(elementValue).anyTimes();
    EasyMock.replay(mock);
    return mock;
  }

}
