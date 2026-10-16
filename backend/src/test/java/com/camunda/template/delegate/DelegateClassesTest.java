package com.camunda.template.delegate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;

public class DelegateClassesTest {

    @Test
    public void helloWorldDelegateUsesWorldWhenNameIsEmpty() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("name")).thenReturn("");

        HelloWorldDelegate delegate = new HelloWorldDelegate();

        delegate.execute(execution);

        verify(execution).setVariable("greeting", "Hello, World!");
    }

    @Test
    public void evaluateDecisionDelegateStoresOutputResponseWithApprovalStatus() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        EvaluateDecisionDelegate delegate = new EvaluateDecisionDelegate();

        delegate.execute(execution);

        verify(execution).setVariable("outputResponse", Map.of("success", true, "approvalStatus", "APPROVED"));
    }

    @Test
    public void loggerDelegateReadsExecutionDetails() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(execution.getCurrentActivityId()).thenReturn("activity-1");
        when(execution.getCurrentActivityName()).thenReturn("Test Activity");
        when(execution.getVariables()).thenReturn(Map.of("key", "value"));

        LoggerDelegate delegate = new LoggerDelegate();

        delegate.execute(execution);

        verify(execution).getProcessInstanceId();
        verify(execution).getCurrentActivityId();
        verify(execution).getCurrentActivityName();
        verify(execution).getVariables();
    }
}
