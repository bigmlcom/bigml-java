package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
public class Model extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Model.class);

    /**
     * Constructor
     *
     */
    public Model() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Model(final String apiUser, final String apiKey,
            final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Model(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(cacheManager);
    }

    /**
     * Check if the current resource is a Model
     *
     * @param resource the resource to be checked
     * @return true if it's a Model
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(MODEL_RE);
    }

    /**
     * Creates a new model.
     *
     * POST /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
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
    @Deprecated
    public JSONObject create(final String resourceId, String args,
            Integer waitTime, Integer retries) {
        return create(resourceId, (JSONObject) JSONValue.parse(args), waitTime, retries);
    }

    /**
     * Creates a new model.
     *
     * POST /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
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
                return createResource(MODEL_URL, requestObject.toJSONString());

            } else if( resourceId.matches(CLUSTER_RE) ) {
                JSONObject requestObject = new JSONObject();

                // If the original resource is a Cluster
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                            .clusterIsReady(resourceId)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (args != null) {
                    requestObject = args;
                }

                if( !requestObject.containsKey("centroid") ) {
                    try {
                        JSONObject cluster = BigMLClient.getInstance(this.devMode).getCluster(resourceId);
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
                return createResource(MODEL_URL, requestObject.toJSONString());
            }
        } catch (Throwable e) {
            logger.error("Failed to generate the model.", e);
            return null;
        }

        return null;
    }

    /**
     * Creates a mdel from a list of `datasets`.
     *
     * POST /andromeda/mdel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            mdel.
     * @param args
     *            set of parameters for the new mdel. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the mdel. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final List datasetsIds, String args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(MODEL_URL, requestObject.toJSONString());
    }

    /**
     * Creates a mdel from a list of `datasets`.
     *
     * POST /andromeda/mdel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            mdel.
     * @param args
     *            set of parameters for the new mdel. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the mdel. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(MODEL_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String modelId) {
        return get(modelId, null, null);
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
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
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param model
     *            a model JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject model) {
        String resourceId = (String) model.get("resource");
        return get(resourceId, null, null);
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param model
     *            a model JSONObject
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject model, final String apiUser,
            final String apiKey) {
        String resourceId = (String) model.get("resource");
        return get(resourceId, apiUser, apiKey);
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String modelId, final String queryString) {
        return get(modelId, queryString, null, null);
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
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
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param model
     *            a model JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject model, final String queryString) {
        String resourceId = (String) model.get("resource");
        return get(resourceId, queryString, null, null);
    }

    /**
     * Retrieves a model.
     *
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
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
     */
    public JSONObject get(final JSONObject model, final String queryString,
            final String apiUser, final String apiKey) {
        String resourceId = (String) model.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

    /**
     * Checks whether a model's status is FINISHED.
     *
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String modelId) {
        return isResourceReady(get(modelId));
    }

    /**
     * Checks whether a model's status is FINISHED.
     *
     * @param model
     *            a model JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject model) {
        return isResourceReady(model)
                || isReady((String) model.get("resource"));
    }

    /**
     * Lists all your models.
     *
     * GET /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(MODEL_URL, queryString);
    }

    /**
     * Updates a model.
     *
     * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final String modelId, final String changes) {
        if (modelId == null || modelId.length() == 0
                || !(modelId.matches(MODEL_RE))) {
            logger.info("Wrong model id");
            return null;
        }
        return updateResource(BIGML_URL + modelId, changes);
    }

    /**
     * Updates a model.
     *
     * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a model JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject model, final JSONObject changes) {
        String resourceId = (String) model.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a model.
     *
     * DELETE
     * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String modelId) {
        if (modelId == null || modelId.length() == 0
                || !(modelId.matches(MODEL_RE))) {
            logger.info("Wrong model id");
            return null;
        }
        return deleteResource(BIGML_URL + modelId);
    }

    /**
     * Deletes a model.
     *
     * DELETE
     * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param model
     *            a model JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject model) {
        String resourceId = (String) model.get("resource");
        return delete(resourceId);
    }

}
