package de.creditreform.crefoteam.cte.tesun.util.dom;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Test-Klasse für DOMMarshaller
 * User: ralf
 * Date: 23.05.14
 * Time: 09:48
 */
public class DOMMarshallerTest {
    public static final String UNFORMATTED = "<doc><child><grandchild>äöü</grandchild></child><child>ÄÖÜ</child></doc>";

    public static final String FORMATTED = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
            "<doc>\r\n" +
            "    <child>\r\n" +
            "        <grandchild>äöü</grandchild>\r\n" +
            "    </child>\r\n" +
            "    <child>ÄÖÜ</child>\r\n" +
            "</doc>\r\n";

    public static final String FORMATTED_LF = FORMATTED.replace("\r\n", "\n");

    @Test
    public void testListAcceptableParameters() {
        DOMMarshaller cut = new DOMMarshaller();
        List<String> acceptable = cut.listAcceptableParameters();
        System.out.println(acceptable);
    }

    @Test
    public void testFormattedResult() {
        Document doc = parseXmlString(UNFORMATTED);
        DOMMarshaller cut = new DOMMarshaller();

        String formatted = cut.printToString(doc);
        Assert.assertEquals(FORMATTED, formatted);

        cut.setNewLine("\n");
        String formattedLF = cut.printToString(doc);
        Assert.assertEquals(FORMATTED_LF, formattedLF);
    }

    @Test
    public void testByteStream()
            throws IOException {
        Document doc = parseXmlString(UNFORMATTED);
        DOMMarshaller cut = new DOMMarshaller();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        cut.printToStream(doc, baos);

        Assert.assertArrayEquals(FORMATTED.getBytes(StandardCharsets.UTF_8), baos.toByteArray());
    }

    private Document parseXmlString(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
