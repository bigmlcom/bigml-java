package org.bigml.binding;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;

public class EnsemblesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(EnsemblesStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    
    @Given("^I create an ensemble$")
    public void I_create_an_ensemble() throws Throwable {
        I_create_an_ensemble_of_models(2);
    }
    
    @Given("^I create an ensemble of (\\d+) models$")
    public void I_create_an_ensemble_of_models(int numberOfModels) 
    		throws Throwable {
    	
    	JSONObject args = new JSONObject();
    	args.put("number_of_models", numberOfModels);
    	I_create_an_ensemble_with_params(args.toString());
    }
    
    
    @Given("^I create an ensemble with \"(.*)\"$")
    public void I_create_an_ensemble_with_params(String params) 
    		throws Throwable {
        
    	JSONObject ensembleSample = new JSONObject();
        ensembleSample.put("rate", 0.70);
        ensembleSample.put("seed", "BigML");
        
        JSONObject args = (JSONObject) JSONValue.parse(params);
        args.put("ensemble_sample", ensembleSample);

    	commonSteps.I_create_a_resource_from_a_dataset_with(
    		"ensemble", args.toString());
    }
    
}
