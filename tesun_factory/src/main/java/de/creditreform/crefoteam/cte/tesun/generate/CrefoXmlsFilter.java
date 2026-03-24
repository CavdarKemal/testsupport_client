package de.creditreform.crefoteam.cte.tesun.generate;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;

public class CrefoXmlsFilter implements FileFilter {

   public CrefoXmlsFilter() {
   }

   @Override
   public boolean accept(File theFile) {
      if(!theFile.isDirectory()) {
         String fileName = theFile.getName();
         Matcher matcherCrefo = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(fileName);
         return matcherCrefo.find();
      }
      return false;
   }
}
