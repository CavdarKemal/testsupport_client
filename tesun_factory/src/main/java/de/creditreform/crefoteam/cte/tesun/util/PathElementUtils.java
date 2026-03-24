package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PathElementUtils {

    public static PathElement findFirstJoungerElement(TesunClientJobListener tesunClientJobListener, List<PathElement> pathElementsList, Calendar lastComletitionCal) {
        lastComletitionCal.set(Calendar.SECOND, 0);
        lastComletitionCal.set(Calendar.MILLISECOND, 0);
        // sortiere die Dinger von alt nach neu, damit man den ersten finden, der jünger ist!
        Collections.sort(pathElementsList, Comparator.comparing(PathElement::getName)); // alt nach jung
        for (PathElement pathElement : pathElementsList) {
            boolean equal = TesunDateUtils.isSameOrAfter(pathElement.getName(), lastComletitionCal);
            if (equal) {
                return pathElement;
            }
        }
        String info = "\n\t\tSuch für das Jüngste PathElement nach '" + TesunDateUtils.formatCalendar(lastComletitionCal) + "' blieb erfolglos!";
        info += "\n\t\tFolgende PathElement-Liste wurde durchsucht:";
        StringBuilder stringBuilder = new StringBuilder();
        for( PathElement pathElement : pathElementsList ) {
            stringBuilder.append(pathElement.getSymbolicPath()).append("\n\t\t");
        }
        info += "\n\t\t" + stringBuilder;
        tesunClientJobListener.notifyClientJob(Level.INFO, info);
        return null;
    }

}
