package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
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
     * @deprecated
     */
	public Deepnet() {
		super.init(null, null, null, null, null, 
				DEEPNET_RE, DEEPNET_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Deepnet(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				DEEPNET_RE, DEEPNET_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Deepnet(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				DEEPNET_RE, DEEPNET_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public Deepnet(final BigMLClient bigmlClient,
    			   final String apiUser, final String apiKey, 
    			   final String project, final String organization,
    			   final CacheManager cacheManager) {
        super.init(bigmlClient, apiUser, apiKey, project, organization,
        		   cacheManager, DEEPNET_RE, DEEPNET_PATH);
    }

}
