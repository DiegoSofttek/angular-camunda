package com.camunda.template.delegate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

public class LoggerDelegateTest {

    @After
    public void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldLogExecutionDetailsWithoutThrowing() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(execution.getCurrentActivityId()).thenReturn("activity-1");
        when(execution.getCurrentActivityName()).thenReturn("My Activity");
        when(execution.getVariables()).thenReturn(Collections.singletonMap("key", "value"));

        LoggerDelegate delegate = new LoggerDelegate();

        delegate.execute(execution);
    }
}
