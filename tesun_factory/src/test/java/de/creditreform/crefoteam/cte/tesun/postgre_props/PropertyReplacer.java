package de.creditreform.crefoteam.cte.tesun.postgre_props;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;

import java.util.ArrayList;
import java.util.List;

public class PropertyReplacer {
    private final String toReplace;
    private final String replaceWith;

    List<CteEnvironmentPropertiesTupel> replacedProperties = new ArrayList<>();

    public PropertyReplacer(String toReplace, String replaceWith) {
        this.toReplace = toReplace;
        this.replaceWith = replaceWith;
    }

    public void replaceProperties(SourcePropertiesHolder sourcePropertiesHolder) {
        List<CteEnvironmentPropertiesTupel> sourcePropertiesTupels = sourcePropertiesHolder.getProperties();
        for (CteEnvironmentPropertiesTupel sourcePropertiesTupel : sourcePropertiesTupels) {
            CteEnvironmentPropertiesTupel replacedPropertiesTupel = new CteEnvironmentPropertiesTupel();
            replaceProperty(sourcePropertiesTupel, replacedPropertiesTupel);
            replacedProperties.add(replacedPropertiesTupel);
        }
    }

    private void replaceProperty(CteEnvironmentPropertiesTupel sourcePropertiesTupel, CteEnvironmentPropertiesTupel replacedPropertiesTupel) {
        replacedPropertiesTupel.setKey(sourcePropertiesTupel.getKey());
        String oldValue = sourcePropertiesTupel.getValue();
        String newValue = replaceWith;
        if (toReplace != null && !toReplace.isEmpty()) {
            newValue = oldValue.replace(toReplace, replaceWith);
            if(sourcePropertiesTupel.getKey().endsWith("exportDir")) {
                newValue = newValue.replace("alle_exporte", "alle_exporte");
            }
        }
        replacedPropertiesTupel.setValue(newValue);
        replacedPropertiesTupel.setDbOverride(sourcePropertiesTupel.isDbOverride());
    }

    public List<CteEnvironmentPropertiesTupel> getProperties() {
        return replacedProperties;
    }

    public void uploadNewProperties(TesunRestService tesunRestServicePG) {
        CteEnvironmentProperties cteEnvironmentProperties = new CteEnvironmentProperties();
        cteEnvironmentProperties.getProperties().addAll(getProperties());
        tesunRestServicePG.setEnvironmentProperties(cteEnvironmentProperties);
    }
}
