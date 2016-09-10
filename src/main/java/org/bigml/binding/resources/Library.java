package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * This class is used by the BigML class as a mixin that provides the Whizzml
 * Library' REST calls.
 *
 * It should not be instantiated independently.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/libraries
 *
 */
public class Library extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Library.class);

    /**
     * Constructor
     *
     */
    public Library() {
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
    public Library(final String apiUser, final String apiKey,
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
    public Library(final String apiUser, final String apiKey,
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
     * Check if the current resource is a Library
     *
     * @param resource the resource to be checked
     * @return true if its an Library
     */
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(LIBRARY_RE);
    }

    /**
     * Creates a whizzml library from its source code.
     *
     * POST /andromeda/library?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param source
     *            source code for the library. It can be either
     *              - string: source code
     *              - library id: the ID for an existing whizzml library
     *              - path: the path to a file containing the source code
     * @param args
     *            set of parameters for the new library. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the library. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(String source, JSONObject args,
                             Integer waitTime, Integer retries) {

        if (source == null || source.length() == 0 ) {
            logger.info("A valid code string or a library id must be provided.");
            return null;
        }

        try {
            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            if (source.matches(LIBRARY_RE)) {
                waitTime = waitTime != null ? waitTime : 3000;
                retries = retries != null ? retries : 10;
                if (waitTime > 0) {
                    int count = 0;
                    while (count < retries
                            && !BigMLClient.getInstance(this.devMode)
                            .libraryIsReady(source)) {
                        Thread.sleep(waitTime);
                        count++;
                    }
                }

                requestObject.put("origin", source);
                return createResource(LIBRARY_URL, requestObject.toJSONString());
            }

            try {
                File file = new File(source);
                if (file.exists()) {
                    source = Utils.readFile(source);
                }
            } catch (Throwable e) {
                logger.error("Could not open the source code file " + source, e);
            }

            requestObject.put("source_code", source);
            return createResource(LIBRARY_URL, requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating library");
            return null;
        }
    }

    /**
     * Retrieves a whizzml library.
     *
     * GET
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param libraryId
     *            a unique identifier in the form library/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String libraryId) {
        if (libraryId == null || libraryId.length() == 0
                || !libraryId.matches(LIBRARY_RE)) {
            logger.info("Wrong library id");
            return null;
        }

        return getResource(BIGML_URL + libraryId);
    }

    /**
     * Retrieves a whizzml library.
     *
     * GET
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param library
     *            a library JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject library) {
        String resourceId = (String) library.get("resource");
        return get(resourceId);
    }

    /**
     * Retrieves a whizzml library.
     *
     * GET
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param libraryId
     *            a unique identifier in the form library/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject get(final String libraryId, final String queryString) {
        if (libraryId == null || libraryId.length() == 0
                || !libraryId.matches(LIBRARY_RE)) {
            logger.info("Wrong library id");
            return null;
        }

        return getResource(BIGML_URL + libraryId, queryString);
    }

    /**
     * Retrieves a whizzml library.
     *
     * GET
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param library
     *            a library JSONObject
     * @param queryString
     *            query for filtering
     *
     */
    public JSONObject get(final JSONObject library, final String queryString) {
        String resourceId = (String) library.get("resource");
        return get(resourceId, queryString);
    }

    /**
     * Checks whether a whizzml library's status is FINISHED.
     *
     * @param libraryId
     *            a unique identifier in the form library/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String libraryId) {
        return isResourceReady(get(libraryId));
    }

    /**
     * Checks whether a whizzml library status is FINISHED.
     *
     * @param library
     *            a library JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject library) {
        return isResourceReady(library)
                || isReady((String) library.get("resource"));
    }

    /**
     * Lists all your whizzml libraries.
     *
     * GET /andromeda/library?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(LIBRARY_URL, queryString);
    }

    /**
     * Updates a whizzml library.
     *
     * PUT
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param libraryId
     *            a unique identifier in the form library/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the library. Optional
     *
     */
    @Override
    public JSONObject update(final String libraryId, final String changes) {
        if (libraryId == null || libraryId.length() == 0
                || !(libraryId.matches(LIBRARY_RE))) {
            logger.info("Wrong library id");
            return null;
        }
        return updateResource(BIGML_URL + libraryId, changes);
    }

    /**
     * Updates a whizzml library.
     *
     * PUT
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param library
     *            a library JSONObject
     * @param changes
     *            set of parameters to update the library. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject library, final JSONObject changes) {
        String resourceId = (String) library.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a whizzml library.
     *
     * DELETE
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param libraryId
     *            a unique identifier in the form library/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject delete(final String libraryId) {
        if (libraryId == null || libraryId.length() == 0
                || !(libraryId.matches(LIBRARY_RE))) {
            logger.info("Wrong library id");
            return null;
        }
        return deleteResource(BIGML_URL + libraryId);
    }

    /**
     * Deletes a library.
     *
     * DELETE
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param library
     *            a library JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject library) {
        String resourceId = (String) library.get("resource");
        return delete(resourceId);
    }
}