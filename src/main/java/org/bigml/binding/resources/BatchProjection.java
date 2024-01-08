package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete
 * batch projections.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/batch_projections
 *
 *
 */
public class BatchProjection extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchProjection.class);
    
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
    public BatchProjection(final BigMLClient bigmlClient,
    					   final String apiUser, final String apiKey, 
    					   final String project, final String organization,
    					   final CacheManager cacheManager) {
		super.init(bigmlClient, apiUser, apiKey, project, organization,
				   cacheManager, BATCH_PROJECTION_RE, 
				   BATCH_PROJECTION_PATH);
    }
    
    /**
     * Creates a new batch projection.
     *
     * POST /andromeda/batchprojection?username=$BIGML_USERNAME&api_key=$BIGML_API_KEY&
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param pcaId
     *            a unique identifier in the form pca/id where id is a
     *            string of 24 alpha-numeric chars for the pca.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset.
     * @param args
     *            set of parameters for the new batch projection. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for pcs before to start to create the batch projection. 
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     * @return a JSONObject for the new batch projection
     */
    public JSONObject create(final String pcaId, final String datasetId,
            JSONObject args, Integer waitTime, Integer retries) {
        if (pcaId == null || pcaId.length() == 0
                || !(pcaId.matches(PCA_RE))) {
            logger.info("Wrong pca id");
            return null;
        }

        if (datasetId == null || datasetId.length() == 0
                || !(datasetId.matches(DATASET_RE))) {
            logger.info("Wrong dataset id");
            return null;
        }

        try {
        	waitForResource(pcaId, "pcaIsReady", waitTime, retries);
        	waitForResource(datasetId, "datasetIsReady", waitTime, retries);
        	
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            requestObject.put("pca", pcaId);
            requestObject.put("dataset", datasetId);

            return createResource(resourceUrl,
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating batch projection");
            return null;
        }
    }
    
    /**
     * Retrieves the batch projection file.
     *
     * Downloads projections, that are stored in a remote CSV file. If a 
     * path is given in filename, the contents of the file are downloaded 
     * and saved locally. A file-like object is returned otherwise.
     *
     * @param batchProjectionId
     *            a unique identifier in the form batchprojection/id  
     *            where id is a string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     * @return a JSONObject for the downloaded batch projection
     */
    public JSONObject downloadBatchProjection(
    		final String batchProjectionId,
            final String filename) {

        if (batchProjectionId == null || batchProjectionId.length() == 0
                || !batchProjectionId.matches(BATCH_PROJECTION_RE)) {
            logger.info("Wrong batch projection id");
            return null;
        }

        String url = BIGML_URL + batchProjectionId + DOWNLOAD_DIR;
        return download(url, filename);
    }

    /**
     * Retrieves the batch projection file.
     *
     * Downloads projections, that are stored in a remote CSV file. If a 
     * path is given in filename, the contents of the file are downloaded 
     * and saved locally. A file-like object is returned otherwise.
     *
     * @param batchProjectionJSON
     *            a batch projection JSONObject.
     * @param filename
     *            Path to save file locally
     *
     * @return a JSONObject for the downloaded batch projection
     */
    public JSONObject downloadBatchProjection(
    		final JSONObject batchProjectionJSON,
            final String filename) {
        String resourceId = (String) batchProjectionJSON.get("resource");
        return downloadBatchProjection(resourceId, filename);
    }
    
}
