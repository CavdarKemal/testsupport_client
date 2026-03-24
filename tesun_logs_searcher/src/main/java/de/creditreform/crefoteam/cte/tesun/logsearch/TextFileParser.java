package de.creditreform.crefoteam.cte.tesun.logsearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

abstract class TextFileParser<T>
{
  public List<T> getMatchedTokens( String fileName ) throws IOException
  {
    List<T> matchedTokens = new ArrayList<>();
    List<String> readLines = FileUtils.readLines( new File( fileName ) );
    for( String strLine : readLines )
    {
      T matchedToken = extractData( strLine );
      if( matchedToken != null )
      {
        matchedTokens.add( matchedToken );
      }
    }
    return matchedTokens;
  }

  abstract public T extractData( String strLine );
}
