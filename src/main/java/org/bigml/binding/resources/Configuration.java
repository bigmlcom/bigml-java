package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by the BigML class as a mixin that provides the configuration's REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/configurations
 *
 *
 */
public class Configuration extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Configuration.class);

    /**
     * Constructor
     *
     */
    public Configuration() {
    		super.init(null, null, false, null);
        this.resourceRe = CONFIGURATION_RE;
        this.resourceUrl = CONFIGURATION_URL;
        this.resourceName = "configuration";
    }

    /**
     * Constructor
     *
     */
    public Configuration(final String apiUser, final String apiKey,
                   final boolean devMode) {
    		super.init(apiUser, apiKey, devMode, null);
        this.resourceRe = CONFIGURATION_RE;
        this.resourceUrl = CONFIGURATION_URL;
        this.resourceName = "configuration";
    }

    /**
     * Constructor
     *
     */
    public Configuration(final String apiUser, final String apiKey,
                   final boolean devMode, final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, devMode, cacheManager);
        this.resourceRe = CONFIGURATION_RE;
        this.resourceUrl = CONFIGURATION_URL;
        this.resourceName = "configuration";
    }

    /**
     * Creates a configuration.
     *
     * Uses a remote resource to create a new configuration using the
     * arguments in `args`.
     *
     * If `wait_time` is higher than 0 then the configuration creation
     * request is not sent until the `sample` has been created successfully.
     *
     * @param args
     *            set of parameters for the new configuration. Optional
     *
     */
    public JSONObject create(JSONObject args) {

        try {
            JSONObject requestObject = new JSONObject();
            requestObject.putAll(args);
            return createResource(CONFIGURATION_URL, requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Failed to generate the configuration.", e);
            return null;
        }
    }

    /**
     * Retrieves a configuration.
     *
     * GET /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String configurationId, final String queryString) {
        return get(BIGML_URL + configurationId, queryString, null, null);
    }

    /**
     * Retrieves a configuration.
     *
     * GET /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String configurationId, final String queryString,
                          final String apiUser, final String apiKey) {
        if (configurationId == null || configurationId.length() == 0
                || !(configurationId.matches(CONFIGURATION_RE))) {
            logger.info("Wrong configuration id");
            return null;
        }

        return getResource(BIGML_URL + configurationId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves an configuration.
     *
     * GET /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param configuration
     *            a configuration JSONObject
     * @param queryString
     *            query for filtering
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject configuration, final String queryString,
                          final String apiUser, final String apiKey) {
        String resourceId = (String) configuration.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

}
