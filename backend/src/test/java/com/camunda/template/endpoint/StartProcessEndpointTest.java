package com.camunda.template.endpoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartProcessEndpointTest {

    @After
    public void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldStartProcessAndReturnStartedStatus() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(Mockito.eq("camunda-starter-process"), Mockito.anyMap()))
                .thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-123");

        StartProcessEndpoint endpoint = new TestableStartProcessEndpoint(processEngine);
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 500);
        request.put("category", "STANDARD");

        Response response = endpoint.startProcess(request);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getEntity()).get("status")).isEqualTo("STARTED");
        verify(runtimeService).startProcessInstanceByKey(Mockito.eq("camunda-starter-process"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> map = (Map<?, ?>) obj;
            return Integer.valueOf(500).equals(map.get("amount")) && "STANDARD".equals(map.get("category"));
        }));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenStartFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        when(processEngine.getRuntimeService()).thenThrow(new RuntimeException("cannot connect"));

        StartProcessEndpoint endpoint = new TestableStartProcessEndpoint(processEngine);

        Response response = endpoint.startProcess(new HashMap<String, Object>());

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getEntity()).get("error")).isEqualTo("cannot connect");
    }

    private static class TestableStartProcessEndpoint extends StartProcessEndpoint {
        private final ProcessEngine processEngine;

        private TestableStartProcessEndpoint(ProcessEngine processEngine) {
            this.processEngine = processEngine;
        }

        @Override
        protected ProcessEngine getProcessEngine() {
            return processEngine;
        }
    }
}
