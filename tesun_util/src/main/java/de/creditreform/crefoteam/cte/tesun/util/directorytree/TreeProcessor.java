package de.creditreform.crefoteam.cte.tesun.util.directorytree;

import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.TesunUtilites;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Utility-Klasse zur Verarbeitung eines Verzeichnisbaums mit Testfällen
 * User: ralf
 * Date: 10.02.14
 * Time: 14:37
 */
public class TreeProcessor implements Callable<Void> {
    private final FileFilter fileFilter;
    private final File srcDir;
    private final FileActionFactory fileActionFactory;
    private final Logger logger;
    private TesunClientJobListener tesunClientJobListener;

    public TreeProcessor(FileFilter fileFilter, File srcDir, FileActionFactory fileActionFactory) {
        this.srcDir = srcDir;
        this.fileActionFactory = fileActionFactory;
        this.fileFilter = fileFilter != null ? fileFilter : FileFilterUtils.trueFileFilter();
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public Void call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, FutureTask> futureTasksMap = new HashMap<>();
        TreeTraverser<File> traverser = Files.fileTreeTraverser();
        int nCount = 0;
        for (File theFile : traverser.preOrderTraversal(srcDir)) {
            if (fileFilter.accept(theFile)) {
                final Callable<Void> callable;
                String absolutePath = TesunUtilites.shortPath(theFile, 50);
                try {
                    if (theFile.isDirectory()) {
                        notifyTesunClientJobListener(Level.INFO, "\n\tTreeProcessor#call() verarbeitet das Verzeichnis '" + absolutePath + "'...");
                        callable = fileActionFactory.createActionOnDirectory(theFile);
                        callable.call();
                    } else {
                        if (++nCount % 100 == 0) {
                            notifyTesunClientJobListener(Level.INFO, ".");
                        }
                        callable = fileActionFactory.createActionOnFile(theFile);
                        FutureTask<Callable> futureTask = new FutureTask(callable);
                        futureTasksMap.put(theFile.getAbsolutePath(), futureTask);
                        //notifyTesunClientJobListener(Level.INFO, "\n\tTreeProcessor#call() Task für '" + absolutePath + "' gestartet");
                        executor.execute(futureTask);
                    }
                } catch (Exception e) {
                    notifyTesunClientJobListener(Level.ERROR, "\n\tFehler beim verarbeiten des Verzeichnisses '" + absolutePath + "'!\n\t\t\t'" + e.getMessage());
                }
            }
        }
        executor.shutdown();
        TesunUtilites.waitForFutureTasks(futureTasksMap, tesunClientJobListener);
        return null;
    }

    public void setTesunClientJobListener(TesunClientJobListener tesunClientJobListener) {
        this.tesunClientJobListener = tesunClientJobListener;
    }

    protected void notifyTesunClientJobListener(Level level, String notifyInfo) {
        if (tesunClientJobListener != null) {
            tesunClientJobListener.notifyClientJob(level, notifyInfo);
        } else if (logger != null && !notifyInfo.equals(".")) {
            logger.info(notifyInfo); // TODO abh. vom Level!!!
        }
    }

}
