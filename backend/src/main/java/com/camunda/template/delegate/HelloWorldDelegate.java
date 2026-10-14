package com.camunda.template.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldDelegate implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String name = (String) execution.getVariable("name");
        if (name == null || name.isEmpty()) {
            name = "World";
        }

        String greeting = "Hello, " + name + "!";
        LOG.info(greeting);

        execution.setVariable("greeting", greeting);
    }
}
