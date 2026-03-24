package de.creditreform.crefoteam.cte.tesun.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor
{

  public Map<String, ByteArrayOutputStream> extractZip( File file ) throws IOException
  {
    Map<String, ByteArrayOutputStream> theMap              = new TreeMap<>();
    FileInputStream                    fileInputStream     = new FileInputStream( file );
    BufferedInputStream                bufferedInputStream = new BufferedInputStream( fileInputStream );
    ZipInputStream                     zipInputStream      = new ZipInputStream( bufferedInputStream );
    try
    {
      ZipEntry zipEntry = zipInputStream.getNextEntry();
      while( zipEntry != null )
      {
        ByteArrayOutputStream byteArrayOutputStream = extractZipEntry( zipInputStream );
        String                zipEntryName          = zipEntry.getName();
        theMap.put( zipEntryName, byteArrayOutputStream );
        zipEntry = zipInputStream.getNextEntry();
      }
    }
    finally
    {
      zipInputStream.close();
    }
    return theMap;
  }

  public ByteArrayOutputStream extractZipEntry( ZipInputStream zipInputStream ) throws IOException
  {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte[]                buffer                = new byte[102400];
    int                   count;
    while( ( count = zipInputStream.read( buffer ) ) != -1 )
    {
      byteArrayOutputStream.write( buffer, 0, count );
    }
    return byteArrayOutputStream;
  }

}
