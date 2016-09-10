package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * This class is used by the BigML class as a mixin that provides the Whizzml
 * Script' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/scripts
 *
 */
public class Script extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Script.class);

    /**
     * Constructor
     *
     */
    public Script() {
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
    public Script(final String apiUser, final String apiKey,
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
    public Script(final String apiUser, final String apiKey,
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
     * Check if the current resource is a Script
     *
     * @param resource the resource to be checked
     * @return true if its an Script
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(SCRIPT_RE);
    }

    /**
     * Creates a whizzml script from its source code.
     *
     * POST /andromeda/script?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param source
     *            source code for the script. It can be either
     *              - string: source code
     *              - script id: the ID for an existing whizzml script
     *              - path: the path to a file containing the source code
     * @param args
     *            set of parameters for the new script. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the script. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(String source, JSONObject args,
                             Integer waitTime, Integer retries) {

        if (source == null || source.length() == 0 ) {
            logger.info("A valid code string or a script id must be provided.");
            return null;
        }

        try {
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            if (source.matches(SCRIPT_RE)) {
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                            .scriptIsReady(source)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                requestObject.put("origin", source);
                return createResource(SCRIPT_URL, requestObject.toJSONString());
            }

            try {
                File file = new File(source);
                if (file.exists()) {
                    source = Utils.readFile(source);
                }
            } catch (Throwable e) {
                logger.error("Could not open the source code file " + source, e);
            }

            requestObject.put("source_code", source);
            return createResource(SCRIPT_URL, requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating evaluation");
            return null;
        }
    }

    /**
     * Retrieves a whizzml script.
     *
     * GET
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String scriptId) {
        if (scriptId == null || scriptId.length() == 0
                || !scriptId.matches(SCRIPT_RE)) {
            logger.info("Wrong script id");
            return null;
        }

        return getResource(BIGML_URL + scriptId);
    }

    /**
     * Retrieves a whizzml script.
     *
     * GET
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param script
     *            a script JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject script) {
        String resourceId = (String) script.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves a whizzml script.
     *
     * GET
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String scriptId, final String queryString) {
        if (scriptId == null || scriptId.length() == 0
                || !scriptId.matches(SCRIPT_RE)) {
            logger.info("Wrong script id");
            return null;
        }

        return getResource(BIGML_URL + scriptId, queryString);
    }

    /**
     * Retrieves a whizzml script.
     *
     * GET
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param script
     *            a script JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject script, final String queryString) {
        String resourceId = (String) script.get("resource");
        return get(resourceId, queryString);
    }

    /**
     * Checks whether a whizzml script's status is FINISHED.
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String scriptId) {
        return isResourceReady(get(scriptId));
    }

    /**
     * Checks whether a whizzml script status is FINISHED.
     *
     * @param script
     *            a script JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject script) {
        return isResourceReady(script)
                || isReady((String) script.get("resource"));
    }

    /**
     * Lists all your whizzml scripts.
     *
     * GET /andromeda/script?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(SCRIPT_URL, queryString);
    }

    /**
     * Updates a whizzml script.
     *
     * PUT
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the script. Optional
     *
     */
    @Override
    public JSONObject update(final String scriptId, final String changes) {
        if (scriptId == null || scriptId.length() == 0
                || !(scriptId.matches(SCRIPT_RE))) {
            logger.info("Wrong script id");
            return null;
        }
        return updateResource(BIGML_URL + scriptId, changes);
    }

    /**
     * Updates a whizzml script.
     *
     * PUT
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param script
     *            a script JSONObject
     * @param changes
     *            set of parameters to update the script. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject script, final JSONObject changes) {
        String resourceId = (String) script.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a whizzml script.
     *
     * DELETE
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String scriptId) {
        if (scriptId == null || scriptId.length() == 0
                || !(scriptId.matches(SCRIPT_RE))) {
            logger.info("Wrong script id");
            return null;
        }
        return deleteResource(BIGML_URL + scriptId);
    }

    /**
     * Deletes a script.
     *
     * DELETE
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param script
     *            a script JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject script) {
        String resourceId = (String) script.get("resource");
        return delete(resourceId);
    }
}