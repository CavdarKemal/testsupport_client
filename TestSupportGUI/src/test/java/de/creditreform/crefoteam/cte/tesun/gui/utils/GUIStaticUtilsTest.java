package de.creditreform.crefoteam.cte.tesun.gui.utils;

import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import java.io.File;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

public class GUIStaticUtilsTest {

   @Test
   public void testGetVersionFromPOM() {
      String pomFileName = System.getProperty("user.dir") + "/pom.xml";
      String versionFromPOM = GUIStaticUtils.getVersionFromPOM(pomFileName);
      Assert.assertNotNull(versionFromPOM);
   }

   @Test
   public void testFormatXMLFilesInDir() throws Exception {
      URL srcURL = this.getClass().getResource("/TESTS/LOCAL/REF-EXPORTS");
      File srcDir = new File(srcURL.getFile());
//      File srcDir  = new File("c:\\Temp\\mic\\");
      File dstDir = new File(System.getProperty("user.dir"), "/target/FORMATTED_XMLS");
//      File dstDir  = new File("c:\\Temp\\micX\\");
      TesunUtilites.formatXMLFilesInDir(srcDir, dstDir);
   }

   protected String assertNonEmptyPassword(final String newPassword) {
      final String nonEmpty;
      if (newPassword == null) {
         throw new IllegalArgumentException("Neues Passwort darf nicht null sein!");
      } else {
         nonEmpty = newPassword.trim();
         if (nonEmpty.length() == 0) {
            throw new IllegalArgumentException("Neues Passwort darf nicht leer sein!");
         }
         /*
         @see https://git.creditreform.de/dm/identity-access-management/sso-docs/-/blob/main/docs/SSO-Doku_Passwort-und-Anmelde-Richtlinie/Doku.md
            Das Passwort muss aus mindestens acht und maximal 60 Zeichen bestehen.
            Das Passwort muss mindestens
            a) einen Großbuchstaben A - Z (inkl. diakritischer Zeichen) UND
            b) einen Kleinbuchstaben a - z (inkl. diakritischer Zeichen) UND
            c) eine Ziffer 0 - 9 ODER ein Sonderzeichen
            enthalten.
            Die Liste der Sonderzeichen orientiert sich an den druckbaren Sonderzeichen in ANSI X3.4-1986 ohne Whitespaces.
            Erlaubte Sonderzeichen: ! " # $ % & ' ( ) * + , - . / : ; < = > ? [ \ _ ] ^ ` { | } ~
         */
         String regExp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\\"@$!(%+*',\\\\.\\/:;<>^{}|)?&#])([a-zA-Z0-9\\\"@$!(%+*',\\\\.\\/:;_<>^{}|)?&#]{8,})$";
         // String regExp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&])([a-zA-Z0-9@$!%*?&]{8,})$";
         if (!newPassword.matches(regExp)) {
            throw new IllegalArgumentException("Neues Passwort '" + newPassword + "' erfüllt die Mindestkriterien nicht!");
         }
      }
      return nonEmpty;
   }

   @Test
   public void testAssertNonEmptyPassword() throws Exception {
      String newPassword = "";

      // zu kurz!
      try {
         newPassword = "aA1'*%";
         assertNonEmptyPassword(newPassword);
         Assert.fail("IllegalArgumentException expected!");
      } catch (IllegalArgumentException ex) {
         Assert.assertTrue(ex.getMessage().contains("Neues Passwort '" + newPassword + "' erfüllt die Mindestkriterien nicht!"));
      }
      // Kleinbuchstabe fehlt!
      try {
         newPassword = "XYZABC123&";
         assertNonEmptyPassword(newPassword);
         Assert.fail("IllegalArgumentException expected!");
      } catch (IllegalArgumentException ex) {
         Assert.assertTrue(ex.getMessage().contains("Neues Passwort '" + newPassword + "' erfüllt die Mindestkriterien nicht!"));
      }
      // Grossbuchstabe fehlt!
      try {
         newPassword = "xyzabc123&";
         assertNonEmptyPassword(newPassword);
         Assert.fail("IllegalArgumentException expected!");
      } catch (IllegalArgumentException ex) {
         Assert.assertTrue(ex.getMessage().contains("Neues Passwort '" + newPassword + "' erfüllt die Mindestkriterien nicht!"));
      }
      // Sonderzeichen fehlt!
      try {
         newPassword = "xyzABC1234";
         assertNonEmptyPassword(newPassword);
         Assert.fail("IllegalArgumentException expected!");
      } catch (IllegalArgumentException ex) {
         Assert.assertTrue(ex.getMessage().contains("Neues Passwort '" + newPassword + "' erfüllt die Mindestkriterien nicht!"));
      }
      // alles da!
      try {
         newPassword = "test_pwd123!A";
//         newPassword = "abcABC123&";
         assertNonEmptyPassword(newPassword);
      } catch (IllegalArgumentException ex) {
         Assert.assertTrue(ex.getMessage().contains("Neues Passwort erfüllt die Mindestkriterien doch!"));
      }

   }


}
