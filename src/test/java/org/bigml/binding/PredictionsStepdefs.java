package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.*;

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

        JSONObject resource = BigMLClient.getInstance().createPrediction(
            modelId, (JSONObject) JSONValue.parse(inputData), args, 5, null);
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

        JSONObject resource = BigMLClient.getInstance().createPrediction(
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
        String objective = (String) obj.get(objectiveField);
        assertEquals(pred, objective);
    }

    @Then("^the confidence for the prediction is ([\\d,.]+)$")
    public void the_confidence_for_the_prediction_is(Double expectedConfidence) {
        Double actualConfidence = (Double) context.prediction.get("confidence");
        assertEquals(String.format("%.4g", expectedConfidence), String.format("%.4g",actualConfidence));
    }

    @When("^I create a prediction with ensemble for \"(.*)\"$")
    public void I_create_a_prediction_with_ensemble_for(String inputData) throws AuthenticationException {
        String ensembleId = (String) context.ensemble.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createPrediction(
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
    
    
    @When("^I create a prediction with logisticregression with operating kind \"(.*)\" for \"(.*)\"$")
    public void I_create_a_prediction_with_logistic_with_operating_kind(
    		String kind, String inputData)
            throws AuthenticationException {

    	String logisticId = (String) context.logisticRegression.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("operating_kind", kind);

        JSONObject resource = BigMLClient.getInstance().createPrediction(
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
    			data, null, kind, true, true);
        context.localPrediction = prediction;
    }


    @When("^I create a logisticregression prediction for \"(.*)\"$")
    public void I_create_a_logisticregression_prediction_for(String inputData)
            throws AuthenticationException {

    	String logisticId = (String) context.logisticRegression.get("resource");

    	JSONObject args = new JSONObject();
    	args.put("tags", Arrays.asList("unitTest"));
        
        JSONObject resource = BigMLClient.getInstance().createPrediction(
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
    			data, null, null, true, true);
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
            Map<Object,Object> combinedPrediction = vote.combine(PredictionMethod.CONFIDENCE, false,
                    null, null, null, null, null);
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
        List<MultiVote> votes = context.multiModel.batchPredict(inputDataList, null, true, false, MissingStrategy.LAST_PREDICTION,
                null, false, true);

        Double prediction = (Double) votes.get(0).getPredictions()[0].get("prediction");
        assertEquals(expectedPrediction, prediction);
    }

}