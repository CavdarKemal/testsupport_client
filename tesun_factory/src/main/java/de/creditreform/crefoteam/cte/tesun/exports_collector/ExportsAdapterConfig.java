package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.tesun.util.TestFallFileUtil;
import de.creditreform.crefoteam.cte.tesun.util.TesunConfigInfoUtils;

import javax.naming.InsufficientResourcesException;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static de.creditreform.crefoteam.cte.pathabstraction.sftpapi.SftpConfigurationMapKeys.*;

/**
 * Created by CavdarK on 30.12.2016.
 */
public class ExportsAdapterConfig {
    private final static String configName = "junit-config";

    private final TesunConfigInfo tesunConfigInfo;
    private final String customerKey;

    private String exportsPath;
    private String exportsHost;
    private Map<Object, Object> sftpCfgMap = new HashMap<>();

    public ExportsAdapterConfig(TesunConfigInfo tesunConfigInfo, String customerKey) {
        this.tesunConfigInfo = tesunConfigInfo;
        this.customerKey = customerKey;
    }

    private boolean isLocal() {
        TesunConfigExportInfo TesunConfigExportInfo0 = tesunConfigInfo.getExportPfade().get(0);
        boolean isSftp = TesunConfigExportInfo0.getRelativePath().contains("@");
        boolean freigabe = TesunConfigExportInfo0.getRelativePath().contains("fileserver.");
        return !isSftp && !freigabe;
    }

    public String getExportsPath() throws InsufficientResourcesException {
        if (exportsPath == null) {
            String relPath = TesunConfigInfoUtils.findRelativePathForCustomer(tesunConfigInfo, customerKey);
            if (isLocal()) {
                exportsPath = new File(relPath).getPath();
            } else {
                // bvd/export/delta oder  10.3.33.47:22@exports/bedirect/export/delta
                if (relPath.contains("@")) {
                    // 10.3.33.47:22@exports/bedirect/export/delta
                    relPath = relPath.substring(relPath.indexOf("@") + 1);
                }
                exportsPath = String.format("sftp:%s:%s", configName, relPath);
            }
        }
        return exportsPath;
    }

    public String getExportsHost() throws InsufficientResourcesException {
        if (exportsHost == null) {
            if (isLocal()) {
                exportsHost = "PC";
            } else {
                exportsHost = TesunConfigInfoUtils.findExportHostForCustomer(tesunConfigInfo, customerKey);
            }
        }
        return exportsHost;
    }

    public Map<Object, Object> getSftpCfgMap() throws InsufficientResourcesException {

        if (!isLocal() && sftpCfgMap.isEmpty()) {
            String relPath = "sftp://" + TesunConfigInfoUtils.findExportUrlForCustomer(tesunConfigInfo, customerKey);
            try {
                URI url = new URI(relPath);
                sftpCfgMap.put(configName + POSTFIX_TARGET_HOST, url.getHost());
                sftpCfgMap.put(configName + POSTFIX_TARGET_PORT, url.getPort());
                // connect scheitert, wenn das aktiv ist!  sftpCfgMap.put(configName + POSTFIX_TARGET_DIRECTORY, url.getPath());
                String[] split = url.getUserInfo().split(":");
                sftpCfgMap.put(configName + POSTFIX_REMOTE_USER_NAME, split[0]);
                sftpCfgMap.put(configName + POSTFIX_REMOTE_USER_PWD, split[1]);
            } catch (Exception ex) {
                StringBuilder strErrMsg = new StringBuilder();
                if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                    strErrMsg.append("\nException beim Ermitteln der cteTestclient.*-Werte für ");
                    strErrMsg.append(customerKey);
                    strErrMsg.append(": ");
                    strErrMsg.append(ex.getMessage());
                } else {
                    strErrMsg.append("\nDer Wert '");
                    strErrMsg.append(relPath);
                    strErrMsg.append("' für ");
                    strErrMsg.append(tesunConfigInfo.getUmgebungsKuerzel());
                    strErrMsg.append("-Masterkonsole-Property 'cteTestclient.basedir' ist nicht gesetzt oder hat falsches Format!");
                    strErrMsg.append("\nFormat: user:password@ip:port");
                }
                throw new InsufficientResourcesException(strErrMsg.toString());
            }
        }
        return sftpCfgMap;
    }

    public String getCustomerKey() {
        return customerKey;
    }
}
