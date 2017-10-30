package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by the BigML class as a mixin that provides the StatisticalTest' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/statisticaltests
 *
 *
 */
public class StatisticalTest extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(StatisticalTest.class);

    /**
     * Constructor
     *
     */
    public StatisticalTest() {
    		super.init(null, null, false, null);
        this.resourceRe = STATISTICALTEST_RE;
        this.resourceUrl = STATISTICALTEST_URL;
        this.resourceName = "statisticaltest";
    }

    /**
     * Constructor
     *
     */
    public StatisticalTest(final String apiUser, final String apiKey,
                   final boolean devMode) {
    		super.init(apiUser, apiKey, devMode, null);
        this.resourceRe = STATISTICALTEST_RE;
        this.resourceUrl = STATISTICALTEST_URL;
        this.resourceName = "statisticaltest";
    }

    /**
     * Constructor
     *
     */
    public StatisticalTest(final String apiUser, final String apiKey,
                   final boolean devMode, final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, devMode, cacheManager);
        this.resourceRe = STATISTICALTEST_RE;
        this.resourceUrl = STATISTICALTEST_URL;
        this.resourceName = "statisticaltest";
    }

}
