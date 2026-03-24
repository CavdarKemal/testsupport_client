package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.easymock.EasyMock.*;

/**
 * Test-Klasse für {@link XmlStreamProcessorLinearCursor}
 */
public class XmlStreamProcessorLinearCursorTest {

   private static class StringAnswer
   implements IAnswer<String> {
      private String currentAnswer;

      public StringAnswer setCurrentAnswer(String currentAnswer) {
         this.currentAnswer = currentAnswer;
         return this;
      }

      @Override
      public String answer()
      throws Throwable {
         return currentAnswer;
      }

   }

   private XMLStreamReader createMockStreamReader(StringAnswer stringAnswer) {
      QName mockQName = createMock(QName.class);
      expect( mockQName.getLocalPart() ).andAnswer( stringAnswer ).anyTimes();
      replay(mockQName);

      XMLStreamReader mockStreamReader = createMock(XMLStreamReader.class);
      mockStreamReader.getAttributeCount();
      expectLastCall().andReturn(0).anyTimes();
      expect( mockStreamReader.getText() ).andAnswer( stringAnswer ).anyTimes();
      expect( mockStreamReader.getName() ).andReturn( mockQName ).anyTimes();
      expect( mockStreamReader.getLocalName() ).andAnswer( stringAnswer ).anyTimes();
      return mockStreamReader;
   }

   @Test
   public void testPathTraversal()
   throws XMLStreamException {
      XmlStreamProcessorLinearCursor cut = new XmlStreamProcessorLinearCursor();
      StringAnswer stringAnswer = new StringAnswer();
      XMLStreamReader mockStreamReader = createMockStreamReader(stringAnswer);
      replay(mockStreamReader);

      Assert.assertEquals("Pfad sollte zu Beginn noch leer sein", "[]", cut.getPathsList().toString());
      Assert.assertEquals("LocalName sollte zu Beginn noch leer (aber nicht null) sein", "", cut.getLocalName());

      // [] -> [root]
      stringAnswer.setCurrentAnswer("root");
      cut.enterElement(mockStreamReader);
      Assert.assertEquals("Vor dem ersten Child-Element sollte der Pfad leer sein", "[]", cut.getPathsList().toString());
      Assert.assertEquals("Vor dem ersten Child-Element sollte 'LocalName' die Wurzel nennen", "root", cut.getLocalName());

      // [root] -> [root/kind-1]
      stringAnswer.setCurrentAnswer("kind-1");
      cut.enterElement(mockStreamReader);
      Assert.assertEquals("Ab dem ersten Child-Element sollte der Pfad 'root' enthalten", "[root]", cut.getPathsList().toString());
      Assert.assertEquals("Der Name des aktuellen Elementes sollte in 'LocalName' geliefert werden", "kind-1", cut.getLocalName());

      // [root/kind-1] -> [root/kind-1/enkel-1]
      stringAnswer.setCurrentAnswer("enkel-1");
      cut.enterElement(mockStreamReader);
      Assert.assertEquals("Mit dem verschachtelten Child-Element sollte der Pfad 'root, kind-1' enthalten", "[root, kind-1]",
                          cut.getPathsList().toString());
      Assert.assertEquals("Der Name des aktuellen Elementes sollte in 'LocalName' geliefert werden", "enkel-1", cut.getLocalName());

      // [root/kind-1/enkel-1] -> [root/kind-1]
      cut.exitElement(mockStreamReader);
      Assert.assertEquals("Nach dem verschachtelten Child-Element sollte der Pfad wieder zu 'root' reduziert sein", "[root]",
                          cut.getPathsList().toString());
      Assert.assertEquals("Nach 'exitElement' sollte 'LocalName' leer sein", "", cut.getLocalName());

      // [root/kind-1] -> [root/kind-1/enkel-2]
      stringAnswer.setCurrentAnswer("enkel-2");
      cut.enterElement(mockStreamReader);
      Assert.assertEquals("Bei zwei Enkel-Elementen sollte der Pfad nur 'root,kind-1' enthalten", "[root, kind-1]", cut.getPathsList().toString());
      Assert.assertEquals("Der Name des aktuellen Elementes sollte in 'LocalName' geliefert werden", "enkel-2", cut.getLocalName());

      // [root/kind-1/enkel-2] -> [root/kind-1] -> [root] -> []
      cut.exitElement(mockStreamReader);
      cut.exitElement(mockStreamReader);
      cut.exitElement(mockStreamReader);
      Assert.assertEquals("Nach dem Verlassen von 'root' sollte der Pfad leer sein", "[]", cut.getPathsList().toString());
      Assert.assertEquals("Nach dem Verlassen von 'root' sollte 'LocalName' leer (aber nicht null) sein", "", cut.getLocalName());

   }

}
