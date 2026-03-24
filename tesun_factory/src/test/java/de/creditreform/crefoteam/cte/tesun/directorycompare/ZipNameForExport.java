package de.creditreform.crefoteam.cte.tesun.directorycompare;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Liste real existiernder Export-Verzeichnisse und der für die Tests
 * relevanten Daten
 * User: ralf
 * Date: 11.06.14
 * Time: 09:52
 */
public class ZipNameForExport {

    /**
     * Die Dateinamen in der Liste MATCHING_ZIPS entsprechen zueinander
     * passenden Zip-Dateien
     */
    public static final List<ZipNameForExport> MATCHING_ZIPS = Collections.unmodifiableList(Arrays.asList(
        new ZipNameForExport("Bedirect", 1, 58, "\\\\fileserver.gee.creditreform.de\\gee\\bedirect\\export\\full\\2014-01-27_11-30\\bedirect_beteiligung_export_2012000000_000.zip",
                                                "\\\\fileserver.gee.creditreform.de\\gee\\bedirect\\export\\full\\2014-05-22_15-25\\bedirect_beteiligung_export_2012000000_000.zip"),
        new ZipNameForExport("BvD",      1, 53, "\\\\fileserver.gee.creditreform.de\\gee\\bvd\\export\\full\\2014-05-26_11-30\\abCrefo_2011000000.zip",
                                                "//fileserver.gee.creditreform.de/gee/bvd/export/full/2013-10-09_11-03/abCrefo_2011000000.zip"),
        new ZipNameForExport("Coface",   1, 53, "\\\\fileserver.gee.creditreform.de\\gee\\cef\\export\\full\\2014-05-26_11-30\\abCrefo_2012000000.zip",
                                                "\\\\fileserver.gee.creditreform.de\\gee\\cef\\export\\full\\2013-09-26_08-49\\abCrefo_2012000000.zip"),
        new ZipNameForExport("CTC",      1, 53, "\\\\fileserver.gee.creditreform.de\\gee\\ctc\\export\\full\\2014-05-26_11-30\\abCrefo201.0900000_createAndUpdate.zip",
                                                "\\\\fileserver.gee.creditreform.de\\gee\\ctc\\export\\full\\2013-09-02_11-29\\abCrefo201.0900000_createAndUpdate.zip"),
        new ZipNameForExport("DRD",      1, 53, "\\\\fileserver.gee.creditreform.de\\gee\\drd\\export\\full\\2013-06-18_08-58\\20130618_0853_abCrefo201.2000000_createAndUpdate.zip",
                                                "\\\\fileserver.gee.creditreform.de\\gee\\drd\\export\\full\\2014-05-22_13-18\\20140522_1313_abCrefo201.2000000_createAndUpdate.zip"),
        new ZipNameForExport("FiWi",     1, 52, "\\\\fileserver.gee.creditreform.de\\gee\\fw\\export\\full\\2013-12-19_09-47\\abCrefo_2011000000.zip",
                                                "\\\\fileserver.gee.creditreform.de\\gee\\fw\\export\\full\\2014-05-26_11-30\\abCrefo_2011000000.zip"),
        new ZipNameForExport("GKM",      2, 47, "\\\\fileserver.gee.creditreform.de\\gee\\gkm_export\\2012-12-11\\ABBLeasing\\gkm-lieferung-00001.xml.zip",
                                                "\\\\fileserver.gee.creditreform.de\\gee\\gkm_export\\2012-12-19\\ABBLeasing\\gkm-lieferung-00001.xml.zip"),
        new ZipNameForExport("VSH",      2, 53, "\\\\fileserver.gee.creditreform.de\\gee\\vsh\\export\\full\\2013-06-03_10-33\\201\\CLZ_201_ab_2000000.zip",
                                                "\\\\fileserver.gee.creditreform.de\\gee\\vsh\\export\\full\\2014-05-26_11-30\\201\\CLZ_201_ab_2000000.zip")
        ));

    public static final List<ZipNameForExport> NON_MATCHING_ZIPS = Collections.unmodifiableList(Arrays.asList(
    new ZipNameForExport("VSH",          2, 53, "\\\\fileserver.gee.creditreform.de\\gee\\vsh\\export\\full\\2013-06-03_10-33\\201\\CLZ_201_ab_2000000.zip",
                                                "\\\\fileserver.gee.creditreform.de\\gee\\vsh\\export\\full\\2014-05-26_11-30\\411\\CLZ_411_ab_2000000.zip")
    ));

    private final String anwendungsFall;
    private final String pfadParent;
    private final String pfadFirst;
    private final String nameFirst;
    private final String pfadSecond;
    private final String nameSecond;

    private ZipNameForExport(String anwendungsFall, int anzSlashes, int lengthParent, String pfadFirst, String pfadSecond) {
        this.anwendungsFall = anwendungsFall;
        this.pfadParent = pfadFirst.substring(0, lengthParent);
        this.pfadFirst = pfadFirst;
        this.nameFirst = extractName(anzSlashes, pfadFirst);
        this.pfadSecond = pfadSecond;
        this.nameSecond = extractName(anzSlashes, pfadSecond);
    }

    private final String extractName(int anzSlashes, String sourcePath) {
        final String pfad = sourcePath.replace('\\','/');
        int pos = pfad.length();
        for (int i=0; i<anzSlashes; i++) {
            pos = pfad.lastIndexOf('/', pos);
        }
        return pfad.substring(pos+1);
    }

    public String getAnwendungsFall() {
        return anwendungsFall;
    }

    public String getPfadParent() {
        return pfadParent;
    }

    public String getPfadFirst() {
        return pfadFirst;
    }

    public String getNameFirst() {
        return nameFirst;
    }

    public String getPfadSecond() {
        return pfadSecond;
    }

    public String getNameSecond() {
        return nameSecond;
    }
}

