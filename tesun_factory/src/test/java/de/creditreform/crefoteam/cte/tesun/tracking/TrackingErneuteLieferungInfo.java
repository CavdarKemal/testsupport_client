package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.CteCrefoTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;

public class TrackingErneuteLieferungInfo extends AbstractTrackingInfoDumper {

    @Override
    public void dumpCrefoTrackingErgebnisInfo(String customer, CteCrefoTrackingErgebnis cteCrefoTrackingErgebnis, TesunClientJobListener tesunClientJobListener) {
        StringBuilder sbDumpInfo = new StringBuilder();
        cteCrefoTrackingErgebnis.getTrackingErneuteLieferung().stream()
                .filter(cteTrackingErneuteLieferung -> (customer == null) || cteTrackingErneuteLieferung.getExportTyp().equals(customer))
                //.sorted(Comparator.comparing(CteTrackingErneuteLieferung::getExportTyp))
                .forEach(cteTrackingErneuteLieferung -> {
                    // Crefo;Export-Typ;Nachlieferung angefordert
                    sbDumpInfo.append(cteTrackingErneuteLieferung.getCrefo()).append(";");
                    sbDumpInfo.append(cteTrackingErneuteLieferung.getExportTyp()).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingErneuteLieferung.getDatumNachlieferungAngefordert())).append("\n");
                });
        writeToiFile(sbDumpInfo, tesunClientJobListener);
    }

    @Override
    public String getHeader() {
        return "Crefo;Export-Typ;Nachlieferung angefordert\n";
    }

    @Override
    public String getTrackingType() {
        return "ErneuteLieferungInfo";
    }

}
