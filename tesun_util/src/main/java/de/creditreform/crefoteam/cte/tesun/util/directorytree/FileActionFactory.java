package de.creditreform.crefoteam.cte.tesun.util.directorytree;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: ralf
 * Date: 10.02.14
 * Time: 14:46
 * To change this template use File | Settings | File Templates.
 */
public interface FileActionFactory {

    Callable<Void> createActionOnDirectory(File f);

    Callable<Void> createActionOnFile(File f);

}
