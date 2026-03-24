package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;

import java.text.ParseException;

public class ExportDirsPathElementFilter implements PathElementFilter {
    @Override
    public boolean accept(PathElement pathElement) {
        boolean isOK = pathElement.isDirectory();
        if (isOK) {
            String name = pathElement.getName();
            try {
                TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MINUS_MM.parse(name);
            } catch (ParseException e) {
                return false;
            }
        }
        return isOK;
    }
}

