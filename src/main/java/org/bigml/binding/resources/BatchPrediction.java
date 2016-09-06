package org.bigml.binding.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete batch predictions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/batch_predictions
 *
 *
 */
public class BatchPrediction extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchPrediction.class);

    /**
     * Constructor
     *
     */
    public BatchPrediction() {
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
    public BatchPrediction(final String apiUser, final String apiKey,
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
    public BatchPrediction(final String apiUser, final String apiKey,
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
     * Check if the current resource is an BatchPrediction
     *
     * @param resource the resource to be checked
     * @return true if its an BatchPrediction
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(BATCH_PREDICTION_RE);
    }

    /**
     * Creates a new batch prediction.
     *
     * POST /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new batch prediction. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the batch prediction.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final String model,
            final String datasetId, String args, Integer waitTime,
            Integer retries) {
        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);
        return create(model, datasetId, argsJSON, waitTime, retries);
    }

    /**
     * Creates a new batch prediction.
     *
     * POST /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new batch prediction. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the batch prediction.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String model,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {

        if (model == null || model.length() == 0 ||
            !(model.matches(MODEL_RE) || model.matches(ENSEMBLE_RE) || model.matches(LOGISTICREGRESSION_RE))) {
            logger.info("Wrong model, ensemble or logisticregression id");
            return null;
        }
        if (datasetId == null || datasetId.length() == 0
                || !datasetId.matches(DATASET_RE)) {
            logger.info("Wrong dataset id");
            return null;
        }

        try {
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;
            if (waitTime > 0) {
                int count = 0;

                if (model.matches(MODEL_RE)) {
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                                    .modelIsReady(model)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (model.matches(ENSEMBLE_RE)) {
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                                    .ensembleIsReady(model)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (model.matches(LOGISTICREGRESSION_RE)) {
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                                    .logisticRegressionIsReady(model)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                count = 0;
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

            if (model.matches(MODEL_RE)) {
                requestObject.put("model", model);
            }
            if (model.matches(ENSEMBLE_RE)) {
                requestObject.put("ensemble", model);
            }
            if (model.matches(LOGISTICREGRESSION_RE)) {
                requestObject.put("logisticregression", model);
            }
            requestObject.put("dataset", datasetId);

            return createResource(BATCH_PREDICTION_URL,
                    requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating batch prediction");
            return null;
        }
    }

    /**
     * Retrieves a batch prediction.
     *
     * GET /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     *
     * @param batchPredictionId
     *            a unique identifier in the form batchPrediction/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String batchPredictionId) {
        if (batchPredictionId == null || batchPredictionId.length() == 0
                || !batchPredictionId.matches(BATCH_PREDICTION_RE)) {
            logger.info("Wrong batch prediction id");
            return null;
        }

        return getResource(BIGML_URL + batchPredictionId);
    }

    /**
     * Retrieves a batch prediction.
     *
     * GET /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     *
     * @param batchPrediction
     *            a batch prediction JSONObject.
     *
     */
    @Override
    public JSONObject get(final JSONObject batchPrediction) {
        String resourceId = (String) batchPrediction.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves the batch predictions file.
     *
     * Downloads predictions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchPredictionId
     *            a unique identifier in the form batchPrediction/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchPrediction(final String batchPredictionId,
            final String filename) {

        if (batchPredictionId == null || batchPredictionId.length() == 0
                || !batchPredictionId.matches(BATCH_PREDICTION_RE)) {
            logger.info("Wrong batch prediction id");
            return null;
        }

        String url = BIGML_URL + batchPredictionId + DOWNLOAD_DIR;
        return download(url, filename);
    }

    /**
     * Retrieves the batch predictions file.
     *
     * Downloads predictions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchPrediction
     *            a batch prediction JSONObject.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchPrediction(final JSONObject batchPrediction,
            final String filename) {
        String resourceId = (String) batchPrediction.get("resource");
        return downloadBatchPrediction(resourceId, filename);
    }

    /**
     * Check whether a batch prediction's status is FINISHED.
     *
     * @param batchPredictionId
     *            a unique identifier in the form batchPrediction/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String batchPredictionId) {
        return isResourceReady(get(batchPredictionId));
    }

    /**
     * Check whether a batch prediction's status is FINISHED.
     *
     * @param batchPrediction
     *            a batchPrediction JSONObject.
     *
     */
    @Override
    public boolean isReady(final JSONObject batchPrediction) {
        String resourceId = (String) batchPrediction.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your batch predictions.
     *
     * GET /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(BATCH_PREDICTION_URL, queryString);
    }

    /**
     * Updates a batch prediction.
     *
     * PUT /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchPredictionId
     *            a unique identifier in the form batchprediction/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the evaluation. Optional
     *
     */
    @Override
    public JSONObject update(final String batchPredictionId,
            final String changes) {
        if (batchPredictionId == null || batchPredictionId.length() == 0
                || !batchPredictionId.matches(BATCH_PREDICTION_RE)) {
            logger.info("Wrong batch prediction id");
            return null;
        }
        return updateResource(BIGML_URL + batchPredictionId, changes);
    }

    /**
     * Updates a batch prediction.
     *
     * PUT /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchPrediction
     *            a batchPrediction JSONObject
     * @param changes
     *            set of parameters to update the batch prediction. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject batchPrediction,
            final JSONObject changes) {
        String resourceId = (String) batchPrediction.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a batch prediction.
     *
     * DELETE /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchPredictionId
     *            a unique identifier in the form batchprediction/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String batchPredictionId) {
        if (batchPredictionId == null || batchPredictionId.length() == 0
                || !batchPredictionId.matches(BATCH_PREDICTION_RE)) {
            logger.info("Wrong batch prediction id");
            return null;
        }
        return deleteResource(BIGML_URL + batchPredictionId);
    }

    /**
     * Deletes a batch prediction.
     *
     * DELETE /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchPrediction
     *            a batchPrediction JSONObject.
     *
     */
    @Override
    public JSONObject delete(final JSONObject batchPrediction) {
        String resourceId = (String) batchPrediction.get("resource");
        return delete(resourceId);
    }

}