package de.creditreform.crefoteam.cte.tesun;

import de.creditreform.crefoteam.cte.pathabstraction.PathElementProcessorFactory;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.propertyfiles.PathInfo;

import java.util.List;

public interface CustomerCollectHelper {

    List<String> getZipFilePrefixes();
    String getZipFileNameForZipEntryName(String zipEntryName, TestCrefo testCrefo);

    PathElementFilter getExportZipsPathElementFilter(List<TestCrefo> nameCrefoList);

    PathElementFilter getExportDirsPathElementFilter();

    List<PathElement> listZipFiles(PathElementProcessorFactory pathElementProcessorFactory, PathElementProcessor processorForZipFiles, List<TestCrefo> nameCrefoList);

}
