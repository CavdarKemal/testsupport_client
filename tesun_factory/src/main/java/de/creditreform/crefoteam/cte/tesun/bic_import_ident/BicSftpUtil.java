package de.creditreform.crefoteam.cte.tesun.bic_import_ident;

import de.creditreform.crefoteam.ctcbase.sftputil.except.SftpUtilException;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpConnection;
import de.creditreform.crefoteam.ctcbase.sftputil.sftpconnection.SftpDirectoryEntry;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BicSftpUtil {
    public static final String COMPLETITION_INFO_TXT = "completioninfo.txt";
    public static final String TODAY_DATE = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD.format(new Date());
    public static final String UPLOAD_FILE_PREFIX = "delta_" + TODAY_DATE.replaceAll("-", "_");
    private final RestInvokerConfig restInvokerConfig;
    private final TesunClientJobListener tesunClientJobListener;

    public BicSftpUtil(RestInvokerConfig restInvokerConfig, TesunClientJobListener tesunClientJobListener) {
        this.restInvokerConfig = restInvokerConfig;
        this.tesunClientJobListener = tesunClientJobListener;
    }

    public void copyCompletitionInfoFile(SftpConnection sftpConnection, File bicTestFilesDir, String sftpPath) throws URISyntaxException, FileNotFoundException, SftpUtilException {
        File completitionFile = new File(bicTestFilesDir, COMPLETITION_INFO_TXT);
        InputStream inputStream = new FileInputStream(completitionFile);
        String dst = sftpPath + "/" + completitionFile.getName();
        sftpConnection.put(inputStream, dst);
        tesunClientJobListener.notifyClientJob(Level.INFO, "\nDatei '" + dst + "' übertragen.");
    }

    public void copySampleFile(SftpConnection sftpConnection, File sampleFile, String sftpPath) throws Exception {
        InputStream inputStream = new FileInputStream(sampleFile);
        String filePath = sftpPath + "/" + sampleFile.getName();
        sftpConnection.put(inputStream, filePath);
        tesunClientJobListener.notifyClientJob(Level.INFO, "\nDatei '" + filePath + "' übertragen.");
    }

    public void deleteExports(String exportPath) throws Exception {
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
            sftpConnection.connect();
            List<SftpDirectoryEntry> sftpDirectoryEntryList = sftpConnection.ls(exportPath).stream().filter(sftpDirectoryEntry -> {
                return sftpDirectoryEntry.isDir() && sftpDirectoryEntry.getFilename().startsWith(TODAY_DATE);
            }).collect(Collectors.toList());
            tesunClientJobListener.notifyClientJob(Level.INFO, "\n" + sftpDirectoryEntryList.size() + " Exporte zum Löschen gefunden");
            sftpDirectoryEntryList.stream().forEach(sftpDirectoryEntry -> {
                try {
                    String dst = exportPath + "/" + sftpDirectoryEntry.getFilename();
                    boolean deleted = sftpConnection.rmDir(dst, true);
                    if (deleted) {
                        tesunClientJobListener.notifyClientJob(Level.INFO, "\nExport '" + dst + "' wurde erfolgreich gelöscht.");
                    } else {
                        tesunClientJobListener.notifyClientJob(Level.INFO, "\nExport '" + dst + "' konnte nicht gelöscht werden!");
                    }
                } catch (SftpUtilException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void deleteUploads(final String path, final String prefix) {
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
            sftpConnection.connect();
            List<SftpDirectoryEntry> sftpDirectoryEntryList = readUploadDirs(sftpConnection, path, prefix);
            tesunClientJobListener.notifyClientJob(Level.INFO, "\nPath: '" + path + "': " + sftpDirectoryEntryList.size() + " Verzeichnisse mit dem Prefix '" + prefix + "'zum Löschen gefunden.");
            sftpDirectoryEntryList.stream().forEach(sftpDirectoryEntry -> {
                try {
                    String dst = path + "/" + sftpDirectoryEntry.getFilename();
                    boolean deleted = sftpConnection.rmDir(dst, true);
                    if (deleted) {
                        tesunClientJobListener.notifyClientJob(Level.INFO, "\nVerzeichnis '" + dst + "' wurde erfolgreich gelöscht.");
                    } else {
                        tesunClientJobListener.notifyClientJob(Level.ERROR, "\nVerzeichnis '" + dst + "' konnte nicht gelöscht werden!");
                    }
                } catch (SftpUtilException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SftpUtilException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SftpDirectoryEntry> readUploadDirs(SftpConnection sftpConnection, String path, String prefix) throws SftpUtilException {
        List<SftpDirectoryEntry> sftpDirectoryEntryList = sftpConnection.ls(path).stream().filter(sftpDirectoryEntry -> {
            return sftpDirectoryEntry.isDir() && sftpDirectoryEntry.getFilename().startsWith(prefix);
        }).collect(Collectors.toList());
        return sftpDirectoryEntryList;
    }

    public void copyNewFilesToDhlToVvc(File bicTestFilesDir, String sftpPath) throws Exception {
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
            sftpConnection.connect();
            sftpConnection.mkdir(sftpPath);
            tesunClientJobListener.notifyClientJob(Level.INFO, "\nVerzeichnis '" + sftpPath + "' erstellt.");
            copyCompletitionInfoFile(sftpConnection, bicTestFilesDir, sftpPath);
            copySampleFile(sftpConnection, new File(bicTestFilesDir,"BIC_Imports_TitelOhneZeilen.csv"), sftpPath);
            copySampleFile(sftpConnection, new File(bicTestFilesDir,"BIC_Imports_OhneTitelMitZeilen.csv"), sftpPath);
            copySampleFile(sftpConnection, new File(bicTestFilesDir,"BIC_Imports_TitelMitZeilen.csv"), sftpPath);
            copySampleFile(sftpConnection, new File(bicTestFilesDir,"BIC_Imports_TitelMitZeilenMitBOM.csv"), sftpPath);
        }
    }

    public void readFilesFromDhlToVvc(File bicTestFilesDir, String sftpPath) throws Exception {
        try (SftpConnection sftpConnection = SftpConnection
                .forTrustedHost(restInvokerConfig.getServiceHost(), 22)
                .setAdditionalAlgo(TestSupportClientKonstanten.ALGORITHM_SSH_RSA)
                .andUser(restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword())) {
            sftpConnection.connect();
            sftpConnection.mkdir(sftpPath);
            tesunClientJobListener.notifyClientJob(Level.INFO, "\nVerzeichnis '" + sftpPath + "' erstellt.");
            copyCompletitionInfoFile(sftpConnection, bicTestFilesDir, sftpPath);
            copySampleFile(sftpConnection, new File(bicTestFilesDir,"BIC_Imports_TitelOhneZeilen.csv"), sftpPath);
            copySampleFile(sftpConnection, new File(bicTestFilesDir,"BIC_Imports_OhneTitelMitZeilen.csv"), sftpPath);
            copySampleFile(sftpConnection, new File(bicTestFilesDir,"BIC_Imports_TitelMitZeilen.csv"), sftpPath);
            copySampleFile(sftpConnection, new File(bicTestFilesDir,"BIC_Imports_TitelMitZeilenMitBOM.csv"), sftpPath);
        }
    }

    private int modifySampleFile(File sampleFile, int nextAnfrageNummer) throws Exception {
        List<String> newStringList = new ArrayList<>();
        List<String> stringList = FileUtils.readLines(sampleFile);
        for (String strLine : stringList) {
            String newLine = strLine;
            if (!strLine.isEmpty() && !strLine.startsWith("#")) {
                String[] split = strLine.split("\t");
                String[] split0 = split[0].split("\\.");
                try {
                    int nr = Integer.valueOf(split0[0]).intValue();
                    newLine = strLine.replaceAll(nr + ".", nextAnfrageNummer + ".");
                    nextAnfrageNummer++;

                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!newLine.isEmpty()) {
                newStringList.add(newLine);
            }
        }
        FileUtils.writeLines(sampleFile, newStringList);
        return nextAnfrageNummer;
    }

}
