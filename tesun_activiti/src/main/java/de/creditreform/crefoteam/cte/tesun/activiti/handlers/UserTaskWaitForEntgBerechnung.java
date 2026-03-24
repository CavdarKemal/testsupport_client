package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;

public class UserTaskWaitForEntgBerechnung extends AbstractUserTaskWaiter {

    public UserTaskWaitForEntgBerechnung(EnvironmentConfig environmentConfig, TesunClientJobListener tesunClientJobListener) {
        super("ENTSCHEIDUNGSTRAEGER_BERECHNUNG", TestSupportClientKonstanten.ENTG_BERECHNUNG_STARTET_AT, environmentConfig, tesunClientJobListener);
    }

}
