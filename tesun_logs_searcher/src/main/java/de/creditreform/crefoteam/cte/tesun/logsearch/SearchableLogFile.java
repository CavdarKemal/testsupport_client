package de.creditreform.crefoteam.cte.tesun.logsearch;

import java.io.IOException;
import java.util.List;

public interface SearchableLogFile
{
  public void addWorkerListener( IWorkerListener workerListener );

  public List<LogEntry> getLogEntries( SearchCriteria searchCriteria ) throws Exception;

  public LogSearchResults search( SearchCriteria searchCriteria ) throws IOException;
}
