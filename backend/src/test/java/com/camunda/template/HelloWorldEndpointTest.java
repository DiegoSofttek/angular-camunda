package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.camunda.template.endpoint.HelloWorldEndpoint;

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
        org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder instantiationBuilder =
                mock(org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(instantiationBuilder);
        when(instantiationBuilder.setVariables(any(Map.class))).thenAnswer(invocation -> {
            Map<?, ?> variables = invocation.getArgument(0, Map.class);
            assertThat(variables).containsEntry("name", "World");
            return instantiationBuilder;
        });
        when(instantiationBuilder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(Map.of("greeting", "Hello, World!"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("greeting", "Hello, World!");
    }

    @Test
    public void shouldReturnGreetingFromProcessResponse() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder instantiationBuilder =
                mock(org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(instantiationBuilder);
        when(instantiationBuilder.setVariables(any(Map.class))).thenReturn(instantiationBuilder);
        when(instantiationBuilder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(Map.of("greeting", "Hello, Alejandro!"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello("Alejandro");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("greeting", "Hello, Alejandro!");
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

        Response response = endpoint.sayHello("Alejandro");

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat((Map<?, ?>) response.getEntity()).containsEntry("error", "Camunda unavailable");
    }
}
