package de.creditreform.crefoteam.cte.tesun.util.directorytree;

import de.creditreform.crefoteam.cte.tesun.util.replacer.Replacer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Implementierung von {@link FileActionFactory} für das Kopieren von Dateien
 * mit gleichzeitiger Textersetzung
 * User: ralf
 * Date: 10.02.14
 * Time: 15:29
 */
public class FileActionFactoryCopyReplace implements FileActionFactory {
    private final String srcAbsolutePath;
    private final String targetAbsolutePath;
    private final Replacer replacer;

    public FileActionFactoryCopyReplace(Replacer replacer, File srcDir, File dstDir) {
        this.replacer = replacer;
        this.srcAbsolutePath = srcDir.getAbsolutePath();
        this.targetAbsolutePath = dstDir.getAbsolutePath();
    }

    @Override
    public Callable<Void> createActionOnDirectory(File f) {
        File newDirectory = newFile(srcAbsolutePath, targetAbsolutePath, f);
        return new CallableCreateDir(newDirectory);
    }

    @Override
    public Callable<Void> createActionOnFile(File f) {
        File newFile = newFile(srcAbsolutePath, targetAbsolutePath, f);
        return new CallableCopyFile(replacer, f, newFile);
    }

    private File newFile(String srcAbsolutePath, String targetAbsolutePath, File current) {
        String currentName = current.getAbsolutePath();
        // Das Eigner-VC XML-Tag sollte im Dateinamen nicht vorkommen. Wenn doch, ignorieren wir das. Der Key für
        // ein Speichern des ursprünglichen Eigner-VC ist null.
        String substring = currentName.substring(srcAbsolutePath.length());
        String offset = replacer.replace(null, substring).toString();
        return new File(targetAbsolutePath + offset);
    }

    public static class CallableCreateDir implements Callable<Void> {
        private final File newDirectory;

        public CallableCreateDir(File newDirectory) {
            this.newDirectory = newDirectory;
        }

        @Override
        public Void call()
                throws Exception {
            if (newDirectory.exists()) {
                if (!newDirectory.isDirectory()) {
                    throw new RuntimeException("file exists and is not a directory: " + newDirectory.getPath());
                }
            } else {
                if (!newDirectory.mkdirs()) {
                    throw new RuntimeException("unable to create new directory: " + newDirectory.getPath());
                }
            }
            return null;
        }
    }

    public static class CallableCopyFile implements Callable<Void> {
        private final Replacer replacer;
        private final File sourceFile;
        private final File newFile;

        public CallableCopyFile(Replacer replacer, File sourceFile, File newFile) {
            this.replacer = replacer;
            this.sourceFile = sourceFile;
            this.newFile = newFile;
        }

        @Override
        public Void call() throws Exception {
            if (!newFile.exists()) {
                if (!newFile.createNewFile()) {
                    throw new RuntimeException("unable to create new file: " + newFile.getPath());
                }
            }
            FileInputStream inpStream = null;
            FileOutputStream outStream = null;
            try {
                inpStream = new FileInputStream(sourceFile);
                outStream = new FileOutputStream(newFile);
                // der ursprüngliche Eigner-VC wird unter dem Namen der Source-Datei gespeichert
                replacer.copyAndReplace(sourceFile.getName(), inpStream, outStream);
                Date oldDate = new Date(sourceFile.lastModified());
                Date newDate = new Date(newFile.lastModified());
                // System.out.println("Src-Datei : " + sourceFile + " mit lastModified-Date: " + TesunDateUtils.DATE_FORMATTER_DD_MM_YYYY_HH_MM_SS.format(oldDate));
                // System.out.println("Dst-Datei : " + newFile + " mit lastModified-Date: " + TesunDateUtils.DATE_FORMATTER_DD_MM_YYYY_HH_MM_SS.format(newDate));
                newFile.setLastModified(sourceFile.lastModified());
                // System.out.println("New-Datei : " + newFile + " mit lastModified-Date: " + TesunDateUtils.DATE_FORMATTER_DD_MM_YYYY_HH_MM_SS.format(oldDate));
            } catch (Exception ex) {
                System.out.println("Exception " + ex.getMessage() + "\nbei Src-Datei : " + sourceFile + " und Dst-Datei_ "  + newFile);
            } finally {
                if (inpStream != null) inpStream.close();
                if (outStream != null) outStream.close();
            }
            return null;
        }
    }

}
