package com.camunda.template.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class HelloWorldEndpointTest {

    @Test
    public void shouldUseDefaultWorldWhenNameIsNull() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder builder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(anyMap())).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(Map.of("greeting", "Hello, World!"));

        HelloWorldEndpoint endpoint = new TestHelloWorldEndpoint(processEngine);

        Response response = endpoint.sayHello(null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Object entity = response.getEntity();
        assertTrue(entity instanceof Map);
        assertEquals("Hello, World!", ((Map<?, ?>) entity).get("greeting"));

        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(builder).setVariables(variablesCaptor.capture());
        assertEquals("World", variablesCaptor.getValue().get("name"));
    }

    @Test
    public void shouldReturnOkWhenProcessExecutesSuccessfully() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder builder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(anyMap())).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(Map.of("greeting", "Hello, Camunda!"));

        HelloWorldEndpoint endpoint = new TestHelloWorldEndpoint(processEngine);

        Response response = endpoint.sayHello("Camunda");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Object entity = response.getEntity();
        assertNotNull(entity);
        assertTrue(entity instanceof Map);
        assertEquals("Hello, Camunda!", ((Map<?, ?>) entity).get("greeting"));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenProcessExecutionFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        doThrow(new RuntimeException("boom"))
                .when(runtimeService)
                .createProcessInstanceByKey("hello-world-process");

        HelloWorldEndpoint endpoint = new TestHelloWorldEndpoint(processEngine);

        Response response = endpoint.sayHello("Camunda");

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        Object entity = response.getEntity();
        assertTrue(entity instanceof Map);
        assertEquals("boom", ((Map<?, ?>) entity).get("error"));
    }

    private static class TestHelloWorldEndpoint extends HelloWorldEndpoint {
        private final ProcessEngine processEngine;

        private TestHelloWorldEndpoint(ProcessEngine processEngine) {
            this.processEngine = processEngine;
        }

        @Override
        protected ProcessEngine getProcessEngine() {
            return processEngine;
        }
    }
}
