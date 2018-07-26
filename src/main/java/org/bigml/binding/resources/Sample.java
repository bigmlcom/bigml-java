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
 * This class is used by the BigML class as a mixin that provides the samples' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/samples
 *
 *
 */
public class Sample extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Sample.class);
    
    /**
     * Constructor
     *
     * @deprecated
     */
	public Sample() {
		super.init(null, null, null, null, null, 
				SAMPLE_RE, SAMPLE_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Sample(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				SAMPLE_RE, SAMPLE_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Sample(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				SAMPLE_RE, SAMPLE_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public Sample(final BigMLClient bigmlClient,
    			  final String apiUser, final String apiKey, 
    			  final String project, final String organization,
    			  final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization, 
    				   cacheManager, SAMPLE_RE, SAMPLE_PATH);
    }

    /**
     * Creates a remote sample from a dataset.
     *
     * Uses a remote resource to create a new sample using the
     * arguments in `args`.
     *
     * If `wait_time` is higher than 0 then the sample creation
     * request is not sent until the `sample` has been created successfuly.
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id
     *            where id is a string of 24 alpha-numeric chars for the
     *            remote dataset to be use to create the sample.
     * @param args
     *            set of parameters for the new sample. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the sample. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        if (datasetId == null || datasetId.length() == 0 ) {
            logger.info("Wrong dataset id. Id cannot be null");
            return null;
        }

        try {
            JSONObject requestObject = new JSONObject();

            // If the original resource is a Source
            if( datasetId.matches(DATASET_RE) ) {
            		waitForResource(datasetId, "datasetIsReady", waitTime, retries);
            	
                if (args != null) {
                    requestObject = args;
                }
                requestObject.put("dataset", datasetId);
            } else {
                throw new IllegalArgumentException(String.format("A dataset id is needed " +
                        "to create a sample. %s found.", datasetId));
            }

            return createResource(resourceUrl, 
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Failed to generate the sample.", e);
            return null;
        }
    }

    /**
     * Retrieves an sample.
     *
     * GET /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param sampleId
     *            a unique identifier in the form sample/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String sampleId, final String queryString,
                          final String apiUser, final String apiKey) {
        if (sampleId == null || sampleId.length() == 0
                || !(sampleId.matches(SAMPLE_RE))) {
            logger.info("Wrong sample id");
            return null;
        }

        return getResource(BIGML_URL + sampleId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves an sample.
     *
     * GET /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param sample
     *            a sample JSONObject
     * @param queryString
     *            query for filtering
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject sample, final String queryString,
                          final String apiUser, final String apiKey) {
        String resourceId = (String) sample.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

    /**
     * Returns the ids of the fields that contain errors and their number.
     *
     * The sample argument is a sample resource structure
     *
     * @param sample
     *            a sample JSONObject
     *
     */
    public Map<String, Long> getErrorCounts(final JSONObject sample) {
        Map<String, Long> errorsDict = new HashMap<String, Long>();

        JSONObject loadedSample = get(sample);
        if( loadedSample != null ) {
            JSONObject errors = (JSONObject) Utils.getJSONObject(loadedSample, "object.status.field_errors",
                    null);
            for (Object fieldId : errors.keySet()) {
                errorsDict.put(fieldId.toString(), ((Number) ((JSONObject) errors.get(fieldId)).get("total")).longValue());
            }
        }

        return errorsDict;
    }

}
