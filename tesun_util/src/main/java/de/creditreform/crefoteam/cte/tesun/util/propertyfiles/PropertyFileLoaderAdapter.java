package de.creditreform.crefoteam.cte.tesun.util.propertyfiles;

import org.slf4j.Logger;

/**
 * Abstrakte Basis-Klasse für den Adapter zwischen {@link PropertyFileLoader}
 * und {@link PropertyFileLoaderFunction} oder {@link PropertyFileLoaderBulkFunction}
 * <p>
 * User: ralf
 * Date: 05.05.14
 * Time: 15:09
 */
public abstract class PropertyFileLoaderAdapter<T extends PropertyFileLoaderFunctionBase>
        implements PropertyFileLoaderFunctionBase {

    private final T function;

    protected PropertyFileLoaderAdapter(T function) {
        this.function = function;
    }

    @Override
    public void init(Logger logger) throws Exception {
        getFunction().init(logger);
    }

    @Override
    public void shutdown() {
        getFunction().shutdown();
    }

    public T getFunction() {
        return function;
    }

    public abstract void reset();

    public abstract void collectOrProcess(PathInfo baseOutputPath, String testFallName, Long crefonummer)
            throws Exception;

    public abstract void processCollected()
            throws Exception;

}
