package org.bigml.binding;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import org.bigml.binding.CommonStepdefs;
import org.bigml.binding.utils.Utils;


public class EvaluationsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(EvaluationsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;
    
    @When("^I create an evaluation for the (model|ensemble|logisticregression|linearregression|fusion) with the dataset$")
    public void I_create_an_evaluation_with_the_dataset(String resourceName)
            throws Throwable {
    	
    	String datasetId = (String) context.dataset.get("resource");
		String modelId = (String) 
			commonSteps.getResource(resourceName).get("resource");
				
		JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = context.api.createEvaluation(
        		modelId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.evaluation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    
    @Then("^the measured \"([^\"]*)\" is equals to ([\\d,.]+)$")
    public void the_measured_is_equals_to(String measure, double value)
            throws Throwable {
    	
        double measureLong = ((Number) Utils.getJSONObject(context.evaluation,
                "result.model." + measure)).doubleValue();
        
        assertEquals(measureLong, value, 0.00001);
    }

}
