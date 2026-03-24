package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class UserTaskInsertAndRemoveTestCLZTest extends UserTaskTestBase {
    static Map<String, String> expectedOldVcListMap = new HashMap<>();

    static {
        expectedOldVcListMap.put("VerarbeitungBereich.vc", "DE_ALL, AT, LU");
        expectedOldVcListMap.put("beteiligungenImport.beteiligungen_import.vc", "ALL");
        expectedOldVcListMap.put("beteiligungenImportDelta.beteiligungen_import.vc", "ALL");
        expectedOldVcListMap.put("beteiligungenImportFull.beteiligungen_import.vc", "ALL");
        expectedOldVcListMap.put("beteiligungen_import.vc", "ALL");
        expectedOldVcListMap.put("ctImportDelta.ctimport.vc", "ALL");
        expectedOldVcListMap.put("ctImportFull.ctimport.vc", "ALL");
        expectedOldVcListMap.put("ctebvdexport.vc", "DE_ALL,AT,LU");
        expectedOldVcListMap.put("ctecrmexport.vc", "DE_ALL,AT,LU");
        expectedOldVcListMap.put("ctertnexport.vc", "DE_ALL,AT");
        expectedOldVcListMap.put("ctevsdexport.vc", "307");
        expectedOldVcListMap.put("ctimport.vc", "ALL");
        expectedOldVcListMap.put("deltaImport.ctimport.vc", "ALL");
        expectedOldVcListMap.put("importCycle.beteiligungen_import.vc", "ALL");
        expectedOldVcListMap.put("importCycle.ctimport.vc", "ALL");
        expectedOldVcListMap.put("relevanzMigrationDelta.relevanzmigration.vc", "ALL");
        expectedOldVcListMap.put("relevanzMigrationFull.relevanzmigration.vc", "ALL");
    }

    static Map<String, String> expectedNewVcListMap = new HashMap<>();

    static {
        expectedNewVcListMap.put("VerarbeitungBereich.vc", "412,912");
        expectedNewVcListMap.put("beteiligungenImport.beteiligungen_import.vc", "412,912");
        expectedNewVcListMap.put("beteiligungenImportDelta.beteiligungen_import.vc", "412,912");
        expectedNewVcListMap.put("beteiligungenImportFull.beteiligungen_import.vc", "412,912");
        expectedNewVcListMap.put("beteiligungen_import.vc", "412,912");
        expectedNewVcListMap.put("ctImportDelta.ctimport.vc", "412,912");
        expectedNewVcListMap.put("ctImportFull.ctimport.vc", "412,912");
        expectedNewVcListMap.put("ctebvdexport.vc", "412,912");
        expectedNewVcListMap.put("ctecrmexport.vc", "412,912");
        expectedNewVcListMap.put("ctertnexport.vc", "412,912");
        expectedNewVcListMap.put("ctevsdexport.vc", "412");
        expectedNewVcListMap.put("ctimport.vc", "412,912");
        expectedNewVcListMap.put("deltaImport.ctimport.vc", "412,912");
        expectedNewVcListMap.put("importCycle.beteiligungen_import.vc", "412,912");
        expectedNewVcListMap.put("importCycle.ctimport.vc", "412,912");
        expectedNewVcListMap.put("relevanzMigrationDelta.relevanzmigration.vc", "412,912");
        expectedNewVcListMap.put("relevanzMigrationFull.relevanzmigration.vc", "412,912");
        expectedNewVcListMap.put("cte_cta_validation.mode", "VALIDIERE_XSD_SCHEMA");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testRunTaskIntegration() throws Exception {
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = new TreeMap<>();
        activeTestCustomersMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, getTestCustomerMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, false));
        activeTestCustomersMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, getTestCustomerMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, false));

        UserTaskPrepareTestSystem cut1 = new UserTaskPrepareTestSystem(environmentConfig, this);
        boolean useOnylTestClzs = true;
        Map<String, Object> taskParamsMap = new HashMap<>();
        taskParamsMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS, activeTestCustomersMapMap);
        taskParamsMap.put(TesunClientJobListener.UT_TASK_PARAM_USE_ONLY_TEST_CLZ, useOnylTestClzs ? "true" : "false");
        Map<String, Object> resultMap1 = cut1.runTask(taskParamsMap);
        Assert.assertTrue(resultMap1.size() == 4);

        List<String> checkErrorsList = checkMaps(resultMap1, useOnylTestClzs);
        if (!checkErrorsList.isEmpty()) {
            Assert.fail("Check der Maps war nicht OK!\n" + checkErrorsList);
        }

        UserTaskRestoreTestSystem cut2 = new UserTaskRestoreTestSystem(environmentConfig, this);
        Map<String, Object> resultMap2 = cut2.runTask(taskParamsMap);
        Assert.assertEquals(resultMap2, resultMap1);
        Assert.assertEquals(resultMap2.size(), resultMap1.size());

        checkErrorsList = checkMaps(resultMap2, useOnylTestClzs);
        if (!checkErrorsList.isEmpty()) {
            Assert.fail("Check der Maps war nicht OK!\n" + checkErrorsList);
        }
    }

    private List<String> checkMaps(Map<String, Object> resultMap1, boolean useOnylTestClzs) {
        List<String> checkErrorsList = new ArrayList<>();

        Map<String, Object> oldVcListMap = (Map<String, Object>) resultMap1.get(TestSupportClientKonstanten.OLD_VC_LIST);
        if (oldVcListMap.size() != expectedOldVcListMap.size()) {
            checkErrorsList.add("Old-Map enthält falsche Anzahl an Properties!");
        }
        expectedOldVcListMap.keySet().forEach(key -> {
            String value = expectedOldVcListMap.get(key);
            checkProperty(checkErrorsList, "Old", oldVcListMap, value, key, useOnylTestClzs);
        });
        Map<String, Object> newVcListMap = (Map<String, Object>) resultMap1.get(TestSupportClientKonstanten.NEW_VC_LIST);
        if (newVcListMap.size() != expectedNewVcListMap.size()) {
            checkErrorsList.add("New-Map enthält falsche Anzahl an Properties!");
        }
        expectedNewVcListMap.keySet().forEach(key -> {
            String value = expectedNewVcListMap.get(key);
            checkProperty(checkErrorsList, "New", newVcListMap, value, key, useOnylTestClzs);
        });

        return checkErrorsList;
    }

    private void checkProperty(List<String> checkErrorsList, String mapName, Map<String, Object> theMap, String expectedValue, String propertyName, boolean useOnylTestClzs) {
        String propValue = theMap.get(propertyName).toString();
        String[] split = propValue.split(":");
        if(split.length > 1) {
            propValue = split[1];
        }
        if (useOnylTestClzs) {
            if (!propValue.equals(expectedValue)) {
                checkErrorsList.add(mapName + "-Map enthält falschen Wert für Property '" + propertyName + "'! Expected: " + expectedValue + ", actual: " + theMap.get(propertyName));
            }
        } else {
            if (!propValue.contains(expectedValue)) {
                checkErrorsList.add(mapName + "-Map enthält falschen Wert für Property '" + propertyName + "'! Expected: " + expectedValue + ", actual: " + theMap.get(propertyName));
            }
        }
    }

}
