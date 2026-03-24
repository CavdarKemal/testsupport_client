package de.creditreform.crefoteam.cte.tesun.util.replacer;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class AddToCurrentAmountBiFunctionForGenerate implements BiFunction<String, String, String> {
    public static Pattern PATTERN_ADD_TO_CURRENT = Pattern.compile(">[+-][0-9]{1,2}[JMT]<");
    int faktor = 0;
    int iAmount = 0;
    int calendarField = 0;

    private void setLocalValues(String matchingText) {
        int indexOfSign = matchingText.indexOf("+");
        if (indexOfSign > -1) {
            faktor = 1;
        } else {
            indexOfSign = matchingText.indexOf("-");
            if (indexOfSign > -1) {
                faktor = -1;
            } else {
                throw new RuntimeException("Platzhlater für Vorzeichen ungültig!");
            }
        }
        int indexOfAmount = matchingText.indexOf("T", indexOfSign);
        if (indexOfAmount < 0) {
            indexOfAmount = matchingText.indexOf("M", indexOfSign);
            if (indexOfAmount < 0) {
                indexOfAmount = matchingText.indexOf("J", indexOfSign);
                if (indexOfAmount < 0) {
                    throw new RuntimeException("Platzhlater füc Calendar-Field ungültig!");
                }
            }
        }
        if (indexOfAmount > 0) {
            String strAmount = matchingText.substring(indexOfSign + 1, indexOfAmount);
            iAmount = Integer.valueOf(strAmount).intValue();
        }
        char cTypeOfAmount = matchingText.charAt(indexOfAmount);
        switch (cTypeOfAmount) {
            case 'T':
                calendarField = Calendar.DAY_OF_MONTH;
                break;
            case 'M':
                calendarField = Calendar.MONTH;
                break;
            case 'J':
                calendarField = Calendar.YEAR;
                break;
            default:
                throw new RuntimeException("Platzhlater füc Calendar-Field ungültig!");
        }
    }

    @Override
    public String apply(String origin, String matchingText) {
        setLocalValues(matchingText);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeZone(TimeZone.getDefault());
        nowCal.add(calendarField, faktor * iAmount);
        String result = ">" + nowCal.get(calendarField) + "<";
        return result;
    }

}
