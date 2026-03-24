package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;

public class RegExpPathElementFilter  implements PathElementFilter {
   final String regExp;

   public RegExpPathElementFilter(final String regExp) {
      this.regExp = regExp;
   }

   @Override
   public boolean accept(PathElement pathElement) {
      boolean isOK = !pathElement.isDirectory();
      if (isOK) {
         isOK &= pathElement.getName().matches(regExp);
      }
      return isOK;
   }
}
