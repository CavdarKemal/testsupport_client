package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.Date;

public class UserTaskWaitForCtImport extends AbstractUserTaskWaiter {

    public UserTaskWaitForCtImport(EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super("FROM_STAGING_INTO_CTE", TestSupportClientKonstanten.CT_IMPORT_STARTET_AT, environmentConfig, tesunClientJobListener);
    }
}
