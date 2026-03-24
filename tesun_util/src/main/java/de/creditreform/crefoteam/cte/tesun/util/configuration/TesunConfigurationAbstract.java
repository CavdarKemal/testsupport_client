package de.creditreform.crefoteam.cte.tesun.util.configuration;

import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportConfiguration;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstrakte Basisklasse für Implementierungen von {@link TestSupportConfiguration}
 * User: ralf
 * Date: 11.02.14
 * Time: 14:41
 */
public abstract class TesunConfigurationAbstract implements TestSupportConfiguration {
    private final Charset charset;
    private final String selectedCommand;
    private final EnvironmentConfig environmentConfig;

    public TesunConfigurationAbstract(Charset charset, EnvironmentConfig environmentConfig, String selectedCommand) {
        this.charset = charset;
        this.environmentConfig = environmentConfig;
        this.selectedCommand = selectedCommand;
    }

    @Override
    public EnvironmentConfig getEnvironmentConfig() {
        return environmentConfig;
    }

    @Override
    public final Charset getCharset() {
        return charset;
    }

    @Override
    public String getSelectedCommand() {
        return selectedCommand;
    }

    @Override
    public final String getOptionalString(String key) {
        return getOptionalString(key, null);
    }

    @Override
    public boolean getBooleanDefaultFalse(String key) {
        String selected = getOptionalString(key);
        return ("true".equalsIgnoreCase(selected) || "1".equals(selected));
    }

    /**
     * Alle Ableitungen dieser Klasse müssen Threadsafe sein, eine
     * Protokollierung fehlender Parameter ist hier nicht sinnvoll.
     */
    @Override
    public final String getMandatoryString(String key) {
        return getOptionalString(key);
    }

    protected final String removeNoneString(String value) {
        if (TestSupportClientKonstanten.OPT_VALUE_NONE.equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    protected List<String> removeNoneString(List<?> valueList) {
        List<String> result = new ArrayList<>(valueList.size());
        for (Object v : valueList) {
            if (v != null) {
                String stringValue = removeNoneString(v.toString());
                if (!stringValue.isBlank()) {
                    result.add(stringValue);
                }
            }
        }
        return result;
    }

}
