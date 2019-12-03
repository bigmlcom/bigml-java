package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point to create, retrieve, list, update, and delete datasets.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/dataset
 *
 *
 */
public class Dataset extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Dataset.class);

    public final static String DOWNLOAD_DIR = "/download";
    
	
    /**
     * Constructor
     *
     */
    public Dataset(final BigMLClient bigmlClient,
    			   final String apiUser, final String apiKey, 
    			   final String project, final String organization,
    			   final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization,
    				  cacheManager, DATASET_RE, DATASET_PATH);
    }

    /**
     * Creates a remote dataset.
     *
     * Uses a remote resource to create a new dataset using the
     * arguments in `args`.
     *
     * If `wait_time` is higher than 0 then the dataset creation
     * request is not sent until the `source` has been created successfuly.
     *
     * @param resourceId
     *            a unique identifier in the form [source|dataset|cluster]/id
     *            where id is a string of 24 alpha-numeric chars for the
     *            remote resource to attach the dataset.
     * @param args
     *            set of parameters for the new dataset. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the dataset. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String resourceId, JSONObject args,
            Integer waitTime, Integer retries) {

        if (resourceId == null || resourceId.length() == 0 ) {
            logger.info("Wrong resource id. Id cannot be null");
            return null;
        }

        try {
            JSONObject requestObject = new JSONObject();

            // If the original resource is a Source
            if( resourceId.matches(SOURCE_RE) ) {
            		waitForResource(resourceId, "sourceIsReady", waitTime, retries);
            	
                if (args != null) {
                    requestObject = args;
                }
                requestObject.put("source", resourceId);

            } else if( resourceId.matches(DATASET_RE) ) {
                // If the original resource is a Dataset
            		waitForResource(resourceId, "datasetIsReady", waitTime, retries);
            	
                if (args != null) {
                    requestObject = args;
                }
                requestObject.put("origin_dataset", resourceId);
            } else if( resourceId.matches(CLUSTER_RE) ) {
                // If the original resource is a Cluster
            		waitForResource(resourceId, "clusterIsReady", waitTime, retries);
            	
                if (args != null) {
                    requestObject = args;
                }

                if( !requestObject.containsKey("centroid") ) {
                    try {
                        JSONObject cluster = this.bigmlClient.getCluster(resourceId);
                        JSONObject clusterDSIds = (JSONObject) Utils.
                                getJSONObject(cluster, "object.cluster_datasets_ids", null);
                        Object centroidId = clusterDSIds.keySet().toArray()[0];
                        args.put("centroid", centroidId);
                    } catch (Exception e) {
                        logger.error("Failed to generate the dataset." +
                                "A centroid id is needed in the args " +
                                "argument to generate a dataset from " +
                                "a cluster.", e);
                        return null;
                    }
                }

                requestObject.put("cluster", resourceId);
            }

            return createResource(resourceUrl, 
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Failed to generate the dataset.", e);
            return null;
        }
    }


    /**
     * Creates a remote dataset.
     *
     * Uses a lists of remote datasets to create a new dataset using the
     * arguments in `args`.
     *
     * If `wait_time` is higher than 0 then the dataset creation
     * request is not sent until the `source` has been created successfuly.
     *
     * @param datasetsIds
     *            list of dataset Ids.
     * @param args
     *            set of parameters for the new dataset. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for source
     *            before to start to create the dataset. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final List<String> datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        if (datasetsIds == null || datasetsIds.size() == 0 ) {
            logger.info("Wrong datasets ids. Ids cannot be null");
            return null;
        }

        try {
            JSONObject requestObject = new JSONObject();

            List originDatasetsIds = new ArrayList<String>();
            for (String resourceId : datasetsIds) {
            		waitForResource(resourceId, "datasetIsReady", waitTime, retries);
                originDatasetsIds.add(resourceId);
            }

            if (args != null) {
                requestObject = args;
            }

            requestObject.put("origin_datasets", originDatasetsIds);

            return createResource(resourceUrl, 
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Failed to generate the dataset.", e);
            return null;
        }
    }


    /**
     * Returns the ids of the fields that contain errors and their number.
     *
     * The dataset argument can be either a dataset resource structure or a
     * dataset id (that will be used to retrieve the associated remote
     * resource).
     *
     * @param dataset
     *            a dataset JSONObject
     *
     */
    public Map<String, Long> getErrorCounts(final JSONObject dataset) {
        Map<String, Long> errorsDict = new HashMap<String, Long>();

        JSONObject loadedDataset = get(dataset);
        if( loadedDataset != null ) {
            JSONObject errors = (JSONObject) Utils.getJSONObject(loadedDataset, "object.status.field_errors",
                    null);
            for (Object fieldId : errors.keySet()) {
                errorsDict.put(fieldId.toString(), ((Number) ((JSONObject) errors.get(fieldId)).get("total")).longValue());
            }
        }

        return errorsDict;
    }


    /**
     * Retrieves the dataset file.
     *
     * Downloads datasets, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadDataset(final String datasetId,
                                              final String filename) {

        if (datasetId == null || datasetId.length() == 0
                || !datasetId.matches(DATASET_RE)) {
            logger.info("Wrong dataset id");
            return null;
        }

        String url = BIGML_URL + datasetId + DOWNLOAD_DIR;
        return downloadAsync(url, filename);
    }

}
