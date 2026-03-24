package de.creditreform.crefoteam.cte.tesun.loescjob;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class SftpClientForLoeschJobsTest {
    String username = "ctcb";
    String password = "********";

    @Test
    public void testSftpClientForLoeschJobs_Exports() throws Exception {
        CustomerJvmInfo customerJvmInfo = new DefaultEneCustomerJvmInfo("rtn", "rating-ag");
        Date filterDate = TesunDateUtils.toDate("2024-09-30_00-00");
        RemoteResourceFilter remoteResourceFilter = RemoteResourceFilterFactory.createRemoteResourceDirFilter(customerJvmInfo.getCustomerKey(), filterDate);
        SftpClientForLoeschJobs sftpClientForLoeschJobs = new SftpClientForLoeschJobs(customerJvmInfo, username, password);
        List<RemoteResourceInfo> remoteResourceInfos = sftpClientForLoeschJobs.listExportsResources(customerJvmInfo.getRemoteExportsBaseDir(), remoteResourceFilter, false);
        checkResources(remoteResourceInfos, customerJvmInfo.getCustomerKey(), null, filterDate);

        String fileExt = "zip";
        remoteResourceFilter = RemoteResourceFilterFactory.createRemoteResourceFileFilter(customerJvmInfo.getCustomerKey(), fileExt, filterDate);
        sftpClientForLoeschJobs = new SftpClientForLoeschJobs(customerJvmInfo, username, password);
        remoteResourceInfos = sftpClientForLoeschJobs.listExportsResources(customerJvmInfo.getRemoteExportsBaseDir(), remoteResourceFilter, true);
        checkResources(remoteResourceInfos, customerJvmInfo.getCustomerKey(), fileExt, filterDate);
    }

    @Test
    public void testSftpClientForLoeschJobs_Uploads() throws Exception {
        CustomerJvmInfo customerJvmInfo = new DefaultEneCustomerJvmInfo("rtn", "rating-ag");
        Date filterDate = TesunDateUtils.toDate("2024-09-31_23-59");
        RemoteResourceFilter remoteResourceFilter = RemoteResourceFilterFactory.createRemoteResourceDirFilter(customerJvmInfo.getCustomerKey(), filterDate);
        SftpClientForLoeschJobs sftpClientForLoeschJobs = new SftpClientForLoeschJobs(customerJvmInfo, username, password);
        List<RemoteResourceInfo> remoteResourceInfos = sftpClientForLoeschJobs.listUploadsResources(customerJvmInfo.getRemoteUploadsBaseDir(), remoteResourceFilter, false);
        checkResources(remoteResourceInfos, customerJvmInfo.getCustomerKey(), null, filterDate);

        String fileExt = "gpg";
        remoteResourceFilter = RemoteResourceFilterFactory.createRemoteResourceFileFilter(customerJvmInfo.getCustomerKey(), fileExt, filterDate);
        sftpClientForLoeschJobs = new SftpClientForLoeschJobs(customerJvmInfo, username, password);
        remoteResourceInfos = sftpClientForLoeschJobs.listUploadsResources(customerJvmInfo.getRemoteUploadsBaseDir(), remoteResourceFilter, true);
        checkResources(remoteResourceInfos, customerJvmInfo.getCustomerKey(), fileExt, filterDate);
    }

    private static void checkResources(List<RemoteResourceInfo> remoteResourceInfoList, String jvmName, String extension, Date filterDate) {
        remoteResourceInfoList.forEach(remoteResourceInfo -> {
            RemoteResourceFilterFactory.printResourceInfo(remoteResourceInfo);
            Assert.assertTrue(remoteResourceInfo.getPath().contains(jvmName));
            if (extension != null) {
                Assert.assertFalse(remoteResourceInfo.isDirectory());
                Assert.assertTrue(remoteResourceInfo.getName().endsWith(extension));
            }
            else {
                Assert.assertTrue(remoteResourceInfo.isDirectory());
            }
            if (filterDate != null) {
                Date resDate = RemoteResourceFilterFactory.getResourceLastAccessDate(remoteResourceInfo);
                Assert.assertTrue(filterDate.after(resDate));
            }
        });
    }

}
