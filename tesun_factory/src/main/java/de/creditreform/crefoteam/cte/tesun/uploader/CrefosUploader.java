package de.creditreform.crefoteam.cte.tesun.uploader;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.*;
import de.creditreform.crefoteam.cte_cta.statistik.xmlbinding.CtaStatistik;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

public class CrefosUploader {
    private final TesunClientJobListener tesunClientJobListener;
    final TesunRestService tesunRestServiceWLS;
    final TesunRestService tesunRestServiceJVMImportC;
    private Map<Long, UploadInfo> uploadInfosMap = new TreeMap<>();
    private Map<Long, TestCrefo> uploadedCrefosMap = new TreeMap<>();
    final Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap;
    final String uploadPath;

    public CrefosUploader(String uploadPath, Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap, TesunRestService tesunRestServiceWLS, TesunRestService tesunRestServiceJVMImportC, TesunClientJobListener tesunClientJobListener) {
        this.uploadPath = uploadPath;
        this.ab30CrefoToPropertiesMap = ab30CrefoToPropertiesMap;
        this.tesunRestServiceWLS = tesunRestServiceWLS;
        this.tesunRestServiceJVMImportC = tesunRestServiceJVMImportC;
        this.tesunClientJobListener = tesunClientJobListener;
    }

    public void uploadCrefos(TestCustomer testCustomer, String command) {
        Iterator<String> scenariosIterator = testCustomer.getTestScenariosMap().keySet().iterator();
        while (scenariosIterator.hasNext()) {
            TestScenario testScenario = testCustomer.getTestScenariosMap().get(scenariosIterator.next());
            Map<String, TestCrefo> testCrefosMap = testScenario.getTestFallNameToTestCrefoMap();
            testCrefosMap.entrySet().forEach(testCrefoEntry -> {
                TestCrefo testCrefo = testCrefoEntry.getValue();
                uploadCrefo(testScenario, testCrefo, command);
            });
        }
    }

    private void uploadCrefo(TestScenario testScenario, TestCrefo testCrefo, String command) {
        File xmlFile = new File(uploadPath, testCrefo.getPseudoCrefoNr() + ".xml") ;
        if (xmlFile.exists()) {
            try {
                if(uploadCrefoWithRetry(testScenario, testCrefo, xmlFile, command) ) {
                    uploadAdditiveInfosForCrefo(testScenario, testCrefo, command);
                }
            } catch (Exception ex) {
                String strInfo = String.format("\n\tUpload der Crefo-XML-Datei '%s' für den Testfall %s gescheitert!\n\tException:\n\t%s",
                        TesunUtilites.shortPath(xmlFile, 50), testCrefo.getPseudoCrefoNr(), ex.getMessage());
                addTestResultAndNotify(testScenario, strInfo, command);
            }
        }
    }

    private boolean uploadCrefoWithRetry(TestScenario testScenario, TestCrefo testCrefo, File xmlFile, String command) {
        String strInfo = "";
        boolean checkOk = checkCrefoClz(testScenario, testCrefo, command);
        if (!checkOk) {
            String strClzList = Arrays.toString(TestSupportClientKonstanten.TEST_CLZ_LIST);
            strInfo = "\n\t\tDie angegebene Test-Crefo " + testCrefo + " muss eine für den Test gültige CLZ aus : " + strClzList + " sein!";
            addTestResultAndNotify(testScenario, strInfo, command);
            return false;
        }
        strInfo = String.format("\n\tCrefo-XML-Datei '%s' für den Testfall %s:%s:%s wird hochgeladen...",
                TesunUtilites.shortPath(xmlFile, 50), testScenario.getTestCustomer().getCustomerKey(), testScenario.getScenarioName(), testCrefo);
        addTestResultAndNotify(null, strInfo, command);
        UploadInfo newUploadInfo = new UploadInfo(testCrefo.getPseudoCrefoNr(), xmlFile);
        Boolean newUploadIsFirma = newUploadInfo.isFirma();
        int numTries = 0;
        while (true) {
            try {
                UploadInfo existentUploadInfo = uploadInfosMap.get(testCrefo.getPseudoCrefoNr());
                if (existentUploadInfo == null) {
                    tesunRestServiceWLS.uploadCrefo(testCrefo.getPseudoCrefoNr(), xmlFile, TestSupportClientKonstanten.AB3_0_XSD);
                    uploadInfosMap.put(testCrefo.getPseudoCrefoNr(), newUploadInfo);
                    strInfo = String.format("\n\t\t... wurde erfolgreich als %s hochgeladen", newUploadIsFirma ? "Firma" : "Privatperson");
                    addTestResultAndNotify(null, strInfo, command);
                    return true;
                } else {
                    Boolean existentIsFirma = existentUploadInfo.isFirma();
                    if (newUploadIsFirma != existentIsFirma) {
                        strInfo = String.format("\n\t\t!! Die Crefo %d wurde schon als %s hochgeladen, diesmal aber als %s!!!", existentUploadInfo.getCrefo(), (existentIsFirma ? "Firma" : "Privatperson"), (newUploadIsFirma ? "Firma" : "Privatperson"));
                        addTestResultAndNotify(testScenario, strInfo, command);
                    }
                }
                return false;
            } catch (Exception ex) {
                numTries++;
                String message = ex.getMessage();
                if (ex.getCause() != null) {
                    message += ex.getCause().getMessage();
                }
                if (message.contains("validier")) {
                    strInfo = "\t!!! " + message;
                    addTestResultAndNotify(testScenario, strInfo, command);
                    return false;
                }
                else if ((numTries > 3) || message.contains("404") || message.contains("403")) {
                    strInfo = "\n\t==> Der Upload für die Terstcrefo " + testCrefo + " wird nach " + numTries + " Versuchen übersprungen!";
                    addTestResultAndNotify(testScenario, strInfo, command);
                    return false;
                }
                else {
                    String[] split = message.split("Response-Body:");
                    strInfo = "\n\t--> Exception beim Upload: Response-Body:" + split[1] ;
                    addTestResultAndNotify(testScenario, strInfo, command);
                }
            }
        }
    }

    private void uploadAdditiveInfosForCrefo(TestScenario testScenario, TestCrefo testCrefo, String command) {
        String strInfo;
        AB30XMLProperties ab30XMLProperties = ab30CrefoToPropertiesMap.get(testCrefo.getPseudoCrefoNr());
        if (ab30XMLProperties != null) {
            Long auftragClz = ab30XMLProperties.getAuftragClz();
            if (auftragClz != null) {
                createOrderForCrefo(testScenario, testCrefo, auftragClz, command);
            }
            AB30XMLProperties.BILANZEN_TYPE bilanzType = ab30XMLProperties.getBilanzType();
            if (bilanzType != null && !bilanzType.equals(AB30XMLProperties.BILANZEN_TYPE.KEINE)) {
                createBilanzForCrefo(testScenario, testCrefo, bilanzType, command);
            }
            AB30XMLProperties.EH_PROD_AUFTR_TYPE ehProdAuftrType = ab30XMLProperties.getEhProduktAuftragType();
            if (AB30XMLProperties.isUploadable(ehProdAuftrType)) {
                createEhProduktAuftragForCrefo(testScenario, testCrefo, ehProdAuftrType, command);
            }
            if (ab30XMLProperties.isMitCtaStatistik()) {
                createCtaStatistikForCrefo(testScenario, testCrefo, command);
            }
            if (ab30XMLProperties.isMitDsgVoSperre()) {
                setDsgVoSperre(testScenario, testCrefo, command);
            }
            TreeSet<Long> btlgCrefosList = ab30XMLProperties.getBtlgCrefosList();
            for (Long btlggCrefo : btlgCrefosList) {
                TestCrefo btlgTestCreo = uploadedCrefosMap.get(btlggCrefo);
                if (btlgTestCreo != null) {
                    strInfo = String.format("\n\t! XML-Datei zur Beteiligten-Crefo %d wurde bereits für den Testfall %s hochgeladen", btlggCrefo, testCrefo);
                    addTestResultAndNotify(null, strInfo, command);
                    continue;
                }
                File xmlFile = new File(uploadPath, testCrefo.getPseudoCrefoNr() + ".xml");
                uploadCrefoWithRetry(testScenario, testCrefo, xmlFile, command);
            }
/*
        } else {
            strInfo = String.format("\n\tFür die Crefo '%d' wurde keine Mapping in der Properties-Datei '%s' erstellt!", testCrefo, TestSupportClientKonstanten.USED_CREFOS_PROPS_FILENAME);
            addTestResultAndNotify(null, strInfo, command);
*/
        }
    }

    private void setDsgVoSperre(TestScenario testScenario, TestCrefo testCrefo, String command) {
        try {
            tesunRestServiceWLS.setDsgVoSperre(testCrefo.getPseudoCrefoNr(), Boolean.TRUE);
            String strInfo = "\n\tDSGVO-Sperre für die Crefo " + testCrefo + " wurde erfolgreich gesetzt.";
            addTestResultAndNotify(null, strInfo, command);
        } catch (Exception ex) {
            String strInfo = String.format("Setzen der DSGVO-Sperre für die Crefo '%s' gescheitert!\n\tException: \n", testCrefo, ex.getMessage());
            addTestResultAndNotify(testScenario, strInfo, command);
        }
    }

    private void createCtaStatistikForCrefo(TestScenario testScenario, TestCrefo testCrefo, String command) {
        try {
            CtaStatistik ctaStatistik = new CtaStatistik();
            ctaStatistik.setCrefonummer(BigInteger.valueOf(testCrefo.getPseudoCrefoNr()));
            ctaStatistik.setAnzahlAuftraegeNachtragspflichtig(BigInteger.valueOf(11));
            ctaStatistik.setAnzahlAuskuenfte(BigInteger.valueOf(22));
            ctaStatistik.setDatumLetzteAuskunft(Calendar.getInstance());
            tesunRestServiceWLS.createCtaStatistikForCrefo(ctaStatistik);
            String strInfo = "\n\tCTA-Statistik für die Crefo " + testCrefo + " wurde erfolgreich hochgeladen.";
            addTestResultAndNotify(null, strInfo, command);
        } catch (Exception ex) {
            String strInfo = String.format("Hochladen der CTA-Statistik für die Crefo '%s' gescheitert!\n\tException: \n", testCrefo, ex.getMessage());
            addTestResultAndNotify(testScenario, strInfo, command);
        }
    }

    private void createOrderForCrefo(TestScenario testScenario, TestCrefo testCrefo, Long auftragClz, String command) {
        try {
            // REST-Aufruf: PUT
            // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/ikaros/beauftragungmanuell/1234567?crefo=9876543210&empfaenger=411
            // DB: select * from &umg.admin.STAGING_IKAROS_AUFTRAG ORDER BY AUFTRAGS_KENNUNG_IKAROS, DATUM_EINGANG_CTE DESC;
            String strAuftrKennung = tesunRestServiceJVMImportC.orderCrefo(testCrefo.getPseudoCrefoNr(), auftragClz);
            String strInfo = String.format("\n\tAuftrag '%s' erfolgreich erteilt", strAuftrKennung);
            addTestResultAndNotify(null, strInfo, command);
        } catch (Exception ex) {
            String strInfo = String.format("\n\tAuftrag für die Crefo '%d' gescheitert!\n\tException: \n", testCrefo, ex.getMessage());
            addTestResultAndNotify(testScenario, strInfo, command);
        }
    }

    private void createBilanzForCrefo(TestScenario testScenario, TestCrefo testCrefo, AB30XMLProperties.BILANZEN_TYPE bilanzType, String command) {
        try {
            // REST-Aufruf: POST
            //  http://http://rhsctem015.ecofis.de:7077/cte_betrieb_service/altbilanzen/0/2019-10-25_11:00:11?crefo=4120114355  +  Bilanz-XML als String-Entity
            // DB: select * from &umg.admin.BESTAND_ALTBILANZ;
            //     select * from &umg.admin.BESTAND_ALTBILANZ_BEFR;
            File xmlFile = new File(uploadPath, bilanzType.getXmlFileName());
            String strBilanz = FileUtils.readFileToString(xmlFile);
            tesunRestServiceWLS.createAltBilanz(testCrefo.getPseudoCrefoNr(), strBilanz);
            String strInfo = String.format("\n\tBilanz-XML '%s' wurde erfolgreich hochgeladen", TesunUtilites.shortPath(xmlFile, 50));
            addTestResultAndNotify(null, strInfo, command);
        } catch (Exception ex) {
            String strInfo = String.format("\n\tHochladen der Bilanz-XML für die Crefo '%d' gescheitert!", testCrefo.getPseudoCrefoNr());
            addTestResultAndNotify(testScenario, strInfo, command);
        }
    }

    private void createEhProduktAuftragForCrefo(TestScenario testScenario, TestCrefo testCrefo, AB30XMLProperties.EH_PROD_AUFTR_TYPE ehProdAuftrType, String command) {
        if (ehProdAuftrType.isAuftragRecherche()) {
            createEhProduktAuftragRecherche(testScenario, testCrefo, ehProdAuftrType, command);
        } else if (ehProdAuftrType.isAuftragInitialBuyer()) {
            tesunRestServiceWLS.createEhInitialBuyer(testCrefo.getPseudoCrefoNr());
        } else {
            String strInfo = "Art des Upload-Auftrages unbekannt: " + ehProdAuftrType.name();
            addTestResultAndNotify(testScenario, strInfo, command);
        }
    }

    private void createEhProduktAuftragRecherche(TestScenario testScenario, TestCrefo testCrefo, AB30XMLProperties.EH_PROD_AUFTR_TYPE ehProdAuftrType, String command) {
        try {
            File xmlFile = new File(uploadPath, ehProdAuftrType.getXmlFileName());
            String strProdAuftr = FileUtils.readFileToString(xmlFile);
            strProdAuftr = strProdAuftr.replace("<!--FILE_TRANSFER_NUMMER-->0", testCrefo.getPseudoCrefoNr().toString().substring(0, 3) + "_" + testCrefo.getPseudoCrefoNr());
            final int indexOfCrNrStart1 = strProdAuftr.indexOf("<crefonummer>") + "<crefonummer>".length();
            final int indexOfCrNrEnd1 = strProdAuftr.indexOf("</crefonummer>", indexOfCrNrStart1);
            String strCrefoNr = strProdAuftr.substring(indexOfCrNrStart1, indexOfCrNrEnd1);
            strProdAuftr = strProdAuftr.replace(strCrefoNr, testCrefo.getPseudoCrefoNr().toString());

            final int indexOfCrNrStart2 = strProdAuftr.indexOf("<crefonummer>", indexOfCrNrStart1) + "<crefonummer>".length();
            final int indexOfCrNrEnd2 = strProdAuftr.indexOf("</crefonummer>", indexOfCrNrStart2);
            String strCrefoNr2 = strProdAuftr.substring(indexOfCrNrStart2, indexOfCrNrEnd2);
            strProdAuftr = strProdAuftr.replace(strCrefoNr2, testCrefo.getPseudoCrefoNr().toString());
            tesunRestServiceWLS.createEhProduktAuftrag(testCrefo.getPseudoCrefoNr(), strProdAuftr);
            String strInfo = String.format("\n\tEH-Produktauftrag-XML '%s' für TestCrefo %s:%d wird hochgeladen...", TesunUtilites.shortPath(xmlFile, 50), testCrefo.getTestFallName(), testCrefo.getPseudoCrefoNr());
            addTestResultAndNotify(null, strInfo, command);
        } catch (Exception ex) {
            String strInfo = String.format("\n\tHochladen der EH-Produktauftrag-XML für die Crefo '%s' gescheitert!\n\tException: %s", testCrefo, ex.getMessage());
            addTestResultAndNotify(testScenario, strInfo, command);
        }
    }

    private void addTestResultAndNotify(TestScenario testScenario, String strInfo, String command) {
        if(testScenario != null) {
            TestResults.ResultInfo resultInfo = new TestResults.ResultInfo(strInfo);
            testScenario.addResultInfo(command, resultInfo);
        }
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(Level.INFO, strInfo);
        }
    }
    private boolean checkCrefoClz(TestScenario testScenario, TestCrefo testCrefo, String command) {
        try {
            String strCrefo = testCrefo.getPseudoCrefoNr().toString();
            int clz = Integer.valueOf(strCrefo.substring(0, 3));
            for (int i = 0; i < TestSupportClientKonstanten.TEST_CLZ_LIST.length; i++) {
                if (clz == TestSupportClientKonstanten.TEST_CLZ_LIST[i]) {
                    return true;
                }
            }
            return false;
        } catch (NumberFormatException ex) {
            String strInfo = String.format("\n\t\tCLZ-Check der Testcrefo %s gescheitert!\n\tException: %s", testCrefo, ex.getMessage());
            addTestResultAndNotify(testScenario, strInfo, command);
            return false;
        }
    }

    private class UploadInfo {
        private final Long crefo;
        private Boolean isFirma;

        private UploadInfo(Long crefo, File xmlFile) {
            this.crefo = crefo;
            setFirma(checkXmloContent(xmlFile));
        }

        private Boolean checkXmloContent(File xmlFile) {
            boolean isFirma = false;
            try {
                String xmlContent = FileUtils.readFileToString(xmlFile).replaceAll("arc:", "");
                int indexOf1 = xmlContent.indexOf("firma>") + "firma>".length();
                int indexOf2 = xmlContent.indexOf("firma>", indexOf1);
                String strTrueFalse = xmlContent.substring(indexOf1, indexOf2-2);
                isFirma = strTrueFalse.equals("true");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return isFirma;
        }

        public Long getCrefo() {
            return crefo;
        }

        public void setFirma(Boolean firma) {
            isFirma = firma;
        }

        public Boolean isFirma() {
            return isFirma;
        }
    }
}
