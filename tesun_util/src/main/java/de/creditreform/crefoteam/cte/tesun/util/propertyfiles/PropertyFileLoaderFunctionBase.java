package de.creditreform.crefoteam.cte.tesun.util.propertyfiles;

import org.slf4j.Logger;

/**
 * Basis-Interface mit den Lifecycle-Methoden von
 * PropertyFileLoaderFunction und PropertyFileLoaderBulkFunction
 * User: ralf
 * Date: 05.05.14
 * Time: 15:08
 */
public interface PropertyFileLoaderFunctionBase {
    /**
     * Initialisierung / Speichern des zu verwendenden Loggers
     */
    void init(Logger logger) throws Exception;

    /**
     * Herunterfahren
     */
    void shutdown();
}
