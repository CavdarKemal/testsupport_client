package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestScenario;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ExportsAdapter {

    PathElement findRelatedPathElement();

    PathElementFilter getExportZipsPathElementFilter(List<TestCrefo> testCrefoList);

    PathElementFilter getExportDirsPathElementFilter();

    List<PathElement> listZipFiles(PathElementProcessor processorForZipFiles, List<TestCrefo> testCrefosList);

    List<String> getZipFilePrefixes();

    boolean isTestScenarioActive(String scenarioName);

    TestCustomer getTestCustomer();

    String getCustomerKey();

    void copyPropsFile(TestScenario testScenario) throws IOException;

    ByteArrayOutputStream retrieveFileContent(PathElement pathElement) throws Exception;

    void createCollectDirStruct(File theFile) throws IOException;

    List<PathElement> listZipPathElements(PathElement joungestPathElement, List<TestCrefo> testCrefosList);

    Map<String, List<File>> extractAndSaveExportsForTestScenario(TestScenario testScenario, List<PathElement> pathElementList) throws Exception;

}
