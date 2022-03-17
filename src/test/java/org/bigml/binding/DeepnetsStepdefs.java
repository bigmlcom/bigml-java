package org.bigml.binding;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;


public class DeepnetsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(DeepnetsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a deepnet with objective \"([^\"]*)\" and params \"(.*)\"$")
    public void I_create_a_deepnet_with_objective_and_params(String objective, String params) 
    		throws Throwable {
        
    	JSONObject args = (JSONObject) JSONValue.parse(params);
        args.put("objective_field", objective);

        commonSteps.I_create_a_resource_from_a_dataset_with(
            "deepnet", args.toString());
    }

    @Given("^I create a deepnet$")
    public void I_create_a_deepnet() throws Throwable {

	    JSONObject args = new JSONObject();
        args.put("suggest_structure", false);
        args.put("max_iterations", 100);
        args.put("deepnet_seed", "bigml");

    	commonSteps.I_create_a_resource_from_a_dataset_with(
    		"deepnet", args.toString());
    }

    @Given("^I create a local deepnet$")
    public void I_create_a_local_deepnet() throws Exception {
        context.localDeepnet = new LocalDeepnet(context.deepnet);
    }

}