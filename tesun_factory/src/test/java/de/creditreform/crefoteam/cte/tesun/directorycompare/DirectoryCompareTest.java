package de.creditreform.crefoteam.cte.tesun.directorycompare;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import de.creditreform.crefoteam.cte.tesun.TestFallTestBase;
import de.creditreform.crefoteam.cte.tesun.testutil.TesunTestSetupUtil;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.easymock.EasyMock.*;

/**
 * Test-Klasse für {@link DirectoryCompare}
 * User: ralf
 * Date: 12.06.14
 * Time: 10:13
 */
public class DirectoryCompareTest  extends TestFallTestBase {
  final TesunTestSetupUtil setupUtil = new TesunTestSetupUtil();

    @Test
    public void testInitInjector() {
        DirectoryCompare cut = new DirectoryCompare(setupUtil.getTesunClientJobListener());
        Provider<TestSupportMutableState> msp = new Provider<TestSupportMutableState>(){
            @Override
            public TestSupportMutableState get() {
                return null;
            }
        };

        Module guiceModule = cut.getGuiceModule(Charset.forName("UTF-8"), msp);
        Injector injector = Guice.createInjector(guiceModule);
        injector.injectMembers(cut);
    }

    private EnvironmentConfig createConfiguration(String folderSnippet) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("PROPES_FILE_PATH", "./target/test-classes");
        properties.setProperty(TestSupportClientKonstanten.OPT_DIRECTORY_TO_COMPARE_FIRST, "/DirectoryCompareTest/"+folderSnippet + "_1");
        properties.setProperty(TestSupportClientKonstanten.OPT_DIRECTORY_TO_COMPARE_SECOND, "/DirectoryCompareTest/"+folderSnippet + "_2");
        properties.setProperty(TestSupportClientKonstanten.OPT_DIRECTORY_COMPARE_RESULTS, "/DirectoryCompareTest/Results");
        properties.setProperty(TestSupportClientKonstanten.OPT_COMPARE_EXTRACT_FIRST, "0");
        properties.setProperty(TestSupportClientKonstanten.OPT_COMPARE_EXTRACT_FIRST, "0");

        EnvironmentConfig envConfig = createMock(EnvironmentConfig.class);
        expect(envConfig.getDirectoryCompareProperties()).andReturn(properties);
        replay(envConfig);

        return envConfig;
    }

    private DiffListener createDiffListenerVSH() {
        DiffListener diffListener = createMock(DiffListener.class);
        diffListener.init(false, false);
        expectLastCall().once();
        diffListener.notifyPathChange((DirectoryScanResult) anyObject());
        expectLastCall().anyTimes();
        diffListener.firstOnly(getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_1/207/CLZ_207_firstOnly.zip"));
        expectLastCall().once();
        diffListener.secondOnly(getScanResult("./target/test-classes//DirectoryCompareTest/vsh_export_2/203/CLZ_203_secondOnly.zip"));
        expectLastCall().once();
        diffListener.notCompared(getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_1/completioninfo.txt"),
                                 getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_2/completioninfo.txt"));
        expectLastCall().once();
        diffListener.close();
        expectLastCall().once();
        replay(diffListener);
        return diffListener;
    }

    private DirectoryScanResult getScanResult(String fileName) {
        File f = new File(fileName);
        File dir = new File("./src/test/resources");
        return new DirectoryScanResult(dir, f);
    }

    private ZipContentCompare createZipContentCompareVSH() {
        ZipContentCompare zipContentCompare = createMock(ZipContentCompare.class);
        zipContentCompare.compareZipFiles("203_CLZ_203_ab_0600000.zip",
                                          getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_1/203/CLZ_203_ab_0600000.zip"),
                                          getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_2/203/CLZ_203_ab_0600000.zip"));
        expectLastCall().once();
        zipContentCompare.compareZipFiles("203_CLZ_203_ab_2000000.zip",
                                          getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_1/203/CLZ_203_ab_2000000.zip"),
                                          getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_2/203/CLZ_203_ab_2000000.zip"));
        expectLastCall().once();
        zipContentCompare.compareZipFiles("207_CLZ_207_ab_2000000.zip",
                                          getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_1/207/CLZ_207_ab_2000000.zip"),
                                          getScanResult("./target/test-classes/DirectoryCompareTest/vsh_export_2/207/CLZ_207_ab_2000000.zip"));
        expectLastCall().once();
        replay(zipContentCompare);
        return zipContentCompare;
    }

    @Test
    public void testCompareDirectoriesVSH()
    throws Exception {
        DirectoryCompare cut = new DirectoryCompare(setupUtil.getTesunClientJobListener());

        DiffListener diffListener = createDiffListenerVSH();
        ZipContentCompare zipContentCompare = createZipContentCompareVSH();
        Injector injector = Guice.createInjector(new DirectoryCompareJUnitModule(diffListener, zipContentCompare));
        injector.injectMembers(cut);

        cut.init(createConfiguration("vsh_export"));

        cut.call();

        verify(diffListener, zipContentCompare);
    }
    
}
