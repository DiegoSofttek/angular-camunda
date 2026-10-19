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
import javax.ws.rs.core.Response;
import org.camunda.bpm.BpmPlatform;
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
    public void helloWorldDelegateUsesDefaultNameWhenInputIsNull() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn(null);

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldDelegateUsesDefaultNameWhenInputIsEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldDelegateCreatesPersonalizedGreetingWhenNameExists() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Diego");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, Diego!");
    }

    @Test
    public void evaluateDecisionDelegateBuildsStructuredResponse() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        new EvaluateDecisionDelegate().execute(execution);

        verify(execution).setVariable(eq("outputResponse"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> map = (Map<?, ?>) obj;
            return Boolean.TRUE.equals(map.get("success"))
                    && "APPROVED".equals(map.get("approvalStatus"));
        }));
    }

    @Test
    public void loggerDelegateReadsExecutionMetadataWithoutMutatingState() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        Map<String, Object> variables = new HashMap<>();
        variables.put("sample", "value");
        when(execution.getProcessInstanceId()).thenReturn("process-123");
        when(execution.getCurrentActivityId()).thenReturn("activity-456");
        when(execution.getCurrentActivityName()).thenReturn("Log Task");
        when(execution.getVariables()).thenReturn(variables);

        new LoggerDelegate().execute(execution);

        verify(execution).getProcessInstanceId();
        verify(execution).getCurrentActivityId();
        verify(execution).getCurrentActivityName();
        verify(execution).getVariables();
    }

    @Test
    public void startProcessStartsApprovalProcessAndReturnsStartedStatus() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 1000);
        request.put("category", "STANDARD");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), anyMap()))
                .thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("instance-1");

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
            Map<?, ?> map = (Map<?, ?>) obj;
            return Integer.valueOf(1000).equals(map.get("amount"))
                    && "STANDARD".equals(map.get("category"));
        }));
    }

    @Test
    public void startProcessReturnsServerErrorWhenRuntimeServiceFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 1000);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), anyMap()))
                .thenThrow(new RuntimeException("boom"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new StartProcessEndpoint().startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("error")).isEqualTo("boom");
    }

    @Test
    public void helloWorldEndpointStartsHelloProcessAndReturnsGreeting() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder processBuilder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);
        Map<String, Object> resultVariables = new HashMap<>();
        resultVariables.put("greeting", "Hello, Diego!");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process"))
                .thenReturn(processBuilder);
        when(processBuilder.setVariables(anyMap())).thenReturn(processBuilder);
        when(processBuilder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(resultVariables);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("greeting")).isEqualTo("Hello, Diego!");
        verify(processBuilder).setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            return "Diego".equals(((Map<?, ?>) obj).get("name"));
        }));
    }

    @Test
    public void helloWorldEndpointUsesWorldWhenQueryParameterIsMissing() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder processBuilder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);
        Map<String, Object> resultVariables = new HashMap<>();
        resultVariables.put("greeting", "Hello, World!");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process"))
                .thenReturn(processBuilder);
        when(processBuilder.setVariables(anyMap())).thenReturn(processBuilder);
        when(processBuilder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(resultVariables);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("greeting")).isEqualTo("Hello, World!");
        verify(processBuilder).setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            return "World".equals(((Map<?, ?>) obj).get("name"));
        }));
    }

    @Test
    public void helloWorldEndpointReturnsServerErrorWhenProcessFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process"))
                .thenThrow(new RuntimeException("hello-failed"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("error")).isEqualTo("hello-failed");
    }
}
