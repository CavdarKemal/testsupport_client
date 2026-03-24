package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Date;

public class LogFileParserForENETest {
    File logFile;
    protected LogFileParser cut;

    public LogFileParserForENETest() throws Exception {

        String logFilePath = "/log_analyse/ene-log4j-all-importcycle-out.log";
        URL resource = this.getClass().getResource(logFilePath);
        if (resource == null) {
            Assert.fail("Die Test-Resource '" + logFilePath + "' existiert nicht im Calss-Path");
        }
        logFile = new File(resource.toURI().getPath());
        String strFromDate = "2024-09-19 09:50:53";
        String strToDate = "2024-09-19 09:51:10";
        Date fromDate = DateUtils.parseDate(strFromDate, new String[]{TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.toPattern()});
        Date toDate = DateUtils.parseDate(strToDate, new String[]{TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.toPattern()});
        cut = new LogFileParser(logFile, fromDate, toDate);
    }

    @Test
    public void testAnzahlDerGefundenenVerzeichnisse() throws Exception {
        cut.addToInclusionLinesList("Anzahl der gefundenen Verzeichnisse");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForENETest#filterForLoeschJobAnalyse()");
        cut.filterLogFile(logFile);
    }

    @Test
    public void testLOESCHUNG_ERFOLGREICH() throws Exception {
        cut.addToInclusionLinesList("LOESCHUNG_ERFOLGREICH");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForENETest#filterForLoeschJobAnalyse()");
        cut.filterLogFile(logFile);
    }

}