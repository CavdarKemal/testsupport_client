package de.creditreform.crefoteam.cte.tesun.util.replacer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.BiFunction;

public class AddToCurrentDateTimeBiFunctionForGenerate implements BiFunction<String, String, String> {
    SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    int faktor = 0;
    int iAmount = 0;
    int calendarField = 0;

    private void setLocalValues(String matchingText) {
        if (matchingText.startsWith("D")) {
            // 2015-11-26+01:00
            DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-ddXXX", Locale.getDefault());
        } else if (matchingText.startsWith("T")) {
            // 2015-11-26T14:37:02.294+01:00
            DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        } else {
            throw new RuntimeException("Datum-Paltzhalter ungültig!");
        }
        int indexOfSign = matchingText.indexOf("+", 1);
        if (indexOfSign > 0) {
            faktor = 1;
        } else {
            indexOfSign = matchingText.indexOf("-", 1);
            if (indexOfSign > 0) {
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
        String result = DATE_FORMATTER.format(nowCal.getTime());
        return result;
    }

}
