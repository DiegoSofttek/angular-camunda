# Timer Events (Timeouts, Delays, Schedules)

Timers can delay execution, set deadlines, or trigger recurring actions.

**Intermediate Timer (wait 5 minutes):**
```xml
<bpmn:intermediateCatchEvent id="Timer_Wait" name="Wait 5 min">
  <bpmn:timerEventDefinition>
    <bpmn:timeDuration xsi:type="bpmn:tFormalExpression">PT5M</bpmn:timeDuration>
  </bpmn:timerEventDefinition>
</bpmn:intermediateCatchEvent>
```

**Boundary Timer (timeout on a task):**
```xml
<bpmn:boundaryEvent id="Timer_Deadline" attachedToRef="Task_Review" cancelActivity="true">
  <bpmn:timerEventDefinition>
    <bpmn:timeDuration xsi:type="bpmn:tFormalExpression">P2D</bpmn:timeDuration>
  </bpmn:timerEventDefinition>
</bpmn:boundaryEvent>
```

**Timer Start Event (cron schedule):**
```xml
<bpmn:startEvent id="Timer_Start">
  <bpmn:timerEventDefinition>
    <bpmn:timeCycle xsi:type="bpmn:tFormalExpression">0 0 8 * * ?</bpmn:timeCycle>
  </bpmn:timerEventDefinition>
</bpmn:startEvent>
```

**ISO 8601 Duration format:** `PT30S` (30 seconds), `PT5M` (5 minutes), `PT1H` (1 hour), `P1D` (1 day), `P2D` (2 days).
