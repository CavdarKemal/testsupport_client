package de.creditreform.crefoteam.cte.tesun.util.directorytree;

import java.util.concurrent.Callable;

/**
 * No-Op Implementierung von Callable<Void>
 * User: ralf
 * Date: 10.02.14
 * Time: 15:00
 */
public class FileActionNop
        implements Callable<Void> {
    @Override
    public Void call()
            throws Exception {
        return null;
    }
}
