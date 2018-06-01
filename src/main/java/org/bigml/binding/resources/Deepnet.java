package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete deepnets.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/deepnets
 *
 *
 */
public class Deepnet extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Deepnet.class);


    /**
     * Constructor
     *
     */
    public Deepnet() {
        super.init(null, null, null);
        this.resourceRe = DEEPNET_RE;
        this.resourceUrl = DEEPNET_URL;
        this.resourceName = "deepnet";
    }

    /**
     * Constructor
     *
     */
    public Deepnet(final String apiUser, final String apiKey) {
        super.init(apiUser, apiKey, null);
        this.resourceRe = DEEPNET_RE;
        this.resourceUrl = DEEPNET_URL;
        this.resourceName = "deepnet";
    }


    /**
     * Constructor
     *
     */
    public Deepnet(final String apiUser, final String apiKey, final CacheManager cacheManager) {
        super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = DEEPNET_RE;
        this.resourceUrl = DEEPNET_URL;
        this.resourceName = "deepnet";
    }

}
