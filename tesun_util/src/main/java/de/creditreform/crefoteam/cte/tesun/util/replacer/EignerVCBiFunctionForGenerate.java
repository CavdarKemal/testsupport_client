package de.creditreform.crefoteam.cte.tesun.util.replacer;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EignerVCBiFunctionForGenerate implements BiFunction<String, String, String> {
    private static final Logger logger = LoggerFactory.getLogger(EignerVCBiFunctionForGenerate.class);
    private final String generateFormat;
    private final Map<String, ReplacementMapping> replacementMappingMap;

    public EignerVCBiFunctionForGenerate(Pattern eignerVCPattern, Map<String, ReplacementMapping> replacementMappingMap) {
        this.generateFormat = eignerVCPattern.pattern().replace("\\d{3}", "%d");
        this.replacementMappingMap = replacementMappingMap;
    }

    @Override
    public String apply(String origin, String matchingText) {
        if (!origin.isBlank()) {
            Matcher matcher = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(origin);
            if (matcher.find()) {
                String originalCrefo = matcher.group();
                // logger.info("EignerVCBiFunctionForGenerate#apply:: Erzeugung des Ersatz-Textes {}", matchingText);
                // Wir erwarten zwingend ein Standard-Format für die Dateinamen und schneiden daraus die ursprüngliche Crefonummer heraus...
                ReplacementMapping replacementMapping = replacementMappingMap.get(originalCrefo);
                if (replacementMapping == null) {
                    throw new RuntimeException("Scheisseeee!!!!!");
                }
                Long pseudoCrefo = replacementMapping.getTargetCrefo();
                // Erzeugung des Ersatz-Textes
                String eignerVcReplacement = String.format(generateFormat, 412);
                if (pseudoCrefo != null) {
                    int from = matchingText.indexOf(">");
                    int to = matchingText.indexOf("<", from);
                    String strEignerVC = matchingText.substring(from + 1, to);
                    String thePattern = matchingText.substring(0, matchingText.indexOf(">"));
                    replacementMapping.setTagNameEignerVC(thePattern);
                    replacementMapping.setEignerVC(Integer.valueOf(strEignerVC));
                    int targetEignerVC = getReplacementForEignerVC(pseudoCrefo);
                    eignerVcReplacement = String.format(generateFormat, targetEignerVC);
                    // logger.info("EignerVCBiFunctionForGenerate#apply:: {} -> {}", matchingText, eignerVcReplacement);
                } else {
                    logger.warn("addBiFunctionEignerVCForGenerate#apply({},{})-> keine Ersetzung, da im crefoReplacementMap kein entsprechendes Element für existiert!", origin, matchingText);
                }
                return eignerVcReplacement;
            } else {
                logger.warn("addBiFunctionEignerVCForGenerate#apply({},{})-> keine Ersetzung, da <origin> keine Crefo enthält!", origin, matchingText);
            }
        }
        logger.warn("addBiFunctionEignerVCForGenerate#apply({},{})-> keine Ersetzung, " + "da <origin> nicht gesetzt ist!", origin, matchingText);
        return null;
    }

    private int getReplacementForEignerVC(Long newCrefoNumeric) {
        int targetClz = 412;
        if (newCrefoNumeric >= 9000000000L) {
            targetClz = 912; // für AT
/* TODO einschalten, wenn LU und CH unterstützt werden!
            if (newCrefoNumeric >= 9370000000L) {
                targetClz = 938; // für LU
                if (newCrefoNumeric >= 9390000000L) {
                    targetClz = 940; // für CH?
                }
            }
*/
        }
        return targetClz;
    }
}
