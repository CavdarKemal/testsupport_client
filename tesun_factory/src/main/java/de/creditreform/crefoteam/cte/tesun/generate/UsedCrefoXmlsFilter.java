package de.creditreform.crefoteam.cte.tesun.generate;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.regex.Matcher;

public class UsedCrefoXmlsFilter implements FileFilter {
   private final List<Long> usedTestCrefos;

   public UsedCrefoXmlsFilter(List<Long> usedTestCrefos) {
      this.usedTestCrefos = usedTestCrefos;
   }

   @Override
   public boolean accept(File theFile) {
      if(!theFile.isDirectory()) {
         String fileName = theFile.getName();
         Matcher matcherCrefo = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(fileName);
         if (matcherCrefo.find()) {
            Long crefoNr = Long.valueOf(matcherCrefo.group());
            return usedTestCrefos.contains(crefoNr);
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
