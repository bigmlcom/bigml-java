package org.bigml.binding;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class LibrariesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(LibrariesStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;


    @Given("^I create a whizzml library from a excerpt of code \"([^\"]*)\"$")
    public void I_create_a_whizzml_library_from_a_excerpt_of_code(String source)
    		throws AuthenticationException {
    	
    	JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createLibrary(source, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.library = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Then("^the library code is \"([^\"]*)\"$")
    public void the_library_code_is_and_the_value_of_is(String source) 
    		throws AuthenticationException {
        assertEquals(source, context.library.get("source_code"));
    }

}