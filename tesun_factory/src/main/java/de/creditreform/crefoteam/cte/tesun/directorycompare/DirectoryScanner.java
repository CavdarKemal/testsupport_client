package de.creditreform.crefoteam.cte.tesun.directorycompare;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

/**
 * Package-interne Klasse für das Durchsuchen eines Verzeichnisses
 * User: ralf
 * Date: 11.06.14
 * Time: 13:21
 */
class DirectoryScanner {

    DirectoryScanner() {
    }

    public Map<String, DirectoryScanResult> scanDirectory(File dir) {
        @SuppressWarnings("unchecked")
        final Collection<File> allFiles = FileUtils.listFiles(dir, new String[]{"zip", "xsd", "txt"}, true);
        Map<String, DirectoryScanResult> resMap = new TreeMap<String, DirectoryScanResult>();

        final int pathPrefixLength = dir.getPath().length() + 1;
        for (File f : allFiles) {
            if (f.getPath().length()>pathPrefixLength) {
                String relativePath = f.getPath().substring(pathPrefixLength);
                if (!relativePath.startsWith(".")) {
                    final DirectoryScanResult scanResult = new DirectoryScanResult(dir, f);
                    resMap.put(scanResult.getIdentifier(), scanResult);
                }
            }
        }
        return resMap;
    }

}
