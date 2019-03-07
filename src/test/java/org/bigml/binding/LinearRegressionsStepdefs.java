package org.bigml.binding;

import cucumber.annotation.en.Given;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


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

    	String datasetId = (String) context.dataset.get("resource");

    	JSONObject args = new JSONObject();
    	if (!"".equals(params)) {
    		args = (JSONObject) JSONValue.parse(params);
    	}
        args.put("tags", Arrays.asList("unitTest"));
        args.put("objective_field", objective);

        JSONObject resource = context.api.createLinearRegression(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.linearRegression = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);

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