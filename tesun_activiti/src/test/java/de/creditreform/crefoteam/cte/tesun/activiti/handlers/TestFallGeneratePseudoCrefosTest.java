package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import de.creditreform.crefoteam.cte.tesun.generate.TestFallGeneratePseudoCrefos;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestFallGeneratePseudoCrefosTest extends UserTaskTestBase {

    Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap = new TreeMap<>();
    Provider<TestSupportMutableState> mutableStateProvider = new Provider<>() {
        @Override
        public TestSupportMutableState get() {
            return () -> ++currentCrefo;
        }
    };
    long currentCrefo = 4120000000L;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        activeTestCustomersMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, getTestCustomerMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, false));
        activeTestCustomersMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, getTestCustomerMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, false));
    }

    @Test
    public void testFor() throws Exception {
        TestFallGeneratePseudoCrefos cut = new TestFallGeneratePseudoCrefos(TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2, activeTestCustomersMapMap, this);
        Module guiceModule = cut.getGuiceModule(Charset.forName("UTF-8"), mutableStateProvider);
        Injector injector = Guice.createInjector(guiceModule);
        injector.injectMembers(cut);

        cut.init(environmentConfig);
        cut.call();

        // Erzeugte "UsedTestCrefos.properties" im LOCAL\ARCHIV-BESTAND\PHASE-1 prüfen...
        File abPhaseRootFile = new File(environmentConfig.getArchivBestandsRoot(), (TestSupportClientKonstanten.TEST_PHASE.PHASE_1.getDirName()));
        checkContent(readContent(abPhaseRootFile),
                Arrays.asList(
                        "4112000001::[RTN],[],[4112000008;4112000009],[KEINE],[KEINE],[],[]",
                        "4112000002::[RTN;BVD],[],[4112000003],[KEINE],[KEINE],[],[]",
                        "4112000003::[RTN;BVD],[],[],[KEINE],[KEINE],[],[]", "4112000008::[BVD;RTN],[],[],[KEINE],[KEINE],[],[]",
                        "4112000009::[BVD;RTN],[],[4112000002;4112000003],[KEINE],[KEINE],[],[]"));

        // Erzeugte "UsedTestCrefos.properties" im TEST-OUTPUTS\PSEUDO-ARCHIV-BESTAND\PHASE-1 prüfen...
        File psPhaseRootFile = new File(environmentConfig.getPseudoArchivBestandsRoot(), (TestSupportClientKonstanten.TEST_PHASE.PHASE_1.getDirName()));
        checkContent(readContent(psPhaseRootFile),
                Arrays.asList(
                        "4120000005::[RTN],[],[4120000001;4120000002],[KEINE],[KEINE],[],[]",
                        "4120000003::[RTN;BVD],[],[4120000004],[KEINE],[KEINE],[],[]",
                        "4120000004::[RTN;BVD],[],[],[KEINE],[KEINE],[],[]",
                        "4120000001::[BVD;RTN],[],[],[KEINE],[KEINE],[],[]",
                        "4120000002::[BVD;RTN],[],[4120000003;4120000004],[KEINE],[KEINE],[],[]"));

        // Erzeugte XML-Dateien im TEST-OUTPUTS\PSEUDO-ARCHIV-BESTAND\PHASE-1 prüfen...
        checkGeneratedXmlFiles(psPhaseRootFile, Arrays.asList("4120000001.xml", "4120000002.xml", "4120000003.xml", "4120000004.xml", "4120000005.xml"));

        // Erzeugte XML-Dateien im TEST-OUTPUTS\PSEUDO-REF-EXPORTS\PHASE-1 prüfen...
        File pseudoRefExportsPhaseFile = new File(environmentConfig.getPseudoRefExportsRoot(), (TestSupportClientKonstanten.TEST_PHASE.PHASE_1.getDirName()));
        checkGeneratedXmlFiles(new File(pseudoRefExportsPhaseFile, "rtn/Relevanz_Positiv"), Arrays.asList("p01_4120000005.xml", "p02_4120000003.xml"));
    }

    private void checkGeneratedXmlFiles(File rootFile, List<String> xmlFilenamesList) {
        xmlFilenamesList.forEach(xmlFilename -> Assert.assertTrue("XML-Datei '" + xmlFilename + " existiert im Verzeichnis " + rootFile.getAbsolutePath() + " nicht!", new File(rootFile, xmlFilename).exists()));
    }

    private void checkContent(List<String> actualList, List<String> expectedList) {
        expectedList.forEach(expectedStr -> {
            Assert.assertTrue("Aktuelle Liste enthält den Eintrag '" + expectedStr + "' nicht!", actualList.contains(expectedStr));
        });
    }

    private static List<String> readContent(File phaseRootFile) throws Exception {
        File usedTestCrefosFile = new File(phaseRootFile, TestSupportClientKonstanten.USED_CREFOS_PROPS_FILENAME);
        List<String> usedTestCrefosContentFile = FileUtils.readLines(usedTestCrefosFile);
        return usedTestCrefosContentFile;
    }
}
