package com.camunda.template.delegate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

public class LoggerDelegateTest {

    @Test
    public void shouldReadExecutionMetadataAndVariables() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(execution.getCurrentActivityId()).thenReturn("activity-1");
        when(execution.getCurrentActivityName()).thenReturn("My Activity");
        when(execution.getVariables()).thenReturn(Map.of("key", "value"));

        LoggerDelegate delegate = new LoggerDelegate();

        delegate.execute(execution);

        verify(execution).getProcessInstanceId();
        verify(execution).getCurrentActivityId();
        verify(execution).getCurrentActivityName();
        verify(execution).getVariables();
    }
}
