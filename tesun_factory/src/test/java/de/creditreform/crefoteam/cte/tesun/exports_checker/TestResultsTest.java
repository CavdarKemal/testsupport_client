package de.creditreform.crefoteam.cte.tesun.exports_checker;

import de.creditreform.crefoteam.cte.tesun.util.TestResults;
import de.creditreform.crefoteam.cte.tesun.testutil.TesunTestSetupUtil;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class TestResultsTest {
    final TesunTestSetupUtil setupUtil = new TesunTestSetupUtil();

    @Before
    public void setUp() {
        try {
            setupUtil.setUp();
            FileUtils.deleteDirectory(setupUtil.getEnvironmentConfig().getCollectsRoot());
            FileUtils.deleteDirectory(setupUtil.getEnvironmentConfig().getCheckedRoot());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @After
    public void teardown() {
        setupUtil.teardown();
    }

    @Test
    public void testXMLstestResult_Dump() throws Exception {
        // für COLLECTED der Kunden bvd und drd
        String collectsFilePath = setupUtil.getEnvironmentConfig().getCollectsRoot().getAbsolutePath();
        String scenarioName = "Relevanz_Positiv";
        Map<String, File> collectedSubDirsMap = setupUtil.createDirWithSubdirs(collectsFilePath, new String[]{
                scenarioName, "bvd/Relevanz_Negativ",
                "drd/Relevanz_Positiv", "drd/Relevanz_Negativ",
        });
        // für REF-EXPORTS der Kunden bvd und drd
        String referenceFilePath = setupUtil.getEnvironmentConfig().getItsqRefExportsRoot().getAbsolutePath();
        Map<String, File> referencesSubDirsMap = setupUtil.createDirWithSubdirs(referenceFilePath, new String[]{
                scenarioName, "bvd/Relevanz_Negativ",
                "drd/Relevanz_Positiv", "drd/Relevanz_Negativ",
        });
        // bvd.Relevanz_Positiv: 0 File-Diffs, also gleiche Datei
        File collBvdTest00File = new File(collectedSubDirsMap.get(scenarioName), "test00.bvd.xml");
        setupUtil.writeModifiedXMLDocument(collBvdTest00File, XMLFragmentsTest.XPATH_EXPRESSIONS, new String[]{});
        File refBvdTest00File = new File(referencesSubDirsMap.get(scenarioName), "test00.bvd.xml");
        setupUtil.writeModifiedXMLDocument(refBvdTest00File, XMLFragmentsTest.XPATH_EXPRESSIONS, new String[]{});
        // bvd.Relevanz_Positiv: 1 Message
/*TODO !!!!!!!!!!!!!!!!!!!!!!!!
        xmlstestResult.addMessage(scenarioName, "Test Message mit Platzhalter %s %d", "String", 1);
        DetailedDiff detailedDiffBVD01 = new DetailedDiff(XMLUnit.compareXML(new FileReader(collBvdTest00File), new FileReader(refBvdTest00File)));
        xmlstestResult.addDifference(scenarioName, collBvdTest00File.getPath(), detailedDiffBVD01.getAllDifferences());

        // drd.Relevanz_Positiv: 2 different files, 2 Messages
        File collDrdTest00File = new File(collectedSubDirsMap.get("drd/Relevanz_Positiv"), "test00.drd.xml");
        setupUtil.writeModifiedXMLDocument(collDrdTest00File, XMLFragmentsTest.XPATH_EXPRESSIONS, new String[]{});
        File refDrdTest00File = new File(referencesSubDirsMap.get("drd/Relevanz_Positiv"), "test00.drd.xml");
        setupUtil.writeModifiedXMLDocument(refDrdTest00File, XMLFragmentsTest.XPATH_EXPRESSIONS, //
                new String[]{ "EN", "RETY-DE-2", "Juristisc Stand" });
        xmlstestResult.addMessage("drd/Relevanz_Positiv", "DRD Test Message 1");

        File collDrdTest01File = new File(collectedSubDirsMap.get("drd/Relevanz_Positiv"), "test01.drd.xml");
        setupUtil.writeModifiedXMLDocument(collDrdTest01File, XMLFragmentsTest.XPATH_EXPRESSIONS, new String[]{});
        File refDrdTest01File = new File(referencesSubDirsMap.get("drd/Relevanz_Positiv"), "test01.drd.xml");
        setupUtil.writeModifiedXMLDocument(refDrdTest01File, XMLFragmentsTest.XPATH_EXPRESSIONS, //
                new String[]{ "TR", "RETY-DE-9", "Jurist Stand" });
        xmlstestResult.addMessage("drd/Relevanz_Positiv", "DRD Test Message 2");

        DetailedDiff detailedDiffDRD01 = new DetailedDiff(XMLUnit.compareXML(new FileReader(collDrdTest00File), new FileReader(refDrdTest00File)));
        xmlstestResult.addDifference("drd/Relevanz_Positiv", collDrdTest00File.getPath(), detailedDiffDRD01.getAllDifferences());
        DetailedDiff detailedDiffDRD02 = new DetailedDiff(XMLUnit.compareXML(new FileReader(collDrdTest01File), new FileReader(refDrdTest01File)));
        xmlstestResult.addDifference("drd/Relevanz_Positiv", collDrdTest01File.getPath(), detailedDiffDRD02.getAllDifferences());

        xmlstestResult.dumpResults(new HashMap<>(), setupUtil.getEnvironmentConfig().getCheckedFile().getPath());

        // check
        EnvironmentConfig environmentConfig = setupUtil.getEnvironmentConfig();
        Map<String, TestCustomer> customerTestInfoMap = environmentConfig.getCustomerTestInfoMap();
        File checkDir = setupUtil.getEnvironmentConfig().getCheckedFile();
        setupUtil.checkMessageFile(new File(checkDir, "bvd/Relevanz_Positiv/" + TestSupportClientKonstanten.ERRORS_TXT), Arrays.asList("Test Message mit Platzhalter String 1"));

        setupUtil.checkMessageFile(new File(checkDir, "drd/Relevanz_Positiv/" + TestSupportClientKonstanten.ERRORS_TXT), Arrays.asList("DRD Test Message 1", "DRD Test Message 2"));
        setupUtil.checkDiffFile(new File(checkDir, "drd/Relevanz_Positiv/test00.drd.xml.diff"),  new String[]{});
        setupUtil.checkDiffFile(new File(checkDir, "drd/Relevanz_Positiv/test01.drd.xml.diff"),  new String[]{});
*/
    }

    @Test
    public void testResultInfoForDir() throws Exception {
        TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(setupUtil.getEnvironmentConfig().getCheckedRoot().getAbsolutePath());
        Assert.assertEquals("Verzeichnis-Name stimmt nicht!", setupUtil.getEnvironmentConfig().getCheckedRoot().getAbsolutePath(), resultInfo.getErrorStr());

/*
        resultInfo.addMessage("Message 1");
        resultInfo.addMessage("Message 2");
        List<String> msgList = resultInfo.getErrorStateList();
        Assert.assertEquals("ResultInfo muss 2 elemente haben!", msgList.size(), 2);
        setupUtil.checkMessageList(msgList, new String[]{ "Message 1", "Message 2" });

        List<Difference> diffList = new ArrayList<>();
        String myKey = "KEY-0";
        resultInfo.addDifference(myKey, diffList);
        Map<String, List<Difference>> differencesMap = resultInfo.getDifferencesMap();
        Assert.assertEquals("ResultInfo müsstr 1 Element haben!", differencesMap.size(), 1);
        List<Difference> listKEY0 = differencesMap.get(myKey);
        Assert.assertTrue("Diff-List sollte leer sein!", listKEY0.isEmpty());

        String modifiedXMLContent1 = XMLFragments.getModifiedXMLContent(XMLFragmentsTest.XPATH_EXPRESSIONS, new String[]{ null, "RETY-DE-2", null, "50001", "Hallo" });
        DetailedDiff myDiff = new DetailedDiff(new Diff(XMLFragments.XMLFRAGMENT, modifiedXMLContent1));
        List allDifferences = myDiff.getAllDifferences();
        resultInfo.addDifference(myKey, allDifferences);
        differencesMap = resultInfo.getDifferencesMap();
        Assert.assertEquals("ResultInfo müsstr 1 Element haben!", differencesMap.size(), 1);
        listKEY0 = differencesMap.get(myKey);
        Assert.assertEquals("Diff-List sollte 4 Elemente haben!", listKEY0.size(), 3);
        setupUtil.checkDiffList(listKEY0, new String[]{ "'RETY-DE-1' but was 'RETY-DE-2'", "'Amtsgericht' but was 'Hallo'", "'58099' but was '50001'", });
*/
    }
}
