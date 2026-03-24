package de.creditreform.crefoteam.cte.tesun.logsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZippedLogFile extends AbstractSearchableLogFile
{
  public ZippedLogFile( String logFileName ) throws FileNotFoundException
  {
    super( logFileName );
  }

  protected BufferedReader getBufferedReaderForLogFile() throws Exception
  {
    File logFile = getLogFile();
    InputStream theInputStream = null;
    if(logFile.getName().endsWith( ".gz" ))
    {
      theInputStream = new GZIPInputStream(new FileInputStream(logFile));
    }
    else if(logFile.getName().endsWith( ".zip" ))
    {
      theInputStream = new ZipInputStream(new FileInputStream(logFile));
      ((ZipInputStream)theInputStream).getNextEntry(); // Stream auf die erste Datei setzen!!!
    }
    else
    {
      throw new IllegalArgumentException("Ungültige ZIP-Date!" + logFile.getPath());
    }
    InputStreamReader isr = new InputStreamReader(theInputStream, "UTF-8" );
    BufferedReader bufferedReader = new BufferedReader( isr );
    return bufferedReader;
  }

  @Override
  public LogSearchResults search( SearchCriteria searchCriteria ) throws IOException
  {
    LogSearchResults logSearchResults = new LogSearchResults();

    return logSearchResults;
  }

}
