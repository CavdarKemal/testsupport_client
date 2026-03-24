package de.creditreform.crefoteam.cte.tesun;

import de.creditreform.crefoteam.cte.pathabstraction.PathElementProcessorFactory;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.exports_collector.ExportDirsPathElementFilter;
import de.creditreform.crefoteam.cte.tesun.exports_collector.ExportZipsPathElementFilter;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;

import java.util.Arrays;
import java.util.List;

public class CustomerCollectHelperDefImpl implements CustomerCollectHelper {
   protected final TestCustomer testCustomer;

   public CustomerCollectHelperDefImpl(TestCustomer testCustomer) {
      this.testCustomer = testCustomer;
   }

   @Override
   public List<String> getZipFilePrefixes() {
      return Arrays.asList("abCrefo");
   }

   @Override
   public String getZipFileNameForZipEntryName(String zipEntryName, TestCrefo testCrefo) {
      if (zipEntryName.contains(testCrefo.getItsqTestCrefoNr() + ".")) {
         return testCrefo.getTestFallName() + "-" + zipEntryName;
      }
      return null;
   }

   @Override
   public PathElementFilter getExportZipsPathElementFilter(List<TestCrefo> testCrefosList) {
      return new ExportZipsPathElementFilter(testCrefosList, getZipFilePrefixes());
   }

   @Override
   public PathElementFilter getExportDirsPathElementFilter() {
      return new ExportDirsPathElementFilter();
   }

   @Override
   public List<PathElement> listZipFiles(PathElementProcessorFactory pathElementProcessorFactory, PathElementProcessor processorForZipFiles, List<TestCrefo> testCrefosList) {
      PathElementFilter exportZipsPathElementFilter = getExportZipsPathElementFilter(testCrefosList);
      List<PathElement> pathElementsList = processorForZipFiles.listFiles(exportZipsPathElementFilter);
      return pathElementsList;
   }

}
