package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.Date;

public class UserTaskWaitForBtlgAktualisierung extends AbstractUserTaskWaiter {

    public UserTaskWaitForBtlgAktualisierung(EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super("BTLG_UPDATE_TRIGGER", TestSupportClientKonstanten.BTLG_UPDATE_TRIGGER_STARTET_AT, environmentConfig, tesunClientJobListener);
    }
}
