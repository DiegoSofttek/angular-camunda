package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
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
        org.mockito.Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldSetDefaultGreetingWhenNameIsNull() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn(null);

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void shouldSetDefaultGreetingWhenNameIsEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void shouldSetPersonalizedGreetingWhenNameIsPresent() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Diego");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, Diego!");
    }

    @Test
    public void shouldCreateStructuredOutputResponseFromApprovalStatus() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        new EvaluateDecisionDelegate().execute(execution);

        verify(execution).setVariable(org.mockito.Mockito.eq("outputResponse"), org.mockito.Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> map = (Map<?, ?>) obj;
            return Boolean.TRUE.equals(map.get("success"))
                    && "APPROVED".equals(map.get("approvalStatus"));
        }));
    }

    @Test
    public void shouldStartApprovalProcessAndReturnStartedStatus() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 500);
        request.put("category", "STANDARD");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey("camunda-starter-process", request)).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-123");

        bpmPlatformMock = mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new StartProcessEndpoint().startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("status")).isEqualTo("STARTED");
        verify(runtimeService).startProcessInstanceByKey("camunda-starter-process", request);
    }
}
