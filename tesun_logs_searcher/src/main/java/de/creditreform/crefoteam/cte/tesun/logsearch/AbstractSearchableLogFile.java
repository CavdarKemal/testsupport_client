package de.creditreform.crefoteam.cte.tesun.logsearch;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractSearchableLogFile implements SearchableLogFile
{
  private boolean               activated          = true;
  private File                  logFile;
  private List<IWorkerListener> workerListenerList = new ArrayList<>();

  public AbstractSearchableLogFile( String logFileName ) throws FileNotFoundException
  {
    setLogFile( new File( logFileName ) );
  }

  abstract protected BufferedReader getBufferedReaderForLogFile() throws Exception;

  @Override
  public LogSearchResults search( SearchCriteria searchCriteria ) throws IOException
  {
    return null;
  }

  @Override
  public String toString()
  {
    return logFile.getPath();
  }

  @Override
  public void addWorkerListener( IWorkerListener workerListener )
  {
    if( !workerListenerList.contains( workerListener ) )
    {
      workerListenerList.add( workerListener );
    }
  }

  public boolean isActivated()
  {
    return activated;
  }

  public void setActivated( boolean activated )
  {
    this.activated = activated;
  }

  public File getLogFile()
  {
    return logFile;
  }

  public void setLogFile( File logFile ) throws FileNotFoundException
  {
    this.logFile = logFile;
    if( !logFile.exists() )
    {
      throw new FileNotFoundException( String.format( "LOG-Datei '%s' existiertn nicht!", logFile.getPath() ) );
    }
    if( !logFile.canRead() )
    {
      throw new FileNotFoundException( String.format( "LOG-Datei '%s' kann nicht gelesen werden!", logFile.getPath() ) );
    }
  }

  @Override
  public List<LogEntry> getLogEntries( SearchCriteria searchCriteria ) throws Exception
  {
    List<LogEntry> logEntryList = new ArrayList<>();
    BufferedReader bufferedReader = getBufferedReaderForLogFile();
    String strLine = null;
    LogEntry logEntry = null;
    LogEntry.ENTRY_TYPE logEntryType = searchCriteria.getLogEntryType();
    int lineIndex = 0;
    while( ( strLine = bufferedReader.readLine() ) != null )
    {
      if( isCanceled() )
      {
        bufferedReader.close();
        return logEntryList;
      }
      // System.out.println( strLine );
      LogEntry logEntryFromLogLine = parseLogEntryFromLogLine( logFile, logEntryType, strLine );
      if( logEntryFromLogLine != null )
      {
        // prüfe zusätzliche Suchkriterien, falls angegeben...
        boolean matches = ( searchCriteria.getLogDateFrom() == null ) || !logEntryFromLogLine.getLogDate().before( searchCriteria.getLogDateFrom() );
        if( matches )
        {
          matches &= ( searchCriteria.getLogDateTo() == null ) || !logEntryFromLogLine.getLogDate().after( searchCriteria.getLogDateTo() );
        }
        if( matches )
        {
          matches &= ( searchCriteria.getLogPackage() == null ) || logEntryFromLogLine.getPackg().startsWith( searchCriteria.getLogPackage() );
        }
        if( matches )
        {
          if( searchCriteria.getLogInfo() != null )
          {
            Pattern pattern = Pattern.compile( searchCriteria.getLogInfo() );
            Matcher matcher = pattern.matcher( logEntryFromLogLine.getInfoList().toString() );
            matches &= matcher.find();
          }
        }
        if( matches )
        {
          if( logEntry != null )
          {
            reportListeners( logEntry );
          }
          logEntryList.add( logEntryFromLogLine );
          logEntry = logEntryFromLogLine;
        }
        else
        {
          // weil die LogEntry nicht zu den Suchkriterien passt, logEntry auf NULL setzten!
          if( logEntry != null )
          {
            reportListeners( logEntry );
            logEntry = null;
          }
        }
      }
      else
      {
        // nur wenn schon mal ein LogEntry geparst werden konnte, kann die nächste Zeile den Rest des LogEntry darstellen!
        if( logEntry != null )
        {
          // nur wenn die aktuelle Zeile KEINE LogEntry ergibt, kann sie den Rest des LogEntry darstellen!
          if( parseLogEntryFromLogLine( logFile, LogEntry.ENTRY_TYPE.ALL, strLine ) == null )
          {
            logEntry.addAdditionalInfo( strLine );
          }
          else
          {
            // weil die Zeile eine LogEntry ergibt, ist die aktuelle LogEntry beendet!
            reportListeners( logEntry );
            logEntry = null;
          }
        }
      }
      progressListeners( this, ++lineIndex % 100 );
    }
    if( logEntry != null )
    {
      reportListeners( logEntry );
    }
    bufferedReader.close();
    return logEntryList;
  }

  /*
    2015-06-22 11:40:48,546 INFO  de.creditreform.crefoteam.ctertnexport.rtnexport.callables.RtnExportCallableImpl - XML-Ausgabe von 6070559291.xml durch Post-Marshalling-Filter unterdrückt
    2015-06-22 11:40:49,887 ERROR de.creditreform.crefoteam.ctertnexport.rtnexport.callables.RtnExportCallableImpl - Fehler in den Quelldaten verhindert das Mapping beim XML-Export der Crefo 6110313042
    de.creditreform.crefoteam.ctertnexport.rtnexportcommons.except.RtnInsufficientDataXmlMapperException: Anschrift ist nicht gefüllt bei Crefo 6110313042
      at de.creditreform.crefoteam.ctertnexport.rtnxmlmapper.util.NotNullValidator.validate(NotNullValidator.java:20)
      at de.creditreform.crefoteam.ctertnexport.rtnxmlmapper.detail.DetailMapperFirmenIdentifikation.mappe(DetailMapperFirmenIdentifikation.java:39)
      at de.creditreform.crefoteam.ctertnexport.rtnxmlmapper.detail.DetailMapperFirmenIdentifikation.mappe(DetailMapperFirmenIdentifikation.java:22)
      ...
      at java.lang.Thread.run(Thread.java:682)
   */
  private LogEntry parseLogEntryFromLogLine( File logFile, LogEntry.ENTRY_TYPE logEntryType, String strLine )
  {
    LogEntry logEntry = null;
    Matcher matcher = logEntryType.getPattern().matcher( strLine );
    if( matcher.find() )
    {
      try
      {
        Date parsedDate = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse( matcher.group( 1 ) );// (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})
        String type = matcher.group( 2 );
        String packg = matcher.group( 3 );
        String info = matcher.group( 4 );
        logEntry = new LogEntry( logFile, parsedDate, type, packg, info );
      }
      catch (Exception ex)
      {
        System.out.println( ex.getMessage() + strLine );
      }
    }
    return logEntry;
  }

  public static void dumpLogEntries( List<LogEntry> logEntries )
  {
    System.out.println( String.format( "\nLogEntry-Liste: Anzahl = %d", logEntries.size() ) );
    for( LogEntry logEntry : logEntries )
    {
      System.out.println( logEntry.toString() );
    }
  }

  private void reportListeners( Object dataObject )
  {
    for( IWorkerListener workerListener : workerListenerList )
    {
      workerListener.updateData( dataObject );
    }
  }

  private void progressListeners( Object dataObject, int progressStep )
  {
    for( IWorkerListener workerListener : workerListenerList )
    {
      workerListener.updateProgress( dataObject, progressStep );
    }
  }

  private boolean isCanceled()
  {
    for( IWorkerListener workerListener : workerListenerList )
    {
      if( workerListener.isCanceled() )
      {
        return true;
      }
    }
    return false;
  }

}
