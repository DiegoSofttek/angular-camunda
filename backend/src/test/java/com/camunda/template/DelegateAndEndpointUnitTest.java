package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.camunda.template.delegate.EvaluateDecisionDelegate;
import com.camunda.template.delegate.HelloWorldDelegate;
import com.camunda.template.delegate.LoggerDelegate;
import com.camunda.template.endpoint.HelloWorldEndpoint;
import com.camunda.template.endpoint.RestApplication;
import com.camunda.template.endpoint.StartProcessEndpoint;

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
    public void helloWorldDelegateUsesDefaultNameWhenMissing() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn(null);

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldDelegateUsesProvidedName() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Diego");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, Diego!");
    }

    @Test
    public void helloWorldDelegateUsesDefaultNameWhenEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void evaluateDecisionDelegateCreatesStructuredResponse() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        new EvaluateDecisionDelegate().execute(execution);

        verify(execution).setVariable(Mockito.eq("outputResponse"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> response = (Map<?, ?>) obj;
            return Boolean.TRUE.equals(response.get("success"))
                    && "APPROVED".equals(response.get("approvalStatus"));
        }));
    }

    @Test
    public void loggerDelegateReadsExecutionDetails() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(execution.getCurrentActivityId()).thenReturn("activity-1");
        when(execution.getCurrentActivityName()).thenReturn("Logger Task");
        when(execution.getVariables()).thenReturn(Collections.singletonMap("key", "value"));

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

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(Mockito.eq("camunda-starter-process"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> variables = (Map<?, ?>) obj;
            return Integer.valueOf(500).equals(variables.get("amount"))
                    && "STANDARD".equals(variables.get("category"));
        }))).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("pid-123");

        Response response = new StartProcessEndpoint().startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("status", "STARTED");
        verify(runtimeService).startProcessInstanceByKey(Mockito.eq("camunda-starter-process"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> variables = (Map<?, ?>) obj;
            return Integer.valueOf(500).equals(variables.get("amount"))
                    && "STANDARD".equals(variables.get("category"));
        }));
    }

    @Test
    public void helloWorldEndpointReturnsGreetingWhenProcessSucceeds() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder builder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> variables = (Map<?, ?>) obj;
            return "Diego".equals(variables.get("name"));
        }))).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(result);
        when(result.getVariables()).thenReturn(Collections.singletonMap("greeting", "Hello, Diego!"));

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("greeting", "Hello, Diego!");
    }

    @Test
    public void helloWorldEndpointReturnsDefaultGreetingInputWhenNameIsNull() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder builder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
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
        when(result.getVariables()).thenReturn(Collections.singletonMap("greeting", "Hello, World!"));

        Response response = new HelloWorldEndpoint().sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldEndpointReturnsInternalServerErrorWhenProcessFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process"))
                .thenThrow(new RuntimeException("boom"));

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("error", "boom");
    }

    @Test
    public void restApplicationHasApiApplicationPath() {
        ApplicationPath applicationPath = RestApplication.class.getAnnotation(ApplicationPath.class);

        assertThat(applicationPath).isNotNull();
        assertThat(applicationPath.value()).isEqualTo("/api");
    }

    @Test
    public void camundaStarterApplicationHasExpectedProcessApplicationAnnotation() {
        ProcessApplication processApplication = CamundaStarterApplication.class.getAnnotation(ProcessApplication.class);

        assertThat(processApplication).isNotNull();
        assertThat(processApplication.value()).isEqualTo("camunda-starter-template");
        assertThat(CamundaStarterApplication.class.getSuperclass().getName())
                .isEqualTo("org.camunda.bpm.application.impl.EjbProcessApplication");
    }
}
