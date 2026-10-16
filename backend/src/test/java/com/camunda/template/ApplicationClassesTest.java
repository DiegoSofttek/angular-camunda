package com.camunda.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.camunda.template.endpoint.RestApplication;
import org.junit.Test;

public class ApplicationClassesTest {

    @Test
    public void camundaStarterApplicationCanBeInstantiated() {
        CamundaStarterApplication application = new CamundaStarterApplication();

        assertThat(application).isNotNull();
    }

    @Test
    public void restApplicationCanBeInstantiated() {
        RestApplication application = new RestApplication();

        assertThat(application).isNotNull();
    }
}
