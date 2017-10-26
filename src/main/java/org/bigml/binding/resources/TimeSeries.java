package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete timeseries.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/timeseries
 *
 *
 */
public class TimeSeries extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(TimeSeries.class);

    /**
     * Constructor
     *
     */
    public TimeSeries() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public TimeSeries(final String apiUser, final String apiKey,
            final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(null);
    }


    /**
     * Constructor
     *
     */
    public TimeSeries(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(cacheManager);
    }

    /**
     * Check if the current resource is a TimeSeries
     *
     * @param resource the resource to be checked
     * @return true if it's a TimeSeries
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(TIMESERIES_RE);
    }

    /**
     * Creates a timeseries from a `dataset`.
     *
     * POST /andromeda/timeseries?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            timeseries.
     * @param args
     *            set of parameters for the new timeseries. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the timeseries. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(TIMESERIES_URL, requestObject.toJSONString());
    }

    /**
     * Creates a timeseries from a list of `datasets`.
     *
     * POST /andromeda/timeseries?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            timeseries.
     * @param args
     *            set of parameters for the new timeseries. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the timeseries. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final List datasetsIds, String args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(TIMESERIES_URL, requestObject.toJSONString());
    }

    /**
     * Creates a timeseries from a list of `datasets`.
     *
     * POST /andromeda/timeseries?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            timeseries.
     * @param args
     *            set of parameters for the new timeseries. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the timeseries. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(TIMESERIES_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves a timeseries.
     *
     * GET /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String timeSeriesId) {
        if (timeSeriesId == null || timeSeriesId.length() == 0
                || !timeSeriesId.matches(TIMESERIES_RE)) {
            logger.info("Wrong timeseries id");
            return null;
        }

        return getResource(BIGML_URL + timeSeriesId);
    }

    /**
     * Retrieves a timeseries.
     *
     * GET /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param timeSeries
     *            a timeseries JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject timeSeries) {
        String timeSeriesId = (String) timeSeries.get("resource");
        return get(timeSeriesId);
    }


    /**
     * Checks whether a timeseries' status is FINISHED.
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String timeSeriesId) {
        return isResourceReady(get(timeSeriesId));
    }

    /**
     * Checks whether a timeseries status is FINISHED.
     *
     * @param timeSeries
     *            a timeseries JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject timeSeries) {
        return isResourceReady(timeSeries)
                || isReady((String) timeSeries.get("resource"));
    }

    /**
     * Lists all your timeseries.
     *
     * GET /andromeda/timeseries?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(TIMESERIES_URL, queryString);
    }

    /**
     * Updates a timeseries.
     *
     * PUT /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the timeseries. Optional
     *
     */
    @Override
    public JSONObject update(final String timeSeriesId, final String changes) {
        if (timeSeriesId == null || timeSeriesId.length() == 0
                || !(timeSeriesId.matches(TIMESERIES_RE))) {
            logger.info("Wrong timeseries id");
            return null;
        }
        return updateResource(BIGML_URL + timeSeriesId, changes);
    }

    /**
     * Updates a timeseries.
     *
     * PUT /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param timeSeries
     *            a timeseries JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject timeSeries, final JSONObject changes) {
        String resourceId = (String) timeSeries.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a timeseries.
     *
     * DELETE
     * /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String timeSeriesId) {
        if (timeSeriesId == null || timeSeriesId.length() == 0
                || !(timeSeriesId.matches(TIMESERIES_RE))) {
            logger.info("Wrong timeseries id");
            return null;
        }
        return deleteResource(BIGML_URL + timeSeriesId);
    }

    /**
     * Deletes a timeseries.
     *
     * DELETE
     * /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param timeSeries
     *            a timeseries JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject timeSeries) {
        String resourceId = (String) timeSeries.get("resource");
        return delete(resourceId);
    }

}
