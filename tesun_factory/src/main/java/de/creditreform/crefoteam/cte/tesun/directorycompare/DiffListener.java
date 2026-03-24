package de.creditreform.crefoteam.cte.tesun.directorycompare;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.List;

/**
 * Schnittstelle zur Entgegennahme der Vergleichs-Ergebnisse
 * User: ralf
 * Date: 16.06.14
 * Time: 11:36
 */
public interface DiffListener
extends Closeable {
    /**
     * Initialisierung für einen neuen Vergleichs-Durchlauf. Über die Flags
     * wird gesteuert, ob fehlede Dateien des ersten oder zweiten
     * Verzeichnisses in das Ziel-Verzeichnis kopiert werden sollen.
     * @param extractFirst true, wenn Dateien aus dem ersten Verzeichnis kopiert werden sollen
     * @param extractSecond true, wenn Dateien aus dem zweiten Verzeichnis kopiert werden sollen
     */
    void init(boolean extractFirst, boolean extractSecond);

    void notifyPathChange(DirectoryScanResult scanResult);

    void identical(DirectoryScanResult firstResult, DirectoryScanResult secondResult);

    void identical(String zipName, String zipEntryName);

    void different(String zipName, String zipEntryName, List<String> reportedDiffs);

    void logException(String zipName, String zipEntryName, Throwable t);

    void notCompared(DirectoryScanResult firstResult, DirectoryScanResult secondResult);

    void notCompared(String zipName, String zipEntryName, String reason);

    void firstOnly(DirectoryScanResult firstResult);

    void firstZipOnly(String relativePath, String zipEntryname, ByteArrayOutputStream stream);

    void secondOnly(DirectoryScanResult secondResult);

    void secondZipOnly(String relativePath, String zipEntryname, ByteArrayOutputStream stream);

    void close();

}
