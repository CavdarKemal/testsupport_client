package de.creditreform.crefoteam.cte.tesun;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class CustomerCollectHelperInsoImpl extends CustomerCollectHelperDefImpl {
    public CustomerCollectHelperInsoImpl(TestCustomer testCustomer) {
        super(testCustomer);
    }

    @Override
    public List<String> getZipFilePrefixes() {
        return Arrays.asList("abCrefo", "abFTN_");
    }

    @Override
    public PathElementFilter getExportDirsPathElementFilter() {
        return new PathElementFilter() {
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
        };
    }
}
