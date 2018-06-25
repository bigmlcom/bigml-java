package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

public class ScriptsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ScriptsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a whizzml script from a excerpt of code \"([^\"]*)\"$")
    public void I_create_a_whizzml_script_from_a_excerpt_of_code(String source)
        throws AuthenticationException {
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createScript(source, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.script = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);

        context.scriptsIds.add((String) context.script.get("resource"));
    }
    
    @Then("^the script code is \"([^\"]*)\"$")
    public void the_resource_name_is(String source) 
    		throws Throwable {
    	assertEquals(source, context.script.get("source_code"));
    }

}