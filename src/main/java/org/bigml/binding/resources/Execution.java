package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;


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
    public Execution(final BigMLClient bigmlClient,
    				 final String apiUser, final String apiKey, 
    				 final String project, final String organization,
    				 final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization,
    				   cacheManager, EXECUTION_RE, EXECUTION_PATH);
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
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            if (script != null && script.matches(SCRIPT_RE)) {
            		waitForResource(script, "scriptIsReady", waitTime, retries);

                requestObject.put("script", script);
                return createResource(resourceUrl, 
                		requestObject.toJSONString());
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
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
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
                		waitForResource(scriptId, "scriptIsReady", waitTime, retries);
                    scriptsIds.add(scriptId);
                } catch (Throwable e) {
                    logger.error("Error creating object");
                    return null;
                }
            }

            requestObject.put("scripts", scriptsIds);
            return createResource(resourceUrl, 
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating execution");
            return null;
        }
    }

}
