package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.tesun.util.CrefoModifier;
import de.creditreform.crefoteam.cte.tesun.util.CrefoModifierResult;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

public class CrefoDownloadModifierUploadForIkarosProblemTest extends CrefoModifierTestBase {
    public CrefoDownloadModifierUploadForIkarosProblemTest() throws URISyntaxException {
        super("ENE", new File(CrefoModifierTestBase.class.getResource("/").toURI().getPath() + "/Incident-17516"));
    }

    @Test
    public void testCrefoModifierStandard() throws Exception {
        CrefoModifier crefoModifier = new CrefoModifier(TestSupportClientKonstanten.TEST_CLZ_412, crefoModifierListener) {
            @Override
            public CrefoModifierResult modifySpecific(Document crefoDocument, CrefoModifierResult crefoModifierResult) {
                return crefoModifierResult;
            }
        };
        File xmlFile = new File(rscDir, "8330577245_BESTAND__v3_0.xml");
        CrefoModifierResult crefoModifierResult = new CrefoModifierResult();
        Document document = createDocumentFromFile(xmlFile);
        crefoModifier.doModificatios(document, crefoModifierResult);
        File modifiedXmlFile = saveDocument(modifedXmlsDir, crefoModifierResult.getModifedCrefoNr(), crefoModifierResult.getXmlDocument());
        checkModification(modifiedXmlFile, crefoModifierResult);
    }

    @Test
    public void testIkarosProblem() throws Exception {
        crefoModifierListener.nodifyModification(String.format("*********** testIkarosProblem ***************"));
        boolean uploadCrefos = true;
        // Crefos aus dem RSC-Verzeichnis anonymisieren...
        CrefoModifier crefoModifier = new CrefoModifier(TestSupportClientKonstanten.TEST_CLZ_412, crefoModifierListener) {
            @Override
            public CrefoModifierResult modifySpecific(Document crefoDocument, CrefoModifierResult crefoModifierResult) {
                return crefoModifierResult;
            }
        };
        final Map<Long, Long> modifiedCrefosMap = doModifyAndBuildMap(crefoModifier, uploadCrefos);

        // modifiziere auch die Beteiligten aus modifiedCrefosMap
        CrefoModifier crefoModifier2 = new CrefoModifier(TestSupportClientKonstanten.TEST_CLZ_412, crefoModifierListener) {
            @Override
            public CrefoModifierResult modifySpecific(Document crefoDocument, CrefoModifierResult crefoModifierResult) {
                modifyBeteiligten(crefoDocument, modifiedCrefosMap, crefoModifierResult);
                return crefoModifierResult;
            }
        };
        File xmlFile = new File(rscDir, "8330572509_BESTAND__v3_0.xml");
        crefoModifierListener.nodifyModification(String.format("Modfiziere die XML-Datei %s...", xmlFile));
        CrefoModifierResult crefoModifierResult = new CrefoModifierResult();
        Document document = createDocumentFromFile(xmlFile);
        crefoModifier2.doModificatios(document, crefoModifierResult);

        // save and uploadCrefos Crefo
        File modifiedXmlFile = saveDocument(modifedXmlsDir, crefoModifierResult.getModifedCrefoNr(), crefoModifierResult.getXmlDocument());
        crefoModifierListener.nodifyModification(String.format("Speichere die modifizierte XML-Datei %s...", modifiedXmlFile.getAbsolutePath()));
        checkModification(modifiedXmlFile, crefoModifierResult);
        if (uploadCrefos) {
            crefoModifierListener.nodifyModification(String.format("Upload der modifizierten Crefo %d...", crefoModifierResult.getModifedCrefoNr()));
            tesunRestServiceWLS.uploadCrefo(crefoModifierResult.getModifedCrefoNr(), modifiedXmlFile, AB3_0_XSD);
        }
        crefoModifierListener.nodifyModification(String.format("----------- testIkarosProblem ---------------"));
    }

    private Map<Long, Long> doModifyAndBuildMap(CrefoModifier crefoModifier, boolean uploadCrefos) throws IOException {
        Map<Long, Long> modifiedCrefosMap = new TreeMap<>();
        Map<Long, File> xmlFilesMap = buildCrefoToXmlFilesFromRsc("970BeteiligteBei6070312009.tsv", "_BESTAND__v3_0");
        xmlFilesMap.entrySet().stream().forEach(theEntry -> {
            try {
                File xmlFile = new File(theEntry.getValue().toString());
                crefoModifierListener.nodifyModification(String.format("Modfiziere die XML-Datei %s...", xmlFile));
                CrefoModifierResult crefoModifierResult = new CrefoModifierResult();
                Document document = createDocumentFromFile(xmlFile);
                crefoModifier.doModificatios(document, crefoModifierResult);
                // save and upload Crefo
                File modifiedXmlFile = saveDocument(modifedXmlsDir, crefoModifierResult.getModifedCrefoNr(), crefoModifierResult.getXmlDocument());
                crefoModifierListener.nodifyModification(String.format("Speichere die modifizierte XML-Datei %s...", modifiedXmlFile.getAbsolutePath()));
                checkModification(modifiedXmlFile, crefoModifierResult);
                modifiedCrefosMap.put(crefoModifierResult.getOriginaldCrefoNr(), crefoModifierResult.getModifedCrefoNr());
                if (uploadCrefos) {
                    crefoModifierListener.nodifyModification(String.format("Upload der modifizierten Crefo %d...", crefoModifierResult.getModifedCrefoNr()));
                    tesunRestServiceWLS.uploadCrefo(crefoModifierResult.getModifedCrefoNr(), modifiedXmlFile, AB3_0_XSD);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return modifiedCrefosMap;
    }

}
