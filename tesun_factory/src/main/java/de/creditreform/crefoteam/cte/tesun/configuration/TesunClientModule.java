package de.creditreform.crefoteam.cte.tesun.configuration;

import java.nio.charset.Charset;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;

import de.creditreform.crefoteam.cte.tesun.directorycompare.DiffListener;
import de.creditreform.crefoteam.cte.tesun.directorycompare.DirectoryCompareFolders;
import de.creditreform.crefoteam.cte.tesun.directorycompare.XmlCompare;
import de.creditreform.crefoteam.cte.tesun.directorycompare.ZipContentCompare;
import de.creditreform.crefoteam.cte.tesun.directorycompare.impl.DiffListenerImpl;
import de.creditreform.crefoteam.cte.tesun.directorycompare.impl.XmlCompareImpl;
import de.creditreform.crefoteam.cte.tesun.directorycompare.impl.ZipContentCompareImpl;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;

/**
 * Guice-{@link Module} für den Testsupport-Client
 * User: ralf
 * Date: 25.02.14
 * Time: 09:04
 */
public class TesunClientModule implements Module {
    private final TesunClientDefaultBindings defaultBindings;

    public TesunClientModule(Charset charset, Provider<TestSupportMutableState> mutableStateProvider) {
        this.defaultBindings = new TesunClientDefaultBindings(charset, mutableStateProvider);
    }

    @Override
    public void configure(Binder binder) {
        defaultBindings.configure(binder);
        DirectoryCompareFolders dcFoldersInstance = new DirectoryCompareFolders();
        binder.bind(DirectoryCompareFolders.class).toInstance(dcFoldersInstance);
        binder.bind(DiffListener.class).toInstance(new DiffListenerImpl(dcFoldersInstance));
        binder.bind(XmlCompare.class).to(XmlCompareImpl.class);
        binder.bind(ZipContentCompare.class).to(ZipContentCompareImpl.class);
    }


}
