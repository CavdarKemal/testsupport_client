package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import java.util.List;

public class LogFileFilterForLineInclusion implements LogFileFilter {
    private final List<String> inclusionList;

    public LogFileFilterForLineInclusion(List<String> inclusionList) {
        this.inclusionList = inclusionList;
    }

    @Override
    public boolean accepted(String strLine) {
        if (inclusionList.isEmpty()) {
            return true;
        }
        boolean contains = false;
        for (String exclusion : inclusionList) {
            contains |= strLine.contains(exclusion);
        }
        return contains;
    }

    public void addInclusion(String inclusion) {
        inclusionList.add(inclusion);
    }

    public List<String> getInclusionList() {
        return inclusionList;
    }

    @Override
    public String toString() {
        return "Inclusions " + inclusionList + "}";
    }

}
