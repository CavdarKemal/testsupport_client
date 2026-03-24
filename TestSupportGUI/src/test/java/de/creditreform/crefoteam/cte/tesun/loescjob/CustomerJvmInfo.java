package de.creditreform.crefoteam.cte.tesun.loescjob;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomerJvmInfo {
    protected String customerKey;
    protected String customerName;
    protected String hostForExports;
    protected String hostForUploads;
    protected String remoteExportsBaseDir;
    protected List<String> remoteExportsSubDirsList = new ArrayList<>();
    protected String remoteUploadsBaseDir;
    protected List<String> remoteUploadsSubDirsList = new ArrayList<>();

    public CustomerJvmInfo(String customerKey, String customerName) {
        this.customerKey = customerKey;
        this.customerName = customerName;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getHostForExports() {
        return hostForExports;
    }

    public String getHostForUploads() {
        return hostForUploads;
    }

    public String getRemoteExportsBaseDir() {
        return remoteExportsBaseDir;
    }

    public String getRemoteUploadsBaseDir() {
        return remoteUploadsBaseDir;
    }

    public List<String> getRemoteExportsSubDirsList() {
        return remoteExportsSubDirsList;
    }

    public List<String> getRemoteUploadsSubDirsList() {
        return remoteUploadsSubDirsList;
    }
}
