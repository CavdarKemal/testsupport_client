package de.creditreform.crefoteam.cte.tesun.generate;

import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.AB30MapperUtil;
import de.creditreform.crefoteam.cte.tesun.util.AB30XMLProperties;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestFallFileUtil;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import de.creditreform.crefoteam.cte.tesun.util.directorytree.FileActionFactoryCopyReplace;
import de.creditreform.crefoteam.cte.tesun.util.directorytree.TreeProcessor;
import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacementMapping;
import de.creditreform.crefoteam.cte.tesun.util.replacer.Replacer;
import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacerFactory;
import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacerParameterException;
import de.creditreform.crefoteam.cte.tesun.util.replacer.ReplacerUtils;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.inject.Inject;
import org.apache.log4j.Level;

public class TestFallGeneratePseudoCrefos extends AbstractTesunClientJob {

    public static boolean USE_EXISTING_CREFOS_MAPPINGS_FILE = false;
    public static final String COMMAND = "UserTask GENERATE-PSEUDO-CREFOS";
    public static final String DESCRIPTION = "invoke generator to create new xml from AB30Docs templates";

    private final TestSupportClientKonstanten.TEST_TYPES testType;
    private final Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomersMapMap;
    private ReplacerFactory replacerFactory;
    private EnvironmentConfig environmentConfig;
    private AB30MapperUtil ab30MapperUtil;
    @Inject
    private TestSupportMutableState mutableState;

    public TestFallGeneratePseudoCrefos(TestSupportClientKonstanten.TEST_TYPES testType, Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomersMapMap, TesunClientJobListener tesunClientJobListener) {
        super(COMMAND, DESCRIPTION, tesunClientJobListener);
        this.activeCustomersMapMap = activeCustomersMapMap;
        this.testType = testType;
    }

    @Override
    public void init(EnvironmentConfig environmentConfig) throws Exception {
        this.environmentConfig = environmentConfig;

        final File mappingsFile = new File(environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.fileNameCrefosMapping);
        if(mappingsFile.exists()) {
            mappingsFile.renameTo(new File(mappingsFile +  "-" + System.nanoTime()));
        }
        Iterator<TestSupportClientKonstanten.TEST_PHASE> phaseIterator = activeCustomersMapMap.keySet().iterator();
        while (phaseIterator.hasNext()) {
            TestSupportClientKonstanten.TEST_PHASE testPhase = phaseIterator.next();
            TesunUtilites.checkAndCreateDirectory(environmentConfig.getArchivBestandsRoot(testPhase), false);
            TesunUtilites.checkAndCreateDirectory(environmentConfig.getPseudoArchivBestandsRoot(testPhase), true);
        }
        ab30MapperUtil = new AB30MapperUtil(environmentConfig, tesunClientJobListener, environmentConfig.mustExtendEntgsFromREST(), null);
    }

    @Override
    public JOB_RESULT call() throws Exception {
        Iterator<TestSupportClientKonstanten.TEST_PHASE> phaseIterator = activeCustomersMapMap.keySet().iterator();
        while (phaseIterator.hasNext()) {
            TestSupportClientKonstanten.TEST_PHASE testPhase = phaseIterator.next();
            printHeader(Level.INFO, COMMAND, testPhase);
            TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), testPhase+"-VOR-" + COMMAND, activeCustomersMapMap.get(testPhase));
        }
        replacerFactory = new ReplacerFactory( Replacer.CHARSET_UTF8);

        // Properties-Datei aus PHASE-2 einlesen und die AB30XMLProperties-Map initialisieren...
        File ab30XmlsDirP2 = environmentConfig.getArchivBestandsRoot(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        File testCrefosFileP2 = new File(ab30XmlsDirP2, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap = ab30MapperUtil.initAb30CrefoPropertiesMap(testCrefosFileP2);

        //  für <activeCustomersMap> filtern
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<Long, AB30XMLProperties>> filteredAb30XMLPropertiesMapMap = new TreeMap<>();
        Map<String, TestCustomer> customerTestInfoMapP1 = activeCustomersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_1);
        Map<Long, AB30XMLProperties> filteredAb30XMLPropertiesMapP1 = ab30MapperUtil.filterAb30CrefoPropertiesMap("\n", ab30CrefoToPropertiesMap, customerTestInfoMapP1, true);
        filteredAb30XMLPropertiesMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, filteredAb30XMLPropertiesMapP1);
        Map<String, TestCustomer> customerTestInfoMapP2 = activeCustomersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2);
        Map<Long, AB30XMLProperties> filteredAb30XMLPropertiesMapP2 = ab30MapperUtil.filterAb30CrefoPropertiesMap("\n", ab30CrefoToPropertiesMap, customerTestInfoMapP2, true);
        filteredAb30XMLPropertiesMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, filteredAb30XMLPropertiesMapP2);

        // Replacer aus PHASE-2, da dort alle Crefos (auch die für PHASE-1) enthalten sind!
        Replacer replacer = getReplacer(filteredAb30XMLPropertiesMapP2);
        doTheReplacements(replacer, filteredAb30XMLPropertiesMapMap);
        ReplacerUtils.saveMappingFiles(environmentConfig.getTestOutputsRoot(), customerTestInfoMapP2, replacer.getReplacementMappingMap());

        phaseIterator = activeCustomersMapMap.keySet().iterator();
        while (phaseIterator.hasNext()) {
            TestSupportClientKonstanten.TEST_PHASE testPhase = phaseIterator.next();
            TesunUtilites.dumpCustomers(environmentConfig.getLogOutputsRoot(), testPhase+"-NACH-" + COMMAND, activeCustomersMapMap.get(testPhase));
            printFooter(Level.INFO, COMMAND, testPhase);
        }
        return JOB_RESULT.OK.setUserObject(activeCustomersMapMap);
    }

    protected void doTheReplacements(Replacer replacer, Map<TestSupportClientKonstanten.TEST_PHASE, Map<Long, AB30XMLProperties>> ab30XMLPropertiesMapMap) throws Exception {
        // REF_EXPORTS der aktiven Kunden ver412ern
        Iterator<TestSupportClientKonstanten.TEST_PHASE> phaseIterator = activeCustomersMapMap.keySet().iterator();
        while (phaseIterator.hasNext()) {
            doReplaceRefExports(replacer, phaseIterator.next());
        }
        // für die relevanten Test-Crefos in die Datei "ITSQ\tesfaelle_cte\PHASE-X\UsedTestCrefos.properties" speichern
        phaseIterator = activeCustomersMapMap.keySet().iterator();
        while (phaseIterator.hasNext()) {
            doReplaceUsedTestCrefosFile(replacer, phaseIterator.next(), ab30XMLPropertiesMapMap);
        }
    }

    protected void doReplaceRefExports(Replacer replacer, TestSupportClientKonstanten.TEST_PHASE testPhase) throws InterruptedException, ExecutionException {
        notifyTesunClientJobListener(Level.INFO, String.format("\nIteriere ueber die REF_EXPORTS- und TEST_CASES- Verzeichnise für die aktiven Kunden in der Test-Phase '" + testPhase + "..."));
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, FutureTask> futureTasksMap = new HashMap<>();
        Map<String, TestCustomer> activeCustomersMap = activeCustomersMapMap.get(testPhase);
        activeCustomersMap.entrySet().forEach(testCustomerEntry -> {
            TestCustomer testCustomer = testCustomerEntry.getValue();
            if (testCustomer.isActivated()) {
                testCustomer.addTestResultsForCommand(COMMAND);
                notifyTesunClientJobListener(Level.INFO, String.format("\n\tIteriere ueber die REF_EXPORTS- und TEST_CASES- Verzeichnise für den Kunden '%s'...", testCustomer.getCustomerKey()));
                CustomerPseudoGenerator customerPseudoGenerator = new CustomerPseudoGenerator(testCustomer, testPhase, replacer, tesunClientJobListener);
                FutureTask<CustomerPseudoGenerator> futureTask = new FutureTask(customerPseudoGenerator);
                futureTasksMap.put(testCustomer.getCustomerKey(), futureTask);
                executor.execute(futureTask);
            }
        });
        executor.shutdown();
        notifyTesunClientJobListener(Level.INFO, String.format("\nWarte auf Beendigung der FutureTasks..."));
        TesunUtilites.waitForFutureTasks(futureTasksMap, tesunClientJobListener);
    }
    protected void doReplaceUsedTestCrefosFile(Replacer replacer, TestSupportClientKonstanten.TEST_PHASE testPhase, Map<TestSupportClientKonstanten.TEST_PHASE, Map<Long, AB30XMLProperties>> ab30XMLPropertiesMapMap) throws Exception {
        // ab30XMLPropertiesMapMap-Infos in die Datei "ITSQ\tesfaelle_cte\ARCHIV-BESTAND/PHASE-X/UsedTestCrefos.properties" speichern
        File archivBestandsRoot = environmentConfig.getArchivBestandsRoot(testPhase);
        final File newPorpsFilePhaseX = new File(archivBestandsRoot, TestSupportClientKonstanten.USED_CREFOS_PROPS_FILENAME);
        ab30MapperUtil.writeAb30CrefoToPropertiesMapToFile(newPorpsFilePhaseX,  ab30XMLPropertiesMapMap.get(testPhase));

        // Die Test-Crefos aus "ITSQ\tesfaelle_cte\ARCHIV-BESTAND/PHASE-X/UsedTestCrefos.properties"" ver412ern
        AB30DataFilesFilter aB30DataFilesFilterPhase2 = new AB30DataFilesFilter(ab30XMLPropertiesMapMap.get(testPhase));
        File pseudoArchivBestandsRoot = environmentConfig.getPseudoArchivBestandsRoot(testPhase);
        doReplacementForFile(archivBestandsRoot, pseudoArchivBestandsRoot, aB30DataFilesFilterPhase2, replacer);
    }

    protected void doReplacementForFile(File srcDir, File dstDir, FileFilter fileFilter, Replacer replacer) throws Exception {
        notifyTesunClientJobListener(Level.INFO, String.format("\nIteriere ueber das Verzeichnis %s..", srcDir.getAbsolutePath()));
        // Kopien im neuen Verzeichnis anlegen
        notifyTesunClientJobListener(Level.INFO, String.format("\nKopien im neuen Verzeichnis %s anlegen...", dstDir.getAbsolutePath()));

        FileActionFactoryCopyReplace fileActionFactoryCopyReplace = new FileActionFactoryCopyReplace(replacer, srcDir, dstDir);
        TreeProcessor treeProcessor = new TreeProcessor(fileFilter, srcDir, fileActionFactoryCopyReplace);
        treeProcessor.setTesunClientJobListener(tesunClientJobListener);
        treeProcessor.call();
    }

    protected Replacer getReplacer(Map<Long, AB30XMLProperties> usedAb30CrefoToPropertiesMap) throws Exception {
        Replacer replacer;
        Map<String, ReplacementMapping> replacementMappingMap = new TreeMap<>();
        final File mappingsFile = new File(environmentConfig.getTestOutputsRoot(), TestSupportClientKonstanten.fileNameCrefosMapping);
        if (USE_EXISTING_CREFOS_MAPPINGS_FILE && mappingsFile.exists()) {
            replacementMappingMap = TestFallFileUtil.readReplacementMappingFromFile(mappingsFile);
            try {
                replacer = createReplacerForCrefosMap(replacementMappingMap, usedAb30CrefoToPropertiesMap);
            } catch (Exception ex) {
                throw new RuntimeException("Fehler beim Parsen der Mapping-Datei '" + mappingsFile.getName() + "\n" + ex.getMessage());
            }
        } else {
            Long nextCrefo = mutableState.getNextCrefo();
            replacer = createReplacerFor(replacementMappingMap, usedAb30CrefoToPropertiesMap, nextCrefo);
        }
        return replacer;
    }

    protected Replacer createReplacerFor(Map<String, ReplacementMapping> replacementMappingMap, Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap, long replacementCrefo) throws PropertiesException {
        Long nextCrefo = replacementCrefo;
        notifyTesunClientJobListener(Level.INFO, String.format("\nZuletzt benutzte Test-Crefo ist %d. Neue Test-Crefos werden ab %d vergeben.", replacementCrefo - 1, nextCrefo));
        for (Map.Entry<Long, AB30XMLProperties> ab30CrefoToPropertiesEntry : ab30CrefoToPropertiesMap.entrySet()) {
            AB30XMLProperties ab30XMLProperties = null;
            try {
                ab30XMLProperties = ab30CrefoToPropertiesEntry.getValue();
                Long oldCrefo = ab30XMLProperties.getCrefoNr();
                nextCrefo = doTheInsert(replacementMappingMap, oldCrefo, nextCrefo);
            } catch (ReplacerParameterException ex) {
                // ist OK! notifyTesunClientJobListener(Level.INFO, String.format("\n\t! Die Crefo %d wurde schon aufgenommen.", ab30XMLProperties.getCrefoNr()));
            }
            for (Long oldCrefo : ab30XMLProperties.getBtlgCrefosList()) {
                try {
                    nextCrefo = doTheInsert(replacementMappingMap, oldCrefo, nextCrefo);
                } catch (ReplacerParameterException ex) {
                    // ist OK! notifyTesunClientJobListener(Level.INFO, String.format("\n\t! Die Beteiligten-Crefo %d wurde schon aufgenommen.", oldCrefo));
                }
            }
        }
        return replacerFactory.create(replacementMappingMap, true);
    }

    protected Long doTheInsert(Map<String, ReplacementMapping> replacementMappingMap, Long oldCrefo, Long nextCrefo) throws PropertiesException {
        // Die AT und LU-Crefos nicht ver412ern, sondern ver912ern oder ver383ern
        Long crfAsReplacement = getReplacementForClzRange(oldCrefo, nextCrefo);
        replacerFactory.insertCrefoReplacement(replacementMappingMap, oldCrefo, crfAsReplacement);
        return ++nextCrefo;
    }

    private Long getReplacementForClzRange(Long oldCrefo, Long newCrefo) throws PropertiesException {
        Integer targetClz = null;
        String strNewCrefo = "";
        if (oldCrefo >= 9370000000L) {
            if (oldCrefo >= 9390000000L) {
                targetClz = environmentConfig.getTargetClzForLuPseudoCrefos();
            }
            else {
                targetClz = environmentConfig.getTargetClzForAtPseudoCrefos();
            }
            String strDeClz = String.valueOf(environmentConfig.getTargetClzForDePseudoCrefos());
            String strNewClz = String.valueOf(targetClz);
            strNewCrefo = newCrefo.toString().replace(strDeClz, strNewClz);
            return Long.valueOf(strNewCrefo);
        }
        return newCrefo;
    }

    protected Replacer createReplacerForCrefosMap(Map<String, ReplacementMapping> replacementMappingMap, Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap) {
        for (Map.Entry<Long, AB30XMLProperties> ab30CrefoToPropertiesEntry : ab30CrefoToPropertiesMap.entrySet()) {
            AB30XMLProperties ab30XMLProperties = null;
            try {
                ab30XMLProperties = ab30CrefoToPropertiesEntry.getValue();
                Long crefoNr = ab30XMLProperties.getCrefoNr();
                ReplacementMapping replacementMapping = replacementMappingMap.get(crefoNr.toString());
                Long crfAsReplacement = replacementMapping.getTargetCrefo();
                if (crfAsReplacement == null) {
                    throw new RuntimeException("Die Map enthält keinen Eintrag für die Crefo " + crefoNr);
                }
                replacerFactory.insertCrefoReplacement(replacementMappingMap, crefoNr, crfAsReplacement);
            } catch (ReplacerParameterException ex) {
                // is OK. notifyTesunClientJobListener(Level.INFO, String.format("\n\t! Die Crefo %d wurde schon aufgenommen.", ab30XMLProperties.getCrefoNr()));
            }
            for (Long entgCrefo : ab30XMLProperties.getBtlgCrefosList()) {
                try {
                    ReplacementMapping replacementMapping = replacementMappingMap.get(entgCrefo.toString());
                    Long crfAsReplacement = replacementMapping.getTargetCrefo();
                    replacerFactory.insertCrefoReplacement(replacementMappingMap, entgCrefo, crfAsReplacement);
                } catch (ReplacerParameterException ex) {
                    // is OK. notifyTesunClientJobListener(Level.INFO, String.format("\n\t! Die Crefo %d wurde schon aufgenommen.", entgCrefo));
                }
            }
        }
        return replacerFactory.create(replacementMappingMap, true);
    }

}