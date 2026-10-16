package com.camunda.template.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationWithVariablesBuilder;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class HelloWorldEndpointTest {

    @After
    public void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldUseDefaultWorldWhenNameIsNull() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationWithVariablesBuilder builder = mock(ProcessInstantiationWithVariablesBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(anyMap())).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(result);

        Map<String, Object> variables = new HashMap<>();
        variables.put("greeting", "Hello, World!");
        when(result.getVariables()).thenReturn(variables);

        HelloWorldEndpoint endpoint = new TestableHelloWorldEndpoint(processEngine);

        javax.ws.rs.core.Response response = endpoint.sayHello(null);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getEntity()).get("greeting")).isEqualTo("Hello, World!");

        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(builder).setVariables(variablesCaptor.capture());
        assertThat(variablesCaptor.getValue()).containsEntry("name", "World");
    }

    @Test
    public void shouldReturnGreetingWhenProcessExecutesSuccessfully() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationWithVariablesBuilder builder = mock(ProcessInstantiationWithVariablesBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(anyMap())).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(result);

        Map<String, Object> variables = new HashMap<>();
        variables.put("greeting", "Hello, Camunda!");
        when(result.getVariables()).thenReturn(variables);

        HelloWorldEndpoint endpoint = new TestableHelloWorldEndpoint(processEngine);

        javax.ws.rs.core.Response response = endpoint.sayHello("Camunda");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getEntity()).get("greeting")).isEqualTo("Hello, Camunda!");
    }

    @Test
    public void shouldReturnInternalServerErrorWhenProcessEngineFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        when(processEngine.getRuntimeService()).thenThrow(new RuntimeException("boom"));

        HelloWorldEndpoint endpoint = new TestableHelloWorldEndpoint(processEngine);

        javax.ws.rs.core.Response response = endpoint.sayHello("Camunda");

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getEntity()).get("error")).isEqualTo("boom");
    }

    private static class TestableHelloWorldEndpoint extends HelloWorldEndpoint {
        private final ProcessEngine processEngine;

        private TestableHelloWorldEndpoint(ProcessEngine processEngine) {
            this.processEngine = processEngine;
        }

        @Override
        protected ProcessEngine getProcessEngine() {
            return processEngine;
        }
    }
}
