package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.tesun.util.CrefoModifier;
import de.creditreform.crefoteam.cte.tesun.util.CrefoModifierResult;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrefoDownloadModifierUploadForInsoProblemTest extends CrefoModifierTestBase {
    public CrefoDownloadModifierUploadForInsoProblemTest() throws URISyntaxException {
        super("ENE", new File(CrefoModifierTestBase.class.getResource("/").toURI().getPath()));
    }

    @Test
    public void testInsoProblem() throws Exception {
        CrefoModifier crefoModifier = new CrefoModifier(TestSupportClientKonstanten.TEST_CLZ_412, crefoModifierListener) {
            @Override
            public CrefoModifierResult modifySpecific(Document crefoDocument, CrefoModifierResult crefoModifierResult) {
                NodeList negativmerkmalNodeList = crefoDocument.getElementsByTagName("arc:negativmerkmal");
                crefoModifierListener.nodifyModification(String.format("\tAnzahl Negativmerkmale: %d ", negativmerkmalNodeList.getLength()));
                if (negativmerkmalNodeList.getLength() < 1) {
                    return crefoModifierResult;
                }
                List<Node> targetNodesList = new ArrayList<>();
                for (int n = 0; n < negativmerkmalNodeList.getLength(); n++) {
                    Node negativmerkmalNode = negativmerkmalNodeList.item(n);
                    NodeList nodes = negativmerkmalNode.getChildNodes();
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node node = nodes.item(i);
                        if ("arc:aktenzeichen".equals(node.getNodeName())) {
                            targetNodesList.add(node);
                        }
                    }
                }
                if (targetNodesList.size() > 1) {
                    List<String> azList = new ArrayList<>();
                    for (Node node : targetNodesList) {
                        if (true || azList.contains(node.getTextContent())) {
                            String newAZ = "KC-" + System.nanoTime() + "-";
                            crefoModifierListener.nodifyModification(String.format("\t\t==> Neues Aktenzeichen: %s", newAZ));
                            node.setTextContent(newAZ);
                            azList.add(node.getTextContent());
                        }
                    }
                }
                return crefoModifierResult;
            }
        };
        crefoModifierListener.nodifyModification(String.format("*********** testInsoProblem ***************"));
        Map<Long, File> xmlFilesMap = buildCrefoToXmlFilesFromRsc("inso_crefos.tsv", "");
        xmlFilesMap.keySet().stream().forEach(crefoNr -> {
            try {
                File xmlFile = xmlFilesMap.get(crefoNr);
                // download and save Crefo
                crefoModifierListener.nodifyModification(String.format("\nDownload der Crefo %d...", crefoNr));
                String crefoContent = tesunRestServiceWLS.downloadCrefo(crefoNr);
                crefoModifierListener.nodifyModification(String.format("Speichere als Datei %s...", xmlFile.getAbsolutePath()));
                FileUtils.writeStringToFile(xmlFile, crefoContent);
                // modify crefo
                Document document = createDocumentFromFile(xmlFile);
                CrefoModifierResult crefoModifierResult = new CrefoModifierResult();
                crefoModifierListener.nodifyModification(String.format("Modfiziere die XML-Datei für die Crefo %d...", crefoNr));
                crefoModifier.doModificatios(document, crefoModifierResult);

                File modifiedXmlFile = saveDocument(modifedXmlsDir, crefoModifierResult.getModifedCrefoNr(), crefoModifierResult.getXmlDocument());
                crefoModifierListener.nodifyModification(String.format("Speichere die modifizierte XML-Datei %s...", modifiedXmlFile.getAbsolutePath()));
                checkModification(modifiedXmlFile, crefoModifierResult);
                // save and upload Crefo
                crefoModifierListener.nodifyModification(String.format("Upload der modifizierten Crefo %d...", crefoModifierResult.getModifedCrefoNr()));
                tesunRestServiceWLS.uploadCrefo(crefoModifierResult.getModifedCrefoNr(), modifiedXmlFile, AB3_0_XSD);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        crefoModifierListener.nodifyModification(String.format("----------- testInsoProblem ---------------"));
    }

}
