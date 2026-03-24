package de.creditreform.crefoteam.cte.tesun.xmlsearch.domain;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.handler.LogInfo;
import de.creditreform.crefoteam.ctreader.transferjobbase.pgputil.LargeFilesPGPUtil;
import de.creditreform.crefoteam.ctreader.transferjobbase.pgputil.MyPGPUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class DecryptHandler {
    private final String passPhrase;
    private final String searchName;
    private MyPGPUtil pgpUtil;
    private XmlStreamListenerGroup listenerGroup;

    public DecryptHandler(XmlStreamListenerGroup listenerGroup, String passPhrase, String searchName) {
        this.searchName = searchName;
        this.listenerGroup = listenerGroup;
        this.passPhrase = passPhrase;
        this.pgpUtil = new LargeFilesPGPUtil();
    }

    public File decryptFile(File encryptedFile, File privateKeyFile, boolean force) throws Exception {
        String strInfo = "Entschlüsseln der GPG-Datei '" + encryptedFile.getAbsolutePath() + "'";
        listenerGroup.updateData(new LogInfo(searchName, ProgressListenerIF.LOG_LEVEL.INFO, strInfo, null));
        long millisStart = System.currentTimeMillis();
        final String newFileName = encryptedFile.getName().replace(".gpg", "");
        File decryptedFile = new File(encryptedFile.getParentFile(), newFileName);
        if (encryptedFile.getName().endsWith(".zip")) {
            FileUtils.copyFile(encryptedFile, decryptedFile);
            return decryptedFile;
        }
        // Parameter force bestimmt, ob das Entschlüsseln optional überspringen werden soll, falls schon entschlüsselt
        if (force || !decryptedFile.exists()) {
            pgpUtil.decryptFile(encryptedFile, privateKeyFile, decryptedFile, passPhrase.toCharArray());
            if (!decryptedFile.exists()) {
                throw new RuntimeException("GPG-Datei wurde nicht erstellt!");
            }
            strInfo = "Datei '" + encryptedFile.getAbsolutePath() + "' wurde entschlüsselt und in\n\t'" + decryptedFile.getAbsolutePath() + "' abgespeichert.";
        } else {
            strInfo = "Datei '" + encryptedFile.getAbsolutePath() + "' existiert bereits und braucht nicht entschlüsselt zu werden";
        }
        listenerGroup.updateData(new LogInfo(searchName, ProgressListenerIF.LOG_LEVEL.INFO, strInfo, null));
        long millisEnd = System.currentTimeMillis();
        listenerGroup.updateData(new LogInfo(searchName, ProgressListenerIF.LOG_LEVEL.INFO, TesunDateUtils.formatElapsedTime("Entschlüsseln der Datei '" + encryptedFile.getAbsolutePath(), millisStart, millisEnd), null));
        return decryptedFile;
    }

}
