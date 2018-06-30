package org.bigml.binding;

import cucumber.annotation.en.Given;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


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
        
    	String datasetId = (String) context.dataset.get("resource");
    	
    	JSONObject args = (JSONObject) JSONValue.parse(params);
        args.put("tags", Arrays.asList("unitTest"));
        args.put("objective_field", objective);
        
        JSONObject resource = BigMLClient.getInstance().createDeepnet(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.deepnet = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status); 	
    }

    @Given("^I create a local deepnet$")
    public void I_create_a_local_deepnet() throws Exception {
        context.localDeepnet = new LocalDeepnet(context.deepnet);
    }

}