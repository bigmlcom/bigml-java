package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete evaluations.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/evaluations
 *
 *
 */
public class Evaluation extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Evaluation.class);

    /**
     * Constructor
     *
     */
    public Evaluation() {
    		super.init(null, null, false, null);
        this.resourceRe = EVALUATION_RE;
        this.resourceUrl = EVALUATION_URL;
        this.resourceName = "evaluation";
    }

    /**
     * Constructor
     *
     */
    public Evaluation(final String apiUser, final String apiKey,
            final boolean devMode) {
    		super.init(apiUser, apiKey, devMode, null);
        this.resourceRe = EVALUATION_RE;
        this.resourceUrl = EVALUATION_URL;
        this.resourceName = "evaluation";
    }

    /**
     * Constructor
     *
     */
    public Evaluation(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, devMode, cacheManager);
        this.resourceRe = EVALUATION_RE;
        this.resourceUrl = EVALUATION_URL;
        this.resourceName = "evaluation";
    }

    /**
     * Create a new evaluation.
     *
     * POST
     * /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
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
     *            set of parameters for the new evaluation. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the evaluation. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final String model, final String datasetId,
            String args, Integer waitTime, Integer retries) {
        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);
        return create(model, datasetId, argsJSON, waitTime, retries);
    }

    /**
     * Create a new evaluation.
     *
     * POST
     * /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
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
     *            set of parameters for the new evaluation. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the evaluation. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String model, final String datasetId,
            JSONObject args, Integer waitTime, Integer retries) {

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

            return createResource(EVALUATION_URL, requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating evaluation");
            return null;
        }
    }

}