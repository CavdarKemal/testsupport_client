package de.creditreform.crefoteam.cte.tesun.relevanz;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.RelevanzDecisionMonitoring;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;

import java.net.URL;
import java.util.List;

public class CtcRelevanzInfo extends AbstractRelevanzInfo {

   public CtcRelevanzInfo(String env, URL resourceURL, TesunClientJobListener tesunClientJobListener) {
      super(env, "CTC", "Crefo;Relevanz;Grund Nicht relevant;Privatperson;SperreAuskunft;AnzRelevNMs\n", resourceURL, tesunClientJobListener);
   }

   @Override
   public StringBuilder dumpRelevanzDecisionMonitoring(RelevanzDecisionMonitoring relevanzDecisionMonitoring) {
      /*
      0 = "Archivbestand zur Crefo 2010002921 gefunden"
      1 = "Info: Privatperson: true"
      2 = "Info: Sperre Auskunft: false"
      3 = "nicht relevant: Datum letzte Auskunft 2003-09-03 00:00:00"
      4 = "nicht relevant: Datum letzte Recherche 2004-01-16 00:00:00"
      5 = "nicht relevant: Datum letzte Telefon-Auskunft nicht gesetzt"
      6 = "nicht relevant: CTC-relevante Auskunft gefunden: "
      7 = "Info: Privatperson hat gültigen Wohnsitz im Inland"
      8 = "Info: Anzahl der relevanten Megativ-Merkmale: 0"
      9 = "nicht relevant: Person insgesamt CTC-relevant: "
      */

      String strPrivatperson = "";
      String strSperreAuskunft = "";
      String strAnzRelevNMs = "";
      StringBuilder sbDumpInfo = new StringBuilder();
      StringBuilder stringBuilderNRelevant = new StringBuilder();
      List<String> decisionLogList = relevanzDecisionMonitoring.getDecisionLog();
      for (String decisionLog : decisionLogList) {
         if (decisionLog.startsWith("nicht relevant: ")) {
            stringBuilderNRelevant.append(decisionLog.replace("nicht relevant: ", "")).append(", ");
         } else if (decisionLog.startsWith("Info: Sperre Auskunft: ")) {
            strSperreAuskunft = decisionLog.replace("Info: Sperre Auskunft: ", "");
         } else if (decisionLog.startsWith("Info: Privatperson: ")) {
            strPrivatperson = decisionLog.replace("Info: Privatperson: ", "");
         } else if (decisionLog.startsWith("Info: Anzahl der relevanten Megativ-Merkmale:")) {
            strAnzRelevNMs = decisionLog.replace("Info: Anzahl der relevanten Megativ-Merkmale: ", "");
         }
      }
      sbDumpInfo.append(relevanzDecisionMonitoring.getCrefo()).append(";");
      sbDumpInfo.append(dumMonitoringErg(relevanzDecisionMonitoring.getMonitoringErgebnisse())).append(";");
      sbDumpInfo.append(stringBuilderNRelevant).append(";");
      sbDumpInfo.append(strPrivatperson).append(";");
      sbDumpInfo.append(strSperreAuskunft).append(";");
      sbDumpInfo.append(strAnzRelevNMs).append(";");
      sbDumpInfo.append("\n");
      return sbDumpInfo;
   }

}
