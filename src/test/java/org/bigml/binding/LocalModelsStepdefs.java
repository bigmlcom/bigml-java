package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bigml.binding.localmodel.Prediction;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class LocalModelsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(LocalModelsStepdefs.class);

    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a local model from a \"(.*)\" file$")
    public void I_create_a_local_model(String jsonModelFile) throws Exception {

        String jsonModel = Utils.readFile(jsonModelFile);
        JSONObject localModel = (JSONObject) JSONValue.parse(jsonModel);
        
        context.localModel = new LocalPredictiveModel(localModel);
        assertTrue("", context.localModel != null);
    }

    @Given("^I create a local model$")
    public void I_create_a_local_model() throws Exception {
    	context.localModel = new LocalPredictiveModel(context.model);
        assertTrue("", context.localModel != null);
    }

    @Then("^the local prediction by name for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = context.localModel.predict(inputObj);
            String prediction = (String) p.getPrediction();
            assertTrue("", prediction != null && prediction.equals(pred));
        } catch (Exception e) {
            assertTrue("", false);
        }
    }

    @Then("^the numerical prediction of proportional missing strategy local prediction for \"(.*)\" is ([\\d,.]+)$")
    public void the_numerical_prediction_of_proportional_missing_strategy_local_predictionfor_is(String args, double expectedPrediction) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = context.localModel.predict(inputObj, MissingStrategy.PROPORTIONAL);
            Double actualPrediction = (Double) p.getPrediction();
            assertEquals(String.format("%.4g%n", expectedPrediction), String.format("%.4g%n", actualPrediction));
        } catch (Exception e) {
            assertTrue("", false);
        }
    }

    @Then("^the proportional missing strategy local prediction for \"(.*)\" is \"([^\"]*)\"$")
    public void the_proportional_missing_strategy_local_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = context.localModel.predict(inputObj, MissingStrategy.PROPORTIONAL);
            String prediction = (String) p.getPrediction();
            assertTrue("", prediction != null && prediction.equals(pred));
        } catch (Exception e) {
            assertTrue("", false);
        }
    }

    @Then("^the confidence of the proportional missing strategy local prediction for \"(.*)\" is ([\\d,.]+)$")
    public void the_confidence_of_the_missing_strategy_local_predictionfor_is(String args, double expectedConfidence) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = context.localModel.predict(inputObj, MissingStrategy.PROPORTIONAL);
            Double actualConfidence = p.getConfidence();
            assertEquals(String.format("%.4g%n", expectedConfidence), String.format("%.4g%n", actualConfidence));
        } catch (Exception e) {
            assertTrue("", false);
        }
    }

    @Then("^the confidence of the local prediction for \"(.*)\" is ([\\d,.]+)$")
    public void the_confidence_of_the_local_prediction_for_is(String args, double expectedConfidence) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = context.localModel.predict(inputObj);
            Double actualConfidence = p.getConfidence();
            assertEquals(String.format("%.4g%n", expectedConfidence), String.format("%.4g%n", actualConfidence));
        } catch (Exception e) {
            assertTrue("", false);
        }
    }

    @Then("^the local prediction for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_model_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = context.localModel.predict(inputObj);

            if (p.getPrediction() instanceof String) {
                String prediction = (String) p.getPrediction();
                assertEquals(pred, prediction);
            } else {
                Double prediction = (Double) p.getPrediction();
                assertEquals(new Double(pred), prediction);
            }
        } catch (Exception e) {
        	assertTrue("", false);
        }
    }

    @Then("^the multiple local prediction for \"(.*)\" is \"(.*)\"$")
    public void the_multiple_local_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            List<Prediction> predictions = context.localModel.predict(inputObj, "all");
            String predictionsStr = JSONValue.toJSONString(predictions);
            
            JSONArray expected = (JSONArray) JSONValue.parse(pred);
            JSONArray was = (JSONArray) JSONValue.parse(predictionsStr);
            assertEquals(expected, was);
        } catch (InputDataParseException parseException) {
        	parseException.printStackTrace();
            assertTrue("", false);
        }
    }

    @Then("^\"(.*)\" field\'s name is changed to \"(.*)\"$")
    public void field_name_to_new_name(String fieldId, String newName) {
        JSONObject field = (JSONObject) Utils.getJSONObject(
        		context.localModel.fields(), fieldId);
        if (!field.get("name").equals(newName)) {
            field.put("name", newName);
        }
        assertEquals(newName, field.get("name"));
    }

}
