package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.bigml.binding.localmodel.Prediction;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

public class LocalModelsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(LocalModelsStepdefs.class);

    LocalPredictiveModel predictiveModel;

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a local model from a \"(.*)\" file$")
    public void I_create_a_local_model(String jsonModelFile) throws Exception {

        String jsonModel = Utils.readFile(jsonModelFile);
        JSONObject localeModel = (JSONObject) JSONValue.parse(jsonModel);

        predictiveModel = new LocalPredictiveModel(localeModel);
        assertTrue("", predictiveModel != null);
    }

    @Given("^I create a local model$")
    public void I_create_a_local_model() throws Exception {
        predictiveModel = new LocalPredictiveModel(context.model);
        assertTrue("", predictiveModel != null);
    }

    @Then("^the local prediction by name for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_by_name_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = predictiveModel.predict(inputObj);
            String prediction = (String) p.getPrediction();
            assertTrue("", prediction != null && prediction.equals(pred));
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the numerical prediction of proportional missing strategy local prediction for \"(.*)\" is ([\\d,.]+)$")
    public void the_numerical_prediction_of_proportional_missing_strategy_local_predictionfor_is(String args, double expectedPrediction) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = predictiveModel.predict(inputObj, true, MissingStrategy.PROPORTIONAL);
            Double actualPrediction = (Double) p.getPrediction();
            assertEquals(String.format("%.4g%n", expectedPrediction), String.format("%.4g%n", actualPrediction));
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the proportional missing strategy local prediction for \"(.*)\" is \"([^\"]*)\"$")
    public void the_proportional_missing_strategy_local_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = predictiveModel.predict(inputObj, true, MissingStrategy.PROPORTIONAL);
            String prediction = (String) p.getPrediction();
            assertTrue("", prediction != null && prediction.equals(pred));
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the confidence of the proportional missing strategy local prediction for \"(.*)\" is ([\\d,.]+)$")
    public void the_confidence_of_the_missing_strategy_local_predictionfor_is(String args, double expectedConfidence) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = predictiveModel.predict(inputObj, true, MissingStrategy.PROPORTIONAL);
            Double actualConfidence = p.getConfidence();
            assertEquals(String.format("%.4g%n", expectedConfidence), String.format("%.4g%n", actualConfidence));
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the confidence of the local prediction for \"(.*)\" is ([\\d,.]+)$")
    public void the_confidence_of_the_local_prediction_for_is(String args, double expectedConfidence) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = predictiveModel.predict(inputObj, true);
            Double actualConfidence = p.getConfidence();
            assertEquals(String.format("%.4g%n", expectedConfidence), String.format("%.4g%n", actualConfidence));
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the local prediction for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Prediction p = predictiveModel.predict(inputObj, true);
            String prediction = (String) p.getPrediction();
            assertEquals(pred, prediction);
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the multiple local prediction for \"(.*)\" is \"(.*)\"$")
    public void the_multiple_local_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            List<Prediction> predictions = predictiveModel.predict(inputObj, true, "all");
            String predictionsStr = JSONValue.toJSONString(predictions);
            assertEquals(pred, predictionsStr);
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the local prediction by name=(true|false) for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_byname_for_is(String by_name, String args,
            String pred) {
        try {
            Boolean byName = new Boolean(by_name);
            Prediction p = predictiveModel.predict( (JSONObject) JSONValue.parse(args),
                    byName);
            String prediction = (String) p.getPrediction();
            assertEquals(pred, prediction);
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^\"(.*)\" field\'s name is changed to \"(.*)\"$")
    public void field_name_to_new_name(String fieldId, String newName) {
        JSONObject field = (JSONObject) Utils.getJSONObject(
                predictiveModel.fields(), fieldId);
        if (!field.get("name").equals(newName)) {
            field.put("name", newName);
        }
        assertEquals(newName, field.get("name"));
    }

}
