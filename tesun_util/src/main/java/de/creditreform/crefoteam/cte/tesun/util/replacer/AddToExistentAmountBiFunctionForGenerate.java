package de.creditreform.crefoteam.cte.tesun.util.replacer;

import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class AddToExistentAmountBiFunctionForGenerate implements BiFunction<String, String, String> {
    public static Pattern PATTERN_ADD_TO_EXISTENT = Pattern.compile("><!-- [+-][0-9]{1,2} -->[0-9]{1,4}<"); // <a><!-- -2 -->2013</a>
    int faktor = 0;
    int iAmount = 0;
    int oldAmount = 0;

    private void setLocalValues(String matchingText) {
        int indexOfSign = matchingText.indexOf("+", 5); // hinter dem "<!-- "
        if (indexOfSign > -1) {
            faktor = 1;
        } else {
            indexOfSign = matchingText.indexOf("-", 5);
            if (indexOfSign > -1) {
                faktor = -1;
            } else {
                throw new RuntimeException("Platzhlater für Vorzeichen ungültig!");
            }
        }
        int endIndex = matchingText.indexOf(" -->");
        String strAmount = matchingText.substring(indexOfSign + 1, endIndex);
        iAmount = Integer.valueOf(strAmount).intValue();
        String strOldValue = matchingText.substring(endIndex + 4, matchingText.indexOf("<", endIndex));
        oldAmount = Integer.valueOf(strOldValue).intValue();
    }

    @Override
    public String apply(String origin, String matchingText) {
        setLocalValues(matchingText);
        return ">" + (oldAmount + faktor * iAmount) + "<";
    }

}
