package de.creditreform.crefoteam.cte.tesun.util;

import java.io.File;

public class CrefoModiferResult {

    private final Long crefoNr;
    private final File crefoFile;

    public CrefoModiferResult(Long crefoNr, File crefoFile) {
        this.crefoNr = crefoNr;
        this.crefoFile = crefoFile;
    }

    public Long getCrefoNr() {
        return crefoNr;
    }

    public File getCrefoFile() {
        return crefoFile;
    }
}
