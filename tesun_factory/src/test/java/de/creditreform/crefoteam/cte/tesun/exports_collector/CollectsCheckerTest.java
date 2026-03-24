package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.tesun.TestFallTestBase;
import de.creditreform.crefoteam.cte.tesun.util.NameCrefo;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.propertyfiles.PathInfo;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

public class CollectsCheckerTest extends TestFallTestBase {

    private static class TestInfo {

        private List<File> xmlFilesList = new ArrayList<>();
        private List<NameCrefo> nameCrefoList = new ArrayList<>();
        private Map<Long, Long> psuedoToOriginalCrefosMap = new TreeMap<>();

        public void addTestInfo(String testName, String xmlFileName) {
            if (xmlFileName.endsWith(".xml")) {
                xmlFilesList.add(new File(xmlFileName));
            }

            Matcher matcher = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(xmlFileName); // xmlFileName = "p002-4120125723_4120126388.xml"
            if (!matcher.find()) {
                Assert.fail("xmlFileName muss eine Crefonummer enthalten!");
            }
            String strCrefo = matcher.group();
            if (matcher.find()) {
                strCrefo = xmlFileName.substring(matcher.start(), matcher.end());
            }
            Long pseudoCrefo = Long.valueOf(strCrefo);
            long originalCrefo = Long.valueOf(strCrefo.replaceFirst(TestSupportClientKonstanten.TEST_CLZ_912, "789"));
            psuedoToOriginalCrefosMap.put(pseudoCrefo, originalCrefo);

            nameCrefoList.add(new NameCrefo(testName, pseudoCrefo));
        }

        public List<File> getXmlFilesList() {
            return xmlFilesList;
        }

        public List<NameCrefo> getNameCrefoList() {
            return nameCrefoList;
        }

        public Map<Long, Long> getPsuedoToOriginalCrefosMap() {
            return psuedoToOriginalCrefosMap;
        }

    }

    @Test
    public void testCollectsAnalyse() throws Exception {
        TestInfo testInfo = new TestInfo();

        testInfo.addTestInfo("p001", "stammcrefo_4120126924.xml"); // OK, da eine XML-Datei als Exportsatz
        testInfo.addTestInfo("p002", "beteiligter_4120126918.xml"); // OK, da eine XML-Datei als Exportsatz
        testInfo.addTestInfo("p003", "4120125723_4120126388.xml"); // OK, da eine XML-Datei als Exportsatz
        testInfo.addTestInfo("p004", "loeschsatz_4120126390.xml"); // Fehler, da ein Löschsatz
        testInfo.addTestInfo("p005", "deleted_4120126391.xml"); // Fehler, da ein Löschsatz
        testInfo.addTestInfo("p006", "4120124567"); // Fehler, da kein Exportsatz

        testInfo.addTestInfo("n001", "4120126925"); // OK, da keine XML-Datei
        testInfo.addTestInfo("n002", "loeschsatz_4120126576.xml"); // OK, da ein Löschsatz
        testInfo.addTestInfo("n003", "deleted_4120126367.xml"); // OK, da ein Löschsatz
        testInfo.addTestInfo("n004", "beteiligter_4120126798.xml"); // Fehler, da ein Exportsatz
        testInfo.addTestInfo("n005", "stammcrefo_4120126799.xml"); // Fehler, da ein Exportsatz

        testInfo.addTestInfo("x001", "loeschsatz_4120121234.xml"); // OK, da eine XML-Datei als Löschsatz
        testInfo.addTestInfo("x002", "deleted_4120125723_4120126389.xml"); // OK, da eine XML-Datei als Löschsatz
        testInfo.addTestInfo("x003", "4120126345"); // Fehler, da keine XML-Datei als Löschsatz
        testInfo.addTestInfo("x004", "beteiligter_4120126998.xml"); // Fehler, da ein Exportsatz
        testInfo.addTestInfo("x005", "stammcrefo_4120126469.xml"); // Fehler, da ein Exportsatz

        Map<String, TestCustomer> customerTestInfoMap = setupUtil.readTestCustomersMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2); //!!! TODO
        File refExportsDir = setupUtil.getEnvironmentConfig().getItsqRefExportsRoot();
        File fileInSubDir = new File(new File(refExportsDir, "bvd/Relevanz_Positiv"), "BVD_Relevanz_Positiv.properties");
        PathInfo pathInfo = new PathInfo(refExportsDir, fileInSubDir);
        TestSupportClientKonstanten.TEST_PHASE testPhase = TestSupportClientKonstanten.TEST_PHASE.PHASE_2;

        final CollectsChecker cut = new CollectsChecker(setupUtil.getEnvironmentConfig(), tesunClientJobListener) {
            protected List<File> readXmlFiles(PathInfo pathInfo) throws PropertiesException {
                return testInfo.getXmlFilesList();
            }

            protected Map<Long, Long> initPsuedoToOriginalCrefosMap() {
                return testInfo.getPsuedoToOriginalCrefosMap();
            }

            protected void dumpErrorsMap(PathInfo pathInfo, Map<Long, String> errosMap) {
                tesunClientJobListener.notifyClientJob(Level.INFO, pathInfo.getCurtomerKey() + errosMap.keySet());
            }
        };
        String[] expectedNotices = {
                "Kunde bvd:: Für den Testfall p001=4120126924[4120126924] wurde Exportsatz 'stammcrefo_4120126924.xml' exportiert.",
                "Kunde bvd:: Für den Testfall p002=4120126918[4120126918] wurde Exportsatz 'beteiligter_4120126918.xml' exportiert.",
                "Kunde bvd:: Für den Testfall p003=4120126388[4120126388] wurde Exportsatz '4120125723_4120126388.xml' exportiert.",
                "Kunde bvd:: Für den Testfall p004=4120126390[4120126390] wurde Exportsatz 'loeschsatz_4120126390.xml' exportiert.",
                "Kunde bvd:: Für den Testfall p005=4120126391[4120126391] wurde Exportsatz 'deleted_4120126391.xml' exportiert.",
                "Kunde bvd:: Testfall p006=4120124567[4120124567] MÜSSTE exportiert werden!",
                "Kunde bvd:: Für den Testfall n001=4120126925[4120126925] wurde ERWARTUNGSGEMÄß nichts exportiert.",
                "Kunde bvd:: Für den Testfall n002=4120126576[4120126576] wurde ERWARTUNGSGEMÄß ein Löschsatz 'loeschsatz_4120126576.xml' exportiert.",
                "Kunde bvd:: Für den Testfall n003=4120126367[4120126367] wurde ERWARTUNGSGEMÄß ein Löschsatz 'deleted_4120126367.xml' exportiert.",
                "Kunde bvd:: Für den Testfall n004=4120126798[4120126798] wurde UNERWARTETERWEISE ein Exportsatz 'beteiligter_4120126798.xml' exportiert!",
                "Kunde bvd:: Für den Testfall n005=4120126799[4120126799] wurde UNERWARTETERWEISE ein Exportsatz 'stammcrefo_4120126799.xml' exportiert!",
                "Kunde bvd:: Für den Testfall x001=4120121234[4120121234] wurde ERWARTUNGSGEMÄß ein Löschsatz 'loeschsatz_4120121234.xml' exportiert.",
                "Kunde bvd:: Für den Testfall x002=4120126389[4120126389] wurde ERWARTUNGSGEMÄß ein Löschsatz 'deleted_4120125723_4120126389.xml' exportiert.",
                "Kunde bvd:: Für den Testfall x003=4120126345[4120126345] wurde kein Löschsatz exportiert!",
                "Kunde bvd:: Für den Testfall x004=4120126998[4120126998] wurde UNERWARTETERWEISE ein Exportsatz 'beteiligter_4120126998.xml' exportiert!",
                "Kunde bvd:: Für den Testfall x005=4120126469[4120126469] wurde UNERWARTETERWEISE ein Exportsatz 'stammcrefo_4120126469.xml' exportiert!",
                "bvd[4120126469, 4120126998, 4120124567, 4120126798, 4120126799, 4120126345]"
        };
        int index = 0;
        for (String notice : notifyList) {
            boolean contains = notice.contains(expectedNotices[index++]);
            if (!contains) {
                Assert.fail("Erwartete Notiz " + notice + "\nnicht vorhanden!");
            }
        }
    }
}
