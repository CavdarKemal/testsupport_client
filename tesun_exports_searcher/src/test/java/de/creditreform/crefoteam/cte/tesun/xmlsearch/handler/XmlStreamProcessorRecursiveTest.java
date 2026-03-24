package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IPerEntryListener;
import org.codehaus.staxmate.in.SMEvent;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.ISMInputCursor;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher.MatcherParameterTag;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher.XmlMatcher;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher.XmlMatcherCrefo;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expectLastCall;

public class XmlStreamProcessorRecursiveTest extends ExportedZipsSearcherBaseTest
{

  @Test public void testISMInputCursorIsNull() throws Exception
  {
    XmlMatcher xmlMatcher = new XmlMatcherCrefo( new MatcherParameterTag("crefonummer") );
    XmlStreamProcessorRecursive xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcher, "test.log" );
    ISMInputCursor childElementCursor = null;
    try
    {
      xmlStreamProcessor.handleForListeners( childElementCursor, null, null );
      Assert.fail( "XMLStreamException expected!" );
    }
    catch (XMLStreamException ex)
    {
      Assert.assertEquals( "childElementCursor ist null!", ex.getMessage() );
    }
  }

  @Test public void testISMInputCursorWithNoneElement() throws Exception
  {
    XmlMatcher xmlMatcher = new XmlMatcherCrefo( new MatcherParameterTag("crefonummer") );
    XmlStreamProcessorRecursive xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcher, "test.log" );

    ISMInputCursor mockISMInputCursor = EasyMock.createStrictMock( ISMInputCursor.class );
    EasyMock.expect( mockISMInputCursor.getNext() ).andReturn( null );
    mockISMInputCursor.exitElement();
    EasyMock.expectLastCall();

    EasyMock.replay( mockISMInputCursor );

    boolean isOK = xmlStreamProcessor.handleForListeners( mockISMInputCursor, null, null );
    Assert.assertFalse( isOK );

    EasyMock.verify( mockISMInputCursor );
  }

  @Test public void testISMInputCursorWithCrefonummerOnly() throws Exception
  {
    XmlMatcher xmlMatcher = new XmlMatcherCrefo( new MatcherParameterTag("crefonummer") );
    XmlStreamProcessorRecursive xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcher, "test.log" );
    ISMInputCursor crfNrCursor = EasyMock.createStrictMock( ISMInputCursor.class );
    IPerEntryListener mockListener = EasyMock.createMock(IPerEntryListener.class);

    EasyMock.expect( crfNrCursor.getNext() ).andReturn( SMEvent.START_ELEMENT );
    EasyMock.expect( crfNrCursor.getLocalName() ).andReturn( "crefonummer" );
    EasyMock.expect( crfNrCursor.getCurrEventCode() ).andReturn( 1 );
    EasyMock.expect( crfNrCursor.getLocalName() ).andReturn( "crefonummer" );
    mockListener.addMatchingCriterion( anyString() );
    expectLastCall().anyTimes();
    EasyMock.expect( crfNrCursor.getElemStringValue() ).andReturn( "1234567890L" );
    EasyMock.expect( crfNrCursor.getCurrEventCode() ).andReturn( XMLStreamConstants.END_ELEMENT );
    EasyMock.expect( crfNrCursor.getNext() ).andReturn( null );
    crfNrCursor.exitElement();
    EasyMock.expectLastCall();

    EasyMock.replay( crfNrCursor, mockListener );

    boolean isOK = xmlStreamProcessor.handleForListeners( crfNrCursor, mockListener, null );
    Assert.assertTrue( isOK );

    EasyMock.verify( crfNrCursor );
  }

  @Test public void testISMInputCursorWithCrefonummerAndOneXmlTag() throws Exception
  {
    XmlMatcher xmlMatcher = new XmlMatcherCrefo( new MatcherParameterTag("crefonummer") );
    XmlStreamProcessorRecursive xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcher, "test.log" );
    ISMInputCursor crfNrCursor = EasyMock.createStrictMock( "crfNrCursor", ISMInputCursor.class );
    ISMInputCursor childCrfCursor = EasyMock.createStrictMock( "childCrfCursor", ISMInputCursor.class );
    IPerEntryListener mockListener = EasyMock.createMock(IPerEntryListener.class);

    EasyMock.expect( crfNrCursor.getNext() ).andReturn( SMEvent.START_ELEMENT );
    EasyMock.expect( crfNrCursor.getLocalName() ).andReturn( "crefonummer" );
    EasyMock.expect( crfNrCursor.getCurrEventCode() ).andReturn( 1 );
    EasyMock.expect( crfNrCursor.getLocalName() ).andReturn( "crefonummer" );
    mockListener.addMatchingCriterion( anyString() );
    expectLastCall().anyTimes();
    EasyMock.expect( crfNrCursor.getElemStringValue() ).andReturn( "1234567890L" );
    EasyMock.expect( crfNrCursor.getCurrEventCode() ).andReturn( XMLStreamConstants.START_ELEMENT );
    EasyMock.expect( crfNrCursor.childElementCursor(EasyMock.anyString()) ).andReturn( childCrfCursor );
    EasyMock.expect( childCrfCursor.getNext() ).andReturn( null );
    childCrfCursor.exitElement();
    EasyMock.expectLastCall();
    EasyMock.expect( crfNrCursor.getNext() ).andReturn( null );
    crfNrCursor.exitElement();
    EasyMock.expectLastCall();

    EasyMock.replay( crfNrCursor, childCrfCursor, mockListener );

    boolean isOK = xmlStreamProcessor.handleForListeners( crfNrCursor, mockListener, null );
    Assert.assertTrue( isOK );
    EasyMock.verify( childCrfCursor, crfNrCursor );
  }

}
