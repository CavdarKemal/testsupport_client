package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.PathElementProcessorFactory;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;

import javax.naming.InsufficientResourcesException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExportsAdapterVsxImpl extends ExportsAdapterDefImpl {

   public ExportsAdapterVsxImpl(ExportsAdapterConfig exportsAdapterConfig, TestCustomer testCustomer, TesunClientJobListener tesunClientJobListener) throws InsufficientResourcesException {
      super(exportsAdapterConfig, testCustomer, tesunClientJobListener);
   }

   @Override
   public List<String> getZipFilePrefixes() {
      return Arrays.asList("CLZ_");
   }

   @Override
   public List<PathElement> listZipFiles(PathElementProcessor processorForZipFiles, List<TestCrefo> testCrefosList) {
      List<PathElement> pathElementsList = processorForZipFiles.listFiles(pathElement -> {
         boolean isOK = pathElement.isDirectory();
         if (isOK) {
            String name = pathElement.getName();
            try {
               Integer clz = Integer.valueOf(name);
               return clz > 100 && clz < 999;
            } catch (Exception e) {
               return false;
            }
         }
         return isOK;
      });
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
