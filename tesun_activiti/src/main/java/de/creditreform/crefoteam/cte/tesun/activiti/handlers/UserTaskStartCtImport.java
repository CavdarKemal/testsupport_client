package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.PropertiesException;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;

public class UserTaskStartCtImport extends AbstractJvmJobStarter {

    public UserTaskStartCtImport(final EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) throws PropertiesException {
        super(environmentConfig.getJobInfoForCtImport(), TestSupportClientKonstanten.CT_IMPORT_STARTET_AT, environmentConfig, tesunClientJobListener);
    }
}