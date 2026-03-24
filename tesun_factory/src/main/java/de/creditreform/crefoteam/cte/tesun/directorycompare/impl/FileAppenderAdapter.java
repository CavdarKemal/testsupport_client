package de.creditreform.crefoteam.cte.tesun.directorycompare.impl;

import de.creditreform.crefoteam.technischebasis.log4j.AppenderInstallationHelper;
import de.creditreform.crefoteam.technischebasis.log4j.AppenderInstallationHelperCollect;
import de.creditreform.crefoteam.technischebasis.log4j.AppenderInstallationHelperUniqueByClass;
import org.apache.log4j.Logger;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter für einen {@link FileAppender} zur Nutzung im Rahmen des DirectoryCompare
 * User: ralf
 * Date: 14.07.14
 * Time: 15:07
 */
public class FileAppenderAdapter
implements Cloneable {

    /**
     * Ableitung von {@link FileAppender} zur eindeutigen Kennzeichnung der
     * vom Adapter erzeugten Instanzen
     */
    protected static class FileAppenderByAdapter
    extends FileAppender {
        public FileAppenderByAdapter(Layout layout, String filename, boolean append)
        throws IOException {
            super(layout, filename, append);
        }
    }

    private final List<Class<?>> forClasses;
    private final String fullFileName;

    public FileAppenderAdapter(File parentDir, String name, Class<?>... forClassArray) {
        this.forClasses = Arrays.asList(forClassArray);
        this.fullFileName = new File(parentDir, name).getPath();
    }

    public FileAppenderAdapter init()
    throws IOException {
        FileAppenderByAdapter fileAppender = new FileAppenderByAdapter(new PatternLayout(), fullFileName, false);
        fileAppender.setThreshold(Level.INFO);
        AppenderInstallationHelper aih = new AppenderInstallationHelperCollect(new AppenderInstallationHelperUniqueByClass());
        for (Class<?> clazz : forClasses) {
            aih.installAppender(Logger.getLogger(clazz), FileAppenderByAdapter.class, fileAppender);
        }
        return this;
    }

    public void close() throws IOException {
        AppenderInstallationHelper aih = new AppenderInstallationHelperCollect(new AppenderInstallationHelperUniqueByClass());
        FileAppenderByAdapter fileAppender = new FileAppenderByAdapter(new PatternLayout(), fullFileName, false);
        for (Class<?> clazz : forClasses) {
            aih.removeAppender(Logger.getLogger(clazz), FileAppenderByAdapter.class, fileAppender);
        }
    }

}
