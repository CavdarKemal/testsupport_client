package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import org.junit.Test;

public class LogFileParserForGEETest extends AbstractLogFileParserTest {

    public LogFileParserForGEETest() throws Exception {
        super("/log_analyse/gee-log4j-all-importcycle-out.log", "2024-09-20 10:54:46", "2024-09-20 11:41:48");
    }

    @Test
    public void filter() throws Exception {
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForGEETest#filter");
        runAndCheckDateInRange();
    }

}