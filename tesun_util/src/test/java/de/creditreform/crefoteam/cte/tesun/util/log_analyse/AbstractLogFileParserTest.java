package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public abstract class AbstractLogFileParserTest {
    File logFile;
    protected LogFileParser cut;

    public AbstractLogFileParserTest(String logFilePath, String strFromDate, String strToDate) throws Exception {
        URL resource = this.getClass().getResource(logFilePath);
        if (resource == null) {
            Assert.fail("Die Test-Resource '" + logFilePath + "' existiert nicht im Calss-Path");
        }
        logFile = new File(resource.toURI().getPath());

        Date fromDate = DateUtils.parseDate(strFromDate, new String[]{TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.toPattern()});
        Date toDate = DateUtils.parseDate(strToDate, new String[]{TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.toPattern()});
        cut = new LogFileParser(logFile, fromDate, toDate);
    }

    @Before
    public void setUp() throws Exception {
    }

    protected void runAndCheckDateInRange() throws Exception {
        List<String> filteredLogLines = cut.filterLogFile(logFile);
        if (!filteredLogLines.isEmpty()) {
            String strDate = filteredLogLines.get(0);
            try {
                Date dateFromLine = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse(strDate.substring(0, 19));
                Assert.assertTrue(!dateFromLine.before(cut.getFromDate()) && !dateFromLine.after(cut.getToDate()));
            } catch (ParseException e) {
                // is OK, da nicht jede Zeile ein Datum ergeben muss!
            }
        } else {
            LogFileParser.LOGGER.warn("Gefilterte Datei ist leer!");
        }
    }

    protected void runAndCheckExclusionList(List<String> exclusionList) throws Exception {
        List<String> filteredLogLines = cut.filterLogFile(logFile);
        for (String strCheck : exclusionList) {
            for (String filteredLine : filteredLogLines) {
                Assert.assertFalse(filteredLine.contains(strCheck));
            }
        }
    }

    protected void runAndCheckInclusionList(List<String> inclusionList) throws Exception {
        List<String> filteredLogLines = cut.filterLogFile(logFile);
        for (String strCheck : inclusionList) {
            boolean contains = false;
            for (String filteredLine : filteredLogLines) {
                contains |= filteredLine.contains(strCheck);
            }
            Assert.assertTrue("Gesuchter String '" + strCheck + "' in den gefilterten Zeilen nicht gefunden!", contains);
        }
    }

}