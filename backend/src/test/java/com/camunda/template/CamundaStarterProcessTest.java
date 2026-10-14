package com.camunda.template;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

public class CamundaStarterProcessTest {

    @Rule
    public ProcessEngineRule processEngine = new ProcessEngineRule();

    @Test
    @Deployment(resources = {"hello-world-process.bpmn"})
    public void testHelloWorldProcess() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alejandro");

        ProcessInstanceWithVariables result = runtimeService()
                .createProcessInstanceByKey("hello-world-process")
                .setVariables(variables)
                .executeWithVariablesInReturn();

        assertThat(result).isNotNull();
        assertThat((String) result.getVariables().get("greeting")).isEqualTo("Hello, Alejandro!");
    }

    @Test
    @Deployment(resources = {"hello-world-process.bpmn"})
    public void testHelloWorldDefaultName() {
        ProcessInstanceWithVariables result = runtimeService()
                .createProcessInstanceByKey("hello-world-process")
                .executeWithVariablesInReturn();

        assertThat(result).isNotNull();
        assertThat((String) result.getVariables().get("greeting")).isEqualTo("Hello, World!");
    }

    @Test
    @Deployment(resources = {"camunda-starter-process.bpmn", "approval-decision.dmn"})
    public void testApprovedFlow() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", 500);
        variables.put("category", "STANDARD");

        ProcessInstanceWithVariables result = runtimeService()
                .createProcessInstanceByKey("camunda-starter-process")
                .setVariables(variables)
                .executeWithVariablesInReturn();

        assertThat(result).isNotNull();
        assertThat((String) result.getVariables().get("approvalStatus")).isEqualTo("APPROVED");
    }

    @Test
    @Deployment(resources = {"camunda-starter-process.bpmn", "approval-decision.dmn"})
    public void testRejectedFlow() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", 10000);
        variables.put("category", "PREMIUM");

        ProcessInstanceWithVariables result = runtimeService()
                .createProcessInstanceByKey("camunda-starter-process")
                .setVariables(variables)
                .executeWithVariablesInReturn();

        assertThat(result).isNotNull();
        assertThat((String) result.getVariables().get("approvalStatus")).isEqualTo("REJECTED");
    }
}
