package de.creditreform.crefoteam.cte.tesun.util.replacer;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test-Klasse für {@link Replacer}
 * User: ralf
 * Date: 07.02.14
 * Time: 13:57
 */
public class ReplacerTest {
    public static final String FILENAME_SOURCE = "./src/test/resources/ReplacerTest.Source.txt";
    public static final String PATHNAME_TARGET = "./target/generated-replacertest";
    public static final String FILENAME_TARGET = PATHNAME_TARGET + "/ReplacerTest.Actual.txt";
    public static final String FILENAME_EXPECTED = "./src/test/resources/ReplacerTest.Expected.txt";

    Map<String, ReplacementMapping> replacementMappingMap = new TreeMap<>();

    private void verifyReplacer(Replacer cut, String outputCharset) throws IOException {
        File targetDir = new File(PATHNAME_TARGET);
        targetDir.mkdirs();
        cut.copyAndReplace("verifyReplacer", FILENAME_SOURCE, FILENAME_TARGET);
        String actual = IOUtils.toString(new FileInputStream(FILENAME_TARGET), outputCharset);
        String expected = IOUtils.toString(new FileInputStream(FILENAME_EXPECTED), StandardCharsets.UTF_8);
        Assert.assertEquals(expected, actual);
    }

    private void verifyReplacer(Replacer cut) throws IOException {
        verifyReplacer(cut, "UTF-8");
    }

    @Test
    public void testPatternForCrefo() {
        String xmlFragment =
                "<fsu-firmendatenexport>\n" +
                        "    <fsu-firmendaten>\n" +
                        "        <crefonummer>2410052999</crefonummer>\n" +
                        "    </fsu-firmendaten>\n" +
                        "</fsu-firmendatenexport>\n";

        Pattern crefosPattern = Pattern.compile("2410052999|4120052990");
        Matcher matcherCrefosPattern = crefosPattern.matcher(xmlFragment);
        Assert.assertTrue(matcherCrefosPattern.find());
        String strTemp = matcherCrefosPattern.group();
        Assert.assertEquals("Crefo-Ersetzung falsch!", "2410052999", strTemp);
    }

    private ReplacerParameterException addExpectingException(ReplacerFactory cut, String toBeReplaced, String replacement, String expectedMessagePrefix) {
        try {
            cut.insertStringReplacement(toBeReplaced, replacement);
            Assert.fail("fehlerhafte Ersetzung nicht abgewiesen: " + toBeReplaced + " --> " + replacement);
            return null;
        } catch (ReplacerParameterException e) {
            Assert.assertTrue(e.getMessage().startsWith(expectedMessagePrefix));
            return e;
        }
    }

    private ReplacerParameterException addExpectingException(ReplacerFactory cut, long toBeReplaced, long replacement) {
        try {
            cut.insertCrefoReplacement(replacementMappingMap, toBeReplaced, replacement);
            Assert.fail("fehlerhafte Crefonummer(n) nicht abgewiesen: " + toBeReplaced + " --> " + replacement);
            return null;
        } catch (ReplacerParameterException e) {
            Assert.assertTrue(e.getMessage().startsWith(ReplacerFactory.MSG_CREFO_10DIGITS));
            return e;
        }
    }

    @Test
    public void testFactory() throws IOException {
        ReplacerFactory factory = new ReplacerFactory(Charset.defaultCharset());
        addExpectingException(factory, null, null, ReplacerFactory.MSG_STRING_NULL);
        addExpectingException(factory, "", "", ReplacerFactory.MSG_STRING_EMPTY);
        factory.insertStringReplacement("1234567890", "2345678901");
        factory.insertStringReplacement("2345678901", "3456789012");
        factory.insertStringReplacement("2345678901", "3456789012");
        addExpectingException(factory, "2345678901", "abcdefghij", ReplacerFactory.MSG_REPLACEMENT_CHANGED);
        addExpectingException(factory, "12345678901234", "45678901234567", ReplacerFactory.MSG_SHORTER_REPLACEMENT_EXISTS);
        factory.insertStringReplacement("<", "[");
        factory.insertStringReplacement(">", "]");
        verifyReplacer(factory.create(replacementMappingMap, true));

        factory.insertStringReplacement("12345678901234", "45678901234567");
        addExpectingException(factory, "1234567890", "2345678901", ReplacerFactory.MSG_LONGER_REPLACEMENT_EXISTS);

        addExpectingException(factory, 1234567890L, 123L);
        addExpectingException(factory, 1234567890L, 12345678901L);
        addExpectingException(factory, 1234L, 1234567890L);

    }

    @Test
    public void testCharsetDetection() throws IOException {
        InputStream resourceISO8859 = getClass().getResourceAsStream("/bilanz_iso-8859-1.xml");
        Assert.assertNotNull("Datei mit den Quelldaten im Encoding ISO-8859-1 nicht gefunden", resourceISO8859);
        Replacer replacerUnterTest = new Replacer(null, null, StandardCharsets.UTF_8.name());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceISO8859);
        String detected = replacerUnterTest.detectCharset(bufferedInputStream);
        Assert.assertEquals(StandardCharsets.ISO_8859_1.name(), detected);
    }

    @Test
    public void testByteIndexOf() {
        Replacer replacerUnterTest = new Replacer(null, null, StandardCharsets.UTF_8.name());
        byte[] sourceBytes = "0123456789".getBytes(StandardCharsets.US_ASCII);
        byte[] bytesToFind = "345".getBytes(StandardCharsets.US_ASCII);
        int foundAtOffset = replacerUnterTest.byteIndexOf(sourceBytes, sourceBytes.length, bytesToFind);
        Assert.assertEquals("Treffer im Byte-Array nicht am erwarteten Offset", 3, foundAtOffset);
    }

}
