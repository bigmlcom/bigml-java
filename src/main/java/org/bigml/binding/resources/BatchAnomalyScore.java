package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete
 * batchanomalyscores.
 * 
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/batch_anomalyscores
 * 
 * 
 */
public class BatchAnomalyScore extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchAnomalyScore.class);

    /**
     * Constructor
     *
     */
    public BatchAnomalyScore() {
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init();
    }

    /**
     * Constructor
     *
     */
    public BatchAnomalyScore(final String apiUser, final String apiKey,
                             final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init();
    }

    /**
     * Check if the current resource is an BatchAnomalyScore
     *
     * @param resource the resource to be checked
     * @return true if its an BatchAnomalyScore
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(BATCH_ANOMALYSCORE_RE);
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
     *            for batchanomalyscore before to start to create the
     *            batchbatchcentroid. Optional
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
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;
            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .anomalyIsReady(anomalyId)) {
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
            requestObject.put("anomaly", anomalyId);
            requestObject.put("dataset", datasetId);

            return createResource(BATCHANOMALYSCORE_URL,
                    requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating batchanomalyscore");
            return null;
        }
    }

    /**
     * Retrieves a batchanomalyscore.
     * 
     * A batchanomalyscore is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the batchanomalyscore values and state info available at the time it
     * is called.
     * 
     * GET /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject get(final String batchAnomalyScoreId) {
        if (batchAnomalyScoreId == null || batchAnomalyScoreId.length() == 0
                || !batchAnomalyScoreId.matches(BATCH_ANOMALYSCORE_RE)) {
            logger.info("Wrong batchanomalyscore id");
            return null;
        }

        return getResource(BIGML_URL + batchAnomalyScoreId);
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

    /**
     * Retrieves a batchanomalyscore.
     * 
     * A batchanomalyscore is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the batchanomalyscore values and state info available at the time it
     * is called.
     * 
     * GET /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param batchAnomalyScore
     *            a batchanomalyscore JSONObject.
     * 
     */
    @Override
    public JSONObject get(final JSONObject batchAnomalyScore) {
        String batchAnomalyScoreId = (String) batchAnomalyScore.get("resource");
        return get(batchAnomalyScoreId);
    }

    /**
     * Check whether a batchanomalyscore's status is FINISHED.
     * 
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public boolean isReady(final String batchAnomalyScoreId) {
        return isResourceReady(get(batchAnomalyScoreId));
    }

    /**
     * Check whether a batchanomalyscore's status is FINISHED.
     * 
     * @param batchAnomalyScore
     *            a batchanomalyscore JSONObject.
     * 
     */
    @Override
    public boolean isReady(final JSONObject batchAnomalyScore) {
        String resourceId = (String) batchAnomalyScore.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your batchanomalyscore.
     * 
     * GET /andromeda/batchanomalyscore?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(BATCHANOMALYSCORE_URL, queryString);
    }

    /**
     * Updates a batchanomalyscore.
     * 
     * PUT /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the batchanomalyscore. Optional
     * 
     */
    @Override
    public JSONObject update(final String batchAnomalyScoreId, final String changes) {
        if (batchAnomalyScoreId == null || batchAnomalyScoreId.length() == 0
                || !batchAnomalyScoreId.matches(BATCH_ANOMALYSCORE_RE)) {
            logger.info("Wrong batchanomalyscore id");
            return null;
        }
        return updateResource(BIGML_URL + batchAnomalyScoreId, changes);
    }

    /**
     * Updates a batchanomalyscore.
     * 
     * PUT /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param batchAnomalyScore
     *            an batchanomalyscore JSONObject
     * @param changes
     *            set of parameters to update the batchanomalyscore. Optional
     * 
     */
    @Override
    public JSONObject update(final JSONObject batchAnomalyScore,
            final JSONObject changes) {
        String resourceId = (String) batchAnomalyScore.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a batchanomalyscore.
     * 
     * DELETE /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject delete(final String batchAnomalyScoreId) {
        if (batchAnomalyScoreId == null || batchAnomalyScoreId.length() == 0
                || !batchAnomalyScoreId.matches(BATCH_ANOMALYSCORE_RE)) {
            logger.info("Wrong batchanomalyscore id");
            return null;
        }
        return deleteResource(BIGML_URL + batchAnomalyScoreId);
    }

    /**
     * Deletes a batchanomalyscore.
     * 
     * DELETE /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param batchAnomalyScore
     *            an batchanomalyscore JSONObject.
     * 
     */
    @Override
    public JSONObject delete(final JSONObject batchAnomalyScore) {
        String resourceId = (String) batchAnomalyScore.get("resource");
        return delete(resourceId);
    }

}