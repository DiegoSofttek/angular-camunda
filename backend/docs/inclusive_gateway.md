# Inclusive Gateway (Multiple Paths Based on Conditions)

Unlike XOR (one path), Inclusive activates ALL paths whose conditions are true.

```xml
<bpmn:inclusiveGateway id="IncGw_Split" />

<bpmn:sequenceFlow sourceRef="IncGw_Split" targetRef="Task_Email">
  <bpmn:conditionExpression>${sendEmail == true}</bpmn:conditionExpression>
</bpmn:sequenceFlow>
<bpmn:sequenceFlow sourceRef="IncGw_Split" targetRef="Task_SMS">
  <bpmn:conditionExpression>${sendSMS == true}</bpmn:conditionExpression>
</bpmn:sequenceFlow>

<bpmn:inclusiveGateway id="IncGw_Join" />
```
