package de.creditreform.crefoteam.cte.tesun.util;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Schnittstelle für Aufrufparameter / Konfiguration
 * Instanzen dieser Klasse werden nicht mehr verändert und sind threadsafe
 * User: ralf
 * Date: 11.02.14
 * Time: 12:56
 */
public interface TestSupportConfiguration {

    /**
     * Lese die Bezeichnung des auszuführenden Befehls
     */
    String getSelectedCommand();

    boolean containsKey(String key);

    EnvironmentConfig getEnvironmentConfig();

    /**
     * Lese den Wert für einen optionalen Parameter. Der Rückgabewert ist null,
     * wenn der Parameter nicht gesetzt ist
     *
     * @param key Key für den Parameter
     * @return Wert des optionalen Parameters oder null
     */
    String getOptionalString(String key);

    /**
     * Lese den Wert für einen optionalen Parameter. Der Rückgabewert ist der
     * als Default angegebene Wert, wenn der Parameter nicht gesetzt ist.
     *
     * @param key          Key für den Parameter
     * @param defaultValue Default-Wert, falls eine Vorgabe fehlt
     * @return Wert des optionalen Parameters oder Default-Value
     */
    String getOptionalString(String key, String defaultValue);

    /**
     * Lese den Wert für einen Pflicht-Parameter, fehlende Werte können
     * mit Hilfe eines Decorators gesammelt und ausgewertet werden.
     *
     * @param key Key für den Parameter
     * @return Value oder null
     */
    String getMandatoryString(String key);

    /**
     * Lese eine Liste von Optionen zum angegebenen Key
     *
     * @param key Key für den Parameter
     * @return Liste, ggf. leer, not-null
     */
    List<String> getOptionalStringList(String key);

    /**
     * Lese ein Flag aus der Konfiguration. Das Ergebnis is 'false', wenn
     * keine oder nicht erkannte Einstellungen zu dem angegebenen Key
     * vorhanden sind,
     *
     * @param key Key für den Parameter
     */
    boolean getBooleanDefaultFalse(String key);

    /**
     * Lese das zu verwendende Charset
     */
    Charset getCharset();

}
