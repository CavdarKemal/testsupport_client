package de.creditreform.crefoteam.cte.tesun.bic_import_ident;

import de.creditreform.crefoteam.ctcbase.sftputil.except.SftpUtilException;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpConnection;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpDirectoryEntry;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UploadsChecker {
    private final RestInvokerConfig restInvokerConfig;
    private final TesunClientJobListener tesunClientJobListener;
    protected static Logger logger = LoggerFactory.getLogger(UploadsChecker.class);;

    public UploadsChecker(RestInvokerConfig restInvokerConfig, TesunClientJobListener tesunClientJobListener) {
        this.restInvokerConfig = restInvokerConfig;
        this.tesunClientJobListener = tesunClientJobListener;
    }

    public void checkUploads(final String vvcToDhlSftpPath, final String prefix, final boolean uploadEmptyPayload) {
        logger.info("Prüfe upgeloadete Files für BIC...");
        try (SftpConnection sftpConnection = SftpConnection.forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                                                           .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                                                           .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword()))
        {
            sftpConnection.connect();
            int emptyUploadsCount = 0;
            int nonEmptyUploadsCount = 0;
            List<SftpDirectoryEntry> collectedZIPs = new ArrayList<>();
            List<SftpDirectoryEntry> collectedTXTs = new ArrayList<>();
            // Lese alle Verzeichnisse...
            List<SftpDirectoryEntry> sftpDirectoryEntryList = sftpConnection.ls(vvcToDhlSftpPath);
            for (SftpDirectoryEntry sftpDirectoryEntry : sftpDirectoryEntryList) {
                if (!sftpDirectoryEntry.isDir()) {
                    continue; // kein Verzeichnis, weiter mit dem nächsten...
                }
                String dirName = sftpDirectoryEntry.getFilename();
                if (!dirName.matches(prefix + ".*")) {
                    continue; // kein passendes Verzeichnis, weiter mit dem nächsten...
                }
                tesunClientJobListener.notifyClientJob(Level.INFO, "\nChecke Verzeichnis '" + dirName + "'...");
                // Lese alle Dateien in unterhalb des aktuellen Verzeichnisses...
                List<SftpDirectoryEntry> sftpDirectoryEntryList1 = sftpConnection.ls(vvcToDhlSftpPath + "/" + dirName);
                for (SftpDirectoryEntry sftpDirectoryEntry1 : sftpDirectoryEntryList1) {
                    if (sftpDirectoryEntry1.isDir()) {
                        continue; // keine Datei, weiter mit dem nächsten...
                    }
                    String fileName = sftpDirectoryEntry1.getFilename();
                    if (fileName.endsWith(".zip")) {
                        tesunClientJobListener.notifyClientJob(Level.INFO, "\n'" + fileName + "' ist eine ZIP-Datei");
                        collectedZIPs.add(sftpDirectoryEntry1);
                    } else if (fileName.contains(BicSftpUtil.COMPLETITION_INFO_TXT)) {
                        tesunClientJobListener.notifyClientJob(Level.INFO, "\n" + fileName + "' ist die completioninfo- Datei");
                        collectedTXTs.add(sftpDirectoryEntry1);
                    }
                }
                if (collectedTXTs.isEmpty()) {
                    emptyUploadsCount++;
                }
                if (!collectedZIPs.isEmpty()) {
                    nonEmptyUploadsCount++;
                }
                collectedTXTs.clear();
                collectedZIPs.clear();
            }
            if (uploadEmptyPayload) {
                if (emptyUploadsCount < 1) {
                    throw new RuntimeException("Keine Leer-Updates gefunden!");
                }
            } else {
                if ((emptyUploadsCount != 0) || (nonEmptyUploadsCount < 1)) {
                    throw new RuntimeException("Leer-Updates gefunden!");
                }
            }
            String strInfo = String.format("\nMit uploadEmptyPayload = %s wurde(n)  %d Leer-Upload(s) und %d  NICHT-Leer-Upload(s) gefunden.", uploadEmptyPayload, emptyUploadsCount, nonEmptyUploadsCount);
            tesunClientJobListener.notifyClientJob(Level.INFO, strInfo);
        } catch (SftpUtilException e) {
            throw new RuntimeException(e);
        }
    }

}
