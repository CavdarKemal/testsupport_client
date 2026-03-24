package de.creditreform.crefoteam.cte.tesun.util;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TesunUtilitesTest {

    @Test
    public void testPrettyPrint() throws Exception {
        final URL resourceURL = this.getClass().getResource("/bilanz_iso-8859-1.xml");
        String xmlFragment = FileUtils.readFileToString(new File(resourceURL.getPath()));
        final String s = TesunUtilites.toPrettyString(xmlFragment, 3);
        System.out.println(s);
    }


    @Test
    public void testWriteXmlFragment() throws Exception {
        String fileName = "target/testWriteXmlFragment.xml";
        final URL resourceURL = this.getClass().getResource("/bilanz_iso-8859-1.xml");
        String xmlFragment = FileUtils.readFileToString(new File(resourceURL.getPath()));
        final String formattedXMLContent = TesunUtilites.toPrettyString(xmlFragment, 2);
        FileUtils.writeStringToFile(new File(fileName), formattedXMLContent, StandardCharsets.UTF_8);
    }

    @Test
    public void testShortPath() {
        final URL resourceURL = getClass().getResource("/ENE-config.properties");
        File theFile = new File(resourceURL.getPath());
        String shortPath = TesunUtilites.shortPath(theFile.getAbsolutePath(), 30);
        Assert.assertTrue(shortPath.startsWith(theFile.getAbsolutePath().substring(0, 5)));
        Assert.assertTrue(shortPath.endsWith("ENE-config.properties"));
    }

}
