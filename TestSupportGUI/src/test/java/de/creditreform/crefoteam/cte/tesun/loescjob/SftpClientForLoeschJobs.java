package de.creditreform.crefoteam.cte.tesun.loescjob;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;
import java.util.*;

public class SftpClientForLoeschJobs {
    private final CustomerJvmInfo customerJvmInfo;
    private SSHClient sshClientForExports = new SSHClient();
    private SSHClient sshClientForUploads = new SSHClient();

    public SftpClientForLoeschJobs(CustomerJvmInfo customerJvmInfo, String userName, String password) throws IOException {
        this.customerJvmInfo = customerJvmInfo;

        sshClientForExports.addHostKeyVerifier(new PromiscuousVerifier());
        sshClientForExports.connect(customerJvmInfo.getHostForExports());
        sshClientForExports.useCompression();
        sshClientForExports.authPassword(userName, password);

        sshClientForUploads.addHostKeyVerifier(new PromiscuousVerifier());
        sshClientForUploads.connect(customerJvmInfo.getHostForUploads());
        sshClientForUploads.useCompression();
        sshClientForUploads.authPassword(userName, password);
    }

    public Map<String, List<RemoteResourceInfo>> listDirsToDelete(Date filterDate) throws IOException {
        RemoteResourceFilter remoteResourceFilter = RemoteResourceFilterFactory.createRemoteResourceDirFilter(customerJvmInfo.getCustomerKey(), filterDate);
        Map<String, List<RemoteResourceInfo>> remoteResourceInfosMap = new TreeMap<>();
        for( String subDir : customerJvmInfo.getRemoteExportsSubDirsList() ) {
            String remoteDir = customerJvmInfo.getRemoteExportsBaseDir() + subDir;
            List<RemoteResourceInfo> remoteResourceInfosExport = listExportsResources(remoteDir, remoteResourceFilter, false);
            remoteResourceInfosMap.put(subDir, remoteResourceInfosExport);
        }
        for( String subDir : customerJvmInfo.getRemoteUploadsSubDirsList() ) {
            String remoteDir = customerJvmInfo.getRemoteExportsBaseDir() + subDir;
            List<RemoteResourceInfo> remoteResourceInfosUploads = listUploadsResources(remoteDir, remoteResourceFilter, false);
            remoteResourceInfosMap.put(subDir, remoteResourceInfosUploads);
        }
        return remoteResourceInfosMap;
    }

   public List<RemoteResourceInfo> listExportsResources(String remoteDir, RemoteResourceFilter remoteResourceFilter, boolean isRecursive) throws IOException {
       SFTPClient sftpClient = sshClientForExports.newSFTPClient();
       List<RemoteResourceInfo> remoteResourceInfos = internalListResources(sftpClient, remoteDir, remoteResourceFilter, isRecursive);
       sftpClient.close();
       sshClientForExports.close();
       return remoteResourceInfos;
   }

   public List<RemoteResourceInfo> listUploadsResources(String remoteDir, RemoteResourceFilter remoteResourceFilter, boolean isRecursive) throws IOException {
       SFTPClient sftpClient = sshClientForUploads.newSFTPClient();
       List<RemoteResourceInfo> remoteResourceInfos = internalListResources(sftpClient, remoteDir, remoteResourceFilter, isRecursive);
       sftpClient.close();
       sshClientForExports.close();
       return remoteResourceInfos;
   }

   private List<RemoteResourceInfo> internalListResources(SFTPClient sftpClient, String remoteDir, RemoteResourceFilter remoteResourceFilter, boolean isRecursive) throws IOException {
        List<RemoteResourceInfo> remoteResourceInfoList = new ArrayList<>();
        List<RemoteResourceInfo> remoteResourceInfos = sftpClient.ls(remoteDir, remoteResourceFilter);
        for (RemoteResourceInfo remoteResourceInfo : remoteResourceInfos) {
            if (isRecursive && remoteResourceInfo.isDirectory()) {
                remoteResourceInfoList.addAll(internalListResources(sftpClient, remoteResourceInfo.getPath(), remoteResourceFilter, isRecursive));
            } else {
                remoteResourceInfoList.add(remoteResourceInfo);
            }
        }
        return remoteResourceInfoList;
    }

}
