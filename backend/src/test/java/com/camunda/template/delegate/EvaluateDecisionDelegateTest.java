package com.camunda.template.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class EvaluateDecisionDelegateTest {

    private final EvaluateDecisionDelegate delegate = new EvaluateDecisionDelegate();

    @Test
    public void shouldCreateStructuredOutputResponseFromApprovalStatus() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        delegate.execute(execution);

        ArgumentCaptor<Map> responseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(execution).setVariable(eq("outputResponse"), responseCaptor.capture());

        Map<String, Object> response = responseCaptor.getValue();
        assertThat(response)
                .containsEntry("success", true)
                .containsEntry("approvalStatus", "APPROVED");
    }
}
