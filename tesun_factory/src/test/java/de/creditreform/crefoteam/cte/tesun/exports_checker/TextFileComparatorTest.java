package de.creditreform.crefoteam.cte.tesun.exports_checker;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.NodeDetail;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

public class TextFileComparatorTest {
    TextFileComparator textFileComparator = new TextFileComparator();
    List<Difference> differenceList;
    String ctrl = "";
    String test = "";

    @Test
    public void testCompareCSVWithEmptyContents() {
        ctrl = "";
        test = "";
        differenceList = textFileComparator.compareCSV(ctrl, test);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Bei leeren Control- und Test-Contents dürfen keine Unterschiede existieren!", 0, differenceList.size());
    }

    @Test
    public void testCompareCSVDiffNumOfLines() {
        ctrl = "COL-0;COL-1;COL-2\nEinsZweiDrei;VierFünfSechs;SiebenAchtNeun\nZweiDrei;FünfSechs;AchtNeun\nDrei;Sechs;Neun";
        test = "COL-0;COL-1;COL-2\nEinsZweiDrei;VierFünfSechs;SiebenAchtNeun\nZweiDrei;FünfSechs;AchtNeun";
        differenceList = textFileComparator.compareCSV(ctrl, test);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Es müsste genau ein Unterschied existieren!", 1, differenceList.size());
        final Difference difference = differenceList.get(0);
        Assert.assertTrue("", difference.getDescription().equals("Zeilenanzahl bei Control(=4) und Test(=3) sind unterschiedlich!"));
    }

    @Test
    public void testCompareCSVDiffNumOfColumns() {
        ctrl = "COL-0;COL-1;COL-2\nEinsZweiDrei;SiebenAchtNeun              \nZweiDrei;FünfSechs;AchtNeun\nDrei;Sechs;Neun";
        test = "COL-0;COL-1;COL-2\nEinsZweiDrei;VierFünfSechs;SiebenAchtNeun\nZweiDrei;          AchtNeun\n           Neun";
        differenceList = textFileComparator.compareCSV(ctrl, test);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Es müssten genau sechs Unterschiede existieren!", 6, differenceList.size());
        Assert.assertTrue("Diff-Bezeichnung 0 falsch!", differenceList.get(0).getDescription().equals("Spaltenanzahl bei Control(=2) und Test(=3) der Zeile 2 sind unterschiedlich!"));
        Assert.assertTrue("Diff-Bezeichnung 1 falsch!", differenceList.get(1).getDescription().equals("Spalte 2 des Control(=SiebenAchtNeun) und Test(=VierFünfSechs) der Zeile 2 sind unterschiedlich!"));
        Assert.assertTrue("Diff-Bezeichnung 2 falsch!", differenceList.get(2).getDescription().equals("Spaltenanzahl bei Control(=3) und Test(=2) der Zeile 3 sind unterschiedlich!"));
        Assert.assertTrue("Diff-Bezeichnung 3 falsch!", differenceList.get(3).getDescription().equals("Spalte 2 des Control(=FünfSechs) und Test(=AchtNeun) der Zeile 3 sind unterschiedlich!"));
        Assert.assertTrue("Diff-Bezeichnung 4 falsch!", differenceList.get(4).getDescription().equals("Spaltenanzahl bei Control(=3) und Test(=1) der Zeile 4 sind unterschiedlich!"));
        Assert.assertTrue("Diff-Bezeichnung 5 falsch!", differenceList.get(5).getDescription().equals("Spalte 1 des Control(=Drei) und Test(=Neun) der Zeile 4 sind unterschiedlich!"));
    }

    @Test
    public void testCompareTXTWithEmptyContents() {
        ctrl = "";
        test = "";
        differenceList = textFileComparator.compareTXT(ctrl, test);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Bei leeren Control- und Test-Contents dürfen keine Unterschiede existieren!", 0, differenceList.size());
    }

    @Test
    public void testCompareTXTWithDifferentContents() {
        ctrl = "Content1-Col-1     Content1-Col-2              Content1-Col-3";
        test = "Content2-Col-1     Content1-Col-2              Content1-Col-3";
        differenceList = textFileComparator.compareTXT(ctrl, test);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Es müsste ein Unterschied existieren!", 1, differenceList.size());
        Assert.assertTrue("Diff-Bezeichnung 0 falsch!", differenceList.get(0).getDescription().equals("Control und Test haben unterschiedlichen Inhalt!"));
    }

    @Test
    public void testCompareXMLWithDifferentContents() throws IOException, SAXException {
        ctrl = "<bvd-firmendaten>\n\t<crefonummer>9379000010</crefonummer>\n\t<selektions-typ>Markus-A</selektions-typ>\n</bvd-firmendaten>";
        test = "<bvd-firmendaten>\n\t<crefonummer>9379000011</crefonummer>\n\t<selektions-typ>Markus-B</selektions-typ>\n</bvd-firmendaten>";
        differenceList = textFileComparator.compareContent("filename.xml", ctrl, test, null);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Es müsste ein Unterschied existieren!", 2, differenceList.size());
        final Difference difference0 = differenceList.get(0);
        final NodeDetail controlNodeDetail = difference0.getControlNodeDetail();
        controlNodeDetail.getValue();
        controlNodeDetail.getXpathLocation();

        final NodeDetail testNodeDetail = difference0.getTestNodeDetail();
    }

    @Test
    public void testCompareContentWithEmpyContents() throws IOException, SAXException {
        differenceList = textFileComparator.compareContent("filename.txt", ctrl, test, null);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Bei leeren Control- und Test-Contents dürfen keine Unterschiede existieren!", 0, differenceList.size());

        differenceList = textFileComparator.compareContent("filename.csv", ctrl, test, null);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Bei leeren Control- und Test-Contents dürfen keine Unterschiede existieren!", 0, differenceList.size());

        differenceList = textFileComparator.compareContent("filename.xml", ctrl, test, null);
        Assert.assertNotNull(differenceList);
        Assert.assertEquals("Bei leeren Control- und Test-Contents dürfen keine Unterschiede existieren!", 0, differenceList.size());
    }

    @Test
    public void testCompareContentWithDifferentContents() throws IOException, SAXException {
        ctrl = "Content1-Col-1     Content1-Col-2              Content1-Col-3";
        test = "Content2-Col-1     Content1-Col-2              Content1-Col-4";
        differenceList = textFileComparator.compareContent("filename.txt", ctrl, test, null);
        Assert.assertFalse("Es müsste(n) Unterschied(e) existieren!", differenceList.isEmpty());

        ctrl = "COL-0;COL-1;COL-2\nEinsZweiDrei;VierFünfSechs;SiebenAchtNeun\nZweiDrei;FünfSechs;AchtNeun\nZwei;Sechs;Neun";
        test = "COL-0;COL-1;COL-2\nEinsZweiNeun;VierFünfSechs;SiebenAchtDrei\nZweiDrei;FünfSechs;AchtNeun\nDrei;Sechs;Eins";
        differenceList = textFileComparator.compareContent("filename.csv", ctrl, test, null);
        Assert.assertFalse("Es müsste(n) Unterschied(e) existieren!", differenceList.isEmpty());

        ctrl = "<bvd-firmendaten>\n\t<crefonummer>9379000010</crefonummer>\n\t<selektions-typ>Markus-A</selektions-typ>\n</bvd-firmendaten>";
        test = "<bvd-firmendaten>\n\t<crefonummer>9379000011</crefonummer>\n\t<selektions-typ>Markus-B</selektions-typ>\n</bvd-firmendaten>";
        differenceList = textFileComparator.compareContent("filename.xml", ctrl, test, null);
        Assert.assertFalse("Es müsste(n) Unterschied(e) existieren!", differenceList.isEmpty());
    }

}
