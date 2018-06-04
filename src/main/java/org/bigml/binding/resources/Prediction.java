package org.bigml.binding.resources;

import java.util.Iterator;

import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
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
    		super.init(null, null, null, 
    			PREDICTION_RE, PREDICTION_PATH);
    }

    /**
     * Constructor
     *
     */
    public Prediction(final String apiUser, final String apiKey) {
    		super.init(apiUser, apiKey, null, 
    			PREDICTION_RE, PREDICTION_PATH);
    }

    /**
     * Constructor
     *
     */
    public Prediction(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, cacheManager, 
    			PREDICTION_RE, PREDICTION_PATH);
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
            		waitForResource(model, "ensembleIsReady", waitTime, retries);
            }

            if (model.matches(MODEL_RE)) {
            		waitForResource(model, "modelIsReady", waitTime, retries);
            }

            if (model.matches(LOGISTICREGRESSION_RE)) {
            		waitForResource(model, "logisticRegressionIsReady", waitTime, retries);
            }
            
            if (model.matches(FUSION_RE)) {
	        		waitForResource(model, "fusionIsReady", waitTime, retries);
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

            return createResource(resourceUrl, 
            		requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating prediction", e);
            return null;
        }

    }

}
