package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.CteCrefoTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;

public class TrackingZeitlicherAblaufInfo extends AbstractTrackingInfoDumper {

    @Override
    public void dumpCrefoTrackingErgebnisInfo(String customer, CteCrefoTrackingErgebnis cteCrefoTrackingErgebnis, TesunClientJobListener tesunClientJobListener) {
        StringBuilder sbDumpInfo = new StringBuilder();
        cteCrefoTrackingErgebnis.getTrackingZeitlicherAblauf().stream()
                //.sorted(Comparator.comparing(CteTrackingZeitlicherAblauf::getDatumEingangCTO))
                .forEach(cteTrackingZeitlicherAblauf -> {
                    // Crefo;Einspielung CTO;Einspielung CTE-Staging;Einspielung CTE-Bestand
                    sbDumpInfo.append(cteTrackingZeitlicherAblauf.getCrefo()).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingZeitlicherAblauf.getDatumEingangCTO())).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingZeitlicherAblauf.getDatumEingangCTEStaging())).append(";");
                    sbDumpInfo.append(TesunDateUtils.formatCalendar(cteTrackingZeitlicherAblauf.getDatumEingangCTEBestand())).append(";");
                    sbDumpInfo.append(cteTrackingZeitlicherAblauf.getFlowStatus()).append("\n");
                });
        writeToiFile(sbDumpInfo, tesunClientJobListener);
    }

    @Override
    public String getHeader() {
        return "Crefo;Einspielung CTO;Einspielung CTE-Staging;Einspielung CTE-Bestand\n";
    }

    @Override
    public String getTrackingType() {
        return "ZeitlicherAblaufInfo";
    }

}
