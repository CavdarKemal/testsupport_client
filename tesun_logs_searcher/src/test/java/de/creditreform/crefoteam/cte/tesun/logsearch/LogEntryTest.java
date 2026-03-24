package de.creditreform.crefoteam.cte.tesun.logsearch;

import java.util.Date;
import java.util.List;

import org.junit.Assert;

import de.creditreform.crefoteam.cte.tesun.logsearch.LogEntry.ENTRY_TYPE;

public class LogEntryTest
{

  public static void checkLogEntries( List<LogEntry> logEntries, int expectedSize, SearchCriteria expectedSearchCriteria )
  {
    Assert.assertNotNull( logEntries );
    AbstractSearchableLogFile.dumpLogEntries( logEntries );
    Assert.assertEquals( logEntries.size(), expectedSize );
    for( LogEntry logEntry : logEntries )
    {
      // Log-Type checken...
      ENTRY_TYPE expectedLogEntryType = expectedSearchCriteria.getLogEntryType();
      if( !expectedLogEntryType.equals( ENTRY_TYPE.ALL ) )
      {
        Assert.assertEquals( logEntry.getType(), expectedLogEntryType );
      }
      // Log-Datümer checken...
      Date expectedLogDateFrom = expectedSearchCriteria.getLogDateFrom();
      if( expectedLogDateFrom != null )
      {
        Assert.assertFalse( logEntry.getLogDate().before( expectedLogDateFrom ) );
      }
      Date expectedLogDateTo = expectedSearchCriteria.getLogDateTo();
      if( expectedLogDateTo != null )
      {
        Assert.assertFalse( logEntry.getLogDate().after( expectedLogDateTo ) );
      }
      // Package checken...
      String expectedLogPackage = expectedSearchCriteria.getLogPackage();
      if( expectedLogPackage != null )
      {
        Assert.assertTrue( logEntry.getPackg().startsWith( expectedLogPackage ) );
      }
      // Info checken...
      String expectedLogInfo = expectedSearchCriteria.getLogInfo();
      if( expectedLogInfo!= null )
      {
        Assert.assertTrue( logEntry.getInfoList().get( 0 ).contains( expectedLogInfo) );
      }

    }
  }

}
