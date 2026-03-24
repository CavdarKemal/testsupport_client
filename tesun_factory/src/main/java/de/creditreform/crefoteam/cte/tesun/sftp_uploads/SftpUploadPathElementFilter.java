package de.creditreform.crefoteam.cte.tesun.sftp_uploads;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import java.util.List;

public class SftpUploadPathElementFilter implements PathElementFilter {
   final List<String> strPrefixe;

   public SftpUploadPathElementFilter(List<String> strPrefixe) {
      this.strPrefixe = strPrefixe;
   }

   @Override
   public boolean accept(PathElement pathElement) {
      if (pathElement.isDirectory()) {
         return false;
      }
      String name = pathElement.getName();
      boolean prefixOK = checkPrefixe(name);
      boolean isZip = prefixOK && (name.endsWith(".zip") || name.endsWith(".gpg"));
      return isZip;
   }

   private boolean checkPrefixe(String name) {
      for(String zipPrefix : strPrefixe) {
         if(name.contains(zipPrefix)) {
            return true;
         }
      }
      return false;
   }

}

