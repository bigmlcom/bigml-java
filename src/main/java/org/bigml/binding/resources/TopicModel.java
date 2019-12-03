package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
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
    public TopicModel(final BigMLClient bigmlClient,
    				  final String apiUser, final String apiKey, 
    				  final String project, final String organization,
    				  final CacheManager cacheManager) {
    		super.init(bigmlClient, apiUser, apiKey, project, organization,
    				   cacheManager, TOPICMODEL_RE, TOPICMODEL_PATH);
    }

}
