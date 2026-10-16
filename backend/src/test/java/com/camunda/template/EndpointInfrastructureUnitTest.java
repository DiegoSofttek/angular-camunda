package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.VariableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.camunda.template.endpoint.HelloWorldEndpoint;
import com.camunda.template.endpoint.RestApplication;

public class EndpointInfrastructureUnitTest {

    private MockedStatic<BpmPlatform> bpmPlatformMock;
    private RuntimeDelegate originalRuntimeDelegate;

    @Before
    public void setUp() {
        originalRuntimeDelegate = RuntimeDelegate.getInstance();
        RuntimeDelegate.setInstance(new org.glassfish.jersey.internal.RuntimeDelegateImpl());
    }

    @After
    public void tearDown() {
        if (bpmPlatformMock != null) {
            bpmPlatformMock.close();
            bpmPlatformMock = null;
        }
        RuntimeDelegate.setInstance(originalRuntimeDelegate);
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void helloWorldEndpointReturnsGreetingFromProcessResult() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        RuntimeService.ProcessInstantiationBuilder builder = mock(RuntimeService.ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);
        VariableMap variables = mock(VariableMap.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> map = (Map<?, ?>) obj;
            return "Diego".equals(map.get("name"));
        }))).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(variables);
        when(variables.get("greeting")).thenReturn("Hello, Diego!");

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getEntity()).get("greeting")).isEqualTo("Hello, Diego!");
    }

    @Test
    public void helloWorldEndpointUsesWorldWhenNameIsNull() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        RuntimeService.ProcessInstantiationBuilder builder = mock(RuntimeService.ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = mock(ProcessInstanceWithVariables.class);
        VariableMap variables = mock(VariableMap.class);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> map = (Map<?, ?>) obj;
            return "World".equals(map.get("name"));
        }))).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(processResult);
        when(processResult.getVariables()).thenReturn(variables);
        when(variables.get("greeting")).thenReturn("Hello, World!");

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(((Map<?, ?>) response.getEntity()).get("greeting")).isEqualTo("Hello, World!");
    }

    @Test
    public void restApplicationIsApplicationSubclass() {
        Application application = new RestApplication();
        assertThat(application).isInstanceOf(Application.class);
        assertThat(application.getClasses()).isEmpty();
    }
}
