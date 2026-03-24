package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.jobadapter.jvminfo.xmlbinding.ServicesList;
import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzDecisionMonitoring;
import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzMonitoringErgebnis;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.clzinfo.TesunClzInfo;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.statistikentscheidungstraegerberechnung.EntscheidungstraegerInfo;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.statistikentscheidungstraegerberechnung.EntscheidungstraegerInfosAlleCrefos;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.statistikentscheidungstraegerberechnung.EntscheidungstraegerInfosProCrefo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class TesunRestServiceIntegrationTest3 extends TesunRestServiceIntegrationTestBase {
    public TesunRestServiceIntegrationTest3() {
        super("ENE");
    }
   /* Aus cte_rest:cte_betrieb_service
       curl -u cavdark:cavdark
            -H "Content-type:application/xml"
            -X PUT
            -d "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><CteErneuteLieferung
                xmlns=\"http://crefoteam.creditreform.de/cte/erneutelieferung/2013/03\">
                <crefos><crefo>4110106787</crefo></crefos><crefos><crefo>4110106780</crefo></crefos></CteErneuteLieferung>"
            http://http://rhsctem015.ecofis.de:7077/cte_betrieb_service/erneutelieferung/EXPORT_CTE_TO_ZEW
   */
/*
   @Test
   public void test_erneuteLieferungBeantragenBS() throws Exception {
      String absolutePath = environmentConfig.getTestInputsFile().getAbsolutePath() + "/" + TestSupportClientKonstanten.TEST_CASES;
      List<String> customerKeysList = Arrays.asList("acb", "bvd", "rtn");
      for (String customerKey : customerKeysList) {
         final List<String> strCrefosList = new ArrayList<>();
         List<TestCrefo> testCrefosList = environmentConfig.getTestCrefosList(absolutePath, customerKey);
         for (TestCrefo testCrefo : testCrefosList) {
            strCrefosList.add(testCrefo.getCrefoNr() + "");
         }
         String selectedProject = "EXPORT_CTE_TO_" + customerKey.toUpperCase();
         tesunRestServiceWLS.erneuteLieferungBeantragenBS(strCrefosList, selectedProject);
      }
   }
*/

    @Test
    public void test_getServicesList() throws Exception {
        TesunRestService tesunRestServiceCteBatchGUI = new TesunRestService(environmentConfig.getRestServiceConfigsForBatchGUI().get(0), new TesunClientJobListener() {
            @Override
            public void notifyClientJob(Level level, Object notifyObject) {
            }

            @Override
            public Object askClientJob(ASK_FOR askFor, Object userObject) {
                return null;
            }
        });
        ServicesList servicesList = tesunRestServiceCteBatchGUI.getJvmInstallationServices();
        Assert.assertNotNull(servicesList);
        Assert.assertFalse(servicesList.getServiceNameUrls().isEmpty());

        Map<String, String> servicesMap = tesunRestServiceCteBatchGUI.getJvmInstallationMap();
        Assert.assertNotNull(servicesMap);
        Assert.assertFalse(servicesMap.isEmpty());
        Assert.assertNotNull(servicesMap.get("BDR (ENE)"));
        Assert.assertNotNull(servicesMap.get("IMPORTCYCLE (ENE)"));
        Assert.assertNotNull(servicesMap.get("LEN (ENE)"));
    }

    @Test
    public void test_getClzInfo() throws Exception {
        doTestGetClzInfo(412);
        doTestGetClzInfo(819);
        doTestGetClzInfo(517);
    }

    private void doTestGetClzInfo(int theClz) throws Exception {
        TesunClzInfo clzInfo = tesunRestServiceWLS.getClzInfo(theClz);
        Assert.assertNotNull(clzInfo);
        Assert.assertEquals("Umgebungskürzel sollte ENE sein!", "ENE", clzInfo.getUmgebungsKuerzel());
        Assert.assertEquals("CLZ sollte " + theClz + " sein!", theClz, clzInfo.getClz());
        Assert.assertTrue("Min-Crefo sollte mit " + theClz + " beginnen!", (clzInfo.getMinCrefo() + "").startsWith(theClz + ""));
        Assert.assertTrue("Anzahl Crefos sollte > " + theClz + " sein!", (clzInfo.getNumCrefos() > 0));
        Assert.assertTrue("Max-Crefo sollte mit  " + theClz + " beginnen!", (clzInfo.getMaxCrefo() + "").startsWith(theClz + ""));
        Assert.assertTrue("Min-Crefo sollte kleiner sein als Max-Crefo", (clzInfo.getMinCrefo() <= clzInfo.getMaxCrefo()));
    }

    @Test
    public void test_getCrefoAnaylseInfo() throws Exception {
        String customerKey = "RTN";
        final long testCrefo = 5272003071L;
        final List<Long> crefosList = Arrays.asList(testCrefo);
        List<RelevanzDecisionMonitoring> relevanzDecisionMonitoringsList = tesunRestServiceJvmImpCycle.getCrefoAnaylseInfos(customerKey, crefosList);
        Assert.assertNotNull(relevanzDecisionMonitoringsList);
        for (RelevanzDecisionMonitoring relevanzDecisionMonitoring : relevanzDecisionMonitoringsList) {
            Assert.assertEquals("5272003071", relevanzDecisionMonitoring.getCrefo());
            Assert.assertEquals("RTN", relevanzDecisionMonitoring.getExportEmpfaengerKuerzel());
            Assert.assertNotNull(relevanzDecisionMonitoring.getBetrachtungsZeitpunkt());
            List<RelevanzMonitoringErgebnis> relevanzMonitoringErgebnisList = relevanzDecisionMonitoring.getMonitoringErgebnisse();
            Assert.assertEquals(1, relevanzMonitoringErgebnisList.size());
        }
    }

    @Test
    public void test_getCrefoAnaylseInfoForInso() throws Exception {
        // de.creditreform.crefoteam.cte.jobadapter.importcycle.monitoringbackend.RelevanzDecisionMonitoringService#getRelevanzDecisionMonitoring
        String customerKey = /*"INSOMON1"*/"INSO_1"; // Krücke 1
        final long testCrefo = 5272003071L;
        final List<Long> crefosList = Arrays.asList(testCrefo);
        List<RelevanzDecisionMonitoring> relevanzDecisionMonitoringsList = tesunRestServiceJvmImpCycle.getCrefoAnaylseInfos(customerKey, crefosList);
        Assert.assertNotNull(relevanzDecisionMonitoringsList);
        for (RelevanzDecisionMonitoring relevanzDecisionMonitoring : relevanzDecisionMonitoringsList) {
            Assert.assertEquals("5272003071", relevanzDecisionMonitoring.getCrefo());
            Assert.assertEquals("INSO", relevanzDecisionMonitoring.getExportEmpfaengerKuerzel());
            Assert.assertNotNull(relevanzDecisionMonitoring.getBetrachtungsZeitpunkt());
            List<RelevanzMonitoringErgebnis> relevanzMonitoringErgebnisList = relevanzDecisionMonitoring.getMonitoringErgebnisse();
            Assert.assertEquals(1, relevanzMonitoringErgebnisList.size());
        }
    }

    @Test
    public void test_pruefeKundenInstallation() throws Exception {
        Map<String, TestCustomer> testCustomerMap = new HashMap<>();
        testCustomerMap.put("RTN", new TestCustomer("RTN", null, new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
        testCustomerMap.put("VSD", new TestCustomer("VSD", null, new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
        testCustomerMap.put("CEF", new TestCustomer("CEF", null, new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
        String responseBody = tesunRestServiceJvmImpCycle.pruefeKundenInstallation(testCustomerMap);
        Assert.assertTrue(responseBody.isEmpty());

        testCustomerMap = new HashMap<>();
        testCustomerMap.put("XYZ", new TestCustomer("XYZ", null, new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
        testCustomerMap.put("YZX", new TestCustomer("YZX", null, new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
        testCustomerMap.put("ZYX", new TestCustomer("ZYX", null, new File(""), TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
        responseBody = tesunRestServiceJvmImpCycle.pruefeKundenInstallation(testCustomerMap);

        Assert.assertTrue("", responseBody.contains("Response-Body:Liste der angefragten Kunden-Kürzel: [YZX, XYZ, ZYX]"));
        Assert.assertTrue("", responseBody.contains("gefundene Fehler:"));
        Assert.assertTrue("", responseBody.contains("FEHLER YZX :"));
        Assert.assertTrue("", responseBody.contains("FEHLER XYZ :"));
        Assert.assertTrue("", responseBody.contains("FEHLER ZYX :"));
        Assert.assertTrue("", responseBody.contains("Im Classpath fehlt eine Implementierung von RelevanzprueferFactoryService"));
    }

    @Test
    public void test_readEntgsForFromCteRest() throws Exception {
        List<Long> expectedEntgsList0 = Arrays.asList(4120013764L, 4120016117L);
        long crefoNr0 = 4120013763L;
        long crefoNr1 = 4120016116L;
        final EntscheidungstraegerInfosAlleCrefos entscheidungstraegerInfosAlleCrefos = tesunRestServiceWLS.readEntscheidugsTraeger(crefoNr0, crefoNr1);
        final List<EntscheidungstraegerInfosProCrefo> entscheidungstraegerInfosProCrefoList = entscheidungstraegerInfosAlleCrefos.getEntscheidungstraegerInfosProCrefo();
        Assert.assertEquals(2, entscheidungstraegerInfosProCrefoList.size());

        final EntscheidungstraegerInfosProCrefo entscheidungstraegerInfosProCrefo0 = entscheidungstraegerInfosProCrefoList.get(0);
        Assert.assertEquals(crefoNr0, entscheidungstraegerInfosProCrefo0.getFirmenCrefo());
        for (EntscheidungstraegerInfo entscheidungstraegerInfo : entscheidungstraegerInfosProCrefo0.getEntscheidungstraegerInfo()) {
            Assert.assertNotNull(entscheidungstraegerInfo);
            Assert.assertEquals(crefoNr0, entscheidungstraegerInfo.getDirectFirmaCrefo());
            Assert.assertTrue(expectedEntgsList0.contains(entscheidungstraegerInfo.getEntscheidungstragerCrefo()));
        }
        final EntscheidungstraegerInfosProCrefo entscheidungstraegerInfosProCrefo1 = entscheidungstraegerInfosProCrefoList.get(1);
        Assert.assertEquals(crefoNr1, entscheidungstraegerInfosProCrefo1.getFirmenCrefo());
        Assert.assertEquals(1, entscheidungstraegerInfosProCrefo1.getEntscheidungstraegerInfo().size());
    }

    @Test
    public void test_createAltBilanz() throws Exception {
        doCreateAltBilanz("/AltBilanzen/nur_bilanzen.xml", 4120000000L);
        doCreateAltBilanz("/AltBilanzen/nur_befreiung.xml", 4120000001L);
        doCreateAltBilanz("/AltBilanzen/bilanzen_und_befreiung.xml", 4120000002L);
    }

    private void doCreateAltBilanz(String xmlFileName, Long crefoNr) throws IOException {
        URL resourceURL = getClass().getResource(xmlFileName);
        String strBilanz = FileUtils.readFileToString(new File(resourceURL.getFile()));
        String errStr = tesunRestServiceWLS.createAltBilanz(crefoNr, strBilanz);
        Assert.assertTrue(errStr.isEmpty());
    }
}
