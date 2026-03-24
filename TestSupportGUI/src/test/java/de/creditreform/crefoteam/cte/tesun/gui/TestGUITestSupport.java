package de.creditreform.crefoteam.cte.tesun.gui;

import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

public class TestGUITestSupport extends BaseGUITest {

    public TestGUITestSupport() {
        super(new TestSupportGUI(new EnvironmentConfig("ENE")));
    }

    @Before
    public void setUp() {
        super.setUp();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void shouldLog() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("log4j.properties");
        System.out.println("resource = " + resource);
        final Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Hello world");
    }

    @Test
    public void testActivitiTestGUI_GetPropertiesFileName() {
        String userDir = System.getProperty("user.dir");
        String fileNameParam = "ENE" + EnvironmentConfig.ENV_PROPFILES_NAME_PATTERN;
        File propsFile = new File(userDir, fileNameParam);
        String propertiesFileName = doTestWithArgs(propsFile.getAbsolutePath());
        Assert.assertTrue(propertiesFileName.startsWith(userDir));

        fileNameParam = "/" + fileNameParam;
        propertiesFileName = doTestWithArgs(fileNameParam);
        Assert.assertTrue(propertiesFileName.contains("target"));
        Assert.assertTrue(propertiesFileName.contains("test-classes"));
        Assert.assertTrue(propertiesFileName.contains(fileNameParam.substring(2)));

        fileNameParam = "." + fileNameParam;
        propertiesFileName = doTestWithArgs(fileNameParam);
        Assert.assertTrue(propertiesFileName.contains(fileNameParam.substring(2)));

    }

    private String doTestWithArgs(String fileNameParam) {
        String[] cmdArgs = new String[]
                {
                        fileNameParam
                };
        String propertiesFileName = getPropertiesFileName(cmdArgs);
        Assert.assertNotNull(propertiesFileName);
        return propertiesFileName;
    }

    protected static String getPropertiesFileName(String[] cmdArgs) {
        File envsFile = null;
        if (cmdArgs.length > 0) {
            String userDir = System.getProperty("user.dir");
            if (cmdArgs[0].startsWith("./")) {
                userDir = System.getProperty("user.dir");
                envsFile = new File(userDir, cmdArgs[0].replace("./", ""));
            } else if (cmdArgs[0].startsWith("/")) {
                URL resource = TestSupportGUI.class.getResource(cmdArgs[0]);
                if (resource != null) {
                    envsFile = new File(resource.getPath());
                }
            } else {
                envsFile = new File(cmdArgs[0]);
            }
            if (envsFile != null && envsFile.exists()) {
                return envsFile.getPath();
            }
            System.out.println("Angegebene Konfigurationsdatei existiert nicht!");
        }
        return null;
    }

}
