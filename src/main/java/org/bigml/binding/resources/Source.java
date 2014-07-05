package org.bigml.binding.resources;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete sources.
 * 
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/sources
 * 
 * 
 */
public class Source extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Source.class);

    /**
     * Constructor
     * 
     */
    public Source() {
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
    public Source(final String apiUser, final String apiKey,
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
     * Creates a source using a local file.
     * 
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: multipart/form-data;
     * 
     * @param fileName
     *            file containing your data in csv format. It can be compressed,
     *            gzipped, or zipped. Required multipart/form-data;
     *            charset=utf-8
     * @param name
     *            the name you want to give to the new source. Optional
     * @param sourceParser
     *            set of parameters to parse the source. Optional
     */
    @Deprecated
    public JSONObject createLocalSource(final String fileName, String name,
            String sourceParser) {
        JSONObject sourceParserJson = (JSONObject) JSONValue
                .parse(sourceParser);
        return this.createLocalSource(fileName, name, sourceParserJson);
    }

    /**
     * Creates a source using a local file.
     * 
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: multipart/form-data;
     * 
     * @param fileName
     *            file containing your data in csv format. It can be compressed,
     *            gzipped, or zipped. Required multipart/form-data;
     *            charset=utf-8
     * @param name
     *            the name you want to give to the new source. Optional
     * @param sourceParser
     *            set of parameters to parse the source. Optional
     * 
     */
    public JSONObject createLocalSource(final String fileName, String name,
            JSONObject sourceParser) {
        int code = HTTP_INTERNAL_SERVER_ERROR;
        String resourceId = null;
        JSONObject resource = null;
        String location = "";

        JSONObject error = new JSONObject();
        JSONObject status = new JSONObject();
        status.put("code", code);
        status.put("message", "The resource couldn't be created");
        error.put("status", status);

        try {
            DefaultHttpClient httpclient = Utils.httpClient();
            HttpPost httppost = new HttpPost(SOURCE_URL + bigmlAuth);

            MultipartEntity reqEntity = new MultipartEntity();

            // Source file
            FileBody bin = new FileBody(new File(fileName));
            reqEntity.addPart("bin", bin);

            // Source name
            if (name != null) {
                reqEntity.addPart("name", new StringBody(name));
            }

            // Source parser
            if (sourceParser != null) {
                reqEntity.addPart("source_parser",
                        new StringBody(sourceParser.toJSONString()));
            }

            httppost.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            code = response.getStatusLine().getStatusCode();

            if (code == HTTP_CREATED) {
                location = response.getHeaders("location")[0].getValue();
                resource = (JSONObject) JSONValue.parse(Utils
                        .inputStreamAsString(resEntity.getContent()));
                resourceId = (String) resource.get("resource");
                error = new JSONObject();
            } else {
                if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
                        || code == HTTP_PAYMENT_REQUIRED
                        || code == HTTP_NOT_FOUND) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(resEntity.getContent()));
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }

        } catch (Throwable e) {
            logger.error("Error creating source", e);
        }

        JSONObject result = new JSONObject();
        result.put("code", code);
        result.put("resource", resourceId);
        result.put("location", location);
        result.put("object", resource);
        result.put("error", error);
        return result;
    }

    /**
     * Creates a source using a URL.
     * 
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     * 
     * @param url
     *            url for remote source
     * @param sourceParser
     *            set of parameters to create the source. Optional
     * 
     */
    @Deprecated
    public JSONObject createRemoteSource(final String url,
            final String sourceParser) {
        JSONObject requestObject = sourceParser != null ? (JSONObject) JSONValue
                .parse(sourceParser) : null;
        return createRemoteSource(url, requestObject);
    }

    /**
     * Creates a source using a URL.
     * 
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     * 
     * @param url
     *            url for remote source
     * @param sourceParser
     *            set of parameters to create the source. Optional
     * 
     */
    public JSONObject createRemoteSource(final String url,
            final JSONObject sourceParser) {
        try {
            JSONObject requestObject = new JSONObject();
            if (sourceParser != null) {
                requestObject = sourceParser;
            }
            requestObject.put("remote", url);

            return createResource(SOURCE_URL, requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating source");
            return null;
        }
    }

    /**
     * Creates a source using inline data.
     * 
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     * 
     * @param data
     *            inline data for source
     * @param sourceParser
     *            set of parameters to create the source. Optional
     * 
     */
    @Deprecated
    public JSONObject createInlineSource(final String data,
            final String sourceParser) {

        JSONObject requestObject = sourceParser != null ? (JSONObject) JSONValue
                .parse(sourceParser) : null;
        return createInlineSource(data, requestObject);
    }

    /**
     * Creates a source using inline data.
     * 
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     * 
     * @param data
     *            inline data for source
     * @param sourceParser
     *            set of parameters to create the source. Optional
     * 
     */
    public JSONObject createInlineSource(final String data,
            final JSONObject sourceParser) {
        try {
            JSONObject requestObject = new JSONObject();
            if (sourceParser != null) {
                requestObject = sourceParser;
            }
            requestObject.put("data", data);
            return createResource(SOURCE_URL, requestObject.toJSONString());
        } catch (Throwable e) {
            logger.error("Error creating source");
            return null;
        }
    }

    /**
     * Retrieves a remote source.
     * 
     * GET /andromeda/source/4f64be4003ce890b4500000b?username=$BIGML_USERNAME;
     * api_key=$BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject get(final String sourceId) {
        if (sourceId == null || sourceId.length() == 0
                || !sourceId.matches(SOURCE_RE)) {
            logger.info("Wrong source id");
            return null;
        }

        return getResource(BIGML_URL + sourceId);
    }

    /**
     * Retrieves a remote source.
     * 
     * GET /andromeda/source/4f64be4003ce890b4500000b?username=$BIGML_USERNAME;
     * api_key=$BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param source
     *            a source JSONObject.
     * 
     */
    @Override
    public JSONObject get(final JSONObject source) {
        String resourceId = (String) source.get("resource");
        return get(resourceId);
    }

    /**
     * Checks whether a source's status is FINISHED.
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    @Override
    public boolean isReady(final String sourceId) {
        return isResourceReady(get(sourceId));
    }

    /**
     * Checks whether a source's status is FINISHED.
     * 
     * @param source
     *            a source JSONObject
     * 
     */
    @Override
    public boolean isReady(final JSONObject source) {
        String resourceId = (String) source.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your remote sources.
     * 
     * GET /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(SOURCE_URL, queryString);
    }

    /**
     * Updates a source.
     * 
     * Updates remote `source` with `changes'.
     * 
     * POST /andromeda/source/4f64191d03ce89860a000000?username=$BIGML_USERNAME;
     * api_key=$BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type:
     * application/json
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    @Override
    public JSONObject update(final String sourceId, final String changes) {
        if (sourceId == null || sourceId.length() == 0
                || !sourceId.matches(SOURCE_RE)) {
            logger.info("Wrong source id");
            return null;
        }
        return updateResource(BIGML_URL + sourceId, changes);
    }

    /**
     * Updates a source.
     * 
     * Updates remote `source` with `changes'.
     * 
     * POST /andromeda/source/4f64191d03ce89860a000000?username=$BIGML_USERNAME;
     * api_key=$BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type:
     * application/json
     * 
     * @param source
     *            a source JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    @Override
    public JSONObject update(final JSONObject source, final JSONObject changes) {
        String resourceId = (String) source.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a remote source permanently.
     * 
     * DELETE
     * /andromeda/source/4f603fe203ce89bb2d000000?username=$BIGML_USERNAME
     * ;api_key=$BIGML_API_KEY; HTTP/1.1
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    @Override
    public JSONObject delete(final String sourceId) {
        if (sourceId == null || sourceId.length() == 0
                || !sourceId.matches(SOURCE_RE)) {
            logger.info("Wrong source id");
            return null;
        }
        return deleteResource(BIGML_URL + sourceId);
    }

    /**
     * Deletes a remote source permanently.
     * 
     * DELETE
     * /andromeda/source/4f603fe203ce89bb2d000000?username=$BIGML_USERNAME
     * ;api_key=$BIGML_API_KEY; HTTP/1.1
     * 
     * @param source
     *            a source JSONObject
     * 
     */
    @Override
    public JSONObject delete(final JSONObject source) {
        String resourceId = (String) source.get("resource");
        return delete(resourceId);
    }

}