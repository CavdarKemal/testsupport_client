package de.creditreform.crefoteam.cte.tesun.generate;

import de.creditreform.crefoteam.cte.tesun.util.AB30XMLProperties;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.regex.Matcher;

public class AB30DataFilesFilter implements FileFilter {
   private final Map<Long, AB30XMLProperties>  usedAb30CrefoToPropertiesMap;

   public AB30DataFilesFilter(Map<Long, AB30XMLProperties> usedAb30CrefoToPropertiesMap) {
      this.usedAb30CrefoToPropertiesMap = usedAb30CrefoToPropertiesMap;
   }

   @Override
   public boolean accept(File theFile) {
      if(!theFile.isDirectory()) {
         String fileName = theFile.getName();
         Matcher matcherCrefo = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(fileName);
         if (matcherCrefo.find()) {
            if(usedAb30CrefoToPropertiesMap != null) {
               Long crefoNr = Long.valueOf(matcherCrefo.group());
               return usedAb30CrefoToPropertiesMap.containsKey(crefoNr);
            }
            return true;
         }
         else {
            if(fileName.equalsIgnoreCase(TestSupportClientKonstanten.USED_CREFOS_PROPS_FILENAME) || fileName.endsWith(".xml")) {
               return true;
            }
         }
      }
      return false;
   }
}
