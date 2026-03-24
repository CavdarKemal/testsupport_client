package de.creditreform.crefoteam.cte.tesun.util.replacer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AddToExistentDateTimeBiFunctionForGenerateTest {

    private SimpleDateFormat simpleDateFormat;

    @Before
    public void setUp() {
        simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    }

    @Test
    public void testParseDate() {
        checkParseDate("2019-03-29T12:18:05.028+01:00", "2019-03-29T12:18:05.028+01:00");
        checkParseDate("2015-11-26+01:00", "2015-11-26+01:00");
    }

    protected void checkParseDate(String inputDateStr, String expectedDateStr) {
        AddToExistentDateTimeBiFunctionForGenerate cut = new AddToExistentDateTimeBiFunctionForGenerate();
        AddToExistentDateTimeBiFunctionForGenerate.InternalDateContainer res = cut.parseDate(inputDateStr);
        if (expectedDateStr == null) {
            Assert.assertTrue("Ergebnis sollte null sein", res == null || res.getTheCal() == null);
        } else {
            Assert.assertFalse("Ergebnis sollte ungleich null sein für " + inputDateStr, res == null || res.getTheCal() == null);
            Assert.assertEquals(expectedDateStr, res.format());
        }
    }

    @Test
    public void testDateTimeBiFunction() throws ParseException, DatatypeConfigurationException {
        checkCalendar("<!-- -3J -->2015-11-26+01:00", "2012-11-26+01:00");
        checkCalendar("<!-- 3J -->2019-03-29T12:18:05.028+01:00", "2022-03-29T12:18:05.028+01:00");
        checkCalendar("<!-- -11M -->2015-11-26+01:00", "2014-12-26+01:00");
        checkCalendar("<!-- 9M -->2019-03-29T12:18:05.028+01:00", "2019-12-29T12:18:05.028+01:00");
        checkCalendar("<!-- -23T -->2015-11-26+01:00", "2015-11-03+01:00");
        checkCalendar("<!-- 15T -->2019-03-29T12:18:05.028+01:00", "2019-04-13T12:18:05.028+01:00");
    }

    protected void checkCalendar(String inputDateStr, String expectedDateStr) throws ParseException, DatatypeConfigurationException {
        AddToExistentDateTimeBiFunctionForGenerate cut = new AddToExistentDateTimeBiFunctionForGenerate();
        System.out.print("Test mit '" + inputDateStr + "'");
        final String strDate = cut.apply("", inputDateStr);
        System.out.println(" --> Result: " + strDate);
        Assert.assertEquals("Datum wurde nicht korrekt geparst!", expectedDateStr, strDate);
    }

}
