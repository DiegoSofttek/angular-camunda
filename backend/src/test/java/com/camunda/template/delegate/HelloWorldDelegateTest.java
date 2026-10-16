package com.camunda.template.delegate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

public class HelloWorldDelegateTest {

    @Test
    public void shouldSetGreetingForProvidedName() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("Alejandro");

        HelloWorldDelegate delegate = new HelloWorldDelegate();

        delegate.execute(execution);

        verify(execution).setVariable("greeting", "Hello, Alejandro!");
    }

    @Test
    public void shouldUseWorldWhenNameIsNull() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn(null);

        HelloWorldDelegate delegate = new HelloWorldDelegate();

        delegate.execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
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
