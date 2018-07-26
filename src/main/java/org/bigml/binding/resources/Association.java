package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete associations.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/associations
 *
 *
 */
public class Association extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Association.class);
    
    /**
     * Constructor
     *
     * @deprecated
     */
	public Association() {
		super.init(null, null, null, null, null, 
				ASSOCIATION_RE, ASSOCIATION_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Association(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				ASSOCIATION_RE, ASSOCIATION_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Association(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				ASSOCIATION_RE, ASSOCIATION_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public Association(final BigMLClient bigmlClient,
    				   final String apiUser, final String apiKey,
    				   final String project, final String organization,
    				   final CacheManager cacheManager) {
        super.init(bigmlClient, apiUser, apiKey, project, organization, 
        		  cacheManager, ASSOCIATION_RE, ASSOCIATION_PATH);
    }

}
