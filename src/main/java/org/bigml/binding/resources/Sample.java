package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used by the BigML class as a mixin that provides the samples' REST calls.
 *
 * It should not be instantiated independently.
 * 
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/samples
 * 
 * 
 */
public class Sample extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Sample.class);

    /**
     * Constructor
     *
     */
    public Sample() {
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
    public Sample(final String apiUser, final String apiKey,
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
     * Check if the current resource is a Sample
     *
     * @param resource the resource to be checked
     * @return true if its an Sample
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(SAMPLE_RE);
    }

    /**
     * Creates a remote sample from a dataset.
     * 
     * Uses a remote resource to create a new sample using the
     * arguments in `args`.
     *
     * If `wait_time` is higher than 0 then the sample creation
     * request is not sent until the `sample` has been created successfuly.
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id
     *            where id is a string of 24 alpha-numeric chars for the
     *            remote dataset to be use to create the sample.
     * @param args
     *            set of parameters for the new sample. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the sample. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject create(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        if (datasetId == null || datasetId.length() == 0 ) {
            logger.info("Wrong dataset id. Id cannot be null");
            return null;
        }

        try {
            JSONObject requestObject = new JSONObject();

            // If the original resource is a Source
            if( datasetId.matches(DATASET_RE) ) {
                // If the original resource is a Dataset
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                            .datasetIsReady(datasetId)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                if (args != null) {
                    requestObject = args;
                }
                requestObject.put("dataset", datasetId);
            } else {
                throw new IllegalArgumentException(String.format("A dataset id is needed " +
                        "to create a sample. %s found.", datasetId));
            }

            return createResource(SAMPLE_URL, requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Failed to generate the sample.", e);
            return null;
        }
    }


    /**
     * Retrieves a sample.
     * 
     * GET
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param sampleId
     *            a unique identifier in the form sample/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject get(final String sampleId) {
        if (sampleId == null || sampleId.length() == 0
                || !(sampleId.matches(SAMPLE_RE))) {
            logger.info("Wrong sample id");
            return null;
        }

        return getResource(BIGML_URL + sampleId);
    }

    /**
     * Retrieves a sample.
     * 
     * GET
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param sample
     *            a sample JSONObject
     * 
     */
    @Override
    public JSONObject get(final JSONObject sample) {
        String resourceId = (String) sample.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves an sample.
     *
     * GET /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param sampleId
     *            a unique identifier in the form sample/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String sampleId, final String queryString) {
        return get(BIGML_URL + sampleId, queryString, null, null);
    }

    /**
     * Retrieves an sample.
     *
     * GET /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param sampleId
     *            a unique identifier in the form sample/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final String sampleId, final String queryString,
                          final String apiUser, final String apiKey) {
        if (sampleId == null || sampleId.length() == 0
                || !(sampleId.matches(SAMPLE_RE))) {
            logger.info("Wrong sample id");
            return null;
        }

        return getResource(BIGML_URL + sampleId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves an sample.
     *
     * GET /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param sample
     *            a model JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject sample, final String queryString) {
        String resourceId = (String) sample.get("resource");
        return get(resourceId, queryString, null, null);
    }

    /**
     * Retrieves an sample.
     *
     * GET /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param sample
     *            a sample JSONObject
     * @param queryString
     *            query for filtering
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject get(final JSONObject sample, final String queryString,
                          final String apiUser, final String apiKey) {
        String resourceId = (String) sample.get("resource");
        return get(resourceId, queryString, apiUser, apiKey);
    }

    /**
     * Checks whether a sample's status is FINISHED.
     * 
     * @param sampleId
     *            a unique identifier in the form sample/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public boolean isReady(final String sampleId) {
        return isResourceReady(get(sampleId));
    }

    /**
     * Checks whether a sample's status is FINISHED.
     * 
     * @param sample
     *            a sample JSONObject
     * 
     */
    @Override
    public boolean isReady(final JSONObject sample) {
        String resourceId = (String) sample.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your samples.
     * 
     * GET /andromeda/sample?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(SAMPLE_URL, queryString);
    }

    /**
     * Updates a sample.
     * 
     * PUT
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param sampleId
     *            a unique identifier in the form sample/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the sample. Optional
     * 
     */
    @Override
    public JSONObject update(final String sampleId, final String changes) {
        if (sampleId == null || sampleId.length() == 0
                || !(sampleId.matches(SAMPLE_RE))) {
            logger.info("Wrong sample id");
            return null;
        }
        return updateResource(BIGML_URL + sampleId, changes);
    }

    /**
     * Updates a sample.
     * 
     * PUT
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param sample
     *            a sample JSONObject
     * @param changes
     *            set of parameters to update the sample. Optional
     * 
     */
    @Override
    public JSONObject update(final JSONObject sample, final JSONObject changes) {
        String resourceId = (String) sample.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a sample.
     * 
     * DELETE
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param sampleId
     *            a unique identifier in the form sample/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject delete(final String sampleId) {
        if (sampleId == null || sampleId.length() == 0
                || !(sampleId.matches(SAMPLE_RE))) {
            logger.info("Wrong sample id");
            return null;
        }
        return deleteResource(BIGML_URL + sampleId);
    }

    /**
     * Deletes a sample.
     * 
     * DELETE
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param sample
     *            a sample JSONObject
     * 
     */
    @Override
    public JSONObject delete(final JSONObject sample) {
        String resourceId = (String) sample.get("resource");
        return delete(resourceId);
    }

    /**
     * Returns the ids of the fields that contain errors and their number.
     *
     * The sample argument is a sample resource structure
     *
     * @param sample
     *            a sample JSONObject
     *
     */
    public Map<String, Long> getErrorCounts(final JSONObject sample) {
        Map<String, Long> errorsDict = new HashMap<String, Long>();

        JSONObject loadedSample = get(sample);
        if( loadedSample != null ) {
            JSONObject errors = (JSONObject) Utils.getJSONObject(loadedSample, "object.status.field_errors",
                    null);
            for (Object fieldId : errors.keySet()) {
                errorsDict.put(fieldId.toString(), ((Number) ((JSONObject) errors.get(fieldId)).get("total")).longValue());
            }
        }

        return errorsDict;
    }

}