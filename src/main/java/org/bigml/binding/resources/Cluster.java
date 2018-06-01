package org.bigml.binding.resources;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete clusters.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/clusters
 *
 *
 */
public class Cluster extends AbstractModelResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Cluster.class);

    /**
     * Constructor
     *
     */
    public Cluster() {
    	super.init(null, null, null);
        this.resourceRe = CLUSTER_RE;
        this.resourceUrl = CLUSTER_URL;
        this.resourceName = "cluster";
    }

    /**
     * Constructor
     *
     */
    public Cluster(final String apiUser, final String apiKey) {
    	super.init(apiUser, apiKey, null);
        this.resourceRe = CLUSTER_RE;
        this.resourceUrl = CLUSTER_URL;
        this.resourceName = "cluster";
    }

    /**
     * Constructor
     *
     */
    public Cluster(final String apiUser, final String apiKey, final CacheManager cacheManager) {
    	super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = CLUSTER_RE;
        this.resourceUrl = CLUSTER_URL;
        this.resourceName = "cluster";
    }

    /**
     * Retrieves a cluster.
     *
     * GET
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String clusterId, final String apiUser,
            final String apiKey) {
        if (clusterId == null || clusterId.length() == 0
                || !(clusterId.matches(CLUSTER_RE))) {
            logger.info("Wrong cluster id");
            return null;
        }

        return getResource(BIGML_URL + clusterId, null, apiUser, apiKey);
    }

    /**
     * Retrieves a cluster.
     *
     * GET
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param cluster
     *            a cluster JSONObject
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject cluster, final String apiUser,
            final String apiKey) {
        String resourceId = (String) cluster.get("resource");
        return get(resourceId, apiUser, apiKey);
    }

    /**
     * Retrieves a cluster.
     *
     * GET
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String clusterId, final String queryString,
            final String apiUser, final String apiKey) {
        if (clusterId == null || clusterId.length() == 0
                || !(clusterId.matches(CLUSTER_RE))) {
            logger.info("Wrong cluster id");
            return null;
        }

        return getResource(BIGML_URL + clusterId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves a cluster.
     *
     * GET
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param cluster
     *            a cluster JSONObject
     * @param queryString
     *            query for filtering
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject cluster, final String queryString,
            final String apiUser, final String apiKey) {
        String resourceId = (String) cluster.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

}
