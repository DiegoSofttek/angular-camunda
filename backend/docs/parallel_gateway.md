# Parallel Gateway (Execute Branches Simultaneously)

All outgoing branches execute at the same time. The joining gateway waits for ALL branches.

```xml
<bpmn:parallelGateway id="Fork" name="Fork" />
<bpmn:parallelGateway id="Join" name="Join" />

<bpmn:sequenceFlow sourceRef="Fork" targetRef="Task_A" />
<bpmn:sequenceFlow sourceRef="Fork" targetRef="Task_B" />
<bpmn:sequenceFlow sourceRef="Task_A" targetRef="Join" />
<bpmn:sequenceFlow sourceRef="Task_B" targetRef="Join" />
```
