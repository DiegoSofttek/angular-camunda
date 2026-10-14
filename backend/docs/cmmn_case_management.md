# CMMN (Case Management)

CMMN is for unstructured, event-driven work where the order of tasks is not predefined.

**Camunda 7 supports CMMN 1.1.** Use `.cmmn` files and the Camunda Modeler to design case models.

```java
CaseService caseService = BpmPlatform.getDefaultProcessEngine().getCaseService();
CaseInstance caseInstance = caseService
    .createCaseInstanceByKey("my-case-definition")
    .setVariables(variables)
    .create();
```

Place `.cmmn` files in `src/main/resources/` alongside `.bpmn` files. They are auto-discovered by `processes.xml`.
