package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.CteCrefoTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;

import java.util.HashMap;
import java.util.Map;

public class TrackingRelevanzPruefungInfo extends AbstractTrackingInfoDumper {

    static Map<String, String> customerToRelPrueferMap = new HashMap<>();

    static {
        customerToRelPrueferMap.put("firmenwissen relevanzpruefer", "fw");
        customerToRelPrueferMap.put("ctc relevanzpruefer", "ctc");
        customerToRelPrueferMap.put("ikaros relevanzpruefer", "ika");
        customerToRelPrueferMap.put("crm relevanzpruefer", "crm");
        customerToRelPrueferMap.put("coface relevanzpruefer", "cef");
        customerToRelPrueferMap.put("rating-ag relevanzpruefer", "rtn");
        customerToRelPrueferMap.put("drd relevanzpruefer", "drd");
        customerToRelPrueferMap.put("bignet gdl relevanzpruefer", "gdl");
        customerToRelPrueferMap.put("inso relevanzpruefer", "inso");
        customerToRelPrueferMap.put("fsu relevanzpruefer", "fsu");
        customerToRelPrueferMap.put("vsh relevanzpruefer", "vsh");
        customerToRelPrueferMap.put("nim relevanzpruefer", "nim");
        customerToRelPrueferMap.put("bvd relevanzpruefer", "bvd");
        customerToRelPrueferMap.put("zew relevanzpruefer", "zew");
        customerToRelPrueferMap.put("dfo relevanzpruefer", "dfo");
        customerToRelPrueferMap.put("eh relevanzpruefer", "eh");
        customerToRelPrueferMap.put("bedirect relevanzpruefer", "bdr");
        customerToRelPrueferMap.put("bignet ism relevanzpruefer", "ism");
        customerToRelPrueferMap.put("mic relevanzpruefer", "mic");
        customerToRelPrueferMap.put("lendico relevanzpruefer", "len");
        customerToRelPrueferMap.put("ppa relevanzpruefer", "ppa");
        customerToRelPrueferMap.put("foo relevanzpruefer", "foo");
    }

    @Override
    public void dumpCrefoTrackingErgebnisInfo(String customer, CteCrefoTrackingErgebnis cteCrefoTrackingErgebnis, TesunClientJobListener tesunClientJobListener) {
        StringBuilder sbDumpInfo = new StringBuilder();
        cteCrefoTrackingErgebnis.getTrackingRelevanzPruefung().stream()
                .filter(cteTrackingRelevanzPruefung -> {
                    String toLowerCase = cteTrackingRelevanzPruefung.getRelevanzPrueferBezeichnung().toLowerCase();
                    String relPruefer = customerToRelPrueferMap.get(toLowerCase);
                    boolean contains = relPruefer.contains(customer.toLowerCase());
                    return contains;
                })
                //.sorted(Comparator.comparing(CteTrackingRelevanzPruefung::getRelevanzPrueferBezeichnung))
                .forEach(cteTrackingRelevanzPruefung -> {
                    // Crefo;Beteiligt an;Relevanzprüfer;Prüfergebnis;Relevant von;Relevant bis;Zuletzt geprüft
                    sbDumpInfo.append(cteTrackingRelevanzPruefung.getCrefo()).append(";");
                    sbDumpInfo.append(cteTrackingRelevanzPruefung.getBeteiligtAnCrefo()).append(";");
                    sbDumpInfo.append(cteTrackingRelevanzPruefung.getRelevanzPrueferBezeichnung()).append(";");
                    sbDumpInfo.append(cteTrackingRelevanzPruefung.getPruefErgebnisArt()).append(";");
                    sbDumpInfo.append(cteTrackingRelevanzPruefung.getRelevantVonIncl()).append(";");
                    sbDumpInfo.append(cteTrackingRelevanzPruefung.getRelevantBisExcl()).append(";");
                    sbDumpInfo.append(cteTrackingRelevanzPruefung.getDatumLetztePruefung()).append("\n");
                });
        writeToiFile(sbDumpInfo, tesunClientJobListener);
    }

    @Override
    public String getHeader() {
        return "Crefo;Beteiligt an;Relevanzprüfer;Prüfergebnis;Relevant von;Relevant bis;Zuletzt geprüft\n";
    }

    @Override
    public String getTrackingType() {
        return "RelevanzPruefungInfo";
    }
}
