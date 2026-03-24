package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import java.io.File;

import org.junit.Test;


public class FileDirTest
{

  private void listFiles( String strPraefix, File sourceFile ) throws Exception
  {
    System.out.println( strPraefix + "[" + sourceFile.getName() + "]" );
    File[] filesList = sourceFile.listFiles();
    for( File theFile : filesList )
    {
      if( theFile.isDirectory() )
      {
        listFiles( ( strPraefix + "\t" ), theFile );
      }
      else
      {
        System.out.println(strPraefix + "\t{ " + theFile.getName() + " }");
      }
    }
  }

  @Test public void testListFiles() throws Exception
  {
    listFiles( "", new File(System.getProperty( "user.dir" )) );
  }

}
