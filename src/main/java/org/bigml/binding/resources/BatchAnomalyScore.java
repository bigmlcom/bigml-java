package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete
 * batch anomaly scores.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/batch_anomalyscores
 *
 *
 */
public class BatchAnomalyScore extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchAnomalyScore.class);
    
    /**
     * Constructor
     *
     * @deprecated
     */
	public BatchAnomalyScore() {
		super.init(null, null, null, null, null, 
				BATCH_ANOMALYSCORE_RE, BATCHANOMALYSCORE_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public BatchAnomalyScore(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				BATCH_ANOMALYSCORE_RE, BATCHANOMALYSCORE_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public BatchAnomalyScore(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				BATCH_ANOMALYSCORE_RE, BATCHANOMALYSCORE_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public BatchAnomalyScore(final BigMLClient bigmlClient,
    						 final String apiUser, final String apiKey, 
    						 final String project, final String organization, 
    						 final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization, 
    				   cacheManager, BATCH_ANOMALYSCORE_RE, 
    				   BATCHANOMALYSCORE_PATH);
    }

    /**
     * Creates a new BatchAnomalyScore.
     *
     * POST /andromeda/batchanomalyscore?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a
     *            string of 24 alpha-numeric chars for the Anomaly Score.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset.
     * @param args
     *            set of parameters for the new batchanomalyscore. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for anomaly before to start to create the
     *            batchanomalyscore. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String anomalyId, final String datasetId,
            JSONObject args, Integer waitTime, Integer retries) {
        if (anomalyId == null || anomalyId.length() == 0
                || !(anomalyId.matches(ANOMALY_RE))) {
            logger.info("Wrong anomaly id");
            return null;
        }

        if (datasetId == null || datasetId.length() == 0
                || !(datasetId.matches(DATASET_RE))) {
            logger.info("Wrong dataset id");
            return null;
        }

        try {
        		waitForResource(anomalyId, "anomalyIsReady", waitTime, retries);
        		waitForResource(datasetId, "datasetIsReady", waitTime, retries);
            
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            requestObject.put("anomaly", anomalyId);
            requestObject.put("dataset", datasetId);

            return createResource(resourceUrl,
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating batchanomalyscore");
            return null;
        }
    }

    /**
     * Retrieves the batch anomaly score file.
     *
     * Downloads scores, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchAnomalyScore(final String batchAnomalyScoreId,
            final String filename) {

        if (batchAnomalyScoreId == null || batchAnomalyScoreId.length() == 0
                || !batchAnomalyScoreId.matches(BATCH_ANOMALYSCORE_RE)) {
            logger.info("Wrong batch anomaly score id");
            return null;
        }

        String url = BIGML_URL + batchAnomalyScoreId + DOWNLOAD_DIR;
        return download(url, filename);
    }

    /**
     * Retrieves the batch anomaly score file.
     *
     * Downloads scores, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchAnomalyScoreJSON
     *            a batch anomaly score JSONObject.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchAnomalyScore(final JSONObject batchAnomalyScoreJSON,
            final String filename) {
        String resourceId = (String) batchAnomalyScoreJSON.get("resource");
        return downloadBatchAnomalyScore(resourceId, filename);
    }

}
