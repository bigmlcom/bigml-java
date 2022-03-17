package org.bigml.binding;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

import org.bigml.binding.utils.Utils;

import io.cucumber.java.en.Then;

public class TimeSeriesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(TimeSeriesStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    private JSONObject cleanUpForecast(JSONObject r) throws Throwable {

        if (r == null)
            return new JSONObject();
        JSONObject rc = new JSONObject();
        for (Object k: r.keySet()) {
            JSONArray v = (JSONArray)r.get((String)k);
            JSONArray vc = new JSONArray();
            for (Object o: v) {
                JSONObject oc = new JSONObject();
                for (String s: new String[]{"point_forecast", "model"}) {
                    oc.put(s, ((JSONObject) o).get(s));
                }
                vc.add(oc);
            }
            rc.put(k, vc);
        }
        return rc;
    }

    @Then("^I create a forecast for \"(.*)\"$")
    public void I_create_a_forecast(String inputData) throws Throwable {
        String timeseries = (String) context.timeSeries.get("resource");

        JSONObject args = commonSteps.setProject(null);

        JSONObject resource =
        		context.api.createForecast(timeseries,
                                           (JSONObject)JSONValue.parse(inputData), 
                                           args, 
                                           5, 
                                           null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.forecast = (JSONObject)resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Then("^I create a local timeseries$")
    public void I_create_a_local_timeseries() throws Exception {
        context.localTimeSeries = new LocalTimeseries(context.timeSeries);
    }

    @Then("^I create a local forecast for \"(.*)\"$")
    public void I_create_a_local_forecast_for(String inputData) 
        throws Throwable {
    	
    	JSONObject data = (JSONObject) JSONValue.parse(inputData);
        Object f = context.localTimeSeries.forecast(data);
    	JSONObject forecasts = (JSONObject)JSONValue.parse(JSONValue.toJSONString(f));
    	context.localForecast = forecasts;
    }
       
    @Then("^the forecasts are \"(.*)\"$")
    public void the_forecasts_are(String forecast) throws Throwable {
        JSONObject expectedResult = (JSONObject) JSONValue.parse(forecast);
        JSONObject forecastResult = (JSONObject)
            Utils.getJSONObject(context.forecast, "forecast.result");

        assertEquals(expectedResult, cleanUpForecast(forecastResult));
    }

    @Then("^the local forecasts are \"(.*)\"$")
    public void the_local_forecasts_are(String forecast) throws Throwable {
        JSONObject expectedResult = (JSONObject) JSONValue.parse(forecast);
        JSONObject forecastResult = context.localForecast;

        assertEquals(expectedResult, cleanUpForecast(forecastResult));
    }

}
