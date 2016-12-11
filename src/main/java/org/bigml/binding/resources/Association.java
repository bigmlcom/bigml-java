package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete associations.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/associations
 *
 *
 */
public class Association extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Association.class);

    /**
     * Constructor
     *
     */
    public Association() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Association(final String apiUser, final String apiKey,
            final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(null);
    }


    /**
     * Constructor
     *
     */
    public Association(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(cacheManager);
    }

    /**
     * Check if the current resource is an Association
     *
     * @param resource the resource to be checked
     * @return true if it's an Association
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(ASSOCIATION_RE);
    }

    /**
     * Creates an association from a `dataset`.
     *
     * POST /andromeda/association?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            association.
     * @param args
     *            set of parameters for the new association. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the association. Optional
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
        return createResource(ASSOCIATION_URL, requestObject.toJSONString());
    }

    /**
     * Creates an association from a `dataset`.
     *
     * POST /andromeda/association?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            association.
     * @param args
     *            set of parameters for the new association. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the association. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(ASSOCIATION_URL, requestObject.toJSONString());
    }

    /**
     * Creates an association from a list of `datasets`.
     *
     * POST /andromeda/association?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            association.
     * @param args
     *            set of parameters for the new association. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the association. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final List datasetsIds, String args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(ASSOCIATION_URL, requestObject.toJSONString());
    }

    /**
     * Creates an association from a list of `datasets`.
     *
     * POST /andromeda/association?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            association.
     * @param args
     *            set of parameters for the new association. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the association. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(ASSOCIATION_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves an association.
     *
     * GET /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String associationId) {
        if (associationId == null || associationId.length() == 0
                || !associationId.matches(ASSOCIATION_RE)) {
            logger.info("Wrong association id");
            return null;
        }

        return getResource(BIGML_URL + associationId);
    }

    /**
     * Retrieves an association.
     *
     * GET /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param association
     *            an association JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject association) {
        String associationId = (String) association.get("resource");
        return get(associationId);
    }


    /**
     * Checks whether an association's status is FINISHED.
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String associationId) {
        return isResourceReady(get(associationId));
    }

    /**
     * Checks whether an association's status is FINISHED.
     *
     * @param association
     *            an association JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject association) {
        return isResourceReady(association)
                || isReady((String) association.get("resource"));
    }

    /**
     * Lists all your associations.
     *
     * GET /andromeda/association?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(ASSOCIATION_URL, queryString);
    }

    /**
     * Updates an association.
     *
     * PUT /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final String associationId, final String changes) {
        if (associationId == null || associationId.length() == 0
                || !(associationId.matches(ASSOCIATION_RE))) {
            logger.info("Wrong association id");
            return null;
        }
        return updateResource(BIGML_URL + associationId, changes);
    }

    /**
     * Updates an association.
     *
     * PUT /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param association
     *            an association JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject association, final JSONObject changes) {
        String resourceId = (String) association.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes an association.
     *
     * DELETE
     * /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String associationId) {
        if (associationId == null || associationId.length() == 0
                || !(associationId.matches(ASSOCIATION_RE))) {
            logger.info("Wrong association id");
            return null;
        }
        return deleteResource(BIGML_URL + associationId);
    }

    /**
     * Deletes an association.
     *
     * DELETE
     * /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param association
     *            an association JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject association) {
        String resourceId = (String) association.get("resource");
        return delete(resourceId);
    }

}
