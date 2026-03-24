package de.creditreform.crefoteam.cte.tesun.sftp_uploads;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigUploadInfo;

import java.util.HashMap;
import java.util.Map;

import static de.creditreform.crefoteam.cte.pathabstraction.sftpapi.SftpConfigurationMapKeys.*;

/**
 * Created by CavdarK on 30.12.2016.
 */
public class SftpUploadAdapterConfig {
   private final static String configName = "junit-config";
   private final static String UPLOAD_PATH = "upload_path";

   private final TesunConfigInfo tesunConfigInfo;
   private final String customerKey;
   private TesunConfigUploadInfo tesunConfigUploadInfo;
   private Map<Object, Object> sftpCfgMap = new HashMap<>();

   public SftpUploadAdapterConfig(TesunConfigInfo tesunConfigInfo, String customerKey) {
      this.tesunConfigInfo = tesunConfigInfo;
      this.customerKey = customerKey;
      tesunConfigUploadInfo = null;
      for(TesunConfigUploadInfo TesunConfigExportInfo : tesunConfigInfo.getUploadPfade()) {
         String kundenKuerzel = TesunConfigExportInfo.getKundenKuerzel();
         boolean ok = kundenKuerzel.startsWith(customerKey);
         if (!ok && kundenKuerzel.length() > 3) {
            ok = kundenKuerzel.substring(0,3).compareTo(customerKey.substring(0,3)) == 0;
         }
         if (ok) {
            tesunConfigUploadInfo = TesunConfigExportInfo;
            break;
         }
      }
      if(tesunConfigUploadInfo == null) {
         throw new RuntimeException("SFTP-Upload-Konfiguration für den Kunden " +customerKey + " wurde nicht gesetzt!");
      }
   }

   private boolean isLocal() {
      String completePath = tesunConfigUploadInfo.getCompletePath();
      boolean isSftp = completePath.contains("@");
      boolean freigabe = completePath.contains("fileserver.");
      return !isSftp && !freigabe;
   }

   public String getSftpUploadPath() {
      return (String)sftpCfgMap.get(UPLOAD_PATH);
   }

   public String getSftpUploadHost() {
      return sftpCfgMap.get(configName + POSTFIX_TARGET_HOST) + ":" + sftpCfgMap.get(configName + POSTFIX_TARGET_PORT);
   }
   public Map<Object, Object> getSftpCfgMap() {
      if (!isLocal() && sftpCfgMap.isEmpty()) {
         String strValue = tesunConfigUploadInfo.getCompletePath();
         try {
            String[] split1 = strValue.split("@");
            String[] userAndPassword = split1[0].split(":");
            String[] hostAndPortAndRelPath = split1[1].split("/", 2);
            String[] hostAndPort = hostAndPortAndRelPath[0].split(":");
            if(!hostAndPort[0].startsWith("?")) {
               sftpCfgMap.put(configName + POSTFIX_TARGET_HOST, hostAndPort[0]);
               sftpCfgMap.put(configName + POSTFIX_TARGET_PORT, hostAndPort[1]);
            }
            else {
            }
            String relPath = String.format("sftp:%s:%s", configName, hostAndPortAndRelPath[1]);
            sftpCfgMap.put(configName + POSTFIX_REMOTE_USER_NAME, userAndPassword[0]);
            sftpCfgMap.put(configName + POSTFIX_REMOTE_USER_PWD, userAndPassword[1]);
            sftpCfgMap.put(configName + POSTFIX_TARGET_DIRECTORY, "null");
            sftpCfgMap.put(configName + POSTFIX_ENABLE_SSH_RSA, "true");
            sftpCfgMap.put(UPLOAD_PATH, relPath);
         } catch (Exception ex) {
            StringBuilder strErrMsg = new StringBuilder();
            if(ex.getMessage() != null && !ex.getMessage().isBlank()) {
               strErrMsg.append("\nException beim Ermitteln der cteTestclient.*-Werte für ");
               strErrMsg.append(customerKey);
               strErrMsg.append(": ");
               strErrMsg.append(ex.getMessage());
            }
            else {
               strErrMsg.append("\nDer Wert '");
               strErrMsg.append(strValue);
               strErrMsg.append("' für ");
               strErrMsg.append(tesunConfigInfo.getUmgebungsKuerzel());
               strErrMsg.append("-Masterkonsole-Property 'cteTestclient.basedir' ist nicht gesetzt oder hat falsches Format!");
               strErrMsg.append("\nFormat: user:password@ip:port");
            }
            throw new RuntimeException(strErrMsg.toString());
         }
      }
      return sftpCfgMap;
   }

   public String getCustomerKey() {
      return customerKey;
   }
}
