package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Cache;

import java.util.List;

/**
 * Entry point to create, retrieve, list, update, and delete anomaly detectors.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/anomalies
 *
 *
 */
public class Anomaly extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Anomaly.class);

    /**
     * Constructor
     *
     */
    public Anomaly() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        this.bigmlDomain = System.getProperty("BIGML_DOMAIN");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Anomaly(final String apiUser, final String apiKey,
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
    public Anomaly(final String apiUser, final String apiKey,
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
     * Check if the current resource is an Anomaly
     *
     * @param resource the resource to be checked
     * @return true if it's an Anomaly
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(ANOMALY_RE);
    }

    /**
     * Creates a new anomaly.
     *
     * POST /andromeda/anomaly?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars for the dataset to attach the anomaly.
     * @param args
     *            set of parameters for the new anomaly. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the anomaly. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(ANOMALY_URL, requestObject.toJSONString());
    }

    /**
     * Creates an anomaly from a list of `datasets`.
     *
     * POST /andromeda/anomaly?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            anomaly.
     * @param args
     *            set of parameters for the new anomaly. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the anomaly. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(ANOMALY_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String anomalyId) {
        return get(anomalyId, null, null);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String anomalyId, final String apiUser,
            final String apiKey) {
        if (anomalyId == null || anomalyId.length() == 0
                || !(anomalyId.matches(ANOMALY_RE))) {
            logger.info("Wrong anomaly id");
            return null;
        }

        return getResource(BIGML_URL + anomalyId, null, apiUser, apiKey);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomaly
     *            an anomaly JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject anomaly) {
        String resourceId = (String) anomaly.get("resource");
        return get(resourceId, null, null);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomaly
     *            an anomaly JSONObject
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject anomaly, final String apiUser,
            final String apiKey) {
        String resourceId = (String) anomaly.get("resource");
        return get(resourceId, apiUser, apiKey);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String anomalyId, final String queryString) {
        return get(anomalyId, queryString, null, null);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String anomalyId, final String queryString,
            final String apiUser, final String apiKey) {
        if (anomalyId == null || anomalyId.length() == 0
                || !(anomalyId.matches(ANOMALY_RE))) {
            logger.info("Wrong anomaly id");
            return null;
        }

        return getResource(BIGML_URL + anomalyId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomaly
     *            a model JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject anomaly, final String queryString) {
        String resourceId = (String) anomaly.get("resource");
        return get(resourceId, queryString, null, null);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomaly
     *            an anomaly JSONObject
     * @param queryString
     *            query for filtering
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject anomaly, final String queryString,
            final String apiUser, final String apiKey) {
        String resourceId = (String) anomaly.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

    /**
     * Checks whether an anomaly's status is FINISHED.
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String anomalyId) {
        return isResourceReady(get(anomalyId));
    }

    /**
     * Checks whether an anomaly's status is FINISHED.
     *
     * @param anomaly
     *            an anomaly JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject anomaly) {
        return isResourceReady(anomaly)
                || isReady((String) anomaly.get("resource"));
    }

    /**
     * Lists all your anomalies.
     *
     * GET /andromeda/anomaly?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(ANOMALY_URL, queryString);
    }

    /**
     * Updates an anomaly.
     *
     * PUT /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final String anomalyId, final String changes) {
        if (anomalyId == null || anomalyId.length() == 0
                || !(anomalyId.matches(ANOMALY_RE))) {
            logger.info("Wrong anomaly id");
            return null;
        }
        return updateResource(BIGML_URL + anomalyId, changes);
    }

    /**
     * Updates an anomaly.
     *
     * PUT /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomaly
     *            an anomaly JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject anomaly, final JSONObject changes) {
        String resourceId = (String) anomaly.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes an anomaly.
     *
     * DELETE
     * /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String anomalyId) {
        if (anomalyId == null || anomalyId.length() == 0
                || !(anomalyId.matches(ANOMALY_RE))) {
            logger.info("Wrong anomaly id");
            return null;
        }
        return deleteResource(BIGML_URL + anomalyId);
    }

    /**
     * Deletes an anomaly.
     *
     * DELETE
     * /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param anomaly
     *            an anomaly JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject anomaly) {
        String resourceId = (String) anomaly.get("resource");
        return delete(resourceId);
    }

}
