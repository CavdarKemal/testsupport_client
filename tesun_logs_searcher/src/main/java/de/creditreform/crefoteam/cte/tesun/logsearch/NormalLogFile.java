package de.creditreform.crefoteam.cte.tesun.logsearch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class NormalLogFile extends AbstractSearchableLogFile
{
  public NormalLogFile( String logFileName ) throws FileNotFoundException
  {
    super( logFileName );
  }

  protected BufferedReader getBufferedReaderForLogFile() throws Exception
  {
    FileInputStream fis = new FileInputStream( getLogFile() );
    InputStreamReader isr = new InputStreamReader( fis, "UTF-8" );
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
