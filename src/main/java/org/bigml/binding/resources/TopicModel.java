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
     * @deprecated
     */
	public TopicModel() {
		super.init(null, null, null, null, null, 
				TOPICMODEL_RE, TOPICMODEL_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public TopicModel(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				TOPICMODEL_RE, TOPICMODEL_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public TopicModel(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				TOPICMODEL_RE, TOPICMODEL_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public TopicModel(final String apiUser, final String apiKey, 
    				  final String project, final String organization,
    				  final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, project, organization,
    				   cacheManager, TOPICMODEL_RE, TOPICMODEL_PATH);
    }

}
