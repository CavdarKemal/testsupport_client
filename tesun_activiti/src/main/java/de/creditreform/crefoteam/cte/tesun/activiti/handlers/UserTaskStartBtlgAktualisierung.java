package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;

public class UserTaskStartBtlgAktualisierung extends AbstractJvmJobStarter {

    public UserTaskStartBtlgAktualisierung(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) throws PropertiesException {
        super(environmentConfig.getJobInfoForBtlgAktualisierung(), TestSupportClientKonstanten.BTLG_UPDATE_TRIGGER_STARTET_AT, environmentConfig, tesunClientJobListener);
    }
}