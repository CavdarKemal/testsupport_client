package de.creditreform.crefoteam.cte.tesun.testutil;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestFallFileUtilTest {

   @Test
   public void testCheckAndCreateDirectory() throws IOException {
      URL resource = TestFallFileUtilTest.class.getResource("/");
      File testDir = new File(resource.getPath(), "TEST-DIR/TEST-SUBDIR");
      FileUtils.deleteDirectory(testDir.getParentFile());
      // Das Verzeichnis existiert nicht, false:soll auch nicht erzeugt werden --> Exception
      try {
         TesunUtilites.checkAndCreateDirectory(testDir, false);
         Assert.fail("Das Verzeichnis sollte nicht exitieren!");
      } catch (Exception ex) {
         Assert.assertTrue(ex.getMessage().contains("TEST-DIR"+File.separator+"TEST-SUBDIR existiert nicht!"));
      }
      // Das Verzeichnis existiert nicht, true:soll erzeugt werden --> keine Exception, muss erzeugt worden sein
      try {
         TesunUtilites.checkAndCreateDirectory(testDir, true);
         Assert.assertTrue(testDir.exists());
      } catch (Exception ex) {
         Assert.fail(ex.getMessage());
      }
      // Das Verzeichnis existiert, false:soll nicht erzeugt werden --> keine Exception, kein Backup!
      try {
         TesunUtilites.checkAndCreateDirectory(testDir, false);
         Assert.assertTrue(testDir.exists());
         String[] list = testDir.getParentFile().list();
         Assert.assertTrue(list.length == 1);
      } catch (Exception ex) {
         Assert.fail(ex.getMessage());
      }
      // Das Verzeichnis existiert, true:soll erzeugt werden --> keine Exception, 1 Backup
      try {
         TesunUtilites.checkAndCreateDirectory(testDir, true);
         Assert.assertTrue(testDir.exists());
         String[] list = testDir.getParentFile().list();
         Assert.assertTrue(list.length == 2);
      } catch (Exception ex) {
         Assert.fail(ex.getMessage());
      }
      // Das Verzeichnis existiert, true:soll erzeugt werden --> keine Exception, 2 Backups
      try {
         TesunUtilites.checkAndCreateDirectory(testDir, true);
         Assert.assertTrue(testDir.exists());
         String[] list = testDir.getParentFile().list();
         Assert.assertTrue(list.length == 3);
      } catch (Exception ex) {
         Assert.fail(ex.getMessage());
      }

   }

}
