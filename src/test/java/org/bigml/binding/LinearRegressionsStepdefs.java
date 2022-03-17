package org.bigml.binding;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;

public class LinearRegressionsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(LinearRegressionsStepdefs.class);

    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;


    @Given("^I create a linearregression with objective \"([^\"]*)\" and params \"(.*)\"$")
    public void I_create_a_linearregression_with_objective_and_params(String objective, String params) 
    		throws Throwable {
    	
    	JSONObject args = new JSONObject();
    	if (!"".equals(params)) {
    		args = (JSONObject) JSONValue.parse(params);
    	}
        args.put("objective_field", objective);
        
    	commonSteps.I_create_a_resource_from_a_dataset_with(
    		"linearregression", args.toString());
    	if( context.models == null ) {
            context.models = new JSONArray();
        }
        context.models.add(context.linearRegression);
    }
    
    @Given("^I create a local linearregression$")
    public void I_create_a_local_linearregression() throws Exception {
		context.localLinearRegression = 
        		new LocalLinearRegression(context.linearRegression);
    }
}