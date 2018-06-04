package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete centroids.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/centroids
 *
 *
 */
public class Centroid extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Centroid.class);

    /**
     * Constructor
     *
     */
    public Centroid() {
    		super.init(null, null, null, 
    			CENTROID_RE, CENTROID_PATH);
    }

    /**
     * Constructor
     *
     */
    public Centroid(final String apiUser, final String apiKey) {
    		super.init(apiUser, apiKey, null, 
        		CENTROID_RE, CENTROID_PATH);
    }

    /**
     * Constructor
     *
     */
    public Centroid(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, cacheManager, 
    			CENTROID_RE, CENTROID_PATH);
    }

    /**
     * Creates a new centroid.
     *
     * POST /andromeda/centroid?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars for the cluster.
     * @param args
     *            set of parameters for the new centroid. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for centroid before to start to create the centroid. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String clusterId, JSONObject inputDataJSON,
            JSONObject args, Integer waitTime, Integer retries) {
        if (clusterId == null || clusterId.length() == 0
                || !(clusterId.matches(CLUSTER_RE))) {
            logger.info("Wrong cluster id");
            return null;
        }

        try {
        		waitForResource(clusterId, "clusterIsReady", waitTime, retries);
        	
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            requestObject.put("cluster", clusterId);
            requestObject.put("input_data", inputDataJSON);

            return createResource(resourceUrl, 
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating centroid");
            return null;
        }
    }

}
