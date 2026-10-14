# Multi-Instance (Loop Over Collections)

Execute a task or subprocess once per item in a collection.

**Sequential multi-instance:**
```xml
<bpmn:serviceTask id="Task_Process" name="Process Item"
                  camunda:class="com.example.ProcessItemDelegate">
  <bpmn:multiInstanceLoopCharacteristics isSequential="true"
      camunda:collection="${items}" camunda:elementVariable="item" />
</bpmn:serviceTask>
```

**Parallel multi-instance (all at once):**
```xml
<bpmn:multiInstanceLoopCharacteristics isSequential="false"
    camunda:collection="${items}" camunda:elementVariable="item" />
```

**Accessing current item in delegate:**
```java
String item = (String) execution.getVariable("item");
```
