package de.creditreform.crefoteam.cte.tesun.loescjob;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.fileimpl.PathElementProcessorFileSystem;
import de.creditreform.crefoteam.cte.pathabstraction.util.PathResolver;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SSHClientTest implements TesunClientJobListener {

    private PathResolver pathResolver;
    private String localFile = "src/test/resources/ssh/input.txt";
    private String localDir = "src/main/resources/ssh";
    String remoteHost15 = "rhsctem015.ecofis.de";
    String remoteHost16 = "rhsctem016.ecofis.de";
    String remoteHost11 = "rhsctem011.ecofis.de";
    String remoteExportsDir1516 = "/home/ctcb/alle_exporte/";
    String remoteExportsDir11 = "/home/ctcb/alle_exporte/";
    String username = "ctcb";
    String password = "***********";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testPathElement() {
        pathResolver = new PathResolver();
        PathElementProcessorFileSystem pathElementProcessorFileSystem = new PathElementProcessorFileSystem(pathResolver, "sftp://ctcb:Consumer00Horst@rhsctem015.ecofis.de/home/ctcb/alle_exporte/bedirect/export/delta");
        List<PathElement> candidates = pathElementProcessorFileSystem.listFiles(pathElement -> {
            return true;
        });
    }

    @Test
    public void testUsingSshJ_CEF_Zips_OlderOnRemoteHost15() throws IOException, ParseException {
        String jvmName = "cef";
        String remoteDir = remoteExportsDir11 + jvmName + "/export/delta/";
        String fileExt = "zip";
        Date filterDate = TesunDateUtils.toDate("2024-10-01_00-00");
        doForRemoteHostX(jvmName, fileExt, filterDate, remoteHost11, remoteDir);
    }

    private void doForRemoteHostX(String jvmName, String fileExt, Date filterDate, String remoteHost, String remoteDir) throws IOException {
        RemoteResourceFilter remoteResourceFilter;
        List<RemoteResourceInfo> remoteResourceInfoList;

        remoteResourceFilter = RemoteResourceFilterFactory.createRemoteResourceFileFilter(jvmName, fileExt, filterDate);
        remoteResourceInfoList = downloadUsingSshJ(remoteHost, remoteDir, remoteResourceFilter);
        checkResources(remoteResourceInfoList, jvmName, fileExt, filterDate);

        remoteResourceFilter = RemoteResourceFilterFactory.createRemoteResourceDirFilter(jvmName, filterDate);
        remoteResourceInfoList = downloadUsingSshJ(remoteHost, remoteDir, remoteResourceFilter);
        checkResources(remoteResourceInfoList, jvmName, null, filterDate);
    }

    private static void checkResources(List<RemoteResourceInfo> remoteResourceInfoList, String jvmName, String fileExt, Date filterDate) {
        remoteResourceInfoList.forEach(remoteResourceInfo -> {
            Date resDate = RemoteResourceFilterFactory.getResourceLastAccessDate(remoteResourceInfo);
            String strInfo = String.format("Resource: %s; LastModifed: %s", remoteResourceInfo.getName(), resDate);
            System.out.println(strInfo);
            Assert.assertFalse(remoteResourceInfo.isDirectory());
            Assert.assertTrue(remoteResourceInfo.getPath().contains(jvmName));
            if (fileExt != null) {
                Assert.assertTrue(remoteResourceInfo.getName().endsWith(fileExt));
            }
            if (filterDate != null) {
                Assert.assertTrue(filterDate.after(resDate));
            }
        });
    }

    private void uploadFileUsingSshJ(String remoteHost, String remoteDir, String theFileName) throws IOException {
        SSHClient sshClient = createSSHClientForSShJ(remoteHost);
        SFTPClient sftpClient = sshClient.newSFTPClient();
        sftpClient.put(localFile, remoteDir + theFileName);
        sftpClient.close();
        sshClient.disconnect();
    }

    private List<RemoteResourceInfo> downloadUsingSshJ(String remoteHost, String remoteDir, RemoteResourceFilter remoteResourceFilter) throws IOException {
        SSHClient sshClient = createSSHClientForSShJ(remoteHost);
        SFTPClient sftpClient = sshClient.newSFTPClient();
        List<RemoteResourceInfo> remoteResourceInfosList = listResourcesFromRemoteDir("", sftpClient, remoteDir, remoteResourceFilter, true);
        sftpClient.close();
        sshClient.disconnect();
        return remoteResourceInfosList;
    }

    private static List<RemoteResourceInfo> listResourcesFromRemoteDir(String prefix, SFTPClient sftpClient, String remoteDir, RemoteResourceFilter remoteResourceFilter, boolean isRecursive) throws IOException {
        List<RemoteResourceInfo> remoteResourceInfoList = new ArrayList<>();
        List<RemoteResourceInfo> remoteResourceInfos = sftpClient.ls(remoteDir, remoteResourceFilter);
        for (RemoteResourceInfo remoteResourceInfo : remoteResourceInfos) {
            if (isRecursive && remoteResourceInfo.isDirectory()) {
                String strInfo = String.format(prefix + "Directory: %s", remoteResourceInfo.getName());
                remoteResourceInfoList.addAll(listResourcesFromRemoteDir(prefix + "\t", sftpClient, remoteResourceInfo.getPath(), remoteResourceFilter, isRecursive));
            } else {
                String strInfo = String.format(prefix + "Resource: %s; Attributes: %s", remoteResourceInfo.getName(), remoteResourceInfo.getAttributes());
                remoteResourceInfoList.add(remoteResourceInfo);
            }
        }
        return remoteResourceInfoList;
    }

    private SSHClient createSSHClientForSShJ(String remoteHost) throws IOException {
        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect(remoteHost);
        client.useCompression();
        client.authPassword(username, password);
        return client;
    }

    @Override
    public void notifyClientJob(Level level, Object notifyObject) {

    }

    @Override
    public Object askClientJob(ASK_FOR askFor, Object userObject) {
        return null;
    }
}
