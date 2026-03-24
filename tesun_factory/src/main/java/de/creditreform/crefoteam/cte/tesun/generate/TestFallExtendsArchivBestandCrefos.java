package de.creditreform.crefoteam.cte.tesun.generate;

import de.creditreform.crefoteam.cte.tesun.AbstractTesunClientJob;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.*;
import java.util.Calendar;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class TestFallExtendsArchivBestandCrefos extends AbstractTesunClientJob {

    public static final String COMMAND = "UserTask EXTEND-TEST-CREFOS";
    public static final String DESCRIPTION = "Extend AB30-Test-Crefos um Beteiligten und Entscheidungsträger";

    private EnvironmentConfig environmentConfig;
    private AB30MapperUtil ab30MapperUtil;
    private final Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomersMapMap;
    private final File alternateSourceDir;
    private final boolean mustExtendEntgsFromREST;

    public TestFallExtendsArchivBestandCrefos(Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeCustomersMapMap, boolean mustExtendEntgsFromREST, File alternateSourceDir, TesunClientJobListener tesunClientJobListener) {
        super(COMMAND, DESCRIPTION, tesunClientJobListener);
        this.activeCustomersMapMap = activeCustomersMapMap;
        this.alternateSourceDir = alternateSourceDir;
        this.mustExtendEntgsFromREST = mustExtendEntgsFromREST;
    }

    @Override
    public void init(EnvironmentConfig envConfig) throws Exception {
        this.environmentConfig = envConfig;
        for (TestSupportClientKonstanten.TEST_PHASE testPhase : TestSupportClientKonstanten.TEST_PHASE.values()) {
            TesunUtilites.checkAndCreateDirectory(new File(environmentConfig.getArchivBestandsRoot(), testPhase.getDirName()), false);
        }
        ab30MapperUtil = new AB30MapperUtil(environmentConfig, tesunClientJobListener, mustExtendEntgsFromREST, alternateSourceDir);
    }

    @Override
    public JOB_RESULT call() throws Exception {
        Iterator<TestSupportClientKonstanten.TEST_PHASE> phaseIterator = activeCustomersMapMap.keySet().iterator();
        while (phaseIterator.hasNext()) {
            TestSupportClientKonstanten.TEST_PHASE testPhase = phaseIterator.next();
            printHeader(Level.INFO, COMMAND, testPhase);
            doForPhase(testPhase);
            printFooter(Level.INFO, COMMAND, testPhase);
        }
        return JOB_RESULT.OK;
    }

    public void doForPhase(TestSupportClientKonstanten.TEST_PHASE testPhase) throws Exception {
        File ab30XmlsDir = new File(environmentConfig.getArchivBestandsRoot(), testPhase.getDirName());
        Map<String, TestCustomer> customerTestInfoMap = activeCustomersMapMap.get(testPhase);

        notifyTesunClientJobListener(Level.INFO, "\ndoForPhase(" + testPhase.getDirName() + ") :: Initialisiere eine neue AB30XMLProperties-Map aus den Testfällen für in customerTestInfoMap befindlichen Testfällen...");
        Map<Long, AB30XMLProperties> ab30CrefoToPropertiesMap = ab30MapperUtil.initAb30CrefoPropertiesMapFromRefExports("\n", ab30XmlsDir, customerTestInfoMap);

        notifyTesunClientJobListener(Level.INFO, "\ndoForPhase(" + testPhase.getDirName() + ") :: erweitere die Map um AB30XMLProperties-Einträge für Beteiligten bzw. Entschedidungsträger der TestCrefo, falls nicht vorhanden...");
        ab30CrefoToPropertiesMap = ab30MapperUtil.extendAb30CrefoPropertiesMapWithBtlgs("\n", ab30XmlsDir, ab30CrefoToPropertiesMap);

        File testCrefosFile = new File(ab30XmlsDir, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        notifyTesunClientJobListener(Level.INFO, "\ndoForPhase(" + testPhase.getDirName() + ") :: Ergänze Attributes von AB30XMLProperties-Map  aus altem 'TestCrefos.properties' - Datei...");
        ab30CrefoToPropertiesMap = ab30MapperUtil.extendAb30CrefoPropertiesWithOldAttributes("\n", testCrefosFile, ab30CrefoToPropertiesMap);

        File newPorpsFile = backupAndCreateNewFile(testPhase, ab30XmlsDir, TestSupportClientKonstanten.TEST_CREFOS_PROPS_FILENAME);
        notifyTesunClientJobListener(Level.INFO,testPhase.getDirName() + ") :: Erzeuge Datei '" + newPorpsFile.getName() + "'...");
        ab30MapperUtil.writeAb30CrefoToPropertiesMapToFile(newPorpsFile, ab30CrefoToPropertiesMap);

        newPorpsFile = backupAndCreateNewFile(testPhase, ab30XmlsDir, TestSupportClientKonstanten.CREFOS_TO_CUSTOMERS_MAP_FILENAME);
        notifyTesunClientJobListener(Level.INFO,testPhase.getDirName() + ") :: Erzeuge Datei " + TestSupportClientKonstanten.CREFOS_TO_CUSTOMERS_MAP_FILENAME + ", in der die Crefos gruppiert nach Kunde aufgelistet werden");
        ab30MapperUtil.writeCrefoToCustomerMappingFile(newPorpsFile, ab30CrefoToPropertiesMap);
    }

    private File backupAndCreateNewFile(TestSupportClientKonstanten.TEST_PHASE testPhase, File ab30PhaseXmlsDir, String fileName) {
        File newFile = new File(ab30PhaseXmlsDir, fileName);
        if(newFile.exists()) {
            notifyTesunClientJobListener(Level.INFO,testPhase.getDirName() + ") :: Bennene '" + fileName + "' um...");
            File oldFile = new File(ab30PhaseXmlsDir, fileName + DateFormatUtils.format(Calendar.getInstance(), ".yyyy-MM-dd HH-mm-ss"));
            oldFile.delete();
            boolean isOK = newFile.renameTo(oldFile);
            if(!isOK) {
                throw new RuntimeException("ExtendArchivBestandCrefos#doForPhase(" + testPhase.getDirName() + ") :: Konnte '" + fileName + "' nicht umbenennen!");
            }
        }
        return newFile;
    }
}