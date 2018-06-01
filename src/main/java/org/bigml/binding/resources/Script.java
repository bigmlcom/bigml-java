package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;


/**
 * This class is used by the BigML class as a mixin that provides the Whizzml
 * Script' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/scripts
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
    	super.init(null, null, null);
        this.resourceRe = SCRIPT_RE;
        this.resourceUrl = SCRIPT_URL;
        this.resourceName = "script";
    }

    /**
     * Constructor
     *
     */
    public Script(final String apiUser, final String apiKey) {
    	super.init(apiUser, apiKey, null);
        this.resourceRe = SCRIPT_RE;
        this.resourceUrl = SCRIPT_URL;
        this.resourceName = "script";
    }

    /**
     * Constructor
     *
     */
    public Script(final String apiUser, final String apiKey, final CacheManager cacheManager) {
    	super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = SCRIPT_RE;
        this.resourceUrl = SCRIPT_URL;
        this.resourceName = "script";
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
                            && !BigMLClient.getInstance().scriptIsReady(source)) {
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

}
