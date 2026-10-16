package com.camunda.template.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class HelloWorldEndpointTest {

    private MockedStatic<BpmPlatform> bpmPlatformMock;

    static {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @After
    public void tearDown() {
        if (bpmPlatformMock != null) {
            bpmPlatformMock.close();
        }
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldUseWorldWhenNameIsNull() {
        ProcessEngine processEngine = Mockito.mock(ProcessEngine.class);
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        ProcessInstantiationBuilder processBuilder = Mockito.mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = Mockito.mock(ProcessInstanceWithVariables.class);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(processBuilder);
        when(processBuilder.setVariables(anyMap())).thenReturn(processBuilder);
        when(processBuilder.executeWithVariablesInReturn()).thenReturn(processResult);
        VariableMap variables = Mockito.mock(VariableMap.class);
        when(processResult.getVariables()).thenReturn(variables);
        when(variables.get("greeting")).thenReturn("Hello, World!");

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello(null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Collections.singletonMap("greeting", "Hello, World!"));
        verify(processBuilder).setVariables(Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            return "World".equals(((Map<?, ?>) obj).get("name"));
        }));
    }

    @Test
    public void shouldReturnGreetingFromProcessVariables() {
        ProcessEngine processEngine = Mockito.mock(ProcessEngine.class);
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        ProcessInstantiationBuilder processBuilder = Mockito.mock(ProcessInstantiationBuilder.class);
        ProcessInstanceWithVariables processResult = Mockito.mock(ProcessInstanceWithVariables.class);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(processBuilder);
        when(processBuilder.setVariables(anyMap())).thenReturn(processBuilder);
        when(processBuilder.executeWithVariablesInReturn()).thenReturn(processResult);
        VariableMap variables = Mockito.mock(VariableMap.class);
        when(processResult.getVariables()).thenReturn(variables);
        when(variables.get("greeting")).thenReturn("Hello, Camunda!");

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello("Camunda");

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Collections.singletonMap("greeting", "Hello, Camunda!"));
        verify(runtimeService).createProcessInstanceByKey("hello-world-process");
    }

    @Test
    public void shouldReturnInternalServerErrorWhenProcessStartFails() {
        ProcessEngine processEngine = Mockito.mock(ProcessEngine.class);
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        ProcessInstantiationBuilder processBuilder = Mockito.mock(ProcessInstantiationBuilder.class);

        bpmPlatformMock = Mockito.mockStatic(BpmPlatform.class);
        bpmPlatformMock.when(BpmPlatform::getDefaultProcessEngine).thenReturn(processEngine);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createProcessInstanceByKey("hello-world-process")).thenReturn(processBuilder);
        when(processBuilder.setVariables(anyMap())).thenReturn(processBuilder);
        when(processBuilder.executeWithVariablesInReturn()).thenThrow(new RuntimeException("camunda unavailable"));

        HelloWorldEndpoint endpoint = new HelloWorldEndpoint();

        Response response = endpoint.sayHello("Camunda");

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(Collections.singletonMap("error", "camunda unavailable"));
    }

    private static class TestRuntimeDelegate extends RuntimeDelegate {

        @Override
        public Response.ResponseBuilder createResponseBuilder() {
            return new TestResponseBuilder();
        }

        @Override
        public UriBuilder createUriBuilder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public javax.ws.rs.core.Variant.VariantListBuilder createVariantListBuilder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T createEndpoint(Application application, Class<T> endpointType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public javax.ws.rs.core.Link.Builder createLinkBuilder() {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestResponseBuilder extends Response.ResponseBuilder {

        private int status;
        private Object entity;

        @Override
        public Response build() {
            return new TestResponse(status, entity);
        }

        @Override
        public Response.ResponseBuilder clone() {
            TestResponseBuilder builder = new TestResponseBuilder();
            builder.status = this.status;
            builder.entity = this.entity;
            return builder;
        }

        @Override
        public Response.ResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        @Override
        public Response.ResponseBuilder status(int status, String reasonPhrase) {
            this.status = status;
            return this;
        }

        @Override
        public Response.ResponseBuilder entity(Object entity) {
            this.entity = entity;
            return this;
        }

        @Override
        public Response.ResponseBuilder entity(Object entity, java.lang.annotation.Annotation[] annotations) {
            this.entity = entity;
            return this;
        }

        @Override
        public Response.ResponseBuilder allow(String... methods) {
            return this;
        }

        @Override
        public Response.ResponseBuilder allow(java.util.Set<String> methods) {
            return this;
        }

        @Override
        public Response.ResponseBuilder cacheControl(javax.ws.rs.core.CacheControl cacheControl) {
            return this;
        }

        @Override
        public Response.ResponseBuilder encoding(String encoding) {
            return this;
        }

        @Override
        public Response.ResponseBuilder header(String name, Object value) {
            return this;
        }

        @Override
        public Response.ResponseBuilder replaceAll(javax.ws.rs.core.MultivaluedMap<String, Object> headers) {
            return this;
        }

        @Override
        public Response.ResponseBuilder language(String language) {
            return this;
        }

        @Override
        public Response.ResponseBuilder language(java.util.Locale language) {
            return this;
        }

        @Override
        public Response.ResponseBuilder type(javax.ws.rs.core.MediaType type) {
            return this;
        }

        @Override
        public Response.ResponseBuilder type(String type) {
            return this;
        }

        @Override
        public Response.ResponseBuilder variant(javax.ws.rs.core.Variant variant) {
            return this;
        }

        @Override
        public Response.ResponseBuilder contentLocation(java.net.URI location) {
            return this;
        }

        @Override
        public Response.ResponseBuilder cookie(javax.ws.rs.core.NewCookie... cookies) {
            return this;
        }

        @Override
        public Response.ResponseBuilder expires(java.util.Date expires) {
            return this;
        }

        @Override
        public Response.ResponseBuilder lastModified(java.util.Date lastModified) {
            return this;
        }

        @Override
        public Response.ResponseBuilder location(java.net.URI location) {
            return this;
        }

        @Override
        public Response.ResponseBuilder tag(javax.ws.rs.core.EntityTag tag) {
            return this;
        }

        @Override
        public Response.ResponseBuilder tag(String tag) {
            return this;
        }

        @Override
        public Response.ResponseBuilder variants(javax.ws.rs.core.Variant... variants) {
            return this;
        }

        @Override
        public Response.ResponseBuilder variants(java.util.List<javax.ws.rs.core.Variant> variants) {
            return this;
        }

        @Override
        public Response.ResponseBuilder links(javax.ws.rs.core.Link... links) {
            return this;
        }

        @Override
        public Response.ResponseBuilder link(java.net.URI uri, String rel) {
            return this;
        }

        @Override
        public Response.ResponseBuilder link(String uri, String rel) {
            return this;
        }
    }

    private static class TestResponse extends Response {

        private final int status;
        private final Object entity;

        private TestResponse(int status, Object entity) {
            this.status = status;
            this.entity = entity;
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public StatusType getStatusInfo() {
            return Response.Status.fromStatusCode(status);
        }

        @Override
        public Object getEntity() {
            return entity;
        }

        @Override
        public <T> T readEntity(Class<T> entityType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T readEntity(javax.ws.rs.core.GenericType<T> entityType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T readEntity(Class<T> entityType, java.lang.annotation.Annotation[] annotations) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T readEntity(javax.ws.rs.core.GenericType<T> entityType, java.lang.annotation.Annotation[] annotations) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasEntity() {
            return entity != null;
        }

        @Override
        public boolean bufferEntity() {
            return false;
        }

        @Override
        public void close() {
        }

        @Override
        public javax.ws.rs.core.MediaType getMediaType() {
            return null;
        }

        @Override
        public java.util.Locale getLanguage() {
            return null;
        }

        @Override
        public int getLength() {
            return -1;
        }

        @Override
        public java.util.Set<String> getAllowedMethods() {
            return Collections.emptySet();
        }

        @Override
        public java.util.Map<String, javax.ws.rs.core.NewCookie> getCookies() {
            return Collections.emptyMap();
        }

        @Override
        public javax.ws.rs.core.EntityTag getEntityTag() {
            return null;
        }

        @Override
        public java.util.Date getDate() {
            return null;
        }

        @Override
        public java.util.Date getLastModified() {
            return null;
        }

        @Override
        public java.net.URI getLocation() {
            return null;
        }

        @Override
        public java.util.Set<javax.ws.rs.core.Link> getLinks() {
            return Collections.emptySet();
        }

        @Override
        public boolean hasLink(String relation) {
            return false;
        }

        @Override
        public javax.ws.rs.core.Link getLink(String relation) {
            return null;
        }

        @Override
        public javax.ws.rs.core.Link.Builder getLinkBuilder(String relation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public javax.ws.rs.core.MultivaluedMap<String, Object> getMetadata() {
            return new javax.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public javax.ws.rs.core.MultivaluedMap<String, Object> getHeaders() {
            return new javax.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public javax.ws.rs.core.MultivaluedMap<String, String> getStringHeaders() {
            return new javax.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public String getHeaderString(String name) {
            return null;
        }

        @Override
        public String toString() {
            return "TestResponse{" + "status=" + status + ", entity=" + entity + '}';
        }
    }
}
