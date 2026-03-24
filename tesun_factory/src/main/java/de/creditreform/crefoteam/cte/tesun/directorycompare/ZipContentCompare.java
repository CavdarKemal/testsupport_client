package de.creditreform.crefoteam.cte.tesun.directorycompare;

/**
 * Schnittstelle für den Vergleich zweier Zip-Dateien im Rahmen des
 * {@link DirectoryCompare}
 * User: ralf
 * Date: 16.06.14
 * Time: 09:10
 */
public interface ZipContentCompare {

    /**
     * Führe den Vergleich zweier Zip-Dateien aus
     * @param matchKey String, anhand dessen die Zuordnung der beiden Zip-Dateien zueinander erfolgte
     * @param firstResult DirectoryScanResult zur Zip-Datei aus dem ersten Verzeichnis
     * @param secondResult DirectoryScanResult zur Zip-Datei aus dem zweiten Verzeichnis
     */
    void compareZipFiles(String matchKey, DirectoryScanResult firstResult, DirectoryScanResult secondResult);

}
