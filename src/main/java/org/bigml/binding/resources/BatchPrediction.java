package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
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
     * @param bigmlClient	the client with connection to BigML
     * @param apiUser		API user
     * @param apiKey		API key
     * @param project		project id
     * @param organization	organization id
     * @param cacheManager	cache manager
     */
    public BatchPrediction(final BigMLClient bigmlClient,
    					   final String apiUser, final String apiKey, 
    					   final String project, final String organization,
    					   final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization,
    				   cacheManager, BATCH_PREDICTION_RE, BATCH_PREDICTION_PATH);
    }

    /**
     * Creates a new batch prediction.
     *
     * POST /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a unique identifier in the form model/id, ensemble/id,
     *            logisticregression/id or linearregression/id where id 
     *            is a string of 24 alpha-numeric chars for the model, 
     *            ensemble, logisticregression or linearregression/id to
     *            attach the batchprediction.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to 
     *            attach the batchprediction.
     * @param args
     *            set of parameters for the new batch prediction. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the batch prediction.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     *@return a JSONObject for the new batch prediction
     */
    public JSONObject create(final String model,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {

        if (model == null || model.length() == 0 ||
                !(model.matches(MODEL_RE) || 
                  model.matches(ENSEMBLE_RE) || 
                  model.matches(LOGISTICREGRESSION_RE) || 
                  model.matches(LINEARREGRESSION_RE) || 
                  model.matches(DEEPNET_RE) ||
                  model.matches(FUSION_RE))) {
                logger.info("Wrong model, ensemble, logisticregression, "
                		+ "linearregression or deepnet or fusion id");
                return null;
            }
        
        if (datasetId == null || datasetId.length() == 0
                || !datasetId.matches(DATASET_RE)) {
            logger.info("Wrong dataset id");
            return null;
        }

        try {
        		if (model.matches(MODEL_RE)) {
        			waitForResource(model, "modelIsReady", waitTime, retries);
        		}
        		
        		if (model.matches(ENSEMBLE_RE)) {
        			waitForResource(model, "ensembleIsReady", waitTime, retries);
        		}
        		
        		if (model.matches(LOGISTICREGRESSION_RE)) {
        			waitForResource(model, "logisticRegressionIsReady", waitTime, retries);
        		}
        		
        		if (model.matches(LINEARREGRESSION_RE)) {
        			waitForResource(model, "linearRegressionIsReady", waitTime, retries);
        		}
        		
        		if (model.matches(DEEPNET_RE)) {
        			waitForResource(model, "deepnetIsReady", waitTime, retries);
        		}
        		
        		if (model.matches(FUSION_RE)) {
        			waitForResource(model, "fusionIsReady", waitTime, retries);
        		}
        		
        		waitForResource(datasetId, "datasetIsReady", waitTime, retries);
            

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
            if (model.matches(LINEARREGRESSION_RE)) {
                requestObject.put("linearregression", model);
            }
            if (model.matches(DEEPNET_RE)) {
                requestObject.put("deepnet", model);
            }
            if (model.matches(FUSION_RE)) {
                requestObject.put("fusion", model);
            }
            requestObject.put("dataset", datasetId);

            return createResource(resourceUrl,
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
     * @return a JSONObject for the downloaded batch prediction
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
     * @return a JSONObject for the downloaded batch prediction
     */
    public JSONObject downloadBatchPrediction(final JSONObject batchPrediction,
            final String filename) {
        String resourceId = (String) batchPrediction.get("resource");
        return downloadBatchPrediction(resourceId, filename);
    }

}
