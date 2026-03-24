package de.creditreform.crefoteam.cte.tesun;

import de.creditreform.crefoteam.cte.pathabstraction.PathElementProcessorFactory;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.exports_collector.ExportZipsPathElementFilter;
import de.creditreform.crefoteam.cte.tesun.util.NameCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomerCollectHelperVsxImpl extends CustomerCollectHelperDefImpl {

    public CustomerCollectHelperVsxImpl(TestCustomer testCustomer) {
        super(testCustomer);
    }

    @Override
    public List<String> getZipFilePrefixes() {
        return Arrays.asList("CLZ_");
    }

    @Override
    public List<PathElement> listZipFiles(PathElementProcessorFactory pathElementProcessorFactory, PathElementProcessor processorForZipFiles, List<TestCrefo> testCrefosList) {
        List<PathElement> pathElementsList = processorForZipFiles.listFiles(pathElement -> false);
        if (!pathElementsList.isEmpty() && pathElementsList.get(0).isDirectory()) {
            pathElementsList = listZipPathElementsForVSx(pathElementProcessorFactory, pathElementsList);
        }
        return pathElementsList;
    }

    private List<PathElement> listZipPathElementsForVSx(PathElementProcessorFactory pathElementProcessorFactory, List<PathElement> exportDirsListX) {
        // null als <nameCrefoList>, da wir alle ZIP's durchsuchen wollen, weil Eigner-VC...
        ExportZipsPathElementFilter vsxFilter = new ExportZipsPathElementFilter(null, Arrays.asList("CLZ_"));
        List<PathElement> pathElementsList = new ArrayList<>();
        for (PathElement pathElement : exportDirsListX) {
            PathElementProcessor processorForZipFiles = pathElementProcessorFactory.create(pathElement);
            List<PathElement> subPathElementsList = processorForZipFiles.listFiles(vsxFilter);
            pathElementsList.addAll(subPathElementsList);
        }
        return pathElementsList;
    }

}
