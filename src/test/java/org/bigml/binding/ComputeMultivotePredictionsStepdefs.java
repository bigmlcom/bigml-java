package org.bigml.binding;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class ComputeMultivotePredictionsStepdefs {

	// Logging
	Logger logger = LoggerFactory.getLogger(ComputeMultivotePredictionsStepdefs.class);

	MultiVote multivote;
	HashMap<Object, Object> combinedPrediction;


	@Given("^I create a MultiVote for the set of predictions in file (.*)$")
	public void i_create_a_multivote(String predictionsFile) throws Throwable {
	    try {
	    	String json = Utils.readFile(predictionsFile);
	    	JSONArray jsonArray =  (JSONArray) JSONValue.parse(json);

	    	HashMap<Object, Object>[] exampleArray = (HashMap<Object, Object>[]) new HashMap[jsonArray.size()];
	    	for (int i=0; i<jsonArray.size(); i++) {
	    		JSONObject item = (JSONObject) jsonArray.get(i);

	    		HashMap <Object, Object> prediction = new HashMap<Object, Object>();
	    		prediction.put("prediction", item.get("prediction"));
		        prediction.put("confidence", item.get("confidence"));
		        prediction.put("count", item.get("count"));

		        JSONArray distributionArray = (JSONArray) item.get("distribution");
		        HashMap<Object, Integer> distributionHash = new HashMap<Object, Integer>();
		        for (int j=0; j<distributionArray.size(); j++) {
		    		JSONArray dist = (JSONArray) distributionArray.get(j);
		    		distributionHash.put(dist.get(0), ((Long)dist.get(1)).intValue());
		        }
		        prediction.put("distribution", distributionHash);

		        exampleArray[i] = prediction;
	    	}

	        // build multivote
	        multivote = new MultiVote(exampleArray);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}


	@When("^I compute the prediction with confidence using method \"([^\"]*)\"$")
	public void I_compute_the_prediction_with_confidence_using_method(String method) throws Throwable {
	    try {
	    	combinedPrediction =  multivote.combine(new Integer(method), true);
	    } catch (Exception e) {
	    	assertTrue("" == "Incorrect method");
	    }
	}


	@Then("^the combined prediction is \"([^\"]*)\"$")
	public void the_combined_prediction_is(String prediction) throws Throwable {
		assertTrue(combinedPrediction.get("prediction").equals(prediction));
	}


	@Then("^the numerical combined prediction is (.*)$")
	public void the_numerical_combined_prediction_is(double prediction) throws Throwable {
		String predictionValue = String.format("%.12g%n", ((Double)combinedPrediction.get("prediction")));
		assertTrue(predictionValue.equals(String.format("%.12g%n", prediction)));
	}


	@Then("^the confidence for the combined prediction is (.*)$")
	public void the_confidence_for_the_combined_prediction_is_(double confidence) throws Throwable {
		String confidenceValue = String.format("%.12g%n", ((Number)combinedPrediction.get("confidence")).doubleValue());
		assertTrue(confidenceValue.equals(String.format("%.12g%n", confidence)));
	}

}
