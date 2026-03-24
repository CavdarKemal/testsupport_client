package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import org.junit.Test;

public class LogFileParserForBaseTest extends AbstractLogFileParserTest {
    public LogFileParserForBaseTest() throws Exception {
        super("/log_analyse/loesch-job.log", "2024-09-19 10:35:20", "2024-09-19 10:35:43");
    }

    @Test
    public void filterForDateRange() throws Exception {
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForBaseTest#filterForDateRange()");
        runAndCheckDateInRange();
    }

    @Test
    public void filterLineExclusion() throws Exception {
        cut.addToExclusionLinesList(" abgeschaltet");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForBaseTest#filterLineExclusion()");
        runAndCheckExclusionList(cut.getExclusionsListItems());
    }

    @Test
    public void filterLineInclusion() throws Exception {
        cut.addToInclusionLinesList("LOESCHUNG_ERFOLGREICH");
        cut.addToInclusionLinesList("gefundenen Verzeichnisse");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForBaseTest#filterLineInclusion()");
        runAndCheckInclusionList(cut.getInclusionsListItems());
    }

    @Test
    public void testFilterLOESCHUNG_ERFOLGREICH() throws Exception {
        cut.addToInclusionLinesList("LOESCHUNG_ERFOLGREICH");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForENETest#filterForLoeschJobAnalyse()");
        runAndCheckInclusionList(cut.getInclusionsListItems());
    }

    @Test
    public void filterScannerRemoverAbgeschaltet() throws Exception {
        cut.addToInclusionLinesList("Scanner abgeschaltet");
        cut.addToInclusionLinesList("Remover abgeschaltet");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForBaseTest#filterLineExclusion()");
        runAndCheckInclusionList(cut.getInclusionsListItems());
    }

    @Test
    public void testAnzahlDerGefundenenVerzeichnisse() throws Exception {
        cut.addToInclusionLinesList("Anzahl der gefundenen Verzeichnisse");
        LogFileParser.LOGGER.info("JUNIT-Test LogFileParserForENETest#filterForLoeschJobAnalyse()");
        cut.filterLogFile(logFile);
    }
}