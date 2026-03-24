package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;

public class CollectInfo {
    private final TestCrefo testCrefo;
    private final String ZipEntryName;
    private final boolean statusOK;
    private final String strInfo;

    public CollectInfo(TestCrefo testCrefo, String zipEntryName, boolean statusOK, String strInfo) {
        this.testCrefo = testCrefo;
        ZipEntryName = zipEntryName;
        this.statusOK = statusOK;
        this.strInfo = strInfo;
    }

    public TestCrefo getTestCrefo() {
        return testCrefo;
    }

    public String getZipEntryName() {
        return ZipEntryName;
    }

    public boolean isStatusOK() {
        return statusOK;
    }

    public String getStrInfo() {
        return strInfo;
    }

    @Override
    public String toString() {
        return "CollectInfo : " + testCrefo + ", Zip ='" + ZipEntryName + ", Status=" + statusOK + ", Info='" + strInfo + "'";
    }
}
