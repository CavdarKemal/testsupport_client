package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import org.junit.Test;

public class LogFileParserForPRETest extends AbstractLogFileParserTest {

    public LogFileParserForPRETest() throws Exception {
        super("/log_analyse/pre-loesch-job.log", "2024-09-19 09:18:04", "2024-09-19 09:18:52");
    }

    @Test
    public void filterDisabledLoeschJobKonfigs() throws Exception {
        cut.addToInclusionLinesList("abgeschaltet für");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForPRETest#filterForLoeschJobDisabled()");
        runAndCheckDateInRange();
    }

    @Test
    public void testLOESCHUNG_ERFOLGREICH() throws Exception {
        cut.addToInclusionLinesList("LOESCHUNG_ERFOLGREICH");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForENETest#filterForLoeschJobAnalyse()");
        runAndCheckDateInRange();
    }

}