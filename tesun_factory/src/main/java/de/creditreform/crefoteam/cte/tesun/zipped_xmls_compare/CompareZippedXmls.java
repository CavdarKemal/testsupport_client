package de.creditreform.crefoteam.cte.tesun.zipped_xmls_compare;

import de.creditreform.crefoteam.cte.pathabstraction.PathElementProcessorFactory;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementFilter;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class CompareZippedXmls {
    private final Map<Object, Object> configuration = new HashMap<>();

    private File refDir;
    private File cmpDir;
    private File resultsDir;

    public CompareZippedXmls(String compare1DirName, String compare2DirName, String resultsDirName) {
        this.refDir = new File(compare1DirName);
        this.cmpDir = new File(compare2DirName);
        try {
            if (!new File(compare1DirName).exists()) {
                throw new RuntimeException("Vergleichsverzeichnis 1 " + compare1DirName + " ist ungültig!");
            }
            if (!new File(compare2DirName).exists()) {
                throw new RuntimeException("Vergleichsverzeichnis 2 " + compare2DirName + "ist ungültig!");
            }
            this.resultsDir = new File(resultsDirName);
            if (this.resultsDir.exists()) {
                File tempFile = new File(resultsDirName + System.currentTimeMillis() + "");
                this.resultsDir.renameTo(tempFile);
            }
            boolean mkdir = this.resultsDir.mkdir();
            if (!mkdir) {
                throw new RuntimeException("Ergebnissverzeichnis " + resultsDirName + " konnte nicht erzeugt werden!");
            }
        } catch (Throwable th) {
            th.printStackTrace();
            System.exit(-1);
        }
    }

    private Map<String, PathElement> listPathElements(String searchPath, PathElementFilter searchFilter) {
        PathElementProcessorFactory factory = new PathElementProcessorFactory(configuration);
        PathElementProcessor processor = factory.create(searchPath);
        List<PathElement> pathElementsList = processor.listFiles(searchFilter);
        factory.close();
        Map<String, PathElement> theMap = new TreeMap<String, PathElement>();
        for (PathElement pathElement : pathElementsList) {
            String name = pathElement.getName();
            if (!name.startsWith("!")) {
                theMap.put(name, pathElement);
            }
        }
        return theMap;
    }

    private void compareExports(List<String> ignorableXPaths, boolean disableQuoting) throws Exception {
        Map<String, PathElement> refPathElementsMap = listPathElements(refDir.getPath(), PathElementFilters.DIRECTORES_FILTER);
        Map<String, PathElement> cmpPathElementsMap = listPathElements(cmpDir.getPath(), PathElementFilters.DIRECTORES_FILTER);
        String strStartTime = TesunDateUtils.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS.format(Calendar.getInstance().getTime());
        PrintStream mainPrintStream = new PrintStream(new File(resultsDir, "cmp_xml_exports.log"));
        mainPrintStream.print("Startzeit: " + strStartTime + "\n");
        Collection<PathElement> pathElements7List = refPathElementsMap.values();
        mainPrintStream.print("\tBearbeite " + pathElements7List.size() + " Kunden...\n");
        System.out.print("\tBearbeite " + pathElements7List.size() + " Kunden...\n");
        for (PathElement refPathElement : pathElements7List) {
            long customerStart = System.currentTimeMillis();
            CustomerExport refCustomerExport = new CustomerExport(refPathElement, resultsDir, ignorableXPaths, disableQuoting);
            String refCustomerName = refCustomerExport.getName();
            mainPrintStream.print("\t\tBearbeite Kunde '" + refCustomerName + "'...");
            System.out.print("\t\tBearbeite Kunde '" + refCustomerName + "': ");
            if (!cmpPathElementsMap.containsKey(refCustomerName)) {
                mainPrintStream.printf("\t-->!!!: Kunde %s existiert nicht im Verzeichnis '%s'!\n", refCustomerName, cmpDir.getPath());
                System.out.println();
                continue;
            }
            PathElement cmpPathElement = cmpPathElementsMap.remove(refCustomerName);
            CustomerExport cmpCustomerExport = new CustomerExport(cmpPathElement, resultsDir, ignorableXPaths, disableQuoting);
            refCustomerExport.compare(cmpCustomerExport);
            mainPrintStream.print("\t--> Zeitverbrauch: " + (System.currentTimeMillis() - customerStart));
            List<String> customerErrorsList = refCustomerExport.getErrorsList();
            if (!customerErrorsList.isEmpty()) {
                mainPrintStream.print("\tAnzahl Fehler: " + customerErrorsList.size() + ", siehe " + refCustomerName + ".log");
                wrteErrorsToFile(refCustomerName, customerErrorsList);
            }
            mainPrintStream.print("\n");
            System.out.println();
        }
        if (!cmpPathElementsMap.isEmpty()) {
            for (Map.Entry entry : cmpPathElementsMap.entrySet()) {
                mainPrintStream.printf("\t\t!!!: Kunde %s existiert nicht im Verzeichnis '%s'!\n", entry.getKey(), refDir.getPath());
            }
        }
        String strEndTime = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.format(Calendar.getInstance().getTime());
        mainPrintStream.print("Endzeit: " + strEndTime + "\n");
        mainPrintStream.close();
        System.out.println("");
    }

    private void wrteErrorsToFile(String refCustomerName, List<String> errorsList) {
        try {
            PrintStream printStream = new PrintStream(new File(resultsDir, (refCustomerName + "-Crefos.txt")));
            for(String strErr : errorsList) {
                int endIndex = strErr.indexOf(".xml");
                String strCrefo = strErr.substring(endIndex - 10, endIndex);
                printStream.printf("%s\n", strCrefo);
            }
            printStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        try {
            String compare1Dir = ((args.length > 0) && !args[0].isEmpty()) ? args[0] : "";
            String compare2Dir = ((args.length > 1) && !args[1].isEmpty()) ? args[1] : "";
            String resultsDir = ((args.length > 2) && !args[2].isEmpty()) ? args[2] : "";
            Boolean disableQuoting = ((args.length > 3) && !args[3].isEmpty()) ? args[3].equalsIgnoreCase("1") : null;
            if (args == null || args.length < 4) {
                StringBuilder message = new StringBuilder("Erforderliche Parameter fehlen!\n");
                message.append("Start:\n\tcmp_xml_exports <V1> <V2> <E> <I>\n");
                message.append("\tParameter:\n");
                message.append("\t\tV1:\tVergleichsverzeichnis 1\t= " + (compare1Dir.isEmpty() ? "fehlt!" : compare1Dir) + "\n");
                message.append("\t\tV2:\tVergleichsverzeichnis 2\t= " + (compare2Dir.isEmpty() ? "fehlt!" : compare2Dir) + "\n");
                message.append("\t\tE:\tErgebnissverzeichnis\t= " + (resultsDir.isEmpty() ? "fehlt!" : resultsDir) + "\n");
                message.append("\t\tI:\tQuotierung ausschalten\t= " + (disableQuoting == null ? "fehlt!, 1: Ja, 0: Nein" : resultsDir.toString()) + "\n");
                message.append("Beispiel:\n\t");
                String userDir = System.getProperty("user.dir");
                message.append(userDir + "\\TESTS\\Exports-A ");
                message.append(userDir + "\\TESTS\\Exports-B ");
                message.append(userDir + "\\Results 1");
                System.out.println(message.toString());
                System.exit(-1);
            }
            CompareZippedXmls cut = new CompareZippedXmls(compare1Dir, compare2Dir, resultsDir);
            List<String> ignorableXPaths = null;
            cut.compareExports(ignorableXPaths, disableQuoting.booleanValue());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
