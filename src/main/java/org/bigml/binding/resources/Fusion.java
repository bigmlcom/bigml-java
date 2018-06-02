package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete Fusionss.
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
     */
    public Fusion() {
        super.init(null, null, null, 
        		FUSION_RE, FUSION_PATH);
    }

    /**
     * Constructor
     *
     */
    public Fusion(final String apiUser, final String apiKey) {
        super.init(apiUser, apiKey, null, 
        		FUSION_RE, FUSION_PATH);
    }


    /**
     * Constructor
     *
     */
    public Fusion(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
        super.init(apiUser, apiKey, cacheManager, 
        		FUSION_RE, FUSION_PATH);
    }
    
    
    /**
     * Creates a fusion from a list of models.
     * Available models types: deppnet, ensemble, fusion, model and
     * logisticregression.
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
            
            for (String model : modelsIds) {
                // Checking valid submodels Ids
                if (model == null || model.length() == 0
                        || !(model.matches(DEEPNET_RE) || 
                        		model.matches(MODEL_RE) || 
                        		model.matches(ENSEMBLE_RE) || 
                        		model.matches(FUSION_RE) || 
                        		model.matches(LOGISTICREGRESSION_RE))) {
                    logger.info("Wrong submodel id");
                    return null;
                }
                
                if (model.matches(ENSEMBLE_RE)) {
                		waitForResource(model, "ensembleIsReady", waitTime, retries);
                }

                if (model.matches(MODEL_RE)) {
                		waitForResource(model, "modelIsReady", waitTime, retries);
                }

                if (model.matches(LOGISTICREGRESSION_RE)) {
                		waitForResource(model, "logisticRegressionIsReady", waitTime, retries);
                }
                
                if (model.matches(DEEPNET_RE)) {
                		waitForResource(model, "deepnetIsReady", waitTime, retries);
                }
                
                if (model.matches(FUSION_RE)) {
                		waitForResource(model, "fusionIsReady", waitTime, retries);
                }

            }
            
            requestObject.put("models", modelsIds);
            return createResource(resourceUrl, 
            		requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating evaluation");
            return null;
        }
    }
    
}
