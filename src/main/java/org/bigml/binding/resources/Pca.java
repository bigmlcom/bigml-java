package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete Pca.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/pca
 *
 *
 */
public class Pca extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Pca.class);
    
    /**
     * Constructor
     *
     * @param bigmlClient	the client with connection to BigML
     * @param apiUser		API user
     * @param apiKey		API key
     * @param project		project id
     * @param organization	organization id
     * @param cacheManager	cache manager
     */
    public Pca(final BigMLClient bigmlClient,
    		   final String apiUser, final String apiKey, 
    		   final String project, final String organization,
    		   final CacheManager cacheManager) {
        super.init(bigmlClient, apiUser, apiKey, project, organization, 
        		   cacheManager, PCA_RE, PCA_PATH);
    }
    
    
}
