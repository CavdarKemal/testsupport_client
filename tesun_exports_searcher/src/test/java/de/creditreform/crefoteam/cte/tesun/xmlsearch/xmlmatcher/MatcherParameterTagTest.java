package de.creditreform.crefoteam.cte.tesun.xmlsearch.xmlmatcher;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test-Klasse für {@link MatcherParameterTag}
 * @author CavdarK
 *
 */
public class MatcherParameterTagTest
{
  @Test public void testSplitPath() {
    MatcherParameterTag cut = new MatcherParameterTag( "1.2.3" );
    Assert.assertEquals("Tag-Name nicht korrekt herausgeschnitten", "3", cut.getXmlTagName() );
    Assert.assertEquals("Pfad nicht korrekt herausgeschnitten", "[1, 2]", cut.getPathOhneTag().toString() );       
  }

  @Test public void testImmutablePath() {
    MatcherParameterTag cut = new MatcherParameterTag( "1.2.3" );
    try {
      cut.getPathOhneTag().remove( 0 );
      Assert.fail("Liste der Pfad-Elemente im MatcherParameterTag sollte nicht veränderbar sein");
    }
    catch (UnsupportedOperationException e) {
      Assert.assertTrue( true );
    }
  }
  
  @Test public void testNoPath() {
    MatcherParameterTag cut = new MatcherParameterTag( "3" );
    Assert.assertEquals("Tag-Name nicht korrekt herausgeschnitten", "3", cut.getXmlTagName() );
    Assert.assertEquals("Pfad sollte leer sein", "[]", cut.getPathOhneTag().toString() );
  }

  private void assertThrows(String param) {
    try {
      new MatcherParameterTag(param);
      Assert.fail("Konstruktor sollte Exception werfen für: "+param);
    }
    catch (IllegalArgumentException e) {
      Assert.assertTrue( true );
    }
  }
  
  @Test public void testInvalidPath() {
    assertThrows(null);
    assertThrows("");
  }
  
}
