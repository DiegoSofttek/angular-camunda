# Process Versioning and Instance Migration

When you deploy a new version of a `.bpmn`, Camunda creates a new version. Existing running instances stay on the old version.

**Migrate running instances to a new version:**
```java
RuntimeService runtimeService = BpmPlatform.getDefaultProcessEngine().getRuntimeService();

MigrationPlan migrationPlan = runtimeService.createMigrationPlan(
    "oldDefinitionId", "newDefinitionId")
    .mapEqualActivities()
    .build();

runtimeService.newMigration(migrationPlan)
    .processInstanceIds(instanceIds)
    .execute();
```

**Query process definition versions:**
```java
RepositoryService repositoryService = BpmPlatform.getDefaultProcessEngine().getRepositoryService();
List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
    .processDefinitionKey("my-process")
    .orderByProcessDefinitionVersion().desc()
    .list();
```
