package org.bigml.binding.resources;

import java.util.List;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point to create, retrieve, list, update, and delete association sets.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/associationsets
 *
 *
 */
public class AssociationSet extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(AssociationSet.class);

    /**
     * Constructor
     *
     */
    public AssociationSet() {
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
    public AssociationSet(final String apiUser, final String apiKey,
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
    public AssociationSet(final String apiUser, final String apiKey,
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
     * Check if the current resource is an AssociationSet
     *
     * @param resource the resource to be checked
     * @return true if it's an AssociationSet
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(ASSOCIATIONSET_RE);
    }

    /**
     * Creates an association set from an association.
     *
     * POST /andromeda/associationset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a
     *            string of 24 alpha-numeric chars for the association to attach
     *            the association set.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create an association set for.
     * @param byName
     *            if we use the name of the fields instead of the internal code
     *            as the key to locate the fields in the inputData map.
     * @param args
     *            set of parameters for the new association set. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the association set.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String associationId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {

        if (associationId == null || associationId.length() == 0 ) {
            logger.info("Wrong association id. Id cannot be null");
            return null;
        }

        try {
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;
            if (waitTime > 0) {
                int count = 0;
                while (count < retries
                        && !BigMLClient.getInstance(this.devMode)
                                .associationIsReady(associationId)) {
                    Thread.sleep(waitTime);
                    count++;
                }
            }

            // Input data
            JSONObject inputDataJSON = null;
            if (inputData == null) {
                inputDataJSON = new JSONObject();
            } else {
                inputDataJSON = inputData;
            }

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            requestObject.put("association", associationId);
            requestObject.put("input_data", inputData);

            return createResource(ASSOCIATIONSET_URL,
                                  requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating an association set");
            return null;
        }
    }

    /**
     * Retrieves an association set.
     *
     * GET /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param associationSetId
     *            a unique identifier in the form associationset/id where id
     *            is a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String associationSetId) {
        if (associationSetId == null || associationSetId.length() == 0
                || !associationSetId.matches(ASSOCIATIONSET_RE)) {
            logger.info("Wrong association set id");
            return null;
        }

        return getResource(BIGML_URL + associationSetId);
    }

    /**
     * Retrieves an association set.
     *
     * GET /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param associationSet
     *            an association set JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject associationSet) {
        String associationSetId = (String) associationSet.get("resource");
        return get(associationSetId);
    }


    /**
     * Checks whether an association set's status is FINISHED.
     *
     * @param associationSetId
     *            a unique identifier in the form associationset/id where id
     *            is a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String associationSetId) {
        return isResourceReady(get(associationSetId));
    }

    /**
     * Checks whether an association set's status is FINISHED.
     *
     * @param associationSet
     *            an association set JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject associationSet) {
        return isResourceReady(associationSet)
                || isReady((String) associationSet.get("resource"));
    }

    /**
     * Lists all your association sets.
     *
     * GET /andromeda/associationset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(ASSOCIATIONSET_URL, queryString);
    }

    /**
     * Updates an association set.
     *
     * PUT /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationSetId
     *            a unique identifier in the form associationset/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the association set. Optional
     *
     */
    @Override
    public JSONObject update(final String associationSetId, final String changes) {
        if (associationSetId == null || associationSetId.length() == 0
                || !(associationSetId.matches(ASSOCIATIONSET_RE))) {
            logger.info("Wrong association set id");
            return null;
        }
        return updateResource(BIGML_URL + associationSetId, changes);
    }

    /**
     * Updates an association set.
     *
     * PUT /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationSet
     *            an association set JSONObject
     * @param changes
     *            set of parameters to update the association set. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject associationSet, final JSONObject changes) {
        String resourceId = (String) associationSet.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes an association set.
     *
     * DELETE
     * /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param associationSetId
     *            a unique identifier in the form associationset/id where id
     *            is a string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String associationSetId) {
        if (associationSetId == null || associationSetId.length() == 0
                || !(associationSetId.matches(ASSOCIATIONSET_RE))) {
            logger.info("Wrong association set id");
            return null;
        }
        return deleteResource(BIGML_URL + associationSetId);
    }

    /**
     * Deletes an association set.
     *
     * DELETE
     * /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param associationSet
     *            an association set JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject associationSet) {
        String resourceId = (String) associationSet.get("resource");
        return delete(resourceId);
    }

}
