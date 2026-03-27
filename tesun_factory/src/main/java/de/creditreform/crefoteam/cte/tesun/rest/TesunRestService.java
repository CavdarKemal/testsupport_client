package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.jobadapter.jvminfo.xmlbinding.ServicesList;
import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzDecisionMonitoring;
import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.RestInvokerApache4;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.rest.RestInvokerResponse;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.clzinfo.TesunClzInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigUploadInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.dsgvoinfo.TesunDsgvoStatusCrefoErgebnisse;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.erneutelieferung.CteErneuteLieferung;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.jobexecution.TesunJobexecutionInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.pendingjobs.TesunPendingJobs;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.produktauftrag.TesunProduktAuftrag;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.produktauftrag.TesunProduktAuftragQuerverweis;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.systeminfo.TesunSystemInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingexport.TesunExportTrackingErgebnis;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.trackingimport.TesunImportTrackingErgebnis;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.FWUpdatesListPerCustomer;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.FachwertAktualisierungInfoByID;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.FachwertAktualisierungList;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.FachwertBenannteGruppe;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.FachwertBenannteGruppenListe;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.KundenKonfig;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.KundenKonfigList;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.statistikentscheidungstraegerberechnung.EntscheidungstraegerInfosAlleCrefos;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte_cta.statistik.xmlbinding.CtaStatistik;
import de.creditreform.crefoteam.cte_cta.statistik.xmlbinding.ObjectFactory;
import de.creditreform.crefoteam.cteinsoexporttesun.insoxmlbinding.TesunInsoAktuellerStand;
import de.creditreform.cte.inso.monitor.xmlbinding.XmlKunde;
import de.creditreform.cte.inso.monitor.xmlbinding.XmlKundeList;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Kapselt die REST-Aufrufe gegen CTE-Rest-Services für tesun
 * Created by CavdarK on 04.07.2016.
 * http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/application.wadl
 */
public class TesunRestService {
    private final static Logger LOGGER = Logger.getLogger(TesunRestService.class);
    private static final int REST_INVOKER_TIMEOUT = 5 * 60 * 1000;
    private static final Map<String, JAXBContext> jaxbContextMap = new TreeMap<>();

    private final RestInvoker restServiceInvoker;

    private final RestInvokerConfig restInvokerConfig;
    private final TesunClientJobListener tesunClientJobListener;

    public TesunRestService(RestInvokerConfig restInvokerConfig, TesunClientJobListener tesunClientJobListener) {
        this.restInvokerConfig = restInvokerConfig;
        try {
            if (jaxbContextMap.isEmpty()) {
                jaxbContextMap.put("TesunConfigInfo", JAXBContext.newInstance(TesunConfigInfo.class.getPackage().getName()));
                jaxbContextMap.put("TesunJobexecutionInfo", JAXBContext.newInstance(TesunJobexecutionInfo.class.getPackage().getName()));
                jaxbContextMap.put("TesunSystemInfo", JAXBContext.newInstance(TesunSystemInfo.class.getPackage().getName()));
                jaxbContextMap.put("CteErneuteLieferung", JAXBContext.newInstance(CteErneuteLieferung.class.getPackage().getName()));
                jaxbContextMap.put("CteEnvironmentProperties", JAXBContext.newInstance(CteEnvironmentProperties.class.getPackage().getName()));
                jaxbContextMap.put("CteClzInfo", JAXBContext.newInstance(TesunClzInfo.class.getPackage().getName()));
                jaxbContextMap.put("TesunImportTrackingErgebnis", JAXBContext.newInstance(TesunImportTrackingErgebnis.class.getPackage().getName()));
                jaxbContextMap.put("TesunExportTrackingErgebnis", JAXBContext.newInstance(TesunExportTrackingErgebnis.class.getPackage().getName()));
                jaxbContextMap.put("RelevanzDecisionMonitoring", JAXBContext.newInstance(RelevanzDecisionMonitoring.class.getPackage().getName()));
                jaxbContextMap.put("TesunPendingJobs", JAXBContext.newInstance(TesunPendingJobs.class.getPackage().getName()));
                jaxbContextMap.put("ServicesNameUrl", JAXBContext.newInstance(ServicesList.class.getPackage().getName()));
                jaxbContextMap.put("EntscheidungstraegerInfosAlleCrefos", JAXBContext.newInstance(EntscheidungstraegerInfosAlleCrefos.class.getPackage().getName()));
                jaxbContextMap.put("TesunProduktAuftrag", JAXBContext.newInstance(TesunProduktAuftrag.class.getPackage().getName()));
                jaxbContextMap.put("TesunProduktAuftragQuerverweis", JAXBContext.newInstance(TesunProduktAuftragQuerverweis.class.getPackage().getName()));
                jaxbContextMap.put("KundenKonfigList", JAXBContext.newInstance(KundenKonfigList.class.getPackage().getName()));
                jaxbContextMap.put("FachwertAktualisierungList", JAXBContext.newInstance(FachwertAktualisierungList.class.getPackage().getName()));
                jaxbContextMap.put("FachwertAktualisierungInfoByID", JAXBContext.newInstance(FachwertAktualisierungInfoByID.class.getPackage().getName()));
                jaxbContextMap.put("FWUpdatesListPerCustomer", JAXBContext.newInstance(FWUpdatesListPerCustomer.class.getPackage().getName()));
                jaxbContextMap.put("FachwertBenannteGruppenListe", JAXBContext.newInstance(FachwertBenannteGruppenListe.class.getPackage().getName()));
                jaxbContextMap.put("FachwertBenannteGruppe", JAXBContext.newInstance(FachwertBenannteGruppe.class.getPackage().getName()));
                jaxbContextMap.put("CtaStatistik", JAXBContext.newInstance(CtaStatistik.class.getPackage().getName()));
                jaxbContextMap.put("TesunDsgvoStatusCrefoErgebnisse", JAXBContext.newInstance(TesunDsgvoStatusCrefoErgebnisse.class.getPackage().getName()));
                jaxbContextMap.put("TesunInsoAktuellerStand", JAXBContext.newInstance(TesunInsoAktuellerStand.class.getPackage().getName()));
                jaxbContextMap.put("TesunInsoKunde", JAXBContext.newInstance(XmlKundeList.class.getPackage().getName()));
            }
            restServiceInvoker = new RestInvokerApache4(this.restInvokerConfig.getServiceURL(), this.restInvokerConfig.getServiceUser(), this.restInvokerConfig.getServicePassword());
            this.tesunClientJobListener = tesunClientJobListener;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public RestInvokerConfig getRestInvokerConfig() {
        return restInvokerConfig;
    }

    public TesunDsgvoStatusCrefoErgebnisse setDsgVoSperre(Long crefoNr, Boolean sperrStatus) {
        // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/dsgvo/update?crefo=4120704598&sperren=true
        final String SERVICE_URL = "/cte_tesun_service/dsgvo/update";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam(TestSupportClientKonstanten.HEADER_PARAM_IKAROS_CREFO, crefoNr.toString());
            restServiceInvoker.queryParam(TestSupportClientKonstanten.QUERY_PARAM_DSGVO_SPERREN, sperrStatus);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#setDsgVoSperre:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokePut("").expectStatusOK();
            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunDsgvoStatusCrefoErgebnisse").createUnmarshaller();
            TesunDsgvoStatusCrefoErgebnisse dsgvoStatusCrefoErgebnisse = (TesunDsgvoStatusCrefoErgebnisse) unmarshaller.unmarshal(new StringReader(responseBody));
            return dsgvoStatusCrefoErgebnisse;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("setDsgVoSperre()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der JVM-Inso auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigJvmInso() initialisiert werden!
     */
    public TesunInsoAktuellerStand readInsoProductForCrefo(Long crefoNr) {
        // http://rhsctem016.ecofis.de:7079/backend/pre-product/by-crefo/1234
        // WADL: http://rhsctem016.ecofis.de:7079/application.wadl
        final String SERVICE_URL = "/backend/pre-product/by-crefo/";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL + crefoNr);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#readreadInsoProductForCrefo:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunInsoAktuellerStand").createUnmarshaller();
            JAXBElement<TesunInsoAktuellerStand> tesunInsoAktuellerStandJaxbE = (JAXBElement<TesunInsoAktuellerStand>) unmarshaller.unmarshal(new StringReader(responseBody));
            return tesunInsoAktuellerStandJaxbE.getValue();
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("readreadInsoProductForCrefo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der JVM-Inso-Backend auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigJvmInsoBackend() initialisiert werden!
     */
    public XmlKunde readInsoKunde(String kundenKuerzel) {
        // http://rhsctem016.ecofis.de:7080/kunden/?q=Test-Tool
        final String SERVICE_URL = "/kunden/";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("q", kundenKuerzel);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#readInsoKunde:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunInsoKunde").createUnmarshaller();
            XmlKundeList xmlKundeList = (XmlKundeList) unmarshaller.unmarshal(new StringReader(responseBody));
            List<XmlKunde> kundeList = xmlKundeList.getKundeList();
            return (kundeList.size() == 1) ? kundeList.get(0) : null;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("readInsoKunde()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public EntscheidungstraegerInfosAlleCrefos readEntscheidugsTraeger(Long... crefos) {
        // http://http://rhsctem015.ecofis.de:7077/tesun/entg-list/?crefo=4110005256,4110006251
        final String SERVICE_URL = "/cte_tesun_service/tesun/entg-list";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            for (Long crefo : crefos) {
                restServiceInvoker.queryParam("crefo", crefo);
            }
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#readEntscheidugsTraeger:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("EntscheidungstraegerInfosAlleCrefos").createUnmarshaller();
            EntscheidungstraegerInfosAlleCrefos entscheidungstraegerInfosAlleCrefos = (EntscheidungstraegerInfosAlleCrefos) unmarshaller.unmarshal(new StringReader(responseBody));
            return entscheidungstraegerInfosAlleCrefos;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("readEntgsFor()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public String createAltBilanz(Long crefoNr, String strBilanz) {
        // FIXME: URL aus dem Tesun-Bereich nach Erweiterung von cte_rest
        final String SERVICE_URL = "/cte_betrieb_service/altbilanzen/";
        URI uri = restServiceInvoker.buildURI();
        String laufendeNummer = "0";
        String datumEinspielungCTE = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.format(new Date());
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.temporaryPath(laufendeNummer + "/" + datumEinspielungCTE);
            restServiceInvoker.queryParam(TestSupportClientKonstanten.HEADER_PARAM_IKAROS_CREFO, crefoNr.toString());
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#createAltBilanz:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokePost(strBilanz, RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            String responseBody = response.getResponseBody();
            return responseBody;
        } catch (Exception ex) {
            return ex.getMessage();
        } finally {
            restServiceInvoker.close();
        }
    }

    public TesunProduktAuftrag getEhProduktAuftragInfos(Date datumVom, Date datumBis) {
        final String SERVICE_URL = "/cte_tesun_service/tesun/produktauftrag/list/bydaterange";
        String strDatumVom = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD.format(datumVom);
        String strDatumBis = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD.format(datumBis);
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("datumVom", strDatumVom);
            restServiceInvoker.queryParam("datumBis", strDatumBis);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService:#getEhProduktAuftragInfos:: %s", uri));
            RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = restInvokerResponse.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunProduktAuftrag").createUnmarshaller();
            TesunProduktAuftrag tesunProduktAuftrag = (TesunProduktAuftrag) unmarshaller.unmarshal(new StringReader(responseBody));
            return tesunProduktAuftrag;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getEhProduktAuftragInfos()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public TesunProduktAuftragQuerverweis getEhProduktAuftragQuerverweis(Long crefo) {
        final String SERVICE_URL = "/cte_tesun_service/tesun/produktauftrag/list/bycrefo";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("crefo", crefo.toString());
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService:getEhProduktAuftragQuerverweis:: %s", uri));
            RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = restInvokerResponse.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunProduktAuftragQuerverweis").createUnmarshaller();
            TesunProduktAuftragQuerverweis produktAuftragQuerverweis = (TesunProduktAuftragQuerverweis) unmarshaller.unmarshal(new StringReader(responseBody));
            return produktAuftragQuerverweis;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getEhProduktAuftragQuerverweis()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public String createEhInitialBuyer(Long crefoNr) {
        // FIXME: URL aus dem Tesun-Bereich nach Erweiterung von cte_rest
        final String SERVICE_URL = "/cte_betrieb_service/initialbuyer/by_crefo/";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL + crefoNr);
            restServiceInvoker.queryParam("kundenKennung", "EH");
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#createEhInitialBuyer:: %s", uri));
            RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost("", RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            final String responseBody = restInvokerResponse.getResponseBody();
            return responseBody;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("createEhInitialBuyer()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public String createEhProduktAuftrag(Long crefoNr, String strProdAuftr) {
        final String SERVICE_URL = "/cte_tesun_service/tesun/produktauftrag/";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL + crefoNr);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#createEhProduktAuftrag:: %s", uri));
            RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost(strProdAuftr, RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            final String responseBody = restInvokerResponse.getResponseBody();
            return responseBody;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("createEhProduktAuftrag()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public String createCtaStatistikForCrefo(CtaStatistik ctaStatistik) {
        final String SERVICE_URL = "/cte_tesun_service/tesun/ctastatistik/";
        URI uri = restServiceInvoker.buildURI();
        try {
            JAXBElement<CtaStatistik> jaxbElement = new ObjectFactory().createCtaStatistik(ctaStatistik);
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = jaxbContextMap.get("CtaStatistik").createMarshaller();
            marshaller.marshal(jaxbElement, stringWriter);

            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL + ctaStatistik.getCrefonummer());
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#createCtaStatistikForCrefo:: %s", uri));
            RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePut(stringWriter.toString(), RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            return restInvokerResponse.getResponseBody();
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("createCtaStatistikForCrefo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der JVM-ImportCycle auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigJvmImpCycle() initialisiert werden!
     *
     * @param testCustomerMap
     */
    public String pruefeKundenInstallation(Map<String, TestCustomer> testCustomerMap) {
        String SERVICE_URL = "/tesun_backend/pruefekundeninstallation/";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            for (String customerKey : testCustomerMap.keySet()) {
                if (customerKey.startsWith("INSO")) {
                    customerKey = "INSO";
                } else if (customerKey.startsWith("SDF")) {
                    customerKey = "SDF";
                }
                restServiceInvoker.queryParam("kuerzel", customerKey);
            }
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#pruefeKundenInstallation:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            if (responseBody.startsWith("Liste der angefragten Kunden-K")) {
                return "";
            }
            return responseBody;
        } catch (Exception ex) {
            return ex.getMessage();
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der JVM-ImportCycle auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigJvmImpCycle() initialisiert werden!
     */
    public String orderCrefo(Long crefo, String testfallName) {
        Long clz = extractCLZFromInput(testfallName);
        return orderCrefo(crefo, clz);
    }

    /**
     * Ruft REST-Service der JVM-ImportCycle auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigJvmImpCycle() initialisiert werden!
     * http://rhsctem015.ecofis.de:7051/tesun_backend/ikaros/beauftragungmanuell/TESUN-9020000001-215?empfaenger=215&crefo=9020000001
     */
    public String orderCrefo(Long crefo, Long clz) {
        final String SERVICE_URL = "/tesun_backend/ikaros/beauftragungmanuell/";
        final String AUFTRAGS_KENNUNG = "TESUN-%d-%d";
        String strAuftrKennung = String.format(AUFTRAGS_KENNUNG, crefo, clz);
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL + strAuftrKennung);
            restServiceInvoker.queryParam(TestSupportClientKonstanten.HEADER_PARAM_IKAROS_RCV_CLZ, clz.toString());
            restServiceInvoker.queryParam(TestSupportClientKonstanten.HEADER_PARAM_IKAROS_CREFO, crefo.toString());
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#orderCrefo:: %s", uri));
            restServiceInvoker.invokePut(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            return strAuftrKennung;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("orderCrefo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der JVM-ImportCycle auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigJvmImpCycle() initialisiert werden!
     * Aufruf-Beispiel in
     * de.creditreform.crefoteam.cte.tesun.rest.TesunRestServiceIntegrationTest#test_erneuteLieferungBeantragen()
     */
    public String erneuteLieferungBeantragen(List<Long> crefosList, String selectedProject) {
        final String SERVICE_URL = "/tesun_backend/erneutelieferung/" + selectedProject;
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            CteErneuteLieferung cteErneuteLieferung = new CteErneuteLieferung();
            List<Long> usedCrefos = new ArrayList<>();
            for (Long crefo : crefosList) {
                if (!usedCrefos.contains(crefo)) {
                    cteErneuteLieferung.getCrefos().add(crefo.toString());
                    usedCrefos.add(crefo); // damit keine doppelten Crefos benutzt werden!
                }
            }
            Marshaller marshaller = jaxbContextMap.get("CteErneuteLieferung").createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(cteErneuteLieferung, stringWriter);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#erneuteLieferungBeantragen:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokePut(stringWriter.toString(), RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            String responseBody = response.getResponseBody();
            return responseBody;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("erneuteLieferungBeantragen()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der JVM-ImportCycle auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigJvmImpCycle() initialisiert werden!
     */
    public RelevanzDecisionMonitoring getCrefoAnaylseInfo(String customerKey, Long crefo) {
        // JVM-ImportCycle-Variante: http://rhsctem015.ecofis.de:7051/backend/relevanzdecision/monitoring/4110000553/EXPORT_CTE_TO_BVD
        String SERVICE_URL = "/backend/relevanzdecision/monitoring/";
        customerKey = adjustCustomerKey(customerKey);
        String selectedProject = "EXPORT_CTE_TO_" + customerKey.toUpperCase();
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            String url = SERVICE_URL + crefo + "/" + selectedProject;
            restServiceInvoker.appendPath(url);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getCrefoAnaylseInfo:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("RelevanzDecisionMonitoring").createUnmarshaller();
            RelevanzDecisionMonitoring relevanzDecisionMonitoring = (RelevanzDecisionMonitoring) unmarshaller.unmarshal(new StringReader(responseBody));
            return relevanzDecisionMonitoring;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getCrefoAnaylseInfo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    private static String adjustCustomerKey(String customerKey) {
        if (customerKey.equalsIgnoreCase("VSD") || customerKey.equalsIgnoreCase("VSO")) {
            customerKey = "VSH";
        }
        return customerKey;
    }

    /**
     * Ruft REST-Service der JVM-ImportCycle auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigJvmImpCycle() initialisiert werden!
     */
    public List<RelevanzDecisionMonitoring> getCrefoAnaylseInfos(String customerKey, List<Long> crefosList) {
        customerKey = adjustCustomerKey(customerKey);
        List<RelevanzDecisionMonitoring> relevanzDecisionMonitoringsList = new ArrayList<>();
        for (Long crefo : crefosList) {
            RelevanzDecisionMonitoring relevanzDecisionMonitoring = getCrefoAnaylseInfo(customerKey, crefo);
            relevanzDecisionMonitoringsList.add(relevanzDecisionMonitoring);
        }
        return relevanzDecisionMonitoringsList;
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public TesunClzInfo getClzInfo(Integer targetClz) {
        // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/clzinfo?clz=412
        final String SERVICE_URL = "/cte_tesun_service/tesun/clzinfo";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            // "clz": siehe: de.creditreform.crefoteam.cte.restservices.tesun.CteTesunKonstanten#TESUN_QRY_PARAM_CLZ
            restServiceInvoker.queryParam("clz", targetClz);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getClzInfo:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("CteClzInfo").createUnmarshaller();
            TesunClzInfo tesunClzInfo = (TesunClzInfo) unmarshaller.unmarshal(new StringReader(responseBody));
            return tesunClzInfo;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getClzInfo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

/*
  public Map<String,String> readJsonArrayFromServicesJsonFile(String envKey) {
    String jsonFile = "/ctebatch_config_generator/" + envKey + "/services.json";
    InputStream jsonInputStream = this.getClass().getResourceAsStream(jsonFile);
    if (jsonInputStream == null) {
      throw new IllegalArgumentException("Fehler: Die Ressource " + jsonFile + " fehlt.");
    }
    JsonArray jsonArray;
    try (JsonReader jsonRdr = Json.createReader(jsonInputStream)) {
      jsonArray = jsonRdr.readArray();
    }
    if (jsonArray == null || jsonArray.isEmpty()) {
      throw new IllegalArgumentException("Fehler: In der Ressource " + jsonFile + " kann das JSON-Array nicht gelesen werden.");
    }
    Map<String, String> jvmInstallationMap = new TreeMap<>();
    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
      String theKey = jsonObject.getString("name").replace("CTE-", "").toUpperCase(); // "name" -> ""CTE-BVD"
      String jvmUrl = jsonObject.getString("url"); // "url" -> ""http://rhsctew002.ecofis.de:7062""
      jvmInstallationMap.put(theKey, jvmUrl);
    }
    return jvmInstallationMap;
  }
*/

    public Map<String, String> getJvmInstallationMap() {
        ServicesList servicesList = getJvmInstallationServices();
        Map<String, String> jvmInstallationMap = new TreeMap<>();
        for (ServicesList.ServiceNameUrl serviceNameUrl : servicesList.getServiceNameUrls()) {
            String theKey = serviceNameUrl.getName().replace("CTE-", "").toUpperCase(); // "name" -> ""CTE-BVD"
            String jvmUrl = serviceNameUrl.getUrl(); // "url" -> ""http://rhsctew002.ecofis.de:7062""
            jvmInstallationMap.put(theKey, jvmUrl);
        }
        return jvmInstallationMap;
    }

    /**
     * Ruft REST-Service der JVM-CteBatchGUI auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigCteBatchGUI() initialisiert werden!
     * http://rhsctem015.ecofis.de:7071/services
     */
    public ServicesList getJvmInstallationServices() {
        final String SERVICE_URL = "/services";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getJvmInstallationServices:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("ServicesNameUrl").createUnmarshaller();
            return (ServicesList) unmarshaller.unmarshal(new StringReader(responseBody));
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getJvmInstallationServices()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    private void fillTesunConfigInfo(TesunConfigExportInfo tesunConfigExportInfo, TesunConfigUploadInfo tesunConfigUploadInfo, String key, String value) {
        if (key.startsWith("common")) {
            return;
        }
        if (key.endsWith("localSourceDirectory")) {
            tesunConfigExportInfo.setRelativePath(value.replace("${CTE_EXPORT_HOME}", ""));
        } else if (key.endsWith("remoteUserName")) {
            String relativePath = tesunConfigExportInfo.getRelativePath();
            relativePath = value + ":" + relativePath;
            tesunConfigExportInfo.setRelativePath(relativePath);
        } else if (key.endsWith("targetDirectory")) {
            tesunConfigUploadInfo.setCompletePath(value.replace(".", ""));
        } else if (key.endsWith("targetHost")) {
            String completePath = tesunConfigUploadInfo.getCompletePath();
            completePath = value + ":" + completePath;
            tesunConfigUploadInfo.setCompletePath(completePath);
        } else if (key.endsWith("targetPort")) {
            String completePath = tesunConfigUploadInfo.getCompletePath();
            String[] split = completePath.split(":");
            completePath = split[0] + ":" + value + split[1];
            tesunConfigUploadInfo.setCompletePath(completePath);
        } else {
        }
    }

    public TesunConfigInfo getTesunConfigInfo(EnvironmentConfig environmentConfig, Map<String, TestCustomer> testCustomerMap) {
        TesunConfigInfo tesunConfigInfo = new TesunConfigInfo();
        testCustomerMap.keySet().stream().forEach(customerKey -> {
            String keyFilter = buildKeyFilter(customerKey);
            CteEnvironmentProperties cteEnvironmentProperties = getEnvironmentProperties(keyFilter, "", true);
            List<CteEnvironmentPropertiesTupel> propertiesList = cteEnvironmentProperties.getProperties();
            List<TesunConfigExportInfo> exportPfadeList = tesunConfigInfo.getExportPfade();
            List<TesunConfigUploadInfo> uploadPfadeList = tesunConfigInfo.getUploadPfade();
            TesunConfigExportInfo tesunConfigExportInfo = new TesunConfigExportInfo();
            tesunConfigExportInfo.setKundenKuerzel(customerKey);
            TesunConfigUploadInfo tesunConfigUploadInfo = new TesunConfigUploadInfo();
            tesunConfigUploadInfo.setKundenKuerzel(customerKey);
            propertiesList.stream().forEach(cteEnvironmentPropertiesTupel -> {
                String value = cteEnvironmentPropertiesTupel.getValue();
                String key = cteEnvironmentPropertiesTupel.getKey();
                tesunConfigInfo.setUmgebungsKuerzel(environmentConfig.getCurrentEnvName());
                fillTesunConfigInfo(tesunConfigExportInfo, tesunConfigUploadInfo, key, value);
            });
            try {
                String relativePath = tesunConfigExportInfo.getRelativePath();
                String propertyVal = environmentConfig.getProperty("EXPORT_NFS_CREDENTIALS", false, "ctcb:Consumer00Horst@rhsctear01.ecofis.de:22");

                String[] splitRelPath = relativePath.split(":");
                String[] splitPropVal1 = propertyVal.split(":");
                relativePath = splitPropVal1[0] + ":" + splitPropVal1[1] + ":" + splitPropVal1[2] + splitRelPath[1];
                tesunConfigExportInfo.setRelativePath(relativePath);
            } catch (PropertiesException e) {
                throw new RuntimeException(e);
            }
            String completePath = tesunConfigUploadInfo.getCompletePath();
            tesunConfigUploadInfo.setCompletePath(completePath);
            exportPfadeList.add(tesunConfigExportInfo);
            uploadPfadeList.add(tesunConfigUploadInfo);
        });
        return tesunConfigInfo;
    }

    private static String buildKeyFilter(String customerKey) {
        if (customerKey.equalsIgnoreCase("bdr")) {
            return "bedirect.transfer.";
        }
        return customerKey.toLowerCase() + "export.transfer.";
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public void restoreEnvironmentProperties() {
        // POST http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/environment-backup-restore/restore
        final String SERVICE_URL = "cte_tesun_service/tesun/environment-backup-restore/restore";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#restoreEnvironmentProperties:: %s", uri));
            restServiceInvoker.invokePost().expectStatusOK();
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("restoreEnvironmentProperties()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public CteEnvironmentProperties getEnvironmentProperties(String keyFilter, String valueFilter, boolean skipClientProps) {
        // GET http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/environmentproperties?lfencoding=true
        CteEnvironmentProperties cteEnvironmentProperties = null;
        final String SERVICE_URL = "/cte_tesun_service/tesun/environmentproperties";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("lfencoding", Boolean.TRUE);
            if (keyFilter != null) {
                String s = skipClientProps ? " -client" : "";
                restServiceInvoker.queryParam("keyFilter", keyFilter + s);
            }
            if (valueFilter != null && !valueFilter.isEmpty()) {
                restServiceInvoker.queryParam("valueFilter", valueFilter);
            }
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getEnvironmentProperties:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("CteEnvironmentProperties").createUnmarshaller();
            cteEnvironmentProperties = (CteEnvironmentProperties) unmarshaller.unmarshal(new StringReader(responseBody));
            return cteEnvironmentProperties;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getEnvironmentProperties()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public String setEnvironmentProperties(CteEnvironmentProperties cteEnvironmentProperties) {
        // PUT http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/environmentproperties?lfencoding=true
        final String SERVICE_URL = "/cte_tesun_service/tesun/environmentproperties";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("lfencoding", Boolean.TRUE);
            Marshaller marshaller = jaxbContextMap.get("CteEnvironmentProperties").createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(cteEnvironmentProperties, stringWriter);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#setEnvironmentProperties:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokePut(stringWriter.toString(), RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            String responseBody = response.getResponseBody();
            return responseBody;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("setEnvironmentProperties()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public String updateDatumStaging(List<String> strCrefosList) {
        final String SERVICE_URL = "/cte_tesun_service/tesun/staging/trigger";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            for (String strCrefo : strCrefosList) {
                restServiceInvoker.queryParam("crefo", strCrefo);
            }
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#updateDatumStaging:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokePut(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            String responseBody = response.getResponseBody();
            return responseBody;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("updateDatumStaging()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public TesunConfigInfo getTesunConfigInfo() {
        // http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/configinfo
        final String SERVICE_URL = "/cte_tesun_service/tesun/configinfo";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getTesunConfigInfo:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunConfigInfo").createUnmarshaller();
            TesunConfigInfo tesunConfigInfo = (TesunConfigInfo) unmarshaller.unmarshal(new StringReader(responseBody));

            tesunConfigInfo.getExportPfade().sort((o1, o2) -> {
                if (o1.getKundenKuerzel() != null && o2.getKundenKuerzel() != null) {
                    return o1.getKundenKuerzel().toLowerCase(Locale.ROOT).compareTo(o2.getKundenKuerzel().toLowerCase(Locale.ROOT));
                }
                return 0;
            });
            tesunConfigInfo.getUploadPfade().sort((o1, o2) -> {
                if (o1.getKundenKuerzel() != null && o2.getKundenKuerzel() != null) {
                    return o1.getKundenKuerzel().toLowerCase(Locale.ROOT).compareTo(o2.getKundenKuerzel().toLowerCase(Locale.ROOT));
                }
                return 0;
            });
            checkTesunConfigInfo(tesunConfigInfo);
            return tesunConfigInfo;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getTesunConfigInfo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    private void checkTesunConfigInfo(TesunConfigInfo tesunConfigInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        tesunConfigInfo.getExportPfade().forEach(tesunConfigExportInfo -> {
            if (tesunConfigExportInfo.getRelativePath().isBlank()) {
                stringBuilder.append("Export-Path für den Kunden '").append(tesunConfigExportInfo.getKundenKuerzel()).append("' ist nicht gesetzt!");
            }
        });
        tesunConfigInfo.getUploadPfade().forEach(tesunConfigUploadInfo -> {
            if (tesunConfigUploadInfo.getCompletePath().isBlank()) {
                stringBuilder.append("Upload-Path für den Kunden '").append(tesunConfigUploadInfo.getKundenKuerzel()).append("' ist nicht gesetzt!");
            }
        });
        if (!stringBuilder.toString().isBlank()) {
            throw new RuntimeException(stringBuilder.toString());
        }
    }

    public FachwertBenannteGruppenListe getFachwertBenannteGruppe() {
        // http://http://rhsctem015.ecofis.de:7077/cte_betrieb_service/fachwert/benanntegruppen
        final String SERVICE_URL = "/cte_betrieb_service/fachwert/benanntegruppen";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getFachwertBenannteGruppe:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("FachwertBenannteGruppenListe").createUnmarshaller();
            FachwertBenannteGruppenListe fachwertBenannteGruppe = (FachwertBenannteGruppenListe) unmarshaller.unmarshal(new StringReader(responseBody));
            return fachwertBenannteGruppe;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getFachwertBenannteGruppe()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public FWUpdatesListPerCustomer getLastFachwertUpdatesForCustomer(String customerProzessName) {
        // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/fachwertconfig/lastUpdatesForCustomer?customer=EXPORT_CTE_TO_CTC
        final String SERVICE_URL = "/cte_tesun_service/tesun/fachwertconfig/lastUpdatesForCustomer";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("customer", customerProzessName);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getLastUpdatesForCustomer:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("FachwertAktualisierungList").createUnmarshaller();
            FWUpdatesListPerCustomer fwUpdatesListPerCustomer = (FWUpdatesListPerCustomer) unmarshaller.unmarshal(new StringReader(responseBody));
            return fwUpdatesListPerCustomer;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getLastUpdatesForCustomer()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public FachwertAktualisierungList getAllFachwertUpdatesForAllTypes() {
        // http://http://rhsctem015.ecofis.de:7077/cte_betrieb_service/fachwert/availableupdates
        final String SERVICE_URL = "/cte_betrieb_service/fachwert/availableupdates";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getAllFachwertUpdatesForAllTypes:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("FachwertBenannteGruppenListe").createUnmarshaller();
            FachwertAktualisierungList fachwertAktualisierungList = (FachwertAktualisierungList) unmarshaller.unmarshal(new StringReader(responseBody));
            return fachwertAktualisierungList;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getAllFachwertUpdatesForAllTypes()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public FachwertAktualisierungList getLastFachwertUpdatesForAllTypes() {
        // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/fachwertconfig/lastUpdates
        final String SERVICE_URL = "/cte_tesun_service/tesun/fachwertconfig/lastUpdates";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getLastFachwertUpdatesForAllTypes:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("FachwertAktualisierungList").createUnmarshaller();
            FachwertAktualisierungList fachwertAktualisierungList = (FachwertAktualisierungList) unmarshaller.unmarshal(new StringReader(responseBody));
            return fachwertAktualisierungList;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getLastFachwertUpdatesForAllTypes()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public FachwertAktualisierungList getAllFachwertUpdatesForType(String strategyName) {
        // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/fachwertconfig/allUpdates
        final String SERVICE_URL = "/cte_tesun_service/tesun/fachwertconfig/allUpdates";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("strategyName", strategyName);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getAllFachwertUpdatesForType:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("FachwertAktualisierungList").createUnmarshaller();
            FachwertAktualisierungList fachwertAktualisierungList = (FachwertAktualisierungList) unmarshaller.unmarshal(new StringReader(responseBody));
            return fachwertAktualisierungList;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getAllFachwertUpdatesForType()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public FachwertAktualisierungList getLastFachwertUpdatesForTypeTilDateIncl(String strategyName, String strDateBisIncl) {
        // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/fachwertconfig/lastUpdatesTil?strategyName=Anrede&dateBisIncl=2019-07-04
        final String SERVICE_URL = "/cte_tesun_service/tesun/fachwertconfig/lastUpdatesTil";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("strategyName", strategyName);
            restServiceInvoker.queryParam("dateBisIncl", strDateBisIncl);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getLastFachwertUpdatesForTypeTilDateIncl:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("FachwertAktualisierungList").createUnmarshaller();
            FachwertAktualisierungList fachwertAktualisierungList = (FachwertAktualisierungList) unmarshaller.unmarshal(new StringReader(responseBody));
            return fachwertAktualisierungList;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getLastUpdatesForFWTypeEnumTilDateIncl()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public FachwertAktualisierungInfoByID getLastFachwertUpdateForTypeTilDateIncl(String strategyName, String strDateBisIncl) {
        // http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/fachwertconfig/lastUpdateTil?strategyName=Anrede&dateBisIncl=2019-07-04
        final String SERVICE_URL = "/cte_tesun_service/tesun/fachwertconfig/lastUpdateTil";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("strategyName", strategyName);
            restServiceInvoker.queryParam("dateBisIncl", strDateBisIncl);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getLastFachwertUpdateForTypeTilDateIncl:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            response = response.expectStatusOK();
            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("FachwertAktualisierungInfoByID").createUnmarshaller();
            FachwertAktualisierungInfoByID aktualisierungInfoByID = (FachwertAktualisierungInfoByID) unmarshaller.unmarshal(new StringReader(responseBody));
            return aktualisierungInfoByID;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getLastUpdateForFWTypeEnumTilDateIncl()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    public KundenKonfigList getAllCustomerConfigs() {
        // http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/fachwertconfig/customerCfgs
        final String SERVICE_URL;
        SERVICE_URL = "/cte_tesun_service/tesun/fachwertconfig/customerCfgs";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getAllCustomerConfigs:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("KundenKonfigList").createUnmarshaller();
            KundenKonfigList kundenKonfigList = (KundenKonfigList) unmarshaller.unmarshal(new StringReader(responseBody));
            return kundenKonfigList;
        } catch (Exception ex) {
            LOGGER.error(extendExceptionMessage("getAllCustomerConfigs()::" + uri, ex));
            return null;
        } finally {
            restServiceInvoker.close();
        }
    }

    public SystemInfo getSystemPropertiesInfo() {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setKundenKonfigList(getAllCustomerConfigs());
        systemInfo.setTesunConfigInfo(getTesunConfigInfo());
        systemInfo.setEnvPropsList(getEnvironmentProperties(".vc|.exportFormat|extra_xml", "", true).getProperties());
        return systemInfo;
    }

    public void extendTestCustomerProperiesInfos(TestCustomer testCustomer, SystemInfo systemInfo) {
        TesunConfigExportInfo tesunConfigExportInfo = systemInfo.findTesunConfigExportInfoForCustomer(testCustomer);
        testCustomer.setExportUrl(tesunConfigExportInfo.getRelativePath());

        TesunConfigUploadInfo tesunConfigUploadInfo = systemInfo.findTesunConfigUploadInfoForCustomer(testCustomer);
        testCustomer.setUploadUrl(tesunConfigUploadInfo.getCompletePath());

        String propertyPrefix = testCustomer.getCustomerPropertyPrefix();
        testCustomer.getPropertyPairsList().forEach((MutablePair<String, String> propertyPair) -> {
            systemInfo.fillPropertyPairForCustomer(propertyPrefix, propertyPair);
        });

        KundenKonfig kundenKonfig = systemInfo.findFachwertconfigInfoForCustomer(testCustomer);
        testCustomer.setFwAktualisierungsdatum(TesunDateUtils.formatCalendar(kundenKonfig.getAktualisierungsdatum()));
        testCustomer.setPdVersion(kundenKonfig.getPdversion());
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public TesunImportTrackingErgebnis getImportTrackingInfo(List<String> crefosAsStringList) {
        final String SERVICE_URL = "/cte_tesun_service/tesun/trackingimport";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            for (String strCrefo : crefosAsStringList) {
                restServiceInvoker.queryParam("crefo", strCrefo);
            }
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getImportTrackingInfo:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunImportTrackingErgebnis").createUnmarshaller();
            TesunImportTrackingErgebnis importTrackingErgebnis = (TesunImportTrackingErgebnis) unmarshaller.unmarshal(new StringReader(responseBody));
            return importTrackingErgebnis;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getImportTrackingInfo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public TesunExportTrackingErgebnis getExportTrackingInfo(List<TestCrefo> testCrefoList, Date datumVom, Date datumBis) {
        final String SERVICE_URL = "/cte_tesun_service/tesun/trackingexport";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            for (TestCrefo testCrefo : testCrefoList) {
                restServiceInvoker.queryParam("crefo", testCrefo.getPseudoCrefoNr());
            }
            String strDateVom = "";
            if (datumVom != null) {
                strDateVom = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.format(datumVom);
                restServiceInvoker.queryParam("datumVom", strDateVom);
            }
            String strDateBis = "";
            if (datumBis != null) {
                strDateBis = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.format(datumBis);
                restServiceInvoker.queryParam("datumBis", strDateBis);
            }
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getExportTrackingInfo:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunExportTrackingErgebnis").createUnmarshaller();
            TesunExportTrackingErgebnis exportTrackingErgebnis = (TesunExportTrackingErgebnis) unmarshaller.unmarshal(new StringReader(responseBody));
            return exportTrackingErgebnis;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getExportTrackingInfo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public String downloadCrefo(long crefo) {
        final String SERVICE_URL = "/cte_tesun_service/tesun/xmlaccess/";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL + crefo);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#downloadCrefo:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            String responseBody = response.getResponseBody();
            return responseBody;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("downloadCrefo(" + crefo + ")::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     * Neuerdings wird nicht mehr "/cte_tesun_service/tesun/xmlaccess/", sondern "/cte_cta_restservices/" -REST-Servuce aufgerufen, damit die XML-Validierung an- und abschaltbar
     * über das Mastewrkonsole-Environment-Property "cte_cta_validation.mode" wird. Default-Wert ist "PRUEFE_NAMESPACE", alternative: "VALIDIERE_XSD_SCHEMA" oder "WELLFORMED"
     * im Modul cte_rest, Enum de.creditreform.crefoteam.cte.xmlvalidation.XmlValidationMode
     */
    public void uploadCrefo(long crefo, File xmlFile, String xsdVersion) {
        Calendar theCal = Calendar.getInstance();
        //        theCal.add(Calendar.MINUTE, -8);
        final String uploadDateParam = new SimpleDateFormat(TestSupportClientKonstanten.HEADER_PARAM_DATE_PATTERN).format(theCal.getTime());
        final String SERVICE_URL = "/cte_tesun_service/tesun/xmlaccess/";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL + crefo);
            restServiceInvoker.queryParam(TestSupportClientKonstanten.HEADER_PARAM_LAST_UPDATE_CT, uploadDateParam);
            restServiceInvoker.queryParam(TestSupportClientKonstanten.HEADER_PARAM_LAST_UPDATE_CTO, uploadDateParam);
            restServiceInvoker.queryParam("archivVersion", xsdVersion);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\tTesunRestService#uploadCrefo:: %s", uri));
            RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost(xmlFile, RestInvoker.CONTENT_TYPE_XML);
            restInvokerResponse.expectStatusOK();
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("uploadCrefo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public TesunJobexecutionInfo getTesunJobExecutionInfo(String processIdentifier) {
        if (processIdentifier.isBlank()) {
            throw new RuntimeException("getTesunJobExecutionInfo():: Prozessidentifier darf nicht leer sein!");
        }
        final String SERVICE_URL = "/cte_tesun_service/tesun/jobs/execution";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            restServiceInvoker.queryParam("infoKey", processIdentifier);
            uri = restServiceInvoker.buildURI();
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunJobexecutionInfo").createUnmarshaller();
            JAXBElement jAXBElement = (JAXBElement) unmarshaller.unmarshal(new StringReader(responseBody));
            TesunJobexecutionInfo tesunJobexecutionInfo = (TesunJobexecutionInfo) jAXBElement.getValue();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getTesunJobExecutionInfo:: %s --> LastCompletiotion-Time: %s",
                    uri, TesunDateUtils.formatCalendar(tesunJobexecutionInfo.getLastCompletitionDate())));
            return tesunJobexecutionInfo;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getTesunJobExecutionInfo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /*
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
       Aufruf-Beispiel
       REST-Client: GET http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/jobs/pending
       curl:        curl -X GET -i 'http://http://rhsctem015.ecofis.de:7077/cte_tesun_service/tesun/jobs/pending'
       Response
            <?xml version="1.0" encoding="UTF-8"?>
            <tesun-pending-jobs xmlns="http://crefoteam.creditreform.de/cte/tesun/pendingjobs/2018/08">
              <umgebungsKuerzel>ENE<u/mgebungsKuerzel>
              <jobs>
                <infokeyStart>micExportStartDate</infokeyStart>
                <prozessIdentifier>EXPORT_CTE_TO_MIC</prozessIdentifier>
                <infokeyTodoBlock>micExportToDoBlock</infokeyTodoBlock>
                <anzahlTodoBloecke>2199</anzahlTodoBloecke>
              </jobs>
              <jobs>
                <infokeyStart>ctimportStartDate</infokeyStart>
                <prozessIdentifier>FROM_STAGING_INTO_CTE</prozessIdentifier>
                <infokeyTodoBlock>ctimportToDoBlock</infokeyTodoBlock>
                <anzahlTodoBloecke>0</anzahlTodoBloecke>
              </jobs>
            </tesun-pending-jobs>
    */
    public TesunPendingJobs getTesunPendingJobs() {
        final String SERVICE_URL = "/cte_tesun_service/tesun/jobs/pending";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getTesunPendingJobs:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();
            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunPendingJobs").createUnmarshaller();
            TesunPendingJobs tesunPendingJobs = (TesunPendingJobs) unmarshaller.unmarshal(new StringReader(responseBody));
            return tesunPendingJobs;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getTesunPendingJobs()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    /**
     * Ruft REST-Service der WLS-Masterconsole auf, daher muss restServiceInvoker über environmentConfig.getRestServiceConfigWLS() initialisiert werden!
     */
    public TesunSystemInfo getTesunSystemInfo() {
        final String SERVICE_URL = "/cte_tesun_service/tesun/systeminfo";
        URI uri = restServiceInvoker.buildURI();
        try {
            restServiceInvoker.init(REST_INVOKER_TIMEOUT);
            restServiceInvoker.appendPath(SERVICE_URL);
            uri = restServiceInvoker.buildURI();
            notifyListeners(Level.INFO, String.format("\n\t-> TesunRestService#getTesunSystemInfo:: %s", uri));
            RestInvokerResponse response = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_XML).expectStatusOK();

            String responseBody = response.getResponseBody();
            Unmarshaller unmarshaller = jaxbContextMap.get("TesunSystemInfo").createUnmarshaller();
            Object document = unmarshaller.unmarshal(new StringReader(responseBody));
            final TesunSystemInfo tesunSystemInfo;
            if (document instanceof JAXBElement) {
                tesunSystemInfo = (TesunSystemInfo) ((JAXBElement) document).getValue();
            } else {
                tesunSystemInfo = (TesunSystemInfo) document;
            }
            return tesunSystemInfo;
        } catch (Exception ex) {
            throw new RuntimeException(extendExceptionMessage("getTesunSystemInfo()::" + uri, ex));
        } finally {
            restServiceInvoker.close();
        }
    }

    private Long extractCLZFromInput(String testFallName) {
        String[] split = testFallName.split("ika.");
        String strInfo = "\tEmpfänger-CLZ konnte aus dem Testfall-Namen nicht erkannt werden: " + testFallName;
        if (split.length < 2) {
            throw new RuntimeException(strInfo);
        }
        try {
            Long lCLZ = Long.valueOf(split[1]);
            return lCLZ;
        } catch (Exception ex) {
            throw new RuntimeException(strInfo);
        }
    }

    public void createIkarosAuftrag(TestCustomer testCustomer) {
        List<TestScenario> testScenariosList = testCustomer.getTestScenariosList();
        for (TestScenario testScenario : testScenariosList) {
            List<TestCrefo> testCrefosList = testScenario.getTestCrefosAsList();
            for (TestCrefo testCrefo : testCrefosList) {
                if (testCrefo.getTestFallInfo().contains("_ika.")) {
                    String strAuftrKennung = orderCrefo(testCrefo.getItsqTestCrefoNr(), testCrefo.getTestFallInfo());
                    notifyListeners(Level.INFO, String.format("\n\tAuftrag '%s' erfolgreich erteilt", strAuftrKennung));
                }
            }
        }
    }

    private String extendExceptionMessage(String strMethodName, Exception exception) {
        String strErr = String.format("\nTesunRestService::%s: Fehler beim REST-Service-Aufruf!", strMethodName);
        if (exception != null && exception.getMessage() != null) {
            String message = exception.getMessage();
            strErr += ":: -> " + message;
        }
        return strErr;
    }

    private void notifyListeners(Level level, String strInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, strInfo);
        } else {
            System.out.println(strInfo);
        }
    }

}
