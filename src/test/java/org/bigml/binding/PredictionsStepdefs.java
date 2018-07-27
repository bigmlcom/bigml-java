package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.*;

import org.bigml.binding.localmodel.Prediction;
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

public class PredictionsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(PredictionsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @When("^I create a proportional missing strategy prediction for \"(.*)\"$")
    public void I_create_a_proportional_missing_strategy_prediction(String inputData)
            throws AuthenticationException {
        String modelId = (String) context.model.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("missing_strategy", 1);

        JSONObject resource = context.api.createPrediction(
            modelId, (JSONObject) JSONValue.parse(inputData), args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @When("^I create a proportional missing strategy prediction with ensemble with \"(.*)\" for \"(.*)\"$")
    public void I_create_a_proportional_missing_strategy_prediction_with_ensemble(String data, String inputData)
            throws AuthenticationException {
        String ensembleId = (String) context.ensemble.get("resource");
        
        JSONObject args = (JSONObject) JSONValue.parse(data);
        args.put("tags", Arrays.asList("unitTest"));
        args.put("missing_strategy", 1);
        
        JSONObject resource = context.api.createPrediction(
        		ensembleId, (JSONObject) JSONValue.parse(inputData), args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create a prediction for \"(.*)\"$")
    public void I_create_a_prediction(String inputData)
            throws AuthenticationException {
        String modelId = (String) context.model.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = context.api.createPrediction(
            modelId, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Then("^the numerical prediction for \"([^\"]*)\" is ([\\d,.]+)$")
    public void the_numerical_prediction_for_is(String objectiveField, double pred) {
        JSONObject obj = (JSONObject) context.prediction.get("prediction");
        String predictionValue = String.format("%.5g", ((Double) obj.get(objectiveField)));

        assertEquals(String.format("%.5g", pred), predictionValue);
    }

    @Then("^the prediction for \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_prediction_for_is(String objectiveField, String pred) {
        JSONObject obj = (JSONObject) context.prediction.get("prediction");
        
        Object prediction = obj.get(objectiveField);
        if( prediction instanceof Number ) { // Regression
            Double expected = Double.parseDouble(pred);
            assertEquals(String.format("%.4g", expected), String.format("%.4g", prediction));
        } else {
            assertTrue("", prediction != null && prediction.equals(pred));
        }
    }

    @Then("^the confidence for the prediction is ([\\d,.]+)$")
    public void the_confidence_for_the_prediction_is(Double expectedConfidence) {
    	Double actualConfidence = (Double) context.prediction.get("confidence");
    	if (actualConfidence == null) {
    		actualConfidence = (Double) context.prediction.get("probability");
    	}
        assertEquals(String.format("%.4g", expectedConfidence), String.format("%.4g",actualConfidence));
        
    }
    
    @Then("^the probability for the prediction is ([\\d,.]+)$")
    public void the_probability_for_the_prediction_is(Double expected) {
    	Double probability = (Double) context.prediction.get("probability");
    	if (probability == null) {
    		probability = (Double) context.prediction.get("confidence");
    	}
        assertEquals(String.format("%.4g", expected), String.format("%.4g",probability));
        
    }
    
    // Models
    
    @When("^I create a prediction with model with operating point \"(.*)\" for \"(.*)\"$")
    public void I_create_a_prediction_with_model_with_operating_point(
    		String operatingPoint, String inputData)
            throws AuthenticationException {

    	String modelId = (String) context.model.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("operating_point", (JSONObject) JSONValue.parse(operatingPoint));

        JSONObject resource = context.api.createPrediction(
        		modelId, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @When("^I create a local prediction with model with operating point \"(.*)\" for \"(.*)\"$")
    public void I_create_a_local_prediction_with_model_with_operating_point(
    		String operatingPoint, String inputData)
            throws Exception {
    	
    	JSONObject data = (JSONObject) JSONValue.parse(inputData);  	
    	try {
    		Prediction prediction = context.localModel.predict(
	    			data, null, (JSONObject) JSONValue.parse(operatingPoint), null, true, null);
	    	
	        context.localModelPrediction = prediction;
    	} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    
    @When("^I create a prediction with model with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_prediction_with_model_with_operating_kind(
    		String kind, String inputData)
            throws AuthenticationException {

    	String modelId = (String) context.model.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("operating_kind", kind);

        JSONObject resource = context.api.createPrediction(
        		modelId, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    
    @When("^I create a local prediction with model with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_local_prediction_with_model_with_operating_kind(
    		String kind, String inputData)
            throws Exception {

    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
    	Prediction prediction = context.localModel.predict(
    			data, null, null, kind, true);
    	
        context.localModelPrediction = prediction;
    }
    
    @Then("^the local model prediction is \"([^\"]*)\"$")
    public void the_local_model_prediction_is(String prediction) 
    		throws Throwable {
    	
    	if (context.localModelPrediction.get("prediction") instanceof String) {
    		assertTrue(prediction.equals((String) context.localModelPrediction.get("prediction")));
    	} else {
    		double result = (Double) context.localModelPrediction.get("probability");
    		double expected = Double.parseDouble(prediction);
    		assertTrue(expected == Utils.roundOff(result, 5));
    	}
    	
    }
    
    
    // Ensembles
    
    @When("^I create a prediction with ensemble for \"(.*)\"$")
    public void I_create_a_prediction_with_ensemble_for(String inputData) throws AuthenticationException {
        String ensembleId = (String) context.ensemble.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = context.api.createPrediction(
            ensembleId, (JSONObject) JSONValue.parse(inputData), args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Then("^the prediction with ensemble for \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_prediction_with_ensemble_for_is(String expected, String pred) {
        JSONObject obj = (JSONObject) context.prediction.get("prediction");
        String objective = (String) obj.get(expected);
        assertEquals(pred, objective);
    }
    
    @When("^I create a prediction with ensemble with operating point \"(.*)\" for \"(.*)\"$")
    public void I_create_a_prediction_with_ensemble_with_operating_point(
    		String operatingPoint, String inputData)
            throws AuthenticationException {

    	String ensembleId = (String) context.ensemble.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("operating_point", (JSONObject) JSONValue.parse(operatingPoint));

        JSONObject resource = context.api.createPrediction(
        		ensembleId, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @When("^I create a local prediction with ensemble with operating point \"(.*)\" for \"(.*)\"$")
    public void I_create_a_local_prediction_with_ensemble_with_operating_point(
    		String operatingPoint, String inputData)
            throws Exception {
    	
    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
    	
    	try {
	    	JSONObject prediction = context.localEnsemble
	                .predict(data, null, null, null,
	                		(JSONObject) JSONValue.parse(operatingPoint), 
	                		null, true, true);
	    	
	        context.localPrediction = prediction;
    	} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    
    @When("^I create a prediction with ensemble with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_prediction_with_ensemble_with_operating_kind(
    		String kind, String inputData)
            throws AuthenticationException {

    	String ensembleId = (String) context.ensemble.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("operating_kind", kind);

        JSONObject resource = context.api.createPrediction(
        		ensembleId, (JSONObject) JSONValue.parse(inputData), args, 5, null);
        
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    
    @When("^I create a local prediction with ensemble with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_local_prediction_with_ensemble_with_operating_kind(
    		String kind, String inputData)
            throws Exception {

    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
    	JSONObject prediction = context.localEnsemble
                .predict(data, null, null, null,
                        null, kind, true, true);
    	
        context.localPrediction = prediction;
    }
    
    @Then("^the local ensemble prediction is \"([^\"]*)\"$")
    public void the_local_ensemble_prediction_is(String prediction) 
    		throws Throwable {
    	if (context.localPrediction.get("prediction") instanceof String) {
    		assertTrue(prediction.equals((String) context.localPrediction.get("prediction")));
    	} else {
    		double result = (Double) context.localPrediction.get("prediction");
    		double expected = Double.parseDouble(prediction);
    		assertTrue(expected == Utils.roundOff(result, 5));
    	}
    }
    
    
    @Then("^the local ensemble confidence is (.*)$")
    public void the_local_ensemble_confidence_is(Double expectedConfidence) 
    		throws Throwable {
    	
    	Double actualConfidence = (Double) context.localPrediction.get("confidence");
    	if (actualConfidence == null) {
    		actualConfidence = (Double) context.localPrediction.get("probability");
    	}
    	assertEquals(String.format("%.4g", expectedConfidence), String.format("%.4g",actualConfidence));
    	
    }
    
    
    // Logistic Regressions
    
    @When("^I create a prediction with logisticregression with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_prediction_with_logistic_with_operating_kind(
    		String kind, String inputData)
            throws AuthenticationException {

    	String logisticId = (String) context.logisticRegression.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("operating_kind", kind);

        JSONObject resource = context.api.createPrediction(
        		logisticId, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    
    @When("^I create a local prediction with logisticregression with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_local_prediction_with_logistic_with_operating_kind(
    		String kind, String inputData)
            throws AuthenticationException {

    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
    	JSONObject prediction = context.localLogisticRegression.predict(
    			data, null, kind, true);
        context.localPrediction = prediction;
    }
    
    @When("^I create a logisticregression prediction for \"(.*)\"$")
    public void I_create_a_logisticregression_prediction_for(String inputData)
            throws AuthenticationException {

    	String logisticId = (String) context.logisticRegression.get("resource");

    	JSONObject args = new JSONObject();
    	args.put("tags", Arrays.asList("unitTest"));
        
        JSONObject resource = context.api.createPrediction(
        		logisticId, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    
    @Then("^I create a local logisticregression prediction for \"(.*)\"$")
    public void I_create_a_local_logisticregression_prediction_for(String inputData) 
    		throws Throwable {
    	
    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
    	JSONObject prediction = context.localLogisticRegression.predict(
    			data, null, null, true);
    	context.localPrediction = prediction;
    }
    
    @Then("^the logisticregression prediction is \"(.*)\"$")
    public void the_logisticregression_prediction_is(String prediction) 
    		throws Throwable {
    	assertTrue(prediction.equals((String) context.prediction.get("output")));
    }

    @Then("^the logisticregression probability for the prediction is \"([^\"]*)\"$")
    public void the_logisticregression_probability_for_the_prediction_is(double probability) 
    		throws Throwable {
    	Double prob =  (Double) context.prediction.get("probability");
    	assert(Utils.roundOff(prob, 4) == Utils.roundOff(probability, 4));
    	
    }

    @Then("^the local logisticregression prediction is \"([^\"]*)\"$")
    public void the_local_logisticregression_prediction_is(String prediction) 
    		throws Throwable {
    	assertTrue(prediction.equals((String) context.localPrediction.get("prediction")));
    }

    @Then("^the local logisticregression probability for the prediction is \"([^\"]*)\"$")
    public void the_local_logistic_regression_probability_for_the_prediction_is(double probability) 
    		throws Throwable {
    	Double prob =  (Double) context.localPrediction.get("probability");
    	assert(Utils.roundOff(prob, 4) == Utils.roundOff(probability, 4));
    }

    
    // Deepnets
    
    @When("^I create a prediction with deepnet with operating point \"(.*)\" for \"(.*)\"$")
    public void I_create_a_prediction_with_deepnet_with_operating_point(
    		String operatingPoint, String inputData)
            throws AuthenticationException {

    	String deepnetId = (String) context.deepnet.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("operating_point", (JSONObject) JSONValue.parse(operatingPoint));

        JSONObject resource = context.api.createPrediction(
        		deepnetId, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @When("^I create a local prediction with deepnet with operating point \"(.*)\" for \"(.*)\"$")
    public void I_create_a_local_prediction_with_deepnet_with_operating_point(
    		String operatingPoint, String inputData)
            throws AuthenticationException {
    	
    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
    	
    	JSONObject prediction = context.localDeepnet.predict(
    			data, (JSONObject) JSONValue.parse(operatingPoint), null, true);
        context.localPrediction = prediction;
    }
    
    @When("^I create a prediction with deepnet with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_prediction_with_deepnet_with_operating_kind(
    		String kind, String inputData)
            throws AuthenticationException {

    	String deepnetId = (String) context.deepnet.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("operating_kind", kind);

        JSONObject resource = context.api.createPrediction(
        		deepnetId, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    
    @When("^I create a local prediction with deepnet with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_local_prediction_with_deepnet_with_operating_kind(
    		String kind, String inputData)
            throws AuthenticationException {

    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
    	JSONObject prediction = context.localDeepnet.predict(
    			data, null, kind, true);
        context.localPrediction = prediction;
    }
    
    @When("^I create a deepnet prediction for \"(.*)\"$")
    public void I_create_a_deepnet_prediction_for(String inputData)
            throws AuthenticationException {

    	String deepnetId = (String) context.deepnet.get("resource");
    	
    	JSONObject args = new JSONObject();
    	args.put("tags", Arrays.asList("unitTest"));
        
        JSONObject resource = context.api.createPrediction(
        		deepnetId, (JSONObject) JSONValue.parse(inputData), args, 5, null);
        
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @Then("^I create a local deepnet prediction for \"(.*)\"$")
    public void I_create_a_local_deepnet_prediction_for(String inputData) 
    		throws Throwable {
    	
    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
    	JSONObject prediction = context.localDeepnet.predict(
    			data, null, null, true);
    	context.localPrediction = prediction;
    }
    
    @Then("^the deepnet prediction for objective \"([^\"]*)\" is \"(.*)\"$")
    public void the_deepnet_prediction_is(String objective, String prediction) 
    		throws Throwable {
    	
    	JSONObject pred = (JSONObject) context.prediction.get("prediction");
    	if (pred.get(objective) instanceof String) {
    		assertTrue(prediction.equals((String) pred.get(objective)));
    	} else {
    		double result = (Double) pred.get(objective);
    		double expected = Double.parseDouble(prediction);
    		assertTrue(Utils.roundOff(expected,5) == Utils.roundOff(result,5));
    	}
    	
    }

    @Then("^the local deepnet prediction is \"([^\"]*)\"$")
    public void the_local_deepnet_prediction_is(String prediction) 
    		throws Throwable {
    	
    	if (context.localPrediction.get("prediction") instanceof String) {
    		assertTrue(prediction.equals((String) context.localPrediction.get("prediction")));
    	} else {
    		double result = (Double) context.localPrediction.get("probability");
    		double expected = Double.parseDouble(prediction);
    		assertTrue(expected == Utils.roundOff(result, 5));
    	}
    	
    }

    
    @Given("^I combine the votes in \"(.*)\"$")
    public void I_combine_the_votes(String dir)
            throws AuthenticationException {
        try {
            context.votes = context.multiModel.batchVotes(dir, null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Exception combining votes", false);
        }
    }

    @Given("^the plurality combined predictions are \"(.*)\"$")
    public void The_plurality_combined_prediction(String predictionsStr)
            throws AuthenticationException {
        JSONArray predictions = (JSONArray) JSONValue.parse(predictionsStr);
        for (int iVote = 0; iVote < context.votes.size(); iVote++ ) {
            MultiVote vote = context.votes.get(iVote);
            Map<Object,Object> combinedPrediction = vote.combine();
            assertEquals("The predictions are not equals", predictions.get(iVote),
                    combinedPrediction.get("prediction"));
        }
    }

    @Given("^the confidence weighted predictions are \"(.*)\"$")
    public void The_confidence_weighted_prediction(String predictionsStr)
            throws AuthenticationException {
        JSONArray predictions = (JSONArray) JSONValue.parse(predictionsStr);
        for (int iVote = 0; iVote < context.votes.size(); iVote++ ) {
            MultiVote vote = context.votes.get(iVote);
            Map<Object,Object> combinedPrediction = vote.combine(PredictionMethod.CONFIDENCE, null);
            assertEquals("The predictions are not equals", predictions.get(iVote),
                    combinedPrediction.get("prediction"));
        }
    }


    @Then("^I create a local mm median batch prediction using \"(.*)\" with prediction (.*)$")
    public void i_create_a_local_mm_median_batch_prediction_using_with_prediction(String args, Double expectedPrediction)
            throws Exception {
        JSONObject inputData = (JSONObject) JSONValue.parse(args);
        JSONArray inputDataList = new JSONArray();
        inputDataList.add(inputData);
        List<MultiVote> votes = context.multiModel.batchPredict(
        		inputDataList, null, false, MissingStrategy.LAST_PREDICTION,
                null, false, true);

        Double prediction = (Double) votes.get(0).getPredictions()[0].get("prediction");
        assertEquals(expectedPrediction, prediction);
    }
    
    
    
    // Fusions
    
    @When("^I create a prediction with fusion for \"(.*)\"$")
    public void I_create_a_prediction_with_fusion_for(String inputData) throws AuthenticationException {
        String fusionId = (String) context.fusion.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = context.api.createPrediction(
        		fusionId, (JSONObject) JSONValue.parse(inputData), args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @When("^I create a local fusion prediction for \"(.*)\"$")
    public void I_create_a_local_fusion_prediction(String inputData)
            throws Exception {
    	
    	JSONObject data = (JSONObject) JSONValue.parse(inputData);  	
    	try {
	        context.localPrediction = context.localFusion.predict(
	        		data, null, null, true);
    	} catch (Exception e) {
			e.printStackTrace();
		}
        
    }
    
    @Then("^the local fusion prediction is \"([^\"]*)\"$")
    public void the_local_fusion_prediction_is(String prediction) 
    		throws Throwable {
    	if (context.localPrediction.get("prediction") instanceof String) {
    		assertTrue(prediction.equals((String) context.localPrediction.get("prediction")));
    	} else {
    		double result = (Double) context.localPrediction.get("prediction");
    		double expected = Double.parseDouble(prediction);
    		assertTrue(expected == Utils.roundOff(result, 5));
    	}
    }
    
    @Then("^the local prediction probability is (.*)$")
    public void the_local_prediction_probability_is(Double expected) 
    		throws Throwable {
    	
    	Double actual = (Double) context.localPrediction.get("probability");
    	if (actual == null) {
    		actual = (Double) context.localPrediction.get("confidence");
    	}
    	assertEquals(String.format("%.4g", expected), String.format("%.4g",actual));
    	
    }
}