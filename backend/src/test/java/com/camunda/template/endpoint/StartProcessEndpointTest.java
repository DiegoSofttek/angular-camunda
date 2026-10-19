package com.camunda.template.endpoint;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void shouldStartApprovalProcessAndReturnStartedStatus() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 250);
        request.put("category", "STANDARD");

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(Mockito.eq("camunda-starter-process"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> variables = (Map<?, ?>) obj;
            return Integer.valueOf(250).equals(variables.get("amount"))
                    && "STANDARD".equals(variables.get("category"));
        }))).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-123");

        Response response = new StartProcessEndpoint().startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity).containsEntry("status", "STARTED");
    }

    @Test
    public void shouldReturnInternalServerErrorWhenProcessStartFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 999);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(Mockito.eq("camunda-starter-process"), Mockito.anyMap()))
                .thenThrow(new RuntimeException("mocked failure"));

        Response response = new StartProcessEndpoint().startProcess(request);

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertThat(entity).containsEntry("error", "mocked failure");
    }
}
