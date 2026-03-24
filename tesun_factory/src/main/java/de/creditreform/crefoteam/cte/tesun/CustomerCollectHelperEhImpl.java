package de.creditreform.crefoteam.cte.tesun;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.tesun.exports_collector.ExportDirsPathElementFilter;
import de.creditreform.crefoteam.cte.tesun.exports_collector.ExportZipsPathElementFilter;
import de.creditreform.crefoteam.cte.tesun.util.NameCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;

import java.util.Arrays;
import java.util.List;

public class CustomerCollectHelperEhImpl extends CustomerCollectHelperDefImpl {

    public CustomerCollectHelperEhImpl(TestCustomer testCustomer) {
        super(testCustomer);
    }

    @Override
    public List<String> getZipFilePrefixes() {
        return Arrays.asList("abCrefo", "abFTN_");
    }

}
