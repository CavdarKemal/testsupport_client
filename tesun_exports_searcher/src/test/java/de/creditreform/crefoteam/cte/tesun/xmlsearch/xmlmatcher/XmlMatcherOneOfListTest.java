package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.XmlSearchCursor;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.schnittstellen.IPerEntryListener;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;

/**
 * Test-Klasse für {@link XmlMatcherOneOfList}
 */
public class XmlMatcherOneOfListTest {

   @BeforeClass
   public static void warmUp() {
      // Das Initialisieren der Klasse (speziell des Loggers) verfälscht die in den Tests gemessenen Laufzeiten.
      // Dies wird mit der statischen WarmUp-Methode verhindert.
      XmlMatcherOneOfList cut = new XmlMatcherOneOfList(null, XmlMatcherCrefo.DEFAULT_TAGNAMES_CREFO);
      Assert.assertNotNull(cut);
   }
   
   @Test
   public void testDefaultResource() {
      validateXmlMatcherOneOfList(new XmlMatcherOneOfList(null, XmlMatcherCrefo.DEFAULT_TAGNAMES_CREFO).initFromResource(null),
                                  XmlMatcherOneOfList.DEFAULT_RESOURCE_NAME, 4);
   }

   @Test
   public void testInitFromFile() {
      final String fileName = "./src/test/resources/find-one-of.config";
      validateXmlMatcherOneOfList(new XmlMatcherOneOfList(null, XmlMatcherCrefo.DEFAULT_TAGNAMES_CREFO).initFromFile(fileName),
                                  "file://"+fileName, 4);
   }

   public XmlMatcherOneOfList validateXmlMatcherOneOfList(XmlMatcherOneOfList cut, String expectedResourceName, int expectedSize) {
      Assert.assertEquals(expectedResourceName, cut.getResourceName());
      Assert.assertEquals(expectedSize, cut.getStringsToFind().size());
      return cut;
   }

   public XmlMatcherOneOfList createAndValidate(String resourceName, int expectedSize) {
      XmlMatcherOneOfList cut = new XmlMatcherOneOfList(null, XmlMatcherCrefo.DEFAULT_TAGNAMES_CREFO).initFromResource(resourceName);
      return validateXmlMatcherOneOfList(cut, resourceName, expectedSize);
   }

   @Test
   public void testEmptyResource() {
      createAndValidate("/find-one-of-empty.config", 0);
   }

   @Test
   public void testResourceWithTrailingLinefeed() {
      createAndValidate("/find-one-of-trailing-lf.config", 1);
   }

   @Test
   public void testResourceWithLeadingAndTrailingSpaces() {
      XmlMatcherOneOfList cut = createAndValidate("/find-one-of-having-spaces.config", 2);
      Assert.assertTrue( cut.getStringsToFind().contains("suchEines"));
      Assert.assertTrue( cut.getStringsToFind().contains("such das Zweite"));
   }

   private void verifyXmlMatch(XmlMatcherOneOfList cut, String textToMatch, boolean expectIsSatisfied)
   throws XMLStreamException {
      XmlSearchCursor mockContext = EasyMock.createMock(XmlSearchCursor.class);
      EasyMock.expect(mockContext.getLocalName()).andReturn("crefonummer").times(2);
      EasyMock.expect(mockContext.getElemStringValue()).andReturn(textToMatch);
      IPerEntryListener mockListener = EasyMock.createMock(IPerEntryListener.class);
      mockListener.addMatchingCriterion(EasyMock.anyString());
      EasyMock.expectLastCall().once();
      if (expectIsSatisfied) {
         mockListener.addMatchingCriterion(EasyMock.anyString());
         EasyMock.expectLastCall().once();
      }
      EasyMock.replay(mockContext, mockListener);

      XmlMatcher returnedFromReset = cut.reset();
      Assert.assertSame("Rückgabewert von 'reset' ist nicht die aktuelle Instanz", returnedFromReset, cut);
      Assert.assertEquals("trotz 'reset' liefert 'isSatisfied' den Wert True", false, cut.isSatisfied());

      XmlMatcher returnedFromMatch = cut.matchCursor(mockContext, mockListener);
      Assert.assertSame("Rückgabewert von 'matchCursor' ist nicht die aktuelle Instanz", returnedFromMatch, cut);
      Assert.assertEquals("Ergebnis von 'matchCursor' ist nicht korrekt", expectIsSatisfied, cut.isSatisfied());

      EasyMock.verify(mockContext);
   }

   @Test
   public void testMatchCursor()
   throws XMLStreamException {
      XmlMatcherOneOfList cut = new XmlMatcherOneOfList(null, XmlMatcherCrefo.DEFAULT_TAGNAMES_CREFO).initFromResource(null);
      verifyXmlMatch(cut, "bla bla", false);
      verifyXmlMatch(cut, "suchdies", true);
      verifyXmlMatch(cut, " suchdies", true);
      verifyXmlMatch(cut, " suchdies ", true);
      verifyXmlMatch(cut, null, false);
      verifyXmlMatch(cut, "AuchJENES", true);
      verifyXmlMatch(cut, "das gibt es nicht", false);
   }

}
