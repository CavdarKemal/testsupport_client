package de.creditreform.crefoteam.cte.tesun.util.configuration;

import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportConfiguration;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementierung von {@link TestSupportConfiguration} mit einem Properties-Backend
 * User: ralf
 * Date: 10.02.16
 */
public class TesunConfigurationProperties extends TesunConfigurationAbstract {
    public static final String SPLIT_PATTERN_REGEX = "[;,]";

    private final Pattern pattern;
    private final Map<String, String> properties = new HashMap<>();

    public TesunConfigurationProperties(Charset charset, EnvironmentConfig environmentConfig, String selectedCommand) {
        super(charset, environmentConfig, selectedCommand);
        this.pattern = Pattern.compile(SPLIT_PATTERN_REGEX);
    }

    @Override
    public boolean containsKey(String key) {
        return this.properties.containsKey(key);
    }

    @Override
    public String getOptionalString(String key, String defaultValue) {
        final String storedValue = this.properties.get(key);
        if (storedValue == null) {
            return defaultValue;
        } else {
            return removeNoneString(storedValue);
        }
    }

    @Override
    public List<String> getOptionalStringList(String key) {
        final List<String> result;
        final String storedValue = this.properties.get(key);
        if (storedValue == null || storedValue.length() == 0) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<>();
            for (String splitted : pattern.split(storedValue)) {
                if (removeNoneString(splitted) != null && splitted.length() > 0) {
                    result.add(splitted);
                }
            }
        }
        return result;
    }

}
