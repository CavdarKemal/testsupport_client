package de.creditreform.crefoteam.cte.tesun.gui.view;

import de.creditreform.crefoteam.activiti.CteActivitiProcess;
import de.creditreform.crefoteam.activiti.CteActivitiService;
import de.creditreform.crefoteam.activiti.CteActivitiTask;
import de.creditreform.crefoteam.cte.tesun.TesunClientJobListener;
import de.creditreform.crefoteam.cte.tesun.activiti.handlers.UserTaskRunnable;
import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;
import de.creditreform.crefoteam.cte.tesun.gui.utils.TestSupportHelper;
import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TestCustomer;
import de.creditreform.crefoteam.cte.tesun.util.TestSupportClientKonstanten;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import org.apache.log4j.Level;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ActivitiProcessController {

    private final TesunClientJobListener callback;
    private final Runnable onCustomersReload;

    // Process state — set in runProcess(), used in runLoop() and stop()
    private volatile Thread processThread;
    private volatile boolean running;
    private EnvironmentConfig environmentConfig;
    private Map<String, Object> taskVariablesMap;
    private CteActivitiService cteActivitiServices;
    private Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> selectedCustomersMapMap;
    private Integer processInstanceID;
    private Integer mainProcessInstanceID;
    private CteActivitiTask activitUserTaskToContinue;

    public ActivitiProcessController(TesunClientJobListener callback, Runnable onCustomersReload) {
        this.callback = callback;
        this.onCustomersReload = onCustomersReload;
    }

    /**
     * Sync part: checks/handles Activiti status (BPMN deploy or continue existing process).
     * Called on EDT. Throws RequestAbortedException if user aborts.
     */
    public CteActivitiTask prepareStart(TestSupportHelper helper, EnvironmentConfig env) throws Exception {
        CteActivitiService activitiRestService = helper.getActivitiRestService();
        CteActivitiTask cteActivitiTask = helper.killOrContinueRunningActivitiProcess(env.getActivitProcessKey(), env.getActivitiProcessName(), true);
        if (cteActivitiTask == null) {
            // Kein laufender Prozess oder User hat NO gewählt (Prozess wurde bereits gelöscht)
            // → BPMN deployen und frisch starten
            callback.notifyClientJob(Level.INFO, "Deploye ACTIVITI-Prozess-Definitionsdateien für " + env.getCurrentEnvName());
            List<File> bpmnFileList = GUIStaticUtils.uploadActivitiProcessesFromClassPath(activitiRestService, env.getCurrentEnvName());
            bpmnFileList.forEach(bpmnFile -> callback.notifyClientJob(Level.INFO, "\nACTIVITI-Prozess-Definitionsdatei '" + bpmnFile.getName() + "' deployed."));
        }
        // cteActivitiTask != null → User hat YES gewählt → Prozess fortsetzen
        return cteActivitiTask;
    }

    /**
     * Async part: starts the process loop in a new thread. No GUI calls here.
     */
    public void runProcess(TestSupportHelper helper, EnvironmentConfig env, Map<String, Object> taskVariablesMap, Map<TestSupportClientKonstanten.TEST_PHASE, Map<String, TestCustomer>> activeTestCustomersMapMap, CteActivitiTask cteActivitiTask) throws Exception {
        this.environmentConfig = env;
        this.taskVariablesMap = taskVariablesMap;
        this.cteActivitiServices = helper.getActivitiRestService();
        this.selectedCustomersMapMap = activeTestCustomersMapMap;
        this.activitUserTaskToContinue = cteActivitiTask;
        this.running = true;

        String activitiProcessName = env.getActivitiProcessName();
        callback.notifyClientJob(Level.INFO, "\nStarte ACTIVITI-Prozess '" + activitiProcessName + "'...");
        callback.notifyClientJob(Level.INFO, "\n===========   Activiti-Process gestartet.   ===========");
        TimelineLogger.info(ActivitiProcessController.class, "\n===========    Activiti-Process gestartet.    ===========");
/* CLAUDE_MODE
        helper.checkStartCoinditions(activeTestCustomersMapMap.get(TestSupportClientKonstanten.TEST_PHASE.PHASE_2), true);
*/
        processThread = new Thread(this::runLoop);
        processThread.start();
    }

    private void runLoop() {
        if (activitUserTaskToContinue == null) {
            CteActivitiProcess cteActivitiProcess;
            try {
                String processDefinitionKey = (String) taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVITI_PROCESS_NAME);
                cteActivitiProcess = cteActivitiServices.startProcess(processDefinitionKey, taskVariablesMap);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            processInstanceID = cteActivitiProcess.getId();
            mainProcessInstanceID = processInstanceID;
        } else {
            // Task kann im Sub-Prozess liegen → processInstanceId wäre Sub-Prozess-ID.
            // Hauptprozess-ID separat abfragen, damit isProcessEnded() korrekt funktioniert.
            processInstanceID = activitUserTaskToContinue.getProcessInstanceId();
            mainProcessInstanceID = findMainProcessInstanceId();
        }
        while (running) {
            if (isProcessEnded()) {
                callback.notifyClientJob(Level.INFO, "\nACTIVITI-Prozess beendet.");
                break;
            }
            try {
                CteActivitiTask currentUserTask = selectTaskForBusinessKey();
                if (currentUserTask == null) {
                    break;
                }
                if (!running) {
                    break;
                }
                handleCurrentTask(currentUserTask);
            } catch (Exception ex) {
                TimelineLogger.error(ActivitiProcessController.class, "runLoop: Exception in handleCurrentTask", ex);
                if (ex.getMessage() != null && ex.getMessage().contains("is already claimed by someone else.")) {
                    throw new RuntimeException(ex);
                }
                try {
                    cteActivitiServices.signalEventReceived(environmentConfig.getCurrentEnvName() + "cancelProcessSignal");
                    break;
                } catch (Exception ex1) {
                    TimelineLogger.error(ActivitiProcessController.class, "runLoop: signalEventReceived fehlgeschlagen", ex1);
                    throw new RuntimeException(ex1);
                }
            }
        }
        processThread = null;
        callback.notifyClientJob(Level.INFO, null); // Prozess beendet
    }

    private void handleCurrentTask(CteActivitiTask currentUserTask) throws Exception {
        dumpUserTask(currentUserTask);
        processInstanceID = currentUserTask.getProcessInstanceId();
        notifyAboutProcessImage();
        callback.notifyClientJob(Level.INFO, currentUserTask);

        String strCurrentUTaskPhase = currentUserTask.getVariables().get(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE);
        TestSupportClientKonstanten.TEST_PHASE currentUTaskPhase = TestSupportClientKonstanten.TEST_PHASE.valueOf(strCurrentUTaskPhase);
        taskVariablesMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_TEST_PHASE, currentUTaskPhase);

        cteActivitiServices.claimTask(currentUserTask, cteActivitiServices.getActivitiRestInvokerConfig().getServiceUser());
        notifyAboutProcessImage();

        UserTaskRunnable userTaskRunnable = getUserTaskRunnableInstance(currentUserTask.getTaskDefinitionKey());
        taskVariablesMap = userTaskRunnable.runTask(taskVariablesMap);

        Map<String, Object> mapForACTIVITI = convertMapForACTIVITI(taskVariablesMap);
        cteActivitiServices.completeTask(currentUserTask, mapForACTIVITI);
    }

    private void dumpUserTask(CteActivitiTask currentUserTask) {
        TimelineLogger.info(this.getClass(), "CteActivitiTask:: "
                + "\tName: " + currentUserTask.getName()
                + ", Id: " + currentUserTask.getId()
                + ", ProcessDefinitionId: " + currentUserTask.getProcessDefinitionId()
                + ", TaskDefinitionKey: " + currentUserTask.getTaskDefinitionKey()
                + ", ProcessInstanceId: " + currentUserTask.getProcessInstanceId()
                + ", ExecutionId: " + currentUserTask.getExecutionId()
        );
    }

    private Map<String, Object> convertMapForACTIVITI(Map<String, Object> userTasksMap) {
        Map<String, Object> activitiMap = new TreeMap<>(userTasksMap);
        userTasksMap.keySet().forEach(key -> {
            Object value = userTasksMap.get(key);
            if (value != null) {
                String strValue;
                if (value instanceof TestSupportClientKonstanten.TEST_TYPES) {
                    strValue = TestSupportClientKonstanten.TEST_TYPES.valueOf(value.toString()).name();
                } else if (value instanceof TestSupportClientKonstanten.TEST_PHASE) {
                    strValue = TestSupportClientKonstanten.TEST_PHASE.valueOf(value.toString()).name();
                } else {
                    strValue = value.toString();
                }
                activitiMap.put(key, strValue);
            } else {
                callback.notifyClientJob(Level.ERROR, "\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!! UserTask Variables-Map eine NULL-Wert für Key '" + key + "'!");
            }
        });
        activitiMap.remove(TesunClientJobListener.UT_TASK_PARAM_NAME_ACTIVE_CUSTOMERS);
        return activitiMap;
    }

    /**
     * Baut eine minimale Query-Map mit nur MEIN_KEY.
     * Wird für isProcessEnded() und findMainProcessInstanceId() verwendet,
     * damit die Activiti-Abfrage unabhängig von der Variablenmenge im taskVariablesMap funktioniert.
     */
    private Map<String, Object> buildMinimalQueryMap() {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY,
                taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_MEIN_KEY));
        return queryMap;
    }

    /**
     * Ermittelt die Hauptprozess-Instanz-ID beim Fortsetzen eines laufenden Prozesses.
     * Notwendig, weil activitUserTaskToContinue.getProcessInstanceId() die Sub-Prozess-ID
     * liefert wenn der Prozess im Sub-Prozess gestoppt wurde.
     */
    private Integer findMainProcessInstanceId() {
        try {
            List<CteActivitiProcess> procs = cteActivitiServices.queryProcessInstances(
                    environmentConfig.getActivitiProcessName(), buildMinimalQueryMap());
            if (procs != null && !procs.isEmpty()) {
                return procs.get(0).getId();
            }
        } catch (Exception e) {
            TimelineLogger.warn(ActivitiProcessController.class, "findMainProcessInstanceId fehlgeschlagen — verwende processInstanceID als Fallback", e);
        }
        return processInstanceID;
    }

    private boolean isProcessEnded() {
        try {
            List<CteActivitiProcess> processes = cteActivitiServices.queryProcessInstances(
                    environmentConfig.getActivitiProcessName(), buildMinimalQueryMap());
            TimelineLogger.info(ActivitiProcessController.class, "isProcessEnded: MainProcessInstanceID=" + mainProcessInstanceID +
                    ", gefundene Instanzen=" + (processes == null ? "null" : processes.size()));
            return processes == null || processes.stream().noneMatch(p -> mainProcessInstanceID.equals(p.getId()));
        } catch (Exception e) {
            return false;
        }
    }

    private CteActivitiTask selectTaskForBusinessKey() throws Exception {
        int nCount = 0;
        while (nCount < 3) {
            try {
                return cteActivitiServices.selectTaskForBusinessKey(processInstanceID, environmentConfig.getActivitProcessKey());
            } catch (Exception ex) {
                String message = ex.getMessage();
                if (message.contains("Fehler beim REST-Service-Aufruf") && message.contains("Gateway Time-out")) {
                    callback.notifyClientJob(Level.INFO, "Fehler beim REST-Service-Aufruf (Gateway Time-out), versuche es nochmal...");
                    nCount++;
                } else if (message.contains("keine Nachfolge-Task")) {
                    return null;
                } else {
                    callback.notifyClientJob(Level.INFO, "Was für eine Exception? : " + ex.getMessage());
                    return null;
                }
            }
        }
        callback.notifyClientJob(Level.INFO, "Nach 3 Versuchen  wird der Prozess abgebrochen!");
        return null;
    }

    private void notifyAboutProcessImage() throws Exception {
        InputStream processImage = cteActivitiServices.getProcessImage(processInstanceID);
        if (processImage != null) {
            callback.notifyClientJob(Level.INFO, processImage);
        }
    }

    private UserTaskRunnable getUserTaskRunnableInstance(String taskDefinitionKey) throws Exception {
        String className = "de.creditreform.crefoteam.cte.tesun.activiti.handlers." + taskDefinitionKey;
        Class<UserTaskRunnable> userTaskRunnableClass = (Class<UserTaskRunnable>) Class.forName(className);
        Constructor<UserTaskRunnable> constructor = userTaskRunnableClass.getConstructor(EnvironmentConfig.class, TesunClientJobListener.class);
        return constructor.newInstance(environmentConfig, callback);
    }

    /**
     * Stops the running process. Safe to call even if no process is running.
     * running = false wird ZUERST gesetzt, damit runLoop beim nächsten Zyklus
     * sicher abbricht — unabhängig davon ob das Signal erfolgreich gesendet wird.
     */
    public void stop() {
        if (running) {
            running = false;
            try {
                cteActivitiServices.signalEventReceived(taskVariablesMap.get(TesunClientJobListener.UT_TASK_PARAM_NAME_ENVIRONMENT) + "cancelProcessSignal");
            } catch (Exception ex) {
                // Activiti schließt die Verbindung wenn der Prozess durch das Signal beendet wird
                // → ConnectionClosedException ist kein Fehler, running ist bereits false
                TimelineLogger.warn(ActivitiProcessController.class, "stop: signalEventReceived fehlgeschlagen (Prozess evtl. bereits beendet)", ex);
            }
        }
    }
}
