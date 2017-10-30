package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
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
public class TopicModel extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(TopicModel.class);

    /**
     * Constructor
     *
     */
    public TopicModel() {
    		super.init(null, null, false, null);
        this.resourceRe = TOPICMODEL_RE;
        this.resourceUrl = TOPICMODEL_URL;
        this.resourceName = "topic model";
    }

    /**
     * Constructor
     *
     */
    public TopicModel(final String apiUser, final String apiKey,
            final boolean devMode) {
    		super.init(apiUser, apiKey, devMode, null);
        this.resourceRe = TOPICMODEL_RE;
        this.resourceUrl = TOPICMODEL_URL;
        this.resourceName = "topic model";
    }


    /**
     * Constructor
     *
     */
    public TopicModel(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, devMode, cacheManager);
        this.resourceRe = TOPICMODEL_RE;
        this.resourceUrl = TOPICMODEL_URL;
        this.resourceName = "topic model";
    }

}
