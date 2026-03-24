package de.creditreform.crefoteam.cte.tesun.exports_collector;

import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.restservices.tesun.xmlbinding.configinfo.TesunConfigInfo;
import de.creditreform.crefoteam.cte.tesun.rest.TesunRestService;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by CavdarK on 19.07.2016.
 */
public class ExportsAdapterIntegrationTest {

    public ExportsAdapterIntegrationTest() {
    }

/* TODO
    @Override
    protected TesunConfigInfo getTesunConfig(String envKey) throws Exception {
        environmentConfig.loadEnvironmentConfig( envKey);
        TesunRestService tesunRestServiceWLS =  new TesunRestService(environmentConfig.getRestServiceConfigWLS() );
        TesunConfigInfo tesunConfigInfo = tesunRestServiceWLS.getTesunConfigInfo();
        return tesunConfigInfo;
    }

    @Test
    public void testExportsAdapter_listExportDirs() throws Exception {
        // Test für die ENE
        //List<PathElement> pathElements = listDirPathElements("ENE", "acb");
        List<PathElement> pathElements = listDirPathElements("GEE", "ikaros");
        for (PathElement pathElement : pathElements) {
            Assert.assertTrue(pathElement.getSymbolicPath().startsWith("sftp:junit-config:"));
            System.out.println("\tPathElement: " + pathElement.getSymbolicPath());
        }
        // Test für die GEE
        pathElements = listDirPathElements("GEE", "acb");
        for (PathElement pathElement : pathElements) {
            // Windows-Freigabe-Variante: Assert.assertTrue(pathElement.getSymbolicPath().startsWith("file:\\\\fileserver.gee.creditreform.de\\gee\\"));
            Assert.assertTrue(pathElement.getSymbolicPath().startsWith("sftp:junit-config:acb/export/delta/"));
            System.out.println("\tPathElement: " + pathElement.getSymbolicPath());
        }
    }

    @Test
    public void testExportsAdapter_listExportZips() throws Exception {
        // Test für die ENE
        List<PathElement> pathElements = listZipPathElements("ENE", "acb");
        for (PathElement pathElement : pathElements) {
            String symbolicPath = pathElement.getSymbolicPath();
            Assert.assertTrue(symbolicPath.startsWith("sftp:junit-config:"));
            Assert.assertTrue(symbolicPath.endsWith(".zip"));
            System.out.println("\tPathElement: " + pathElement.getSymbolicPath());
        }
        // Test für die GEE
        pathElements = listZipPathElements("GEE", "acb");
        for (PathElement pathElement : pathElements) {
            String symbolicPath = pathElement.getSymbolicPath();
            // Windows-Freigabe-Variante: Assert.assertTrue(pathElement.getSymbolicPath().startsWith("file:\\\\fileserver.gee.creditreform.de\\gee\\"));
            Assert.assertTrue(pathElement.getSymbolicPath().startsWith("sftp:junit-config:acb/export/delta/"));
            Assert.assertTrue(symbolicPath.endsWith(".zip"));
            System.out.println("\tPathElement: " + pathElement.getSymbolicPath());
        }
    }
    @Test
    public void testExportsAdapter_retrieveZipContents() throws Exception {
        // Test für die ENE
        List<PathElement> pathElements = listZipPathElements("ENE", "acb");
        Map<String, ByteArrayOutputStream> byteArrayOutputStreamMap = retrieveZipContentsFor("ENE", "acb", pathElements);
        List<File> savedZipFiles = saveZipsFor("GEE", "acb", byteArrayOutputStreamMap);
        for (File savedFile : savedZipFiles) {
            System.out.println("\tSaved ZIP: " + savedFile.getAbsolutePath());
        }
        // Test für die GEE
        pathElements = listZipPathElements("GEE", "acb");
        byteArrayOutputStreamMap = retrieveZipContentsFor("GEE", "acb", pathElements);
        savedZipFiles = saveZipsFor("GEE", "acb", byteArrayOutputStreamMap);
        for (File savedFile : savedZipFiles) {
            System.out.println("\tSaved ZIP: " + savedFile.getAbsolutePath());
        }
    }
*/

}
