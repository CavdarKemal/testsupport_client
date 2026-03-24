package de.creditreform.crefoteam.cte.tesun.generate;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

public class ReplaceOldDatePatterns implements TesunClientJobListener {

   final Pattern dateTimePattern = Pattern.compile("<.*>T[+-][0-9]{1,2}[JMT]");
   final Pattern datePattern = Pattern.compile("<.*>D[+-][0-9]{1,2}[JMT]");

   final EnvironmentConfig environmentConfig;

   public ReplaceOldDatePatterns(EnvironmentConfig environmentConfig) {
      this.environmentConfig = environmentConfig;
   }

   public void replaceOldDatePatterns(File srcAb30XmlsDir, File dstAb30XmlsDir) throws Exception {
      TesunRestService tesunRestServiceWLS = new TesunRestService(environmentConfig.getRestServiceConfigsForMasterkonsole().get(0), this);
      final Collection<File> xmlFiles = FileUtils.listFiles(srcAb30XmlsDir, new String[]{"xml"}, true);
      for (File theFile : xmlFiles) {
         Matcher matcherCrefo = TestSupportClientKonstanten.CREFONUMMER_PATTERN.matcher(theFile.getName());
         if (matcherCrefo.find()) {
            Long theCrefo = Long.valueOf(matcherCrefo.group());
            String downloadedXML = downloadCrefo(tesunRestServiceWLS, theCrefo);
            if (downloadedXML != null) {
               String srcXml = FileUtils.readFileToString(theFile);
               List<String> tokensList = collectMatchedPatterns(srcXml);
               if (!tokensList.isEmpty()) {
                  for (String token : tokensList) {
                     // "<arc:letzte-recherche>D-6M"
                     // "<arc:erfassung>D-2J"
                     // "<arc:letzte-archivaenderung>T-1J"
                     // "<arc:letzte-manuelle-archivaenderung>T-2J"
                     // "<arc:zeitpunkt>T-6M"
                     String strAmount = extractAmount(token);
                     final String[] split = token.split(">");
                     String fieldName = split[0] + ">"; // "<arc:letzte-recherche>"
                     final String strNewAmount = String.format("<!-- %s -->", strAmount);
                     String originalValue = extractOriginalValue(fieldName, downloadedXML);
                     if (!originalValue.isEmpty()) {
                        final String replacement = fieldName + strNewAmount + originalValue;
                        final String[] splitX = token.split(">");
                        srcXml = srcXml.replace(fieldName + splitX[1], replacement);
                     }
                  }
                  if(dstAb30XmlsDir != null) {
                     final File file = new File(dstAb30XmlsDir, (theCrefo + ".xml"));
                     FileUtils.writeStringToFile(file, srcXml, Charset.forName("UTF-8"));
                  }
                  else {
                     FileUtils.writeStringToFile(theFile, srcXml, Charset.forName("UTF-8"));
                  }
               }
            }
         }
      }
   }

   private String extractOriginalValue(String fieldName, String crefoXML) {
      final Pattern fieldValuePattern = Pattern.compile(fieldName + ".*<");
      final Matcher matcher = fieldValuePattern.matcher(crefoXML);
      if (matcher.find()) {
         final String strValue = matcher.group();
         // <arc:erfassung> ==> <arc:erfassung>2019-02-28+01:00<
         return strValue.substring(fieldName.length(), strValue.length() - 1);
      }
      return "";
   }

   private List<String> collectMatchedPatterns(String ab30Xml) {
      List<String> tokensList = new ArrayList<>();
      Matcher matcherCrefo1 = datePattern.matcher(ab30Xml);
      while (matcherCrefo1.find()) {
         tokensList.add(matcherCrefo1.group());
      }
      Matcher matcherCrefo2 = dateTimePattern.matcher(ab30Xml);
      while (matcherCrefo2.find()) {
         tokensList.add(matcherCrefo2.group());
      }
      return tokensList;
   }

   private String downloadCrefo(TesunRestService tesunRestServiceWLS, Long theCrefo) throws Exception {
      String ab30Xml = null;
      try {
         ab30Xml = tesunRestServiceWLS.downloadCrefo(theCrefo);
      } catch (Exception ex) {
         if (!ex.getMessage().contains("204 (No Content)")) {
            throw ex;
         }
      }
      /*
      final File xmlsFilesDir = new File(environmentConfig.getPseudoAB30XmlsFile().getParentFile(), "XXX");
      final File theXmlFile = new File(xmlsFilesDir, (theCrefo + ".xml"));
      if (theXmlFile.exists()) {
         ab30Xml = FileUtils.readFileToString(theXmlFile);
      }
      */
      return ab30Xml;
   }

   private String extractAmount(String token) {
      final String[] split = token.split(">");
      String fieldValue = split[1]; // "D-6M" -->  "<!-- -6M -->"
      final String[] split1 = fieldValue.split("[DT]");
      return split1[1];
   }

   @Override
   public void notifyClientJob(Level level, Object notifyObject) {

   }

   @Override
   public Object askClientJob(ASK_FOR askFor, Object userObject) {
      return null;
   }
}
