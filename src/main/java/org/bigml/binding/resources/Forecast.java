package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete forecasts.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/forecasts
 *
 *
 */
public class Forecast extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Forecast.class);

    /**
     * Constructor
     *
     */
    public Forecast() {
    		super.init(null, null, false, null);
        this.resourceRe = FORECAST_RE;
        this.resourceUrl = FORECAST_URL;
        this.resourceName = "forecast";
    }

    /**
     * Constructor
     *
     */
    public Forecast(final String apiUser, final String apiKey,
            final boolean devMode) {
    		super.init(apiUser, apiKey, devMode, null);
        this.resourceRe = FORECAST_RE;
        this.resourceUrl = FORECAST_URL;
        this.resourceName = "forecast";
    }


    /**
     * Constructor
     *
     */
    public Forecast(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, devMode, cacheManager);
        this.resourceRe = FORECAST_RE;
        this.resourceUrl = FORECAST_URL;
        this.resourceName = "forecast";
    }

    /**
     * Creates a forecast from a timeseries.
     *
     * POST /andromeda/forecast?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars for the timeseries to attach
     *            the forecast.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a forecast for.
     * @param args
     *            set of parameters for the new forecast. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the forecast.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String timeSeriesId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {

        if (timeSeriesId == null || timeSeriesId.length() == 0 ) {
            logger.info("Wrong timeseries id. Id cannot be null");
            return null;
        }

        try {
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;
            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .timeSeriesIsReady(timeSeriesId)) {
                    Thread.sleep(waitTime);
                    count++;
                }
            }

            // Input data
            JSONObject inputDataJSON = null;
            if (inputData == null) {
                inputDataJSON = new JSONObject();
            } else {
                inputDataJSON = inputData;
            }

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            requestObject.put("timeseries", timeSeriesId);
            requestObject.put("input_data", inputData);

            return createResource(FORECAST_URL,
                                  requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating timeseries");
            return null;
        }
    }

}
