package com.camunda.template.endpoint;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Path("/approve")
public class StartProcessEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(StartProcessEndpoint.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startProcess(Map<String, Object> request) {
        try {
            RuntimeService runtimeService = getProcessEngine().getRuntimeService();

            Map<String, Object> variables = new HashMap<>(request);

            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("camunda-starter-process", variables);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "STARTED");

            LOG.info("Process started with ID: {}", processInstance.getId());
            return Response.ok(response).build();

        } catch (Exception e) {
            LOG.error("Error starting process", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    protected org.camunda.bpm.engine.ProcessEngine getProcessEngine() {
        return BpmPlatform.getDefaultProcessEngine();
    }
}
