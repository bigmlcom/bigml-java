package org.bigml.binding;

import java.util.ArrayList;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class ExecutionsStepdefs {

    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a whizzml script execution from an existing script$")
    public void I_create_a_whizzml_script_execution_from_an_existing_script()
    		throws AuthenticationException {
    	
    	JSONObject args = commonSteps.setProject(null);
        String scriptId = (String) context.script.get("resource");
        JSONObject resource = context.api.createExecution(scriptId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.execution = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a whizzml script execution from the last two scripts$")
    public void I_create_a_whizzml_script_execution_from_the_last_two_scripts() 
    		throws Throwable {
    	
    	JSONObject args = commonSteps.setProject(null);
        JSONObject resource = context.api.createExecution(context.scriptsIds, args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.execution = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I reset scripts$")
    public void I_reset_scripts() throws AuthenticationException {
        context.scriptsIds = new ArrayList<>();
    }

    @Then("^the script id is correct and the result is \"([^\"]*)\"$")
    public void the_script_id_is_correct_and_the_result_is(Long expectedResult) throws Throwable {
        assertEquals(context.script.get("resource"), context.execution.get("script"));

        Long result = ((Number) Utils.getJSONObject(
        	context.execution, "execution.result")).longValue();
        assertEquals(expectedResult, result);
    }
    
    @Then("^the result is \"([^\"]*)\"$")
    public void the_value_of_is_and_the_result_is(String expectedResult) throws Throwable {
        JSONArray result = (JSONArray) Utils.getJSONObject(context.execution, "execution.results");
        assertEquals(expectedResult, result.toString());
    }
}