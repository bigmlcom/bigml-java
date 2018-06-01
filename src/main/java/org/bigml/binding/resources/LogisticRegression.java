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
    	super.init(null, null, null);
        this.resourceRe = LOGISTICREGRESSION_RE;
        this.resourceUrl = LOGISTICREGRESSION_URL;
        this.resourceName = "logisticregression";
    }

    /**
     * Constructor
     *
     */
    public LogisticRegression(final String apiUser, final String apiKey) {
    	super.init(apiUser, apiKey, null);
        this.resourceRe = LOGISTICREGRESSION_RE;
        this.resourceUrl = LOGISTICREGRESSION_URL;
        this.resourceName = "logisticregression";
    }

    /**
     * Constructor
     *
     */
    public LogisticRegression(final String apiUser, final String apiKey, final CacheManager cacheManager) {
    	super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = LOGISTICREGRESSION_RE;
        this.resourceUrl = LOGISTICREGRESSION_URL;
        this.resourceName = "logisticregression";
    }

}
