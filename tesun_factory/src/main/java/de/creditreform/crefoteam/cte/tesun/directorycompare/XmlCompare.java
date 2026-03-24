package de.creditreform.crefoteam.cte.tesun.directorycompare;

/**
 * Schnittstelle für den Vergleich zweier Datenströme mit XMLs im Rahmen
 * des {@link DirectoryCompare}
 * User: ralf
 * Date: 16.06.14
 * Time: 09:30
 */
public interface XmlCompare {

    void compareXml(String zipName, String zipEntryName, byte[] first, byte[] second);

}
