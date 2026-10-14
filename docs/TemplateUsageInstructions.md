# Angular + Camunda 7 Starter Template

Monorepo: **`frontend/`** (Angular 21) + **`backend/`** (Camunda 7 WAR for WildFly). 

## What's Already Implemented

### Backend (`backend/`) — Camunda 7

This part is a **functional template** for Camunda 7 BPMN on JBoss/WildFly. Prefer extending these pieces before introducing parallel patterns.

#### Core Infrastructure
- **Camunda 7.18.0** – BPMN process engine (provided by JBoss as a shared module)
- **Java 11** – Source and target compilation level
- **Java EE 8** – EJB, CDI, JAX-RS APIs (provided by JBoss)
- **Maven WAR Packaging** – Generates `camunda-starter-template.war` ready to deploy
- **JBoss/WildFly Deployment** – Pre-configured `jboss-deployment-structure.xml` with Camunda module imports
- **Process Application** – EJB Singleton `@ProcessApplication` class with auto-scan enabled via `processes.xml`
- **JAX-RS REST API** – Configured at `@ApplicationPath("/api")` for exposing endpoints

#### BPMN Process Engine
- **Process Registration** – `processes.xml` with `isScanForProcessDefinitions=true` auto-discovers `.bpmn` files
- **JavaDelegate Pattern** – Extend `JavaDelegate`, implement `execute(DelegateExecution)` to add business logic
- **BpmPlatform API** – Use `BpmPlatform.getDefaultProcessEngine()` to access engine services (RuntimeService, HistoryService, etc.)
- **Process Variables** – Pass variables via `executeWithVariablesInReturn()` to get results from completed processes
- **Synchronous Execution** – Processes execute synchronously within the REST request lifecycle

#### DMN Decision Engine
- **Decision Tables** – DMN 1.3 format with hit policy support (FIRST, UNIQUE, COLLECT, etc.)
- **Business Rule Tasks** – BPMN tasks that evaluate DMN decisions using `camunda:decisionRef`
- **Result Mapping** – `camunda:mapDecisionResult="singleEntry"` maps a single output to a process variable
- **Input Expressions** – DMN inputs read directly from process variables using JUEL expressions (`${variableName}`)

#### REST Endpoints
- **GET `/api/hello?name={name}`** – Triggers `hello-world-process`, returns greeting from HelloWorldDelegate
- **POST `/api/approve`** – Triggers `camunda-starter-process` with DMN evaluation, expects JSON body with `amount` and `category`

#### Testing
- **JUnit 4 + ProcessEngineRule** – In-memory H2 database with full Camunda engine for unit tests
- **camunda-bpm-assert** – Fluent assertions for process instance state verification

### Frontend (`frontend/`) — Angular 21

**Purpose:** Thin UI that calls the Camunda REST API exposed by the WAR. **Business rules stay in the backend / BPMN**; the front only orchestrates HTTP and presentation.

**Already in place:**

- **Angular 21** with **standalone** components, **`signal()`** state, **`ChangeDetectionStrategy.OnPush`** on the root component.
- **PrimeNG 21** + **Aura** theme (`providePrimeNG`) + **Tailwind CSS 4** (global styles in `src/styles.css`).
- **`provideHttpClient()`** — app-wide HTTP; no custom interceptors yet.
- **Router** is registered (`provideRouter`) but **`app.routes.ts` is empty** — there is **no feature routing** yet; everything demo lives in **`App`** (`app.ts` / `app.html`).
- **Dev proxy** — `proxy.conf.json` proxies `/camunda-starter-template` → `http://localhost:8080` when using `ng serve` (avoids CORS during local development).
- **`HelloApiService`** — single API wrapper: **`GET /camunda-starter-template/api/hello`** with optional `name` query; base path is **relative** (same origin + proxy in dev).
- **Demo screen** — name input, “Greet” button, displays greeting or error; documents the **integration pattern** with the backend.
- **Unit tests** — **Vitest** via `ng test`; `App` tests use `HttpClientTesting` where needed.

---

## Project Structure

### Backend (`backend/`)

```
backend/
├── pom.xml                                          <- Maven build config, Camunda 7.18.0 BOM (artifact: camunda-starter-template)
└─── src/
   ├── main/
   │   ├── java/com/camunda/template/
   │   │   ├── CamundaStarterApplication.java       <- EJB @ProcessApplication (entry point)
   │   │   ├── delegate/
   │   │   │   ├── HelloWorldDelegate.java          <- Simple delegate: generates greeting
   │   │   │   ├── LoggerDelegate.java              <- Logs process variables
   │   │   │   └── EvaluateDecisionDelegate.java    <- Reads DMN result, builds response
   │   │   └── endpoint/
   │   │       ├── RestApplication.java             <- JAX-RS @ApplicationPath("/api")
   │   │       ├── HelloWorldEndpoint.java          <- GET /api/hello
   │   │       └── StartProcessEndpoint.java        <- POST /api/approve
   │   ├── resources/
   │   │   ├── hello-world-process.bpmn             <- Simple: Start -> Delegate -> End
   │   │   ├── camunda-starter-process.bpmn         <- DMN + Gateway: Start -> Log -> DMN -> Response -> Gateway -> End
   │   │   ├── approval-decision.dmn                <- Decision table: amount + category -> status
   │   │   └── META-INF/processes.xml               <- Process archive registration
   │   └── webapp/WEB-INF/
   │       ├── beans.xml                            <- CDI descriptor
   │       └── jboss-deployment-structure.xml        <- Camunda JBoss module imports
   └── test/
       ├── java/com/camunda/template/
       │   └── CamundaStarterProcessTest.java       <- 4 unit tests
       └── resources/
           └── camunda.cfg.xml                      <- In-memory H2 process engine for tests
```

---

## How to Add a New Process

Paths are relative to **`backend/`**.

1. **Create a `.bpmn` file** in `src/main/resources/` (use Camunda Modeler desktop app)
2. **Create JavaDelegate classes** in `src/main/java/com/camunda/template/delegate/` implementing `JavaDelegate`
3. **Reference delegates** in BPMN service tasks via `camunda:class="com.camunda.template.delegate.YourDelegate"`
4. **Create a REST endpoint** in `src/main/java/com/camunda/template/endpoint/` using JAX-RS annotations
5. **Start the process** from the endpoint using:
   ```java
   ProcessEngine engine = BpmPlatform.getDefaultProcessEngine();
   ProcessInstanceWithVariables result = engine.getRuntimeService()
       .createProcessInstanceByKey("your-process-id")
       .setVariables(variables)
       .executeWithVariablesInReturn();
   ```
6. **Add unit tests** using `@Deployment(resources = {"your-process.bpmn"})` and `ProcessEngineRule`

---

## How to Add a New DMN Decision

Paths are relative to **`backend/`**.

1. **Create a `.dmn` file** in `src/main/resources/` (use Camunda Modeler)
2. **Add a Business Rule Task** in your BPMN with:
   - `camunda:decisionRef="your-decision-id"`
   - `camunda:resultVariable="outputVar"`
   - `camunda:mapDecisionResult="singleEntry"` (or `resultList`, `singleResult`, `collectEntries`)
3. **DMN inputs** read from process variables automatically via JUEL `${variableName}`
4. **Deploy together** – both `.bpmn` and `.dmn` must be in `src/main/resources/`

---

## Key Patterns and Conventions

| Pattern | Implementation |
|---|---|
| Get engine services | `BpmPlatform.getDefaultProcessEngine().getRuntimeService()` |
| Execute sync process | `runtimeService.createProcessInstanceByKey("id").setVariables(vars).executeWithVariablesInReturn()` |
| Read result variables | `result.getVariables().get("varName")` |
| Delegate class | Implement `JavaDelegate`, override `execute(DelegateExecution)` |
| Read variable in delegate | `execution.getVariable("name")` |
| Write variable in delegate | `execution.setVariable("name", value)` |
| REST endpoint | JAX-RS `@Path`, `@GET`/`@POST`, `@Produces(APPLICATION_JSON)` |
| BPMN service task | `camunda:class="fully.qualified.ClassName"` |
| DMN in BPMN | Business Rule Task with `camunda:decisionRef` |
| XOR Gateway condition | `${variable == 'VALUE'}` |


---

## Advanced Camunda 7 Concepts Reference

This section documents Camunda 7 capabilities **not yet implemented** in this template but available for use. Detailed guides live under `backend/docs/` so this file stays a short overview.

| Topic | File | When to read |
|-------|------|----------------|
| **User Tasks (Human Tasks with Forms)** | [user_tasks.md](../backend/docs/user_tasks.md) | You need human steps, Tasklist, assignees, or completing tasks from code. |
| **Timer Events** | [timer_events.md](../backend/docs/timer_events.md) | Delays, deadlines, scheduled starts, or boundary timeouts. |
| **Message Events** | [message_events.md](../backend/docs/message_events.md) | Correlating messages, start-by-message, or waiting for external signals. |
| **Signal Events** | [signal_events.md](../backend/docs/signal_events.md) | Broadcasting to many process instances at once. |
| **Error Handling** | [error_handling.md](../backend/docs/error_handling.md) | Boundary error events, `BpmnError` from delegates, error definitions. |
| **Subprocesses** | [subprocesses.md](../backend/docs/subprocesses.md) | Embedded subprocesses or call activities to another process definition. |
| **Parallel Gateway** | [parallel_gateway.md](../backend/docs/parallel_gateway.md) | Fork/join: run multiple branches in parallel. |
| **Inclusive Gateway** | [inclusive_gateway.md](../backend/docs/inclusive_gateway.md) | Activate every branch whose condition is true (not only one). |
| **Multi-Instance** | [multi_instance.md](../backend/docs/multi_instance.md) | Loop over a collection (sequential or parallel). |
| **External Tasks** | [external_tasks.md](../backend/docs/external_tasks.md) | Workers that poll the engine (separate from in-container delegates). |
| **HTTP/SOAP Connectors** | [http_soap_connectors.md](../backend/docs/http_soap_connectors.md) | Call REST/SOAP from BPMN via connector extension elements. |
| **XSLT Transforms** | [xslt_transforms.md](../backend/docs/xslt_transforms.md) | SOAP/XML transformation with XSLT in the process. |
| **Spin / complex variables** | [spin_complex_variables.md](../backend/docs/spin_complex_variables.md) | JSON/XML in variables and Spin expressions in BPMN. |
| **Execution & Task Listeners** | [execution_and_task_listeners.md](../backend/docs/execution_and_task_listeners.md) | Hooks on activity lifecycle (start/end, task create/complete). |
| **CMMN** | [cmmn_case_management.md](../backend/docs/cmmn_case_management.md) | Case-style, unstructured work (`.cmmn` models). |
| **Versioning & migration** | [process_versioning_and_migration.md](../backend/docs/process_versioning_and_migration.md) | New BPMN versions and migrating running instances. |
