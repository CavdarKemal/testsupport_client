package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import org.junit.Test;

public class LogFileParserForABETest extends AbstractLogFileParserTest {

    public LogFileParserForABETest() throws Exception {
        super("/log_analyse/abe-log4j-all-importcycle-out.log", "2024-09-20 11:39:00", "2024-09-20 11:41:39");
    }

    @Test
    public void filter() throws Exception {
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForABETest#filter");
        runAndCheckDateInRange();
    }

}