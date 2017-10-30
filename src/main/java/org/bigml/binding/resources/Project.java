package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by the BigML class as a mixin that provides the Project' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/project
 *
 *
 */
public class Project extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Project.class);

    /**
     * Constructor
     *
     */
    public Project() {
    		super.init(null, null, false, null);
        this.resourceRe = PROJECT_RE;
        this.resourceUrl = PROJECT_URL;
        this.resourceName = "project";
    }

    /**
     * Constructor
     *
     */
    public Project(final String apiUser, final String apiKey,
                   final boolean devMode) {
    		super.init(apiUser, apiKey, devMode, null);
        this.resourceRe = PROJECT_RE;
        this.resourceUrl = PROJECT_URL;
        this.resourceName = "project";
    }

    /**
     * Constructor
     *
     */
    public Project(final String apiUser, final String apiKey,
                   final boolean devMode, final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, devMode, cacheManager);
        this.resourceRe = PROJECT_RE;
        this.resourceUrl = PROJECT_URL;
        this.resourceName = "project";
    }

    /**
     * Creates a project.
     *
     * Uses a remote resource to create a new project using the
     * arguments in `args`.
     *
     * If `wait_time` is higher than 0 then the project creation
     * request is not sent until the `sample` has been created successfuly.
     *
     * @param args
     *            set of parameters for the new project. Optional
     *
     */
    public JSONObject create(JSONObject args) {

        try {
            JSONObject requestObject = new JSONObject();
            requestObject.putAll(args);
            return createResource(PROJECT_URL, requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Failed to generate the project.", e);
            return null;
        }
    }

    /**
     * Retrieves an project.
     *
     * GET /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String projectId, final String queryString,
                          final String apiUser, final String apiKey) {
        if (projectId == null || projectId.length() == 0
                || !(projectId.matches(PROJECT_RE))) {
            logger.info("Wrong project id");
            return null;
        }

        return getResource(BIGML_URL + projectId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves an project.
     *
     * GET /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param project
     *            a project JSONObject
     * @param queryString
     *            query for filtering
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject project, final String queryString,
                          final String apiUser, final String apiKey) {
        String resourceId = (String) project.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

}
