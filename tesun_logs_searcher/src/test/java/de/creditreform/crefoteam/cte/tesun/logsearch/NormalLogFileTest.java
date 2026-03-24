package de.creditreform.crefoteam.cte.tesun.logsearch;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NormalLogFileTest
{

  private SearchableLogFile getSearchableLogFile( String testName, String fileName ) throws URISyntaxException, FileNotFoundException
  {
    String logFileName = NormalLogFileTest.class.getResource( fileName ).toURI().getPath();
    SearchableLogFile logFile = SearchableLogFileFactory.createInstanceFor( logFileName );
    logFile.addWorkerListener( new MyIWorkerListener( testName, fileName ) );
    return logFile;
  }

  @Before
  public void setUp()
  {
  }

  @After
  public void tearDown()
  {

  }

  @Test
  public void testNormalLogFileForLogTypeOnly() throws Exception
  {
    SearchableLogFile logFile = getSearchableLogFile( "testNormalLogFileForLogTypeOnly", "/all_flux_out.log" );
    SearchCriteria searchCriteriaALL = new SearchCriteria( LogEntry.ENTRY_TYPE.ALL );
    List<LogEntry> logEntries = logFile.getLogEntries( searchCriteriaALL );
    LogEntryTest.checkLogEntries( logEntries, 69, searchCriteriaALL );

    SearchCriteria searchCriteriaINFO = new SearchCriteria( LogEntry.ENTRY_TYPE.INFO );
    logEntries = logFile.getLogEntries( searchCriteriaINFO );
    LogEntryTest.checkLogEntries( logEntries, 60, searchCriteriaINFO );

    SearchCriteria searchCriteriaWARN = new SearchCriteria( LogEntry.ENTRY_TYPE.WARN );
    logEntries = logFile.getLogEntries( searchCriteriaWARN );
    LogEntryTest.checkLogEntries( logEntries, 6, searchCriteriaWARN );

    SearchCriteria searchCriteriaERROR = new SearchCriteria( LogEntry.ENTRY_TYPE.ERROR );
    logEntries = logFile.getLogEntries( searchCriteriaERROR );
    LogEntryTest.checkLogEntries( logEntries, 3, searchCriteriaERROR );
  }

  @Test
  public void testNormalLogFileForLogDateOnly() throws Exception
  {
    SearchableLogFile logFile = getSearchableLogFile( "testNormalLogFileForLogDateOnly", "/all_flux_out.log" );

    // From "2015-06-22 11:01:40", To NULL
    SearchCriteria searchCriteriaFrom = new SearchCriteria();
    Date logDateFrom = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse( "2015-06-22 11:01:40" );
    searchCriteriaFrom.setLogDateFrom( logDateFrom );
    List<LogEntry> logEntries = logFile.getLogEntries( searchCriteriaFrom );
    LogEntryTest.checkLogEntries( logEntries, 68, searchCriteriaFrom );

    // From "2015-06-22 11:02:52", To "2015-06-22 11:56:12"
    logDateFrom = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse( "2015-06-22 11:02:52" );
    searchCriteriaFrom.setLogDateFrom( logDateFrom );
    Date logDateTo = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse( "2015-06-22 11:56:12" );
    searchCriteriaFrom.setLogDateTo( logDateTo );
    logEntries = logFile.getLogEntries( searchCriteriaFrom );
    LogEntryTest.checkLogEntries( logEntries, 29, searchCriteriaFrom );

    // From NULL, To "2015-06-22 11:01:47"
    searchCriteriaFrom.setLogDateFrom( null );
    logDateTo = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse( "2015-06-22 11:01:47" );
    searchCriteriaFrom.setLogDateTo( logDateTo );
    logEntries = logFile.getLogEntries( searchCriteriaFrom );
    LogEntryTest.checkLogEntries( logEntries, 3, searchCriteriaFrom );
  }

  @Test
  public void testNormalLogFileForLogPackageOnly() throws Exception
  {
    SearchableLogFile logFile = getSearchableLogFile( "testNormalLogFileForLogPackageOnly", "/all_flux_out.log" );

    // From "2015-06-22 11:01:40", To NULL
    SearchCriteria searchCriteriaFrom = new SearchCriteria();
    searchCriteriaFrom.setLogPackage( "cte_flux_monitoring.ctecexport.xmlexport" );
    List<LogEntry> logEntries = logFile.getLogEntries( searchCriteriaFrom );
    LogEntryTest.checkLogEntries( logEntries, 7, searchCriteriaFrom );

    searchCriteriaFrom.setLogPackage( "cte_flux_monitoring." );
    logEntries = logFile.getLogEntries( searchCriteriaFrom );
    LogEntryTest.checkLogEntries( logEntries, 7, searchCriteriaFrom );

    searchCriteriaFrom.setLogPackage( "com.mchange" );
    logEntries = logFile.getLogEntries( searchCriteriaFrom );
    LogEntryTest.checkLogEntries( logEntries, 5, searchCriteriaFrom );

    searchCriteriaFrom.setLogPackage( "de.creditreform.crefoteam.fluxsupport" );
    logEntries = logFile.getLogEntries( searchCriteriaFrom );
    LogEntryTest.checkLogEntries( logEntries, 27, searchCriteriaFrom );

    searchCriteriaFrom.setLogPackage( "de.creditreform.crefoteam" );
    logEntries = logFile.getLogEntries( searchCriteriaFrom );
    LogEntryTest.checkLogEntries( logEntries, 56, searchCriteriaFrom );
  }

  @Test
  public void testNormalLogFileForErrorsInPackage() throws Exception
  {
    SearchableLogFile logFile = getSearchableLogFile( "testNormalLogFileForErrorsInPackage", "/all_flux_out.log" );

    // From "2015-06-22 11:01:40", To NULL
    SearchCriteria searchCriteriaERROR = new SearchCriteria( LogEntry.ENTRY_TYPE.ERROR );
    searchCriteriaERROR.setLogPackage( "de.creditreform.crefoteam.ctertnexport.rtnexport" );
    List<LogEntry> logEntries = logFile.getLogEntries( searchCriteriaERROR );
    LogEntryTest.checkLogEntries( logEntries, 3, searchCriteriaERROR );
  }

  @Test
  public void testNormalLogFileForWarningsInPackage() throws Exception
  {
    SearchableLogFile logFile = getSearchableLogFile( "testNormalLogFileForWarningsInPackage", "/all_flux_out.log" );

    // From "2015-06-22 11:01:40", To NULL
    SearchCriteria searchCriteriaWARN = new SearchCriteria( LogEntry.ENTRY_TYPE.WARN );
    searchCriteriaWARN.setLogPackage( "cte_flux_monitoring.ctecexport" );
    List<LogEntry> logEntries = logFile.getLogEntries( searchCriteriaWARN );
    LogEntryTest.checkLogEntries( logEntries, 1, searchCriteriaWARN );
  }

  @Test
  public void testNormalLogFileForInfoFromDateInPackage() throws Exception
  {
    SearchableLogFile logFile = getSearchableLogFile( "testNormalLogFileForInfoFromDateInPackage", "/all_flux_out.log" );

    SearchCriteria searchCriteriaINFO = new SearchCriteria( LogEntry.ENTRY_TYPE.INFO );
    Date logDateFrom = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse( "2015-06-22 11:56:13" );
    searchCriteriaINFO.setLogDateFrom( logDateFrom );
    searchCriteriaINFO.setLogPackage( "com.mchange.v2.c3p0" );
    List<LogEntry> logEntries = logFile.getLogEntries( searchCriteriaINFO );
    LogEntryTest.checkLogEntries( logEntries, 4, searchCriteriaINFO );

    logDateFrom = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse( "2015-06-22 11:02:52" );
    searchCriteriaINFO.setLogDateFrom( logDateFrom );
    Date logDateTo = TesunDateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.parse( "2015-06-22 11:56:12" );
    searchCriteriaINFO.setLogDateTo( logDateTo );
    searchCriteriaINFO.setLogPackage( "de.creditreform.crefoteam" );
    logEntries = logFile.getLogEntries( searchCriteriaINFO );
    LogEntryTest.checkLogEntries( logEntries, 18, searchCriteriaINFO );

  }

  @Test
  public void testNormalLogFileForLogInfoOnly() throws Exception
  {
    SearchableLogFile logFile = getSearchableLogFile( "testNormalLogFileForLogInfoOnly", "/all_flux_out.log" );

    SearchCriteria searchCriteriaALL = new SearchCriteria();
    searchCriteriaALL.setLogInfo( "durch Post-Marshalling-Filter unterdrückt" );
    List<LogEntry> logEntries = logFile.getLogEntries( searchCriteriaALL );
    LogEntryTest.checkLogEntries( logEntries, 7, searchCriteriaALL );

  }

  @Test
  public void testNormalLogFileForLogErrorWithRuntimeException() throws Exception
  {
    SearchableLogFile logFile = getSearchableLogFile("testNormalLogFileForLogErrorWithRuntimeException", "/all_flux_out.log" );

    SearchCriteria searchCriteriaERROR = new SearchCriteria( LogEntry.ENTRY_TYPE.ERROR );
    searchCriteriaERROR.setLogInfo( "RuntimeException beim XML-Export der Crefo" );
    List<LogEntry> logEntries = logFile.getLogEntries( searchCriteriaERROR );
    LogEntryTest.checkLogEntries( logEntries, 1, searchCriteriaERROR );

    LogEntry logEntry0 = logEntries.get( 0 );
    Matcher matcher = logEntry0.searchInInfosList( "Crefo (\\d{10})" );
    String group1 = matcher.group( 1 );
    Long crefo = Long.valueOf( group1 );
    Assert.assertEquals( 6250301832l, crefo.longValue() );
  }

  class MyIWorkerListener implements IWorkerListener
  {
    final String testName;
    final String fileName;

    public MyIWorkerListener( String testName, String fileName )
    {
      this.testName = testName;
      this.fileName = fileName;

    }

    @Override
    public void updateProgress(Object dataObject, int progressStep )
    {
      System.out.println( String.format( "updateProgress(%d):: Test: %s, Datei: %s", progressStep, testName, fileName ) );
    }

    @Override
    public void updateData( Object dataObject )
    {
      System.out.println( String.format( "updateProgress(%s):: Test: %s, Datei: %s", dataObject.toString(), testName, fileName ) );
    }

    @Override
    public void updateTaskState( TASK_STATE taskState )
    {
      System.out.println( String.format( "updateProgress(%s):: Test: %s, Datei: %s", taskState.toString(), testName, fileName ) );
    }

    @Override
    public boolean isCanceled()
    {
      return false;
    }

  }
}
