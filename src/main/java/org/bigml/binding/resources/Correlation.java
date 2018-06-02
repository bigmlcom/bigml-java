package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by the BigML class as a mixin that provides the 
 * Correlation' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/correlations
 *
 *
 */
public class Correlation extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Correlation.class);

    /**
     * Constructor
     *
     */
    public Correlation() {
    		super.init(null, null, null, 
    			CORRELATION_RE, CORRELATION_PATH);
    }

    /**
     * Constructor
     *
     */
    public Correlation(final String apiUser, final String apiKey) {
    		super.init(apiUser, apiKey, null, 
    			CORRELATION_RE, CORRELATION_PATH);
    }

    /**
     * Constructor
     *
     */
    public Correlation(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, cacheManager, 
    			CORRELATION_RE, CORRELATION_PATH);
    }

}
