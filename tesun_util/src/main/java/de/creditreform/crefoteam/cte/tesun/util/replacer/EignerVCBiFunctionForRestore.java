package de.creditreform.crefoteam.cte.tesun.util.replacer;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EignerVCBiFunctionForRestore implements BiFunction<String, String, String> {
    private static final Logger logger = LoggerFactory.getLogger(EignerVCBiFunctionForRestore.class);
    final String reverseFormat;
    final Map<String, ReplacementMapping> replacemetMappingMap;

    public EignerVCBiFunctionForRestore(Pattern reversePattern, final Map<String, ReplacementMapping> replacemetMappingMap) {
        this.reverseFormat = reversePattern.pattern().replace("\\d{3}", "%d");
        this.replacemetMappingMap = replacemetMappingMap;
    }

    @Override
    public String apply(String origin, String matchingText) {
        if (!origin.isBlank()) {
            Matcher matcher = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(origin);
            if (matcher.find()) {
                String key = matcher.group();
                ReplacementMapping replacementMapping = replacemetMappingMap.get(key);
                if (replacementMapping != null) {
                    String replacement = String.format(reverseFormat, replacementMapping.getEignerVC());
                    //logger.info("EignerVCBiFunctionForRestore#apply({},{})-> Ersetzung: {}", matchingText, key, replacement);
                    return replacement;
                }
                logger.warn("EignerVCBiFunctionForRestore#apply({},{})-> keine Ersetzung, da im eignerVCReverseReplacementMap kein entsprechendes Element existiert!", matchingText, key);
            } else {
                logger.warn("EignerVCBiFunctionForRestore#apply({},{})-> keine Ersetzung da keine Entsprechung aus der Map eignerVCReverseReplacementMap liefert!", matchingText, origin);
            }
            return null;
        }
        logger.warn("EignerVCBiFunctionForRestore#apply():Keine Ersetzung für {}, da <origin> nicht gesetzt ist!", origin);
        return null;
    }
}
