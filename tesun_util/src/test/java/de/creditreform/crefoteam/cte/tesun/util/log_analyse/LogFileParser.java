package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogFileParser {
    public static Logger LOGGER = LoggerFactory.getLogger(LogFileFilter.class);
    LogFileFilterForLineExclusion logFileFilterForExclusion;
    LogFileFilterForLineInclusion logFileFilterForInclusion;
    final Date fromDate;
    final Date toDate;
    final List<LogFileFilter> logFileFilterList;
    String strDateRange;

    public LogFileParser(File logFile, Date fromDate, Date toDate) {
        if (!logFile.exists()) {
            throw new IllegalArgumentException("Die LOG-Datei '" + logFile.getAbsolutePath() + "' existiert nicht!");
        }
        logFileFilterList = new ArrayList();
        this.fromDate = fromDate;
        this.toDate = toDate;
        strDateRange = "Date-Range " +
                TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS.format(fromDate) +
                " - " +
                TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS.format(toDate);

        logFileFilterForExclusion = new LogFileFilterForLineExclusion(new ArrayList<>());
        addToExclusionLinesList("SftpConnectionImpl - JSch");
        logFileFilterList.add(logFileFilterForExclusion);

        List<String> inclusionList = new ArrayList<>();
        logFileFilterForInclusion = new LogFileFilterForLineInclusion(inclusionList);
        logFileFilterList.add(logFileFilterForInclusion);
    }

    public File writeLinesToLogResultFile(File logFile, List<String> filteredLinesList) throws IOException {
        File targetLogFile = new File(logFile.getParent(), logFile.getName() + "-" + strDateRange + "-" + logFileFilterList + ".txt");
        FileUtils.writeLines(targetLogFile, filteredLinesList);
        LOGGER.info("\tGefilterte LOG-Einträge wurden in der Datei gespeichert:");
        LOGGER.info("\t'" + targetLogFile.getAbsolutePath() + "'");
        return targetLogFile;
    }

    public List<String> filterLogLines(List<String> sourceLinesList) throws Exception {
        // filtern nach Datümern
        sourceLinesList = filterDateRange(sourceLinesList);

        // filtern nach weiteren Filtern
        List<String> filteredLinesList = new ArrayList<>();
        for (String strLine : sourceLinesList) {
            boolean stilSatisfied = true;
            for (LogFileFilter logFileFilter : logFileFilterList) {
                boolean accepted = logFileFilter.accepted(strLine);
                if (!accepted) {
                    stilSatisfied = false;
                    break;
                }
            }
            if (stilSatisfied) {
                filteredLinesList.add(strLine);
            }
        }
        return filteredLinesList;
    }

    public List<String> filterLogFile(File logFile) throws Exception {
        List<String> sourceLinesList = FileUtils.readLines(logFile);
        return filterLogLines(sourceLinesList);
    }

    private List<String> filterDateRange(List<String> sourceLinesList) {
        int startIndex = -1;
        int endIndex = -1;
        for (int tmpIndex = 0; tmpIndex < sourceLinesList.size(); tmpIndex++) {
            String strLine = sourceLinesList.get(tmpIndex);
            int strLen = strLine.length() > 19 ? 19 : strLine.length();
            // Beginn suchen, falls noch nicht gefunden...
            if (startIndex < 0) {
                try {
                    Date dateFromLine = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse(strLine.substring(0, strLen));
                    if (!dateFromLine.before(fromDate)) {
                        startIndex = tmpIndex;
                    }
                } catch (ParseException e) {
                    // is OK, da nicht jede Zeile ein Datum ergeben muss!
                }
            } else {
                // Beginn gefunden; nun Ende suchen, falls noch nicht gefunden...
                try {
                    Date dateFromLine = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse(strLine.substring(0, strLen));
                    if (dateFromLine.compareTo(toDate) >= 0) {
                        endIndex = tmpIndex;
                        break;
                    }
                } catch (ParseException e) {
                    // is OK, da nicht jede Zeile ein Datum ergeben muss!
                }
            }
        }
        if (startIndex < 0) {
            throw new RuntimeException("Datum '" + TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS.format(fromDate) +
                    "' konnte in der Datei nicht gefunden werden!");
        }
        if (endIndex < 0) {
            throw new RuntimeException("Datum '" + TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS.format(fromDate) +
                    "' konnte in der Datei nicht gefunden werden!");
        }
        List<String> filteredLiensList = new ArrayList<>(sourceLinesList.subList(startIndex, endIndex));
        return filteredLiensList;
    }

    public List<String> getExclusionsListItems() {
        return logFileFilterForExclusion.getExclusionList();
    }

    public void addToExclusionLinesList(String exclusion) {
        logFileFilterForExclusion.addExclusion(exclusion);
    }

    public List<String> getInclusionsListItems() {
        return logFileFilterForInclusion.getInclusionList();
    }

    public void addToInclusionLinesList(String inclusion) {
        logFileFilterForInclusion.addInclusion(inclusion);
    }

    public void addToLogFileFilterList(LogFileFilter logFileFilter) {
        logFileFilterList.add(logFileFilter);
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public List<LogFileFilter> getLogFileFilterList() {
        return logFileFilterList;
    }


}
