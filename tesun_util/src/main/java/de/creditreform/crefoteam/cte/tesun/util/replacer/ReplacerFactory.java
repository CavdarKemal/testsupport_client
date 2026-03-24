package de.creditreform.crefoteam.cte.tesun.util.replacer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class ReplacerFactory {
    private static final Logger logger = LoggerFactory.getLogger(ReplacerFactory.class);

    public static final String MSG_CHARSET_NULL = "Charset must not be null";
    public static final String MSG_CREFO_10DIGITS = "Crefo should have 10 digits, supplied parameter: ";
    public static final String MSG_STRING_NULL = "Replacement and string-to-be-replaced must not be null";
    public static final String MSG_STRING_EMPTY = "string-to-be-replaced must not be empty (replacement-string might)";
    public static final String MSG_REPLACEMENT_CHANGED = "Replacement for string-to-be-replaced must not be ambiguous: ";
    public static final String MSG_SHORTER_REPLACEMENT_EXISTS = "Replacement for a shorter string-to-be-replaced already exists: ";
    public static final String MSG_LONGER_REPLACEMENT_EXISTS = "Replacement for a longer string-to-be-replaced already exists: ";
    public static final String PARAM_CONFLICT_SEPARATOR = "<->";

    enum EIGNER_VC_PATTERN_TYPE {
        EIGNER_CLZ_VC_PATTERN_AB30("<arc:clz-eigner-vc"), // AB30
        EIGNER_VC_PATTERN_REF_EXP("<eigner-vc"), // REF-EXP
        CLZ_EIGNER_VC_PATTERN_REF_EXP("<clz-eigner-vc"), // REF-EXP
        VC_CLZ_PATTERN("<vcClz"), // REF-EXP
        DATEN_VC_PATTERN1("<Daten-VC"), // REF-EXP
        DATEN_VC_PATTERN2("<datenVC"), // REF-EXP
        ZUST_DATENVC_PATTERN("<zustaendigerDatenVC"), // REF-EXP
        ZUST_VC_PATTERN("<zustaendigerVC"), // REF-EXP
        ZUST_DATEN_VC_PATTERN("<zustaendiger-daten-vc"); // REF-EXP

        private final Pattern pattern;

        EIGNER_VC_PATTERN_TYPE(String eignerVCTagPostfix) {
            pattern = Pattern.compile(eignerVCTagPostfix + ">\\d{3}</");
        }

        public Pattern getPattern() {
            return pattern;
        }
    }

    protected static final Map<String, Pattern> EIGNER_VC_PATTERNS_MAP = new HashMap<>();
    private final String charsetName;

    static {
        for (EIGNER_VC_PATTERN_TYPE eigner_vc_pattern_type : EIGNER_VC_PATTERN_TYPE.values()) {
            Pattern pattern = eigner_vc_pattern_type.getPattern();
            String key = pattern.pattern();
            EIGNER_VC_PATTERNS_MAP.put(key, pattern);
        }
    }

    public ReplacerFactory(Charset charset) {
        if (charset == null) {
            throw new ReplacerParameterException(MSG_CHARSET_NULL);
        }
        final String newCharsetName = charset.name();
        if (newCharsetName == null) {
            throw new ReplacerParameterException(MSG_CHARSET_NULL);
        }
        this.charsetName = newCharsetName;
    }

    /**
     * Definiere die Ersetzung einer 10-stelligen Crefo
     */
    public ReplacerFactory insertCrefoReplacement(Map<String, ReplacementMapping> replacementMappingMap, Long toBeReplaced, Long replacement) throws ReplacerParameterException {
        if (toBeReplaced == null || replacement == null) {
            throw new ReplacerParameterException(MSG_STRING_NULL);
        } else {
            if (replacementMappingMap.get(toBeReplaced.toString()) != null) {
                throw new ReplacerParameterException(MSG_REPLACEMENT_CHANGED + toBeReplaced);
            }
            ReplacementMapping replacementMapping = new ReplacementMapping(toBeReplaced);
            replacementMapping.setTargetCrefo(replacement);
            replacementMapping.setEignerVC(Integer.valueOf(-1));
            replacementMapping.setTagNameEignerVC("NOT-SET");
            replacementMappingMap.put(toBeReplaced.toString(), replacementMapping);
            return this;
        }
    }

    /**
     * Definiere die Ersetzung eines Strings durch einen anderen String
     */
    public ReplacerFactory insertStringReplacement(String toBeReplaced, String replacement) throws ReplacerParameterException {
        // TODO REPLCEMENT
        return this;
    }

    public Replacer create(Map<String, ReplacementMapping> replacementMappingMap, boolean forGenerate) {
        Map<Pattern, BiFunction<String, String, String>> patternMap = new LinkedHashMap<>();
        addBiFunctionCrefoReplacement(patternMap, replacementMappingMap);
        if (forGenerate) {
            addBiFunctionEignerVCForGenerate(patternMap, replacementMappingMap);
        } else {
            addBiFunctionEignerVCForRestore(patternMap, replacementMappingMap);
        }
        return new Replacer(patternMap, replacementMappingMap, charsetName);
    }

    protected static void addBiFunctionEignerVCForRestore(final Map<Pattern, BiFunction<String, String, String>> patternMap,
                                                          final Map<String, ReplacementMapping> replacemetMappingMap) {
        // Reverse-Ersetzungen für Eigner-VC übernehmen
        if (!replacemetMappingMap.isEmpty()) {
            // ...Das Pattern für die Reverse-Ersetzung ist konstant ... wir brauchen eine Map mit den gewünschten Ersetzungen
            for (Map.Entry<String, Pattern> eignerVCPatternMap : EIGNER_VC_PATTERNS_MAP.entrySet()) {
                Pattern eignerVCPattern = eignerVCPatternMap.getValue();
                patternMap.put(eignerVCPattern, new EignerVCBiFunctionForRestore(eignerVCPattern, replacemetMappingMap));
                //logger.debug("addBiFunctionEignerVCForRestore():\n\tPattern = {}", eignerVCPattern.pattern());
            }
        }
    }

    /**
     * Erzeuge ein Pattern mit {@link BiFunction} für die Ersetzung des Eigner-VC, verwendet nur bei Generate
     */
    public void addBiFunctionEignerVCForGenerate(final Map<Pattern, BiFunction<String, String, String>> patternMap,
                                                 final Map<String, ReplacementMapping> replacementMappingMap) {
        // Pattern für die Ersetzung des 'eignerVC' im Generate-Prozess, ersetzt wird durch einen festen String
        for (Map.Entry<String, Pattern> eignerVCPatternMap : EIGNER_VC_PATTERNS_MAP.entrySet()) {
            Pattern eignerVCPattern = eignerVCPatternMap.getValue();
            patternMap.put(eignerVCPattern, new EignerVCBiFunctionForGenerate(eignerVCPattern, replacementMappingMap));
            //logger.debug("addBiFunctionEignerVCForGenerate():\n\tPattern = {}", eignerVCPattern.pattern());
        }
    }

    /**
     * Erzeuge ein Pattern mit {@link BiFunction} für die Ersetzung der Crefonummern, verwendet bei Generate und Restore
     */
    public static void addBiFunctionCrefoReplacement(Map<Pattern, BiFunction<String, String, String>> patternMap, Map<String, ReplacementMapping> replacementMappingMap) {
        final Map<String, ReplacementMapping> crefoReplacementMap4Function = new TreeMap<>(replacementMappingMap);
        // Haupt-Anwendungsfall ist die Ersetzung von 10- bis 13-stelligen Zahlen...
        StringBuilder sb = new StringBuilder(15 * replacementMappingMap.size());
        for (String key : replacementMappingMap.keySet()) {
            if (key != null && key.length() > 0) {
                sb.append(key).append('|');
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        if (sb.length() > 0) {
            Pattern pattern = Pattern.compile(sb.toString());
            //logger.debug("addBiFunctionCrefoReplacement():\n\tPattern = {}", pattern);
            // Pattern für die Ersetzung aller angegebenen Crefonummern, ersetzt wird mit der Entsprechung aus der Map
            BiFunction<String, String, String> stringStringFunction = (origin, matchingText) -> {
                ReplacementMapping replacementMapping = crefoReplacementMap4Function.get(matchingText);
                return replacementMapping.getTargetCrefo().toString();
            };
            patternMap.put(pattern, stringStringFunction);
        }
    }

}
