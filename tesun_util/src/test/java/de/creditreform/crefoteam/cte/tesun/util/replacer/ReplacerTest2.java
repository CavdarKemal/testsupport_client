package de.creditreform.crefoteam.cte.tesun.util.replacer;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class ReplacerTest2 {
    public static final String CREFO_FILENAME_SOURCE = "./src/test/resources/date_patterned_2010121119.xml";
    public static final String CREFO_FILENAME_TARGET = "./target/replaced_2010121119.xml";

    private String doReplaceDatePatterns(InputStream inputStream) throws IOException {
/* TODO REPLACEMENT
        Map<String, String> crefoReplacementMap = new HashMap<>();
        Map<Pattern, BiFunction<String, String, String>> patternMap = new HashMap<>();
        patternMap.put(AddToCurrentDateTimeBiFunctionForGenerate.DATETIME_PATTERN_ADD_TO_CURRENT, new AddToCurrentDateTimeBiFunctionForGenerate());
        patternMap.put(AddToExistentDateTimeBiFunctionForGenerate.DATETIME_PATTERN_ADD_TO_EXISTING, new AddToExistentDateTimeBiFunctionForGenerate());
        patternMap.put(AddToCurrentAmountBiFunctionForGenerate.PATTERN_ADD_TO_CURRENT, new AddToCurrentAmountBiFunctionForGenerate());
        patternMap.put(AddToExistentAmountBiFunctionForGenerate.PATTERN_ADD_TO_EXISTENT, new AddToExistentAmountBiFunctionForGenerate());
        Map<String, String> eignerVCOriginalMap = new HashMap<>();
        Replacer cut = new Replacer(patternMap, crefoReplacementMap, eignerVCOriginalMap, "UTF-8");
        OutputStream outputStream = new ByteArrayOutputStream();
        cut.copyAndReplace("", inputStream, outputStream);
        return outputStream.toString();
        */
        return "TODO REPLACEMENT";
    }

    @Test
    public void testReplacerForDatePatternsX() throws IOException {
        String strReplaced = doReplaceDatePatterns(new FileInputStream(CREFO_FILENAME_SOURCE));
        IOUtils.write(strReplaced, new FileOutputStream(CREFO_FILENAME_TARGET));
    }

    @Test
    public void testReplacerForDatePatterns() throws IOException, ParseException {
        String inputString = "" +
            "<a>D-2J</a>\n" + "<a>D+3J</a>\n" + "<a>D-2M</a>\n" + "<a>D+6M</a>\n" + "<a>D-12T</a>\n" + "<a>D+16T</a>\n" +
            "<a>T-2J</a>\n" + "<a>T+3J</a>\n" + "<a>T-2M</a>\n" + "<a>T+6M</a>\n" + "<a>T-12T</a>\n" + "<a>T+16T</a>\n" +
            "<a><!-- -9J -->2015-11-26T14:50:17.920+01:00</a>\n" + "<a><!-- +4J -->2015-11-26T14:50:17.920+01:00</a>\n" +
            "<a><!-- -2M -->2015-11-26T14:50:17.920+01:00</a>\n" + "<a><!-- +6M -->2015-11-26T14:50:17.920+01:00</a>\n" +
            "<a><!-- -7T -->2015-11-26T14:50:17.920+01:00</a>\n" + "<a><!-- +9T -->2015-11-26T14:50:17.920+01:00</a>\n" +
            "<a>-4J</a>\n" + "<a>+1J</a>\n" + "<a>-8M</a>\n" + "<a>+2M</a>\n" + "<a>-22T</a>\n" + "<a>+14T</a>\n" +
            "<a><!-- -2 -->2013</a>\n" + "<a><!-- +2 -->10</a>\n" + "<a><!-- -22 -->5</a>\n" +
            "";
        String strReplaced = doReplaceDatePatterns(new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8)));

        Assert.assertFalse(strReplaced.isEmpty());
        String[] outLines = strReplaced.split("\n");
        Calendar refCal = Calendar.getInstance();
        checkDateTimeToRefDate(outLines[0], Calendar.YEAR, -2, refCal); // "<a>D-2J</a>"
        checkDateTimeToRefDate(outLines[1], Calendar.YEAR, +3, refCal); // "<a>D+3J</a>"
        checkDateTimeToRefDate(outLines[2], Calendar.MONTH, -2, refCal); // "<a>D-2M</a>"
        checkDateTimeToRefDate(outLines[3], Calendar.MONTH, +6, refCal); // "<a>D+6M</a>"
        checkDateTimeToRefDate(outLines[4], Calendar.DAY_OF_MONTH, -12, refCal); // "<a>D-12T</a>"
        checkDateTimeToRefDate(outLines[5], Calendar.DAY_OF_MONTH, +16, refCal); // "<a>D+16T</a>"

        checkDateTimeToRefDate(outLines[6], Calendar.YEAR, -2, refCal);  // "<a>T-2J</a>"
        checkDateTimeToRefDate(outLines[7], Calendar.YEAR, +3, refCal);  // "<a>T+3J</a>"
        checkDateTimeToRefDate(outLines[8], Calendar.MONTH, -2, refCal);  // "<a>T-2M</a>"
        checkDateTimeToRefDate(outLines[9], Calendar.MONTH, +6, refCal);  // "<a>T+6M</a>"
        checkDateTimeToRefDate(outLines[10], Calendar.DAY_OF_MONTH, -12, refCal); // "<a>T-12T</a>"
        checkDateTimeToRefDate(outLines[11], Calendar.DAY_OF_MONTH, +16, refCal); // "<a>T+16T</a>"

        refCal.setTime(TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM.parse("2015-11-26 14:50:17"));
        checkDateTimeToRefDate(outLines[12], Calendar.YEAR, -9, refCal); // <a><!-- -9J -->2015-11-26T14:50:17...
        checkDateTimeToRefDate(outLines[13], Calendar.YEAR, +4, refCal); // <a><!-- +4J -->2015-11-26T14:50:17...
        checkDateTimeToRefDate(outLines[14], Calendar.MONTH, -2, refCal); // <a><!-- -2M -->2015-11-26T14:50:17...
        checkDateTimeToRefDate(outLines[15], Calendar.MONTH, +6, refCal); // <a><!-- +6M -->2015-11-26T14:50:17...
        checkDateTimeToRefDate(outLines[16], Calendar.DAY_OF_MONTH, -7, refCal); // <a><!-- -7T -->2015-11-26T14:50:17...
        checkDateTimeToRefDate(outLines[17], Calendar.DAY_OF_MONTH, +9, refCal); // <a><!-- +9T -->2015-11-26T14:50:17...

        refCal = Calendar.getInstance();
        checkCalendarField(outLines[18], Calendar.YEAR, -4, refCal); // <a>-4J</a>
        checkCalendarField(outLines[19], Calendar.YEAR, +1, refCal); // <a>+1J</a>
        checkCalendarField(outLines[20], Calendar.MONTH, -8, refCal); // <a>-8M</a>
        checkCalendarField(outLines[21], Calendar.MONTH, +2, refCal); // <a>+2M</a>
        checkCalendarField(outLines[22], Calendar.DAY_OF_MONTH, -22, refCal); // <a>-22T</a>
        checkCalendarField(outLines[23], Calendar.DAY_OF_MONTH, +14, refCal); // <a>+14T</a>

        checkAmountField(outLines[24], -2, 2013); // <a><!-- -2 -->2013</a>
        checkAmountField(outLines[25], +2, 10); // <a><!-- +2 -->10</a>
        checkAmountField(outLines[26], -22, 5); // <a><!-- -22 -->5</a>
    }

    private void checkAmountField(String strFieldContent, int amount, int refAmount) {
        String strValue = strFieldContent.substring(3, strFieldContent.indexOf("</"));
        int iValue = Integer.valueOf(strValue).intValue();
        Assert.assertEquals(iValue, refAmount + amount);
    }

    private void checkCalendarField(String strFieldContent, int calendarField, int amount, Calendar refCal) {
        String strFieldValue = strFieldContent.substring(3, strFieldContent.indexOf("<", 3));
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(refCal.getTime());
        tempCal.add(calendarField, amount);
        int calFieldValue = tempCal.get(calendarField);
        Assert.assertEquals(strFieldValue, "" + calFieldValue);
    }

    private void checkDateTimeToRefDate(String strDateNew, int calendarField, int amount, Calendar refCal) throws ParseException {
        Calendar testCal = Calendar.getInstance();
        final Date parsedDate;
        if (strDateNew.contains("T")) {
            parsedDate = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM.parse(strDateNew.substring(3, 22).replace("T", " "));
        } else {
            parsedDate = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD.parse(strDateNew.substring(3, 13));
        }
        testCal.setTime(parsedDate);

        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(refCal.getTime());
        tempCal.add(calendarField, amount);
        Assert.assertEquals(tempCal.get(calendarField), testCal.get(calendarField));
    }

}
