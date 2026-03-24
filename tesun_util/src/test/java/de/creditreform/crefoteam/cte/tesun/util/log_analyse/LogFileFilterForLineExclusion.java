package de.creditreform.crefoteam.cte.tesun.util.log_analyse;

import java.util.List;

public class LogFileFilterForLineExclusion implements LogFileFilter {
    private final List<String> exclusionList;

    public LogFileFilterForLineExclusion(List<String> exclusionList) {
        this.exclusionList = exclusionList;
    }

    @Override
    public boolean accepted(String strLine) {
        boolean contains = false;
        for (String exclusion : exclusionList) {
            contains |= strLine.contains(exclusion);
        }
        return !contains;
    }

    public void addExclusion(String exclusion) {
        exclusionList.add(exclusion);
    }

    public List<String> getExclusionList() {
        return exclusionList;
    }

    @Override
    public String toString() {
        return "Exclusions {" + exclusionList + "}";
    }

}
