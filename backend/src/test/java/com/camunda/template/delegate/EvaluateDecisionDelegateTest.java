package com.camunda.template.delegate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class EvaluateDecisionDelegateTest {

    @Test
    public void shouldPopulateOutputResponseWithApprovalStatus() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        EvaluateDecisionDelegate delegate = new EvaluateDecisionDelegate();

        delegate.execute(execution);

        ArgumentCaptor<Map<String, Object>> responseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(execution).setVariable(eq("outputResponse"), responseCaptor.capture());
        assertTrue(Boolean.TRUE.equals(responseCaptor.getValue().get("success")));
        assertEquals("APPROVED", responseCaptor.getValue().get("approvalStatus"));
    }
}
