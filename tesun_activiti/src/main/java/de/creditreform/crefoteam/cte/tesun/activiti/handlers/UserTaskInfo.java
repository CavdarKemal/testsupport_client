package de.creditreform.crefoteam.cte.tesun.activiti.handlers;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class UserTaskInfo {
    private String taskId;
    private String groupID;
    private String taskDefinitionKey;
    private String taskOwner;
    private String taskAssignee;
    private String taskName;
    private String taskExecutionId;
    private String taskProcessInstanceId;
    private String taskVariables;

    public UserTaskInfo() {

    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getTaskOwner() {
        return taskOwner;
    }

    public void setTaskOwner(String taskOwner) {
        this.taskOwner = taskOwner;
    }

    public String getTaskAssignee() {
        return taskAssignee;
    }

    public void setTaskAssignee(String taskAssignee) {
        this.taskAssignee = taskAssignee;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskExecutionId() {
        return taskExecutionId;
    }

    public void setTaskExecutionId(String taskExecutionId) {
        this.taskExecutionId = taskExecutionId;
    }

    public String getTaskProcessInstanceId() {
        return taskProcessInstanceId;
    }

    public void setTaskProcessInstanceId(String taskProcessInstanceId) {
        this.taskProcessInstanceId = taskProcessInstanceId;
    }

    public String getTaskVariables() {
        return taskVariables;
    }

    public void setTaskVariables(String taskVariables) {
        this.taskVariables = taskVariables;
    }

    public void initRequestNodeParams(ObjectNode requestNode) {
        if (getTaskId() != null) {
            requestNode.put("id", getTaskId());
        }
        if (getTaskOwner() != null) {
            requestNode.put("owner", getTaskOwner());
        }
        if (getTaskAssignee() != null) {
            requestNode.put("assignee", getTaskAssignee());
        }
        if (getTaskName() != null) {
            requestNode.put("name", getTaskName());
        }
        if (getTaskExecutionId() != null) {
            requestNode.put("taskExecutionId", getTaskExecutionId());
        }
        if (getTaskProcessInstanceId() != null) {
            requestNode.put("taskProcessInstanceId", getTaskProcessInstanceId());
        }
        if (getTaskVariables() != null) {
            requestNode.put("taskVariables", getTaskVariables());
        }
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

}
