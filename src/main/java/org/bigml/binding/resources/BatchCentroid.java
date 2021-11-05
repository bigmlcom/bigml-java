package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete
 * batch centroids.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/batchcentroids
 *
 *
 */
public class BatchCentroid extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchCentroid.class);

    public final static String DOWNLOAD_DIR = "/download";
    
    
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
    public BatchCentroid(final BigMLClient bigmlClient,
    					 final String apiUser, final String apiKey, 
    					 final String project, final String organization,
    					 final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization, 
    				   cacheManager, BATCH_CENTROID_RE, BATCH_CENTROID_PATH);
    }

    /**
     * Creates a new batchcentroid.
     *
     * POST /andromeda/batchcentroid?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars for the cluster.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset.
     * @param args
     *            set of parameters for the new batchcentroid. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for centroid before to start to create the
     *            batchcentroid. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     * @return a JSONObject for the new batch centroid
     */
    public JSONObject create(final String clusterId, final String datasetId,
            JSONObject args, Integer waitTime, Integer retries) {
        if (clusterId == null || clusterId.length() == 0
                || !(clusterId.matches(CLUSTER_RE))) {
            logger.info("Wrong cluster id");
            return null;
        }

        if (datasetId == null || datasetId.length() == 0
                || !(datasetId.matches(DATASET_RE))) {
            logger.info("Wrong dataset id");
            return null;
        }

        try {
        		waitForResource(clusterId, "clusterIsReady", waitTime, retries);
        		waitForResource(datasetId, "datasetIsReady", waitTime, retries);
        	
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            requestObject.put("cluster", clusterId);
            requestObject.put("dataset", datasetId);

            return createResource(resourceUrl,
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating batchcentroid");
            return null;
        }
    }

    /**
     * Retrieves the batch centroid file.
     *
     * Downloads predictions, that are stored in a remote CSV file. If a 
     * path is given in filename, the contents of the file are 
     * downloaded and saved locally. A file-like object is returned 
     * otherwise.
     *
     * @param batchCentroidId
     *            a unique identifier in the form batchCentroid/id where 
     *            id is a string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     * @return a JSONObject for the downloaded batch centroid
     */
    public JSONObject downloadBatchCentroid(final String batchCentroidId,
            final String filename) {

        if (batchCentroidId == null || batchCentroidId.length() == 0
                || !batchCentroidId.matches(BATCH_CENTROID_RE)) {
            logger.info("Wrong batch centroid id");
            return null;
        }

        String url = BIGML_URL + batchCentroidId + DOWNLOAD_DIR;
        return download(url, filename);
    }

    /**
     * Retrieves the batch centroid file.
     *
     * Downloads predictions, that are stored in a remote CSV file. If a 
     * path is given in filename, the contents of the file are 
     * downloaded and saved locally. A file-like object is returned 
     * otherwise.
     *
     * @param batchCentroid
     *            a batch centroid JSONObject.
     * @param filename
     *            Path to save file locally
     *
     * @return a JSONObject for the downloaded batch centroid
     */
    public JSONObject downloadBatchCentroid(
    		final JSONObject batchCentroid, final String filename) {

        String resourceId = (String) batchCentroid.get("resource");
        if (resourceId != null) {
            String url = BIGML_URL + resourceId + DOWNLOAD_DIR;
            return download(url, filename);
        }
        return null;
    }

}
