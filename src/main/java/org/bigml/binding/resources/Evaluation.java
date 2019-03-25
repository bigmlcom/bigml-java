package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
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
     * @deprecated
     */
	public Evaluation() {
		super.init(null, null, null, null, null, 
				EVALUATION_RE, EVALUATION_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Evaluation(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				EVALUATION_RE, EVALUATION_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Evaluation(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				EVALUATION_RE, EVALUATION_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public Evaluation(final BigMLClient bigmlClient,
    				  final String apiUser, final String apiKey, 
    				  final String project, final String organization,
    				  final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization, 
    				   cacheManager, EVALUATION_RE, EVALUATION_PATH);
    }

    /**
     * Create a new evaluation.
     *
     * POST
     * /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a unique identifier in the form model/id, ensemble/id,
     *            logisticregression/id, fusion/id or linearregression/id 
     *            where id is a string of 24 alpha-numeric chars for the 
     *            model, ensemble, logisticregression, fusion or
     *            linearregression to attach the evaluation.
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
    		!(model.matches(MODEL_RE) || 
              	  model.matches(ENSEMBLE_RE) || 
              	  model.matches(LOGISTICREGRESSION_RE)  || 
              	  model.matches(LINEARREGRESSION_RE) ||
              	  model.matches(FUSION_RE))) {
              logger.info("Wrong model, ensemble, logisticregression, fusion "
              		+ "or linearregression id");
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
            if (model.matches(FUSION_RE)) {
                requestObject.put("fusion", model);
            }
            requestObject.put("dataset", datasetId);

            return createResource(resourceUrl, 
            		requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating evaluation");
            return null;
        }
    }

}