package de.creditreform.crefoteam.cte.tesun.fachwert;

import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.*;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestServiceIntegrationTestBase;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FachwerteTest extends TesunRestServiceIntegrationTestBase {

    public FachwerteTest() {
        super("ENE");
    }

    @Test
    public void test_FachwertMesswerte() throws Exception {

    }

    @Test
    public void test_getFachwertBenannteGruppe() throws Exception {
        final File outputFile = createTestOutputFile("FachwertBenannteGruppe.csv");
        FachwertBenannteGruppenListe fachwertBenannteGruppenListe = tesunRestServiceWLS.getFachwertBenannteGruppe();
        Stream<FachwertBenannteGruppe> fachwertBenannteGruppeStream = fachwertBenannteGruppenListe.getBenannteGruppens().stream();
        final StringBuilder stringBuilderTitle = new StringBuilder();
        stringBuilderTitle.append("Benannte Fachwert-Gruppen\n");
        stringBuilderTitle.append("NamePostfix;UpdatesBisIncl;RefVersionCT;RefVersionKeylist\n");
        FileUtils.writeStringToFile(outputFile, stringBuilderTitle.toString(), false);
        fachwertBenannteGruppeStream.forEach(fachwertBenannteGruppe -> {
            final StringBuilder stringBuilderDataLine = new StringBuilder();
            stringBuilderDataLine.append(fachwertBenannteGruppe.getNamePostfix()).append(TestSupportClientKonstanten.COLUMN_DELIMITER)
                    .append(TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS.format(fachwertBenannteGruppe.getEnumUpdatesBisIncl())).append(TestSupportClientKonstanten.COLUMN_DELIMITER)
                    .append(fachwertBenannteGruppe.getRefVersionCT()).append(TestSupportClientKonstanten.COLUMN_DELIMITER)
                    .append(fachwertBenannteGruppe.getRefVersionKeylist()).append("\n");
            try {
                FileUtils.writeStringToFile(outputFile, stringBuilderDataLine.toString(), true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Test
    public void test_listAvailableFWUpdates() throws Exception {
        final File outputFile = createTestOutputFile("AllFachwertUpdatesForAllTypes.csv");
        FachwertAktualisierungList fachwertAktualisierungList = tesunRestServiceWLS.getAllFachwertUpdatesForAllTypes();
        List<FachwertAktualisierungInfo> aktualisierungInfoList = fachwertAktualisierungList.getAktualisierungs();
        StringBuilder stringBuilderHeader = new StringBuilder();
        stringBuilderHeader.append("Available-Fachwert-Updates for all Types").append("\n");
        dumpFachwertAktualisierungInfoList(outputFile, stringBuilderHeader, aktualisierungInfoList);
    }

    @Test
    public void test_getLastUpdatesForAllFWTypeEnums() throws Exception {
        File file = createTestOutputFile("LastFachwertUpdatesForAllTypes.csv");
        final FachwertAktualisierungList tesunFachwertUpdates = tesunRestServiceWLS.getLastFachwertUpdatesForAllTypes();
        final List<FachwertAktualisierungInfo> aktualisierungsList = tesunFachwertUpdates.getAktualisierungs();
        StringBuilder stringBuilderHeader = new StringBuilder();
        stringBuilderHeader.append("Last-Fachwert-Updates for all Types").append("\n");
        dumpFachwertAktualisierungInfoList(file, stringBuilderHeader, aktualisierungsList);
    }

    @Test
    public void test_getAllUpdatesForFWTypeEnum() throws Exception {
        File file = createTestOutputFile("AllFachwertUpdatesForType.csv");
        final String strategyName = "Anrede";
        final FachwertAktualisierungList tesunFachwertUpdates = tesunRestServiceWLS.getAllFachwertUpdatesForType(strategyName);
        final List<FachwertAktualisierungInfo> aktualisierungsList = tesunFachwertUpdates.getAktualisierungs();
        StringBuilder stringBuilderHeader = new StringBuilder();
        stringBuilderHeader.append("Available Fachwert-Updates for Strategy '")
                .append(strategyName)
                .append("'")
                .append("\n");
        dumpFachwertAktualisierungInfoList(file, stringBuilderHeader, aktualisierungsList);
    }

    @Test
    public void test_getLastUpdatesForFWTypeEnumTilDateIncl() throws Exception {
        File file = createTestOutputFile("LastFachwertUpdatesForTypeTilDateIncl.csv");
        final String strategyName = "Anrede";
        final String strDateBisIncl = "2022-01-01";
        final FachwertAktualisierungList tesunFachwertUpdates = tesunRestServiceWLS.getLastFachwertUpdatesForTypeTilDateIncl(strategyName, strDateBisIncl);
        final List<FachwertAktualisierungInfo> aktualisierungsList = tesunFachwertUpdates.getAktualisierungs();
        StringBuilder stringBuilderHeader = new StringBuilder();
        stringBuilderHeader.append("Last Fachwert-Updates for Strategy '")
                .append(strategyName)
                .append("' bis incl. ")
                .append(strDateBisIncl)
                .append("\n");
        dumpFachwertAktualisierungInfoList(file, stringBuilderHeader, aktualisierungsList);
    }

    @Test
    public void test_getLastUpdateForFWTypeEnumTilDateIncl() throws Exception {
        File file = createTestOutputFile("LastFachwertUpdateForTypeTilDateIncl.csv");
        final String strategyName = "Anrede";
        String strDateBisIncl = "2019-01-01";
        final FachwertAktualisierungInfoByID aktualisierungInfoByID = tesunRestServiceWLS.getLastFachwertUpdateForTypeTilDateIncl(strategyName, strDateBisIncl);
        final FachwertAktualisierungInfo fachwertAktualisierungInfo = aktualisierungInfoByID.getAktualisierung();
        StringBuilder stringBuilderHeader = new StringBuilder();
        stringBuilderHeader.append("Last Fachwert-Update for Strategy '")
                .append(strategyName)
                .append("' bis incl. ")
                .append(strDateBisIncl)
                .append("\n");
        dumpFachwertAktualisierungInfoList(file, stringBuilderHeader, Arrays.asList(fachwertAktualisierungInfo));
    }

    @Test
    public void test_getLastUpdatesForCustomers() throws Exception {
        final File outputFile = createTestOutputFile("LastUpdatesForCustomer.csv");
        Stream<Map.Entry<String, TestCustomer>> testCustomerEntryStream = testCustomerMap.entrySet().stream();

        FachwertAktualisierungList lastFachwertUpdatesForAllTypes = tesunRestServiceWLS.getLastFachwertUpdatesForAllTypes();
        String strTitle = buildHeaderForFachwertAktualisierungInfosList(lastFachwertUpdatesForAllTypes.getAktualisierungs()).toString();
        FileUtils.writeStringToFile(outputFile, strTitle, false);

        testCustomerEntryStream.forEach(testCustomerEntry -> {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            final FWUpdatesListPerCustomer fwUpdatesListPerCustomer = tesunRestServiceWLS.getLastFachwertUpdatesForCustomer(testCustomer.getProcessIdentifier());
            String strDataLine = buildDataLineForFachwertAktualisierungInfosList(testCustomer, fwUpdatesListPerCustomer).toString();
            try {
                FileUtils.writeStringToFile(outputFile, strDataLine, true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Test
    public void test_getAllCustomerConfigs() throws Exception {
        final File outputFile = createTestOutputFile("AllCustomerConfigs.csv");
        String strTitle = "Prozessname;Aktualisierungsdatum;PD-Version\n";
        FileUtils.writeStringToFile(outputFile, strTitle, false);
        final KundenKonfigList tesunFachwertKundenConfigs = tesunRestServiceWLS.getAllCustomerConfigs();
        List<KundenKonfig> kundenKonfigList = tesunFachwertKundenConfigs.getKonfigs();
        StringBuilder stringBuilderLines = new StringBuilder();
        kundenKonfigList.stream().forEach(kundenKonfig -> {
            stringBuilderLines.append(kundenKonfig.getProzessName()).append(";");
            stringBuilderLines.append(TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS.format(kundenKonfig.getAktualisierungsdatum())).append(";");
            stringBuilderLines.append(kundenKonfig.getPdversion()).append("\n");
        });
        try {
            FileUtils.writeStringToFile(outputFile, stringBuilderLines.toString(), true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void logElapsedTime(String strAction, long currentTimeMillisStart, long currentTimeMillisEnd) {
        final long l = currentTimeMillisEnd - currentTimeMillisStart;
        logger.info("Aufruf " + strAction + " hat " + l + " ms in anspruch genommen.");
    }

    private File createTestOutputFile(String fileName) {
        URL rscUrl = getClass().getResource("/");
        String strFName = environmentConfig.getCurrentEnvName() + "-" + fileName;
        File file = new File(rscUrl.getFile(), strFName);
        tesunClientJobListener.notifyClientJob(Level.INFO, "Der Output wird in der Datei \n\t'" + file.getAbsolutePath() + "'\ngespeichert.");
        return file;
    }

    private StringBuilder buildHeaderForFachwertAktualisierungInfosList(List<FachwertAktualisierungInfo> aktualisierungsList) {
        StringBuilder stringBuilderTitle = new StringBuilder();
        stringBuilderTitle.append("Kunde;Konfig-Datum;");
        aktualisierungsList.stream().forEach(fachwertAktualisierungInfo -> {
            stringBuilderTitle.append(fachwertAktualisierungInfo.getFachwertType()).append(TestSupportClientKonstanten.COLUMN_DELIMITER);
        });
        return stringBuilderTitle;
    }

    private StringBuilder buildDataLineForFachwertAktualisierungInfosList(TestCustomer testCustomer, FWUpdatesListPerCustomer fwUpdatesListPerCustomer) {
        StringBuilder stringBuilderDataLine = new StringBuilder();
        stringBuilderDataLine.append(testCustomer.getCustomerKey()).append(TestSupportClientKonstanten.COLUMN_DELIMITER);
        stringBuilderDataLine.append(TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS.format(fwUpdatesListPerCustomer.getCustomerConfigDate())).append(TestSupportClientKonstanten.COLUMN_DELIMITER);

        final List<FachwertAktualisierungInfo> fwUpdatesLists = fwUpdatesListPerCustomer.getFwUpdatesLists();
        Stream<FachwertAktualisierungInfo> fwUpdatesListsStream = fwUpdatesLists.stream();
        fwUpdatesListsStream.forEach(fachwertAktualisierungInfo -> {
            stringBuilderDataLine.append(TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS.format(fachwertAktualisierungInfo.getAktualisierungsDatum())).append(TestSupportClientKonstanten.COLUMN_DELIMITER);
        });
        stringBuilderDataLine.append("\n");
        return stringBuilderDataLine;
    }

    private void dumpFachwertAktualisierungInfoList(File file, StringBuilder stringBuilderHeader, List<FachwertAktualisierungInfo> aktualisierungsList) throws IOException {
        StringBuilder stringBuilderDataLine = new StringBuilder(stringBuilderHeader);
        stringBuilderDataLine.append("\n");
        for (FachwertAktualisierungInfo fachwertAktualisierungInfo : aktualisierungsList) {
            stringBuilderDataLine.append(TestSupportClientKonstanten.COLUMN_DELIMITER)
                    .append(fachwertAktualisierungInfo.getFachwertType()).append(TestSupportClientKonstanten.COLUMN_DELIMITER)
                    .append(TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS.format(fachwertAktualisierungInfo.getAktualisierungsDatum())).append("\n");
        }
        stringBuilderDataLine.append("\n");
        FileUtils.writeStringToFile(file, stringBuilderDataLine.toString());
    }
}
