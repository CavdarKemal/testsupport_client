package de.creditreform.crefoteam.cte.tesun.loescjob;

public class EneRtnJvmInfo extends DefaultEneCustomerJvmInfo{
    public EneRtnJvmInfo() {
        super("rtn", "rating-ag");
        this.remoteExportsSubDirsList.add("tempzip");
        this.remoteExportsSubDirsList.add("tempencrypted");
        this.remoteExportsSubDirsList.add("full");
        this.remoteExportsSubDirsList.add("delta");
        this.remoteUploadsSubDirsList.add("");
    }

}
