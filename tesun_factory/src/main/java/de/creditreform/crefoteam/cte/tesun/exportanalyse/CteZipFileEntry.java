package de.creditreform.crefoteam.cte.tesun.exportanalyse;

public class CteZipFileEntry  {
    private  String zipName;
    private  Long size;

    public CteZipFileEntry(String zipName) {
        this.zipName = zipName;
    }

    public String getFilename() {
        return zipName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
