package org.bigml.binding.resources;

import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete ensembles.
 * 
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/ensembles
 * 
 * 
 */
public class Ensemble extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Ensemble.class);

    /**
     * Constructor
     * 
     */
    public Ensemble() {
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
    public Ensemble(final String apiUser, final String apiKey,
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
     * Check if the current resource is an Ensemble
     *
     * @param resource the resource to be checked
     * @return true if its an Ensemble
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(ENSEMBLE_RE);
    }

    /**
     * Creates a new ensemble.
     * 
     * POST /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars for the dataset to attach the
     *            ensemble.
     * @param args
     *            set of parameters for the new ensemble. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for dataset before to start to create the ensemble. Optional
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
        return createResource(ENSEMBLE_URL, requestObject.toJSONString());
    }

    /**
     * Creates a new ensemble.
     * 
     * POST /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars for the dataset to attach the
     *            ensemble.
     * @param args
     *            set of parameters for the new ensemble. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for dataset before to start to create the ensemble. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {
        String[] datasetsIds = { datasetId };
        JSONObject requestObject = createFromDatasets(datasetsIds, args,
                waitTime, retries, null);
        return createResource(ENSEMBLE_URL, requestObject.toJSONString());
    }

    /**
     * Creates an ensemble from a list of `datasets`.
     * 
     * POST /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            ensemble.
     * @param args
     *            set of parameters for the new ensemble. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the ensemble. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject create(final List datasetsIds, String args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(ENSEMBLE_URL, requestObject.toJSONString());
    }

    /**
     * Creates an ensemble from a list of `datasets`.
     * 
     * POST /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            ensemble.
     * @param args
     *            set of parameters for the new ensemble. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the ensemble. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject create(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject requestObject = createFromDatasets(
                (String[]) datasetsIds.toArray(new String[datasetsIds.size()]), args, waitTime, retries, null);
        return createResource(ENSEMBLE_URL, requestObject.toJSONString());
    }

    /**
     * Retrieves an ensemble.
     * 
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param ensembleId
     *            a unique identifier in the form ensemble/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject get(final String ensembleId) {
        if (ensembleId == null || ensembleId.length() == 0
                || !ensembleId.matches(ENSEMBLE_RE)) {
            logger.info("Wrong ensemble id");
            return null;
        }

        return getResource(BIGML_URL + ensembleId);
    }

    /**
     * Retrieves an ensemble.
     * 
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param ensemble
     *            a ensemble JSONObject
     * 
     */
    @Override
    public JSONObject get(final JSONObject ensemble) {
        String resourceId = (String) ensemble.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves an ensemble.
     * 
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param ensembleId
     *            a unique identifier in the form ensemble/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * 
     */
    public JSONObject get(final String ensembleId, final String queryString) {
        if (ensembleId == null || ensembleId.length() == 0
                || !ensembleId.matches(ENSEMBLE_RE)) {
            logger.info("Wrong ensemble id");
            return null;
        }

        return getResource(BIGML_URL + ensembleId, queryString);
    }

    /**
     * Retrieves an ensemble.
     * 
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param ensemble
     *            a ensemble JSONObject
     * @param queryString
     *            query for filtering
     * 
     */
    public JSONObject get(final JSONObject ensemble, final String queryString) {
        String resourceId = (String) ensemble.get("resource");
        return get(resourceId, queryString);
    }

    /**
     * Checks whether a ensemble's status is FINISHED.
     * 
     * @param ensembleId
     *            a unique identifier in the form ensemble/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public boolean isReady(final String ensembleId) {
        return isResourceReady(get(ensembleId));
    }

    /**
     * Checks whether a ensemble's status is FINISHED.
     * 
     * @param ensemble
     *            a ensemble JSONObject
     * 
     */
    @Override
    public boolean isReady(final JSONObject ensemble) {
        String resourceId = (String) ensemble.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your ensembles.
     * 
     * GET /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(ENSEMBLE_URL, queryString);
    }

    /**
     * Updates an ensemble.
     * 
     * PUT
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param ensembleId
     *            a unique identifier in the form ensemble/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the ensemble. Optional
     * 
     */
    @Override
    public JSONObject update(final String ensembleId, final String changes) {
        if (ensembleId == null || ensembleId.length() == 0
                || !ensembleId.matches(ENSEMBLE_RE)) {
            logger.info("Wrong ensemble id");
            return null;
        }
        return updateResource(BIGML_URL + ensembleId, changes);
    }

    /**
     * Updates an ensemble.
     * 
     * PUT
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param ensemble
     *            a ensemble JSONObject
     * @param changes
     *            set of parameters to update the ensemble. Optional
     * 
     */
    @Override
    public JSONObject update(final JSONObject ensemble, final JSONObject changes) {
        String resourceId = (String) ensemble.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes an ensemble.
     * 
     * DELETE
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param ensembleId
     *            a unique identifier in the form ensemble/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject delete(final String ensembleId) {
        if (ensembleId == null || ensembleId.length() == 0
                || !ensembleId.matches(ENSEMBLE_RE)) {
            logger.info("Wrong ensemble id");
            return null;
        }
        return deleteResource(BIGML_URL + ensembleId);
    }

    /**
     * Deletes an ensemble.
     * 
     * DELETE
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param ensemble
     *            a ensemble JSONObject
     * 
     */
    @Override
    public JSONObject delete(final JSONObject ensemble) {
        String resourceId = (String) ensemble.get("resource");
        return delete(resourceId);
    }

}