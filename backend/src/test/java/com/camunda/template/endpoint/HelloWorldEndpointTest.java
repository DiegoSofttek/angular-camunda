package com.camunda.template.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
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
            bpmPlatformMock = null;
        }
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldUseWorldWhenNameIsNull() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder processBuilder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(processBuilder);
        when(processBuilder.setVariables(anyMap())).thenAnswer(invocation -> {
            Map<?, ?> variables = invocation.getArgument(0);
            assertThat(variables.get("name")).isEqualTo("World");
            return processBuilder;
        });
        when(processBuilder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(Collections.singletonMap("greeting", "Hello, World!"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Collections.singletonMap("greeting", "Hello, World!"));
    }

    @Test
    public void shouldReturnGreetingWhenProcessStartsSuccessfully() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder processBuilder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(processBuilder);
        when(processBuilder.setVariables(anyMap())).thenAnswer(invocation -> processBuilder);
        when(processBuilder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(Collections.singletonMap("greeting", "Hello, Alejandro!"));

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello("Alejandro");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Collections.singletonMap("greeting", "Hello, Alejandro!"));
    }

    @Test
    public void shouldReturnInternalServerErrorWhenProcessCreationFails() {
        RuntimeException failure = new RuntimeException("camunda unavailable");
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenThrow(failure);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello("Any");

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Collections.singletonMap("error", "camunda unavailable"));
    }
}
