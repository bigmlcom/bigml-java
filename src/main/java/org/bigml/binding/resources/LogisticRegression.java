package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete LogisticRegression.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/logisticregressions
 *
 *
 */
public class LogisticRegression extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(LogisticRegression.class);

    /**
     * Constructor
     *
     */
    public LogisticRegression() {
    		super.init(null, null, null, 
    			LOGISTICREGRESSION_RE, LOGISTICREGRESSION_PATH);
    }

    /**
     * Constructor
     *
     */
    public LogisticRegression(final String apiUser, final String apiKey) {
    	super.init(apiUser, apiKey, null, 
    			LOGISTICREGRESSION_RE, LOGISTICREGRESSION_PATH);
    }

    /**
     * Constructor
     *
     */
    public LogisticRegression(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, cacheManager, 
    			LOGISTICREGRESSION_RE, LOGISTICREGRESSION_PATH);
    }

}
