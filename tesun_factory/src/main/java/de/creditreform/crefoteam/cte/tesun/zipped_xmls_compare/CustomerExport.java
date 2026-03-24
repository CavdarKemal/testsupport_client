package de.creditreform.crefoteam.cte.tesun.zipped_xmls_compare;

import de.creditreform.crefoteam.cte.pathabstraction.PathElementProcessorFactory;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.exports_checker.ExportContentsDifferenceListener;
import de.creditreform.crefoteam.cte.tesun.util.XmlDiffFormatter;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.xml.sax.SAXException;

import static de.creditreform.crefoteam.cte.tesun.zipped_xmls_compare.PathElementFilters.DIRECTORES_FILTER;
import static de.creditreform.crefoteam.cte.tesun.zipped_xmls_compare.PathElementFilters.ZIP_FILES_FILTER;

/**
 * Created by CavdarK on 22.11.2016.
 */
public class CustomerExport {
   private final String[] runSymbols = new String[] {"|\b", "/\b", "-\b", "\\\b"};
   private final Map<Object, Object> configuration = new HashMap<>();
   private final Set<String> ignoredPathsSet = new TreeSet<>();

   private final PathElement pathElement;
   private final File resultsDir;
   private final ExportContentsDifferenceListener ignorableElementsListener;
   private final boolean disableQuoting;

   private PrintStream printStream;
   private Map<String, Long> nodeDifferencesMap = new TreeMap<>();
   private List<String> errorsList = new ArrayList<>();

   public CustomerExport(PathElement pathElement, File resultsDir, List<String> ignorableXPaths, boolean disableQuoting) {
      this.resultsDir = resultsDir;
      this.pathElement = pathElement;
      ignoredPathsSet.clear();
      ignorableElementsListener = new ExportContentsDifferenceListener(ignorableXPaths);
      this.disableQuoting = disableQuoting;
   }

   public PathElement getPathElement() {
      return pathElement;
   }

   public String getName() {
      return pathElement.getName();
   }

   public void compare(CustomerExport cmpCustomerExport) throws Exception {
      long customerStart = System.currentTimeMillis();
      printStream = new PrintStream(new File(resultsDir, (getName() + ".log")));

      if (getName().contains("VS")) {
         makeFlat(pathElement);
         makeFlat(cmpCustomerExport.getPathElement());
      } else if (getName().contains("DRD")) {
         renameZips(pathElement);
         renameZips(cmpCustomerExport.getPathElement());
      }
      Map<String, PathElement> zipPathElementsMap = listPathElements(pathElement.getPath(), PathElementFilters.ZIP_FILES_FILTER);
      printStream.printf("Bearbeite Kunde '%s' mit %d ZIP-Dateien...\n", getName(), zipPathElementsMap.size());
      Map<String, PathElement> zipPathElementsMapX = listPathElements(cmpCustomerExport.getPathElement().getPath(), PathElementFilters.ZIP_FILES_FILTER);
      Collection<PathElement> zipPathElementsList = zipPathElementsMap.values();
      for (PathElement zipPathElement : zipPathElementsList) {
         String zipName = zipPathElement.getName();
         printStream.printf("\tBearbeite ZipFile '%s' ", zipName);
         long zipTimeStart = System.currentTimeMillis();
         try {
            if (!zipPathElementsMapX.containsKey(zipName)) {
               printStream.printf("\n\t\t--> !!!: ZipFile '%s'für '%s' im Verzeichnis '%s' nicht vorhanden!\n", zipName, getName(), cmpCustomerExport.getPathElement().getPath().replaceAll("\\\\", "/") );
               continue;
            }
            PathElement cmpPathElement = zipPathElementsMapX.get(zipPathElement.getName());
            List<String> zipErrorsList = compareZipFile(zipName, zipPathElement, cmpPathElement);
            printStream.printf(" --> %d ms.\n", (System.currentTimeMillis() - zipTimeStart));
            if (!zipErrorsList.isEmpty()) {
               errorsList.addAll(zipErrorsList);
               for (String errorString : zipErrorsList) {
                  printStream.print(errorString);
               }
            }
         } catch (Exception ex) {
            printStream.printf("\t\t--> !!!: ZipFile '%s' für '%s' fehlerhaft!\n", zipPathElement.getPath().replaceAll("\\\\", "/"), getName());
         }
         System.out.print("*");
      }
      printStream.printf("\nZusammenfassung der Unterschiede für den Kunden '%s'\n", getName());
      if (!nodeDifferencesMap.isEmpty()) {
         printStream.print("\tUnterschiedliche XML-Elemente\n");
         printStream.printf("\t%8s\t%s\n", "Anzahl", "XML-Element");
         printStream.print("\t-----------------------------------------------------------------------------------------\n");
         for (Map.Entry entry : nodeDifferencesMap.entrySet()) {
            Long differencesCount = (Long) entry.getValue();
            printStream.printf("\t%8d\t%s\n", differencesCount, entry.getKey());
         }
      }
      if (!ignoredPathsSet.isEmpty()) {
         printStream.print("\n\tIgnorierte Elemente wegen falscher Reihenfolge:\n");
         for (String xPath : ignoredPathsSet) {
            printStream.print("\t\t" + xPath + "\n");
         }
      }
      printStream.printf("\nZeitverbrauch für %d ZIP-Dateien: %d ms.\n", zipPathElementsList.size(), (System.currentTimeMillis() - customerStart));
      printStream.close();
   }

   protected List<String> compareZipFile(String zipName, PathElement refPathElement, PathElement cmpPathElement) throws Exception {
      ZipFile refZipInputFile = null;
      ZipFile cmpZipInputFile = null;
      List<String> errorsList = new ArrayList<>();
      try {
         Map<String, ZipArchiveEntry> refZipArchiveEntriesMap = new TreeMap<String, ZipArchiveEntry>();
         refZipInputFile = new ZipFile(refPathElement.getPath());
         Enumeration<ZipArchiveEntry> refZipArchiveEntries = refZipInputFile.getEntries();
         while (refZipArchiveEntries.hasMoreElements()) {
            ZipArchiveEntry zipArchiveEntry = refZipArchiveEntries.nextElement();
            refZipArchiveEntriesMap.put(zipArchiveEntry.getName(), zipArchiveEntry);
         }
         cmpZipInputFile = new ZipFile(cmpPathElement.getPath());
         Enumeration<ZipArchiveEntry> cmpZipArchiveEntries = cmpZipInputFile.getEntries();
         int index = 0;
         int nCount = 0;
         while (cmpZipArchiveEntries.hasMoreElements()) {
            index = printRunning(nCount++, index);
            ZipArchiveEntry cmpZipArchiveEntry = cmpZipArchiveEntries.nextElement();
            ZipArchiveEntry refZipArchiveEntry = refZipArchiveEntriesMap.remove(cmpZipArchiveEntry.getName());
            if (refZipArchiveEntry != null) {
               String errorString = compareZipArchiveEntry(refZipInputFile, cmpZipInputFile, cmpZipArchiveEntry, refZipArchiveEntry);
               if (!errorString.isEmpty()) {
                  String fileName = getName() + "/" + zipName + "." + refZipArchiveEntry.getName() + ".diff.txt";
                  File diffFile = new File(resultsDir, fileName);
                  FileUtils.writeStringToFile(diffFile, errorString, Charset.forName("UTF-8"));
                  errorsList.add("\t\t!!!: Unterschied --> " + diffFile.getAbsolutePath().replaceAll("\\\\", "/") + "\n");
               }
            } else {
               errorsList.add("\t\t!!!: Zip-Enty " + cmpZipArchiveEntry.getName() + " in Referenz-Map nicht vorhaneden!\n");
            }
         }
         System.out.print(runSymbols[0]);
         printStream.printf("mit %d ZIP-Entries ", nCount);
         Iterator<String> refIterator = refZipArchiveEntriesMap.keySet().iterator();
         while (refIterator.hasNext()) {
            String refKey = refIterator.next();
            errorsList.add("\t\t!!!: Zip-Enty '" + refKey + "' in Referenz-Map übriggeblieben!\n");
         }
      } finally {
         if (refZipInputFile != null) {
            refZipInputFile.close();
         }
         if (cmpZipInputFile != null) {
            cmpZipInputFile.close();
         }
      }
      return errorsList;
   }

   private int printRunning(int nCount, int index) {
      if (nCount % 20 == 0) {
         System.out.print(runSymbols[index%4]);
         index++;
      }
      return index;
   }

   private String compareZipArchiveEntry(ZipFile refZipInputFile, ZipFile cmpZipInputFile, ZipArchiveEntry cmpZipArchiveEntry, ZipArchiveEntry refZipArchiveEntry) throws Exception {

      StringBuilder errorString = new StringBuilder();

      InputStream refInputStream = refZipInputFile.getInputStream(refZipArchiveEntry);
      InputStream cmpInputStream = cmpZipInputFile.getInputStream(cmpZipArchiveEntry);
      StringWriter refWriter = new StringWriter();
      IOUtils.copy(refInputStream, refWriter, "UTF-8");
      StringWriter cmpWriter = new StringWriter();
      IOUtils.copy(cmpInputStream, cmpWriter, "UTF-8");

      List<Difference> differences = compareXMLs(refWriter.toString(), cmpWriter.toString());
      if (differences.size() > 0) {
         XmlDiffFormatter xmlDiffFormatter = new XmlDiffFormatter();
         for (Difference difference : differences) {
            String xpathLocation = difference.getControlNodeDetail().getXpathLocation().replaceAll("[\\d]", "");
            if (difference.getDescription().equals("sequence of child nodes")) {
               ignoredPathsSet.add(xpathLocation);
               continue;
            } else {
               Long differencesCount = nodeDifferencesMap.get(xpathLocation);
               if (differencesCount == null) {
                  differencesCount = Long.valueOf(0);
               }
               differencesCount++;
               nodeDifferencesMap.put(xpathLocation, differencesCount);
            }
            errorString = xmlDiffFormatter.appendDifference(errorString, "", difference);
         }
      }
      return errorString.toString();
   }

   private List<Difference> compareXMLs(String refXml, String cmpXml) throws SAXException, IOException {
      XMLUnit.setIgnoreWhitespace(true);
      XMLUnit.setIgnoreAttributeOrder(true);
      String control = disableQuoting ? refXml.replaceAll("&", "#") : refXml;
      String test = disableQuoting ? cmpXml.replaceAll("&", "#") : cmpXml;
      Diff diff = XMLUnit.compareXML(control, test);
      diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
      diff.overrideDifferenceListener(ignorableElementsListener);
      DetailedDiff detailedDiff = new DetailedDiff(diff);
      List<Difference> allDifferences = detailedDiff.getAllDifferences();
      return allDifferences;
   }

   private Map<String, PathElement> listPathElements(String searchPath, PathElementFilter searchFilter) {
      PathElementProcessorFactory factory = new PathElementProcessorFactory(configuration);
      PathElementProcessor processor = factory.create(searchPath);
      List<PathElement> pathElementsList = processor.listFiles(searchFilter);
      factory.close();
      Map<String, PathElement> theMap = new TreeMap<String, PathElement>();
      for (PathElement pathElement : pathElementsList) {
         theMap.put(pathElement.getName(), pathElement);
      }
      return theMap;
   }

   private void makeFlat(PathElement pathElement) throws Exception {
      Map<String, PathElement> dirPathElementsMap = listPathElements(pathElement.getPath(), DIRECTORES_FILTER);
      Collection<PathElement> dirPathElements = dirPathElementsMap.values();
      for (PathElement dirPathElement : dirPathElements) {
         File srcFile = new File(dirPathElement.getPath());
         FileUtils.copyDirectory(srcFile, srcFile.getParentFile());
         FileUtils.deleteDirectory(srcFile);
      }
   }

   private void renameZips(PathElement pathElement) throws Exception {
      Map<String, PathElement> dirPathElementsMap = listPathElements(pathElement.getPath(), ZIP_FILES_FILTER);
      Collection<PathElement> dirPathElements = dirPathElementsMap.values();
      for (PathElement dirPathElement : dirPathElements) {
         String path = dirPathElement.getPath();
         File srcFile = new File(path);
         // 20161117_1155_abCrefo203.2000000_createAndUpdate.zip
         int abCrefo = path.indexOf("_abCrefo");
         if (abCrefo > 0) {
            File destFile = new File(srcFile.getParentFile(), path.substring(abCrefo + 1));
            FileUtils.moveFile(srcFile, destFile);
         }
      }

   }

   public List<String> getErrorsList() {
      return errorsList;
   }

}
