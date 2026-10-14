package com.camunda.template.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(LoggerDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOG.info("Process Instance ID: {}", execution.getProcessInstanceId());
        LOG.info("Activity ID: {}", execution.getCurrentActivityId());
        LOG.info("Activity Name: {}", execution.getCurrentActivityName());
        LOG.info("Variables: {}", execution.getVariables());
    }
}
