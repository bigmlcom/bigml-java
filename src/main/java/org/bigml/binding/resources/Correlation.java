package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used by the BigML class as a mixin that provides the Correlation' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/correlations
 *
 *
 */
public class Correlation extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Correlation.class);

    /**
     * Constructor
     *
     */
    public Correlation() {
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
    public Correlation(final String apiUser, final String apiKey,
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
    public Correlation(final String apiUser, final String apiKey,
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
     * Check if the current resource is a Correlation
     *
     * @param resource the resource to be checked
     * @return true if its an Correlation
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(CORRELATION_RE);
    }


    /**
     * Creates a correlation from a `dataset`.
     *
     * POST /andromeda/correlation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            correlation.
     * @param args
     *            set of parameters for the new correlation. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the correlation. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(CORRELATION_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves a correlation.
     *
     * GET
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String correlationId) {
        if (correlationId == null || correlationId.length() == 0
                || !correlationId.matches(CORRELATION_RE)) {
            logger.info("Wrong correlation id");
            return null;
        }

        return getResource(BIGML_URL + correlationId);
    }

    /**
     * Retrieves a correlation.
     *
     * GET
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param correlation
     *            a correlation JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject correlation) {
        String resourceId = (String) correlation.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves a correlation.
     *
     * GET
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String correlationId, final String queryString) {
        if (correlationId == null || correlationId.length() == 0
                || !correlationId.matches(CORRELATION_RE)) {
            logger.info("Wrong correlation id");
            return null;
        }

        return getResource(BIGML_URL + correlationId, queryString);
    }

    /**
     * Retrieves a correlation.
     *
     * GET
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param correlation
     *            a correlation JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject correlation, final String queryString) {
        String resourceId = (String) correlation.get("resource");
        return get(resourceId, queryString);
    }

    /**
     * Checks whether a correlation's status is FINISHED.
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String correlationId) {
        return isResourceReady(get(correlationId));
    }

    /**
     * Checks whether a correlation status is FINISHED.
     *
     * @param correlation
     *            a correlation JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject correlation) {
        return isResourceReady(correlation)
                || isReady((String) correlation.get("resource"));
    }

    /**
     * Lists all your correlation.
     *
     * GET /andromeda/correlation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(CORRELATION_URL, queryString);
    }

    /**
     * Updates a correlation.
     *
     * PUT
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the correlation. Optional
     *
     */
    @Override
    public JSONObject update(final String correlationId, final String changes) {
        if (correlationId == null || correlationId.length() == 0
                || !(correlationId.matches(CORRELATION_RE))) {
            logger.info("Wrong correlation id");
            return null;
        }
        return updateResource(BIGML_URL + correlationId, changes);
    }

    /**
     * Updates a correlation.
     *
     * PUT
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param correlation
     *            a correlation JSONObject
     * @param changes
     *            set of parameters to update the correlation. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject correlation, final JSONObject changes) {
        String resourceId = (String) correlation.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a correlation.
     *
     * DELETE
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String correlationId) {
        if (correlationId == null || correlationId.length() == 0
                || !(correlationId.matches(CORRELATION_RE))) {
            logger.info("Wrong correlation id");
            return null;
        }
        return deleteResource(BIGML_URL + correlationId);
    }

    /**
     * Deletes a correlation.
     *
     * DELETE
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param correlation
     *            a correlation JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject correlation) {
        String resourceId = (String) correlation.get("resource");
        return delete(resourceId);
    }
}