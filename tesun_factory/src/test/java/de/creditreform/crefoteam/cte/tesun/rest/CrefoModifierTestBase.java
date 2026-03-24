package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.tesun.util.CrefoModifierListener;
import de.creditreform.crefoteam.cte.tesun.util.CrefoModifierResult;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CrefoModifierTestBase extends TesunRestServiceIntegrationTestBase {

    protected static String AB3_0_XSD = "http://www.creditreform.de/crefoteam/archivbestandv3_0";

    final File rscDir;
    final File modifedXmlsDir;
    protected static CrefoModifierListener crefoModifierListener = new CrefoModifierListener() {
        @Override
        public void nodifyModification(String strInfo) {
            logger.info(strInfo);
        }
    };

    public CrefoModifierTestBase(String umgebung, File rscDir) throws URISyntaxException {
        super(umgebung);
        this.rscDir = rscDir;
        this.modifedXmlsDir = new File(rscDir.getParentFile(), "modified");
        modifedXmlsDir.mkdirs();
    }

    public static void checkTagValue(String xmlContent, String xmlTagName, String expectedXmlTagValue) {
        int indexOf = xmlContent.indexOf(xmlTagName);
        if (indexOf > 0) {
            indexOf += xmlTagName.length();
            String xmlTagValue = xmlContent.substring(indexOf, indexOf + expectedXmlTagValue.length());
            Assert.assertEquals("Ungleicer Wert beim XML-Tag " + xmlTagName, expectedXmlTagValue, xmlTagValue);
        }
    }

    public void checkModification(File modifiedXmlFile, CrefoModifierResult crefoModifierResult) throws Exception {
        Assert.assertNotNull(crefoModifierResult);
        String xmlContent = FileUtils.readFileToString(modifiedXmlFile);
        checkTagValue(xmlContent, "<arc:crefonummer>", crefoModifierResult.getModifedCrefoNr().toString());
        checkTagValue(xmlContent, "<arc:ort>", "Düsseldorf");
        checkTagValue(xmlContent, "<arc:strasse>", "Kaiserstrasse");
        if (crefoModifierResult.isFirma()) {
            checkTagValue(xmlContent, "<arc:firmierung>", "KC-Firmierung");
            checkTagValue(xmlContent, "<arc:registernummer>", "0HR453-523245-AB");
        }
    }

    public List<Long> getCrefoNummersFromRsc(String rscFileName) throws IOException {
        List<Long> crefosList = new ArrayList<>();
        File rscFile = new File(rscDir, rscFileName);
        List<String> readLines = FileUtils.readLines(rscFile);
        for (String line : readLines) {
            try {
                Long crefoNr = Long.valueOf(line);
                crefosList.add(crefoNr);
            } catch (NumberFormatException e) {
                // is OK
            }
        }
        return crefosList;
    }

    public Map<Long, File> buildCrefoToXmlFilesFromRsc(String rscFileName, String fineNamePostfix) throws IOException {
        List<Long> crefosList = getCrefoNummersFromRsc(rscFileName);
        Map<Long, File> xmlFilesMap = new TreeMap<>();
        crefosList.stream().forEach(crefoNr -> {
            File xmlFile = new File(rscDir, crefoNr + fineNamePostfix + ".xml");
            xmlFilesMap.put(crefoNr, xmlFile);
        });
        return xmlFilesMap;
    }

    public Document createDocumentFromFile(File xmlFile) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFile);
            return document;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public File saveDocument(File rscDir, Long crefNr, Document xmlDocument) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(xmlDocument);
        File newCrefoFile = new File(rscDir, crefNr + ".xml");
        StreamResult streamResult = new StreamResult(newCrefoFile);
        transformer.transform(domSource, streamResult);
        return newCrefoFile;
    }
}
