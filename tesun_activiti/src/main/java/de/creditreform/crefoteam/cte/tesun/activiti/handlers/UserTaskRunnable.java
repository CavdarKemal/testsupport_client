package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import java.util.Map;

public interface UserTaskRunnable {
    Map<String, Object> runTask(Map<String, Object> taskVariablesMap) throws Exception;

    void cancel();
}
