package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;

import org.bigml.binding.utils.Utils;

import static org.junit.Assert.*;

public class ExecutionsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ExecutionsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a whizzml script execution from an existing script$")
    public void I_create_a_whizzml_script_execution_from_an_existing_script()
        throws AuthenticationException {
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        String scriptId = (String) context.script.get("resource");
        JSONObject resource = BigMLClient.getInstance().createExecution(scriptId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.execution = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a whizzml script execution from the last two scripts$")
    public void I_create_a_whizzml_script_execution_from_the_last_two_scripts() throws Throwable {
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        JSONObject resource = BigMLClient.getInstance().createExecution(context.scriptsIds, args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.execution = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I wait until the execution is ready less than (\\d+) secs$")
    public void I_wait_until_the_execution_is_ready_less_than_secs(int secs)
        throws AuthenticationException {
        I_wait_until_execution_status_code_is(AbstractResource.HTTP_OK,
                AbstractResource.HTTP_NOT_FOUND, secs);
    }

    @Given("^I wait until the execution status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_execution_status_code_is(int code1, int code2,
                                                   int secs) throws AuthenticationException {
        Long code = (Long) context.execution.get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_execution((String) context.execution.get("resource"));
            code = (Long) context.execution.get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I get the execution \"(.*)\"")
    public void I_get_the_execution(String executionId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getExecution(executionId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.execution = (JSONObject) resource.get("object");
    }

    @Given("^I update the execution with \"([^\"]*)\", \"([^\"]*)\"$")
    public void I_update_the_execution_with_(String param, String paramValue) throws Throwable {
        JSONObject chamges = new JSONObject();
        chamges.put(param, paramValue);

        JSONObject resource = BigMLClient.getInstance().updateExecution(
                context.execution, chamges);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.execution = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Given("^I wait until the execution update is ready less than (\\d+) secs$")
    public void I_wait_until_the_execution_update_is_ready_less_than_secs(int secs)
        throws AuthenticationException {
        I_wait_until_execution_status_code_is(AbstractResource.HTTP_ACCEPTED,
                AbstractResource.HTTP_NOT_FOUND, secs);
    }

    @Given("^I reset scripts$")
    public void I_reset_scripts() throws AuthenticationException {
        context.scriptsIds = new ArrayList<String>();
    }

    @Then("^the script id is correct, the value of \"([^\"]*)\" is \"([^\"]*)\" and the result is \"([^\"]*)\"$")
    public void the_script_id_is_correct_the_value_of_is_and_the_result_is(String param, String paramValue, Long expectedResult) throws Throwable {
        assertEquals(context.script.get("resource"), context.execution.get("script"));
        assertEquals(paramValue, context.execution.get(param));

        Long result = (Long) Utils.getJSONObject(context.execution, "execution.result");
        assertEquals(expectedResult, result);
    }

    @Then("^the value of \"([^\"]*)\" is \"([^\"]*)\" and the result is \"([^\"]*)\"$")
    public void the_value_of_is_and_the_result_is(String param, String paramValue, String expectedResult) throws Throwable {
        assertEquals(paramValue, context.execution.get(param));

        JSONArray result = (JSONArray) Utils.getJSONObject(context.execution, "execution.results");
        assertEquals(expectedResult, result.toString());
    }
}