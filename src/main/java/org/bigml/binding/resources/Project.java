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
    public Project(final String apiUser, final String apiKey,
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
    public Project(final String apiUser, final String apiKey,
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
     * Check if the current resource is a Project
     *
     * @param resource the resource to be checked
     * @return true if it's a Project
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(PROJECT_RE);
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
     * Retrieves a project.
     *
     * GET
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String projectId) {
        if (projectId == null || projectId.length() == 0
                || !(projectId.matches(PROJECT_RE))) {
            logger.info("Wrong project id");
            return null;
        }

        return getResource(BIGML_URL + projectId);
    }

    /**
     * Retrieves a project.
     *
     * GET
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param project
     *            a project JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject project) {
        String resourceId = (String) project.get("resource");
        return get(resourceId);
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
     *
     */
    public JSONObject get(final String projectId, final String queryString) {
        return get(BIGML_URL + projectId, queryString, null, null);
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
     *
     */
    public JSONObject get(final JSONObject project, final String queryString) {
        String resourceId = (String) project.get("resource");
        return get(resourceId, queryString, null, null);
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

    /**
     * Checks whether a project's status is FINISHED.
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String projectId) {
        return isResourceReady(get(projectId));
    }

    /**
     * Checks whether a project's status is FINISHED.
     *
     * @param project
     *            a project JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject project) {
        String resourceId = (String) project.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your projects.
     *
     * GET /andromeda/project?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(PROJECT_URL, queryString);
    }

    /**
     * Updates a project.
     *
     * PUT
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the project. Optional
     *
     */
    @Override
    public JSONObject update(final String projectId, final String changes) {
        if (projectId == null || projectId.length() == 0
                || !(projectId.matches(PROJECT_RE))) {
            logger.info("Wrong project id");
            return null;
        }
        return updateResource(BIGML_URL + projectId, changes);
    }

    /**
     * Updates a project.
     *
     * PUT
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param project
     *            a project JSONObject
     * @param changes
     *            set of parameters to update the project. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject project, final JSONObject changes) {
        String resourceId = (String) project.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a project.
     *
     * DELETE
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String projectId) {
        if (projectId == null || projectId.length() == 0
                || !(projectId.matches(PROJECT_RE))) {
            logger.info("Wrong project id");
            return null;
        }
        return deleteResource(BIGML_URL + projectId);
    }

    /**
     * Deletes a project.
     *
     * DELETE
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param project
     *            a project JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject project) {
        String resourceId = (String) project.get("resource");
        return delete(resourceId);
    }

}
