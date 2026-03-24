package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.CteCrefoTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;

public class TrackingExportInfoDumper extends AbstractTrackingInfoDumper {
    @Override
    public void dumpCrefoTrackingErgebnisInfo(String customer, CteCrefoTrackingErgebnis cteCrefoTrackingErgebnis, TesunClientJobListener tesunClientJobListener) {
        StringBuilder sbDumpInfo = new StringBuilder();
        cteCrefoTrackingErgebnis.getTrackingExport().stream()
                .filter(cteTrackingExport -> (customer == null) || cteTrackingExport.getExportTyp().contains(customer))
                //.sorted(Comparator.comparing(CteTrackingExport::getExportTyp))
                .forEach(cteTrackingExport -> {
                    // Crefo;Export-Typ;Erste Lieferung;Letzte Lieferung;Löschsatz;Export-Status;Letzte Voll-Lieferung;Letzte Lieferung als Beteiligter
                    sbDumpInfo.append(cteTrackingExport.getCrefo()).append(";");
                    sbDumpInfo.append(cteTrackingExport.getExportTyp()).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingExport.getDatumErsteLieferung())).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingExport.getDatumLetzteLieferung())).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingExport.getDatumLoeschsatz())).append(";");
                    sbDumpInfo.append(cteTrackingExport.getExportStatus()).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingExport.getDatumLetzteVollLieferung())).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingExport.getDatumLetzteLieferungAlsBeteiligter())).append("\n");
                });
        writeToiFile(sbDumpInfo, tesunClientJobListener);
    }

    @Override
    public String getHeader() {
        return "Crefo;Export-Typ;Erste Lieferung;Letzte Lieferung;Löschsatz;Export-Status;Letzte Voll-Lieferung;Letzte Lieferung als Beteiligter\n";
    }

    @Override
    public String getTrackingType() {
        return "ExportInfo";
    }
}
