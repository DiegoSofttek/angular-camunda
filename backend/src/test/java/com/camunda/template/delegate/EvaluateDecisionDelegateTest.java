package com.camunda.template.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class EvaluateDecisionDelegateTest {

    @After
    public void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldStoreOutputResponseWithApprovalStatus() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        EvaluateDecisionDelegate delegate = new EvaluateDecisionDelegate();

        delegate.execute(execution);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(execution).setVariable(org.mockito.Mockito.eq("outputResponse"), captor.capture());
        assertThat(captor.getValue()).containsEntry("success", true);
        assertThat(captor.getValue()).containsEntry("approvalStatus", "APPROVED");
    }
}
