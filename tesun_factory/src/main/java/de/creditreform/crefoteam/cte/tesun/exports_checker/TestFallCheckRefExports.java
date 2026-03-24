package de.creditreform.crefoteam.cte.tesun.exports_checker;

import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.httpstest.TestClientUtils;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestFallCheckRefExports extends AbstractTesunClientJob {
    public static final String COMMAND = "UserTask CHECK-REF-EXPORTS";
    public static final String DESCRIPTION = "Vergleich der gesammleten XML-Dateien mit den Referenz-XML-Dateien";

    private String collectedPath;
    private String pseudoRefExportsPath;
    private List<String> ignorableXPaths = new ArrayList<>();
    private final Map<String, TestCustomer> activeCustomersMap;
    private final TestSupportClientKonstanten.TEST_PHASE testPhase;
    private EnvironmentConfig environmentConfig;

    public TestFallCheckRefExports(Map<String, TestCustomer> activeCustomersMap, TestSupportClientKonstanten.TEST_PHASE testPhase, TesunClientJobListener tesunClientJobListener) {
        super(COMMAND, DESCRIPTION, tesunClientJobListener);
        this.activeCustomersMap = activeCustomersMap;
        this.testPhase = testPhase;
    }

    @Override
    public void init(EnvironmentConfig envConfig) throws Exception {
        this.environmentConfig = envConfig;
        initTesunConfig(environmentConfig);

        collectedPath = TesunUtilites.checkAndCreateDirectory(envConfig.getCollectsRoot(testPhase), false);
        pseudoRefExportsPath = TesunUtilites.checkAndCreateDirectory(envConfig.getPseudoRefExportsRoot(testPhase), false);
        String strIgnorableXPaths = envConfig.getProperty(TestSupportClientKonstanten.OPT_IGNORABLE_XPATHS, false, "firma-bonitaet/ratingstufe;auftrags-referenz;identification-number;/vsh-firmendatenexport/vsh-firmendaten/korrespondenz-adresse/hausnummer;vsh-firmendatenexport/vsh-firmendaten/bonitaet/bonitaets-index;/vsh-firmendatenexport[1]/vsh-firmendaten[1]/statistik[1]/letzte-aenderung[1]/text()[1]");
        ignorableXPaths = parseIgnorablePaths(strIgnorableXPaths);
    }

    @Override
    public JOB_RESULT call() throws Exception {
        printHeader(Level.INFO, COMMAND, testPhase);
        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "VOR-" + COMMAND, activeCustomersMap);

        notifyTesunClientJobListener(Level.INFO, String.format("\nVergleiche XML's aus \n\t%s und \n\t%s...", collectedPath, pseudoRefExportsPath));
        ExportContentsComparator exportContentsComparator = new ExportContentsComparator(ignorableXPaths, tesunClientJobListener);
        activeCustomersMap.entrySet().forEach(testCustomerEntry -> {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            testCustomer.addTestResultsForCommand(COMMAND);
            testCustomer.refreshRestoredCollects();
            exportContentsComparator.compareFileContents(testCustomer);
            notifyTesunClientJobListener(Level.INFO, ".");
        });
        TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), "NACH-" + COMMAND, activeCustomersMap);
        printFooter(Level.INFO, COMMAND, testPhase);
        return JOB_RESULT.OK;

    }

    protected static List<String> parseIgnorablePaths(String strIgnorableXPaths) {
        List<String> ignorableXPaths = new ArrayList<>();
        if ((strIgnorableXPaths != null) && !strIgnorableXPaths.isEmpty()) {
            String[] ignXPathsArray = strIgnorableXPaths.split(";");
            for (String ignorableXPath : ignXPathsArray) {
                // der XPath-element muss mit "/" beginnen
                if (!ignorableXPath.startsWith("/")) {
                    ignorableXPath = "/" + ignorableXPath;
                }
                String[] ignorableXPathPieces = ignorableXPath.split("/");
                for (int i = 1; i < ignorableXPathPieces.length; i++) {
                    if (!ignorableXPathPieces[i].endsWith("]") && !ignorableXPathPieces[i].startsWith("@")) {
                        // ersetze nur ein Vorkommen, ohne Regular Expression!
                        ignorableXPath = StringUtils.replaceOnce(ignorableXPath, ignorableXPathPieces[i], (ignorableXPathPieces[i] + "[1]"));
                    }
                }
                ignorableXPaths.add(ignorableXPath);
            }
        }
        return ignorableXPaths;
    }

}
