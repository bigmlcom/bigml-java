package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
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

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @When("^I create a prediction by name=(true|false) for \"(.*)\"$")
    public void I_create_a_prediction(String by_name, String inputData)
            throws AuthenticationException {
        String modelId = (String) context.model.get("resource");
        Boolean byName = new Boolean(by_name);
        JSONObject resource = BigMLClient.getInstance().createPrediction(
                modelId, (JSONObject) JSONValue.parse(inputData), byName,
                new JSONObject(), 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I get the prediction \"(.*)\"")
    public void I_get_the_prediction(String predictionId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getPrediction(
                predictionId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.prediction = (JSONObject) resource.get("object");
    }

    @Then("^the prediction for \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_prediction_for_is(String expected, String pred) {
        JSONObject obj = (JSONObject) context.prediction.get("prediction");
        String objective = (String) obj.get(expected);
        assertEquals(objective, pred);
    }

    @When("^I create a prediction with ensemble by name=(true|false) for \"(.*)\"$")
    public void I_create_a_prediction_with_ensemble_for(String by_name,
            String inputData) throws AuthenticationException {
        String ensembleId = (String) context.ensemble.get("resource");
        Boolean byName = new Boolean(by_name);
        JSONObject resource = BigMLClient.getInstance().createPrediction(
                ensembleId, (JSONObject) JSONValue.parse(inputData), byName,
                new JSONObject(), 5, null);
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

    @Given("^I wait until the predition status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_prediction_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.prediction.get("status"))
                .get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_prediction((String) context.prediction.get("resource"));
            code = (Long) ((JSONObject) context.prediction.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the prediction is ready less than (\\d+) secs$")
    public void I_wait_until_the_prediction_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_prediction_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }
}