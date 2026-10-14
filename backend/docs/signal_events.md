# Signal Events (Broadcast to Multiple Processes)

Signals broadcast to ALL listening processes (unlike messages which target one).

**Signal Catch Event:**
```xml
<bpmn:intermediateCatchEvent id="Signal_Wait">
  <bpmn:signalEventDefinition signalRef="Signal_SystemShutdown" />
</bpmn:intermediateCatchEvent>
```

**Broadcasting a signal from code:**
```java
RuntimeService runtimeService = BpmPlatform.getDefaultProcessEngine().getRuntimeService();
runtimeService.signalEventReceived("Signal_SystemShutdown");
```
