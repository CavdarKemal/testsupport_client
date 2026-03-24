package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.CteCrefoTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;

public class TrackingImportEventInfo extends AbstractTrackingInfoDumper {

    @Override
    public void dumpCrefoTrackingErgebnisInfo(String customer, CteCrefoTrackingErgebnis cteCrefoTrackingErgebnis, TesunClientJobListener tesunClientJobListener) {
        StringBuilder sbDumpInfo = new StringBuilder();
        cteCrefoTrackingErgebnis.getTrackingImportEventList().stream()
                //.sorted(Comparator.comparing(CteTrackingImportEvent::getEventQuelle))
                .forEach(cteTrackingImportEvent -> {
                    // Crefo;Event Quelle;Event Datum;Event Counter
                    sbDumpInfo.append(cteTrackingImportEvent.getCrefo()).append(";");
                    sbDumpInfo.append(cteTrackingImportEvent.getEventQuelle()).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingImportEvent.getEventDatum())).append(";");
                    sbDumpInfo.append(cteTrackingImportEvent.getEventCounter()).append("\n");
                });
        writeToiFile(sbDumpInfo, tesunClientJobListener);
    }

    @Override
    public String getHeader() {
        return "Crefo;Event Quelle;Event Datum;Event Counter\n";
    }

    @Override
    public String getTrackingType() {
        return "ImportEventInfo";
    }

}
