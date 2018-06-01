package org.bigml.binding.resources;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Set;

import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.MultipartUtility;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete sources.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/sources
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
    	super.init(null, null, null);
        this.resourceRe = SOURCE_RE;
        this.resourceUrl = SOURCE_URL;
        this.resourceName = "source";
    }

    /**
     * Constructor
     *
     */
    public Source(final String apiUser, final String apiKey) {
    	super.init(apiUser, apiKey, null);
        this.resourceRe = SOURCE_RE;
        this.resourceUrl = SOURCE_URL;
        this.resourceName = "source";
    }

    /**
     * Constructor
     *
     */
    public Source(final String apiUser, final String apiKey, final CacheManager cacheManager) {
    	super.init(apiUser, apiKey, cacheManager);
        this.resourceRe = SOURCE_RE;
        this.resourceUrl = SOURCE_URL;
        this.resourceName = "source";
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
    public JSONObject createLocalSource(final String fileName, final String name,
            final JSONObject sourceParser) {
        return this.createLocalSource(fileName, name, sourceParser, null);
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
     * @param args
     *            set of parameters for the new model. Optional
     */
    public JSONObject createLocalSource(final String fileName, final String name,
            final JSONObject sourceParser, final JSONObject args) {
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
            MultipartUtility multipartUtility = new MultipartUtility(SOURCE_URL + bigmlAuth, "UTF-8");

            multipartUtility.addFilePart("bin", new File(fileName));

            if (name != null) {
                multipartUtility.addFormField("name", name);
            }

            // Source parser
            if (sourceParser != null) {
                multipartUtility.addFormField("source_parser", Utils.unescapeJSONString(sourceParser.toJSONString()));
            }

            if (args != null) {
                for (String key : (Set<String>)args.keySet()) {
                    multipartUtility.addFormField(key, Utils.unescapeJSONString(String.valueOf(args.get(key))));
                }
            }

            HttpURLConnection connection = multipartUtility.finish();
            code = connection.getResponseCode();

            if (code == HTTP_CREATED) {
                location = connection.getHeaderField(location);
                resource = (JSONObject) JSONValue.parse(Utils
                        .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                resourceId = (String) resource.get("resource");
                error = new JSONObject();
            } else {
                if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
                        || code == HTTP_PAYMENT_REQUIRED
                        || code == HTTP_NOT_FOUND) {
                    error = (JSONObject) JSONValue.parse(Utils
                            .inputStreamAsString(connection.getInputStream(), "UTF-8"));
                } else {
                    logger.info("Unexpected error (" + code + ")");
                    code = HTTP_INTERNAL_SERVER_ERROR;
                }
            }

        } catch (Throwable e) {
            logger.error("Error creating source", e);
        }

        // Cache the resource if the resource if ready
        if( cacheManager != null && resource != null && isResourceReady(resource)) {
            cacheManager.put(resourceId, null, resource);
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
     * Creates a source using a Batch Prediction Id.
     *
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     *
     * @param batchPredictionId
     *            the ID of the batch prediction resource to use
     * @param sourceParser
     *            set of parameters to create the source. Optional
     *
     */
    public JSONObject createSourceFromBatchPrediction(final String batchPredictionId,
            final JSONObject sourceParser) {

        return this.createSourceFromBatchPrediction(batchPredictionId, sourceParser, null);
    }

    /**
     * Creates a source using a Batch Prediction Id.
     *
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     *
     * @param batchPredictionId
     *            the ID of the batch prediction resource to use
     * @param sourceParser
     *            set of parameters to create the source. Optional
     * @param args
     *            set of parameters for the new model. Optional
     */
    public JSONObject createSourceFromBatchPrediction(final String batchPredictionId,
            final JSONObject sourceParser, final JSONObject args) {

        String url = String.format("%s%s%s%s", BIGML_URL, batchPredictionId,
                BatchPrediction.DOWNLOAD_DIR, bigmlAuth);

        return createRemoteSource(url, sourceParser, args);
    }

    /**
     * Creates a source using an Anomaly Score Id.
     *
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     *
     * @param anomalyScoreId
     *            the ID of the anomaly score resource to use
     * @param sourceParser
     *            set of parameters to create the source. Optional
     *
     */
    public JSONObject createSourceFromBatchAnomalyScore(final String anomalyScoreId,
            final JSONObject sourceParser) {

        return this.createSourceFromBatchAnomalyScore(anomalyScoreId, sourceParser, null);
    }

    /**
     * Creates a source using an Anomaly Score Id.
     *
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     *
     * @param anomalyScoreId
     *            the ID of the anomaly score resource to use
     * @param sourceParser
     *            set of parameters to create the source. Optional
     * @param args
     *            set of parameters for the new model. Optional
     */
    public JSONObject createSourceFromBatchAnomalyScore(final String anomalyScoreId,
            final JSONObject sourceParser, final JSONObject args) {

        String url = String.format("%s%s%s%s", BIGML_URL, anomalyScoreId,
                BatchPrediction.DOWNLOAD_DIR, bigmlAuth);

        return createRemoteSource(url, sourceParser, args);
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
        return this.createRemoteSource(url, sourceParser, null);
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
     * @param args
     *            set of parameters for the new model. Optional
     */
    public JSONObject createRemoteSource(final String url,
            final JSONObject sourceParser, final JSONObject args) {
        try {
            JSONObject requestObject = new JSONObject();
            if (sourceParser != null) {
                requestObject = sourceParser;
            }
            if (args != null) {
                requestObject.putAll(args);
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

}
