package com.camunda.template.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EvaluateDecisionDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluateDecisionDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String approvalStatus = (String) execution.getVariable("approvalStatus");
        LOG.info("DMN Decision Result - approvalStatus: {}", approvalStatus);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("approvalStatus", approvalStatus);

        execution.setVariable("outputResponse", response);
    }
}
