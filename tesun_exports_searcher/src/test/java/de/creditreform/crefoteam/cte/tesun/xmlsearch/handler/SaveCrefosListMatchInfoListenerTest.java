package de.creditreform.crefoteam.cte.tesun.xmlsearch.handler;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.testbasics.ExportedZipsSearcherBaseTest;
import de.creditreform.crefoteam.cte.tesun.xmlsearch.domain.ZipSearcResult.ZipEntryInfo;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class SaveCrefosListMatchInfoListenerTest extends ExportedZipsSearcherBaseTest
{

  @Test public void testWriteMatchInfo() throws Exception
  {
    SaveCrefosListMatchInfoListener listener = new SaveCrefosListMatchInfoListener(TestSupportClientKonstanten.SEARCH_RESULT_TYPE.CREFOS_LIST.getIdentifier(),
                                                                                   searchResultsDir.getPath(), null);

    String entryName = "Test.xml";
    String[] strCrefoNummers = new String[] {"1234567890", "1234567891", "1234567892"};

    ZipEntryInfo zipEntryInfo = new ZipEntryInfo(null, "dummy.zip", entryName );
    for( String strCrefoNummer : strCrefoNummers )
    {
      zipEntryInfo.setCrefonummer( strCrefoNummer );
      listener.notifyEntryMatched(null, zipEntryInfo, null);
    }
    File resultFile = new File( zipEntryInfo.getResultFileName() );
    Assert.assertTrue( resultFile.exists() );

    listener.close();

    List<String> lines = FileUtils.readLines( resultFile );
    Assert.assertEquals( lines.size() , strCrefoNummers.length );
    for( int i=0; i<strCrefoNummers.length; i++ )
    {
      Assert.assertEquals( lines.get(i) , strCrefoNummers[i]);
    }
  }
}
