package de.creditreform.crefoteam.cte.tesun;

public class CustomerTransferProps {
    private final String customerKey;
    private String exportHost;
    private String exportDirectory;
    private String localTemporaryZipDirectory;
    private String pgpEncryptedDirectory;
    private String remoteUserName;
    private String remoteUserPrivateKeyFile;
    private String pgpPublicKeyFile;
    private String targetDirectory;
    private String targetHost;
    private String targetEnableSshRsa;
    private String targetOverwriteExisting;
    private String exportUser;
    private String uploadUser;
    private String uploadPassword;
    private String exportPassword;

    public CustomerTransferProps(String customerKey) {
        this.customerKey = customerKey;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public String getExportDirectory() {
        return exportDirectory;
    }

    public void setExportDirectory(String exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    public String getLocalTemporaryZipDirectory() {
        return localTemporaryZipDirectory;
    }

    public void setLocalTemporaryZipDirectory(String localTemporaryZipDirectory) {
        this.localTemporaryZipDirectory = localTemporaryZipDirectory;
    }

    public String getPgpEncryptedDirectory() {
        return pgpEncryptedDirectory;
    }

    public void setPgpEncryptedDirectory(String pgpEncryptedDirectory) {
        this.pgpEncryptedDirectory = pgpEncryptedDirectory;
    }

    public String getRemoteUserName() {
        return remoteUserName;
    }

    public void setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

    public String getRemoteUserPrivateKeyFile() {
        return remoteUserPrivateKeyFile;
    }

    public void setRemoteUserPrivateKeyFile(String remoteUserPrivateKeyFile) {
        this.remoteUserPrivateKeyFile = remoteUserPrivateKeyFile;
    }

    public String getPgpPublicKeyFile() {
        return pgpPublicKeyFile;
    }

    public void setPgpPublicKeyFile(String pgpPublicKeyFile) {
        this.pgpPublicKeyFile = pgpPublicKeyFile;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public String getTargetEnableSshRsa() {
        return targetEnableSshRsa;
    }

    public void setTargetEnableSshRsa(String targetEnableSshRsa) {
        this.targetEnableSshRsa = targetEnableSshRsa;
    }

    public String getTargetOverwriteExisting() {
        return targetOverwriteExisting;
    }

    public void setTargetOverwriteExisting(String targetOverwriteExisting) {
        this.targetOverwriteExisting = targetOverwriteExisting;
    }

    public String getExportHost() {
        return exportHost;
    }

    public void setExportHost(String exportHost) {
        this.exportHost = exportHost;
    }

    public String getExportUser() {
        return exportUser;
    }

    public void setExportUser(String exportUser) {
        this.exportUser = exportUser;
    }

    public String getUploadUser() {
        return uploadUser;
    }

    public void setUploadUser(String uploadUser) {
        this.uploadUser = uploadUser;
    }

    public String getUploadPassword() {
        return uploadPassword;
    }

    public void setUploadPassword(String uploadPassword) {
        this.uploadPassword = uploadPassword;
    }

    public String getExportPassword() {
        return exportPassword;
    }

    public void setExportPassword(String exportPassword) {
        this.exportPassword = exportPassword;
    }
}