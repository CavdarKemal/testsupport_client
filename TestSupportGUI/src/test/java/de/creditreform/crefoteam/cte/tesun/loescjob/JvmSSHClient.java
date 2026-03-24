package de.creditreform.crefoteam.cte.tesun.loescjob;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigUploadInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import org.apache.log4j.Level;

import java.util.List;

public class JvmSSHClient implements TesunClientJobListener {
    private String remoteDirINsoEllis = "/home/ctcb/alle_exporte/sdf/export/delta/sdf-ellis/";
    private String remoteDir = "/home/ctcb/alle_exporte/";
    private String remoteFile = "welcome.txt";
    private String localDir = "src/main/resources/ssh";
    String remoteHost15 = "rhsctem015.ecofis.de";
    String remoteHost16 = "rhsctem016.ecofis.de";
    String username = "ctcb";
    String password = "***********";
    private EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");
    List<TesunConfigUploadInfo> uploadPfadeList;
    List<TesunConfigExportInfo> exportPfadeList;

    public void setUp() throws Exception {
        TesunRestService tesunRestServiceWLS = new TesunRestService(this.environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), this);
        TesunConfigInfo tesunConfigInfo = tesunRestServiceWLS.getTesunConfigInfo();
        uploadPfadeList = tesunConfigInfo.getUploadPfade();
        exportPfadeList = tesunConfigInfo.getExportPfade();
    }

    private TesunConfigUploadInfo findTesunConfigUploadInfo(String customerKey) {
        for (TesunConfigUploadInfo tesunConfigUploadInfo : uploadPfadeList) {
            if (tesunConfigUploadInfo.getKundenKuerzel().equalsIgnoreCase(customerKey)) {
                return tesunConfigUploadInfo;
            }
        }
        return null;
    }

    private TesunConfigExportInfo findTesunConfigExportInfo(String customerKey) {
        for (TesunConfigExportInfo tesunConfigExportInfo : exportPfadeList) {
            if (tesunConfigExportInfo.getKundenKuerzel().equalsIgnoreCase(customerKey)) {
                return tesunConfigExportInfo;
            }
        }
        return null;
    }

    @Override
    public void notifyClientJob(Level level, Object notifyObject) {

    }

    @Override
    public Object askClientJob(ASK_FOR askFor, Object userObject) {
        return null;
    }
}
