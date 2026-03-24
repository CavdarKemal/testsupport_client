package de.creditreform.crefoteam.cte.tesun.loescjob;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class SftpClientForLoeschJobsTest2 {
    String username = "ctcb";
    String password = "************";

    @Test
    public void testSftpClientForLoeschJobs_Exports() throws Exception {
        CustomerJvmInfo customerJvmInfo = new EneRtnJvmInfo();
        SftpClientForLoeschJobs sftpClientForLoeschJobs = new SftpClientForLoeschJobs(customerJvmInfo, username, password);
        Date filterDate = TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS.parse("2024-09-30_23-59");
        Map<String, List<RemoteResourceInfo>> remoteResourceInfoMap = sftpClientForLoeschJobs.listDirsToDelete(filterDate);
    }

}
