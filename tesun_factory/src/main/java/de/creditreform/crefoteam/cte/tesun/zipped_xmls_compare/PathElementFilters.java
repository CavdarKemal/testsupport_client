package de.creditreform.crefoteam.cte.tesun.zipped_xmls_compare;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;

/**
 * Created by CavdarK on 22.11.2016.
 */
public class PathElementFilters {

    public static final PathElementFilter ZIP_FILES_FILTER = new PathElementFilter() {
        @Override
        public boolean accept(PathElement pathElement) {
            return pathElement.getName().endsWith(".zip");
        }
    };

    public static final PathElementFilter DIRECTORES_FILTER = new PathElementFilter() {
        @Override
        public boolean accept(PathElement pathElement) {
            return pathElement.isDirectory();
        }
    };

}
