package de.creditreform.crefoteam.cte.tesun.util.configuration;

import org.junit.Assert;

import java.util.List;
import java.util.Properties;

/**
 * Test-Klasse für {@link TesunConfigurationProperties}
 * Created by ralf on 10.02.16.
 */
public class TesunConfigurationPropertiesTest {
    protected static final String DUMMY_KEY = "DUMMY-KEY";

    /**** TODO
     @Test public void testNoneReplacement() {
     TesunConfigurationProperties cut = createPropertyContainer("none");
     Assert.assertTrue(cut.containsKey(DUMMY_KEY));
     Assert.assertNull(cut.getOptionalString(DUMMY_KEY, "123"));
     }
     */
    private TesunConfigurationProperties createPropertyContainer(String sourceValue) {
        Properties p = new Properties();
        p.put(DUMMY_KEY, sourceValue);
        final String cmd = "ValueList-cmd-" + sourceValue;
        TesunConfigurationProperties cut = new TesunConfigurationProperties(null, null, cmd);
        Assert.assertEquals("Kommando nicht übernommen", cmd, cut.getSelectedCommand());
        Assert.assertNull(cut.getCharset());
        return cut;
    }

    /**** TODO
     @Test public void testSplitValues() {
     pruefeValueList("2 Parameter, 1 x Komma", 2, "[1, 2]", "1,2");
     pruefeValueList("2 Parameter, 2 x Komma", 2, "[1, 2]", "1,2,");
     pruefeValueList("3 Parameter, 2 x Semikolon", 3, "[4, 5, 6]", "4;5;6");
     pruefeValueList("3 Parameter, 3 x Semikolon", 3, "[7, 8, 9]", "7;8;9;");
     pruefeValueList("1 Parameter, kein Trennzeichen", 1, "[0]", "0");
     pruefeValueList("keine Parameter, kein Trennzeichen", 0, "[]", "");
     pruefeValueList("keine Parameter, mehrere Trennzeichen", 0, "[]", ",;;,");
     pruefeValueList("keine Parameter, 'none' verwendet, mehrere Trennzeichen", 0, "[]", ",;;none,");
     pruefeValueList("1 Parameter, führendes Leerzeichen, kein Trennzeichen", 1, "[ 0]", " 0");
     }
     */

    private void pruefeValueList(String message, int expectedLength, String expectedResult, String sourceValue) {
        TesunConfigurationProperties cut = createPropertyContainer(sourceValue);
        final List<String> optionalStringList = cut.getOptionalStringList(DUMMY_KEY);
        Assert.assertEquals("Fehler in der Anzahl bei: " + message, expectedLength, optionalStringList.size());
        Assert.assertEquals("Fehler im Inhalt bei: " + message, expectedResult, optionalStringList.toString());
    }

}
