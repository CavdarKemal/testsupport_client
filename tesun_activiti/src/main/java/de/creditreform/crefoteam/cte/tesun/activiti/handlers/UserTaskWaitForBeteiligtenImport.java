package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;

public class UserTaskWaitForBeteiligtenImport extends AbstractUserTaskWaiter {

    public UserTaskWaitForBeteiligtenImport(EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super("BETEILIGUNGEN_IMPORT", TestSupportClientKonstanten.BTLG_IMPORT_STARTET_AT, environmentConfig, tesunClientJobListener);
    }
}
