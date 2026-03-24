package de.creditreform.crefoteam.cte.tesun.gui.utils;

import org.apache.commons.io.FileUtils;
//import org.eclipse.jgit.api.Git;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// https://www.codeaffine.com/2015/05/06/jgit-initialize-repository/
// https://dzone.com/articles/jgit-library-examples-in-java
// https://www.baeldung.com/jgit
// https://www.vogella.com/tutorials/JGit/article.html

public class JGitTest {
    private final String userDir = System.getProperty("user.dir");
    private final File targetDir = new File(userDir, "/target/jgit/");
    private final String gitRepoUrl = "git@git.creditreform.de:cte/experiments/jgitexamples.git";

    @Before
    public void setUp() throws IOException {
    }

/*
    @Test
    public void testCloneProjectsDefaultBranch() throws Exception {
        File masterDir = new File(targetDir, "master");
        FileUtils.deleteDirectory(masterDir);
        Git gitObject = Git.cloneRepository().setURI(gitRepoUrl).setDirectory(masterDir).call();
        // Check git object
        checkGitObject(gitObject, Arrays.asList("origin"));
        // Check die geklonte Repo...
        checkRepo(masterDir,true);
    }

    @Test
    public void testCloneProjectsSpecificBranch() throws Exception {
        File branchDir = new File(targetDir, "BASIS");
        FileUtils.deleteDirectory(branchDir);
        Git gitObject = Git.cloneRepository().setURI(gitRepoUrl).setDirectory(branchDir).setBranchesToClone(Arrays.asList("refs/heads/BASIS")).setBranch("refs/heads/BASIS").call();
        // Check git object
        checkGitObject(gitObject, Arrays.asList("origin"));
        // Check die geklonte Repo...
        checkRepo(branchDir,true);
    }

    @Test
    public void testCloneProjectsAllBranches() throws Exception {
        File allDir = new File(targetDir, "all");
        FileUtils.deleteDirectory(allDir);
        Git gitObject = Git.cloneRepository().setURI(gitRepoUrl).setDirectory(allDir).setCloneAllBranches(true).call();
        // Check git object
        checkGitObject(gitObject, Arrays.asList("origin"));
        // Check die geklonte Repo...
        checkRepo(allDir,true);
    }

*/
/*
    @Test
    public void testCreateLocalRepositoryX() throws Exception {
        // https://www.vogella.com/tutorials/JGit/article.html
        File newGitRepo = new File(userDir, "/target/jgit/new-repo");
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(newGitRepo)
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();
    }
*/
/*
    @Test
    public void testCreateLocalRepository() throws Exception {
        // https://www.codeaffine.com/2015/05/06/jgit-initialize-repository/
        File newGitRepo = new File(userDir, "/target/jgit/new-repo");
        Git gitObject = Git.init().setDirectory( newGitRepo ).call();
        // Check die geklonte Repo...
        checkRepo(newGitRepo, false);
    }

    private void checkRepo(File branchDir, boolean checkFiles) {
        File repoDir = new File(branchDir, ".git");
        Assert.assertTrue("Repo-git Verzeichnis '"+ repoDir.getAbsolutePath() + "' existiert nicht!", repoDir.exists());
        if(checkFiles) {
            File pomFile = new File(branchDir, "pom.xml");
            Assert.assertTrue("POM-File '" + pomFile.getAbsolutePath() + "' existiert nicht!", pomFile.exists());
            File srcFile = new File(branchDir, "src");
            Assert.assertTrue("Source-Verzeichnis '" + srcFile.getAbsolutePath() + "' existiert nicht!", srcFile.exists());
            File mainDir = new File(srcFile, "main");
            Assert.assertTrue("main-Verzeichnis '" + mainDir.getAbsolutePath() + "' existiert nicht!", mainDir.exists());
            File testDir = new File(srcFile, "main");
            Assert.assertTrue("test-Verzeichnis '" + testDir.getAbsolutePath() + "' existiert nicht!", testDir.exists());
        }
    }

    private void checkGitObject(Git gitObject, List<String> expectedList) {
        Set<String> remoteNames = gitObject.getRepository().getRemoteNames();
        Assert.assertNotNull(remoteNames);
        Assert.assertEquals(expectedList.size(), remoteNames.size());
        expectedList.stream().forEach(s -> {
            Assert.assertTrue(remoteNames.contains(s));
        });
    }

*/
}
