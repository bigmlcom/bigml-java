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
     * @param bigmlClient	the client with connection to BigML
     * @param apiUser		API user
     * @param apiKey		API key
     * @param project		project id
     * @param organization	organization id
     * @param cacheManager	cache manager
     */
    public Forecast(final BigMLClient bigmlClient,
    				final String apiUser, final String apiKey, 
    				final String project, final String organization,
    				final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization,
    				   cacheManager, FORECAST_RE, FORECAST_PATH);
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
     * @return a JSONObject for the new forecast
     */
    public JSONObject create(final String timeSeriesId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {

        if (timeSeriesId == null || timeSeriesId.length() == 0 ) {
            logger.info("Wrong timeseries id. Id cannot be null");
            return null;
        }

        try {
        	waitForResource(timeSeriesId, "timeSeriesIsReady", waitTime, retries);
        	
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            requestObject.put("timeseries", timeSeriesId);
            requestObject.put("input_data", inputData);

            return createResource(resourceUrl,
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating timeseries");
            return null;
        }
    }

}
