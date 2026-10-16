package com.camunda.template.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class StartProcessEndpointTest {

    private MockedStatic<BpmPlatform> bpmPlatformMock;

    @After
    public void tearDown() {
        if (bpmPlatformMock != null) {
            bpmPlatformMock.close();
        }
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void startProcessReturnsStartedStatusWhenProcessLaunchSucceeds() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 500);
        request.put("category", "STANDARD");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), eq(request))).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-123");

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        StartProcessEndpoint endpoint = new StartProcessEndpoint();

        Response response = endpoint.startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Map.of("status", "STARTED"));
    }

    @Test
    public void startProcessReturnsInternalServerErrorWhenEngineFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        Map<String, Object> request = Map.of("amount", 10000, "category", "PREMIUM");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), eq(request)))
                .thenThrow(new RuntimeException("Start failed"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        StartProcessEndpoint endpoint = new StartProcessEndpoint();

        Response response = endpoint.startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Map.of("error", "Start failed"));
    }
}
