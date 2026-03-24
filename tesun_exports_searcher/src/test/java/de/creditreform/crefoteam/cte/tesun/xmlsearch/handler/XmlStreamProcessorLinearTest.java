package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.ProgressListenerNop;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.XmlSearchCursor;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.XmlStreamListenerGroup;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IPerEntryListener;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher.*;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test-Klasse für {@link XmlStreamProcessorLinear}
 */
public class XmlStreamProcessorLinearTest {

   private static final Logger logger = LoggerFactory.getLogger(XmlStreamProcessorLinearTest.class);

   String ABF_LOESCHSATZ_9110188048_XML = "abf_loeschsatz_9110188048.xml";
   String STAMMCREFO_2410064578_XML = "stammcrefo_2410064578.xml";
   String DEFEKT_TEXT_MIT_CHILD_XML = "defekt_TextUndChildElement.xml";
   XmlStreamListenerGroup listenerGroup = new XmlStreamListenerGroup(null, new ProgressListenerNop());

   private static class LoggingXmlMatcher
   implements XmlMatcher {
      @Override
      public XmlMatcher matchCursor(XmlSearchCursor childCursor, IPerEntryListener perEntryListener)
      throws XMLStreamException {
         StringBuilder msg = new StringBuilder();
         for (String trc : childCursor.getPathsList()) {
            msg.append(trc).append('/');
         }
         final String elemStringValue = childCursor.getElemStringValue();
         if (elemStringValue!=null) {
            msg.append(':').append(elemStringValue.replaceAll("\n","<CR>")
                                                  .replaceAll("\r","<LF>")
                                  );
         }
//         if (childCursor.getCurrEventCode() == XMLEvent.START_ELEMENT) {
         if (childCursor instanceof XmlStreamProcessorLinearCursor) {
            XmlStreamProcessorLinearCursor lc = (XmlStreamProcessorLinearCursor) childCursor;
            msg.append('#').append( lc.getAttributeCount() );
         }
//         }
         System.out.println(msg);

         return null;
      }

      @Override
      public void notifyExitElement(XmlSearchCursor childCursor, IPerEntryListener perEntryListener) throws XMLStreamException {
         // intentionally empty
      }

      @Override
      public boolean isSatisfied() {
         return false;
      }

      @Override
      public XmlMatcher reset() {
         return this;
      }

      @Override
      public void notifyZipEntryCompleted(boolean success, IPerEntryListener perEntryListener) {
         // intentionally empty
      }

   }

   @Test
   public void testTraverseTree()
   throws XMLStreamException {
      traverseTree(STAMMCREFO_2410064578_XML);
      traverseTree(DEFEKT_TEXT_MIT_CHILD_XML);
   }

   private void traverseTree(String xmlFileName)
   throws XMLStreamException {
      InputStream resourceAsStream = this.getClass().getResourceAsStream("/" + xmlFileName);
      XmlStreamProcessorLinear cut = new XmlStreamProcessorLinear(new LoggingXmlMatcher(), "test.log");
      cut.handleForListeners(resourceAsStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
   }

   @Test
   public void testXmlMatcherImplementations()
   throws XMLStreamException, IOException {
      XmlMatcherCrefo matcherCrefo = pruefeMitMatcher(STAMMCREFO_2410064578_XML, true, new XmlMatcherCrefo(new MatcherParameterTag("crefonummer") ) );
      // ohne vorgeschalteten Filter liefert der MatcherCrefo immer den letzten Treffer
      Assert.assertEquals("falsche Crefonummer gefunden", "2410064578", matcherCrefo.getMatcherResult());
      pruefeMitMatcher(STAMMCREFO_2410064578_XML, false, new XmlMatcherCrefo(new MatcherParameterTag("keine-crefo-nummer") ) );
      pruefeMitMatcher(STAMMCREFO_2410064578_XML, false, new XmlMatcherTagNameOnly(new MatcherParameterTag("keine-crefo-nummer") ));
      pruefeMitMatcher(STAMMCREFO_2410064578_XML, true, new XmlMatcherTagNameOnly(new MatcherParameterTag("crefonummer") ));
   }

   @Test
   public void testXmlMatcherFindFirstCrefo()
   throws XMLStreamException, IOException {
      XmlMatcherCrefo matcherCrefo = new XmlMatcherCrefo(new MatcherParameterTag("crefonummer") );
      XmlMatcherFindFirst matcherFindFirst = new XmlMatcherFindFirst(matcherCrefo);
      pruefeMitMatcher(STAMMCREFO_2410064578_XML, true, matcherFindFirst);
      Assert.assertEquals("falsche Crefonummer gefunden", "2410064578", matcherCrefo.getMatcherResult());
   }

   @Test
   public void testXmlMatcherCrefoInPath()
   throws XMLStreamException, IOException {
      XmlMatcherCrefo matcherCrefo = new XmlMatcherCrefo(new MatcherParameterTag("crefonummer") );
      XmlMatcherPath matcherPath = new XmlMatcherPath(new MatcherParameterTag("vsh-firmendaten.crefonummer"), matcherCrefo);
      pruefeMitMatcher(STAMMCREFO_2410064578_XML, true, matcherPath);
      Assert.assertEquals("falsche Crefonummer gefunden", "2410064578", matcherCrefo.getMatcherResult());
   }

   @Test
   public void test_XmlMatcherCrefo_And_XmlMatcherTagNameOnly()
   throws XMLStreamException, IOException {
      // Matcher für Crefonummer
      XmlMatcherCrefo matcherCrefo = new XmlMatcherCrefo(new MatcherParameterTag("crefonummer") );

      // XmlMatcherTagNameOnly
      String tagName = "abf-loeschsatz-export";
      MatcherParameterTag parameterTag = new MatcherParameterTag(tagName);
      XmlMatcher xmlMatcherX = new XmlMatcherTagNameOnly( parameterTag, Collections.EMPTY_LIST );

      // AND-Matcher aus XmlMatcherCrefo und XmlMatcherTagNameOnly
      XmlMatcherLogicAnd xmlMatcherLogicAnd = new XmlMatcherLogicAnd(matcherCrefo, xmlMatcherX);

      pruefeMitMatcher(ABF_LOESCHSATZ_9110188048_XML, true, xmlMatcherLogicAnd);
      List<XmlMatcher> childXmlMatchers = xmlMatcherLogicAnd.getChildXmlMatchers();
      Assert.assertEquals("falsche Anzahl an Matcher-Results", 2, childXmlMatchers.size());
      XmlMatcherCrefo xmlMatcherCrefo = (XmlMatcherCrefo)childXmlMatchers.get(0);
      Assert.assertEquals("falsche Crefo gefunden!", "9110188048", xmlMatcherCrefo.getMatcherResult());
      XmlMatcherTagNameOnly xmlMatcherTagNameOnly = (XmlMatcherTagNameOnly)childXmlMatchers.get(1);
      Assert.assertEquals("XmlMatcherTagNameOnly stimmt nicht!", tagName, xmlMatcherTagNameOnly.getAllTagNames().get(0));
   }

   @Test
   public void test_XmlMatcherCrefo_And_XmlMatcherLogicOr_With_XmlMatcherTagNameOnly()
   throws XMLStreamException, IOException {
      // Matcher für Crefonummer
      XmlMatcherCrefo matcherCrefo = new XmlMatcherCrefo(new MatcherParameterTag("crefonummer") );

      // OR-Matcher aus einem XmlMatcherTagNameOnly
      List<XmlMatcher> searchCriteriaMatcherList = new ArrayList<>();
      String tagName = "abf-loeschsatz-export";
      MatcherParameterTag parameterTag = new MatcherParameterTag(tagName);
      XmlMatcher xmlMatcherX = new XmlMatcherTagNameOnly( parameterTag, Collections.EMPTY_LIST);
      searchCriteriaMatcherList.add(xmlMatcherX);
      XmlMatcherLogicOr xmlMatcherLogicOr = new XmlMatcherLogicOr( searchCriteriaMatcherList );

      // AND-Matcher aus XmlMatcherCrefo und XmlMatcherLogicOr
      XmlMatcherLogicAnd xmlMatcherLogicAnd = new XmlMatcherLogicAnd(matcherCrefo, xmlMatcherLogicOr);

      pruefeMitMatcher(ABF_LOESCHSATZ_9110188048_XML, true, xmlMatcherLogicAnd);
      List<XmlMatcher> childXmlMatchers = xmlMatcherLogicAnd.getChildXmlMatchers();
      Assert.assertEquals("falsche Anzahl an Matcher-Results", 2, childXmlMatchers.size());
      XmlMatcherCrefo xmlMatcherCrefo = (XmlMatcherCrefo)childXmlMatchers.get(0);
      Assert.assertEquals("falsche Crefo gefunden!", "9110188048", xmlMatcherCrefo.getMatcherResult());
      XmlMatcherLogicOr xmlMatcherLogicOr1 = (XmlMatcherLogicOr)childXmlMatchers.get(1);
      List<XmlMatcher> childXmlMatchers1 = xmlMatcherLogicOr1.getChildXmlMatchers();
      XmlMatcherTagNameOnly xmlMatcherTagNameOnly = (XmlMatcherTagNameOnly)childXmlMatchers1.get(0);
      Assert.assertEquals("XmlMatcherTagNameOnly stimmt nicht!", tagName, xmlMatcherTagNameOnly.getAllTagNames().get(0));
   }

   private <T extends XmlMatcher> T pruefeMitMatcher(String xmlFileName, boolean expectMatch, T xmlMatcher)
   throws XMLStreamException, IOException {
      try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/" + xmlFileName)){
         XmlStreamProcessorIF cut = new XmlStreamProcessorLinear(xmlMatcher, "test.log");
         final boolean actualMatch = cut.handleForListeners(resourceAsStream, listenerGroup.createPerEntryListener(), listenerGroup.getProgressListener());
         Assert.assertEquals(expectMatch, actualMatch);
      }
      return xmlMatcher;
   }

}
