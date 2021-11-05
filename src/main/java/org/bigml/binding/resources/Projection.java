package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete 
 * projections.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/projections
 *
 *
 */
public class Projection extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Projection.class);
    
    /**
     * Constructor
     *
     * @param bigmlClient	the client with connection to BigML
     * @param apiUser		API user
     * @param apiKey		API key
     * @param project		project id
     * @param organization	organization id
     * @param cacheManager	cache manager
     */
    public Projection(final BigMLClient bigmlClient,
    				  final String apiUser, final String apiKey, 
    				  final String project, final String organization,
    				  final CacheManager cacheManager) {
		super.init(bigmlClient, apiUser, apiKey, project, organization,
				   cacheManager, PROJECTION_RE, PROJECTION_PATH);
    }
    
    /**
     * Creates a projection from a pca.
     *
     * POST /andromeda/projection?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param pcaId
     *            a unique identifier in the form pca/id where id is a
     *            string of 24 alpha-numeric chars for the pca to attach
     *            the projection.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a projection for.
     * @param args
     *            set of parameters for the new projection. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED 
     *            status for source before to start to create the projection.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     * @return a JSONObject for the new projection
     */
    public JSONObject create(final String pcaId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {

        if (pcaId == null || pcaId.length() == 0 ) {
            logger.info("Wrong pca id. Id cannot be null");
            return null;
        }

        try {
        	waitForResource(pcaId, "pcaIsReady", waitTime, retries);

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            requestObject.put("pca", pcaId);
            requestObject.put("input_data", inputData);

            return createResource(resourceUrl,
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating a projection");
            return null;
        }
    }

}
