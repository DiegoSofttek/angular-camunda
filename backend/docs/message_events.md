# Message Events (Process-to-Process Communication)

Messages allow processes to communicate or wait for external signals.

**Message Start Event (start a process by message):**
```xml
<bpmn:startEvent id="Msg_Start">
  <bpmn:messageEventDefinition messageRef="Message_OrderReceived" />
</bpmn:startEvent>
```

**Intermediate Message Catch (wait for message):**
```xml
<bpmn:intermediateCatchEvent id="Msg_Wait" name="Wait for Payment">
  <bpmn:messageEventDefinition messageRef="Message_PaymentConfirmed" />
</bpmn:intermediateCatchEvent>
```

**Sending a message from code:**
```java
RuntimeService runtimeService = BpmPlatform.getDefaultProcessEngine().getRuntimeService();
runtimeService.correlateMessage("Message_PaymentConfirmed",
    "businessKey123",
    variables);
```
