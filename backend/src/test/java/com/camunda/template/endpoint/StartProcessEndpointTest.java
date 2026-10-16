package com.camunda.template.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
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
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class StartProcessEndpointTest {

    private final StartProcessEndpoint endpoint = new StartProcessEndpoint();

    @Test
    public void shouldStartApprovalProcessAndReturnStartedStatus() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), anyMap())).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-123");

        Map<String, Object> request = new HashMap<>();
        request.put("amount", 500);
        request.put("category", "STANDARD");

        try (MockedStatic<BpmPlatform> bpmPlatformMock = mockStatic(BpmPlatform.class)) {
            bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

            Response response = endpoint.startProcess(request);

            ArgumentCaptor<Map> variablesCaptor = ArgumentCaptor.forClass(Map.class);
            verify(runtimeService).startProcessInstanceByKey(eq("camunda-starter-process"), variablesCaptor.capture());

            assertThat(variablesCaptor.getValue()).containsAllEntriesOf(request);
            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat((Map<String, Object>) response.getEntity()).containsEntry("status", "STARTED");
        }
    }

    @Test
    public void shouldReturnInternalServerErrorWhenProcessStartFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), anyMap()))
                .thenThrow(new RuntimeException("engine down"));

        try (MockedStatic<BpmPlatform> bpmPlatformMock = mockStatic(BpmPlatform.class)) {
            bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

            Response response = endpoint.startProcess(new HashMap<>());

            assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            assertThat((Map<String, String>) response.getEntity()).containsEntry("error", "engine down");
        }
    }
}
