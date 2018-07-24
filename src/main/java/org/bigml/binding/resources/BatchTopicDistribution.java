package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete
 * batch topic distributions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/batch_topicdistributions
 *
 *
 */
public class BatchTopicDistribution extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchTopicDistribution.class);
    
    /**
     * Constructor
     *
     * @deprecated
     */
	public BatchTopicDistribution() {
		super.init(null, null, null, null, null, 
				BATCH_TOPICDISTRIBUTION_RE, BATCH_TOPICDISTRIBUTION_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public BatchTopicDistribution(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				BATCH_TOPICDISTRIBUTION_RE, BATCH_TOPICDISTRIBUTION_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public BatchTopicDistribution(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				BATCH_TOPICDISTRIBUTION_RE, BATCH_TOPICDISTRIBUTION_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public BatchTopicDistribution(final String apiUser, final String apiKey, 
    							  final String project, final String organization,
    							  final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, project, organization,
    				   cacheManager, BATCH_TOPICDISTRIBUTION_RE, 
    				   BATCH_TOPICDISTRIBUTION_PATH);
    }

    /**
     * Creates a new batch topic distribution.
     *
     * POST /andromeda/batchtopicdistribution?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a
     *            string of 24 alpha-numeric chars for the topic model.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset.
     * @param args
     *            set of parameters for the new batch topic distribution. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for topic model before to start to create the
     *            batch topic distribution. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String topicModelId, final String datasetId,
            JSONObject args, Integer waitTime, Integer retries) {
        if (topicModelId == null || topicModelId.length() == 0
                || !(topicModelId.matches(TOPICMODEL_RE))) {
            logger.info("Wrong topic model id");
            return null;
        }

        if (datasetId == null || datasetId.length() == 0
                || !(datasetId.matches(DATASET_RE))) {
            logger.info("Wrong dataset id");
            return null;
        }

        try {
        		waitForResource(topicModelId, "topicModelIsReady", waitTime, retries);
        		waitForResource(datasetId, "datasetIsReady", waitTime, retries);
        	
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            requestObject.put("topicmodel", topicModelId);
            requestObject.put("dataset", datasetId);

            return createResource(resourceUrl,
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating batch topic distribution");
            return null;
        }
    }

    /**
     * Retrieves the batch topic distribution file.
     *
     * Downloads topic distributions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchTopicDistribution(final String batchTopicDistributionId,
            final String filename) {

        if (batchTopicDistributionId == null || batchTopicDistributionId.length() == 0
                || !batchTopicDistributionId.matches(BATCH_TOPICDISTRIBUTION_RE)) {
            logger.info("Wrong batch topic distribution id");
            return null;
        }

        String url = BIGML_URL + batchTopicDistributionId + DOWNLOAD_DIR;
        return download(url, filename);
    }

    /**
     * Retrieves the batch topic distribution file.
     *
     * Downloads topic distributions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchTopicDistributionJSON
     *            a batch topic distribution JSONObject.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchTopicDistribution(final JSONObject batchTopicDistributionJSON,
            final String filename) {
        String resourceId = (String) batchTopicDistributionJSON.get("resource");
        return downloadBatchTopicDistribution(resourceId, filename);
    }

}
