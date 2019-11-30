package org.bigml.binding;

import cucumber.annotation.en.Given;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


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
        
    	JSONObject args = new JSONObject();
    	if (!"".equals(params)) {
    		args = (JSONObject) JSONValue.parse(params);
    	}
        args.put("objective_field", objective);
        
    	commonSteps.I_create_a_resource_from_a_dataset_with(
    		"logisticregression", args.toString());
    	if( context.models == null ) {
            context.models = new JSONArray();
        }
        context.models.add(context.logisticRegression);
    }

    @Given("^I create a local logisticregression$")
    public void I_create_a_local_logisticregression() throws Exception {
        context.localLogisticRegression = 
        		new LocalLogisticRegression(context.logisticRegression);
    }

}