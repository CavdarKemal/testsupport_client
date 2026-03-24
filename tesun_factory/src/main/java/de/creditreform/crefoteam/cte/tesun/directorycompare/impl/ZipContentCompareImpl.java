package de.creditreform.crefoteam.cte.tesun.directorycompare.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.creditreform.crefoteam.cte.tesun.directorycompare.DiffListener;
import de.creditreform.crefoteam.cte.tesun.directorycompare.DirectoryScanResult;
import de.creditreform.crefoteam.cte.tesun.directorycompare.XmlCompare;
import de.creditreform.crefoteam.cte.tesun.directorycompare.ZipContentCompare;
import de.creditreform.crefoteam.cte.tesun.util.ZipExtractor;

/**
 * Vergleich der Inhalte zweier Zip-Dateien
 * User: ralf
 * Date: 12.06.14
 * Time: 09:40
 */
public class ZipContentCompareImpl
implements ZipContentCompare {
    private final Logger logger = LoggerFactory.getLogger(ZipContentCompareImpl.class);

    private final DiffListener listener;
    private final XmlCompare xmlCompare;

    @Inject
    public ZipContentCompareImpl(DiffListener listener, XmlCompare xmlCompare) {
        this.listener = listener;
        this.xmlCompare = xmlCompare;
    }

    private ZipExtractor getZipExtractorInstance()
    {
      ZipExtractor zipExtractor = new ZipExtractor();
      return zipExtractor;
    }

    @Override
    public void compareZipFiles(String matchKey, DirectoryScanResult firstResult, DirectoryScanResult secondResult) {
        try {
        	ZipExtractor zipExtractor = getZipExtractorInstance();
            Map<String, ByteArrayOutputStream> mapFirst = zipExtractor.extractZip(firstResult.getFile());
            Map<String, ByteArrayOutputStream> mapSecond = zipExtractor.extractZip(secondResult.getFile());

            for (Map.Entry<String, ByteArrayOutputStream> e : mapFirst.entrySet()) {
                ByteArrayOutputStream other = mapSecond.remove(e.getKey());
                if (other==null) {
                    listener.firstZipOnly(firstResult.getIdentifier(), e.getKey(), e.getValue());
                }
                else {
                    xmlCompare.compareXml(firstResult.getIdentifier(), e.getKey(), e.getValue().toByteArray(), other.toByteArray());
                }
            }
            for (Map.Entry<String, ByteArrayOutputStream> e : mapSecond.entrySet()) {
                listener.secondZipOnly(secondResult.getIdentifier(), e.getKey(), e.getValue());
            }
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
