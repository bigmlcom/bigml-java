package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete LogisticRegression.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/logisticregressions
 *
 *
 */
public class LogisticRegression extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(LogisticRegression.class);

    /**
     * Constructor
     *
     */
    public LogisticRegression() {
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
    public LogisticRegression(final String apiUser, final String apiKey,
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
    public LogisticRegression(final String apiUser, final String apiKey,
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
     * Check if the current resource is a LogisticRegression
     *
     * @param resource the resource to be checked
     * @return true if it's a LogisticRegression
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(LOGISTICREGRESSION_RE);
    }

    /**
     * Creates a new logisticregression.
     *
     * POST /andromeda/logisticregression?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars for the dataset to attach the
     *            logisticregression.
     * @param args
     *            set of parameters for the new logisticregression. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for dataset before to start to create the logisticregression. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {
        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(LOGISTICREGRESSION_URL, requestObject.toJSONString());
    }


    /**
     * Creates an logisticregression from a list of `datasets`.
     *
     * POST /andromeda/logisticregression?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            logisticregression.
     * @param args
     *            set of parameters for the new logisticregression. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the logisticregression. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(LOGISTICREGRESSION_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves an logisticregression.
     *
     * GET
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param logisticregressionId
     *            a unique identifier in the form logisticregression/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String logisticregressionId) {
        if (logisticregressionId == null || logisticregressionId.length() == 0
                || !logisticregressionId.matches(LOGISTICREGRESSION_RE)) {
            logger.info("Wrong logisticregression id");
            return null;
        }

        return getResource(BIGML_URL + logisticregressionId);
    }

    /**
     * Retrieves a logisticregression.
     *
     * GET
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param logisticregression
     *            a logisticregression JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject logisticregression) {
        String resourceId = (String) logisticregression.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves a logisticregression.
     *
     * GET
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param logisticregressionId
     *            a unique identifier in the form logisticregression/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String logisticregressionId, final String queryString) {
        if (logisticregressionId == null || logisticregressionId.length() == 0
                || !logisticregressionId.matches(LOGISTICREGRESSION_RE)) {
            logger.info("Wrong logisticregression id");
            return null;
        }

        return getResource(BIGML_URL + logisticregressionId, queryString);
    }

    /**
     * Retrieves a logisticregression.
     *
     * GET
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param logisticregression
     *            a logisticregression JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject logisticregression, final String queryString) {
        String resourceId = (String) logisticregression.get("resource");
        return get(resourceId, queryString);
    }

    /**
     * Checks whether a logisticregression's status is FINISHED.
     *
     * @param logisticregressionId
     *            a unique identifier in the form logisticregression/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String logisticregressionId) {
        return isResourceReady(get(logisticregressionId));
    }

    /**
     * Checks whether a logisticregression's status is FINISHED.
     *
     * @param logisticregression
     *            a logisticregression JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject logisticregression) {
        String resourceId = (String) logisticregression.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your logisticregressions.
     *
     * GET /andromeda/logisticregression?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(LOGISTICREGRESSION_URL, queryString);
    }

    /**
     * Updates an logisticregression.
     *
     * PUT
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param logisticregressionId
     *            a unique identifier in the form logisticregression/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the logisticregression. Optional
     *
     */
    @Override
    public JSONObject update(final String logisticregressionId, final String changes) {
        if (logisticregressionId == null || logisticregressionId.length() == 0
                || !logisticregressionId.matches(LOGISTICREGRESSION_RE)) {
            logger.info("Wrong logisticregression id");
            return null;
        }
        return updateResource(BIGML_URL + logisticregressionId, changes);
    }

    /**
     * Updates an logisticregression.
     *
     * PUT
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param logisticregression
     *            a logisticregression JSONObject
     * @param changes
     *            set of parameters to update the logisticregression. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject logisticregression, final JSONObject changes) {
        String resourceId = (String) logisticregression.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a logisticregression.
     *
     * DELETE
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param logisticregressionId
     *            a unique identifier in the form logisticregression/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String logisticregressionId) {
        if (logisticregressionId == null || logisticregressionId.length() == 0
                || !logisticregressionId.matches(LOGISTICREGRESSION_RE)) {
            logger.info("Wrong logisticregression id");
            return null;
        }
        return deleteResource(BIGML_URL + logisticregressionId);
    }

    /**
     * Deletes a logisticregression.
     *
     * DELETE
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param logisticregression
     *            a logisticregression JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject logisticregression) {
        String resourceId = (String) logisticregression.get("resource");
        return delete(resourceId);
    }

}
