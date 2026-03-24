package de.creditreform.crefoteam.cte.tesun.util.propertyfiles;

import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;

import java.util.List;

/**
 * Schnittstelle für die Blockweise-Übermittlung der gefundenen Treffer
 * durch die Klasse {@link PropertyFileLoader}
 * User: ralf
 * Date: 05.05.14
 * Time: 14:58
 */
public interface PropertyFileLoaderBulkFunction
        extends PropertyFileLoaderFunctionBase {

    /**
     * Abarbeiten der Funktionalität für eine Menge von Paaren, jedes einzelne
     * Paar besteht aus Dateipfad und Crefonummer
     *
     * @param baseDirKey     Verzeichnis-Name der Property-Datei
     * @param testCrefosList Liste mit Testfall-Beschreibungen
     * @throws Exception
     */
    void applyBulk(PathInfo baseDirKey, List<TestCrefo> testCrefosList) throws Exception;

}
