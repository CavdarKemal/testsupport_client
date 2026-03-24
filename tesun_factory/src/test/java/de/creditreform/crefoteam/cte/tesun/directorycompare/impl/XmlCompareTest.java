package de.creditreform.crefoteam.cte.tesun.directorycompare.impl;

import de.creditreform.crefoteam.cte.tesun.directorycompare.DiffListener;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.NodeDetail;
import org.easymock.IMockBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * Test-Klasse für {@link XmlCompareImpl}
 * User: ralf
 * Date: 16.06.14
 * Time: 11:06
 */
public class XmlCompareTest {
  private final String pathPrefix = "./src/test/resources/";

    private Difference createDifference(int pos, boolean invoked) {
        /*
        IMockBuilder<Difference> mb = createMockBuilder(Difference.class);
        mb.addMockedMethod("getDescription");
        Difference diff = mb.createMock("Difference_" + pos);
        if (invoked) {
            expect(diff.getDescription()).andReturn(String.valueOf(pos)).times(2);
        }
        */
        Difference diff = createMock( Difference.class );
        if (invoked) {
          expect( diff.getControlNodeDetail() ).andReturn( new NodeDetail( "", null, ("Difference_" + pos ) ));
          expect( diff.getTestNodeDetail()).andReturn( new NodeDetail( "", null, ("Difference_" + pos ) ));
          expect(diff.getDescription()).andReturn(String.valueOf(pos));
        }
        replay(diff);
        return diff;
    }

    private DetailedDiff createDetailedDiff(List<Difference> diffs) {
        IMockBuilder<DetailedDiff> mb = createMockBuilder(DetailedDiff.class);
        mb.addMockedMethod("getAllDifferences");
        DetailedDiff detailedDiff = mb.createMock("DetailedDiff");
        expect(detailedDiff.getAllDifferences()).andReturn(diffs).once();
        replay(detailedDiff);
        return detailedDiff;
    }

    @Test
    public void testMapDifferences() throws Exception {
        List<String> reportedDiffsLimited = mapMockDifferences(3, 5);
        Assert.assertEquals("Anzahl der Ergebnisse sollte maxRows+1 sein", 4, reportedDiffsLimited.size());
        Assert.assertEquals("Markierung für ausgelassene Differenzen nicht gesetzt", XmlCompareImpl.CONTINUATION_MARKER, reportedDiffsLimited.get(3));

        List<String> reportedDiffsNoLimit = mapMockDifferences(5, 5);
        Assert.assertEquals("Anzahl der Ergebnisse sollte maxRows sein", 5, reportedDiffsNoLimit.size());
        Assert.assertNotEquals("Markierung für ausgelassene Differenzen hier nicht zulässig", XmlCompareImpl.CONTINUATION_MARKER, reportedDiffsNoLimit.get(4));

    }

    private List<String> mapMockDifferences(int maxRows, int sourceRows) throws TransformerException, ParserConfigurationException {
        XmlCompareImpl cut = new XmlCompareImpl(null);
        List<Difference> diffs = new ArrayList<>();
        for (int i=0; i<sourceRows; i++) {
            diffs.add(createDifference(i, i<maxRows));
        }
        final DetailedDiff detailedDiff = createDetailedDiff(diffs);
        List<String> reportedDiffs = cut.mapDifferences(maxRows, detailedDiff);
        for (int i=0; i<maxRows; i++) {
            String actual = reportedDiffs.get(i);
            String expected = String.format("Element 'Difference_%d' hat unterschiedlichen Wert!\n\tTest: \n\tCtrl: \n\n", i);
            Assert.assertEquals("Fehlermeldung nicht korrekt oder in der falschen Reihenfolge übernommen", expected, actual);
        }
        verify(detailedDiff);
        for (Difference d : diffs) {
            verify(d);
        }
        return reportedDiffs;
    }

    private DiffListener createDiffListenerCompare() {
        DiffListener diffListener = createMock(DiffListener.class);
        diffListener.different(eq("drd"), eq("2032000385.xml"), (List<String>) anyObject());
        replay(diffListener);
        return diffListener;
    }

    @Test
    public void testCompare()
    throws IOException {
        XmlCompareImpl cut = new XmlCompareImpl(createDiffListenerCompare());
        cut.compareXml("drd", "2032000385.xml",
                       toByteArray(pathPrefix + "DirectoryCompareTest/xml_entpackt/drd1_2032000385.xml"),
                       toByteArray(pathPrefix + "DirectoryCompareTest/xml_entpackt/drd2_2032000385.xml"));
    }

    private byte[] toByteArray(String fileName)
    throws IOException {
        return IOUtils.toByteArray(new FileInputStream(fileName));
    }
}
