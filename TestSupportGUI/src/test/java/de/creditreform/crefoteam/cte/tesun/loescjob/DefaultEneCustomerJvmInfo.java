package de.creditreform.crefoteam.cte.tesun.loescjob;

public class DefaultEneCustomerJvmInfo extends CustomerJvmInfo{
    public DefaultEneCustomerJvmInfo(String customerKey, String customerName) {
        super(customerKey, customerName);
        this.hostForExports = "rhsctem011.ecofis.de";
        this.hostForUploads = "rhsctem011.ecofis.de";
        this.remoteExportsBaseDir = "/home/ctcb/alle_exporte/" + customerKey + "/export/";
        this.remoteUploadsBaseDir = "/home/ctcb/sftp_upload/" + customerKey + "/export/";
    }

}
