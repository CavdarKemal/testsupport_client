package de.creditreform.crefoteam.cte.tesun.util;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.NodeDetail;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;

/**
 * Utility-Klasse zur Ausgabe der {@link Difference}-Instanzen aus
 * XMLUnit
 * <p>
 * User: cavdark
 * Date: 16.06.14
 * Time: 14:58
 */
public class XmlDiffFormatter {

    public StringBuilder appendDifferences(StringBuilder sBuilder, String prefix, List<Difference> differenceList) throws Exception {
        for (Difference theDifference : differenceList) {
            sBuilder = appendDifference(sBuilder, prefix, theDifference);
        }
        return sBuilder;
    }

    public StringBuilder appendDifference(StringBuilder sBuilder, String prefix, Difference theDifference) throws ParserConfigurationException, TransformerException {
        final String description = theDifference.getDescription();
        final NodeDetail controlNodeDetail = theDifference.getControlNodeDetail();
        String xpathLocation = (controlNodeDetail != null) ? controlNodeDetail.getXpathLocation() : "";
        String controlXpathLocation = (xpathLocation != null) ? xpathLocation.replace("/text()[1]", "") : null;
        final NodeDetail testNodeDetail = theDifference.getTestNodeDetail();
        final String testXpathLocation = (testNodeDetail != null) ? testNodeDetail.getXpathLocation() : null;
        sBuilder.append(prefix + "  Element '");
        if (description.equals("number of child nodes")) {
            sBuilder.append(controlXpathLocation);
            sBuilder.append("' hat unterschiedliche Anzahl an Child-Elementen ::");
            sBuilder.append(" Test: ");
            sBuilder.append((controlNodeDetail != null) ? controlNodeDetail.getValue() : "?");
            sBuilder.append("  Ctrl: ");
            sBuilder.append((testNodeDetail != null) ? testNodeDetail.getValue() : "????");
        } else if (description.equals("sequence of child nodes")) {
            sBuilder.append(controlXpathLocation);
            sBuilder.append("' ist an unterschiedlicher Stelle!");
        } else if (description.equals("presence of child node")) {
            if (controlXpathLocation != null) {
                sBuilder.append(controlXpathLocation);
                sBuilder.append("' existiert nur im Test-XML und fehlt im Control-XML!");
            } else if (testXpathLocation != null) {
                sBuilder.append(testXpathLocation);
                sBuilder.append("' existiert nur im Control-XML und fehlt im Test-XML!");
            }
        } else {
            if (description.equals("attribute value")) {
                sBuilder.append("-Attribut '");
            }
            sBuilder.append(controlXpathLocation);
            sBuilder.append("' hat unterschiedlichen Wert :: ");
            sBuilder.append(prefix + "  Test: ");
            sBuilder.append((controlNodeDetail != null) ? controlNodeDetail.getValue() : "?");
            sBuilder.append(prefix + "  Ctrl: ");
            sBuilder.append((testNodeDetail != null) ? testNodeDetail.getValue() : "`????");
        }
        return sBuilder;
    }

}
