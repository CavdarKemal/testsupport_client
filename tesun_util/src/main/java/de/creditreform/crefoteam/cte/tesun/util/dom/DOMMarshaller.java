package de.creditreform.crefoteam.cte.tesun.util.dom;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper-Klasse für die Ausgabe eines DOM-{@link Node} inklusive Pretty-Print
 * User: ralf
 * Date: 23.05.14
 * Time: 08:56
 */
public class DOMMarshaller {

    private final Charset encoding;
    private final Map<String, Object> parameterMap;
    private String newLine;

    public DOMMarshaller() {
        this(StandardCharsets.UTF_8);
    }

    public DOMMarshaller(Charset encoding) {
        this.encoding = encoding;
        this.parameterMap = new LinkedHashMap<String, Object>();
        setNewLine("\r\n");
        addParameter("format-pretty-print", Boolean.TRUE);
        addParameter("element-content-whitespace", Boolean.TRUE);
        addParameter("cdata-sections", Boolean.TRUE);
    }

    public final void setNewLine(String newLine) {
        this.newLine = newLine;
    }

    public final void addParameter(String key, Object value) {
        parameterMap.put(key, value);
    }

    public List<String> listAcceptableParameters() {
        DOMImplementationLS impls = createDomImplementationLS();
        LSSerializer domWriter = impls.createLSSerializer();
        DOMConfiguration domConfig = domWriter.getDomConfig();
        DOMStringList dsl = domConfig.getParameterNames();
        int length = dsl.getLength();
        List<String> res = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            res.add(dsl.item(i));
        }
        return res;
    }

    public void printToStream(Node source, OutputStream outputStream)
            throws IOException {
        DOMImplementationLS impls = createDomImplementationLS();
        //Prepare the output
        LSOutput domOutput = impls.createLSOutput();
        domOutput.setEncoding(encoding.name());
        BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream, 10240);
        domOutput.setByteStream(bufferedStream);
        writeToLSOutput(impls, domOutput, source);
        bufferedStream.flush();
    }

    public String printToString(Node source) {
        StringWriter stringWriter = new StringWriter();
        DOMImplementationLS impls = createDomImplementationLS();
        // Prepare LSOutput
        LSOutput domOutput = impls.createLSOutput();
        domOutput.setEncoding(encoding.name());
        domOutput.setCharacterStream(stringWriter);
        // marshal
        writeToLSOutput(impls, domOutput, source);
        return domOutput.getCharacterStream().toString();
    }

    private DOMImplementationLS createDomImplementationLS() {
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            return (DOMImplementationLS) registry.getDOMImplementation("LS");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Fehler bei der Instantiierung der DOMImplementationRegistry", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Fehler bei der Instantiierung der DOMImplementationRegistry", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Fehler bei der Instantiierung der DOMImplementationRegistry", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("DOMImplementationRegistry.getDOMImplementation(\"LS\") liefert keine Instanz von DOMImplementationLS", e);
        }
    }

    private DOMConfiguration writeToLSOutput(DOMImplementationLS impls, LSOutput domOutput, Node source) {
        // Prepare LSSerializer
        LSSerializer domWriter = impls.createLSSerializer();
        DOMConfiguration domConfig = domWriter.getDomConfig();
        for (Map.Entry<String, Object> param : parameterMap.entrySet()) {
            domConfig.setParameter(param.getKey(), param.getValue());
        }
        if (newLine != null) {
            domWriter.setNewLine(newLine);
        }
        // write
        domWriter.write(source, domOutput);
        return domConfig;
    }
}
