package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import java.io.InputStream;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorRecursive;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IPerEntryListener;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.ISMInputCursor;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.XmlStreamProcessorIF;

import javax.xml.stream.XMLStreamException;

import static org.easymock.EasyMock.*;

public class XmlMatcherTagNameOnlyTest extends ExportedZipsSearcherBaseTest
{
  @Test public void testXmlStreamProcessorWithXmlMatcherTagNameOnly() throws Exception
  {
    InputStream inputStream = makeInputStream( "/1234567890.xml" );

    XmlMatcherTagNameOnly xmlMatcherTagNameOnly = new XmlMatcherTagNameOnly(new MatcherParameterTag("vsh-firmendaten"));
    XmlStreamProcessorIF xmlStreamProcessor = new XmlStreamProcessorRecursive(xmlMatcherTagNameOnly, "vsh-firmendaten.log");
    boolean isMatch = xmlStreamProcessor.handleForListeners(inputStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
    Assert.assertTrue( isMatch );

    // Stream close!
    inputStream.close();
  }

    @Test
    public void testAlternateTagNames() throws Exception {
        XmlMatcherTagNameOnly cut = new XmlMatcherTagNameOnly(new MatcherParameterTag("alt1"), "alt2", "alt3");
        final boolean expectedMatch = true;
        pruefeMatch(cut, "alt1", expectedMatch);
        pruefeMatch(cut, "alt2", expectedMatch);
        pruefeMatch(cut, "alt3", expectedMatch);
        pruefeMatch(cut, "alt4", false);
    }

    protected void pruefeMatch(XmlMatcherTagNameOnly cut, String tagName, boolean expectedMatch)
    throws XMLStreamException {
        ISMInputCursor mockCursor = createMock(ISMInputCursor.class);
        expect( mockCursor.getLocalName() ).andReturn(tagName).anyTimes();
        IPerEntryListener mockListener = EasyMock.createMock(IPerEntryListener.class);
        mockListener.addMatchingCriterion(EasyMock.anyString());
        expectLastCall().anyTimes();
        replay( mockCursor, mockListener );
        Assert.assertEquals(expectedMatch, cut.reset().matchCursor(mockCursor, mockListener).isSatisfied());
    }

}
