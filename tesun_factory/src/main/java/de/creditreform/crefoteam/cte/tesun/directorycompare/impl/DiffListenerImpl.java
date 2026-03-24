package de.creditreform.crefoteam.cte.tesun.directorycompare.impl;

import de.creditreform.crefoteam.cte.tesun.directorycompare.DiffListener;
import de.creditreform.crefoteam.cte.tesun.directorycompare.DirectoryCompareFolders;
import de.creditreform.crefoteam.cte.tesun.directorycompare.DirectoryScanResult;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener für die Ergebnisse des {@link de.creditreform.crefoteam.cte.tesun.directorycompare.DirectoryCompare}
 * User: ralf
 * Date: 12.06.14
 * Time: 08:38
 */
public class DiffListenerImpl
implements DiffListener {
    private final DirectoryCompareFolders folders;
    private final Logger infoLogger;

    private int identicalFiles;
    private List<String> differentFiles;
    private List<String> firstOnlyFiles;
    private List<String> secondOnlyFiles;
    private int notComparedFiles;
    private List<String> messages;
    private boolean extractFirst;
    private boolean extractSecond;

    @Inject
    public DiffListenerImpl(DirectoryCompareFolders folders) {
        this.folders = folders;
        infoLogger = LoggerFactory.getLogger(TestSupportClientKonstanten.LOW_THRESHOLD_LOGGER);
        init(false, false);
    }

    /**
     * Initialisierung für einen neuen Vergleichs-Durchlauf. Über die Flags
     * wird gesteuert, ob fehlede Dateien des ersten oder zweiten
     * Verzeichnisses in das Ziel-Verzeichnis kopiert werden sollen.
     * @param extractFirst true, wenn Dateien aus dem ersten Verzeichnis kopiert werden sollen
     * @param extractSecond true, wenn Dateien aus dem zweiten Verzeichnis kopiert werden sollen
     */
    @Override
    public final void init(boolean extractFirst, boolean extractSecond) {
        this.identicalFiles = 0;
        this.notComparedFiles = 0;
        this.messages = new ArrayList<>(256);
        this.differentFiles = new ArrayList<>();
        this.firstOnlyFiles = new ArrayList<>();
        this.secondOnlyFiles = new ArrayList<>();
        this.extractFirst = extractFirst;
        this.extractSecond = extractSecond;
    }

    private void addMessage(String message) {
        messages.add(message);
        infoLogger.info(message);
    }

    @Override
    public void notifyPathChange(DirectoryScanResult scanResult) {
        addMessage("entering directory: "+scanResult.getFile().getParent());
    }

    @Override
    public void identical(DirectoryScanResult firstResult, DirectoryScanResult secondResult) {
        identicalFiles++; // Keine Ausgabe in die messages
    }

    @Override
    public void identical(String zipName, String zipEntryName) {
        identicalFiles++; // Keine Ausgabe in die messages
    }

    @Override
    public void different(String zipName, String zipEntryName, List<String> reportedDiffs) {
        differentFiles.add(zipName+'_'+zipEntryName);
        folders.logDifferences(zipName, zipEntryName, reportedDiffs);
    }

    @Override
    public void logException(String zipName, String zipEntryName, Throwable t) {

    }

    @Override
    public void notCompared(String zipName, String zipEntryName, String reason) {
        notComparedFiles++;
        addMessage("Zip-Einträge nicht verglichen: " + zipName+":"+zipEntryName+"\nGrund: " + reason);
    }

    @Override
    public void notCompared(DirectoryScanResult firstResult, DirectoryScanResult secondResult) {
        notComparedFiles++;
        addMessage("Dateien nicht verglichen: \n1. " + firstResult.getFile().getPath() + "\n2. " + secondResult.getFile().getPath());
    }

    @Override
    public void firstOnly(DirectoryScanResult firstResult) {
        if (extractFirst) {
            folders.copyFile(firstResult.getFile(), firstResult.getDirectoryBasedNamePrefix(), folders.getResultFirstOnly());
        }
        addMessage("Datei nur im ersten Verzeichnis enthalten: " + firstResult.getIdentifier());
    }

    @Override
    public void firstZipOnly(String zipFileIdentifier, String zipEntryname, ByteArrayOutputStream stream) {
        folders.copyContentToFile(stream.toByteArray(), zipFileIdentifier, zipEntryname, folders.getResultFirstOnly());
        addMessage("Datensatz nur in der ersten Zip-Datei enthalten: " + zipFileIdentifier + zipEntryname);
    }

    @Override
    public void secondOnly(DirectoryScanResult secondResult) {
        if (extractSecond) {
            folders.copyFile(secondResult.getFile(), secondResult.getDirectoryBasedNamePrefix(), folders.getResultSecondOnly());
        }
        addMessage("Datei nur im zweiten Verzeichnis enthalten: " + secondResult.getIdentifier());
    }

    @Override
    public void secondZipOnly(String zipFileIdentifier, String zipEntryname, ByteArrayOutputStream stream) {
        folders.copyContentToFile(stream.toByteArray(), zipFileIdentifier, zipEntryname, folders.getResultSecondOnly());
        addMessage("Datensatz nur in der zweiten Zip-Datei enthalten: " + zipFileIdentifier + zipEntryname);
    }

    @Override
    public void close() {
        StringBuilder sb = new StringBuilder(20480);
        sb.append("\n=== Zusammenfassung ===");
        sb.append("\nAnzahl der identischen Dateien: ").append(identicalFiles);
        sb.append("\nAnzahl der nicht verglichenen Dateien: ").append(notComparedFiles);
        sb.append("\nAnzahl der unterschiedlichen Dateien: ").append(differentFiles.size());
        sb.append("\nAnzahl der Dateien nur im ersten Verzeichnis: ").append(firstOnlyFiles.size());
        sb.append("\nAnzahl der Dateien nur im zweiten Verzeichnis: ").append(secondOnlyFiles.size());
        sb.append("\n=== Ende der Zusammenfassung ===");
        appendList(sb, "Unterschiedliche Dateien", differentFiles);
        appendList(sb, "Dateien nur im ersten Verzeichnis", firstOnlyFiles);
        appendList(sb, "Dateien nur im zweiten Verzeichnis", secondOnlyFiles);
        folders.logCompletionReport(sb.toString());
    }

    private void appendList(StringBuilder sb, String headline, List<String> elements) {
        if (elements!=null && !elements.isEmpty()) {
            sb.append("\n").append(headline);
            for (String e : elements) {
                sb.append('\n').append(e);
            }
        }
    }
}
