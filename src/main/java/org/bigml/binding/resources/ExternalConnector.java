package org.bigml.binding.resources;

import java.util.Map;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by the BigML class as a mixin that provides the 
 * ExternalConnector's REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/externalconnectors
 *
 *
 */
public class ExternalConnector extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(ExternalConnector.class);
    
	
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
    public ExternalConnector(final BigMLClient bigmlClient,
    					   final String apiUser, final String apiKey, 
    					   final String project, final String organization,
    					   final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization,
    				   cacheManager, EXTERNALCONNECTOR_RE, EXTERNALCONNECTOR_PATH);
    }
    
    
    /**
     * Creates a new resource.
     *
     * POST /andromeda/xxxxx?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param source
     *            the name of the external data source to connect to.
     * @param connectionInfo
     *            a map containing the connection information.
     * @param args
     *            set of parameters for the new resource. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for dataset before to start to create the resource. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     * @return a JSONObject for the new external connector
     */
    public JSONObject create(final String source, 
    		final Map connectionInfo, JSONObject args,
            Integer waitTime, Integer retries) {

    	
    	if (source == null || connectionInfo == null) {
            logger.info(
            	"To create an external connector you need to provide a " +
            	"source and a map with the connection information. PLease," +
            	"refer to the API externalconnector docs for details.");
            return null;
        }
    	
    	if (connectionInfo.containsKey("source")) {
    		connectionInfo.remove("source");
    	}
    	
    	try {
    		JSONObject requestObject = new JSONObject();
    		requestObject.put("source", source);
    		requestObject.put("connection", connectionInfo);

    		return createResource(resourceUrl, 
            		requestObject.toJSONString());
    	} catch (Throwable e) {
            logger.error("Failed to generate the external connector.", e);
            return null;
        }
    }

}
