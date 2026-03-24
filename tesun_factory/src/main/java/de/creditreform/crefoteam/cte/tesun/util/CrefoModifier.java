package de.creditreform.crefoteam.cte.tesun.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Map;

public abstract class CrefoModifier {
    private final String strPseutoClz;
    private boolean isFirma;
    private final CrefoModifierListener crefoModifierListener;

    protected CrefoModifier(String strPseutoClz, CrefoModifierListener crefoModifierListener) {
        this.strPseutoClz = strPseutoClz;
        this.crefoModifierListener = crefoModifierListener;
    }

    public abstract CrefoModifierResult modifySpecific(Document crefoDocument, CrefoModifierResult crefoModifierResult);

    public void doModificatios(Document crefoDocument, CrefoModifierResult crefoModifierResult) {
        crefoModifierResult.setXmlDocument(crefoDocument);

        anomymizeDocument(crefoDocument, crefoModifierResult);
        modifyCrefoNummer(crefoDocument, crefoModifierResult);

        // abstrakte Methode aufrufen
        crefoModifierResult = modifySpecific(crefoDocument, crefoModifierResult);
    }

    private void anomymizeDocument(Document crefoDocument, CrefoModifierResult crefoModifierResult) {
        changeNodeValue(crefoDocument, "arc:ort", "Düsseldorf");
        changeNodeValue(crefoDocument, "arc:strasse", "Kaiserstrasse");

        NodeList nodeList = crefoDocument.getElementsByTagName("arc:firma");
        isFirma = nodeList.item(0).getTextContent().equals("true");
        if (isFirma) {
            changeNodeValue(crefoDocument, "arc:firmierung", "KC-Firmierung");
            changeNodeValue(crefoDocument, "arc:registernummer", "0HR453-523245-AB");
        }
        crefoModifierResult.setFirma(isFirma);
    }

    private void modifyCrefoNummer(Document crefoDocument, CrefoModifierResult crefoModifierResult) {
        NodeList nodeList = crefoDocument.getElementsByTagName("arc:crefonummer");
        Node crefoNode = nodeList.item(0);
        String oldCrefoNr = crefoNode.getTextContent();
        String newCrefoNr = strPseutoClz + oldCrefoNr.toString().substring(3);
        crefoNode.setTextContent(newCrefoNr.toString());
        crefoModifierResult.setOriginaldCrefoNr(Long.valueOf(oldCrefoNr));
        crefoModifierResult.setModifedCrefoNr(Long.valueOf(newCrefoNr));
        crefoModifierListener.nodifyModification(String.format("\t XML-Dag '%s' modifiziert: '%s' --> '%s'", "crefonummer", oldCrefoNr, newCrefoNr));
    }

    protected void modifyBeteiligten(Document document, Map<Long, Long> modifiedCrefosMap, CrefoModifierResult crefoModifierResult) {
        crefoModifierListener.nodifyModification("\tModifizere Beteilgten-Crefos der Crefo-XML-Datei...");
        NodeList nodeList = document.getElementsByTagName("arc:crefonummer-beteiligter");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String oldBtlgCrefo = item.getTextContent();
            Long newBtlgCrefo = modifiedCrefosMap.get(Long.valueOf(oldBtlgCrefo));
            if (newBtlgCrefo != null) {
                item.setTextContent(newBtlgCrefo.toString());
                crefoModifierListener.nodifyModification(String.format("\t\tXML-Dag '%s' modifiziert: '%s' --> '%s'", "crefonummer-beteiligter", oldBtlgCrefo, newBtlgCrefo));
            } else {
                crefoModifierListener.nodifyModification(String.format("\t\t\t!!! Kein Map-Eintrag für Btlg-Crefo %s", oldBtlgCrefo));
            }
        }
    }

    private void changeNodeValue(Document document, String nodeName, String newValue) {
        NodeList nodeList = document.getElementsByTagName(nodeName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String oldValue = item.getTextContent();
            item.setTextContent(newValue);
            crefoModifierListener.nodifyModification(String.format("\t XML-Dag '%s' modifiziert: '%s' --> '%s'", nodeName, oldValue, newValue));
        }
    }

}
