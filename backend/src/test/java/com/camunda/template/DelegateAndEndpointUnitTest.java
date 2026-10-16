package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.camunda.template.delegate.EvaluateDecisionDelegate;
import com.camunda.template.delegate.HelloWorldDelegate;
import com.camunda.template.endpoint.HelloWorldEndpoint;
import com.camunda.template.endpoint.RestApplication;
import com.camunda.template.endpoint.StartProcessEndpoint;

public class DelegateAndEndpointUnitTest {

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
    public void shouldDefaultGreetingToWorldWhenNameIsNull() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn(null);

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void shouldDefaultGreetingToWorldWhenNameIsEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void shouldCreateGreetingWithProvidedName() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Diego");

        new HelloWorldDelegate().execute(execution);

        verify(execution).setVariable("greeting", "Hello, Diego!");
    }

    @Test
    public void shouldMapApprovalStatusIntoOutputResponse() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        new EvaluateDecisionDelegate().execute(execution);

        verify(execution).setVariable(eq("outputResponse"), Mockito.argThat(response ->
                response != null
                        && Boolean.TRUE.equals(response.get("success"))
                        && "APPROVED".equals(response.get("approvalStatus"))));
    }

    @Test
    public void shouldStartApprovalProcessAndReturnStartedStatus() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Map<String, Object> request = Collections.singletonMap("amount", 500);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), anyMap())).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-123");
        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new StartProcessEndpoint().startProcess(request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertThat((Map<String, Object>) response.getEntity()).containsEntry("status", "STARTED");
        verify(runtimeService).startProcessInstanceByKey(eq("camunda-starter-process"), eq(request));
    }

    @Test
    public void shouldReturnServerErrorWhenStartProcessFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.startProcessInstanceByKey(eq("camunda-starter-process"), anyMap()))
                .thenThrow(new IllegalStateException("boom"));

        Response response = new StartProcessEndpoint().startProcess(Collections.singletonMap("amount", 500));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertThat((Map<String, String>) response.getEntity()).containsEntry("error", "boom");
    }

    @Test
    public void shouldStartHelloProcessWithNameAndReturnGreeting() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder builder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);
        Map<String, Object> expectedVariables = Collections.singletonMap("name", "Diego");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(expectedVariables)).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(result);
        when(result.getVariables()).thenReturn(Collections.singletonMap("greeting", "Hello, Diego!"));
        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertThat((Map<String, Object>) response.getEntity()).containsEntry("greeting", "Hello, Diego!");
        verify(builder).setVariables(eq(expectedVariables));
    }

    @Test
    public void shouldDefaultHelloEndpointNameToWorldWhenQueryParamIsNull() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstantiationBuilder builder = mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables result = mock(ProcessInstanceWithVariables.class);
        Map<String, Object> expectedVariables = Collections.singletonMap("name", "World");

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(builder);
        when(builder.setVariables(expectedVariables)).thenReturn(builder);
        when(builder.executeWithVariablesInReturn()).thenReturn(result);
        when(result.getVariables()).thenReturn(Collections.singletonMap("greeting", "Hello, World!"));
        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);

        Response response = new HelloWorldEndpoint().sayHello(null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertThat((Map<String, Object>) response.getEntity()).containsEntry("greeting", "Hello, World!");
        verify(builder).setVariables(eq(expectedVariables));
    }

    @Test
    public void shouldReturnServerErrorWhenHelloEndpointFails() {
        ProcessEngine processEngine = mock(ProcessEngine.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process"))
                .thenThrow(new IllegalArgumentException("hello-failed"));

        Response response = new HelloWorldEndpoint().sayHello("Diego");

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertThat((Map<String, String>) response.getEntity()).containsEntry("error", "hello-failed");
    }

    @Test
    public void shouldExposeExpectedApplicationPathOnRestApplication() {
        ApplicationPath annotation = RestApplication.class.getAnnotation(ApplicationPath.class);

        assertNotNull(annotation);
        assertEquals("/api", annotation.value());
        assertTrue(javax.ws.rs.core.Application.class.isAssignableFrom(RestApplication.class));
    }

    @Test
    public void shouldCarryProcessApplicationAnnotationMetadata() {
        assertTrue(EjbProcessApplication.class.isAssignableFrom(com.camunda.template.CamundaStarterApplication.class));
        ProcessApplication annotation = com.camunda.template.CamundaStarterApplication.class
                .getAnnotation(ProcessApplication.class);

        assertNotNull(annotation);
        assertEquals("camunda-starter-template", annotation.value());
    }
}
