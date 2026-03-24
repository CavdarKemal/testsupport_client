package de.creditreform.crefoteam.cte.tesun.tracking;

import de.creditreform.crefoteam.cte.monitoringbackend.xmlbinding.CteCrefoTrackingErgebnis;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public abstract class AbstractTrackingInfoDumper {

    protected File outputFile;

    public void prepareOutputFile(String umgebung, String customer, File sourceFile, TesunClientJobListener tesunClientJobListener) {
        URL rscUrl = getClass().getResource("/");
        outputFile = new File(rscUrl.getFile(), "TrackingInfo-" + umgebung + "-" + getTrackingType() + "-" + (customer != null ? customer : "ALL") + "-" + sourceFile.getName() + ".csv");
        outputFile.delete();
        tesunClientJobListener.notifyClientJob(Level.INFO, "Ergebnisse werden in derDatei " + outputFile.getAbsolutePath() + " gespeichert.");
        try {
            FileUtils.writeStringToFile(outputFile, getHeader(), Charset.forName("UTF-8"), false);
        } catch (IOException ex) {
            tesunClientJobListener.notifyClientJob(Level.ERROR, "Fehler beim Schreiben in die Datei " + outputFile.getAbsolutePath());
        }
    }

    protected void writeToiFile(StringBuilder sbDumpInfo, TesunClientJobListener tesunClientJobListener) {
        try {
            FileUtils.writeStringToFile(outputFile, sbDumpInfo.toString(), Charset.forName("UTF-8"), true);
        } catch (IOException ex) {
            tesunClientJobListener.notifyClientJob(Level.ERROR, "Fehler beim Schreiben in die Datei " + outputFile.getAbsolutePath());
        }
    }

    abstract public void dumpCrefoTrackingErgebnisInfo(String customer, CteCrefoTrackingErgebnis cteCrefoTrackingErgebnis, TesunClientJobListener tesunClientJobListener);

    abstract public String getHeader();

    abstract public String getTrackingType();
}
