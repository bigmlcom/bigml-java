package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;


public class TimeSeriesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(TimeSeriesStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;


    @Given("^I create time series from a dataset$")
    public void I_create_time_series_from_a_dataset()
        throws AuthenticationException {

        String datasetId = (String) context.dataset.get("resource");
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        JSONObject resource = BigMLClient.getInstance().createTimeSeries(
            datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.timeSeries = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }


    @Given("^I wait until the time series is ready less than (\\d+) secs$")
    public void I_wait_until_the_time_series_is_ready_less_than_secs(int secs)
        throws AuthenticationException {

        I_wait_until_time_series_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I wait until the time series status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_time_series_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.timeSeries.get("status")).get("code");

        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_time_series((String) context.timeSeries.get("resource"));
            code = (Long) ((JSONObject) context.timeSeries.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I get the time series \"(.*)\"")
    public void I_get_the_time_series(String timeSeriesId)
    throws AuthenticationException {

        JSONObject resource =
            BigMLClient.getInstance().getTimeSeries(timeSeriesId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.timeSeries = (JSONObject) resource.get("object");
    }

    @Given("^I update the time series name to \"([^\"]*)\"$")
    public void I_update_the_time_series_name_to(String timeSeriesName)
        throws AuthenticationException {

        String timeSeriesId = (String) context.timeSeries.get("resource");

        JSONObject args = new JSONObject();
        args.put("name", timeSeriesName);

        JSONObject resource = BigMLClient.getInstance().updateTimeSeries(
                timeSeriesId, args.toString());
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.timeSeries = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Then("^the time series name is \"([^\"]*)\"$")
    public void the_time_series_name_is(String expectedName)
        throws AuthenticationException {

        assertEquals(expectedName, context.timeSeries.get("name"));
    }

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