package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import de.creditreform.cte.inso.monitor.xmlbinding.PgpKey;
import de.creditreform.cte.inso.monitor.xmlbinding.XmlKunde;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;

public class UserTaskPrepareTestSystem extends AbstractUserTaskRunnable {
    public static final String COMMAND = "UserTask PREPARE_TEST_SYSTEM";
    protected TesunRestService tesunRestServiceWLS;
    protected TesunRestService tesunRestServiceJvmInsoBackend;

    public UserTaskPrepareTestSystem(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) throws PropertiesException {
        super(environmentConfig, tesunClientJobListener);
        tesunRestServiceJvmInsoBackend = new TesunRestService(environmentConfig.getRestServiceConfigsForJvmInsoBackend().get(0), tesunClientJobListener);
        tesunRestServiceWLS = new TesunRestService(this.environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), tesunClientJobListener);
    }

    @Override
    public Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception {
        TestSupportClientKonstanten.TEST_PHASE testPhase = (TestSupportClientKonstanten.TEST_PHASE) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        notifyUserTask(Level.INFO, buildNotifyStringForClassName(testPhase));
        if (checkDemoMode((Boolean)taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_DEMO_MODE))) {
            return taskVariablesMap;
        }
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customersMapMap = (Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>>) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        Map<String, TestCustomer> customersMapPhase2 = customersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2); // NUR PHASE-2
        // Wenn INSO dabei ist, müssen zusätzlich T/W/M-Kunden hinzugefügt werden
        appendInsoCustomers(customersMapPhase2);
        Boolean onlyTestCrefos = Boolean.valueOf(taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_USE_ONLY_TEST_CLZ).toString());

        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "VOR-" + COMMAND, customersMapPhase2);

        // Properties in DB restaurieren...
        tesunRestServiceWLS.restoreEnvironmentProperties();
        Map<String, Pair<String, String>> uniquePairsMap = new TreeMap<>();
        List<CteEnvironmentPropertiesTupel> propertiesTupelList = new ArrayList<>();
        Iterator<Map.Entry<String, TestCustomer>> entryIterator = customersMapPhase2.entrySet().iterator();
        while (entryIterator.hasNext()) {
            TestCustomer testCustomer = entryIterator.next().getValue();
            if (testCustomer.getCustomerKey().contains("INSO")) {
                checkInsoKunde();
            }
            for (MutablePair<String, String> propertyPair : testCustomer.getPropertyPairsList()) {
                if(propertyPair.getLeft().endsWith(".vc")) {
                    extendVcList(propertyPair, onlyTestCrefos);
                }
                if (!uniquePairsMap.containsKey(propertyPair.getLeft())) {
                    CteEnvironmentPropertiesTupel propertiesTupel = new CteEnvironmentPropertiesTupel();
                    propertiesTupel.setKey(propertyPair.getLeft());
                    propertiesTupel.setValue(propertyPair.getRight());
                    propertiesTupel.setDbOverride(true);
                    propertiesTupelList.add(propertiesTupel);
                    uniquePairsMap.put(propertyPair.getLeft(), propertyPair);
                }
            }
        }
        // Property für XSD-Validierung auf "VALIDIERE_XSD_SCHEMA" setzen
        CteEnvironmentPropertiesTupel propertiesTupel = new CteEnvironmentPropertiesTupel();
        propertiesTupel.setKey("cte_cta_validation.mode");
        propertiesTupel.setValue("VALIDIERE_XSD_SCHEMA");
        propertiesTupel.setDbOverride(true);
        propertiesTupelList.add(propertiesTupel);

        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "NACH-" + COMMAND, customersMapPhase2);
        uniquePairsMap.keySet().forEach(pairKey -> {
            notifyUserTask(Level.INFO, "\nSetze Property '" + pairKey + "' auf '" + uniquePairsMap.get(pairKey).getRight() + "'");
        });

        notifyUserTask(Level.INFO, "\nSpeichere die Erweiterten Properties in die CTE-Datebnak...");
        CteEnvironmentProperties cteEnvironmentProperties = new CteEnvironmentProperties();
        cteEnvironmentProperties.getProperties().addAll(propertiesTupelList);
        tesunRestServiceWLS.setEnvironmentProperties(cteEnvironmentProperties);

/*
            // Options.cfg einlesen und in die Properties-Liste des Kunden aufnehmen
            File optionsFile = new File(testCustomer.getItsqRefExportsDir(), TestSupportClientKonstanten.OPTIONS_CONFIG_FILENAME);
            List<String> strLinesList = FileUtils.readLines(optionsFile);
            strLinesList.forEach(strLine -> {
                String[] split = strLine.replaceAll(":\\\t", ":").split(":");
                if(split.length != 2) {
                    throw new RuntimeException("Falsches Format in der Datei " + optionsFile.getAbsolutePath());
                }
                propertyPairsList.add(new MutablePair<>(split[0], split[1]));
            });
*/
        return taskVariablesMap;
    }

    private void extendVcList(MutablePair<String, String> propertyPair, boolean onlyTestCrefos) throws PropertiesException {
        String value = propertyPair.getRight() != null ? propertyPair.getRight() : "";
        boolean containsALL = value.contains(",ALL") || value.equals("ALL");
        boolean containsAT = value.contains("AT") || containsALL;
        boolean containsLU = value.contains("LU") || containsALL;
        if (onlyTestCrefos) {
            propertyPair.setRight(TestSupportClientKonstanten.TEST_CLZ_412);
        } else {
            propertyPair.setRight(value);
        }
        if (containsAT) {
            propertyPair.setRight(propertyPair.getRight() + "," + environmentConfig.getTargetClzForAtPseudoCrefos());
        }
        if (containsLU) {
            propertyPair.setRight(propertyPair.getRight() + "," + environmentConfig.getTargetClzForLuPseudoCrefos());
        }
    }

    private Map<String, TestCustomer> appendInsoCustomers(Map<String, TestCustomer> selectedCustomersMap) {
        Map<String, TestCustomer> selectedCustomersMapX = new HashMap<>(selectedCustomersMap);
        Iterator<String> iterator = selectedCustomersMapX.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.startsWith("INSO")) {
                TestCustomer insoTestCustomer = selectedCustomersMapX.get(key);
                selectedCustomersMapX.put("INSOMON2T", TestCustomer.cloneInsoMonitorPhase2(insoTestCustomer, "inso.deltaExportKundeDaily", "inso.deltaUploadKunde", "EXPORT_CTE_TO_INSO_2T"));
                selectedCustomersMapX.put("INSOMON2W", TestCustomer.cloneInsoMonitorPhase2(insoTestCustomer, "inso.deltaExportKundeWeekly", "inso.deltaUploadKunde", "EXPORT_CTE_TO_INSO_2W"));
                selectedCustomersMapX.put("INSOMON2M", TestCustomer.cloneInsoMonitorPhase2(insoTestCustomer, "inso.deltaExportKundeMonthly", "inso.deltaUploadKunde", "EXPORT_CTE_TO_INSO_2M"));
                break;
            }
        }
        return selectedCustomersMapX;
    }

    private void checkInsoKunde() {
        XmlKunde xmlKunde = tesunRestServiceJvmInsoBackend.readInsoKunde("Test-Tool");
        StringBuilder stringBuilder = new StringBuilder();
        if (xmlKunde == null) {
            throw new RuntimeException("INSO-Kunde 'Test-Tool' wurde nicht konfiguriert!");
        }
        if (!"Test-Tool-Company".equals(xmlKunde.getFirmenName())) {
            stringBuilder.append("\nFirmenname wurde geändert!");
        }
        if (!"Test-Tool".equals(xmlKunde.getKundenKuerzel())) {
            stringBuilder.append("\nKunden-Kürzel wurde geändert!");
        }
        if (xmlKunde.getProduktivSeit() == null) {
            stringBuilder.append("\nProduktiv-Seit wurde geändert!");
        }
        if (xmlKunde.getPrduktivBis() != null) {
            stringBuilder.append("\nProduktiv-Bis wurde geändert!");
        }
        if (!xmlKunde.isAktiv()) {
            stringBuilder.append("\nAktiv wurde geändert!");
        }
        if (xmlKunde.isLoeschKennzeichen() == null || xmlKunde.isLoeschKennzeichen()) {
            stringBuilder.append("\nLoeschKennzeichen wurde geändert!");
        }
        List<PgpKey> pgpKeyList = xmlKunde.getPgpKeyList();
        if (pgpKeyList.size() < 1) {
            stringBuilder.append("\nPGP-Keys wurde geändert!");
        }
        if (!"zew_pgp_public_key.asc".equals(pgpKeyList.get(0).getFileName())) {
            stringBuilder.append("\nPGP-Key wurde geändert!");
        }
        if (stringBuilder.length() > 0) {
            throw new RuntimeException("INSO-Kunde 'Test-Tool' passt nicht zum Test-Tool!\nBitte die Kundenkonfiguration anpassen!" + stringBuilder.toString());
        }
    }

}
