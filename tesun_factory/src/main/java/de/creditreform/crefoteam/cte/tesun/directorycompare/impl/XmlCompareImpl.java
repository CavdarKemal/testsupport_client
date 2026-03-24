package de.creditreform.crefoteam.cte.tesun.directorycompare.impl;

import com.google.inject.Inject;
import de.creditreform.crefoteam.cte.tesun.directorycompare.DiffListener;
import de.creditreform.crefoteam.cte.tesun.directorycompare.XmlCompare;
import de.creditreform.crefoteam.cte.tesun.util.XmlDiffFormatter;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Standard-Implementierung von {@link XmlCompare}
 * User: ralf
 * Date: 16.06.14
 * Time: 09:32
 */
public class XmlCompareImpl implements XmlCompare
{
  protected static final String CONTINUATION_MARKER = "...";
  
  private final DiffListener     diffListener;
  @SuppressWarnings ( "FieldCanBeLocal" )
  private final int              maxReportedRows = 100;
  private final XmlDiffFormatter diffFormatter;
  
  @Inject
  public XmlCompareImpl( DiffListener diffListener )
  {
    this.diffListener = diffListener;
    this.diffFormatter = new XmlDiffFormatter();
  }
  
  @Override
  public void compareXml( String zipName, String zipEntryName, byte [] first, byte [] second )
  {
    if( Arrays.equals( first, second ) )
    {
      diffListener.identical( zipName, zipEntryName );
    }
    else if( first.length > 1024 * 1024 )
    {
      diffListener.notCompared( zipName, zipEntryName, "Xml-Datei zu gross: " + first.length );
    }
    else
    {
      InputSource firstSource = new InputSource( new ByteArrayInputStream( first ) );
      InputSource secondSource = new InputSource( new ByteArrayInputStream( second ) );
      try
      {
        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreAttributeOrder( true );
        DetailedDiff detailedDiff = new DetailedDiff( XMLUnit.compareXML( firstSource, secondSource ) );
        if( detailedDiff.identical() )
        {
          diffListener.identical( zipName, zipEntryName );
        }
        else
        {
          List<String> reportedDiffs = mapDifferences( maxReportedRows, detailedDiff );
          diffListener.different( zipName, zipEntryName, reportedDiffs );
        }
      }
      catch (SAXException e)
      {
        diffListener.logException( zipName, zipEntryName, e );
      }
      catch (IOException e)
      {
        diffListener.logException( zipName, zipEntryName, e );
      }
      catch (ParserConfigurationException e)
      {
        diffListener.logException( zipName, zipEntryName, e );
      }
      catch (TransformerException e)
      {
        diffListener.logException( zipName, zipEntryName, e );
      }
    }
  }
  
  protected List<String> mapDifferences( int maxRows, DetailedDiff detailedDiff ) throws TransformerException, ParserConfigurationException
  {
    List<?> allDiffs = detailedDiff.getAllDifferences();
    List<String> reportedDiffs = new ArrayList<>( Math.max( maxRows + 1, allDiffs.size() ) );
    int row = 0;
    StringBuilder sb = new StringBuilder( 10240 );
    for( Object o : allDiffs )
    {
      sb.setLength( 0 );
      if( row >= maxRows )
      {
        reportedDiffs.add( CONTINUATION_MARKER );
        break;
      }
      Difference d = (Difference)o;
      sb = diffFormatter.appendDifference( sb, "", d );
      reportedDiffs.add( sb.toString() );
      row++;
    }
    return reportedDiffs;
  }
  
}
