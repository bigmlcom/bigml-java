package org.bigml.binding;

import cucumber.annotation.en.Given;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


public class LogisticRegressionsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(LogisticRegressionsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a logisticregression with objective \"([^\"]*)\" and params \"(.*)\"$")
    public void I_create_a_logisticregression_with_objective_and_params(String objective, String params) 
    		throws Throwable {
        
    	String datasetId = (String) context.dataset.get("resource");

    	JSONObject args = new JSONObject();
    	if (!"".equals(params)) {
    		args = (JSONObject) JSONValue.parse(params);
    	}
        args.put("tags", Arrays.asList("unitTest"));
        args.put("objective_field", objective);
        
        JSONObject resource = BigMLClient.getInstance().createLogisticRegression(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.logisticRegression = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status); 	
    }

    @Given("^I create a local logisticregression$")
    public void I_create_a_local_logisticregression() throws Exception {
        context.localLogisticRegression = 
        		new LocalLogisticRegression(context.logisticRegression);
    }

}