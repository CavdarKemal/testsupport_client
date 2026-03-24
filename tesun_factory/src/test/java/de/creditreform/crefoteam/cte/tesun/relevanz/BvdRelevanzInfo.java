package de.creditreform.crefoteam.cte.tesun.relevanz;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzDecisionMonitoring;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;

import java.net.URL;
import java.util.List;

public class BvdRelevanzInfo extends AbstractRelevanzInfo {

   public BvdRelevanzInfo(String env, URL resourceURL, TesunClientJobListener tesunClientJobListener) {
      super(env, "BVD", "Crefo;Relevanz;Grund Nicht relevant;Produkt;Rechtsform;AnzAuskunft;DatLetztRech;LetzteArchivÄnd\n", resourceURL, tesunClientJobListener);
   }

   @Override
   public StringBuilder dumpRelevanzDecisionMonitoring(RelevanzDecisionMonitoring relevanzDecisionMonitoring) {
   /*
      0 = "Archivbestand zur Crefo 2010002921 gefunden"
      1 = "Info: Maximales Alter für relevante Bilanzen ist 13 Jahre"
      2 = "nicht relevant: Markus: keine Firma"
      3 = "nicht relevant: Bilanzen: keine Firma"
      4 = "nicht relevant: INSO: keine Firma"
      5 = "Info: Markus: Datum der letzten Recherche: 2004-01-16 00:00:00"
      6 = "Info: Markus: Erfassungs-Datum der Crefo: 1983-09-26 00:00:00"
      7 = "Info: Markus: Letzte Archiv-Änderung zur Crefo: 2021-11-08 00:09:16"
      8 = "Info: Markus: Anzahl Auskünfte: 6"
      9 = "Info: Zur Crefo 2010002921 liegt keine BilanzStatistik vor"
      10 = "Info: Bilanzen: Relevanz-Extension Bilanzen fehlt"
      11 = "Info: INSO: Relevanz-Extension NegativMerkmale fehlt"
      12 = "nicht relevant: Markus: Produkt-Art kann nicht zugeordnet werden, Crefo wird nicht geliefert"
      13 = "Info: Markus: Crefo ist aktuell bis 2022-02-16 12:09:19"
      14 = "Info: Bilanzen, Zusammenfasssung: insgesamt keine Relevanz der Crefo in diesem Bereich"
      15 = "Info: Bilanzen, Zusammenfassung: Crefo ist aktuell bis 2022-02-16 12:09:19"
      16 = "Info: INSO, Zusammenfasssung: insgesamt keine Relevanz der Crefo in diesem Bereich"
      17 = "Info: INSO, Zusammenfassung: Aktualität der Crefo zeitlich nicht begrenzt"
   */

      StringBuilder sbDumpInfo = new StringBuilder();
      StringBuilder stringBuilderNRelevant = new StringBuilder();
      String strProduktArt = "";
      String strDatLetztRech = "";
      String strAnzAuskunft = "";
      String strEnumRechtsform = "";
      String strLetzteArchivAend = "";
      List<String> decisionLogList = relevanzDecisionMonitoring.getDecisionLog();
      for (String decisionLog : decisionLogList) {
         if (decisionLog.startsWith("nicht relevant: ")) {
            stringBuilderNRelevant.append(decisionLog.replace("nicht relevant: ", "")).append(", ");
         } else if (decisionLog.startsWith("Info: Markus: Produkt-Art ist: ")) {
            strProduktArt = decisionLog.replace("Info: Markus: Produkt-Art ist: ", "");
         } else if (decisionLog.startsWith("Info: Markus: Datum der letzten Recherche: ")) {
            strDatLetztRech = decisionLog.replace("Info: Markus: Datum der letzten Recherche: ", "");
         } else if (decisionLog.startsWith("Info: Markus: Anzahl Auskünfte: ")) {
            strAnzAuskunft = decisionLog.replace("Info: Markus: Anzahl Auskünfte: ", "");
         } else if (decisionLog.startsWith("Info: Markus: Rechtsform-Enum: ")) {
            strEnumRechtsform = decisionLog.replace("Info: Markus: Rechtsform-Enum: ", "");
         } else if (decisionLog.startsWith("Info: Markus: Letzte Archiv-Änderung zur Crefo: ")) {
            strLetzteArchivAend = decisionLog.replace("Info: Markus: Letzte Archiv-Änderung zur Crefo: ", "");
         }
      }
      sbDumpInfo.append(relevanzDecisionMonitoring.getCrefo()).append(";");
      sbDumpInfo.append(dumMonitoringErg(relevanzDecisionMonitoring.getMonitoringErgebnisse())).append(";");
      sbDumpInfo.append(stringBuilderNRelevant).append(";");
      sbDumpInfo.append(strProduktArt).append(";");
      sbDumpInfo.append(strEnumRechtsform).append(";");
      sbDumpInfo.append(strAnzAuskunft).append(";");
      sbDumpInfo.append(strDatLetztRech).append(";");
      sbDumpInfo.append(strLetzteArchivAend).append(";");
      sbDumpInfo.append("\n");
      return sbDumpInfo;
   }

}
