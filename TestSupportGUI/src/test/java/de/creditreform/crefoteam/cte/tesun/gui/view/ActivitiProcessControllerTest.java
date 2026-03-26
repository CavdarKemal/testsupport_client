package de.creditreform.crefoteam.cte.tesun.gui.view;

import de.creditreform.crefoteam.activiti.CteActivitiService;
import de.creditreform.crefoteam.activiti.CteActivitiTask;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.gui.utils.TestSupportHelper;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Tests für ActivitiProcessController#prepareStart().
 *
 * Dokumentiert das korrigierte Verhalten: callback.askClientJob() liefert Integer
 * (JOptionPane.YES_OPTION = 0 bzw. NO_OPTION = 1).
 * Der Fix behebt den früheren Boolean-Cast und die falsche Logik.
 */
public class ActivitiProcessControllerTest {

    private TesunClientJobListener callback;
    private Runnable onCustomersReload;
    private TestSupportHelper helper;
    private CteActivitiService activitiService;
    private CteActivitiTask existingTask;
    private EnvironmentConfig env;
    private ActivitiProcessController controller;

    @Before
    public void setUp() throws Exception {
        callback          = createMock(TesunClientJobListener.class);
        onCustomersReload = createMock(Runnable.class);
        helper            = createMock(TestSupportHelper.class);
        activitiService   = createMock(CteActivitiService.class);
        existingTask      = createMock(CteActivitiTask.class);
        env               = createMock(EnvironmentConfig.class);

        controller = new ActivitiProcessController(callback, onCustomersReload);
    }

    // -----------------------------------------------------------------------
    // Hilfsmethode: setzt das Standardverhalten für helper + env auf, so dass
    // killOrContinueRunningActivitiProcess den existingTask zurückgibt.
    // -----------------------------------------------------------------------
    private void expectExistingTask() throws Exception {
        expect(helper.getActivitiRestService()).andReturn(activitiService);
        expect(env.getActivitProcessKey()).andReturn("ENE");
        expect(env.getActivitiProcessName()).andReturn("ENE-TestAutomationProcess");
        expect(helper.killOrContinueRunningActivitiProcess(
                "ENE", "ENE-TestAutomationProcess", true))
                .andReturn(existingTask);
    }

    // -----------------------------------------------------------------------
    // BUG-Test 3 (Logikfehler):
    // Selbst wenn der Cast irgendwie umgangen würde und Integer(0) ankäme:
    // Boolean.TRUE.equals(Integer(0)) == false → RequestAbortedException.
    // Der Benutzer kann also NIE fortsetzen, egal was er antwortet.
    // Dieser Test zeigt, dass die Logik auch nach Behebung des Cast-Fehlers
    // korrigiert werden muss.
    // -----------------------------------------------------------------------
    @Test
    public void testPrepareStart_booleanTrueEqualsInteger0_isAlwaysFalse() {
        // Dokumentiert das Logikproblem — kein Controller-Aufruf nötig
        Integer yesOption = 0;
        assertFalse("Boolean.TRUE.equals(Integer(0)) muss false sein — daher immer Abbruch",
                Boolean.TRUE.equals(yesOption));
    }

    // -----------------------------------------------------------------------
    // Korrektes Verhalten: killOrContinueRunningActivitiProcess liefert
    // existingTask (User hat "Ja" gewählt) → prepareStart gibt ihn direkt
    // zurück, ohne askClientJob aufzurufen.
    // -----------------------------------------------------------------------
    @Test
    public void testPrepareStart_existingTask_returnedDirectlyWithoutAskClientJob() throws Exception {
        expectExistingTask();
        // askClientJob darf NICHT aufgerufen werden — Dialog liegt in killOrContinueRunningActivitiProcess
        replay(callback, onCustomersReload, helper, activitiService, existingTask, env);

        CteActivitiTask result = controller.prepareStart(helper, env);

        assertSame("Der zurückgegebene Task muss der existingTask sein", existingTask, result);
        verify(callback, onCustomersReload, helper, env);
    }

    // -----------------------------------------------------------------------
    // Korrektes Verhalten: killOrContinueRunningActivitiProcess wirft
    // RequestAbortedException (User hat "Abbrechen" gewählt) →
    // prepareStart propagiert die Exception.
    // -----------------------------------------------------------------------
    @Test
    public void testPrepareStart_killOrContinueThrowsRequestAbortedException_propagates() throws Exception {
        expect(helper.getActivitiRestService()).andReturn(activitiService);
        expect(env.getActivitProcessKey()).andReturn("ENE");
        expect(env.getActivitiProcessName()).andReturn("ENE-TestAutomationProcess");
        expect(helper.killOrContinueRunningActivitiProcess(
                "ENE", "ENE-TestAutomationProcess", true))
                .andThrow(new RequestAbortedException("Aborted!"));

        replay(callback, onCustomersReload, helper, activitiService, existingTask, env);

        try {
            controller.prepareStart(helper, env);
            fail("RequestAbortedException erwartet");
        } catch (RequestAbortedException e) {
            // erwartet — CANCEL-Antwort im Dialog von killOrContinueRunningActivitiProcess
        }

        verify(callback, onCustomersReload, helper, env);
    }
}
