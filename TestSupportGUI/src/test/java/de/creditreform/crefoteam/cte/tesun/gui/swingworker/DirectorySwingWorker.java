package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import java.io.File;
import java.io.FileFilter;

import javax.swing.SwingWorker;

public class DirectorySwingWorker extends SwingWorker<Integer, File>
{

  private final String     workerPath;
  private final FileFilter fileFilter;
  private boolean          isPaused;

  public DirectorySwingWorker( String workerPath, FileFilter fileFilter )
  {
    this.workerPath = workerPath;
    this.fileFilter = fileFilter;
  }

  private void listFiles( String strPraefix, File sourceFile ) throws Exception
  {
    // System.out.println( strPraefix + "[" + sourceFile.getName() + "]" );
    File[] filesList = sourceFile.listFiles(fileFilter);
    for( File theFile : filesList )
    {
      while( isPaused() && !isCancelled() )
      {
        Thread.sleep( 500 );
        System.out.print( "." );
      }
      if( isCancelled() )
      {
        return;
      }
      publish( theFile );
      setProgress( (getProgress() + 1) % 100 );
      if( theFile.isDirectory() )
      {
        listFiles( ( strPraefix + "\t" ), theFile );
        Thread.sleep( 5 );
      }
      else
      {
        // System.out.println(strPraefix + "\t{ " + theFile.getName() + " }");
      }
    }
  }

  public void setPaused( boolean isPaused )
  {
    this.isPaused = isPaused;
  }

  public boolean isPaused()
  {
    return isPaused;
  }

  @Override protected Integer doInBackground() throws Exception
  {
    File workerDir = new File( workerPath );
    listFiles( "", workerDir );
    return 0;
  }

  //  @Override protected void process( List<File> chunks )
  //  {
  //    for( File theFile : chunks )
  //    {
  //      boolean isDir = theFile.isDirectory();
  //      System.out.println( isDir ? ( "[" + theFile.getName() + "]" ) : ( "\t" + theFile.getName() ) );
  //    }
  //  }
  //
  //  @Override protected void done()
  //  {
  //    try
  //    {
  //      Integer numFiles = get();
  //      if( isCancelled() )
  //      {
  //        System.out.println( "Vorgang abgebrochen!" );
  //      }
  //      else
  //      {
  //        System.out.println( "Das Verzeichnis enthält " + numFiles + " Dateien/Unterverzeichnisse" );
  //      }
  //    }
  //    catch( InterruptedException ex )
  //    {
  //    }
  //    catch( ExecutionException ex )
  //    {
  //    }
  //  }
}
