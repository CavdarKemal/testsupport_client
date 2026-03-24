package de.creditreform.crefoteam.cte.tesun.directorycompare;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Guice-Module für den Unit-Test im Bereich {@link DirectoryCompare}
 * User: ralf
 * Date: 16.06.14
 * Time: 13:08
 */
public class DirectoryCompareJUnitModule
implements Module {
    private final DiffListener listener;

    private final ZipContentCompare zipContentCompare;

    public DirectoryCompareJUnitModule(DiffListener listener, ZipContentCompare zipContentCompare) {
        this.listener = listener;
        this.zipContentCompare = zipContentCompare;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(DiffListener.class).toInstance(listener);
        binder.bind(ZipContentCompare.class).toInstance(zipContentCompare);
    }
}
