package de.creditreform.crefoteam.cte.tesun.util;

import org.w3c.dom.Document;

import java.io.File;

public class CrefoModifierResult {
    private Document xmlDocument;
    private Long originaldCrefoNr;
    private Long modifedCrefoNr;
    boolean isFirma;

    public void setXmlDocument(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
    }
    public Document getXmlDocument() {
        return xmlDocument;
    }

    public Long getModifedCrefoNr() {
        return modifedCrefoNr;
    }

    public void setModifedCrefoNr(Long modifedCrefoNr) {
        this.modifedCrefoNr = modifedCrefoNr;
    }

    public Long getOriginaldCrefoNr() {
        return originaldCrefoNr;
    }

    public void setOriginaldCrefoNr(Long originaldCrefoNr) {
        this.originaldCrefoNr = originaldCrefoNr;
    }

    public boolean isFirma() {
        return isFirma;
    }

    public void setFirma(boolean firma) {
        isFirma = firma;
    }

}
