package de.creditreform.crefoteam.cte.tesun.sftp_uploads;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadDirsPathElementFilter implements PathElementFilter {
    private final static Logger logger = LoggerFactory.getLogger(UploadDirsPathElementFilter.class);

    private final boolean searchDirs;

    public UploadDirsPathElementFilter(boolean searchDirs) {
        this.searchDirs = searchDirs;
    }

    @Override
    public boolean accept(PathElement pathElement) {
        final String name = pathElement.getName();
        // logger.debug("UploadDirsPathElementFilter#accept() mit PathElement {}", name);
        boolean isOK = isPathElementOK(pathElement, searchDirs);
        if (isOK) {
            Calendar calendar = TesunDateUtils.extractDateFromString(name);
            isOK = calendar != null;
        }
        return isOK;
    }

    private boolean isPathElementOK(PathElement pathElement, boolean searchDirs) {
        final String name = pathElement.getName();
        boolean isOK = !name.contains("full") && !name.contains("online") && (name.length() > 10);
        if (searchDirs) {
            return isOK && pathElement.isDirectory();
        } else {
            return isOK && !pathElement.isDirectory() && (name.endsWith("zip") || name.endsWith("gpg") || name.endsWith("completioninfo.txt"));
        }
    }
}
