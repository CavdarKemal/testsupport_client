package de.creditreform.crefoteam.cte.tesun.postgre_props;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PropertyUtil {
    public static SimpleDateFormat DATE_FORMAT_DD_MM_JJJJ = new SimpleDateFormat("dd.MM.yyyy");

    public static void dumpProperties(List<CteEnvironmentPropertiesTupel> properties, File propsFile) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (CteEnvironmentPropertiesTupel cteEnvironmentPropertiesTupel : properties) {
            String strTemp = cteEnvironmentPropertiesTupel.isDbOverride() ? "[DB]" : "[FLUX]";
            stringBuilder.append(String.format("%s%s=%s\n", strTemp, cteEnvironmentPropertiesTupel.getKey(), cteEnvironmentPropertiesTupel.getValue()));
        }
        FileUtils.writeStringToFile(propsFile, stringBuilder.toString());
    }

    public static String getCurrentTiemStr() {
        return DATE_FORMAT_DD_MM_JJJJ.format(new Date());
    }


}
