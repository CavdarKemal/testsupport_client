package de.creditreform.crefoteam.cte.tesun.directorycompare;

import de.creditreform.crefoteam.cte.tesun.directorycompare.impl.FileAppenderAdapter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
* Container für die im {@link DirectoryCompare} verwendeten Verzeichnisse
* User: ralf
* Date: 12.06.14
* Time: 08:58
*/
public class DirectoryCompareFolders
implements Closeable {
    public static final String FILENAME_COMPLETION_REPORT = "DirectoryComparisonReport.txt";
    public static final Charset FILE_ENCODING = Charset.forName("UTF-8");

    private final Logger logger;
    private File firstDir;
    private File secondDir;
    private File resultDir;
    private File resultDifferences;
    private File resultFirstOnly;
    private File resultSecondOnly;
    private FileAppenderAdapter fileAppenderAdapter;

    public DirectoryCompareFolders() {
        logger = LoggerFactory.getLogger(DirectoryCompareFolders.class);
    }

    public File getFirstDir() {
        return firstDir;
    }

    public File getResultFirstOnly() {
        return resultFirstOnly;
    }

    public File getSecondDir() {
        return secondDir;
    }

    public File getResultSecondOnly() {
        return resultSecondOnly;
    }

    public void logDifferences(String zipName, String zipEntryName, List<String> reportedDifferences) {
        try {
            final String child = (zipName + '_' + zipEntryName).replace('/','_').replace('\\','_');
            FileOutputStream outputStream = new FileOutputStream(new File(resultDifferences, child));
            Writer writer = new OutputStreamWriter(outputStream, FILE_ENCODING);
            for (String msg : reportedDifferences) {
                writer.write(msg);
            }
            writer.flush();
            writer.close();
        }
        catch (FileNotFoundException e) {
            throw loggedException(e);
        }
        catch (IOException e) {
            throw loggedException(e);
        }

    }

    public void logCompletionReport(String content) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(resultDir, FILENAME_COMPLETION_REPORT), false);
            Writer writer = new OutputStreamWriter(fos, FILE_ENCODING);
            writer.write("============================================\n");
            writer.write("\nErgebnisse des Vergleiches der Verzeichnisse");
            writer.write("\n1. ");
            writer.write(getFirstDir().getPath());
            writer.write("\n2. ");
            writer.write(getSecondDir().getPath());
            writer.write("\n============================================\n");
            writer.write(content);
            writer.flush();
            writer.close();
        }
        catch (FileNotFoundException e) {
            throw loggedException(e);
        }
        catch (IOException e) {
            throw loggedException(e);
        }
    }

    public DirectoryCompareFolders init(String dirFirst, String dirSecond, String dirResult)
    throws IOException {
        firstDir = assertDirectoryExists(false, dirFirst);
        secondDir = assertDirectoryExists(false, dirSecond);
        resultDir = assertDirectoryExists( true, dirResult);
        fileAppenderAdapter = new FileAppenderAdapter(resultDir, "comparisonResult.log", DirectoryCompareFolders.class).init();
        resultDifferences = assertDirectoryExists( true, dirResult+"/differences");
        resultFirstOnly = assertDirectoryExists( true, dirResult+"/first_only");
        resultSecondOnly = assertDirectoryExists( true, dirResult+"/second_only");
        return this;
    }

    public void close() {
        if (fileAppenderAdapter !=null) {
            try {
                fileAppenderAdapter.close();
            } catch (IOException e) {
                throw loggedException(e);
            }
        }
    }

    private RuntimeException loggedException(String message) {
        logger.error(message);
        return new RuntimeException(message);
    }

    private RuntimeException loggedException(Throwable cause) {
        logger.error(cause.getMessage(), cause);
        return new RuntimeException(cause.getMessage(), cause);
    }

    private File assertDirectoryExists(boolean create, String path) {
        File f = new File(path);
        if (!f.exists()) {
            if (!create) {
                throw loggedException("Verzeichnis " + path + " existiert nicht");
            }
            else {
                if (!f.mkdirs()) {
                    throw loggedException("Verzeichnis " + path + " konnte nicht angelegt werden");
                }
            }
        }
        else if (!f.isDirectory()) {
            throw loggedException("Der Pfad " + path + " ist kein Verzeichnis");
        }
        return f;
    }

    public void copyFile(File source, String namePrefix, File dstParent) {
        try {
            FileUtils.copyFile(source, new File(dstParent, namePrefix+source.getName()), true);
        }
        catch (IOException e) {
            throw loggedException(e);
        }
    }

    public void copyContentToFile(byte[] source, String zipFileIdentifier, String zipEntryName, File dstParent) {
        try {
            FileUtils.writeByteArrayToFile(new File(dstParent, zipFileIdentifier+zipEntryName), source);
        }
        catch (IOException e) {
            throw loggedException(e);
        }
    }
}
