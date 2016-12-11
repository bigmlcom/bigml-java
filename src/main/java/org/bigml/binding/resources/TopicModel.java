package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete topic models.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/topicmodels
 *
 *
 */
public class TopicModel extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(TopicModel.class);

    /**
     * Constructor
     *
     */
    public TopicModel() {
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
    public TopicModel(final String apiUser, final String apiKey,
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
    public TopicModel(final String apiUser, final String apiKey,
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
     * Check if the current resource is a TopicModel
     *
     * @param resource the resource to be checked
     * @return true if it's a TopicModel
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(TOPICMODEL_RE);
    }

    /**
     * Creates a topic model from a `dataset`.
     *
     * POST /andromeda/topicmodel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            topic model.
     * @param args
     *            set of parameters for the new topic model. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the topic model. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(TOPICMODEL_URL, requestObject.toJSONString());
    }

    /**
     * Creates a topic model from a list of `datasets`.
     *
     * POST /andromeda/topicmodel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            topic model.
     * @param args
     *            set of parameters for the new topic model. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the topic model. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final List datasetsIds, String args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(TOPICMODEL_URL, requestObject.toJSONString());
    }

    /**
     * Creates a topic model from a list of `datasets`.
     *
     * POST /andromeda/topicmodel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            topic model.
     * @param args
     *            set of parameters for the new topic model. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the topic model. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(TOPICMODEL_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves a topic model.
     *
     * GET /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String topicModelId) {
        if (topicModelId == null || topicModelId.length() == 0
                || !topicModelId.matches(TOPICMODEL_RE)) {
            logger.info("Wrong topic model id");
            return null;
        }

        return getResource(BIGML_URL + topicModelId);
    }

    /**
     * Retrieves a topic model.
     *
     * GET /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param topicModel
     *            a topic model JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject topicModel) {
        String topicModelId = (String) topicModel.get("resource");
        return get(topicModelId);
    }


    /**
     * Checks whether a topic model's status is FINISHED.
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String topicModelId) {
        return isResourceReady(get(topicModelId));
    }

    /**
     * Checks whether a topic model status is FINISHED.
     *
     * @param topicModel
     *            a topic model JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject topicModel) {
        return isResourceReady(topicModel)
                || isReady((String) topicModel.get("resource"));
    }

    /**
     * Lists all your topic models.
     *
     * GET /andromeda/topicmodel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(TOPICMODEL_URL, queryString);
    }

    /**
     * Updates a topic model.
     *
     * PUT /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final String topicModelId, final String changes) {
        if (topicModelId == null || topicModelId.length() == 0
                || !(topicModelId.matches(TOPICMODEL_RE))) {
            logger.info("Wrong topic model id");
            return null;
        }
        return updateResource(BIGML_URL + topicModelId, changes);
    }

    /**
     * Updates a topic model.
     *
     * PUT /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicModel
     *            a topicmodel JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject topicModel, final JSONObject changes) {
        String resourceId = (String) topicModel.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a topic model.
     *
     * DELETE
     * /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String topicModelId) {
        if (topicModelId == null || topicModelId.length() == 0
                || !(topicModelId.matches(TOPICMODEL_RE))) {
            logger.info("Wrong topic model id");
            return null;
        }
        return deleteResource(BIGML_URL + topicModelId);
    }

    /**
     * Deletes a topic model.
     *
     * DELETE
     * /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param topicModel
     *            a topic model JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject topicModel) {
        String resourceId = (String) topicModel.get("resource");
        return delete(resourceId);
    }

}
