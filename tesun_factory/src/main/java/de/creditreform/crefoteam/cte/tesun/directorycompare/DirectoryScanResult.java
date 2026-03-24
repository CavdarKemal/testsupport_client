package de.creditreform.crefoteam.cte.tesun.directorycompare;

import java.io.File;

/**
 * Container für die Ergebnisse des {@link DirectoryScanner}
 * User: ralf
 * Date: 03.07.14
 * Time: 12:51
 */
public class DirectoryScanResult
implements Comparable<DirectoryScanResult> {

    private final String directoryBasedNamePrefix;
    private final String comparableFileNameSection;
    private final String identifier;
    private final File file;

    public DirectoryScanResult(File directory, File file) {
        this.comparableFileNameSection = extractComparableString(file.getName());
        final int beginIndex = directory.getPath().length() + 1;
        final int endIndex = file.getPath().length() - file.getName().length() - 1;
        if (endIndex <= beginIndex) {
            this.directoryBasedNamePrefix = "";
            this.identifier = this.comparableFileNameSection;
        }
        else {
            this.directoryBasedNamePrefix = createNamePrefix(file.getPath(), beginIndex, endIndex);
            this.identifier = this.directoryBasedNamePrefix+this.comparableFileNameSection;
        }
        this.file = file;
    }

    protected String extractComparableString(String zipFileName) {
        if (zipFileName==null) {
            return "";
        }
        else {
            int pos = zipFileName.indexOf("abCrefo");
            if (pos>0) {
                return zipFileName.substring(pos);
            }
            else {
                return zipFileName;
            }
        }
    }

    protected String createNamePrefix(String filePath, int beginIndex, int endIndex) {
        StringBuilder sb = new StringBuilder(endIndex - beginIndex + 2);
        boolean skipMode = false;
        for (int i = beginIndex; i < endIndex; i++) {
            final char c = filePath.charAt(i);
            final char nxt;
            switch (c) {
                case '\\':
                case '/':
                    skipMode = false;
                    nxt = '_';
                    break;
                case ' ':
                case '_':
                    skipMode = true;
                    nxt = '_';
                    break;
                default:
                    nxt = c;
            }
            if (!skipMode) {
                sb.append(nxt);
            }

        }
        sb.append('_');

        return sb.toString();
    }


    /**
     * Lese den Prefix, der im Dateinamen als Ersatz für den relativen Pfad
     * dienen soll
     */
    public String getDirectoryBasedNamePrefix() {
        return directoryBasedNamePrefix;
    }

    /**
     * Lese den Teil des Namens, der zum Abgleich zweier Instanzen in
     * vergleichbaren Verzeichnissen herangezogen werden soll.
     */
    public String getComparableFileNameSection() {
        return comparableFileNameSection;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Lese das {@link File}-Objekt zu dioesem Treffer
     */
    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectoryScanResult)) return false;

        DirectoryScanResult that = (DirectoryScanResult) o;

        return file.equals(that.file);

    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public int compareTo(DirectoryScanResult o) {
        int cmp = this.getDirectoryBasedNamePrefix().compareTo(o.getDirectoryBasedNamePrefix());
        if (cmp==0) {
            cmp = this.getComparableFileNameSection().compareTo(o.getComparableFileNameSection());
        }
        return cmp;
    }
}
