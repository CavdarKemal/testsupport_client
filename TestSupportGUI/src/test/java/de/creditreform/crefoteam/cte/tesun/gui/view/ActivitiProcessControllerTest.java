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
 * Dokumentiert den Bug: callback.askClientJob() liefert Integer,
 * wird aber nach Boolean gecastet — mit folgenden Konsequenzen:
 *
 * 1. ClassCastException bei jedem Integer-Rückgabewert
 * 2. Selbst wenn kein Cast-Fehler auftreten würde: Boolean.TRUE.equals(Integer)
 *    ist immer false → RequestAbortedException wird IMMER geworfen,
 *    d.h. der Benutzer kann den Prozess nie fortsetzen.
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
    // BUG-Test 1:
    // askClientJob() liefert Integer(0) = YES_OPTION.
    // (Boolean) Integer(0) → ClassCastException
    // -----------------------------------------------------------------------
    @Test
    public void testPrepareStart_askReturnsInteger0_throwsClassCastException() throws Exception {
        expectExistingTask();
        expect(callback.askClientJob(
                eq(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CONTINUE),
                anyObject()))
                .andReturn(0);  // Integer statt Boolean

        replay(callback, onCustomersReload, helper, activitiService, existingTask, env);

        try {
            controller.prepareStart(helper, env);
            fail("Es muss eine ClassCastException geworfen werden");
        } catch (ClassCastException e) {
            // erwartetes Verhalten — der Cast (Boolean) schlägt fehl
        }

        verify(callback, helper, env);
    }

    // -----------------------------------------------------------------------
    // BUG-Test 2:
    // askClientJob() liefert Integer(1) = NO_OPTION.
    // Auch hier → ClassCastException (gleicher Fehler wie bei YES).
    // -----------------------------------------------------------------------
    @Test
    public void testPrepareStart_askReturnsInteger1_throwsClassCastException() throws Exception {
        expectExistingTask();
        expect(callback.askClientJob(
                eq(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CONTINUE),
                anyObject()))
                .andReturn(1);  // Integer statt Boolean

        replay(callback, onCustomersReload, helper, activitiService, existingTask, env);

        try {
            controller.prepareStart(helper, env);
            fail("Es muss eine ClassCastException geworfen werden");
        } catch (ClassCastException e) {
            // erwartet
        }

        verify(callback, helper, env);
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
    // KORREKTES Verhalten (nach Fix):
    // askClientJob() liefert Integer(0) = YES_OPTION →
    // onCustomersReload.run() wird aufgerufen, existingTask wird zurückgegeben.
    //
    // Dieser Test SCHEITERT mit dem aktuellen Code (ClassCastException)
    // und BESTEHT nach dem Fix.
    // -----------------------------------------------------------------------
    @Test
    public void testPrepareStart_afterFix_askReturnsInteger0_callsOnCustomersReloadAndReturnsTask() throws Exception {
        expectExistingTask();
        expect(callback.askClientJob(
                eq(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CONTINUE),
                anyObject()))
                .andReturn(0);  // YES_OPTION
        onCustomersReload.run();
        expectLastCall().once();

        replay(callback, onCustomersReload, helper, activitiService, existingTask, env);

        CteActivitiTask result = controller.prepareStart(helper, env);

        assertSame("Der zurückgegebene Task muss der existingTask sein", existingTask, result);
        verify(callback, onCustomersReload, helper, env);
    }

    // -----------------------------------------------------------------------
    // KORREKTES Verhalten (nach Fix):
    // askClientJob() liefert Integer(1) = NO_OPTION →
    // RequestAbortedException wird geworfen, onCustomersReload NICHT aufgerufen.
    // -----------------------------------------------------------------------
    @Test
    public void testPrepareStart_afterFix_askReturnsInteger1_throwsRequestAbortedException() throws Exception {
        expectExistingTask();
        expect(callback.askClientJob(
                eq(TesunClientJobListener.ASK_FOR.ASK_OBJECT_CONTINUE),
                anyObject()))
                .andReturn(1);  // NO_OPTION — Abbruch

        replay(callback, onCustomersReload, helper, activitiService, existingTask, env);

        try {
            controller.prepareStart(helper, env);
            fail("RequestAbortedException erwartet");
        } catch (RequestAbortedException e) {
            // erwartet
        }

        // onCustomersReload darf NICHT aufgerufen worden sein
        verify(callback, onCustomersReload, helper, env);
    }
}
