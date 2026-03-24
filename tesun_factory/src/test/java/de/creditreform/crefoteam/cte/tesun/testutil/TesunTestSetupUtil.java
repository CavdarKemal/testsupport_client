package de.creditreform.crefoteam.cte.tesun.testutil;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigExportInfo;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.exports_checker.XMLFragments;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.technischebasis.log4j.SystemOutAppender;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class TesunTestSetupUtil {
    private SystemOutAppender appender;
    public static Logger logger;
    private EnvironmentConfig environmentConfig;

    public TesunTestSetupUtil setUp() {
        SystemOutAppender temporaryAppender = SystemOutAppender.INFO().installIntoRootLogger();
        logger = LoggerFactory.getLogger(TesunTestSetupUtil.class);
        temporaryAppender.removeFromRootLogger();

        try {
            environmentConfig = new EnvironmentConfig("ENE");
            File testRscRootDir = new File(environmentConfig.getTestResourcesRoot(), "LOCAL");
            environmentConfig.setTestResourcesDir(testRscRootDir);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        return this;
    }

    public void teardown() {
        if (appender != null) {
            appender.removeFromCT();
        }
    }

    public TesunClientJobListener getTesunClientJobListener() {
        return new TesunClientJobListener() {
            @Override
            public void notifyClientJob(Level level, Object notifyObject) {
                System.out.println(notifyObject.toString());
            }

            @Override
            public Object askClientJob(ASK_FOR askFor, Object userObject) {
                System.out.println(askFor.toString());
                if (askFor.equals(ASK_FOR.ASK_OBJECT_TEST_TYPE)) {
                  return TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2;
                }
                return null;
            }
        };
    }

    public EnvironmentConfig getEnvironmentConfig() {
        return environmentConfig;
    }

    public Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> readTestCustomersMapMap() throws Exception {
        Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> customerTestInfoMapMap = new TreeMap<>();
        customerTestInfoMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_1, readTestCustomersMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_1));
        customerTestInfoMapMap.put(TestSupportClientKonstanten.TEST_PHASE.PHASE_2, readTestCustomersMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2));
        return customerTestInfoMapMap;
    }

    public Map<String, TestCustomer> readTestCustomersMap(TestSupportClientKonstanten.TEST_PHASE thePhase) throws Exception {
        EnvironmentConfig environmentConfig = getEnvironmentConfig();
        Map<String, TestCustomer> customerTestInfoMap = environmentConfig.getCustomerTestInfoMap(thePhase);
        return customerTestInfoMap;
    }

    public TesunConfigInfo getTesunConfigInfo(boolean isLocal) {
        return isLocal ? getTesunConfigInfoForLocal() : getTesunConfigInfoForSFTP();
    }

    private TesunConfigInfo getTesunConfigInfoForSFTP() {
        try {
         RestInvokerConfig restServiceConfigTesun = getEnvironmentConfig().getRestServiceConfigsForMasterkonsole().get(0);
            TesunRestService tesunRestService = new TesunRestService(restServiceConfigTesun, new TesunClientJobListener() {
                @Override
                public void notifyClientJob(Level level, Object notifyObject) {
                }

                @Override
                public Object askClientJob(ASK_FOR askFor, Object userObject) {
                    return null;
                }
            });
            TesunConfigInfo tesunConfigInfo = tesunRestService.getTesunConfigInfo();
            return tesunConfigInfo;
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }

    private TesunConfigInfo getTesunConfigInfoForLocal() {
        try {
            Map<String, TestCustomer> customerTestInfoMap = getEnvironmentConfig().getCustomerTestInfoMap(TestSupportClientKonstanten.TEST_PHASE.PHASE_2); // !!!!!!!!!!! TODO
            TesunConfigInfo tesunConfigInfo = new TesunConfigInfo();
            tesunConfigInfo.setUmgebungsKuerzel("ene");
            String exportsPath = new File(getEnvironmentConfig().getTestResourcesRoot(), TestSupportClientKonstanten.EXPORTS).getAbsolutePath();
            Set<Map.Entry<String, TestCustomer>> entries = customerTestInfoMap.entrySet();
            for (Map.Entry<String, TestCustomer> testCustomerEntry : entries) {
                TesunConfigExportInfo exportPfad = new TesunConfigExportInfo();
                String customerKey = testCustomerEntry.getKey();
                exportPfad.setKundenKuerzel(customerKey.toUpperCase());
                exportPfad.setNamedAs(String.format("cteTestclient.%s.exportDir", customerKey));
                exportPfad.setRelativePath(String.format("%s/export/delta", customerKey.toLowerCase())); // Kunden-Kürzel im Dateinamen immer in Kleinbuchstaben!
                tesunConfigInfo.getExportPfade().add(exportPfad);
            }
            return tesunConfigInfo;
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }

    public String[] prepareForCompareVSDExporte() throws Exception {
        return new String[]{
                "ENE",
                "directorycompare"
        };
    }

    public Map<String, File> createDirWithSubdirs(String basePath, String[] subDirNames) throws IOException {
        Map<String, File> subDirsMap = new TreeMap<>();
        for (String subDirName : subDirNames) {
            File subDir = new File(basePath, subDirName);
            FileUtils.forceMkdir(subDir);
            subDirsMap.put(subDirName, subDir);
        }
        return subDirsMap;
    }

    public void writeModifiedXMLDocument(File theFile, String[] xpathExpressions, String[] newValues) throws Exception {
        String modifiedXMLContent = XMLFragments.getModifiedXMLContent(xpathExpressions, newValues);
        FileUtils.writeStringToFile(theFile, modifiedXMLContent, Charset.forName("UTF-8"));
    }

    private static List<String> readTestFileNamesFromPropFile(File thePropsFile, boolean withNonTests) {
        List<String> result = new ArrayList<>();
        try {
            List<String> readLines = FileUtils.readLines(thePropsFile);
            for (String line : readLines) {
                String[] split = line.split("=");
                if ((split != null) && (split.length == 2) && (withNonTests || !split[0].startsWith("n"))) {
                    result.add(split[1] + ".xml");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public void checkMessageFile(File msgFile, List<String> strLines) throws IOException {
        Assert.assertTrue(msgFile.exists());
        String strContent = FileUtils.readFileToString(msgFile);
        int nLines = 0;
        for (String strLine : strLines) {
            Assert.assertTrue(("Text '" + strLine + "' ist in der Datei nicht enthalten!"), strContent.contains(strLine));
            nLines++;
        }
        Assert.assertTrue("Die Datei enthält mehr Zeilen als erwartet! ", strLines.size() == nLines);
    }

    public void configureLog4JProperties() throws URISyntaxException {
        String LOG4J_PROPS_FILE_NAME = "/log4j.junit.properties";
        URL resource = TesunTestSetupUtil.class.getResource(LOG4J_PROPS_FILE_NAME);
        if (resource != null) {
            PropertyConfigurator.configure(resource.toURI().getPath());
        }
    }

    public TesunTestSetupUtil installAppender() {
        appender = SystemOutAppender.INFO().installIntoCT();
        return this;
    }

    public static class FileFilterNoXml implements FileFilter {
        @Override
        public boolean accept(File theFile) {
            return theFile.isDirectory() || !theFile.getName().endsWith(".xml");
        }
    }

    public static class FileFilterXml implements FileFilter {
        @Override
        public boolean accept(File theFile) {
            return theFile.isDirectory() || theFile.getName().endsWith(".xml");
        }
    }

    public static class FileFilterProperties implements FileFilter {
        @Override
        public boolean accept(File theFile) {
            return theFile.isDirectory() || theFile.getName().endsWith(".properties");
        }
    }

    public static class FileFilterXmlAndProperties implements FileFilter {
        @Override
        public boolean accept(File theFile) {
            return theFile.isDirectory() || theFile.getName().endsWith(".xml") || theFile.getName().endsWith(".properties");
        }
    }

}
