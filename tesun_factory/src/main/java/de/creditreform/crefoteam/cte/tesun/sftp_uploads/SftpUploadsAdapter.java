package de.creditreform.crefoteam.cte.tesun.sftp_uploads;

import de.creditreform.crefoteam.cte.pathabstraction.PathElementProcessorFactory;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElement;
import de.creditreform.crefoteam.cte.pathabstraction.api.PathElementProcessor;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.PathElementUtils;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TesunDateUtils;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javassist.NotFoundException;
import org.apache.log4j.Level;

public class SftpUploadsAdapter {

    private final SftpUploadAdapterConfig uploadAdapterConfig;
    private final TesunClientJobListener tesunClientJobListener;
    private final PathElementProcessorFactory pathElementProcessorFactory;

    public SftpUploadsAdapter(SftpUploadAdapterConfig uploadAdapterConfig, TesunClientJobListener tesunClientJobListener) {
        this.uploadAdapterConfig = uploadAdapterConfig;
        this.tesunClientJobListener = tesunClientJobListener;
        pathElementProcessorFactory = new PathElementProcessorFactory(uploadAdapterConfig.getSftpCfgMap());
    }

    public ByteArrayOutputStream retrieveFileContent(PathElement pathElement) throws Exception {
        PathElementProcessor processor = pathElementProcessorFactory.create(pathElement);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000 * 1024);
        processor.readFile(baos);
        baos.close();
        pathElementProcessorFactory.close();
        return baos;
    }

    protected List<PathElement> listSftpUploadPathElements(TestCustomer testCustomer) throws NotFoundException {
        String sftpUploadPath = uploadAdapterConfig.getSftpUploadPath();
        String sftpUploadHost = uploadAdapterConfig.getSftpUploadHost();
        PathElementProcessor processorForUploadDirs = pathElementProcessorFactory.create(sftpUploadPath);
        tesunClientJobListener.notifyClientJob(Level.INFO, "\n\tDurchsuche SFTP-Sever '" + sftpUploadHost + "' nach Uploads ab '" + TesunDateUtils.formatCalendar(testCustomer.getLastJobStartetAt()) + "'...");
        // zuerst nach Verzeichnissen im SFTP-Upload-Verzichnis suchen : true
        String info = String.format("\n\t\tSuche nach Unterverzeichnissen im Verzeichnis '%s'...", sftpUploadPath);
        tesunClientJobListener.notifyClientJob(Level.INFO, info);
        List<PathElement> pathElementsList = processorForUploadDirs.listFiles(new UploadDirsPathElementFilter(true));
        if (pathElementsList.isEmpty()) {
            // ansonsten Dateien im SFTP-Upload-Verzichnis suchen : false
            info = String.format("\n\t\t\tAlternativ, suche nach Dateien im Verzeichnis '%s'...", sftpUploadPath);
            tesunClientJobListener.notifyClientJob(Level.INFO, info);
            pathElementsList = processorForUploadDirs.listFiles(new UploadDirsPathElementFilter(false));
        }
        PathElement joungestPathElement = PathElementUtils.findFirstJoungerElement(tesunClientJobListener, pathElementsList, testCustomer.getLastJobStartetAt());
        pathElementsList.clear();
        if (joungestPathElement == null) {
            pathElementProcessorFactory.close();
            return pathElementsList;
        }
        tesunClientJobListener.notifyClientJob(Level.INFO, "\n\t\tDas Jüngste PathElement ist: " + joungestPathElement.getSymbolicPath());
        if (!joungestPathElement.isDirectory()) {
            pathElementsList.add(joungestPathElement);
        } else {
            PathElementProcessor processorForZipFiles = pathElementProcessorFactory.create(joungestPathElement);
            info = String.format("\n\t\tDurchsuche das Jüngste PathElement '%s'...", joungestPathElement.getSymbolicPath());
            tesunClientJobListener.notifyClientJob(Level.INFO, info);
            pathElementsList = processorForZipFiles.listFiles(new UploadDirsPathElementFilter(false));
        }
        pathElementProcessorFactory.close();
        if(pathElementsList.isEmpty()) {
            throw new NotFoundException("Jüngstes PathElement nach " + TesunDateUtils.formatCalendar(testCustomer.getLastJobStartetAt()) + " : " + joungestPathElement.getSymbolicPath() + " enthält keine Exporte!");
        }
        return pathElementsList;
    }

    protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyInfo);
        }
    }

    public Object getCustomerKey() {
        return uploadAdapterConfig.getCustomerKey();
    }
}
