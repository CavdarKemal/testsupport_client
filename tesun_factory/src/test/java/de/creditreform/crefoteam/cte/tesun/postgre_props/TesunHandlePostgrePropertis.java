package de.creditreform.crefoteam.cte.tesun.postgre_props;

import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.environmentproperties.CteEnvironmentProperties;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import org.apache.log4j.Level;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TesunHandlePostgrePropertis implements TesunClientJobListener {
    final static String sourceEnvName = "ENE";
    final static String targetEnvName = "ENE-PG";
    final static File rootDir = new File(System.getProperty("user.dir"));
    final static Map<String, SourcePropertiesHolder> propertiesHolderMap = new HashMap<String, SourcePropertiesHolder>() {{
        put("exportDir", new SourcePropertiesHolder(".exportDir", "", false));
        put("uploadDir", new SourcePropertiesHolder(".uploadDir", "", false));
        put("driver_class", new SourcePropertiesHolder("hibernate.connection.driver_class", "", true));
        put("url", new SourcePropertiesHolder("hibernate.connection.url", "", true));
        put("dialect", new SourcePropertiesHolder("hibernate.dialect", "", true));
        put("targetHost", new SourcePropertiesHolder("transfer.targetHost", "", true));

    }};
    final static Map<String, PropertyReplacer> replacerMap = new HashMap<String, PropertyReplacer>() {{
        put("exportDir", new PropertyReplacer("rhsctem011", "rhsctew004"));
        put("uploadDir", new PropertyReplacer("rhsctem011", "rhsctew004"));
        put("driver_class", new PropertyReplacer("", "org.postgresql.Driver"));
        put("url", new PropertyReplacer("", "jdbc:postgresql://rhscted001.ecofis.de/cteene?currentSchema=eneadmin"));
        put("dialect", new PropertyReplacer("", "org.hibernate.dialect.PostgreSQL9Dialect"));
        put("targetHost", new PropertyReplacer("rhsctem011", "rhsctew004"));

    }};
    EnvironmentConfig sourceEnvironmentConfig;
    TesunRestService sourceRestService;
    EnvironmentConfig targetEnvironmentConfig;
    TesunRestService targetRestService;

    public TesunHandlePostgrePropertis() throws PropertiesException {
        sourceEnvironmentConfig = new EnvironmentConfig(sourceEnvName);
        sourceRestService = new TesunRestService(sourceEnvironmentConfig.getRestServiceConfigsForMasterkonsole().get(0), this);
        targetEnvironmentConfig = new EnvironmentConfig(targetEnvName);
        targetRestService = new TesunRestService(targetEnvironmentConfig.getRestServiceConfigsForMasterkonsole().get(0), this);
    }

    private void extracted(TesunRestService sourceRestService, EnvironmentConfig sourceEnvironmentConfig) throws IOException {
        CteEnvironmentProperties cteEnvironmentProperties = sourceRestService.getEnvironmentProperties("", "", true);
        String fileName = sourceEnvironmentConfig.getCurrentEnvName() + "-PropertiesWithoutCteTestClient-" + PropertyUtil.getCurrentTiemStr() + ".txt";
        File propsFile = new File(rootDir, fileName);
        PropertyUtil.dumpProperties(cteEnvironmentProperties.getProperties(), propsFile);

        cteEnvironmentProperties = sourceRestService.getEnvironmentProperties("cteTestclient", "", false);
        fileName = sourceEnvironmentConfig.getCurrentEnvName() + "-PropertiesWithCteTestClient-" + PropertyUtil.getCurrentTiemStr() + ".txt";
        propsFile = new File(rootDir, fileName);
        PropertyUtil.dumpProperties(cteEnvironmentProperties.getProperties(), propsFile);
    }

    @Test
    public void readAllCfgSettingsForTarget() throws IOException {
        extracted(sourceRestService, sourceEnvironmentConfig);
        extracted(targetRestService, targetEnvironmentConfig);
    }

    @Test
    public void preparePropertiesForPostgreTest() throws IOException {
        Iterator<String> iterator = propertiesHolderMap.keySet().iterator();
        while (iterator.hasNext()) {
            preparePropertiesForPostgre(iterator.next());
        }
    }

    private void preparePropertiesForPostgre(String mapKey) throws IOException {
        SourcePropertiesHolder sourcePropertiesHolder = propertiesHolderMap.get(mapKey);

        // lese Properties aus der Quell-Umgebung und schreibe in die Datei...
        sourcePropertiesHolder.initSourcePropertiesHolder(sourceRestService);
        String fileName = sourceEnvironmentConfig.getCurrentEnvName() + "-Properties-" + sourcePropertiesHolder.getKeyFilter() + ".txt";
        File propsFile = new File(rootDir, fileName);
        PropertyUtil.dumpProperties(sourcePropertiesHolder.getProperties(), propsFile);

        // modifiziere Properties aus der Quelle und schreibe in die Ziel-Datei...
        PropertyReplacer propertyReplacer = replacerMap.get(mapKey);
        propertyReplacer.replaceProperties(sourcePropertiesHolder);
        fileName = targetEnvironmentConfig.getCurrentEnvName() + "-Properties-" + sourcePropertiesHolder.getKeyFilter() + ".txt";
        propsFile = new File(rootDir, fileName);
        PropertyUtil.dumpProperties(propertyReplacer.getProperties(), propsFile);

        // Properties in die Target-Umgenung setzen: zuerst müssen die Props aber restauriert werden!
        targetRestService.restoreEnvironmentProperties();
        propertyReplacer.uploadNewProperties(targetRestService);
        PropertyUtil.dumpProperties(propertyReplacer.getProperties(), propsFile);
    }

    @Override
    public void notifyClientJob(Level level, Object notifyObject) {
        System.out.println(notifyObject.toString());
    }

    @Override
    public Object askClientJob(ASK_FOR askFor, Object userObject) {
        return null;
    }
}
