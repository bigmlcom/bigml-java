package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete batch predictions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/batch_predictions
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
    	super.init(null, null, null);
        this.resourceRe = BATCH_PREDICTION_RE;
        this.resourceUrl = BATCH_PREDICTION_URL;
        this.resourceName = "batch prediction";
    }

    /**
     * Constructor
     *
     */
    public BatchPrediction(final String apiUser, final String apiKey) {
    	super.init(apiUser, apiKey, null);
        this.resourceRe = BATCH_PREDICTION_RE;
        this.resourceUrl = BATCH_PREDICTION_URL;
        this.resourceName = "batch prediction";
    }

    /**
     * Constructor
     *
     */
    public BatchPrediction(final String apiUser, final String apiKey, final CacheManager cacheManager) {
    	super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = BATCH_PREDICTION_RE;
        this.resourceUrl = BATCH_PREDICTION_URL;
        this.resourceName = "batch prediction";
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
                            && !BigMLClient.getInstance().modelIsReady(model)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (model.matches(ENSEMBLE_RE)) {
                    while (count < retries
                            && !BigMLClient.getInstance().ensembleIsReady(model)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (model.matches(LOGISTICREGRESSION_RE)) {
                    while (count < retries
                            && !BigMLClient.getInstance().logisticRegressionIsReady(model)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                count = 0;
                while (count < retries
                        && !BigMLClient.getInstance().datasetIsReady(datasetId)) {
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

}
