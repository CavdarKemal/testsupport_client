package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExportsAdapterGetCollectInfoTest extends ExportsAdapterTestBase {

    private static List<String> zipEntryNamesList = Arrays.asList(
            "stammcrefo_4123525101.xml", // stammcrefo
            "stammcrefo_4123525102.xml",
            "stammcrefo_4123525103.xml",
            "beteiligter_4123530201.xml",
            "beteiligter_4123530202.xml",
            "beteiligter_4123530203.xml",
            "loeschsatz_4123525301.xml",
            "loeschsatz_4123525302.xml",
            "loeschsatz_4123525303.xml",
            "loeschsatz_4123525101.xml" // gleichzeitig auch löschsatz!
    );

    @Test
    public void testGetCollectInfoPCasesOnly() throws Exception {
        List<TestCrefo> testCrefoList = new ArrayList<>();
        testCrefoList.add(new TestCrefo("p01", 4123530201L)); // P-Test mit Beteiligten-Export: beteiligter_4123530201.xml -> OK
        testCrefoList.add(new TestCrefo("p02", 4123525103L)); // P-Test mit Stammsatz-Export: stammcrefo_4123525103.xml -> OK
        testCrefoList.add(new TestCrefo("p03", 4129999999L)); // P-Test ohne Export -> Fehler

        ExportsAdapterDefImpl cut = getExportsAdapter("BVD");
        List<CollectInfo> collectInfoList = cut.getCollectInfoForTestCrefosList(testCrefoList, zipEntryNamesList);
        collectInfoList.forEach(collectInfo -> System.out.println(collectInfo));
        collectInfoList.forEach(collectInfo -> {
            String testFallName = collectInfo.getTestCrefo().getTestFallName();
            if (testFallName.equals("p01")) {
                Assert.assertTrue(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4123530201L);
                Assert.assertTrue("Für den P-Export-Test wurde ein falscher Export gefunden!", collectInfo.getZipEntryName().contains("beteiligter_4123530201.xml"));
            } else if (testFallName.equals("p02")) {
                Assert.assertTrue(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4123525103L);
                Assert.assertTrue("Für den P-Export-Test wurde ein falscher Export gefunden!", collectInfo.getZipEntryName().contains("stammcrefo_4123525103.xml"));
            } else if (testFallName.equals("p03")) {
                Assert.assertFalse(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4129999999L);
                Assert.assertNull("Für den P-Export-Test dürfte es keinen Export geben!", collectInfo.getZipEntryName());
            }
        });
    }

    @Test
    public void testGetCollectInfoPAndXCaseForSameCrefo() throws Exception {
        List<TestCrefo> testCrefoList = new ArrayList<>();
        testCrefoList.add(new TestCrefo("p01", 4123525101L)); // P-Test mit Beteiligten-Export: stammcrefo_4123525101.xml -> OK
        testCrefoList.add(new TestCrefo("x01", 4123525101L)); // P-Test mit Stammsatz-Export: loeschsatz_4123525101.xml -> OK

        ExportsAdapterDefImpl cut = getExportsAdapter("BVD");
        List<CollectInfo> collectInfoList = cut.getCollectInfoForTestCrefosList(testCrefoList, zipEntryNamesList);
        collectInfoList.forEach(collectInfo -> System.out.println(collectInfo));
        collectInfoList.forEach(collectInfo -> {
            String testFallName = collectInfo.getTestCrefo().getTestFallName();
            if (testFallName.equals("p01")) {
                Assert.assertTrue(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("Für den P-Export-Test wurde ein falscher Export gefunden!", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4123525101L);
                Assert.assertTrue("", collectInfo.getZipEntryName().contains("stammcrefo_4123525101.xml"));
            } else if (testFallName.equals("x01")) {
                Assert.assertTrue(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4123525101L);
                Assert.assertTrue("Für den X-Export-Test wurde ein falscher Export gefunden!!", collectInfo.getZipEntryName().contains("loeschsatz_4123525101.xml"));
            }
        });
    }

    @Test
    public void testGetCollectInfoNCases() throws Exception {
        List<TestCrefo> testCrefoList = new ArrayList<>();
        testCrefoList.add(new TestCrefo("n01", 4123525301L)); // N-Test mit Löschsatz-Export: loeschsatz_4123525301.xml -> Fehler
        testCrefoList.add(new TestCrefo("n02", 4129999999L)); // X-Test ohne Export -> OK

        ExportsAdapterDefImpl cut = getExportsAdapter("BVD");
        List<CollectInfo> collectInfoList = cut.getCollectInfoForTestCrefosList(testCrefoList, zipEntryNamesList);
        collectInfoList.forEach(collectInfo -> System.out.println(collectInfo));
        collectInfoList.forEach(collectInfo -> {
            String testFallName = collectInfo.getTestCrefo().getTestFallName();
            if (testFallName.equals("n01")) {
                Assert.assertFalse(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4123525301L);
                Assert.assertNotNull("Für den N-Export-Test sollte ein falscher Export gefunden worden sein!", collectInfo.getZipEntryName());
            } else if (testFallName.equals("n02")) {
                Assert.assertTrue(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.fail("Für den N-Export-Test kein Export geben!");
            }
        });
    }

    @Test
    public void testGetCollectInfoXCases() throws Exception {
        List<TestCrefo> testCrefoList = new ArrayList<>();
        testCrefoList.add(new TestCrefo("x01", 4123525301L)); // X-Test mit Löschsatz-Export: loeschsatz_4123525301.xml -> OK
        testCrefoList.add(new TestCrefo("x02", 4123525302L)); // X-Test mit Löschsatz-Export: loeschsatz_4123525302.xml -> OK
        testCrefoList.add(new TestCrefo("x03", 4129999999L)); // X-Test ohne Export -> Fehler

        ExportsAdapterDefImpl cut = getExportsAdapter("BVD");
        List<CollectInfo> collectInfoList = cut.getCollectInfoForTestCrefosList(testCrefoList, zipEntryNamesList);
        collectInfoList.forEach(collectInfo -> System.out.println(collectInfo));
        collectInfoList.forEach(collectInfo -> {
            String testFallName = collectInfo.getTestCrefo().getTestFallName();
            if (testFallName.equals("x01")) {
                Assert.assertTrue(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4123525301L);
                Assert.assertTrue("Für den X-Export-Test wurde ein falscher Export gefunden!", collectInfo.getZipEntryName().contains("loeschsatz_4123525301.xml"));
            } else if (testFallName.equals("x02")) {
                Assert.assertTrue(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4123525302L);
                Assert.assertTrue("Für den X-Export-Test wurde ein falscher Export gefunden!", collectInfo.getZipEntryName().contains("loeschsatz_4123525302.xml"));
            } else if (testFallName.equals("x03")) {
                Assert.assertFalse(collectInfo.getStrInfo(), collectInfo.isStatusOK());
                Assert.assertTrue("", collectInfo.getTestCrefo().getPseudoCrefoNr().longValue() == 4129999999L);
                Assert.assertNull("Für den X-Export-Test dürfte es keinen Export geben!", collectInfo.getZipEntryName());
            }
        });
    }

}
