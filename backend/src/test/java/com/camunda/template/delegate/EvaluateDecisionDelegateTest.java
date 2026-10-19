package com.camunda.template.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EvaluateDecisionDelegateTest {

    @After
    public void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void shouldBuildStructuredOutputResponseFromApprovalStatus() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn("APPROVED");

        new EvaluateDecisionDelegate().execute(execution);

        verify(execution).setVariable(Mockito.eq("outputResponse"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> response = (Map<?, ?>) obj;
            return Boolean.TRUE.equals(response.get("success"))
                    && "APPROVED".equals(response.get("approvalStatus"));
        }));
    }

    @Test
    public void shouldStoreApprovalStatusEvenWhenItIsNull() throws Exception {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("approvalStatus")).thenReturn(null);

        new EvaluateDecisionDelegate().execute(execution);

        verify(execution).setVariable(Mockito.eq("outputResponse"), Mockito.argThat(obj -> {
            if (!(obj instanceof Map)) {
                return false;
            }
            Map<?, ?> response = (Map<?, ?>) obj;
            assertThat(response).containsEntry("success", true);
            return response.containsKey("approvalStatus") && response.get("approvalStatus") == null;
        }));
    }
}
