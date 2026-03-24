package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.tesun.util.TestCrefo;

import java.util.List;

public class ExportZipsPathElementFilter  implements PathElementFilter {
   final List<TestCrefo> testCrefosList;
   final List<String> zipPrefixe;

   public ExportZipsPathElementFilter(List<TestCrefo> testCrefosList, List<String> zipPrefixe) {
      this.testCrefosList = testCrefosList;
      this.zipPrefixe = zipPrefixe;
   }

   @Override
   public boolean accept(PathElement pathElement) {
      if (pathElement.isDirectory()) {
         return false;
      }
      String name = pathElement.getName();
      boolean prefixOK = checkPrefixe(name);
      boolean isZip = prefixOK && name.endsWith(".zip");
      if (isZip && (testCrefosList != null)) {
         return nameCrefoListContaines(name);
      }
      return isZip;
   }

   private boolean checkPrefixe(String name) {
      for(String zipPrefix : zipPrefixe) {
         if(name.contains(zipPrefix)) {
            return true;
         }
      }
      return false;
   }

   private boolean nameCrefoListContaines(String name) {
      for (TestCrefo testCrefo : testCrefosList) {
         Long pseudoCrefoNr = testCrefo.getPseudoCrefoNr();
         if(pseudoCrefoNr != null) {
            String strClz = pseudoCrefoNr.toString().substring(0, 3);
            if (name.contains(strClz)) {
               return true;
            }
         }
      }
      return false;
   }
}

