package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;

import java.text.ParseException;
import java.util.Date;

public class LogFileFilterForDateRange implements LogFileFilter {
    final Date fromDate;
    final Date toDate;

    public LogFileFilterForDateRange(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override
    public boolean accepted(String strLine) {
        if (strLine.length() > 19) {
            try {
                Date dateFromLine = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse(strLine.substring(0, 19));
                return !dateFromLine.before(fromDate) && !dateFromLine.after(toDate);
            } catch (ParseException e) {
                // is OK, da nicht jede Zeile ein Datum ergeben muss!
                return true;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Date-Range " +
                " - " +
                TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS.format(fromDate) +
                TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM_MINUS_SS.format(toDate) + "}";
    }

}
