package de.creditreform.crefoteam.cte.tesun.exportanalyse;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ZipAnalyseInfo {
    private final String zipFilename;
    private Map<String, List<Long>> prefixCrefosMap = new TreeMap<>();
    private long zipSize;

    public ZipAnalyseInfo(String zipFilename) {
        this.zipFilename = zipFilename;
    }

    public long getZipSize() {
        return zipSize;
    }

    public void setZipSize(long zipSize) {
        this.zipSize = zipSize;
    }

    public Map<String, List<Long>> getPrefixCrefosMap() {
        return prefixCrefosMap;
    }

    public String getZipFilename() {
        return zipFilename;
    }
}
