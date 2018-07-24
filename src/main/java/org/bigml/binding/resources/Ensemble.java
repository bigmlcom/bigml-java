package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete ensembles.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/ensembles
 *
 *
 */
public class Ensemble extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Ensemble.class);
    
    /**
     * Constructor
     *
     * @deprecated
     */
	public Ensemble() {
		super.init(null, null, null, null, null, 
				ENSEMBLE_RE, ENSEMBLE_PATH);
	}

	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Ensemble(final String apiUser, final String apiKey) {
		super.init(apiUser, apiKey, null, null, null, 
				ENSEMBLE_RE, ENSEMBLE_PATH);
	}
	
	/**
	 * Constructor
	 *
	 * @deprecated
	 */
	public Ensemble(final String apiUser, final String apiKey,
			final CacheManager cacheManager) {
		super.init(apiUser, apiKey, null, null, null, 
				ENSEMBLE_RE, ENSEMBLE_PATH);
	}
	
    /**
     * Constructor
     *
     */
    public Ensemble(final String apiUser, final String apiKey, 
    			    final String project, final String organization,
    			    final CacheManager cacheManager) {
    		super.init(apiUser, apiKey, project, organization, 
    				   cacheManager, ENSEMBLE_RE, ENSEMBLE_PATH);
    }

}