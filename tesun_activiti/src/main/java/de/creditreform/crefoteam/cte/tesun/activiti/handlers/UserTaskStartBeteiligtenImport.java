package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;

public class UserTaskStartBeteiligtenImport extends AbstractJvmJobStarter {

    public UserTaskStartBeteiligtenImport(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) throws PropertiesException {
        super(environmentConfig.getJobInfoForBtlgImport(), TestSupportClientKonstanten.BTLG_IMPORT_STARTET_AT, environmentConfig, tesunClientJobListener);
    }
}