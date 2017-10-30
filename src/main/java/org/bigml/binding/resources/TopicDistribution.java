package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
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
    		super.init(null, null, false, null);
        this.resourceRe = TOPICDISTRIBUTION_RE;
        this.resourceUrl = TOPICDISTRIBUTION_URL;
        this.resourceName = "topic distribution";
    }

    /**
     * Constructor
     *
     */
    public TopicDistribution(final String apiUser, final String apiKey,
            final boolean devMode) {
    		super.init(apiUser, apiKey, devMode, null);
        this.resourceRe = TOPICDISTRIBUTION_RE;
        this.resourceUrl = TOPICDISTRIBUTION_URL;
        this.resourceName = "topic distribution";
    }


    /**
     * Constructor
     *
     */
    public TopicDistribution(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, devMode, cacheManager);
        this.resourceRe = TOPICDISTRIBUTION_RE;
        this.resourceUrl = TOPICDISTRIBUTION_URL;
        this.resourceName = "topic distribution";
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

}
