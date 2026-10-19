package com.camunda.template.endpoint;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HelloWorldEndpointTest {

    private MockedStatic<BpmPlatform> bpmPlatformMock;

    @After
    public void tearDown() {
        if (bpmPlatformMock != null) {
            bpmPlatformMock.close();
        }
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldReturnGreetingFromHelloWorldProcess() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder instantiationBuilder = mock(org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);
        Map<String, Object> variables = new HashMap<>();
        variables.put("greeting", "Hello, Diego!");

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(instantiationBuilder);
        when(instantiationBuilder.setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> requestVariables = (Map<?, ?>) obj;
            return "Diego".equals(requestVariables.get("name"));
        }))).thenReturn(instantiationBuilder);
        when(instantiationBuilder.executeWithVariablesInReturn()).thenReturn(result);
        when(result.getVariables()).thenReturn(variables);

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("greeting", "Hello, Diego!");
    }

    @Test
    public void shouldDefaultNameToWorldWhenQueryParamIsNull() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder instantiationBuilder = mock(org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(instantiationBuilder);
        when(instantiationBuilder.setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> requestVariables = (Map<?, ?>) obj;
            return "World".equals(requestVariables.get("name"));
        }))).thenReturn(instantiationBuilder);
        when(instantiationBuilder.executeWithVariablesInReturn()).thenReturn(result);
        when(result.getVariables()).thenReturn(Collections.singletonMap("greeting", "Hello, World!"));

        Response response = new HelloWorldEndpoint().sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("greeting", "Hello, World!");
    }

    @Test
    public void shouldReturnInternalServerErrorWhenHelloWorldProcessFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process"))
                .thenThrow(new RuntimeException("hello failure"));

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("error", "hello failure");
    }
}
