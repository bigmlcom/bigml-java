package org.bigml.binding;

import cucumber.annotation.en.Then;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

import org.bigml.binding.utils.Utils;


public class TimeSeriesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(TimeSeriesStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Then("^I create a forecast for \"(.*)\"$")
    public void I_create_a_forecast(String inputData) throws Throwable {
        String timeSeries = (String) context.timeSeries.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createForecast(
            timeSeries, (JSONObject) JSONValue.parse(inputData), args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.forecast = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Then("^the forecasts are \"(.*)\"$")
    public void the_forecasts_are(String forecast) throws Throwable {
        JSONObject expectedResult = (JSONObject) JSONValue.parse(forecast);
        JSONObject forecastResult = (JSONObject)
            Utils.getJSONObject(context.forecast, "forecast.result");

        assertEquals(expectedResult, forecastResult);
    }

}