package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete optiMLs.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/optimls
 *
 *
 */
public class OptiML extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(OptiML.class);


    /**
     * Constructor
     *
     */
    public OptiML() {
        super.init(null, null, null);
        this.resourceRe = OPTIML_RE;
        this.resourceUrl = OPTIML_URL;
        this.resourceName = "optiml";
    }

    /**
     * Constructor
     *
     */
    public OptiML(final String apiUser, final String apiKey) {
        super.init(apiUser, apiKey, null);
        this.resourceRe = OPTIML_RE;
        this.resourceUrl = OPTIML_URL;
        this.resourceName = "optiml";
    }


    /**
     * Constructor
     *
     */
    public OptiML(final String apiUser, final String apiKey, final CacheManager cacheManager) {
        super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = OPTIML_RE;
        this.resourceUrl = OPTIML_URL;
        this.resourceName = "optiml";
    }

}
