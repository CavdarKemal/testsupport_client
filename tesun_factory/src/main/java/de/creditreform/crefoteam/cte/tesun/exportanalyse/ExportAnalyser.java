package de.creditreform.crefoteam.cte.tesun.exportanalyse;

import de.creditreform.crefoteam.ctcbase.sftputil.except.SftpUtilException;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpConnection;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpDirectoryEntry;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ExportAnalyser {
    private final String kundenKuerzel;
    private final RestInvokerConfig restInvokerConfig;
    private Logger logger;

    public ExportAnalyser(String kundenKuerzel, RestInvokerConfig restInvokerConfig, Logger logger) {
        this.kundenKuerzel = kundenKuerzel;
        this.restInvokerConfig = restInvokerConfig;
        this.logger = logger;
    }

    public CustomerAnalyseInfo analyseExport(List<String> exportPathsList) {
        CustomerAnalyseInfo customerAnalyseInfo = new CustomerAnalyseInfo(kundenKuerzel);
        exportPathsList.stream().forEach(exportPath -> {
            logger.info("\tAnalysiere Kunden-Export '" + exportPath + "'...");
            ExportAnalyseInfo exportAnalyseInfo = new ExportAnalyseInfo(exportPath);
            customerAnalyseInfo.getExportAnalyseInfoList().add(exportAnalyseInfo);
            List<CteZipFileEntry> exportDirZipList = readZipFilesFromExportPath(exportPath);
            exportDirZipList.stream().forEach(zipFileEntry -> {
                if (zipFileEntry.getFilename().endsWith(".zip")) {
                    if (!kundenKuerzel.equalsIgnoreCase("bic")) {
                        ZipAnalyser zipAnalyser = new ZipAnalyser(restInvokerConfig, zipFileEntry, logger);
                        ZipAnalyseInfo zipInfo = zipAnalyser.analyseZip();
                        zipInfo.setZipSize(zipFileEntry.getSize());
                        exportAnalyseInfo.addZipAnalyseInfo(zipInfo);
                    }
                }
            });
        });
        return customerAnalyseInfo;
    }

    public List<CteZipFileEntry> readZipFilesFromExportPath(String exportPath) {
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
            sftpConnection.connect();
            List<CteZipFileEntry> exportDirZipList = getSftpDirectoryEntries(exportPath, sftpConnection);
            return exportDirZipList;
        } catch (SftpUtilException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CteZipFileEntry> getSftpDirectoryEntries(String exportPath, SftpConnection sftpConnection) throws SftpUtilException {
        List<CteZipFileEntry> exportDirZipList = new ArrayList<>();
        List<SftpDirectoryEntry> zipFileEntriesList = sftpConnection.ls(exportPath);
        for (SftpDirectoryEntry zipFileEntry : zipFileEntriesList) {
            if (!zipFileEntry.isDir()) {
                if (zipFileEntry.getFilename().endsWith(".zip")) {
                    CteZipFileEntry cteZipFileEntry = new CteZipFileEntry(exportPath + "/" + zipFileEntry.getFilename());
                    cteZipFileEntry.setSize(zipFileEntry.getSize());
                    exportDirZipList.add(cteZipFileEntry);
                }
            } else if (!zipFileEntry.getFilename().startsWith(".")) {
                String exportPath1 = exportPath + "/" + zipFileEntry.getFilename();
                // logger.info("Rekursiver Aufruf readZipFilesFromExportPath(" + exportPath1 +" )...");
                exportDirZipList.addAll(getSftpDirectoryEntries(exportPath1, sftpConnection));
            }
        }
        return exportDirZipList;
    }

}
