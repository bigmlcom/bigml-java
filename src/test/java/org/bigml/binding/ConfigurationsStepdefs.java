package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.When;
import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class ConfigurationsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ConfigurationsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a configuration with \"(.*)\"$")
    public void I_create_a_configuration_with_name(String args)
        throws AuthenticationException {

        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);
        argsJSON.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createConfiguration(argsJSON);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.configuration = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I update the configuration with \"(.*)\"$")
    public void I_update_the_configuration_with(String args)
            throws Throwable {
        if (args.equals("{}")) {
            assertTrue("No update params. Continue", true);
        } else {
            String configurationId = (String) context.configuration.get("resource");
            JSONObject resource = BigMLClient.getInstance().updateConfiguration(
                    configurationId, args);
            context.status = (Integer) resource.get("code");
            context.location = (String) resource.get("location");
            context.configuration = (JSONObject) resource.get("object");
            commonSteps.the_resource_has_been_updated_with_status(context.status);
        }
    }

    @Given("^I check the configuration name \"(.*)\"$")
    public void I_check_the_configuration_name(String expectedName)
            throws Throwable {
        assertEquals(expectedName, context.configuration.get("name"));
    }

    @Given("^I wait until the configuration status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_configuration_status_code_is(int code1, int code2,
                                                   int secs) throws AuthenticationException {
        Long code = (Long) context.configuration.get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_configuration((String) context.configuration.get("resource"));
            code = (Long) context.configuration.get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the configuration is ready less than (\\d+) secs$")
    public void I_wait_until_the_configuration_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_configuration_status_code_is(AbstractResource.HTTP_CREATED,
                AbstractResource.HTTP_NOT_FOUND, secs);
    }

    @Given("^I get the configuration \"(.*)\"")
    public void I_get_the_configuration(String configurationId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getConfiguration(configurationId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.configuration = (JSONObject) resource.get("object");
    }


    @When("I delete the configuration$")
    public void i_delete_the_configuration() throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().deleteConfiguration(context.configuration);
        context.status = (Integer) resource.get("code");
        assertTrue(context.status == AbstractResource.HTTP_NO_CONTENT);
        context.configuration = null;
    }

}