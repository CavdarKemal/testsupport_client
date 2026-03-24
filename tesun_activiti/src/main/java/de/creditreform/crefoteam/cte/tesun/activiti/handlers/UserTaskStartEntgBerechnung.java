package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;

public class UserTaskStartEntgBerechnung extends AbstractJvmJobStarter {

    public UserTaskStartEntgBerechnung(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) throws PropertiesException {
        super(environmentConfig.getJobInfoForEntgBerechnung(), TestSupportClientKonstanten.ENTG_BERECHNUNG_STARTET_AT, environmentConfig, tesunClientJobListener);
    }
}