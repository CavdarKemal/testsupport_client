package de.creditreform.crefoteam.cte.tesun.util.configuration;

import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportConfiguration;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decorator für Instanzen von {@link TestSupportConfiguration}, der fehlende Pflichtfelder sammelt
 * User: ralf
 * Date: 14.02.14
 * Time: 12:39
 */
public class TesunConfigurationDecoratorCollectMissing implements TestSupportConfiguration {

    private final String missingStringMarker;
    private final TestSupportConfiguration wrapped;
    private final List<String> missingMandatoryParameters;

    public TesunConfigurationDecoratorCollectMissing(TestSupportConfiguration wrappedConfiguration) {
        this.missingStringMarker = "missing";
        this.wrapped = wrappedConfiguration;
        this.missingMandatoryParameters = new ArrayList<>();
    }

    public List<String> getMissingParameters() {
        return Collections.unmodifiableList(missingMandatoryParameters);
    }

    @Override
    public String getSelectedCommand() {
        return wrapped.getSelectedCommand();
    }

    @Override
    public boolean containsKey(String key) {
        return wrapped.containsKey(key);
    }

    @Override
    public String getOptionalString(String key) {
        return wrapped.getOptionalString(key);
    }

    @Override
    public String getOptionalString(String key, String defaultValue) {
        return wrapped.getOptionalString(key, defaultValue);
    }

    @Override
    public List<String> getOptionalStringList(String key) {
        return wrapped.getOptionalStringList(key);
    }

    @Override
    public boolean getBooleanDefaultFalse(String key) {
        return wrapped.getBooleanDefaultFalse(key);
    }

    @Override
    public String getMandatoryString(String key) {
        String value = wrapped.getOptionalString(key, missingStringMarker);
        // Hier ist nicht die Gleichheit zweier Strings entscheidend. Vielmehr
        // soll die Identität mit dem Instanz-eigenen missingStringMarker
        // geprüft werden.
        //noinspection StringEquality
        if (value != missingStringMarker) {
            return value;
        } else {
            this.missingMandatoryParameters.add(key);
            return null;
        }
    }

    @Override
    public Charset getCharset() {
        return wrapped.getCharset();
    }

    @Override
    public EnvironmentConfig getEnvironmentConfig() {
        return wrapped.getEnvironmentConfig();
    }

}
