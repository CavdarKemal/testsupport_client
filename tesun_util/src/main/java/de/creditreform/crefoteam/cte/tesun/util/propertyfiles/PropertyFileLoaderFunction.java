package de.creditreform.crefoteam.cte.tesun.util.propertyfiles;

import de.creditreform.crefoteam.cte.tesun.util.NameCrefoPfad;

/**
 * Schnittstelle für die Übermittlung der gefundenen Treffer durch
 * die Klasse {@link PropertyFileLoader}
 * User: ralf
 * Date: 26.02.14
 * Time: 11:05
 */
public interface PropertyFileLoaderFunction
        extends PropertyFileLoaderFunctionBase {

    /**
     * Abarbeiten der Funktionalität für ein einzelnes Paar aus Dateipfad
     * und Crefonummer
     *
     * @param input Daten zum Testfall inkl. Dateipfad
     * @throws Exception
     */
    void apply(NameCrefoPfad input) throws Exception;

}
