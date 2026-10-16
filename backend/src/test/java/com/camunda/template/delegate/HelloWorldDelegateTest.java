package com.camunda.template.delegate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloWorldDelegateTest {

    @After
    public void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldSetGreetingWithProvidedName() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Alejandro");

        HelloWorldDelegate delegate = new HelloWorldDelegate();

        delegate.execute(execution);

        verify(execution).setVariable("greeting", "Hello, Alejandro!");
    }

    @Test
    public void shouldUseWorldWhenNameIsEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        HelloWorldDelegate delegate = new HelloWorldDelegate();

        delegate.execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }
}
