package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete timeseries.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/timeseries
 *
 *
 */
public class TimeSeries extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(TimeSeries.class);

    /**
     * Constructor
     *
     */
    public TimeSeries() {
    		super.init(null, null, null, 
    			TIMESERIES_RE, TIMESERIES_PATH);
    }

    /**
     * Constructor
     *
     */
    public TimeSeries(final String apiUser, final String apiKey) {
    		super.init(apiUser, apiKey, null, 
    			TIMESERIES_RE, TIMESERIES_PATH);
    }


    /**
     * Constructor
     *
     */
    public TimeSeries(final String apiUser, final String apiKey, 
    			final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, cacheManager, 
    			TIMESERIES_RE, TIMESERIES_PATH);
    }

}
