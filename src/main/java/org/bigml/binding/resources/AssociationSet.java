package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete association sets.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/associationsets
 *
 *
 */
public class AssociationSet extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(AssociationSet.class);

    /**
     * Constructor
     *
     */
    public AssociationSet() {
    		super.init(null, null, null, 
    			ASSOCIATIONSET_RE, ASSOCIATIONSET_PATH);
    }

    /**
     * Constructor
     *
     */
    public AssociationSet(final String apiUser, final String apiKey) {
    		super.init(apiUser, apiKey, null, 
    			ASSOCIATIONSET_RE, ASSOCIATIONSET_PATH);
    }


    /**
     * Constructor
     *
     */
    public AssociationSet(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, cacheManager, 
        		ASSOCIATIONSET_RE, ASSOCIATIONSET_PATH);
    }

    /**
     * Creates an association set from an association.
     *
     * POST /andromeda/associationset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a
     *            string of 24 alpha-numeric chars for the association to attach
     *            the association set.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create an association set for.
     * @param args
     *            set of parameters for the new association set. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the association set.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String associationId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {

        if (associationId == null || associationId.length() == 0 ) {
            logger.info("Wrong association id. Id cannot be null");
            return null;
        }

        try {
        		waitForResource(associationId, "associationIsReady",  
        				waitTime, retries);

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            requestObject.put("association", associationId);
            requestObject.put("input_data", inputData);

            return createResource(resourceUrl,
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating an association set");
            return null;
        }
    }

}
