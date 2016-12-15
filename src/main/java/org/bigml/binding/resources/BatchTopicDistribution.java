package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
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
     */
    public BatchTopicDistribution() {
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
    public BatchTopicDistribution(final String apiUser, final String apiKey,
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
    public BatchTopicDistribution(final String apiUser, final String apiKey,
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
     * Check if the current resource is a BatchTopicDistribution
     *
     * @param resource the resource to be checked
     * @return true if it's a BatchTopicDistribution
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(BATCH_TOPICDISTRIBUTION_RE);
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
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;
            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .topicModelIsReady(topicModelId)) {
                    Thread.sleep(waitTime);
                    count++;
                }
            }

            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .datasetIsReady(datasetId)) {
                    Thread.sleep(waitTime);
                    count++;
                }
            }

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            requestObject.put("topicmodel", topicModelId);
            requestObject.put("dataset", datasetId);

            return createResource(BATCH_TOPICDISTRIBUTION_URL,
                    requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating batch topic distribution");
            return null;
        }
    }

    /**
     * Retrieves a batch topic distribution.
     *
     * A batch topic distribution is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the batch topic distribution values and state info available at the time it
     * is called.
     *
     * GET /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String batchTopicDistributionId) {
        if (batchTopicDistributionId == null || batchTopicDistributionId.length() == 0
                || !batchTopicDistributionId.matches(BATCH_TOPICDISTRIBUTION_RE)) {
            logger.info("Wrong batch topic distribution id");
            return null;
        }

        return getResource(BIGML_URL + batchTopicDistributionId);
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

    /**
     * Retrieves a batch topic distribution.
     *
     * A batch topic distribution is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the batch topic distribution values and state info available at the time it
     * is called.
     *
     * GET /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param batchTopicDistribution
     *            a batch topic distribution JSONObject.
     *
     */
    @Override
    public JSONObject get(final JSONObject batchTopicDistribution) {
        String batchTopicDistributionId = (String) batchTopicDistribution.get("resource");
        return get(batchTopicDistributionId);
    }

    /**
     * Check whether a batch topic distribution's status is FINISHED.
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String batchTopicDistributionId) {
        return isResourceReady(get(batchTopicDistributionId));
    }

    /**
     * Check whether a batch topic distribution's status is FINISHED.
     *
     * @param batchTopicDistribution
     *            a batch topic distribution JSONObject.
     *
     */
    @Override
    public boolean isReady(final JSONObject batchTopicDistribution) {
        String resourceId = (String) batchTopicDistribution.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your batch topic distribution.
     *
     * GET /andromeda/batchtopicdistribution?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(BATCH_TOPICDISTRIBUTION_URL, queryString);
    }

    /**
     * Updates a batch topic distribution.
     *
     * PUT /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the batch topic distribution. Optional
     *
     */
    @Override
    public JSONObject update(final String batchTopicDistributionId, final String changes) {
        if (batchTopicDistributionId == null || batchTopicDistributionId.length() == 0
                || !batchTopicDistributionId.matches(BATCH_TOPICDISTRIBUTION_RE)) {
            logger.info("Wrong batch topic distribution id");
            return null;
        }
        return updateResource(BIGML_URL + batchTopicDistributionId, changes);
    }

    /**
     * Updates a batch topic distribution.
     *
     * PUT /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchTopicDistribution
     *            a batch topic distribution JSONObject
     * @param changes
     *            set of parameters to update the batch topic distribution. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject batchTopicDistribution,
            final JSONObject changes) {
        String resourceId = (String) batchTopicDistribution.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a batch topic distribution.
     *
     * DELETE /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String batchTopicDistributionId) {
        if (batchTopicDistributionId == null || batchTopicDistributionId.length() == 0
                || !batchTopicDistributionId.matches(BATCH_TOPICDISTRIBUTION_RE)) {
            logger.info("Wrong batch topic distribution id");
            return null;
        }
        return deleteResource(BIGML_URL + batchTopicDistributionId);
    }

    /**
     * Deletes a batch topic distribution.
     *
     * DELETE /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchTopicDistribution
     *            a batch topic distribution JSONObject.
     *
     */
    @Override
    public JSONObject delete(final JSONObject batchTopicDistribution) {
        String resourceId = (String) batchTopicDistribution.get("resource");
        return delete(resourceId);
    }

}
