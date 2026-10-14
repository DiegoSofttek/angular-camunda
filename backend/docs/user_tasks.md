# User Tasks (Human Tasks with Forms)

User Tasks pause the process until a human completes them. They appear in Camunda Tasklist.

**BPMN:**
```xml
<bpmn:userTask id="Task_Review" name="Review Request"
               camunda:assignee="${assignee}"
               camunda:candidateGroups="managers">
  <bpmn:incoming>Flow_1</bpmn:incoming>
  <bpmn:outgoing>Flow_2</bpmn:outgoing>
</bpmn:userTask>
```

**Completing a task from code:**
```java
TaskService taskService = BpmPlatform.getDefaultProcessEngine().getTaskService();
Task task = taskService.createTaskQuery()
    .processInstanceId(processInstanceId)
    .singleResult();
taskService.complete(task.getId(), variables);
```

**Key attributes:** `camunda:assignee` (specific user), `camunda:candidateGroups` (group of users), `camunda:candidateUsers` (list of users), `camunda:dueDate`, `camunda:priority`.
