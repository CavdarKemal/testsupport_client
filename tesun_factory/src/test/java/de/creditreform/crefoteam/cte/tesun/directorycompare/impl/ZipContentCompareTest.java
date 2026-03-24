package de.creditreform.crefoteam.cte.tesun.directorycompare.impl;

import de.creditreform.crefoteam.cte.tesun.directorycompare.DiffListener;
import de.creditreform.crefoteam.cte.tesun.directorycompare.DirectoryScanResult;
import de.creditreform.crefoteam.cte.tesun.directorycompare.XmlCompare;
import org.junit.Test;

import java.io.File;

import static org.easymock.EasyMock.*;

/**
 * Test-Klasse für {@link ZipContentCompareImpl}
 * User: ralf
 * Date: 16.06.14
 * Time: 12:32
 */
public class ZipContentCompareTest {
    protected static final String pathPrefix = "./src/test/resources/";
    protected static final String FILENAME_FIRST = pathPrefix + "DirectoryCompareTest/vsh_export_1/203/CLZ_203_ab_0600000.zip";
    protected static final String FILENAME_SECOND = pathPrefix + "DirectoryCompareTest/vsh_export_2/203/CLZ_203_ab_0600000.zip";
    protected static final String NAME_PREFIX_ZIP = "DirectoryCompareTest_vsh_203_CLZ_203_ab_0600000.zip";

    private DiffListener createDiffListener() {
        DiffListener diffListener = createMock(DiffListener.class);
        replay(diffListener);
        return diffListener;
    }

    private XmlCompare createXmlCompare() {
        XmlCompare xmlCompare = createMock(XmlCompare.class);
        xmlCompare.compareXml(eq(NAME_PREFIX_ZIP), eq("beteiligter_7330694291.xml"), (byte[]) anyObject(), (byte[]) anyObject());
        expectLastCall().once();
        xmlCompare.compareXml(eq(NAME_PREFIX_ZIP), eq("beteiligter_7330694626.xml"), (byte[]) anyObject(), (byte[]) anyObject());
        expectLastCall().once();
        replay(xmlCompare);
        return xmlCompare;
    }

    private DirectoryScanResult getScanResult(String fileName) {
        File f = new File(fileName);
        File dir = new File("./src/test/resources");
        return new DirectoryScanResult(dir, f);
    }

    @Test
    public void testCompareZip() {
        ZipContentCompareImpl cut = new ZipContentCompareImpl(createDiffListener(), createXmlCompare());
        cut.compareZipFiles(NAME_PREFIX_ZIP, getScanResult(FILENAME_FIRST), getScanResult(FILENAME_SECOND));
    }
}
