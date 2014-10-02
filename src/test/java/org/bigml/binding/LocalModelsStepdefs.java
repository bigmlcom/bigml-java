package org.bigml.binding;

import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

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

    @Given("^I create a local model$")
    public void I_create_a_local_model() throws Exception {
        predictiveModel = new LocalPredictiveModel(context.model);
        assertTrue("", predictiveModel != null);
    }

    @Then("^the local prediction by name for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_by_name_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            HashMap<Object, Object> p = predictiveModel
                    .predict(inputObj, false);
            String prediction = (String) p.get("prediction");
            assertTrue("", prediction != null && prediction.equals(pred));
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the local prediction for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            HashMap<Object, Object> p = predictiveModel.predict(inputObj, null);
            String prediction = (String) p.get("prediction");
            assertTrue("", prediction != null && prediction.equals(pred));
        } catch (InputDataParseException parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the local prediction by name=(true|false) for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_byname_for_is(String by_name, String args,
            String pred) {
        try {
            Boolean byName = new Boolean(by_name);
            HashMap<Object, Object> p = predictiveModel.predict( (JSONObject) JSONValue.parse(args),
                    byName);
            String prediction = (String) p.get("prediction");
            assertTrue("", prediction != null && prediction.equals(pred));
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
        assertTrue("", field.get("name").equals(newName));
    }

}
