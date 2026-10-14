# Execution and Task Listeners

Listeners execute code at specific lifecycle events.

**Execution Listener (on process flow):**
```xml
<bpmn:serviceTask id="Task_1" name="My Task">
  <bpmn:extensionElements>
    <camunda:executionListener event="start" class="com.example.OnTaskStartListener" />
    <camunda:executionListener event="end" class="com.example.OnTaskEndListener" />
  </bpmn:extensionElements>
</bpmn:serviceTask>
```

**Task Listener (on user tasks only):**
```xml
<bpmn:userTask id="Task_Review">
  <bpmn:extensionElements>
    <camunda:taskListener event="create" class="com.example.OnTaskCreatedListener" />
    <camunda:taskListener event="complete" class="com.example.OnTaskCompletedListener" />
  </bpmn:extensionElements>
</bpmn:userTask>
```

**Listener events:** `start`, `end` (execution); `create`, `assignment`, `complete`, `delete`, `update`, `timeout` (task).
