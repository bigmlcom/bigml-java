package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete topic distributions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/topicdistributions
 *
 *
 */
public class TopicDistribution extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(TopicDistribution.class);

    /**
     * Constructor
     *
     */
    public TopicDistribution() {
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
    public TopicDistribution(final String apiUser, final String apiKey,
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
    public TopicDistribution(final String apiUser, final String apiKey,
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
     * Check if the current resource is a TopicDistribution
     *
     * @param resource the resource to be checked
     * @return true if it's a TopicDistribution
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(TOPICDISTRIBUTION_RE);
    }

    /**
     * Creates a topic distribution from a topic model.
     *
     * POST /andromeda/topicdistribution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a
     *            string of 24 alpha-numeric chars for the topic model to attach
     *            the topic distribution.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a topic distribution for.
     * @param args
     *            set of parameters for the new topic distribution. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the topic distribution.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String topicModelId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {

        if (topicModelId == null || topicModelId.length() == 0 ) {
            logger.info("Wrong topic model id. Id cannot be null");
            return null;
        }

        try {
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;
            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .topicModelIsReady(topicModelId)) {
                    Thread.sleep(waitTime);
                    count++;
                }
            }

            // Input data
            JSONObject inputDataJSON = null;
            if (inputData == null) {
                inputDataJSON = new JSONObject();
            } else {
                inputDataJSON = inputData;
            }

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            requestObject.put("topicmodel", topicModelId);
            requestObject.put("input_data", inputData);

            return createResource(TOPICDISTRIBUTION_URL,
                                  requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating topic distribution");
            return null;
        }
    }

    /**
     * Retrieves a topic distribution.
     *
     * GET /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param topicDistributionId
     *            a unique identifier in the form topicdistribution/id where id
     *            is a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String topicDistributionId) {
        if (topicDistributionId == null || topicDistributionId.length() == 0
                || !topicDistributionId.matches(TOPICDISTRIBUTION_RE)) {
            logger.info("Wrong topic distribution id");
            return null;
        }

        return getResource(BIGML_URL + topicDistributionId);
    }

    /**
     * Retrieves a topic distribution.
     *
     * GET /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param topicDistribution
     *            a topic distribution JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject topicDistribution) {
        String topicDistributionId = (String) topicDistribution.get("resource");
        return get(topicDistributionId);
    }


    /**
     * Checks whether a topic distribution's status is FINISHED.
     *
     * @param topicDistributionId
     *            a unique identifier in the form topicdistribution/id where id
     *            is a stringof 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String topicDistributionId) {
        return isResourceReady(get(topicDistributionId));
    }

    /**
     * Checks whether a topic distribution's status is FINISHED.
     *
     * @param topicDistribution
     *            an topicDistribution JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject topicDistribution) {
        return isResourceReady(topicDistribution)
                || isReady((String) topicDistribution.get("resource"));
    }

    /**
     * Lists all your topic distributions.
     *
     * GET /andromeda/topicdistribution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(TOPICDISTRIBUTION_URL, queryString);
    }

    /**
     * Updates a topic distribution.
     *
     * PUT /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicDistributionId
     *            a unique identifier in the form topicdistribution/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the topic distribution. Optional
     *
     */
    @Override
    public JSONObject update(final String topicDistributionId, final String changes) {
        if (topicDistributionId == null || topicDistributionId.length() == 0
                || !(topicDistributionId.matches(TOPICDISTRIBUTION_RE))) {
            logger.info("Wrong topic distribution id");
            return null;
        }
        return updateResource(BIGML_URL + topicDistributionId, changes);
    }

    /**
     * Updates a topic distribution.
     *
     * PUT /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicDistribution
     *            a topic distribution JSONObject
     * @param changes
     *            set of parameters to update the topic distribution. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject topicDistribution, final JSONObject changes) {
        String resourceId = (String) topicDistribution.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a topic distribution.
     *
     * DELETE
     * /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param topicDistributionId
     *            a unique identifier in the form topicdistribution/id where id
     *            is a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String topicDistributionId) {
        if (topicDistributionId == null || topicDistributionId.length() == 0
                || !(topicDistributionId.matches(TOPICDISTRIBUTION_RE))) {
            logger.info("Wrong topic distribution id");
            return null;
        }
        return deleteResource(BIGML_URL + topicDistributionId);
    }

    /**
     * Deletes a topic distribution.
     *
     * DELETE
     * /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param topicDistribution
     *            a topic distribution JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject topicDistribution) {
        String resourceId = (String) topicDistribution.get("resource");
        return delete(resourceId);
    }

}
