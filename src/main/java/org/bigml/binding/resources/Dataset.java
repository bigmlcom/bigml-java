package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
 * https://bigml.com/developers/dataset
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
    public Dataset() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Dataset(final String apiUser, final String apiKey,
            final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Dataset(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(cacheManager);
    }

    /**
     * Check if the current resource is an Dataset
     *
     * @param resource the resource to be checked
     * @return true if its an Dataset
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(DATASET_RE);
    }

    /**
     * Creates a remote dataset.
     *
     * Uses remote `source` to create a new dataset using the arguments in
     * `args`. If `wait_time` is higher than 0 then the dataset creation request
     * is not sent until the `source` has been created successfuly.
     *
     *
     * POST /andromeda/dataset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars for the source to attach the
     *            dataset.
     * @param args
     *            set of parameters for the new dataset. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the dataset. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final String sourceId, String args,
            Integer waitTime, Integer retries) {

        JSONObject argsJSON = args != null ? (JSONObject) JSONValue.parse(args)
                : null;
        return create(sourceId, argsJSON, waitTime, retries);

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
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                                    .sourceIsReady(resourceId)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (args != null) {
                    requestObject = args;
                }
                requestObject.put("source", resourceId);

            } else if( resourceId.matches(DATASET_RE) ) {
                // If the original resource is a Dataset
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                            .datasetIsReady(resourceId)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (args != null) {
                    requestObject = args;
                }
                requestObject.put("origin_dataset", resourceId);
            } else if( resourceId.matches(CLUSTER_RE) ) {
                // If the original resource is a Cluster
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                            .clusterIsReady(resourceId)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (args != null) {
                    requestObject = args;
                }

                if( !requestObject.containsKey("centroid") ) {
                    try {
                        JSONObject cluster = BigMLClient.getInstance(this.devMode).getCluster(resourceId);
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

            return createResource(DATASET_URL, requestObject.toJSONString());
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

            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;

            List originDatasetsIds = new ArrayList<String>();
            for (String resourceId : datasetsIds) {
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                            .datasetIsReady(resourceId)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }
                originDatasetsIds.add(resourceId);
            }

            if (args != null) {
                requestObject = args;
            }

            requestObject.put("origin_datasets", originDatasetsIds);

            return createResource(DATASET_URL, requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Failed to generate the dataset.", e);
            return null;
        }
    }

    /**
     * Retrieves a dataset.
     *
     * GET
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String datasetId) {
        if (datasetId == null || datasetId.length() == 0
                || !(datasetId.matches(DATASET_RE))) {
            logger.info("Wrong dataset id");
            return null;
        }

        return getResource(BIGML_URL + datasetId);
    }

    /**
     * Retrieves a dataset.
     *
     * GET
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param dataset
     *            a dataset JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject dataset) {
        String resourceId = (String) dataset.get("resource");
        return get(resourceId);
    }

    /**
     * Checks whether a dataset's status is FINISHED.
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String datasetId) {
        return isResourceReady(get(datasetId));
    }

    /**
     * Checks whether a dataset's status is FINISHED.
     *
     * @param dataset
     *            a dataset JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject dataset) {
        String resourceId = (String) dataset.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your datasources.
     *
     * GET /andromeda/dataset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(DATASET_URL, queryString);
    }

    /**
     * Updates a dataset.
     *
     * PUT
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final String datasetId, final String changes) {
        if (datasetId == null || datasetId.length() == 0
                || !(datasetId.matches(DATASET_RE))) {
            logger.info("Wrong dataset id");
            return null;
        }
        return updateResource(BIGML_URL + datasetId, changes);
    }

    /**
     * Updates a dataset.
     *
     * PUT
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param dataset
     *            a dataset JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject dataset, final JSONObject changes) {
        String resourceId = (String) dataset.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a dataset.
     *
     * DELETE
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String datasetId) {
        if (datasetId == null || datasetId.length() == 0
                || !(datasetId.matches(DATASET_RE))) {
            logger.info("Wrong dataset id");
            return null;
        }
        return deleteResource(BIGML_URL + datasetId);
    }

    /**
     * Deletes a dataset.
     *
     * DELETE
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param dataset
     *            a dataset JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject dataset) {
        String resourceId = (String) dataset.get("resource");
        return delete(resourceId);
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