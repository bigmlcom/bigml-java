package org.bigml.binding.resources;

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
     */
    public Association() {
        super.init(null, null, null, 
        		ASSOCIATION_RE, ASSOCIATION_PATH);
    }

    /**
     * Constructor
     *
     */
    public Association(final String apiUser, final String apiKey) {
        super.init(apiUser, apiKey, null, 
        		ASSOCIATION_RE, ASSOCIATION_PATH);
    }


    /**
     * Constructor
     *
     */
    public Association(final String apiUser, final String apiKey,
    			final CacheManager cacheManager) {
        super.init(apiUser, apiKey, cacheManager, 
        		ASSOCIATION_RE, ASSOCIATION_PATH);
    }

}
