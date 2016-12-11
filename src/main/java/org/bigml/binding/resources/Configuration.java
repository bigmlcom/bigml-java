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
    public Configuration(final String apiUser, final String apiKey,
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
    public Configuration(final String apiUser, final String apiKey,
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
     * Check if the current resource is a configuration
     *
     * @param resource the resource to be checked
     * @return true if it's a Configuration
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(CONFIGURATION_RE);
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
     * GET
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String configurationId) {
        if (configurationId == null || configurationId.length() == 0
                || !(configurationId.matches(CONFIGURATION_RE))) {
            logger.info("Wrong configuration id");
            return null;
        }

        return getResource(BIGML_URL + configurationId);
    }

    /**
     * Retrieves a configuration.
     *
     * GET
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param configuration
     *            a configuration JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject configuration) {
        String resourceId = (String) configuration.get("resource");
        return get(resourceId);
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
     * Retrieves a configuration.
     *
     * GET /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param configuration
     *            a configuration JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject configuration, final String queryString) {
        String resourceId = (String) configuration.get("resource");
        return get(resourceId, queryString, null, null);
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

    /**
     * Checks whether a configuration's status is FINISHED.
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String configurationId) {
        return isResourceReady(get(configurationId));
    }

    /**
     * Checks whether a configuration's status is FINISHED.
     *
     * @param configuration
     *            a configuration JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject configuration) {
        String resourceId = (String) configuration.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your configurations.
     *
     * GET /andromeda/configuration?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(CONFIGURATION_URL, queryString);
    }

    /**
     * Updates a configuration.
     *
     * PUT
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the configuration. Optional
     *
     */
    @Override
    public JSONObject update(final String configurationId, final String changes) {
        if (configurationId == null || configurationId.length() == 0
                || !(configurationId.matches(CONFIGURATION_RE))) {
            logger.info("Wrong configuration id");
            return null;
        }
        return updateResource(BIGML_URL + configurationId, changes);
    }

    /**
     * Updates a configuration.
     *
     * PUT
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param configuration
     *            a configuration JSONObject
     * @param changes
     *            set of parameters to update the configuration. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject configuration, final JSONObject changes) {
        String resourceId = (String) configuration.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a configuration.
     *
     * DELETE
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String configurationId) {
        if (configurationId == null || configurationId.length() == 0
                || !(configurationId.matches(CONFIGURATION_RE))) {
            logger.info("Wrong configuration id");
            return null;
        }
        return deleteResource(BIGML_URL + configurationId);
    }

    /**
     * Deletes a configuration.
     *
     * DELETE
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param configuration
     *            a configuration JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject configuration) {
        String resourceId = (String) configuration.get("resource");
        return delete(resourceId);
    }

}
