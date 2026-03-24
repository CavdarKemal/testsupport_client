package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.generate.TestFallExtendsArchivBestandCrefos;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.*;

public class TestFallExtendsArchivBestandCrefosTest extends UserTaskTestBase {
    File alternateSourceDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        URI uri = getClass().getResource("/TESTS/LOCAL/ALTERNATE-SOURCE").toURI();
        alternateSourceDir = new File(uri);
    }

    @Test
    public void testForPhase1() throws Exception {
        doTheTestForPhase(TestSupportClientKonstanten.TEST_PHASE.PHASE_1,
                Arrays.asList(
                        "4112000001::[RTN],[],[4112000008;4112000009],[KEINE],[KEINE],[],[]",
                        "4112000002::[RTN],[],[4112000003],[KEINE],[KEINE],[],[]",
                        "4112000003::[],[],[],[KEINE],[KEINE],[],[]",
                        "4112000008::[],[],[],[KEINE],[KEINE],[],[]",
                        "4112000009::[],[],[],[KEINE],[KEINE],[],[]"),
                Arrays.asList("RTN", "\t[4112000002]"),
                Arrays.asList("4112000001.xml", "4112000002.xml"));
    }

    @Test
    public void testForPhase2() throws Exception {
        doTheTestForPhase(TestSupportClientKonstanten.TEST_PHASE.PHASE_2,
                Arrays.asList(
                        "4112000001::[RTN],[],[4112000008;4112000009],[KEINE],[KEINE],[],[]",
                        "4112000002::[RTN],[],[4112000003],[KEINE],[KEINE],[],[]",
                        "4112000003::[],[],[],[KEINE],[KEINE],[],[]",
                        "4112000004::[BVD;RTN],[],[4112000007],[KEINE],[KEINE],[],[]",
                        "4112000005::[BVD;RTN],[],[],[KEINE],[KEINE],[],[]",
                        "4112000006::[BVD;RTN],[],[],[KEINE],[KEINE],[],[]",
                        "4112000007::[BVD],[],[],[KEINE],[KEINE],[],[]",
                        "4112000008::[BVD],[],[],[KEINE],[KEINE],[],[]",
                        "4112000009::[BVD],[],[4112000002;4112000003],[KEINE],[KEINE],[],[]"),
                Arrays.asList("BVD", "\t[4112000009, 4112000004, 4112000005, 4112000006, 4112000007]", "RTN", "\t[4112000002, 4112000004, 4112000005, 4112000006]"),
                Arrays.asList("4112000001.xml", "4112000002.xml", "4112000003.xml", "4112000004.xml", "4112000005.xml", "4112000006.xml", "4112000008.xml"));
    }

    protected void doTheTestForPhase(TestSupportClientKonstanten.TEST_PHASE testPhase,
                                     List<String> expectedExtendedTestCrefosContentList,
                                     List<String> expectedCrefosToCustomersMapContentList,
                                     List<String> expectedXmlsFilenamesList) throws Exception {
        // Dateien im target löschen
        final File ab30XmlsDir = environmentConfig.getArchivBestandsRoot(testPhase);
        deleteFilesInDir(ab30XmlsDir);

        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = new TreeMap<>();
        activeTestCustomersMapMap.put(testPhase, getTestCustomerMap(testPhase, false));
        TestFallExtendsArchivBestandCrefos cut = new TestFallExtendsArchivBestandCrefos(activeTestCustomersMapMap, false, alternateSourceDir, this);
        cut.init(environmentConfig);
        cut.doForPhase(testPhase);

        File testCrefosFile = new File(ab30XmlsDir, "TestCrefos.properties");
        Assert.assertTrue(testCrefosFile.exists());
        final List<String> contentList1 = FileUtils.readLines(testCrefosFile);
        Assert.assertEquals(2, contentList1.size());

        File extendedTestCrefosFile = new File(ab30XmlsDir, "ExtendedTestCrefos.properties");
        Assert.assertTrue(extendedTestCrefosFile.exists());
        final List<String> contentList2 = FileUtils.readLines(extendedTestCrefosFile);
        Assert.assertFalse(contentList2.isEmpty());
        expectedExtendedTestCrefosContentList.forEach(expectedExtendedTestCrefosContent -> {
            Assert.assertTrue(contentList2.contains(expectedExtendedTestCrefosContent));
        });

        File crefosToCustomersMapFile = new File(ab30XmlsDir, "CrefosToCustomersMap.txt");
        Assert.assertTrue(crefosToCustomersMapFile.exists());
        final List<String> contentList3 = FileUtils.readLines(crefosToCustomersMapFile);
        Assert.assertFalse(contentList3.isEmpty());
        expectedCrefosToCustomersMapContentList.forEach(expectedCrefosToCustomersMapContent -> {
            Assert.assertTrue(contentList3.contains(expectedCrefosToCustomersMapContent));
        });

        expectedXmlsFilenamesList.forEach(expectedXmlsFilename -> {
            Assert.assertTrue(new File(ab30XmlsDir, expectedXmlsFilename).exists());
        });

    }

    protected void deleteFilesInDir(File ab30XmlsDir) {
        Collection<File> fileCollection = FileUtils.listFiles(ab30XmlsDir, null, false);
        fileCollection.forEach(file -> file.delete());
    }
}
