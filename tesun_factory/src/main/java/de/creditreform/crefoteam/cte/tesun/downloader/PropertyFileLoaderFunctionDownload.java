package de.creditreform.crefoteam.cte.tesun.downloader;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class PropertyFileLoaderFunctionDownload extends PropertyFileLoaderFunctionRestAbstract {
    private final FileFilter fileFilter;

    public PropertyFileLoaderFunctionDownload(EnvironmentConfig environmentConfig, Map<String, TestCustomer> activeCustomersMap, TesunClientJobListener tesunClientJobListener) throws PropertiesException {
        super(environmentConfig, tesunClientJobListener);
        fileFilter = new TestCustomerFileFilter(activeCustomersMap, false);
    }

    @Override
    public void apply(NameCrefoPfad input) throws Exception {
        final Long crefo = input.getTestFallCrefo();
        String filePath = String.format("%s/%d.xml", input.getPathInfo().getFullPath(), crefo);
        String relativeSubDir = input.getPathInfo().getRelativeSubDir();
        if (!fileFilter.accept(new File(filePath))) {
            return;
        }
        try {
            String crefoXML = tesunRestServiceWLS.downloadCrefo(crefo);
            try {
                File outputFile = new File(environmentConfig.getNewTestCasesRoot(), String.format("%s/%d.xml", relativeSubDir, crefo));
                outputFile.getParentFile().mkdirs();
                final String formattedXMLContent = TesunUtilites.toPrettyString(crefoXML, 2);
                FileUtils.writeStringToFile(outputFile, formattedXMLContent, Charset.forName("UTF-8"));
                String strInfo = String.format("\n\tCrefo %d für Testfall [ %s/%s ] wurde heruntergeladen und abgespeichert", crefo, relativeSubDir, input.getTestFallName());
                notifyTesunClientJobListener(Level.INFO, strInfo);
            } catch (Exception ex) {
                String strErr = "\n\t!!! Fehler beim Speichern der heruntergeladenen Crefo-XML nach " + filePath;
                notifyTesunClientJobListener(Level.ERROR, strErr);
            }
        } catch (Exception ex) {
            StringBuilder strErr = new StringBuilder("!!! Download gescheitert: ");
            if (ex.getMessage().contains("204 (No Content)")) {
                strErr.append(String.format("Crefo %d für Testfall [ %s/%s ] ist nicht im CTE-Bestand!", crefo, relativeSubDir, input.getTestFallName()));
                notifyTesunClientJobListener(Level.WARN, "\n" + strErr.toString());
                File outputFile = new File(environmentConfig.getNewTestCasesRoot(), String.format("%s/" + TestSupportClientKonstanten.ERRORS_TXT, relativeSubDir));
                FileOutputStream errorFileOS = new FileOutputStream(outputFile, true);
                errorFileOS.write(strErr.toString().getBytes("UTF-8"));
                errorFileOS.write("\n".getBytes("UTF-8"));
                IOUtils.closeQuietly(errorFileOS);
            } else {
                strErr.append(ex.getMessage());
                throw new RuntimeException(strErr.toString());
            }
        }
    }

}
