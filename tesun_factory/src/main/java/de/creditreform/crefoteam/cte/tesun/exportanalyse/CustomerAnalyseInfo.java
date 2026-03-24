package de.creditreform.crefoteam.cte.tesun.exportanalyse;

import java.util.ArrayList;
import java.util.List;

public class CustomerAnalyseInfo {
    private final List<ExportAnalyseInfo> exportAnalyseInfoList = new ArrayList<>();
    private final String kundenKuerzel;

    public CustomerAnalyseInfo(String kundenKuerzel) {
        this.kundenKuerzel = kundenKuerzel;
    }

    public List<ExportAnalyseInfo> getExportAnalyseInfoList() {
        return exportAnalyseInfoList;
    }

    public String getKundenKuerzel() {
        return kundenKuerzel;
    }
}
