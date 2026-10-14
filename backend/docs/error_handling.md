# Error Handling (Boundary Events, Compensation)

**Boundary Error Event (catch errors from service tasks):**
```xml
<bpmn:boundaryEvent id="Error_Boundary" attachedToRef="Task_CallService">
  <bpmn:errorEventDefinition errorRef="Error_ServiceFailed"
      camunda:errorCodeVariable="errorCode"
      camunda:errorMessageVariable="errorMessage" />
</bpmn:boundaryEvent>
```

**Throwing an error from a JavaDelegate:**
```java
@Override
public void execute(DelegateExecution execution) throws Exception {
    try {
        callExternalService();
    } catch (Exception e) {
        throw new BpmnError("SERVICE_FAILED", e.getMessage());
    }
}
```

**Error Reference definition:**
```xml
<bpmn:error id="Error_ServiceFailed" name="Service Failed" errorCode="SERVICE_FAILED" />
```
