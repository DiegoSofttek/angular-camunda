package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
    public void helloWorldDelegateShouldUseDefaultNameWhenMissing() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn(null);

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void helloWorldDelegateShouldUseProvidedName() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Diego");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, Diego!");
    }

    @Test
    public void helloWorldDelegateShouldUseDefaultNameWhenEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void evaluateDecisionDelegateShouldCreateStructuredResponse() throws Exception {
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
    public void startProcessShouldStartCamundaProcessAndReturnStartedResponse() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 500);
        request.put("category", "STANDARD");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey("camunda-starter-process", request)).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-123");

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new StartProcessEndpoint().startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("status")).isEqualTo("STARTED");
        verify(runtimeService).startProcessInstanceByKey("camunda-starter-process", request);
    }

    @Test
    public void startProcessShouldReturnInternalServerErrorWhenEngineFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 10000);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey("camunda-starter-process", request))
                .thenThrow(new RuntimeException("boom"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new StartProcessEndpoint().startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity.get("error")).isEqualTo("boom");
    }
}
