package de.creditreform.crefoteam.cte.tesun.gui;

import de.creditreform.crefoteam.cte.tesun.gui.utils.ClassPathSearcher;
import javax.swing.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ClassPathSearcherTest
{
  private static Logger LOGGER  = LoggerFactory.getLogger( ClassPathSearcher.class );
  private static String classPath;

  @BeforeClass
  public static void setUp() {
    classPath = System.getProperty( "java.class.path" );
    System.setProperty( "java.class.path", "C:\\Program Files\\AdoptOpenJDK\\jdk-8\\jre\\lib" );
  }

  @AfterClass
  public static void tearDown() {
    System.setProperty( "java.class.path", classPath );
  }

  @Test
  public void testFindFilesInClassPath()
  {
    UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
    Assert.assertTrue( "list of files was empty but shouldn't", lookAndFeels.length > 0 );
    for (int i = 0; i < lookAndFeels.length; i++) {
      System.out.println("Classname: " + lookAndFeels[i].getClassName());
      System.out.println("L&F-Name: " + lookAndFeels[i].getName());
    }
  }

  @Test
  public void testFindResourceInDirectory() throws IOException {
    ClassPathSearcher searcher = new ClassPathSearcher();
    File directory = new File("../auslieferung/lookandfeels");
    Map<String, InputStream> foundFiles = searcher.findResourceInDirectory( directory, ".*LookAndFeel.class" );
    Assert.assertFalse( "list of files was empty but shouldn't", foundFiles.isEmpty() );
    for( String key : foundFiles.keySet() )
    {
      LOGGER.info( "filename: " + key );
    }
  }
}
