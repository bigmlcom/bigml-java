package org.bigml.binding.resources;

import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete clusters.
 * 
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/clusters
 * 
 * 
 */
public class Cluster extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Cluster.class);

    /**
     * Constructor
     * 
     */
    public Cluster() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init();
    }

    /**
     * Constructor
     * 
     */
    public Cluster(final String apiUser, final String apiKey,
            final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init();
    }

    /**
     * Creates a cluster from a `dataset`.
     * 
     * POST /andromeda/cluster?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            cluster.
     * @param args
     *            set of parameters for the new cluster. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the cluster. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject create(final String datasetId, String args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(CLUSTER_URL, requestObject.toJSONString());
    }

    /**
     * Creates a cluster from a `dataset`.
     * 
     * POST /andromeda/cluster?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            cluster.
     * @param args
     *            set of parameters for the new cluster. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the cluster. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(CLUSTER_URL, requestObject.toJSONString());
    }

    /**
     * Creates a cluster from a list of `datasets`.
     * 
     * POST /andromeda/cluster?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            cluster.
     * @param args
     *            set of parameters for the new cluster. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the cluster. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject create(final List datasetsIds, String args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(), args, waitTime, retries, null);
        return createResource(CLUSTER_URL, requestObject.toJSONString());
    }

    /**
     * Creates a cluster from a list of `datasets`.
     * 
     * POST /andromeda/cluster?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            cluster.
     * @param args
     *            set of parameters for the new cluster. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the cluster. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(), args, waitTime, retries, null);
        return createResource(CLUSTER_URL, requestObject.toJSONString());
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
     * 
     */
    @Override
    public JSONObject get(final String clusterId) {
        return get(clusterId, null, null);
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
     * 
     */
    @Override
    public JSONObject get(final JSONObject cluster) {
        String resourceId = (String) cluster.get("resource");
        return get(resourceId, null, null);
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
     * 
     */
    public JSONObject get(final String clusterId, final String queryString) {
        return get(clusterId, queryString, null, null);
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
     * 
     */
    public JSONObject get(final JSONObject cluster, final String queryString) {
        String resourceId = (String) cluster.get("resource");
        return get(resourceId, queryString, null, null);
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

    /**
     * Checks whether a cluster's status is FINISHED.
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public boolean isReady(final String clusterId) {
        return isResourceReady(get(clusterId));
    }

    /**
     * Checks whether a cluster status is FINISHED.
     * 
     * @param cluster
     *            a cluster JSONObject
     * 
     */
    @Override
    public boolean isReady(final JSONObject cluster) {
        return isResourceReady(cluster)
                || isReady((String) cluster.get("resource"));
    }

    /**
     * Lists all your cluster.
     * 
     * GET /andromeda/cluster?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(CLUSTER_URL, queryString);
    }

    /**
     * Updates a cluster.
     * 
     * PUT
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the cluster. Optional
     * 
     */
    @Override
    public JSONObject update(final String clusterId, final String changes) {
        if (clusterId == null || clusterId.length() == 0
                || !(clusterId.matches(CLUSTER_RE))) {
            logger.info("Wrong cluster id");
            return null;
        }
        return updateResource(BIGML_URL + clusterId, changes);
    }

    /**
     * Updates a cluster.
     * 
     * PUT
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param cluster
     *            a cluster JSONObject
     * @param changes
     *            set of parameters to update the cluster. Optional
     * 
     */
    @Override
    public JSONObject update(final JSONObject cluster, final JSONObject changes) {
        String resourceId = (String) cluster.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a cluster.
     * 
     * DELETE
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject delete(final String clusterId) {
        if (clusterId == null || clusterId.length() == 0
                || !(clusterId.matches(CLUSTER_RE))) {
            logger.info("Wrong cluster id");
            return null;
        }
        return deleteResource(BIGML_URL + clusterId);
    }

    /**
     * Deletes a cluster.
     * 
     * DELETE
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param cluster
     *            a cluster JSONObject
     * 
     */
    @Override
    public JSONObject delete(final JSONObject cluster) {
        String resourceId = (String) cluster.get("resource");
        return delete(resourceId);
    }

}
