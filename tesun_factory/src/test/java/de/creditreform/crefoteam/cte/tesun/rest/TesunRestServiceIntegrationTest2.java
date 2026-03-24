package de.creditreform.crefoteam.cte.tesun.rest;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigUploadInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentPropertiesTupel;
import de.creditreform.crefoteam.cte.tesun.CustomerTransferProps;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TesunRestServiceIntegrationTest2 extends TesunRestServiceIntegrationTestBase {
    public TesunRestServiceIntegrationTest2() {
        super("ABE");
    }

    @Test
    public void test_getEnvironmentProperties() throws IOException {
        String keyFilter = "importCycle.ctimport.vc";
        String valueFilter = "";
        CteEnvironmentProperties cteEnvironmentProperties = tesunRestServiceWLS.getEnvironmentProperties(keyFilter, valueFilter, true);
        Assert.assertNotNull(cteEnvironmentProperties);
        dumpProperties(cteEnvironmentProperties, keyFilter, valueFilter, null);
        for (CteEnvironmentPropertiesTupel cteEnvironmentPropertiesTupel : cteEnvironmentProperties.getProperties()) {
            Assert.assertTrue(cteEnvironmentPropertiesTupel.getKey().contains(keyFilter));
        }
    }

    @Test
    public void test_getPropsWith412() throws IOException {
        String keyFilter = "";
        CteEnvironmentProperties cteEnvironmentProperties = tesunRestServiceWLS.getEnvironmentProperties(keyFilter, TestSupportClientKonstanten.TEST_CLZ_412, true);
        Assert.assertNotNull(cteEnvironmentProperties);
        dumpProperties(cteEnvironmentProperties, keyFilter, TestSupportClientKonstanten.TEST_CLZ_412, null);
        for (CteEnvironmentPropertiesTupel cteEnvironmentPropertiesTupel : cteEnvironmentProperties.getProperties()) {
            Assert.assertTrue(cteEnvironmentPropertiesTupel.getValue().contains(TestSupportClientKonstanten.TEST_CLZ_412));
        }
    }


    @Test
    public void testGetTestclientProps() throws IOException {
        String keyFilter = "cteTestclient";
        String valueFilter = "";
        CteEnvironmentProperties cteEnvironmentProperties = tesunRestServiceWLS.getEnvironmentProperties(keyFilter, valueFilter, true);
        Assert.assertNotNull(cteEnvironmentProperties);
        dumpProperties(cteEnvironmentProperties, keyFilter, valueFilter, null);
    }

}
