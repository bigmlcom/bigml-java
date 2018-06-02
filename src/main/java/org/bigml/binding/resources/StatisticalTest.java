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
    		super.init(null, null, null, 
    			STATISTICALTEST_RE, STATISTICALTEST_PATH);
    }

    /**
     * Constructor
     *
     */
    public StatisticalTest(final String apiUser, final String apiKey) {
    		super.init(apiUser, apiKey, null, 
    			STATISTICALTEST_RE, STATISTICALTEST_PATH);
    }

    /**
     * Constructor
     *
     */
    public StatisticalTest(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, cacheManager, 
    			STATISTICALTEST_RE, STATISTICALTEST_PATH);
    }

}
