package de.creditreform.crefoteam.cte.tesun.util;

import de.creditreform.crefoteam.cte.tesun.util.propertyfiles.PathInfo;

/**
 * Erweiterung von {@link NameCrefo} um das Verzeichnis mit der Testfall-
 * Definition
 * User: ralf
 * Date: 13.05.14
 * Time: 17:03
 */
public class NameCrefoPfad
        extends NameCrefo {
    private final PathInfo pathInfo;

    public NameCrefoPfad(PathInfo pathInfo, String testFallName, Long testFallCrefo) {
        super(testFallName, testFallCrefo);
        this.pathInfo = pathInfo;
    }

    public PathInfo getPathInfo() {
        return pathInfo;
    }

}
