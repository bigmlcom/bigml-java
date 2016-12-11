package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This class is used by the BigML class as a mixin that provides the Whizzml
 * Execution' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/executions
 *
 */
public class Execution extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Execution.class);

    /**
     * Constructor
     *
     */
    public Execution() {
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
    public Execution(final String apiUser, final String apiKey,
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
    public Execution(final String apiUser, final String apiKey,
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
     * Check if the current resource is an Execution
     *
     * @param resource the resource to be checked
     * @return true if it's an Execution
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(EXECUTION_RE);
    }

    /**
     * Creates a whizzml execution for a script.
     *
     * POST /andromeda/execution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param script
     *            a unique identifier in the form script/id where id is a string
     *            of 24 alpha-numeric chars for the script to attach the execution.
     * @param args
     *            set of parameters for the new execution. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the execution. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(String script, JSONObject args,
                             Integer waitTime, Integer retries) {

        try {
            if (script != null && script.matches(SCRIPT_RE)) {
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                            .scriptIsReady(script)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                JSONObject requestObject = new JSONObject();
                requestObject.put("script", script);
                return createResource(EXECUTION_URL, requestObject.toJSONString());
            } else {
                logger.info("A script id or a list of them is needed to create a whizzml execution");
                return null;
            }
        } catch (Throwable e) {
            logger.error("Error creating execution");
            return null;
        }
    }


    /**
     * Creates a whizzml execution for a list of scripts.
     *
     * POST /andromeda/execution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param scripts
     *            a list of identifiers in the form script/id where id is a string
     *            of 24 alpha-numeric chars for the script to attach the execution.
     * @param args
     *            set of parameters for the new execution. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the execution. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(List<String> scripts, JSONObject args,
                             Integer waitTime, Integer retries) {

        if (scripts == null || scripts.size()==0) {
            logger.info("A script id or a list of them is needed to create a whizzml execution");
            return null;
        }

        try {
            List<String> scriptsIds = new ArrayList<String>();

            for (String scriptId : scripts) {
                // Checking valid scriptId
                if (scriptId == null || scriptId.length() == 0
                        || !(scriptId.matches(SCRIPT_RE))) {
                    logger.info("Wrong scriptId id");
                    return null;
                }

                // Checking status
                try {
                    waitTime = waitTime != null ? waitTime : 3000;
                    retries = retries != null ? retries : 10;
                    if (waitTime > 0) {
                        int count = 0;
                        while (count < retries
                                && !BigMLClient.getInstance(this.devMode)
                                        .scriptIsReady(scriptId)) {
                            Thread.sleep(waitTime);
                            count++;
                        }
                    }
                    scriptsIds.add(scriptId);
                } catch (Throwable e) {
                    logger.error("Error creating object");
                    return null;
                }
            }

            JSONObject requestObject = new JSONObject();
            requestObject.put("scripts", scriptsIds);
            return createResource(EXECUTION_URL, requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating execution");
            return null;
        }
    }


    /**
     * Retrieves a whizzml execution.
     *
     * GET
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param executionId
     *            a unique identifier in the form execution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String executionId) {
        if (executionId == null || executionId.length() == 0
                || !executionId.matches(EXECUTION_RE)) {
            logger.info("Wrong execution id");
            return null;
        }

        return getResource(BIGML_URL + executionId);
    }

    /**
     * Retrieves a whizzml execution.
     *
     * GET
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param execution
     *            a execution JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject execution) {
        String resourceId = (String) execution.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves a whizzml execution.
     *
     * GET
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param  executionId
     *            a unique identifier in the form  execution/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String executionId, final String queryString) {
        if (executionId == null || executionId.length() == 0
                || !executionId.matches(EXECUTION_RE)) {
            logger.info("Wrong  execution id");
            return null;
        }

        return getResource(BIGML_URL + executionId, queryString);
    }

    /**
     * Retrieves a whizzml execution.
     *
     * GET
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param execution
     *            a execution JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject execution, final String queryString) {
        String resourceId = (String) execution.get("resource");
        return get(resourceId, queryString);
    }

    /**
     * Checks whether a whizzml  execution's status is FINISHED.
     *
     * @param  executionId
     *            a unique identifier in the form  execution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String  executionId) {
        return isResourceReady(get( executionId));
    }

    /**
     * Checks whether a whizzml  execution status is FINISHED.
     *
     * @param  execution
     *            a  execution JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject  execution) {
        return isResourceReady( execution)
                || isReady((String) execution.get("resource"));
    }

    /**
     * Lists all your whizzml executions.
     *
     * GET /andromeda/execution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(EXECUTION_URL, queryString);
    }

    /**
     * Updates a whizzml execution.
     *
     * PUT
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param executionId
     *            a unique identifier in the form execution/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the execution. Optional
     *
     */
    @Override
    public JSONObject update(final String executionId, final String changes) {
        if (executionId == null || executionId.length() == 0
                || !(executionId.matches(EXECUTION_RE))) {
            logger.info("Wrong execution id");
            return null;
        }
        return updateResource(BIGML_URL + executionId, changes);
    }

    /**
     * Updates a whizzml execution.
     *
     * PUT
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param executionJSON
     *            a execution JSONObject
     * @param changes
     *            set of parameters to update the execution. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject executionJSON, final JSONObject changes) {
        String resourceId = (String) executionJSON.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a whizzml execution.
     *
     * DELETE
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param executionId
     *            a unique identifier in the form execution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String executionId) {
        if (executionId == null || executionId.length() == 0
                || !(executionId.matches(EXECUTION_RE))) {
            logger.info("Wrong execution id");
            return null;
        }
        return deleteResource(BIGML_URL + executionId);
    }

    /**
     * Deletes a whizzml execution.
     *
     * DELETE
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param executionJSON
     *            a execution JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject executionJSON) {
        String resourceId = (String) executionJSON.get("resource");
        return delete(resourceId);
    }

}
