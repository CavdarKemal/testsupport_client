package de.creditreform.crefoteam.cte.tesun.exportanalyse;

import java.util.*;

public class ExportAnalyseInfo {
    private final List<ZipAnalyseInfo> zipAnalyseInfos = new ArrayList<>();
    private Map<String, Long> prefixNumCrefosMap = new TreeMap<>();
    private final String exportDir;
    private long exportSize = 0;
    private String exportName;
    public ExportAnalyseInfo(String exportDir) {
        this.exportDir = exportDir;
        exportName = exportDir.split("delta/")[1];
    }

    public List<ZipAnalyseInfo> getZipAnalyseInfos() {
        return zipAnalyseInfos;
    }

    public String getExportDir() {
        return exportDir;
    }

    public String getExportName() {
        return exportName;
    }

    public Map<String, Long> getPrefixNumCrefosMap() {
        return prefixNumCrefosMap;
    }
    public long getExportSize() {
        return exportSize;
    }

    public void addZipAnalyseInfo(ZipAnalyseInfo zipAnalyseInfo) {
        zipAnalyseInfos.add(zipAnalyseInfo);
        exportSize += zipAnalyseInfo.getZipSize();
        Set<Map.Entry<String, List<Long>>> entrySet = zipAnalyseInfo.getPrefixCrefosMap().entrySet();
        for(Map.Entry<String, List<Long>> prefixCrefosMapKey : entrySet) {
            List<Long> prefixCrefosMapValue = prefixCrefosMapKey.getValue();
            if(prefixNumCrefosMap.containsKey(prefixCrefosMapKey.getKey())) {
                Long oldSize = prefixNumCrefosMap.get(prefixCrefosMapKey.getKey());
                prefixNumCrefosMap.put(prefixCrefosMapKey.getKey(), Long.valueOf(oldSize + prefixCrefosMapValue.size()));
            }
            else {
                prefixNumCrefosMap.put(prefixCrefosMapKey.getKey(), Long.valueOf(prefixCrefosMapValue.size()));
            }
        }
    }
}
