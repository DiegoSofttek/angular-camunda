# Subprocesses (Embedded and Call Activities)

**Embedded Subprocess (inline within the same process):**
```xml
<bpmn:subProcess id="SubProcess_Validation" name="Validate Data">
  <bpmn:startEvent id="Sub_Start" />
  <bpmn:serviceTask id="Sub_Task1" name="Validate A" camunda:class="com.example.ValidateA" />
  <bpmn:serviceTask id="Sub_Task2" name="Validate B" camunda:class="com.example.ValidateB" />
  <bpmn:endEvent id="Sub_End" />
</bpmn:subProcess>
```

**Call Activity (call another BPMN process):**
```xml
<bpmn:callActivity id="Call_SubProcess" name="Execute Child Process"
                   calledElement="child-process-id"
                   camunda:calledElementBinding="latest">
  <bpmn:extensionElements>
    <camunda:in source="parentVar" target="childVar" />
    <camunda:out source="childResult" target="parentResult" />
  </bpmn:extensionElements>
</bpmn:callActivity>
```

`camunda:in` passes variables into the child. `camunda:out` passes results back.
