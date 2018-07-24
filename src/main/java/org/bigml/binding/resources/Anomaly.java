package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete anomaly detectors.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/anomalies
 *
 *
 */
public class Anomaly extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Anomaly.class);
    
    /**
     * Constructor
     *
     * @deprecated
     */
	public Anomaly() {
		super.init(null, null, null, null, null, ANOMALY_RE, ANOMALY_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Anomaly(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, ANOMALY_RE, ANOMALY_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Anomaly(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, ANOMALY_RE, ANOMALY_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public Anomaly(final String apiUser, final String apiKey, 
    		       final String project, final String organization,
    			   final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, project, organization, 
    				   cacheManager, ANOMALY_RE, ANOMALY_PATH);
    }


    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String anomalyId, final String apiUser,
            final String apiKey) {
        if (anomalyId == null || anomalyId.length() == 0
                || !(anomalyId.matches(ANOMALY_RE))) {
            logger.info("Wrong anomaly id");
            return null;
        }

        return getResource(BIGML_URL + anomalyId, null, apiUser, apiKey);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomaly
     *            an anomaly JSONObject
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject anomaly, final String apiUser,
            final String apiKey) {
        String resourceId = (String) anomaly.get("resource");
        return get(resourceId, apiUser, apiKey);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String anomalyId, final String queryString,
            final String apiUser, final String apiKey) {
        if (anomalyId == null || anomalyId.length() == 0
                || !(anomalyId.matches(ANOMALY_RE))) {
            logger.info("Wrong anomaly id");
            return null;
        }

        return getResource(BIGML_URL + anomalyId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomaly
     *            an anomaly JSONObject
     * @param queryString
     *            query for filtering
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject anomaly, final String queryString,
            final String apiUser, final String apiKey) {
        String resourceId = (String) anomaly.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

}
