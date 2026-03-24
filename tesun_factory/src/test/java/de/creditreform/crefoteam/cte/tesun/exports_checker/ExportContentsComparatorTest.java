package de.creditreform.crefoteam.cte.tesun.exports_checker;

import de.creditreform.crefoteam.cte.tesun.TestFallTestBase;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestResults;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.custommonkey.xmlunit.Difference;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExportContentsComparatorTest extends TestFallTestBase {
    @Test
    public void testCompareXMLsEmpty() throws IOException {
/*
        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST);
        Map<String, TestResults> testResultsMap = exportContentsComparator.compareFileContents(null);
        Assert.assertNotNull(testResultsMap);
        Assert.assertTrue("TestResults-Map sollte leer sein!", testResultsMap.isEmpty());
*/
    }

    @Test
    public void testCompareXMLsNoRefSubDir() throws IOException {
        Map<String, List<File>> collectedXMLsMap = new TreeMap<>();
        ArrayList<File> colletedFilesList = new ArrayList<>();
        colletedFilesList.add(new File("xxx"));
        collectedXMLsMap.put("aaa", colletedFilesList);
        Map<String, List<File>> referenceXMLsMap = new TreeMap<>();
        try {
            ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST, tesunClientJobListener);
            exportContentsComparator.compareFileContents(null);
            Assert.fail("Exception expected!");
        } catch (Exception ex) {
            Assert.assertEquals("Restored-Ordner 'blabla' enthaelt keine XML-Dateien, obwohl der References-Ordner nicht leer ist!", ex.getMessage());
        }
    }

    @Test
    public void testCompareXMLsEmptyFilesList() throws IOException {
        Map<String, List<File>> collectedXMLsMap = new TreeMap<>();
        ArrayList<File> colletedFilesList = new ArrayList<>();
        collectedXMLsMap.put("aaa", colletedFilesList);

        Map<String, List<File>> referenceXMLsMap = new TreeMap<>();
        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST, tesunClientJobListener);
/* TODO !!!!!!!!!!!!!!!!!!!!!!!!
    TestResults testResult = exportContentsComparator.compareFileContents( null );
    
    Map<String, ResultInfo> resultInfoForDirMap = testResult.getResultInfoMap();
    Assert.assertNotNull( resultInfoForDirMap );
    Assert.assertEquals( "TestResults-Map sollte genau 1 Element haben!", resultInfoForDirMap.size(), 1 );
    TestResults.ResultInfo resultInfoForScenario = resultInfoForDirMap.get( "aaa" );
    Assert.assertNotNull( "ResultInfo für 'aaa' dürfte nicht null sein!", resultInfoForScenario);
    List<String> msgList = resultInfoForScenario.getErrorStateList();
    Assert.assertEquals( "ResultInfo sollte genau 1 Message-Element haben!", msgList.size(), 1 );
    Assert.assertEquals( "ResultInfo hat eine falsache Message!", msgList.get( 0 ), "Restored-Ordner 'aaa' existiert nicht!" );
    Assert.assertNotNull( msgList );
    Map<String, List<Difference>> differencesMap = resultInfoForScenario.getDifferencesMap();
    Assert.assertNotNull( differencesMap );
    Assert.assertEquals( "Difference-Map sollte leer sein!", differencesMap.size(), 0 );
*/
    }

    @Test
    public void testCompareXMLsEmptyReferencesSubDir() throws IOException {
        Map<String, List<File>> collectedXMLsMap = new TreeMap<>();
        ArrayList<File> colletedFilesList = new ArrayList<>();
        colletedFilesList.add(new File("xxx"));
        collectedXMLsMap.put("aaa", colletedFilesList);

        Map<String, List<File>> referenceXMLsMap = new TreeMap<>();
        ArrayList<File> referencesFilesList = new ArrayList<>();
        referenceXMLsMap.put("aaa", referencesFilesList);

        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST, tesunClientJobListener);
/*TODO !!!!!!!!!!!!!!!!!!!!!!!!
    TestResults testResult = exportContentsComparator.compareFileContents( null );
    
    Map<String, ResultInfo> resultInfoForDirMap = testResult.getResultInfoMap();
    Assert.assertEquals( "TestResults-Map sollte genau 1 Element haben!", resultInfoForDirMap.size(), 1 );
    TestResults.ResultInfo resultInfoForScenario = resultInfoForDirMap.get( "aaa" );
    List<String> msgList = resultInfoForScenario.getErrorStateList();
    Assert.assertEquals( "ResultInfo sollte genau 1 Message-Element haben!", msgList.size(), 1 );
    Assert.assertEquals( "ResultInfo hat eine falsache Message!", msgList.get( 0 ), "Restored-Ordner 'aaa' enthaelt keine XML-Dateien, obwohl der References-Ordner nicht leer ist!" );
    Map<String, List<Difference>> differencesMap = resultInfoForScenario.getDifferencesMap();
    Assert.assertEquals( "Difference-Map sollte leer sein!", differencesMap.size(), 0 );
*/
    }

    @Test
    public void testCompareXMLsEmptyCollectedSubDir() throws IOException {
        Map<String, List<File>> collectedXMLsMap = new TreeMap<>();
        ArrayList<File> colletedFilesList = new ArrayList<>();
        collectedXMLsMap.put("aaa", colletedFilesList);

        Map<String, List<File>> referenceXMLsMap = new TreeMap<>();
        ArrayList<File> referencesFilesList = new ArrayList<>();
        referencesFilesList.add(new File("yyy"));
        referenceXMLsMap.put("aaa", referencesFilesList);

        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST, tesunClientJobListener);
/*TODO !!!!!!!!!!!!!!!!!!!!!!!!
    TestResults testResult = exportContentsComparator.compareFileContents( null );
    
    Map<String, ResultInfo> resultInfoForDirMap = testResult.getResultInfoMap();
    Assert.assertEquals( "TestResults-Map sollte genau 1 Element haben!", resultInfoForDirMap.size(), 1 );
    ResultInfo resultInfoForScenario = resultInfoForDirMap.get( "aaa" );
    List<String> msgList = resultInfoForScenario.getErrorStateList();
    Assert.assertEquals( "ResultInfo sollte genau 1 Message-Element haben!", msgList.size(), 1 );
    Assert.assertEquals( "ResultInfo hat eine falsache Message!", msgList.get( 0 ), "Restored-Ordner 'aaa' enthaelt XML-Dateien, obwohl der References-Ordner leer ist!" );
    Map<String, List<Difference>> differencesMap = resultInfoForScenario.getDifferencesMap();
    Assert.assertEquals( "Difference-Map sollte leer sein!", differencesMap.size(), 0 );
*/
    }

    @Test
    public void testCompareXMLsEmptySubDirs() throws IOException {
        Map<String, List<File>> collectedXMLsMap = new TreeMap<>();
        ArrayList<File> colletedFilesList = new ArrayList<>();
        collectedXMLsMap.put("aaa", colletedFilesList);

        Map<String, List<File>> referenceXMLsMap = new TreeMap<>();
        ArrayList<File> referencesFilesList = new ArrayList<>();
        referenceXMLsMap.put("aaa", referencesFilesList);

        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST, tesunClientJobListener);
/*TODO !!!!!!!!!!!!!!!!!!!!!!!!
    TestResults testResult = exportContentsComparator.compareFileContents( null );
    
    Map<String, ResultInfo> resultInfoForDirMap = testResult.getResultInfoMap();
    Assert.assertEquals( "TestResults-Map dürfte keine Elemente haben!", resultInfoForDirMap.size(), 0 );
    TestResults.ResultInfo resultInfoForScenario = resultInfoForDirMap.get( "aaa" );
    Assert.assertNull(resultInfoForScenario);
*/
    }

    @Test
    public void testCompareXMLsRefNotMatched() throws IOException {
        Map<String, List<File>> collectedXMLsMap = new TreeMap<>();
        ArrayList<File> colletedFilesList = new ArrayList<>();
        colletedFilesList.add(new File("xxx"));
        collectedXMLsMap.put("aaa", colletedFilesList);

        Map<String, List<File>> referenceXMLsMap = new TreeMap<>();
        ArrayList<File> referenceFilesList = new ArrayList<>();
        referenceFilesList.add(new File("yyy"));
        referenceXMLsMap.put("aaa", referenceFilesList);

        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST, tesunClientJobListener);
/*TODO !!!!!!!!!!!!!!!!!!!!!!!!
    TestResults testResult = exportContentsComparator.compareFileContents( null );
    
    Map<String, ResultInfo> resultInfoForDirMap = testResult.getResultInfoMap();
    Assert.assertEquals( "TestResults-Map sollte genau 1 Element haben!", resultInfoForDirMap.size(), 1 );
    TestResults.ResultInfo resultInfoForScenario = resultInfoForDirMap.get( "aaa" );
    List<String> msgList = resultInfoForScenario.getErrorStateList();
    Assert.assertEquals( "ResultInfo sollte genau 2 Message-Elemente haben!", msgList.size(), 2 );
    Assert.assertEquals( "1. Message in ResultInfo ist falsach!", "Im References-Ordner existiert keine Ref-Export-Datei für 'E:\\Projekte\\CTE\\testsupport_client\\tesun_factory\\yyy'!", msgList.get( 0 ) );
    Assert.assertEquals( "2. Message in ResultInfo ist falsach!", "Der Testfall xxx wurde nicht exportiert!", msgList.get( 1 ) );
    Map<String, List<Difference>> differencesMap = resultInfoForScenario.getDifferencesMap();
    Assert.assertEquals( "Difference-Map sollte leer sein!", differencesMap.size(), 0 );
*/
    }

    @Test
    public void testCompareXMLs() throws Exception {
        EnvironmentConfig environmentConfig = setupUtil.getEnvironmentConfig();
        File refExportsDir = environmentConfig.getItsqRefExportsRoot();
        File refXmlFile = new File(refExportsDir, "bvd/Relevanz_Positiv/p001_01-stammcrefo_4112000183.xml");

        File testOutputsParentFile = environmentConfig.getTestOutputsRoot().getParentFile();
        File pseudoRefExportsFile = new File(new File(testOutputsParentFile, "TEST-OUTPUTS"), TestSupportClientKonstanten.PSEUDO_REF_EXPORTS);

        File ctrlXmlFile = new File(pseudoRefExportsFile, "bvd/Relevanz_Positiv/p001_01-stammcrefo_4120195063.xml");
        String checkDirName = environmentConfig.getCheckedRoot().getPath();
        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST, tesunClientJobListener);
        TestCrefo testCrefo = new TestCrefo("Dummy-Testfall", -1L, "NULL", true, null);
        TestResults.DiffenrenceInfo diffenrenceInfo = exportContentsComparator.compareFileContents(testCrefo, refXmlFile, ctrlXmlFile, new File(checkDirName, "bvd/Relevanz_Positiv"), null);
        Map<String, List<Difference>> diffsMap = diffenrenceInfo.getDiffsMap();
        Assert.assertEquals("TestResults-Map sollte genau 6 Elemente haben!", 6, diffsMap.size());
        int foundMatches = 0;
        for (String key : diffsMap.keySet()) {
            if (key.contains("Expected text value '4112000183' but was '4120195063' ")) {
                foundMatches++;
            }
            if (key.contains("Expected text value 'Markus-B' but was 'Markus-A'")) {
                foundMatches++;
            }
            if (key.contains("Expected text value '58135' but was '99999'")) {
                foundMatches++;
            }
        }
        Assert.assertEquals(3, foundMatches);
    }

    @Test
    public void testCompareXMLsUnexpectedExport() throws IOException {
        Map<String, List<File>> collectedXMLsMap = new TreeMap<>();
        ArrayList<File> colletedFilesList = new ArrayList<>();
        colletedFilesList.add(new File("nxxx"));
        collectedXMLsMap.put("aaa", colletedFilesList);

        Map<String, List<File>> referenceXMLsMap = new TreeMap<>();
        ArrayList<File> referenceFilesList = new ArrayList<>();
        referenceFilesList.add(new File("xxx"));
        referenceXMLsMap.put("aaa", referenceFilesList);

        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(Collections.EMPTY_LIST, tesunClientJobListener);
/*TODO !!!!!!!!!!!!!!!!!!!!!!!!
    TestResults testResult = exportContentsComparator.compareFileContents( null );
    
    Map<String, TestResults.ResultInfo> resultInfoForDirMap = testResult.getResultInfoMap();
    Assert.assertEquals( "TestResults-Map sollte genau 1 Element haben!", resultInfoForDirMap.size(), 1 );
    ResultInfo resultInfoForScenario = resultInfoForDirMap.get( "aaa" );
    List<String> msgList = resultInfoForScenario.getErrorStateList();
    Assert.assertEquals( "ResultInfo sollte genau 2 Message-Elemente haben!", msgList.size(), 2 );
    Assert.assertEquals( "1. Message in ResultInfo ist falsach!", "Im References-Ordner existiert keine Ref-Export-Datei für 'E:\\Projekte\\CTE\\testsupport_client\\tesun_factory\\xxx'!", msgList.get( 0 )  );
    Assert.assertEquals( "2. Message in ResultInfo ist falsach!", "Der Testfall nxxx wurde nicht exportiert!", msgList.get( 1 ) );
    Map<String, List<Difference>> differencesMap = resultInfoForScenario.getDifferencesMap();
    Assert.assertEquals( "Difference-Map sollte leer sein!", differencesMap.size(), 0 );
*/
    }

}
