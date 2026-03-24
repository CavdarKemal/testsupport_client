package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigUploadInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.KundenKonfig;
import de.creditreform.crefoteam.cte.restservices.xmlbinding.fachwertaktualisierung.KundenKonfigList;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.lang3.tuple.MutablePair;

public class SystemInfo {

    private KundenKonfigList kundenKonfigList;
    private List<CteEnvironmentPropertiesTupel> envPropsList;
    private TesunConfigInfo tesunConfigInfo;

    public void setKundenKonfigList(KundenKonfigList kundenKonfigList) {
        this.kundenKonfigList = kundenKonfigList;
    }

    public void setTesunConfigInfo(TesunConfigInfo tesunConfigInfo) {
        this.tesunConfigInfo = tesunConfigInfo;
    }

    public void setEnvPropsList(List<CteEnvironmentPropertiesTupel> envPropsList) {
        this.envPropsList = envPropsList;
    }


    public void fillPropertyPairForCustomer(String propertyPrefix, final MutablePair<String, String> mutablePair) {
        if(mutablePair.getLeft().contains("%")) {
            mutablePair.setLeft(mutablePair.getLeft().replace("%", propertyPrefix));
        }
        for (CteEnvironmentPropertiesTupel propertiesTupel : envPropsList) {
            if (propertiesTupel.getKey().equalsIgnoreCase(mutablePair.getLeft())) {
                mutablePair.setRight(propertiesTupel.getValue());
                return;
            }
        }
    }

    public TesunConfigExportInfo findTesunConfigExportInfoForCustomer(TestCustomer testCustomer) {
        List<TesunConfigExportInfo> exportPfadeList = tesunConfigInfo.getExportPfade();
        for (TesunConfigExportInfo tesunConfigExportInfo : exportPfadeList) {
            if (tesunConfigExportInfo.getKundenKuerzel().startsWith(testCustomer.getJvmName())) {
                return tesunConfigExportInfo;
            }
        }
        return null;
    }

    public TesunConfigUploadInfo findTesunConfigUploadInfoForCustomer(TestCustomer testCustomer) {
        List<TesunConfigUploadInfo> uploadPfadeList = tesunConfigInfo.getUploadPfade();
        for (TesunConfigUploadInfo tesunConfigUploadInfo : uploadPfadeList) {
            if (tesunConfigUploadInfo.getKundenKuerzel().startsWith(testCustomer.getJvmName())) {
                return tesunConfigUploadInfo;
            }
        }
        return null;
    }

    public KundenKonfig findFachwertconfigInfoForCustomer(TestCustomer testCustomer) {
        for (KundenKonfig kundenKonfig : kundenKonfigList.getKonfigs()) {
            if (kundenKonfig.getProzessName().equalsIgnoreCase(testCustomer.getProcessIdentifier())) {
                return kundenKonfig;
            }
        }
        KundenKonfig kundenKonfig = new KundenKonfig();
        kundenKonfig.setAktualisierungsdatum(Calendar.getInstance());
        kundenKonfig.setPdversion("???");
        return kundenKonfig;
    }
}
