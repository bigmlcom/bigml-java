package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class PcaStepdefs {

	// Logging
	Logger logger = LoggerFactory.getLogger(PcaStepdefs.class);

	@Autowired
	CommonStepdefs commonSteps;

	@Autowired
	private ContextRepository context;
	
	LocalPca localPca;
	
	@Given("^I create a pca with \"(.*)\"$")
    public void I_create_a_pca_with_params(String args) throws Throwable {
        String datasetId = (String) context.dataset.get("resource");
        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);

        if( argsJSON != null ) {
            if (argsJSON.containsKey("tags")) {
                ((JSONArray) argsJSON.get("tags")).add("unitTest");
            } else {
                argsJSON.put("tags", Arrays.asList("unitTest"));
            }
        } else {
            argsJSON = new JSONObject();
            argsJSON.put("tags", Arrays.asList("unitTest"));
        }

        JSONObject resource = context.api.createPca(datasetId,
                argsJSON, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.pca = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
	

	@When("^I create a projection for \"(.*)\"$")
	public void I_create_a_projection_for(String inputData)
			throws AuthenticationException {
		String pcaId = (String) context.pca.get("resource");

		JSONObject args = new JSONObject();
		args.put("tags", Arrays.asList("unitTest"));
		
		JSONObject resource = context.api.createProjection(pcaId,
				(JSONObject) JSONValue.parse(inputData), args, 5, null);
		context.status = (Integer) resource.get("code");
		context.location = (String) resource.get("location");
		context.projection = (JSONObject) resource.get("object");
		commonSteps.the_resource_has_been_created_with_status(context.status);
	}

	@Then("^the projection is \"(.*)\"$")
    public void the_projection_is(String projection) throws Throwable {
        JSONObject expected = (JSONObject) JSONValue.parse(projection);
        JSONObject actual = (JSONObject)
                Utils.getJSONObject(context.projection, "projection.result");
        assertEquals(expected, actual);
    }
	
    
    @Given("^I create a local pca$")
    public void I_create_a_local_pca() throws Exception {
        localPca = new LocalPca(context.pca);
    }
    
    
    @Then("^I create a local projection for \"(.*)\"$")
    public void I_create_a_local_projection_for(String data) 
    		throws Throwable {
    	if( data == null || data.trim().length() == 0 ) {
            data = "{}";
        }

        JSONObject inputData = (JSONObject) JSONValue.parse(data);
        
        context.localProjection = localPca.projection(
        		inputData, null, null, null);
    }
    
    @Then("^the local projection is \"(.*)\"$")
    public void the_local_projection_is(String projection) throws Throwable {
        JSONObject expected = (JSONObject) JSONValue.parse(projection);
        JSONObject actual = context.localProjection;
        
        for (Object keyElement: actual.keySet()) {
        	String key = (String) keyElement;
        	Double actualValue = ((Number) actual.get(key)).doubleValue();
        	Double expectedValue = ((Number) expected.get(key)).doubleValue();
        	assertTrue(expectedValue == Utils.roundOff(actualValue, 5));
        }
    }
    
}