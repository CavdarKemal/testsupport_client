package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.propertyfiles.PropertyFileLoaderFunction;
import org.apache.log4j.Level;
import org.slf4j.Logger;

/**
 * Basis-Klasse für die Implementierungen von {@link PropertyFileLoaderFunction}
 * mit Zugriff auf einen Restservice
 * User: ralf
 * Date: 26.02.14
 * Time: 12:54
 */
public abstract class PropertyFileLoaderFunctionRestAbstract implements PropertyFileLoaderFunction {
    private Logger logger;

    protected final EnvironmentConfig environmentConfig;
    protected final TesunClientJobListener tesunClientJobListener;
    protected final TesunRestService tesunRestServiceWLS;
    protected final TesunRestService tesunRestServiceJVMImportC;

    public PropertyFileLoaderFunctionRestAbstract(EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) throws PropertiesException {
        this.environmentConfig = environmentConfig;
        this.tesunClientJobListener = tesunClientJobListener;
        tesunRestServiceWLS = new TesunRestService(environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), tesunClientJobListener);
        tesunRestServiceJVMImportC = new TesunRestService(environmentConfig.getRestServiceConfigsForJvmImpCycle().get(0), tesunClientJobListener);
    }

    public Logger getLogger() {
        return logger;
    }

    protected void notifyTesunClientJobListener(Level level, Object notifyObject) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyObject);
        } else if (getLogger() != null) {
            getLogger().info(notifyObject.toString()); // TODO abh. vom Level!!!
        }
    }

    @Override
    public void init(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void shutdown() {
        // intentionally empty
    }

}
