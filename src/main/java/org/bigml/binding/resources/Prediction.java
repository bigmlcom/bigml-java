package org.bigml.binding.resources;

import java.util.Iterator;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Entry point to create, retrieve, list, update, and delete predictions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/predictions
 *
 *
 */
public class Prediction extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Prediction.class);

    /**
     * Constructor
     *
     */
    public Prediction() {
    	super.init(null, null, null);
        this.resourceRe = PREDICTION_RE;
        this.resourceUrl = PREDICTION_URL;
        this.resourceName = "prediction";
    }

    /**
     * Constructor
     *
     */
    public Prediction(final String apiUser, final String apiKey) {
    	super.init(apiUser, apiKey, null);
        this.resourceRe = PREDICTION_RE;
        this.resourceUrl = PREDICTION_URL;
        this.resourceName = "prediction";
    }

    /**
     * Constructor
     *
     */
    public Prediction(final String apiUser, final String apiKey, final CacheManager cacheManager) {
    	super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = PREDICTION_RE;
        this.resourceUrl = PREDICTION_URL;
        this.resourceName = "prediction";
    }

    /**
     * Creates a new prediction.
     *
     * POST
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a prediction for.
     * @param byName
     * @param args
     *            set of parameters for the new prediction. Required
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the prediction. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final String model,
            JSONObject inputData, Boolean byName, String args,
            Integer waitTime, Integer retries) {
        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);
        return create(model, inputData, byName, argsJSON, waitTime, retries);
    }

    /**
     * Creates a new prediction.
     *
     * POST
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a prediction for.
     * @param byName
     * @param args
     *            set of parameters for the new prediction. Required
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the prediction. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String model,
            JSONObject inputData, Boolean byName, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject modelJSON = null;

        if (model == null || model.length() == 0 ||
            !(model.matches(MODEL_RE) || model.matches(ENSEMBLE_RE) || model.matches(LOGISTICREGRESSION_RE))) {
            logger.info("Wrong model, ensemble or logisticregression id");
            return null;
        }

        try {
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;

            if (model.matches(ENSEMBLE_RE)) {
                JSONObject ensembleObj = BigMLClient.getInstance().getEnsemble(model);

                if (ensembleObj != null) {
                    modelJSON = ensembleObj;
                    if (waitTime > 0) {
                        int count = 0;
                        while (count < retries
                                && !BigMLClient.getInstance().ensembleIsReady(ensembleObj)) {
                            Thread.sleep(waitTime);
                            count++;
                        }
                    }
                }
            }

            if (model.matches(MODEL_RE)) {
                JSONObject modelObj = BigMLClient.getInstance().getModel(model);
                if (modelObj != null) {
                    modelJSON = modelObj;
                    if (waitTime > 0) {
                        int count = 0;
                        while (count < retries
                                && !BigMLClient.getInstance().modelIsReady(modelObj)) {
                            Thread.sleep(waitTime);
                            count++;
                        }
                    }
                }
            }

            if (model.matches(LOGISTICREGRESSION_RE)) {
                JSONObject logisticRegressionObj =
                    BigMLClient.getInstance().getLogisticRegression(model);
                if (logisticRegressionObj != null) {
                    modelJSON = logisticRegressionObj;
                    if (waitTime > 0) {
                        int count = 0;
                        while (count < retries
                                && !BigMLClient.getInstance().logisticRegressionIsReady(logisticRegressionObj)) {
                            Thread.sleep(waitTime);
                            count++;
                        }
                    }
                }
            }

            // Input data
            JSONObject inputDataJSON = null;
            if (inputData == null) {
                inputDataJSON = new JSONObject();
            } else {
                if (byName && !model.matches(ENSEMBLE_RE)) {
                    JSONObject fields = (JSONObject) Utils.getJSONObject(modelJSON,
                            "object.model.fields");

                    if (fields != null) {
                        JSONObject invertedFields = Utils.invertDictionary(fields);
                        inputDataJSON = new JSONObject();
                        Iterator iter = inputData.keySet().iterator();
                        while (iter.hasNext()) {
                            String key = (String) iter.next();
                            if (invertedFields.get(key) != null) {
                                inputDataJSON.put( ((JSONObject) invertedFields.get(key)).get("fieldID"), inputData.get(key));
                            }
                        }
                    } else {
                        inputDataJSON = new JSONObject();
                    }

                } else {
                    inputDataJSON = inputData;
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

            requestObject.put("input_data", inputDataJSON);

            return createResource(PREDICTION_URL, requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating prediction", e);
            return null;
        }

    }

}
