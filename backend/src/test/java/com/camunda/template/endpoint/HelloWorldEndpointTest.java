package com.camunda.template.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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
    public void shouldUseDefaultWorldWhenNameIsNull() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder instantiationBuilder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(instantiationBuilder);
        when(instantiationBuilder.setVariables(anyMap())).thenReturn(instantiationBuilder);
        when(instantiationBuilder.executeWithVariablesInReturn()).thenReturn(result);
        when(result.getVariables()).thenReturn(Map.of("greeting", "Hello, World!"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Map.of("greeting", "Hello, World!"));
        verify(instantiationBuilder).setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            return "World".equals(((Map<?, ?>) obj).get("name"));
        }));
    }

    @Test
    public void shouldReturnGreetingFromProcessResponse() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder instantiationBuilder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(instantiationBuilder);
        when(instantiationBuilder.setVariables(anyMap())).thenReturn(instantiationBuilder);
        when(instantiationBuilder.executeWithVariablesInReturn()).thenReturn(result);
        when(result.getVariables()).thenReturn(Map.of("greeting", "Hello, Camunda!"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello("Camunda");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Map.of("greeting", "Hello, Camunda!"));
        verify(instantiationBuilder).setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            return "Camunda".equals(((Map<?, ?>) obj).get("name"));
        }));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenProcessCreationFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process"))
                .thenThrow(new RuntimeException("Camunda unavailable"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello("Camunda");

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Map.of("error", "Camunda unavailable"));
    }
}
