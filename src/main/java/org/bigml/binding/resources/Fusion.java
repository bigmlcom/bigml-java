package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.BigMLClient;
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
        super.init(null, null, null);
        this.resourceRe = FUSION_RE;
        this.resourceUrl = FUSION_URL;
        this.resourceName = "fusion";
    }

    /**
     * Constructor
     *
     */
    public Fusion(final String apiUser, final String apiKey) {
        super.init(apiUser, apiKey, null);
        this.resourceRe = FUSION_RE;
        this.resourceUrl = FUSION_URL;
        this.resourceName = "fusion";
    }


    /**
     * Constructor
     *
     */
    public Fusion(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
        super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = FUSION_RE;
        this.resourceUrl = FUSION_URL;
        this.resourceName = "fusion";
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
            
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;

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
                    JSONObject ensembleObj = BigMLClient.getInstance().getEnsemble(model);

                    if (ensembleObj != null) {
                        if (waitTime > 0) {
                            int count = 0;
                            while (count < retries
                                    && !BigMLClient.getInstance()
                                    .ensembleIsReady(ensembleObj)) {
                                Thread.sleep(waitTime);
                                count++;
                            }
                        }
                    }
                }

                if (model.matches(MODEL_RE)) {
                    JSONObject modelObj = BigMLClient.getInstance().getModel(model);
                    if (modelObj != null) {
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
                
                if (model.matches(DEEPNET_RE)) {
                    JSONObject deepnetObj = BigMLClient.getInstance().getDeepnet(model);
                    if (deepnetObj != null) {
                        if (waitTime > 0) {
                            int count = 0;
                            while (count < retries
                                    && !BigMLClient.getInstance().deepnetIsReady(deepnetObj)) {
                                Thread.sleep(waitTime);
                                count++;
                            }
                        }
                    }
                }
                
                if (model.matches(FUSION_RE)) {
                    JSONObject fusionObj = BigMLClient.getInstance().getFusion(model);
                    if (fusionObj != null) {
                        if (waitTime > 0) {
                            int count = 0;
                            while (count < retries
                                    && !BigMLClient.getInstance().fusionIsReady(fusionObj)) {
                                Thread.sleep(waitTime);
                                count++;
                            }
                        }
                    }
                }

            }
            
            requestObject.put("models", modelsIds);
            return createResource(FUSION_URL, requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating evaluation");
            return null;
        }
    }
}
