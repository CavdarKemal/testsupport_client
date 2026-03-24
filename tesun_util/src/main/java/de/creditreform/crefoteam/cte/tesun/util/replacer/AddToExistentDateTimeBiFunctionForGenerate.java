package de.creditreform.crefoteam.cte.tesun.util.replacer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;

public class AddToExistentDateTimeBiFunctionForGenerate implements BinaryOperator<String> {

    public static Pattern DATETIME_PATTERN_ADD_TO_EXISTING = Pattern.compile("<!-- [+-][0-9]{1,2}[JMT] -->[0-9]{4}-[0-9]{2}-[0-9]{2}.*<"); // <!-- -3J -->2019-03-29...<

    protected static class InternalDateContainer {
        private final DateFormat formatter;
        private final Calendar theCal;
        private final String postfix;

        public InternalDateContainer(DateFormat formatter, Calendar theCal, String postfix) {
            this.formatter = formatter;
            this.theCal = theCal;
            this.postfix = postfix;
        }

        public Calendar getTheCal() {
            return theCal;
        }

        public String getPostfix() {
            return postfix;
        }

        public String format() {
            StringBuilder sb = new StringBuilder();
            if (formatter != null && theCal != null) {
                final String dateAsString = formatter.format(theCal.getTime());
                if (dateAsString.length() <= 10) {
                    sb.append(dateAsString);
                } else {
                    sb.append(dateAsString, 0, 10);
                }
            }
            if (postfix != null) {
                sb.append(postfix);
            }
            return sb.toString();
        }

    }

    @Override
    public String apply(String origin, String matchingText) {
        final String[] split = matchingText.split("-->");
        if (split.length != 2) {
            throw new RuntimeException("Unerwartetes Token für Datum-Modifikator: " + matchingText);
        }
        final String strOp = split[0].substring(5);

        final InternalDateContainer parseResult = parseDate(split[1]);
        if (parseResult == null || parseResult.getTheCal() == null) {
            // nicht konvertierber, die unveränderten Eingangs-Daten werden zurück gegeben
            return matchingText;
        }

        final int iAmount;
        try {
            iAmount = Integer.parseInt(strOp.substring(0, strOp.length() - 2));
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Unerwartetes Token für Amount: " + strOp);
        }

        final int calendarField;
        char cTypeOfAmount = strOp.charAt(strOp.length() - 2);
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
            default: {
                throw new RuntimeException("Unerwartetes Token : " + cTypeOfAmount);
            }
        }
        parseResult.getTheCal().add(calendarField, iAmount);
        return parseResult.format();
    }

    protected InternalDateContainer parseDate(String inputText) {
        if (inputText == null || inputText.length() < 10) {
            // Kein Datums-Anteil in der Eingabe
            return null;
        } else {
            final String datePortion = inputText.substring(0, 10);
            final String postfix = inputText.substring(10);
            final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date parsedDate = formatter.parse(datePortion);
                final Calendar theCal = Calendar.getInstance();
                theCal.setTime(parsedDate);
                return new InternalDateContainer(formatter, theCal, postfix);
            } catch (ParseException e) {
                throw new RuntimeException("Unerwartetes Token für Datum: " + inputText, e);
            }
        }
    }
}
