package de.creditreform.crefoteam.cte.tesun.util.propertyfiles;

import de.creditreform.crefoteam.cte.tesun.util.NameCrefoPfad;

/**
 * Ableitung von {@link PropertyFileLoaderAdapter} für die Verarbeitung
 * einzelner Paare aus Testfall-Bezeichnung und Crefonummer
 * User: ralf
 * Date: 05.05.14
 * Time: 15:11
 */
public class PropertyFileLoaderAdapterSingle
        extends PropertyFileLoaderAdapter<PropertyFileLoaderFunction> {

    public PropertyFileLoaderAdapterSingle(PropertyFileLoaderFunction function) {
        super(function);
    }

    @Override
    public void reset() {
        // intentionally empty
    }

    @Override
    public void collectOrProcess(PathInfo baseOutputPath, String testFallName, Long crefonummer)
            throws Exception {
        getFunction().apply(new NameCrefoPfad(baseOutputPath, testFallName, crefonummer));
    }

    @Override
    public void processCollected() {
        // intentionally empty
    }
}
