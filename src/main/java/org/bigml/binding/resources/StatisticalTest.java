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
 * This class is used by the BigML class as a mixin that provides the StatisticalTest' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/statisticaltests
 *
 *
 */
public class StatisticalTest extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(StatisticalTest.class);

    /**
     * Constructor
     *
     */
    public StatisticalTest() {
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
    public StatisticalTest(final String apiUser, final String apiKey,
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
    public StatisticalTest(final String apiUser, final String apiKey,
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
     * Check if the current resource is a StatisticalTest
     *
     * @param resource the resource to be checked
     * @return true if its an StatisticalTest
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(STATISTICALTEST_RE);
    }


    /**
     * Creates a StatisticalTest from a `dataset`.
     *
     * POST /andromeda/statisticaltest?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            statisticaltest.
     * @param args
     *            set of parameters for the new statisticaltest. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the statisticaltest. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(STATISTICALTEST_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves a statisticaltest.
     *
     * GET
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String statisticaltestId) {
        if (statisticaltestId == null || statisticaltestId.length() == 0
                || !statisticaltestId.matches(STATISTICALTEST_RE)) {
            logger.info("Wrong statisticaltest id");
            return null;
        }

        return getResource(BIGML_URL + statisticaltestId);
    }

    /**
     * Retrieves a statisticaltest.
     *
     * GET
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param statisticaltest
     *            a statisticaltest JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject statisticaltest) {
        String resourceId = (String) statisticaltest.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves a statisticaltest.
     *
     * GET
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String statisticaltestId, final String queryString) {
        if (statisticaltestId == null || statisticaltestId.length() == 0
                || !statisticaltestId.matches(STATISTICALTEST_RE)) {
            logger.info("Wrong statisticaltest id");
            return null;
        }

        return getResource(BIGML_URL + statisticaltestId, queryString);
    }

    /**
     * Retrieves a statisticaltest.
     *
     * GET
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param statisticaltest
     *            a statisticaltest JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject statisticaltest, final String queryString) {
        String resourceId = (String) statisticaltest.get("resource");
        return get(resourceId, queryString);
    }

    /**
     * Checks whether a statisticaltest's status is FINISHED.
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String statisticaltestId) {
        return isResourceReady(get(statisticaltestId));
    }

    /**
     * Checks whether a statisticaltest status is FINISHED.
     *
     * @param statisticaltest
     *            a statisticaltest JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject statisticaltest) {
        return isResourceReady(statisticaltest)
                || isReady((String) statisticaltest.get("resource"));
    }

    /**
     * Lists all your statisticaltest.
     *
     * GET /andromeda/statisticaltest?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(STATISTICALTEST_URL, queryString);
    }

    /**
     * Updates a statisticaltest.
     *
     * PUT
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the statisticaltest. Optional
     *
     */
    @Override
    public JSONObject update(final String statisticaltestId, final String changes) {
        if (statisticaltestId == null || statisticaltestId.length() == 0
                || !(statisticaltestId.matches(STATISTICALTEST_RE))) {
            logger.info("Wrong statisticaltest id");
            return null;
        }
        return updateResource(BIGML_URL + statisticaltestId, changes);
    }

    /**
     * Updates a statisticaltest.
     *
     * PUT
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param statisticaltest
     *            a statisticaltest JSONObject
     * @param changes
     *            set of parameters to update the statisticaltest. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject statisticaltest, final JSONObject changes) {
        String resourceId = (String) statisticaltest.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a statisticaltest.
     *
     * DELETE
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String statisticaltestId) {
        if (statisticaltestId == null || statisticaltestId.length() == 0
                || !(statisticaltestId.matches(STATISTICALTEST_RE))) {
            logger.info("Wrong statisticaltest id");
            return null;
        }
        return deleteResource(BIGML_URL + statisticaltestId);
    }

    /**
     * Deletes a statisticaltest.
     *
     * DELETE
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param statisticaltest
     *            a statisticaltest JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject statisticaltest) {
        String resourceId = (String) statisticaltest.get("resource");
        return delete(resourceId);
    }
}