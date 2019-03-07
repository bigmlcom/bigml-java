package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete Fusions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/fusions
 *
 *
 */
public class Fusion extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Fusion.class);
    
    /**
     * Constructor
     *
     * @deprecated
     */
	public Fusion() {
		super.init(null, null, null, null, null, 
				FUSION_RE, FUSION_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Fusion(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				FUSION_RE, FUSION_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Fusion(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				FUSION_RE, FUSION_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public Fusion(final BigMLClient bigmlClient,
    			  final String apiUser, final String apiKey, 
    			  final String project, final String organization,
    			  final CacheManager cacheManager) {
        super.init(bigmlClient, apiUser, apiKey, project, organization, 
        		   cacheManager, FUSION_RE, FUSION_PATH);
    }
    
    
    /**
     * Creates a fusion from a list of models.
     * Available models types: deepnet, ensemble, fusion, model,
     * logisticregression and linearregression.
     *
     * POST /andromeda/fusion?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param modelsIds
     *            list of identifiers in the form xxx/id, where xxx is
     *            one of the model types availables and id is a string
     *            of 24 alpha-numeric chars for the model to include in
     *            the fusion resource.
     * @param args
     *            set of parameters for the new fusion. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED 
     *            status for every submodel before to start to create 
     *            the fusion. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(List<String> modelsIds, JSONObject args,
                             Integer waitTime, Integer retries) {

        if (modelsIds == null || modelsIds.size() == 0 ) {
            logger.info("A valid model id must be provided.");
            return null;
        }
        
        try {
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            
            // Checking valid submodels Ids
            for (String modelId : modelsIds) {
            	if (!validateModel(modelId, waitTime, retries)) {
            		return null;
            	}
            }
            
            requestObject.put("models", modelsIds);
            return createResource(resourceUrl, 
            		requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating fusion");
            return null;
        }
    }
    
    
    /**
     * Creates a fusion from a list of models definitions.
     *
     * POST /andromeda/fusion?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param models
     *            list of models definitions int he form
     *            
     *           {
     *           	"id": xxx/id, where xxx is
     *            			one of the model types availables and id is a string
     *            			of 24 alpha-numeric chars for the model to include in
     *            			the fusion resource,
     *            	"weight": a number specifying the weight of the submodel 
     *            			in the fusion
     *            }
     * @param args
     *            set of parameters for the new fusion. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED 
     *            status for every submodel before to start to create 
     *            the fusion. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createWithModels(final List<JSONObject> models,
        JSONObject args, Integer waitTime, Integer retries) {

    	if (models == null || models.size() == 0 ) {
            logger.info("A valid model configuration must be provided.");
            return null;
        }
        
        try {
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }
            
            for (JSONObject model : models) {
            	String modelId = (String) model.get("id");
            	if (!validateModel(modelId, waitTime, retries)) {
            		return null;
            	}
            }
            
            requestObject.put("models", models);
            return createResource(resourceUrl, 
            		requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating fusion");
            return null;
        }
    }
    
    
    private Boolean validateModel(String modelId, Integer waitTime, Integer retries) {
    	// Checking valid submodels Ids
        if (modelId == null || modelId.length() == 0
                || !(modelId.matches(DEEPNET_RE) || 
                		modelId.matches(MODEL_RE) || 
                		modelId.matches(ENSEMBLE_RE) || 
                		modelId.matches(FUSION_RE) || 
                		modelId.matches(LOGISTICREGRESSION_RE) ||
                		modelId.matches(LINEARREGRESSION_RE))) {
            logger.info("Wrong submodel id");
            return false;
        }
        
        if (modelId.matches(ENSEMBLE_RE)) {
        		waitForResource(modelId, "ensembleIsReady", waitTime, retries);
        }

        if (modelId.matches(MODEL_RE)) {
        		waitForResource(modelId, "modelIsReady", waitTime, retries);
        }

        if (modelId.matches(LOGISTICREGRESSION_RE)) {
        		waitForResource(modelId, "logisticRegressionIsReady", waitTime, retries);
        }
        
        if (modelId.matches(LINEARREGRESSION_RE)) {
    		waitForResource(modelId, "linearRegressionIsReady", waitTime, retries);
        }
        
        if (modelId.matches(DEEPNET_RE)) {
        		waitForResource(modelId, "deepnetIsReady", waitTime, retries);
        }
        
        if (modelId.matches(FUSION_RE)) {
        		waitForResource(modelId, "fusionIsReady", waitTime, retries);
        }
        return true;
    }
    
}
