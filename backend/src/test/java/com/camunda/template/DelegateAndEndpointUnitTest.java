package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.camunda.template.delegate.EvaluateDecisionDelegate;
import com.camunda.template.delegate.HelloWorldDelegate;
import com.camunda.template.delegate.LoggerDelegate;
import com.camunda.template.endpoint.HelloWorldEndpoint;
import com.camunda.template.endpoint.StartProcessEndpoint;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Response;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.application.ProcessApplication;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class DelegateAndEndpointUnitTest {

    private MockedStatic<BpmPlatform> bpmPlatformMock;

    @After
    public void tearDown() {
        if (bpmPlatformMock != null) {
            bpmPlatformMock.close();
            bpmPlatformMock = null;
        }
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void helloWorldDelegateUsesDefaultNameWhenNull() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn(null);

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldDelegateUsesDefaultNameWhenEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldDelegateBuildsGreetingForProvidedName() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Diego");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, Diego!");
    }

    @Test
    public void evaluateDecisionDelegateStoresStructuredResponse() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        new EvaluateDecisionDelegate().execute(execution);

        verify(execution).setVariable(eq("outputResponse"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> response = (Map<?, ?>) obj;
            return Boolean.TRUE.equals(response.get("success"))
                    && "APPROVED".equals(response.get("approvalStatus"));
        }));
    }

    @Test
    public void loggerDelegateReadsExecutionMetadata() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(execution.getCurrentActivityId()).thenReturn("activity-1");
        when(execution.getCurrentActivityName()).thenReturn("My Activity");
        when(execution.getVariables()).thenReturn(new HashMap<>());

        new LoggerDelegate().execute(execution);

        verify(execution).getProcessInstanceId();
        verify(execution).getCurrentActivityId();
        verify(execution).getCurrentActivityName();
        verify(execution).getVariables();
    }

    @Test
    public void startProcessEndpointStartsApprovalProcessAndReturnsStarted() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 500);
        request.put("category", "STANDARD");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), anyMap())).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-id");
        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new StartProcessEndpoint().startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("status")).isEqualTo("STARTED");
        verify(runtimeService).startProcessInstanceByKey(eq("camunda-starter-process"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            return request.equals(obj);
        }));
    }

    @Test
    public void helloWorldEndpointUsesDefaultNameAndReturnsGreeting() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder builder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);
        Map<String, Object> returnedVariables = new HashMap<>();
        returnedVariables.put("greeting", "Hello, World!");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> variables = (Map<?, ?>) obj;
            return "World".equals(variables.get("name"));
        }))).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(result);
        when(result.getVariables()).thenReturn(returnedVariables);
        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("greeting")).isEqualTo("Hello, World!");
    }

    @Test
    public void helloWorldEndpointReturnsInternalServerErrorWhenProcessFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenThrow(new RuntimeException("boom"));
        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("error")).isEqualTo("boom");
    }

    @Test
    public void restApplicationHasApiApplicationPath() {
        ApplicationPath annotation = com.camunda.template.endpoint.RestApplication.class.getAnnotation(ApplicationPath.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("/api");
    }

    @Test
    public void camundaStarterApplicationHasExpectedProcessApplicationAnnotation() {
        ProcessApplication annotation = com.camunda.template.CamundaStarterApplication.class.getAnnotation(ProcessApplication.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("camunda-starter-template");
    }
}
