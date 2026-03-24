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
import java.util.Map;
import java.util.TreeMap;

public class ExportSchecker {
    private final RestInvokerConfig restInvokerConfig;
    private final TesunClientJobListener tesunClientJobListener;
    protected static Logger logger = LoggerFactory.getLogger(ExportSchecker.class);;

    public ExportSchecker(RestInvokerConfig restInvokerConfig, TesunClientJobListener tesunClientJobListener) {
        this.restInvokerConfig = restInvokerConfig;
        this.tesunClientJobListener = tesunClientJobListener;
    }

    public Map<String, SftpDirectoryEntry> readTodaysExports(String exportPath) {
        logger.info("Ermittle aktuelle Exports...");
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
            sftpConnection.connect();
            Map<String, SftpDirectoryEntry> exportEntryMap = new TreeMap<>();
            List<SftpDirectoryEntry> exortDirectoryEntryList = sftpConnection.ls(exportPath);
            for (SftpDirectoryEntry exportDirectoryEntry : exortDirectoryEntryList) {
                if (!exportDirectoryEntry.isDir() || !exportDirectoryEntry.getFilename().startsWith(BicSftpUtil.TODAY_DATE)) {
                    continue; // kein passendes Verzeichnis, weiter mit dem nächsten...
                }
                exportEntryMap.put(exportDirectoryEntry.getFilename(), exportDirectoryEntry);
            }
            return exportEntryMap;
        } catch (SftpUtilException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkNewExports(Map<String, SftpDirectoryEntry> todaysExportsEntriesMap, String exportPath, int expectedNewExports, boolean checkEmptyExports) {
        logger.info("Prüfe neue Exporte...");
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
            sftpConnection.connect();
            List<SftpDirectoryEntry> exportEntryList = new ArrayList<>();
            List<SftpDirectoryEntry> exortDirectoryEntryList = sftpConnection.ls(exportPath);
            for (SftpDirectoryEntry exportDirectoryEntry : exortDirectoryEntryList) {
                if (!exportDirectoryEntry.isDir() || !exportDirectoryEntry.getFilename().startsWith(BicSftpUtil.TODAY_DATE)) {
                    continue; // kein passendes Verzeichnis, weiter mit dem nächsten...
                }
                // nur die aufnehmen, die NICHT in der Eingangsliste dabei waren...
                if (!todaysExportsEntriesMap.containsKey(exportDirectoryEntry.getFilename())) {
                    exportEntryList.add(exportDirectoryEntry);
                }
            }
            if (expectedNewExports != exportEntryList.size()) {
                throw new RuntimeException("Export-Entries-Liste sollte " +expectedNewExports + " Element enthalten!");
            }
            if(checkEmptyExports) {
                // check, dass mindestens ein leeres Export-Verzeichnis dabei ist...
                checkEmptyExport(sftpConnection, exportPath, exportEntryList);
            }
        } catch (SftpUtilException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkEmptyExport(SftpConnection sftpConnection, String exportPath, List<SftpDirectoryEntry> exportEntryList) throws SftpUtilException {
        logger.info("Prüfe leere Exporte...");
        boolean emptyExportFound = false;
        for (SftpDirectoryEntry exportEntry : exportEntryList) {
            boolean zipFound = false;
            boolean txtFound = false;
            List<SftpDirectoryEntry> exportEntryContentList = sftpConnection.ls(exportPath + "/" + exportEntry.getFilename());
            String notifyObject = "\nExport '" + exportPath + "/" + exportEntry.getFilename() + "' enthält : " + exportEntryContentList.size() + " Dateien";
            tesunClientJobListener.notifyClientJob(Level.INFO, notifyObject);
            for (SftpDirectoryEntry exportEntryContent : exportEntryContentList) {
                if (!exportEntryContent.isDir()) {
                    if (exportEntryContent.getFilename().endsWith(".zip")) {
                        zipFound = true;
                    }
                    if (exportEntryContent.getFilename().endsWith(".txt")) {
                        txtFound = true;
                    }
                }
            }
            if (txtFound && !zipFound) {
                emptyExportFound = true;
            }
        }
        if (!emptyExportFound) {
            throw new RuntimeException("Leer-Export nicht gefunden!");
        }
    }


}
