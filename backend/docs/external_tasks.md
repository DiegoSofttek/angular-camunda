# External Tasks (Async Workers)

External Tasks decouple the worker from the engine. A worker polls for tasks, executes, and completes them.

**BPMN definition:**
```xml
<bpmn:serviceTask id="Task_External" name="Process Payment"
                  camunda:type="external"
                  camunda:topic="payment-processing" />
```

**Java worker (separate application):**
```java
ExternalTaskClient client = ExternalTaskClient.create()
    .baseUrl("http://localhost:8080/engine-rest")
    .build();

client.subscribe("payment-processing")
    .handler((externalTask, externalTaskService) -> {
        String orderId = externalTask.getVariable("orderId");
        // process payment...
        externalTaskService.complete(externalTask);
    })
    .open();
```
