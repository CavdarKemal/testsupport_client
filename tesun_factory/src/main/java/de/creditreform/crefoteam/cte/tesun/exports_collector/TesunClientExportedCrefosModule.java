package de.creditreform.crefoteam.cte.tesun.exports_collector;

import java.nio.charset.Charset;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;

import de.creditreform.crefoteam.cte.tesun.configuration.TesunClientDefaultBindings;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;

/**
 * Guice-{@link com.google.inject.Module} für den Testsupport-Client
 * User: ralf
 * Date: 25.02.14
 * Time: 09:04
 */
public class TesunClientExportedCrefosModule implements Module {
    private final TesunClientDefaultBindings defaultBindings;

    public TesunClientExportedCrefosModule(Charset charset, Provider<TestSupportMutableState> mutableStateProvider) {
        this.defaultBindings = new TesunClientDefaultBindings(charset, mutableStateProvider);
    }

    @Override
    public void configure(Binder binder) {
        defaultBindings.configure(binder);
    }
}
