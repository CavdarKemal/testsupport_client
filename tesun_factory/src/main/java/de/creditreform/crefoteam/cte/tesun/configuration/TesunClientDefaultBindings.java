package de.creditreform.crefoteam.cte.tesun.configuration;

import java.nio.charset.Charset;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;

import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;
import de.creditreform.crefoteam.cte.tesun.util.propertyfiles.PropertyFileLoader;

/**
 * Default-Bindings, Job-übergreifend
 * User: ralf
 * Date: 09.05.14
 * Time: 15:16
 */
public class TesunClientDefaultBindings
implements Module {
    private final Charset charset;
    private final Provider<TestSupportMutableState> mutableStateProvider;

    public TesunClientDefaultBindings(Charset charset, Provider<TestSupportMutableState> mutableStateProvider) {
        this.charset = charset;
        this.mutableStateProvider = mutableStateProvider;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind( PropertyFileLoader.class ).toInstance( new PropertyFileLoader( charset ) );
        binder.bind( TestSupportMutableState.class ).toProvider( mutableStateProvider );
    }
}
