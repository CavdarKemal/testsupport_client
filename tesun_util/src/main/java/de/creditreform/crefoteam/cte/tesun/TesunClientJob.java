package de.creditreform.crefoteam.cte.tesun;

import com.google.inject.Module;
import com.google.inject.Provider;
import de.creditreform.crefoteam.cte.tesun.TesunClientJob.JOB_RESULT;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportMutableState;

import java.nio.charset.Charset;
import java.util.concurrent.Callable;

/**
 * Interface für die im Testsupport Client-seitig auszuführenden Jobs
 * User: ralf
 * Date: 13.02.14
 * Time: 10:54
 */
public interface TesunClientJob extends Callable<JOB_RESULT> {

    enum JOB_RESULT {
        OK(0), ERROR(-1);

        private final int numericValue;
        private Object userObject;

        JOB_RESULT(int numericValue) {
            this.numericValue = numericValue;
        }

        public int getNumericValue() {
            return numericValue;
        }

        public Object getUserObject() {
            return userObject;
        }

        public JOB_RESULT setUserObject(Object userObject) {
            this.userObject = userObject;
            return this;
        }
    }

    /**
     * Lese die Bezeichnung des Komandozeilen-Befehls, mit dem dieser Job
     * gestartet werden kann
     */
    String getJobCommandName();

    /**
     * Lese die Erläuterung des Komandozeilen-Befehls, mit dem dieser Job
     * gestartet werden kann
     */
    String getJobCommandDescription();

    /**
     * Setze die erkannte Konfiguration, initialisiere die Instanz
     *
     * @param envConfig
     */
    void init(EnvironmentConfig envConfig) throws Exception;

    Module getGuiceModule(Charset charset, Provider<TestSupportMutableState> mutableStateProvider);

}
