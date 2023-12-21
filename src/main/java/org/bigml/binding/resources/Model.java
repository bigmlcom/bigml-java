package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete models.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/models
 *
 *
 */
public class Model extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Model.class);


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
    public Model(final BigMLClient bigmlClient,
    			 final String apiUser, final String apiKey,
    			 final String project, final String organization,
    			 final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization,
    				   cacheManager, MODEL_RE, MODEL_PATH);
    }

    /**
     * Creates a new model.
     *
     * POST /andromeda/model?username=$BIGML_USERNAME&api_key=$BIGML_API_KEY&
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param resourceId
     *            a unique identifier in the form [dataset|cluster]/id
     *            where id is a string of 24 alpha-numeric chars for the
     *            remote resource to attach the model.
     * @param args
     *            set of parameters for the new model. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the model. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String resourceId, JSONObject args,
            Integer waitTime, Integer retries) {

        if (resourceId == null || resourceId.length() == 0 ) {
            logger.info("Wrong resource id. Id cannot be null");
            return null;
        }

        try {
            if( resourceId.matches(DATASET_RE) ) {
                String[] datasetsIds = { resourceId };
                JSONObject requestObject = createFromDatasets(datasetsIds, args,
                    waitTime, retries, null);
                return createResource(resourceUrl,
                		requestObject.toJSONString());

            } else if( resourceId.matches(CLUSTER_RE) ) {
                JSONObject requestObject = new JSONObject();

                // If the original resource is a Cluster
                waitForResource(resourceId, "clusterIsReady", waitTime, retries);

                if (args != null) {
                    requestObject = args;
                }

                if( !requestObject.containsKey("centroid") ) {
                    try {
                        JSONObject cluster = this.bigmlClient.getCluster(resourceId);
                        JSONObject clusterModelsIds = (JSONObject) Utils.
                                getJSONObject(cluster, "object.cluster_models", null);
                        Object centroidId = clusterModelsIds.keySet().toArray()[0];
                        args.put("centroid", centroidId);
                    } catch (Exception e) {
                        logger.error("Failed to generate the model." +
                                "A centroid id is needed in the args " +
                                "argument to generate a model from " +
                                "a cluster.", e);
                        return null;
                    }
                }

                requestObject.put("cluster", resourceId);
                return createResource(resourceUrl,
                		requestObject.toJSONString());
            }
        } catch (Throwable e) {
            logger.error("Failed to generate the model.", e);
            return null;
        }

        return null;
    }


    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME&api_key=$BIGML_API_KEY&
     * Host: bigml.io
     *
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     * @return a JSONObject for the model
     */
    public JSONObject get(final String modelId, final String apiUser,
            final String apiKey) {
        if (modelId == null || modelId.length() == 0
                || !(modelId.matches(MODEL_RE))) {
            logger.info("Wrong model id");
            return null;
        }

        return getResource(BIGML_URL + modelId, null, apiUser, apiKey);
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME&api_key=$BIGML_API_KEY&
     * Host: bigml.io
     *
     * @param model
     *            a model JSONObject
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     * @return a JSONObject for the model
     */
    public JSONObject get(final JSONObject model, final String apiUser,
            final String apiKey) {
        String resourceId = (String) model.get("resource");
        return get(resourceId, apiUser, apiKey);
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME&api_key=$BIGML_API_KEY&
     * Host: bigml.io
     *
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     * @return a JSONObject for the model
     */
    public JSONObject get(final String modelId, final String queryString,
            final String apiUser, final String apiKey) {
        if (modelId == null || modelId.length() == 0
                || !(modelId.matches(MODEL_RE))) {
            logger.info("Wrong model id");
            return null;
        }

        return getResource(BIGML_URL + modelId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME&api_key=$BIGML_API_KEY&
     * Host: bigml.io
     *
     * @param model
     *            a model JSONObject
     * @param queryString
     *            query for filtering
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     * @return a JSONObject for the model
     */
    public JSONObject get(final JSONObject model, final String queryString,
            final String apiUser, final String apiKey) {
        String resourceId = (String) model.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

}
