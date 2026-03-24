package de.creditreform.crefoteam.cte.tesun.util;

public class TestEnvPropsInfo {
    private String customerPropertyPrefix;
    private String exportFormat;
    private String exportFormatOptions;
    private String exportFormatBranchen;
    private String extraXmlFeatures;
    private String extraXmlFeaturesBranchen;
    private String vcListInfo; // "ctebvdexport.vc = 412"
    private String fachwertKonfig; // "Aktualisierungsdatum: YY-MM-DD, PD-Version: Grundbestand (PD2018Q3)"

    public String getCustomerPropertyPrefix() {
        return customerPropertyPrefix;
    }

    public void setCustomerPropertyPrefix(String customerPropertyPrefix) {
        this.customerPropertyPrefix = customerPropertyPrefix;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getExportFormatOptions() {
        return exportFormatOptions;
    }

    public void setExportFormatOptions(String exportFormatOptions) {
        this.exportFormatOptions = exportFormatOptions;
    }

    public String getExportFormatBranchen() {
        return exportFormatBranchen;
    }

    public void setExportFormatBranchen(String exportFormatBranchen) {
        this.exportFormatBranchen = exportFormatBranchen;
    }

    public String getExtraXmlFeatures() {
        return extraXmlFeatures;
    }

    public void setExtraXmlFeatures(String extraXmlFeatures) {
        this.extraXmlFeatures = extraXmlFeatures;
    }

    public String getExtraXmlFeaturesBranchen() {
        return extraXmlFeaturesBranchen;
    }

    public void setExtraXmlFeaturesBranchen(String extraXmlFeaturesBranchen) {
        this.extraXmlFeaturesBranchen = extraXmlFeaturesBranchen;
    }

    public String getVcListInfo() {
        return vcListInfo;
    }

    public void setVcListInfo(String vcListInfo) {
        this.vcListInfo = vcListInfo;
    }

    public String getFachwertKonfig() {
        return fachwertKonfig;
    }

    public void setFachwertKonfig(String fachwertKonfig) {
        this.fachwertKonfig = fachwertKonfig;
    }
}
