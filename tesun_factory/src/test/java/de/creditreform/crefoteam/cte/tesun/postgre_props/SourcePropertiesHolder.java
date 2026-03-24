package de.creditreform.crefoteam.cte.tesun.postgre_props;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;

import java.util.ArrayList;
import java.util.List;

public class SourcePropertiesHolder {
    final String keyFilter;
    private final String valueFilter;
    private final boolean skipClientProps;

    List<CteEnvironmentPropertiesTupel> properties = new ArrayList<>();

    SourcePropertiesHolder(String keyFilter, String valueFilter, boolean skipClientProps) {
        this.keyFilter = keyFilter;
        this.valueFilter = valueFilter;
        this.skipClientProps = skipClientProps;
    }

    public String getKeyFilter() {
        return keyFilter;
    }

    public String getValueFilter() {
        return valueFilter;
    }

    public boolean isSkipClientProps() {
        return skipClientProps;
    }

    public List<CteEnvironmentPropertiesTupel> getProperties() {
        return properties;
    }

    public void initSourcePropertiesHolder(TesunRestService tesunRestServiceWLS) {
        CteEnvironmentProperties cteEnvironmentProperties = tesunRestServiceWLS.getEnvironmentProperties(getKeyFilter(), getValueFilter(), isSkipClientProps());
        getProperties().addAll(cteEnvironmentProperties.getProperties());
    }

}
