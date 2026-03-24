package de.creditreform.crefoteam.cte.tesun.util.directorytree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Implementierung von {@link FileActionFactory} zum Sammeln aller Einträge
 * User: ralf
 * Date: 10.02.14
 * Time: 14:56
 */
public class FileActionFactoryCollectEntries
        implements FileActionFactory {

    private final List<File> files;
    private final List<File> directories;
    private final Callable<Void> nopAction;

    public FileActionFactoryCollectEntries() {
        files = new ArrayList<>();
        directories = new ArrayList<>();
        nopAction = new FileActionNop();
    }

    public List<File> getFiles() {
        return files;
    }

    public List<File> getDirectories() {
        return directories;
    }

    @Override
    public Callable<Void> createActionOnDirectory(File f) {
        directories.add(f);
        return nopAction;
    }

    @Override
    public Callable<Void> createActionOnFile(File f) {
        files.add(f);
        return nopAction;
    }
}
