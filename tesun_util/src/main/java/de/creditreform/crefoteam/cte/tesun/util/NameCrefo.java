package de.creditreform.crefoteam.cte.tesun.util;

/**
 * Container für einen einzelnen Testfall mit Bezeichnung unf Crefonummer
 * User: ralf
 * Date: 13.05.14
 * Time: 17:01
 */
public class NameCrefo {
    private final String testFallName;
    private final Long testFallCrefo;

    @Override
    public String toString() {
        return testFallName + "=" + testFallCrefo;
    }

    public NameCrefo(String testFallName, Long testFallCrefo) {
        this.testFallName = testFallName;
        this.testFallCrefo = testFallCrefo;
    }

    public String getTestFallName() {
        return testFallName;
    }

    public Long getTestFallCrefo() {
        return testFallCrefo;
    }
}
