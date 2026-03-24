package de.creditreform.crefoteam.cte.tesun.logsearch;

import java.io.FileNotFoundException;

public class SearchableLogFileFactory
{
  public static SearchableLogFile createInstanceFor(String logFileName) throws FileNotFoundException
  {
    SearchableLogFile searchableLogFile = null;
    if(logFileName.endsWith( ".log" ))
    {
      searchableLogFile = new NormalLogFile(logFileName);
    }
    else if(logFileName.endsWith( ".gz" ) || logFileName.endsWith( ".zip" ))
    {
      searchableLogFile = new ZippedLogFile(logFileName);
    }
    else
    {
      throw new UnsupportedOperationException( String.format("Typ der Log-Datei '%s' unbekannt!", logFileName));
    }
    return searchableLogFile;
  }

}
