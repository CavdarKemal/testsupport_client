package de.creditreform.crefoteam.cte.tesun.gui.utils;

import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestCaseFilesFromGitTest {
    static final File TEST_BASE_DIR = new File(System.getProperty("java.io.tmpdir"));
    static final File TEST_BASE_ITSQ_DIR = new File(TEST_BASE_DIR, "ITSQ");
    static final String READ_ME_FILE_NAME = "README.md";
    static final String READ_ME_FILE_CONTENT_TEST_UTILS = "### Testfaelle-CTE ";
    static final String READ_ME_FILE_CONTENT_TEST_FAELLE = "### Testfaelle-CTE ";
    static final String DIES_IST_IM_MASTER = "Dies ist im Branch master!";
    static final String DIES_IST_IM_TEST_BRANCH = "Dies ist im Branch TEST_BRANCH!";

    static final CommandExecutorListener commandExecutorListener = strInfo -> {
        if(!strInfo.contains("Updating files")) {
            System.out.print(strInfo);
        }
    };
    static final TestCaseFilesFromGit cut = new TestCaseFilesFromGit(commandExecutorListener);

    private EnvironmentConfig environmentConfig = new EnvironmentConfig("ENE");

    @Before
    public void setUp() {
        FileUtils.deleteQuietly(TEST_BASE_ITSQ_DIR);
    }

    @Test
    public void testCloneSchnittStelleAb30Project() throws Exception {
        EnvironmentConfig.GitProjectInfo testUtilsGitRepo = environmentConfig.getSchnittstelleAb30GitRepo("master");
        File cloneTargetDir = new File(TEST_BASE_ITSQ_DIR, testUtilsGitRepo.getLocalRepoName());
        // clone testUtilsGitRepo
        cut.cloneGitProject(testUtilsGitRepo, cloneTargetDir);
        Assert.assertTrue(cloneTargetDir.exists());
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_UTILS});
    }
    @Test
    public void testCloneTestUtilsProject() throws Exception {
        EnvironmentConfig.GitProjectInfo testUtilsGitRepo = environmentConfig.getTestUtilsGitRepo("master");
        File cloneTargetDir = new File(TEST_BASE_ITSQ_DIR, testUtilsGitRepo.getLocalRepoName());
        // clone testUtilsGitRepo
        cut.cloneGitProject(testUtilsGitRepo, cloneTargetDir);
        Assert.assertTrue(cloneTargetDir.exists());
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_UTILS});
    }

    @Test
    public void testUpdateTestUtilsProject() throws Exception {
        EnvironmentConfig.GitProjectInfo testUtilsGitRepo = environmentConfig.getTestUtilsGitRepo("master");
        // clone testUtilsGitRepo
        File cloneTargetDir = new File(TEST_BASE_ITSQ_DIR, testUtilsGitRepo.getLocalRepoName());
        cut.cloneGitProject(testUtilsGitRepo, cloneTargetDir);
        Assert.assertTrue(cloneTargetDir.exists());
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_UTILS});

        // update testUtilsGitRepo
        cut.updateGitProject(testUtilsGitRepo, cloneTargetDir);
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_UTILS});
    }

    @Test
    public void testCloneTestFaelleProject() throws Exception {
        EnvironmentConfig.GitProjectInfo itsqTestfaelleGitRepo = environmentConfig.getItsqTestfaelleGitRepo("master");
        File cloneTargetDir = new File(TEST_BASE_ITSQ_DIR, itsqTestfaelleGitRepo.getLocalRepoName());
        // clone itsqTestfaelleGitRepo
        cut.cloneGitProject(itsqTestfaelleGitRepo, cloneTargetDir);
        Assert.assertTrue(cloneTargetDir.exists());
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_MASTER});
    }

    @Test
    public void testUpdateTestFaelleProject() throws Exception {
        EnvironmentConfig.GitProjectInfo itsqTestfaelleGitRepo = environmentConfig.getItsqTestfaelleGitRepo("master");
        File cloneTargetDir = new File(TEST_BASE_ITSQ_DIR, itsqTestfaelleGitRepo.getLocalRepoName());
        // clone itsqTestfaelleGitRepo
        cut.cloneGitProject(itsqTestfaelleGitRepo, cloneTargetDir);
        Assert.assertTrue(cloneTargetDir.exists());
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_MASTER});

        // clone itsqTestfaelleGitRepo from master
        cut.updateGitProject(itsqTestfaelleGitRepo, cloneTargetDir);
        // check, ob README.md aus dem korrekten Branch kommt...
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_MASTER});

        // Update itsqTestfaelleGitRepo to TEST_BRANCH
        itsqTestfaelleGitRepo.setGitRepoRevision("TEST_BRANCH");
        cut.updateGitProject(itsqTestfaelleGitRepo, cloneTargetDir);
        // check, ob README.md aus dem korrekten Branch kommt...
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_TEST_BRANCH});
    }

    @Test
    public void testCloneOrUpdateTestFaelleProject() throws Exception {
        EnvironmentConfig.GitProjectInfo itsqTestfaelleGitRepo =  environmentConfig.getItsqTestfaelleGitRepo("master");
        File cloneTargetDir = new File(TEST_BASE_ITSQ_DIR, itsqTestfaelleGitRepo.getLocalRepoName());
        // clone or update itsqTestfaelleGitRepo
        cut.cloneOrUpdateGitProject(itsqTestfaelleGitRepo, cloneTargetDir);
        // check, ob README.md aus dem korrekten Branch kommt...
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_MASTER});
    }

    @Test
    public void testUpdateItsqTestPaket() throws Exception {
        List<EnvironmentConfig.GitProjectInfo> gitReposList = new ArrayList<>();
        EnvironmentConfig.GitProjectInfo itsqTestfaelleGitRepo = environmentConfig.getItsqTestfaelleGitRepo("master");
        gitReposList.add(itsqTestfaelleGitRepo);
        EnvironmentConfig.GitProjectInfo testUtilsGitRepo = environmentConfig.getTestUtilsGitRepo("master");
        gitReposList.add(testUtilsGitRepo);

        // Update gitReposList to master
        File cloneTargetDir = cut.updateItsqTestPaket(TEST_BASE_DIR, gitReposList);
        // check, ob README.md aus dem korrekten Branch kommt...
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_MASTER});
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_UTILS});

        // Update itsqTestfaelleGitRepo to TEST_BRANCH
        itsqTestfaelleGitRepo.setGitRepoRevision("TEST_BRANCH");
        cut.updateItsqTestPaket(TEST_BASE_DIR, gitReposList);
        // check, ob README.md aus dem korrekten Branch kommt...
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_TEST_BRANCH});
        cloneTargetDir = new File(cloneTargetDir.getParentFile(), itsqTestfaelleGitRepo.getLocalRepoName());
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_UTILS});

        // Update itsqTestfaelleGitRepo to master
        itsqTestfaelleGitRepo.setGitRepoRevision("TEST_BRANCH");
        cloneTargetDir = cut.updateItsqTestPaket(TEST_BASE_DIR, gitReposList);
        // check, ob README.md aus dem korrekten Branch kommt...
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_MASTER});
        cloneTargetDir = new File(cloneTargetDir.getParentFile(), itsqTestfaelleGitRepo.getLocalRepoName());
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_UTILS});
    }

    @Test
    public void testTagAndPushITSQProject() throws Exception {
        EnvironmentConfig.GitProjectInfo itsqTestfaelleGitRepo = environmentConfig.getItsqTestfaelleGitRepo("master");
        File cloneTargetDir = new File(TEST_BASE_ITSQ_DIR, itsqTestfaelleGitRepo.getLocalRepoName());
        // clone itsqTestfaelleGitRepo
        cut.cloneGitProject(itsqTestfaelleGitRepo, cloneTargetDir);
        Assert.assertTrue(cloneTargetDir.exists());
        checkReadMeFile(new File(cloneTargetDir, READ_ME_FILE_NAME), new String[]{READ_ME_FILE_CONTENT_TEST_FAELLE, DIES_IST_IM_MASTER});

        // tag itsqTestfaelleGitRepo
        String itsqTagName = environmentConfig.getItsqTagName(TestSupportClientKonstanten.TEST_TYPES.PHASE1_AND_PHASE2.name());
        cut.tagAndPushITSQProject(cloneTargetDir.getAbsolutePath(), itsqTagName);
    }

    private void checkReadMeFile(File readmeFile, String[] containingStrings) throws IOException {
        Assert.assertTrue(readmeFile.exists());
        List<String> linesList = Files.readAllLines(readmeFile.toPath(), Charset.forName("UTF-8"));
        for (String containingString : containingStrings) {
            Assert.assertTrue("Erwarteter Text " + containingString + " existiert in der Liste nicht:\n" + linesList, linesList.contains(containingString));
        }
    }

}
