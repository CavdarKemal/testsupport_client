package de.creditreform.crefoteam.cte.tesun.exportanalyse;

import de.creditreform.crefoteam.ctcbase.sftputil.except.SftpUtilException;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpConnection;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpDirectoryEntry;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CustormerExportAnalyser {
    private final String kundenKuerzel;
    private final Logger logger;
    private final TesunConfigExportInfo TesunConfigExportInfo;
    RestInvokerConfig restInvokerConfigForExports;

    public CustormerExportAnalyser(String kundenKuerzel, TesunConfigExportInfo TesunConfigExportInfo, Logger logger) {
        this.kundenKuerzel = kundenKuerzel;
        this.TesunConfigExportInfo = TesunConfigExportInfo;
        this.logger = logger;
        restInvokerConfigForExports = getRestInvokerConfig();
    }

    public CustomerAnalyseInfo analyse() {
        logger.info("Analysiere Kunde '" + kundenKuerzel + "'...");
        ExportAnalyser exportAnalyser = new ExportAnalyser(kundenKuerzel, restInvokerConfigForExports, logger);
        String exportPath = TesunConfigExportInfo.getRelativePath().split(":22/")[1];
        List<String> exportPathsList = readLastTwoExportPaths(exportPath);
        return exportAnalyser.analyseExport(exportPathsList);
    }

    public List<String> readLastTwoExportPaths(String exportPath) {
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfigForExports.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfigForExports.getServiceUser(), restInvokerConfigForExports.getServicePassword())) {
            sftpConnection.connect();
            List<String> exportPathsList = new ArrayList<>();
            List<SftpDirectoryEntry> exortDirectoryEntryList = sftpConnection.ls(exportPath);
            exortDirectoryEntryList.sort((SftpDirectoryEntry o1, SftpDirectoryEntry o2) -> o2.getFilename().compareTo(o1.getFilename()));
            if (exortDirectoryEntryList.size() > 0) {
                SftpDirectoryEntry sftpDirectoryEntry0 = exortDirectoryEntryList.get(0);
                if(!sftpDirectoryEntry0.getFilename().startsWith(".")) {
                    String exportDir = exportPath + "/" + sftpDirectoryEntry0.getFilename();
                    exportPathsList.add(exportDir);
                }
            }
            if (exortDirectoryEntryList.size() > 1) {
                SftpDirectoryEntry sftpDirectoryEntry1 = exortDirectoryEntryList.get(1);
                if(!sftpDirectoryEntry1.getFilename().startsWith(".")) {
                    String exportDir = exportPath + "/" + sftpDirectoryEntry1.getFilename();
                    exportPathsList.add(exportDir);
                }
            }
            return exportPathsList;
        } catch (SftpUtilException e) {
            throw new RuntimeException(e);
        }
    }

    public RestInvokerConfig getRestInvokerConfig() {
        String serviceURL = TesunConfigExportInfo.getRelativePath();
        String[] split1 = serviceURL.split("@");
        String[] split2 = split1[0].split(":");
        return new RestInvokerConfig(split1[1] + ":22", split2[0], split2[1]);
    }

}
