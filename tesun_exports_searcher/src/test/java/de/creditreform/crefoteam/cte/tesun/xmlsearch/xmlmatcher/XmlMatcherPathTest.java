package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.ISMInputCursor;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.XmlSearchCursor;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorIF;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorRecursive;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IPerEntryListener;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Arrays;

import static org.easymock.EasyMock.*;

public class XmlMatcherPathTest extends ExportedZipsSearcherBaseTest
{
  private XmlMatcher createMatcherAnything() throws XMLStreamException
  {
    XmlMatcher xmlMatcher = new XmlMatcher()
    {
      private boolean invoked;
      @Override public XmlMatcher reset()
      {
        invoked = false;
        return this;
      }

      @Override
      public void notifyZipEntryCompleted(boolean success, IPerEntryListener perEntryListener) {
        // intentionally empty
      }

      @Override public boolean isSatisfied()
      {
        return invoked;
      }
      
      @Override public XmlMatcher matchCursor(XmlSearchCursor childCursor, IPerEntryListener perEntryListener) throws XMLStreamException
      {
        invoked = true;
        return this;
      }

      @Override
      public void notifyExitElement(XmlSearchCursor childCursor, IPerEntryListener perEntryListener) throws XMLStreamException {
        // intentionally empty
      }

    };
    return xmlMatcher;
  }

  private ISMInputCursor createCursor( String... pathElements )
  {
    ISMInputCursor cursor = createMock( ISMInputCursor.class );
    expect( cursor.getPathsList() ).andReturn( Arrays.asList( pathElements ) ).anyTimes();
    replay( cursor );
    return cursor;
  }

  private IPerEntryListener createListener(boolean expectIsSatisfied) {
    IPerEntryListener mockListener = EasyMock.createMock(IPerEntryListener.class);
    if (expectIsSatisfied) {
      mockListener.addMatchingCriterion(EasyMock.anyString());
      EasyMock.expectLastCall().once();
    }
    replay(mockListener);
    return mockListener;
  }

  @Test public void testPathMatching() throws Exception
  {
    // Der XmlMatcherPath filtert das letzte Element der Vorgabe nicht, dafür ist der gewrappte Matcher zuständig.
    // In der Vorgabe '1.2.3' ist daher '3' für den XmlMatcherPath irrelevant.
    XmlMatcherPath cut = new XmlMatcherPath( new MatcherParameterTag("1.2.3"), createMatcherAnything() );
    ISMInputCursor shortPathCursor = createCursor();
    Assert.assertFalse( "Mit leerem Pfad darf kein Treffer erzielt werden", cut.reset().matchCursor( shortPathCursor, createListener(false)).isSatisfied() );

    ISMInputCursor incompletePathCursor = createCursor( "2" );
    Assert.assertFalse( "Mit unvollständigem Pfad darf kein Treffer erzielt werden", cut.reset().matchCursor( incompletePathCursor, createListener(false)).isSatisfied() );

    ISMInputCursor exactPathCursor = createCursor( "1", "2" );
    Assert.assertTrue( "Mit exakt passendem Pfad muss ein Treffer erzielt werden", cut.reset().matchCursor( exactPathCursor, createListener(true)).isSatisfied() );

    ISMInputCursor longerPathCursor = createCursor( "0", "1", "2" );
    Assert.assertTrue( "Mit längerem, aber passendem Pfad muss ein Treffer erzielt werden", cut.reset().matchCursor( longerPathCursor, createListener(true)).isSatisfied() );

    ISMInputCursor tooLongPathCursor = createCursor( "0", "1", "2", "3" );
    Assert.assertFalse( "Bei Child-Elementen des passenden Pfades sollte kein Treffer erzielt werden", cut.reset().matchCursor( tooLongPathCursor, createListener(false)).isSatisfied() );

  }
  
  @Test public void testSinglePathElement() throws Exception {
    XmlMatcherPath cut = new XmlMatcherPath( new MatcherParameterTag("3"), createMatcherAnything() );

    ISMInputCursor incompletePathCursor = createCursor();    
    Assert.assertTrue( "Bei unvollständigem Pfad muss die Entscheidung an den gekapselten Matcher delegiert werden", cut.reset().matchCursor( incompletePathCursor, createListener(true)).isSatisfied() );

    ISMInputCursor exactPathCursor = createCursor( "1", "2" );
    Assert.assertTrue( "Mit passendem Pfad muss die Entscheidung an den gekapselten Matcher delegiert werden", cut.reset().matchCursor( exactPathCursor, createListener(true)).isSatisfied() );
    
  }
  
  @Test public void testXmlStreamProcessorWithXmlMatcherPath() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    // Der Tag existiert nicht ==> kein Treffer!
    MatcherParameterTag matcherParameterTagKorrespondenz = new MatcherParameterTag( "korrespondenz-adresse.bezeichnung");
    XmlMatcherSearchCriteria xmlMatcherSearchCriteria = new XmlMatcherSearchCriteria(matcherParameterTagKorrespondenz, "blabla" );
    XmlMatcherPath xmlMatcherPath = new XmlMatcherPath( matcherParameterTagKorrespondenz, xmlMatcherSearchCriteria );
    XmlMatcherLogicAnd xmlMatcherLogicAnd = new XmlMatcherLogicAnd( xmlMatcherPath );

    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicAnd, "test.log");
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertFalse( isMatch );

    // Stream zurück setzen!
    inputStream.reset();
    
    // Der Tag existiert zwar, hat aber nicht den erwarteten Wert  ==> kein Treffer!
    matcherParameterTagKorrespondenz = new MatcherParameterTag( "bezeichnung");
    xmlMatcherSearchCriteria = new XmlMatcherSearchCriteria(matcherParameterTagKorrespondenz, "Bezeichnung xyz Firmendatenexport" );
    xmlMatcherPath = new XmlMatcherPath( matcherParameterTagKorrespondenz, xmlMatcherSearchCriteria );
    xmlMatcherLogicAnd = new XmlMatcherLogicAnd( xmlMatcherPath );

    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicAnd, "test.log");
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertFalse( isMatch );

    // Stream zurück setzen!
    inputStream.reset();

    // Der Tag existiert auf der obersten Ebene und hat dern erwarteten Wert == > ein Treffer.
    MatcherParameterTag matcherParameterTagFirmendatenExp = new MatcherParameterTag( "bezeichnung");
    xmlMatcherSearchCriteria = new XmlMatcherSearchCriteria(matcherParameterTagFirmendatenExp, "Bezeichnung der Firmendatenexport" );
    xmlMatcherPath = new XmlMatcherPath( matcherParameterTagFirmendatenExp, xmlMatcherSearchCriteria );
    xmlMatcherLogicAnd = new XmlMatcherLogicAnd( xmlMatcherPath );

    xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherLogicAnd, "test.log");
    isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream schliessen!
    inputStream.close();
  }

  
}
