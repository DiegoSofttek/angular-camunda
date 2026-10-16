package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.camunda.template.delegate.EvaluateDecisionDelegate;
import com.camunda.template.delegate.HelloWorldDelegate;
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
    public void helloWorldDelegateShouldUseWorldWhenNameIsNull() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn(null);

        new HelloWorldDelegate().execute(execution);

        Mockito.verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldDelegateShouldUseWorldWhenNameIsEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        new HelloWorldDelegate().execute(execution);

        Mockito.verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldDelegateShouldCreatePersonalizedGreeting() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Diego");

        new HelloWorldDelegate().execute(execution);

        Mockito.verify(execution).setVariable("greeting", "Hello, Diego!");
    }

    @Test
    public void evaluateDecisionDelegateShouldStoreStructuredResponse() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        new EvaluateDecisionDelegate().execute(execution);

        Mockito.verify(execution).setVariable(eq("outputResponse"), Mockito.argThat(argument ->
                argument instanceof Map
                        && Boolean.TRUE.equals(((Map<?, ?>) argument).get("success"))
                        && "APPROVED".equals(((Map<?, ?>) argument).get("approvalStatus"))));
    }

    @Test
    public void startProcessShouldReturnStartedStatus() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), anyMap())).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-123");

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        StartProcessEndpoint endpoint = new StartProcessEndpoint();
        Map<String, Object> request = java.util.Collections.<String, Object>unmodifiableMap(
                new java.util.HashMap<String, Object>() {{
                    put("amount", 500);
                    put("category", "STANDARD");
                }});

        Response response = endpoint.startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("status", "STARTED");
        Mockito.verify(runtimeService).startProcessInstanceByKey(eq("camunda-starter-process"), Mockito.argThat(argument ->
                argument instanceof Map
                        && Integer.valueOf(500).equals(((Map<?, ?>) argument).get("amount"))
                        && "STANDARD".equals(((Map<?, ?>) argument).get("category"))));
    }
}
