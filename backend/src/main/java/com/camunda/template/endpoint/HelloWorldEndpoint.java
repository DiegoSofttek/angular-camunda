package com.camunda.template.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Path("/hello")
public class HelloWorldEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response sayHello(@QueryParam("name") String name) {
        try {
            ProcessEngine processEngine = BpmPlatform.getDefaultProcessEngine();
            RuntimeService runtimeService = processEngine.getRuntimeService();

            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name != null ? name : "World");

            ProcessInstanceWithVariables result = runtimeService
                    .createProcessInstanceByKey("hello-world-process")
                    .setVariables(variables)
                    .executeWithVariablesInReturn();

            String greeting = (String) result.getVariables().get("greeting");

            Map<String, Object> response = new HashMap<>();
            response.put("greeting", greeting);

            return Response.ok(response).build();

        } catch (Exception e) {
            LOG.error("Error in hello world process", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
