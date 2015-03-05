package org.bigml.binding;

import org.bigml.binding.resources.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Entry point to create, retrieve, list, update, and delete sources, datasets,
 * models, predictions, evaluations and ensembles.
 * 
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers
 * 
 * Resources are wrapped in a dictionary that includes: code: HTTP status code
 * resource: The resource/id location: Remote location of the resource object:
 * The resource itself error: An error code and message
 * 
 * If left unspecified, `username` and `api_key` will default to the values of
 * the `BIGML_USERNAME` and `BIGML_API_KEY` environment variables respectively.
 * 
 * If `dev_mode` is set to `True`, the API will be used in development mode
 * where the size of your datasets are limited but you are not charged any
 * credits.
 * 
 * If storage is set to a directory name, the resources obtained in CRU
 * operations will be stored in the given directory.
 */
public class BigMLClient {
    // URLs below don't care about BigML.io version. They will hit last
    // (current)
    // BigML version
    final public static String BIGML_URL = "https://bigml.io/";
    final public static String BIGML_DEV_URL = "https://bigml.io/dev/";

    final public static Locale DEFAUL_LOCALE = Locale.ENGLISH;

    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(BigMLClient.class.getName());

    private static BigMLClient instance = null;

    private String bigmlUrl;
    private String bigmlUser;
    private String bigmlDomain;
    private String bigmlApiKey;

    /**
     * A string to be hashed to generate deterministic samples
     */
    private String seed;

    private Source source;
    private Dataset dataset;
    private Model model;
    private Prediction prediction;
    private Evaluation evaluation;
    private Ensemble ensemble;
    private Anomaly anomaly;
    private AnomalyScore anomalyScore;
    private BatchAnomalyScore batchAnomalyScore;
    private BatchPrediction batchPrediction;
    private Cluster cluster;
    private Centroid centroid;
    private BatchCentroid batchCentroid;
    private Properties props;
    private Boolean devMode = false;
    private String storage;

    protected BigMLClient() {
    }

    public static BigMLClient getInstance() throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(false);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(null, null, false, storage);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String seed, final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(null, null, seed, false, storage);
        }
        return instance;
    }

    public static BigMLClient getInstance(final boolean devMode)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(devMode);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String seed, final boolean devMode)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(seed, devMode);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String apiUser,
            final String apiKey, final boolean devMode)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(apiUser, apiKey, devMode);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String apiUser,
            final String apiKey, final String seed, final boolean devMode)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(apiUser, apiKey, seed, devMode);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String apiUser,
            final String apiKey, final boolean devMode, final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(apiUser, apiKey, devMode, storage);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String apiUser,
            final String apiKey, final String seed, final boolean devMode, final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(apiUser, apiKey, seed, devMode, storage);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String bigmlDomain, final String apiUser,
            final String apiKey, final String seed, final boolean devMode, final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(bigmlDomain, apiUser, apiKey, seed, devMode, storage);
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    /**
     * Initialization object.
     */
    private void init(final boolean devMode) throws AuthenticationException {
        this.devMode = devMode;
        initConfiguration();

        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        if (this.bigmlUser == null || this.bigmlUser.equals("")
                || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
            this.bigmlUser = props.getProperty("BIGML_USERNAME");
            this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
            if (this.bigmlUser == null || this.bigmlUser.equals("")
                    || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
                AuthenticationException ex = new AuthenticationException(
                        "Missing authentication information.");
                logger.info(instance.toString(), ex);
                throw ex;
            }
        }

        // The seed to be used to create deterministic samples and models
        this.seed = System.getProperty("BIGML_SEED");
        if( this.seed == null || this.seed.equals("") ) {
            this.seed = props.getProperty("BIGML_SEED");
        }

        initResources();
    }

    /**
     * Initialization object.
     */
    private void init(final String seed, final boolean devMode) throws AuthenticationException {
        this.devMode = devMode;
        initConfiguration();

        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        if (this.bigmlUser == null || this.bigmlUser.equals("")
                || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
            this.bigmlUser = props.getProperty("BIGML_USERNAME");
            this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
            if (this.bigmlUser == null || this.bigmlUser.equals("")
                    || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
                AuthenticationException ex = new AuthenticationException(
                        "Missing authentication information.");
                logger.info(instance.toString(), ex);
                throw ex;
            }
        }

        // The seed to be used to create deterministic samples and models
        this.seed = seed != null ? seed : System.getProperty("BIGML_SEED");
        if( this.seed == null || this.seed.equals("") ) {
            this.seed = props.getProperty("BIGML_SEED");
        }

        initResources();
    }

    /**
     * Initialization object.
     */
    private void init(final String apiUser, final String apiKey,
            final boolean devMode) throws AuthenticationException {
        this.devMode = devMode;
        initConfiguration();

        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        if (this.bigmlUser == null || this.bigmlUser.equals("")
                || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
            this.bigmlUser = props.getProperty("BIGML_USERNAME");
            this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
            if (this.bigmlUser == null || this.bigmlUser.equals("")
                    || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
                AuthenticationException ex = new AuthenticationException(
                        "Missing authentication information.");
                logger.info(instance.toString(), ex);
                throw ex;
            }
        }

        // The seed to be used to create deterministic samples and models
        this.seed = System.getProperty("BIGML_SEED");
        if( this.seed == null || this.seed.equals("") ) {
            this.seed = props.getProperty("BIGML_SEED");
        }

        initResources();
    }

    /**
     * Initialization object.
     */
    private void init(final String apiUser, final String apiKey, String seed,
            final boolean devMode) throws AuthenticationException {
        this.devMode = devMode;
        initConfiguration();

        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        if (this.bigmlUser == null || this.bigmlUser.equals("")
                || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
            this.bigmlUser = props.getProperty("BIGML_USERNAME");
            this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
            if (this.bigmlUser == null || this.bigmlUser.equals("")
                    || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
                AuthenticationException ex = new AuthenticationException(
                        "Missing authentication information.");
                logger.info(instance.toString(), ex);
                throw ex;
            }
        }

        // The seed to be used to create deterministic samples and models
        this.seed = seed != null ? seed : System.getProperty("BIGML_SEED");
        if( this.seed == null || this.seed.equals("") ) {
            this.seed = props.getProperty("BIGML_SEED");
        }

        initResources();
    }

    /**
     * Initialization object.
     */
    private void init(final String apiUser, final String apiKey,
            final boolean devMode, final String storage)
            throws AuthenticationException {
        this.devMode = devMode;
        this.storage = storage;
        initConfiguration();

        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        if (this.bigmlUser == null || this.bigmlUser.equals("")
                || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
            this.bigmlUser = props.getProperty("BIGML_USERNAME");
            this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
            if (this.bigmlUser == null || this.bigmlUser.equals("")
                    || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
                AuthenticationException ex = new AuthenticationException(
                        "Missing authentication information.");
                logger.info(instance.toString(), ex);
                throw ex;
            }
        }

        // The seed to be used to create deterministic samples and models
        this.seed = System.getProperty("BIGML_SEED");
        if( this.seed == null || this.seed.equals("") ) {
            this.seed = props.getProperty("BIGML_SEED");
        }

        initResources();
    }

    /**
     * Initialization object.
     */
    private void init(final String apiUser, final String apiKey, String seed,
            final boolean devMode, final String storage)
            throws AuthenticationException {
        this.devMode = devMode;
        this.storage = storage;
        initConfiguration();

        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        if (this.bigmlUser == null || this.bigmlUser.equals("")
                || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
            this.bigmlUser = props.getProperty("BIGML_USERNAME");
            this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
            if (this.bigmlUser == null || this.bigmlUser.equals("")
                    || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
                AuthenticationException ex = new AuthenticationException(
                        "Missing authentication information.");
                logger.info(instance.toString(), ex);
                throw ex;
            }
        }

        // The seed to be used to create deterministic samples and models
        this.seed = seed != null ? seed : System.getProperty("BIGML_SEED");
        if( this.seed == null || this.seed.equals("") ) {
            this.seed = props.getProperty("BIGML_SEED");
        }

        initResources();
    }

    /**
     * Initialization object.
     */
    private void init(final String bigmlDomain, final String apiUser,
                      final String apiKey, String seed,
            final boolean devMode, final String storage)
            throws AuthenticationException {
        this.bigmlDomain = bigmlDomain;
        this.devMode = devMode;
        this.storage = storage;
        initConfiguration();

        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        if (this.bigmlUser == null || this.bigmlUser.equals("")
                || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
            this.bigmlUser = props.getProperty("BIGML_USERNAME");
            this.bigmlApiKey = props.getProperty("BIGML_API_KEY");
            if (this.bigmlUser == null || this.bigmlUser.equals("")
                    || this.bigmlApiKey == null || this.bigmlApiKey.equals("")) {
                AuthenticationException ex = new AuthenticationException(
                        "Missing authentication information.");
                logger.info(instance.toString(), ex);
                throw ex;
            }
        }

        // The seed to be used to create deterministic samples and models
        this.seed = seed != null ? seed : System.getProperty("BIGML_SEED");
        if( this.seed == null || this.seed.equals("") ) {
            this.seed = props.getProperty("BIGML_SEED");
        }

        initResources();
    }

    private void initConfiguration() {
        try {
            props = new Properties();
            FileInputStream fis = new FileInputStream(new File(
                    "src/main/resources/binding.properties"));
            props.load(fis);
            fis.close();

            if( bigmlDomain != null && bigmlDomain.length() > 0 ) {
                bigmlUrl = this.devMode ? (bigmlDomain + (bigmlDomain.endsWith("/") ? "" : "/") +
                        "dev/") : (bigmlDomain + (bigmlDomain.endsWith("/") ? "" : "/"));
            } else {
                bigmlUrl = this.devMode ? props.getProperty("BIGML_DEV_URL",
                        BIGML_DEV_URL) : props.getProperty("BIGML_URL", BIGML_URL);
            }
        } catch (Throwable e) {
            // logger.error("Error loading configuration", e);
            bigmlUrl = this.devMode ? BIGML_DEV_URL : BIGML_URL;
        }
    }

    private void initResources() {
        source = new Source(this.bigmlUser, this.bigmlApiKey, this.devMode);
        dataset = new Dataset(this.bigmlUser, this.bigmlApiKey, this.devMode);
        model = new Model(this.bigmlUser, this.bigmlApiKey, this.devMode);
        prediction = new Prediction(this.bigmlUser, this.bigmlApiKey,
                this.devMode);
        evaluation = new Evaluation(this.bigmlUser, this.bigmlApiKey,
                this.devMode);
        ensemble = new Ensemble(this.bigmlUser, this.bigmlApiKey, this.devMode);
        anomaly = new Anomaly(this.bigmlUser, this.bigmlApiKey, this.devMode);
        anomalyScore = new AnomalyScore(this.bigmlUser, this.bigmlApiKey, this.devMode);
        batchAnomalyScore = new BatchAnomalyScore(this.bigmlUser, this.bigmlApiKey,
                this.devMode);
        batchPrediction = new BatchPrediction(this.bigmlUser, this.bigmlApiKey,
                this.devMode);
        cluster = new Cluster(this.bigmlUser, this.bigmlApiKey, this.devMode);
        centroid = new Centroid(this.bigmlUser, this.bigmlApiKey, this.devMode);
        batchCentroid = new BatchCentroid(this.bigmlUser, this.bigmlApiKey,
                this.devMode);
    }

    public String getBigMLUrl() {
        return bigmlUrl;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    // ################################################################
    // #
    // # Sources
    // # https://bigml.com/developers/sources
    // #
    // ################################################################

    /**
     * Creates a new source.
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
    public JSONObject createSource(final String fileName, String name,
            String sourceParser) {
        return source.createLocalSource(fileName, name, sourceParser);
    }

    /**
     * Creates a new source.
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
    public JSONObject createSource(final String fileName, String name,
            JSONObject sourceParser) {
        return source.createLocalSource(fileName, name, sourceParser);
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
        return source.createRemoteSource(url, sourceParser);
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
        return source.createRemoteSource(url, sourceParser);
    }

    /**
     * Creates a source using a BatchPrediction ID.
     *
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     *
     * @param batchPredictionId
     *            the resource ID of the batch prediction resource
     * @param sourceParser
     *            set of parameters to create the source. Optional
     *
     */
    public JSONObject createSourceFromBatchPrediction(final String batchPredictionId,
            final JSONObject sourceParser) {

        return source.createSourceFromBatchPrediction(batchPredictionId, sourceParser);
    }

    /**
     * Creates a source using a BatchAnomalyScore ID.
     *
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     *
     * @param batchAnomalyScoreId
     *            the resource ID of the batch anomaly score resource
     * @param sourceParser
     *            set of parameters to create the source. Optional
     *
     */
    public JSONObject createSourceFromBatchAnomalyScore(final String batchAnomalyScoreId,
            final JSONObject sourceParser) {

        return source.createSourceFromBatchAnomalyScore(batchAnomalyScoreId, sourceParser);
    }

    /**
     * Creates a source using a URL.
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
        return source.createInlineSource(data, sourceParser);
    }

    /**
     * Creates a source using a URL.
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
        return source.createInlineSource(data, sourceParser);
    }

    /**
     * Retrieves a remote source.
     * 
     * GET /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getSource(final String sourceId) {
        return source.get(sourceId);
    }

    /**
     * Retrieves a remote source.
     * 
     * GET /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param sourceJSON
     *            a source JSONObject
     * 
     */
    public JSONObject getSource(final JSONObject sourceJSON) {
        return source.get(sourceJSON);
    }

    /**
     * Checks whether a source's status is FINISHED.
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    public boolean sourceIsReady(final String sourceId) {
        return source.isReady(sourceId);
    }

    /**
     * Checks whether a source's status is FINISHED.
     * 
     * @param sourceJSON
     *            a source JSONObject
     * 
     */
    public boolean sourceIsReady(final JSONObject sourceJSON) {
        return source.isReady(sourceJSON);
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
    public JSONObject listSources(final String queryString) {
        return source.list(queryString);
    }

    /**
     * Updates a source.
     * 
     * Updates remote `source` with `changes'.
     * 
     * POST
     * /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    public JSONObject updateSource(final String sourceId, final String changes) {
        return source.update(sourceId, changes);
    }

    /**
     * Updates a source.
     * 
     * Updates remote `source` with `changes'.
     * 
     * POST
     * /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param sourceJSON
     *            a source JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    public JSONObject updateSource(final JSONObject sourceJSON,
            final JSONObject changes) {
        return source.update(sourceJSON, changes);
    }

    /**
     * Deletes a remote source permanently.
     * 
     * DELETE
     * /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    public JSONObject deleteSource(final String sourceId) {
        return source.delete(sourceId);
    }

    /**
     * Deletes a remote source permanently.
     * 
     * DELETE
     * /andromeda/source/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param sourceJSON
     *            a source JSONObject
     * 
     */
    public JSONObject delete(final JSONObject sourceJSON) {
        return source.delete(sourceJSON);
    }

    // ################################################################
    // #
    // # Datasets
    // # https://bigml.com/developers/datasets
    // #
    // ################################################################

    /**
     * Creates a remote dataset.
     * 
     * Uses remote `source` to create a new dataset using the arguments in
     * `args`. If `wait_time` is higher than 0 then the dataset creation request
     * is not sent until the `source` has been created successfuly.
     * 
     * POST /andromeda/dataset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param sourceId
     *            a unique identifier in the form source/id where id is a string
     *            of 24 alpha-numeric chars for the source to attach the
     *            dataset.
     * @param args
     *            set of parameters for the new dataset. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for source
     *            before to start to create the dataset. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createDataset(final String sourceId, String args,
            Integer waitTime, Integer retries) {
        return dataset.create(sourceId, args, waitTime, retries);
    }

    /**
     * Creates a remote dataset.
     * 
     * Uses a remote resource to create a new dataset using the arguments in `args`.
     * The allowed remote resources can be:
     *      - source
     *      - dataset
     *      - cluster
     * In the case of using cluster id as origin_resources, a centroid must
     * also be provided in the args argument. The first centroid is used
     * otherwise.
     *
     * If `wait_time` is higher than 0 then the dataset creation
     * request is not sent until the `source` has been created successfuly.
     *
     * @param resourceId
     *            a unique identifier in the form resource_name/id where id is a string
     *            of 24 alpha-numeric chars for the remote resource to attach the
     *            dataset.
     * @param args
     *            set of parameters for the new dataset. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for source
     *            before to start to create the dataset. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createDataset(final String resourceId, JSONObject args,
            Integer waitTime, Integer retries) {
        return dataset.create(resourceId, args, waitTime, retries);
    }

    /**
     * Retrieves a dataset.
     * 
     * GET
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getDataset(final String datasetId) {
        return dataset.get(datasetId);
    }

    /**
     * Returns the ids of the fields that contain errors and their number.
     *
     * @param datasetId the dataset id of the dataset to be inspected
     */
    public Map<String, Long> getErrorCounts(final String datasetId) {
        return dataset.getErrorCounts(getDataset(datasetId));
    }

    /**
     * Returns the ids of the fields that contain errors and their number.
     *
     * @param datasetJSON the dataset JSON object to be inspected
     */
    public Map<String, Long> getErrorCounts(final JSONObject datasetJSON) {
        return dataset.getErrorCounts(datasetJSON);
    }

    /**
     * Retrieves a dataset.
     * 
     * GET
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param datasetJSON
     *            a dataset JSONObject
     * 
     */
    public JSONObject getDataset(final JSONObject datasetJSON) {
        return dataset.get(datasetJSON);
    }

    /**
     * Check whether a dataset's status is FINISHED.
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public boolean datasetIsReady(final String datasetId) {
        return dataset.isReady(datasetId);
    }

    /**
     * Checks whether a dataset's status is FINISHED.
     * 
     * @param datasetJSON
     *            a dataset JSONObject
     * 
     */
    public boolean datasetIsReady(final JSONObject datasetJSON) {
        return dataset.isReady(datasetJSON);
    }

    /**
     * Lists all your datasources.
     * 
     * GET /andromeda/dataset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    public JSONObject listDatasets(final String queryString) {
        return dataset.list(queryString);
    }

    /**
     * Updates a dataset.
     * 
     * PUT
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    public JSONObject updateDataset(final String datasetId, final String changes) {
        return dataset.update(datasetId, changes);
    }

    /**
     * Updates a dataset.
     * 
     * PUT
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetJSON
     *            a dataset JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    public JSONObject updateDataset(final JSONObject datasetJSON,
            final JSONObject changes) {
        return dataset.update(datasetJSON, changes);
    }

    /**
     * Deletes a dataset.
     * 
     * DELETE
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject deleteDataset(final String datasetId) {
        return dataset.delete(datasetId);
    }

    /**
     * Deletes a dataset.
     * 
     * DELETE
     * /andromeda/dataset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param datasetJSON
     *            a dataset JSONObject
     * 
     */
    public JSONObject deleteDataset(final JSONObject datasetJSON) {
        return dataset.delete(datasetJSON);
    }

    /**
     * Retrieves the dataset file.
     *
     * Downloads dataset, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadDataset(final String datasetId,
                                                final String filename) {
        return dataset.downloadDataset(datasetId, filename);
    }


    // ################################################################
    // #
    // # Models
    // # https://bigml.com/developers/models
    // #
    // ################################################################

    /**
     * Creates a new model.
     * 
     * POST /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars for the dataset to attach the model.
     * @param args
     *            set of parameters for the new model. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for source
     *            before to start to create the model. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createModel(final String datasetId, String args,
            Integer waitTime, Integer retries) {
        return model.create(datasetId, args, waitTime, retries);
    }

    /**
     * Creates a new model.
     * 
     * POST /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars for the dataset to attach the model.
     * @param args
     *            set of parameters for the new model. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for source
     *            before to start to create the model. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createModel(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("seed") ) {
                args.put("seed", seed);
            }
        }

        return model.create(datasetId, args, waitTime, retries);
    }

    /**
     * Creates a mdel from a list of `datasets`.
     * 
     * POST /andromeda/mdel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            mdel.
     * @param args
     *            set of parameters for the new mdel. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the mdel. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createModel(final List datasetsIds, String args,
            Integer waitTime, Integer retries) {
        return model.create(datasetsIds, args, waitTime, retries);
    }

    /**
     * Creates a mdel from a list of `datasets`.
     * 
     * POST /andromeda/mdel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            mdel.
     * @param args
     *            set of parameters for the new mdel. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the mdel. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createModel(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("seed") ) {
                args.put("seed", seed);
            }
        }

        return model.create(datasetsIds, args, waitTime, retries);
    }

    /**
     * Retrieves a model.
     * 
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getModel(final String modelId) {
        return getModel(modelId, null, null);
    }

    /**
     * Retrieves a model.
     * 
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     * 
     */
    public JSONObject getModel(final String modelId, final String apiUser,
            final String apiKey) {
        return model.get(modelId, apiUser, apiKey);
    }

    /**
     * Retrieves a public model.
     * 
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getPublicModel(final String modelId) {
        return getPublicModel(modelId, null, null);
    }

    /**
     * Retrieves a public model.
     * 
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     * 
     */
    public JSONObject getPublicModel(final String modelId,
            final String apiUser, final String apiKey) {
        return model.get("public/" + modelId, apiUser, apiKey);
    }

    /**
     * Retrieves a model.
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelJSON
     *            a model JSONObject
     * 
     */
    public JSONObject getModel(final JSONObject modelJSON) {
        return getModel(modelJSON, null, null);
    }

    /**
     * Retrieves a model.
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelJSON
     *            a model JSONObject
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     * 
     */
    public JSONObject getModel(final JSONObject modelJSON,
            final String apiUser, final String apiKey) {
        return model.get(modelJSON, apiUser, apiKey);
    }

    /**
     * Retrieves a model.
     * 
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * 
     */
    public JSONObject getModel(final String modelId, final String queryString) {
        return getModel(modelId, queryString, null, null);
    }

    /**
     * Retrieves a model.
     * 
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     * 
     */
    public JSONObject getModel(final String modelId, final String queryString,
            final String apiUser, final String apiKey) {
        return model.get(modelId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves a public model.
     * 
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * 
     */
    public JSONObject getPublicModel(final String modelId,
            final String queryString) {
        return getPublicModel(modelId, queryString, null, null);
    }

    /**
     * Retrieves a public model.
     * 
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     * 
     */
    public JSONObject getPublicModel(final String modelId,
            final String queryString, final String apiUser, final String apiKey) {
        return model.get("public/" + modelId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves a model.
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelJSON
     *            a model JSONObject
     * @param queryString
     *            query for filtering.
     * 
     */
    public JSONObject getModel(final JSONObject modelJSON,
            final String queryString) {
        return getModel(modelJSON, queryString, null, null);
    }

    /**
     * Retrieves a model.
     * 
     * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param modelJSON
     *            a model JSONObject
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     * 
     */
    public JSONObject getModel(final JSONObject modelJSON,
            final String queryString, final String apiUser, final String apiKey) {
        return model.get(modelJSON, queryString, apiUser, apiKey);
    }

    /**
     * Checks whether a model's status is FINISHED.
     * 
     * @param modelId
     *            modelId a unique identifier in the form model/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public boolean modelIsReady(final String modelId) {
        return model.isReady(modelId);
    }

    /**
     * Checks whether a model's status is FINISHED.
     * 
     * @param modelJSON
     *            a model JSONObject
     * 
     */
    public boolean modelIsReady(final JSONObject modelJSON) {
        return model.isReady(modelJSON);
    }

    /**
     * Lists all your models.
     * 
     * GET /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    public JSONObject listModels(final String queryString) {
        return model.list(queryString);
    }

    /**
     * Updates a model.
     * 
     * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    public JSONObject updateModel(final String modelId, final String changes) {
        return model.update(modelId, changes);
    }

    /**
     * Updates a model.
     * 
     * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param modelJSON
     *            modelJSON a model JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    public JSONObject updateModel(final JSONObject modelJSON,
            final JSONObject changes) {
        return model.update(modelJSON, changes);
    }

    /**
     * Deletes a model.
     * 
     * DELETE
     * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param modelId
     *            a unique identifier in the form model/id where id is a string
     *            of 24 alpha-numeric chars.
     * 
     */
    public JSONObject deleteModel(final String modelId) {
        return model.delete(modelId);
    }

    /**
     * Deletes a model.
     * 
     * DELETE
     * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param modelJSON
     *            a model JSONObject
     * 
     */
    public JSONObject deleteModel(final JSONObject modelJSON) {
        return model.delete(modelJSON);
    }

    // ################################################################
    // #
    // # Anomalies
    // # https://bigml.com/developers/anomalies
    // #
    // ################################################################

    /**
     * Creates a new anomaly.
     *
     * POST /andromeda/anomaly?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form datset/id where id is a string
     *            of 24 alpha-numeric chars for the dataset to attach the anomaly.
     * @param args
     *            set of parameters for the new anomaly. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for source
     *            before to start to create the anomaly. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createAnomaly(final String datasetId, JSONObject args,
                                  Integer waitTime, Integer retries) {

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("seed") ) {
                args.put("seed", seed);
            }
        }

        return anomaly.create(datasetId, args, waitTime, retries);
    }

    /**
     * Creates an anomaly from a list of `datasets`.
     *
     * POST /andromeda/anomaly?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            anomaly.
     * @param args
     *            set of parameters for the new anomaly. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the anomaly. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createAnomaly(final List datasetsIds, JSONObject args,
                                  Integer waitTime, Integer retries) {

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("seed") ) {
                args.put("seed", seed);
            }
        }

        return anomaly.create(datasetsIds, args, waitTime, retries);
    }

    /**
     * Retrieves an anomaly.
     *
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    public JSONObject getAnomaly(final String anomalyId) {
        return getAnomaly(anomalyId, null, null);
    }

    /**
     * Retrieves an anomaly.
     *
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject getAnomaly(final String anomalyId, final String apiUser,
                               final String apiKey) {
        return anomaly.get(anomalyId, apiUser, apiKey);
    }

    /**
     * Retrieves a public anomaly.
     *
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    public JSONObject getPublicAnomaly(final String anomalyId) {
        return getPublicModel(anomalyId, null, null);
    }

    /**
     * Retrieves a public anomaly.
     *
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject getPublicAnomaly(final String anomalyId,
                                     final String apiUser, final String apiKey) {
        return anomaly.get("public/" + anomalyId, apiUser, apiKey);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyJSON
     *            an anomaly JSONObject
     *
     */
    public JSONObject getAnomaly(final JSONObject anomalyJSON) {
        return getAnomaly(anomalyJSON, null, null);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyJSON
     *            an anomaly JSONObject
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject getAnomaly(final JSONObject anomalyJSON,
                               final String apiUser, final String apiKey) {
        return anomaly.get(anomalyJSON, apiUser, apiKey);
    }

    /**
     * Retrieves an anomaly.
     *
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject getAnomaly(final String anomalyId, final String queryString) {
        return getAnomaly(anomalyId, queryString, null, null);
    }

    /**
     * Retrieves an anomaly.
     *
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject getAnomaly(final String anomalyId, final String queryString,
                               final String apiUser, final String apiKey) {
        return anomaly.get(anomalyId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves a public anomaly.
     *
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject getPublicAnomaly(final String anomalyId,
                                     final String queryString) {
        return getPublicAnomaly(anomalyId, queryString, null, null);
    }

    /**
     * Retrieves a public anomaly.
     *
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject getPublicAnomaly(final String anomalyId,
                                     final String queryString, final String apiUser, final String apiKey) {
        return anomaly.get("public/" + anomalyId, queryString, apiUser, apiKey);
    }

    /**
     * Retrieves a anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyJSON
     *            an anomaly JSONObject
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject getAnomaly(final JSONObject anomalyJSON,
                               final String queryString) {
        return getAnomaly(anomalyJSON, queryString, null, null);
    }

    /**
     * Retrieves an anomaly.
     *
     * GET /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param anomalyJSON
     *            a anomaly JSONObject
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject getAnomaly(final JSONObject anomalyJSON,
                               final String queryString, final String apiUser, final String apiKey) {
        return anomaly.get(anomalyJSON, queryString, apiUser, apiKey);
    }

    /**
     * Checks whether an anomaly's status is FINISHED.
     *
     * @param anomalyId
     *            anomalyId a unique identifier in the form anomaly/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean anomalyIsReady(final String anomalyId) {
        return anomaly.isReady(anomalyId);
    }

    /**
     * Checks whether a anomaly's status is FINISHED.
     *
     * @param anomalyJSON
     *            an anomaly JSONObject
     *
     */
    public boolean anomalyIsReady(final JSONObject anomalyJSON) {
        return anomaly.isReady(anomalyJSON);
    }

    /**
     * Lists all your anomalies.
     *
     * GET /andromeda/anomaly?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listAnomalies(final String queryString) {
        return anomaly.list(queryString);
    }

    /**
     * Updates a anomaly.
     *
     * PUT /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    public JSONObject updateAnomaly(final String anomalyId, final String changes) {
        return anomaly.update(anomalyId, changes);
    }

    /**
     * Updates an anomaly.
     *
     * PUT /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomalyJSON
     *            anomalyJSON a model JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    public JSONObject updateAnomaly(final JSONObject anomalyJSON,
                                  final JSONObject changes) {
        return anomaly.update(anomalyJSON, changes);
    }

    /**
     * Deletes an anomaly.
     *
     * DELETE
     * /andromeda/anomaly/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteAnomaly(final String anomalyId) {
        return anomaly.delete(anomalyId);
    }

    /**
     * Deletes an anomaly.
     *
     * DELETE
     * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param anomalyJSON
     *            an anomaly JSONObject
     *
     */
    public JSONObject deleteAnomaly(final JSONObject anomalyJSON) {
        return anomaly.delete(anomalyJSON);
    }


    // ################################################################
    // #
    // # Predictions
    // # https://bigml.com/developers/predictions
    // #
    // ################################################################

    /**
     * Creates a new prediction.
     * 
     * POST
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param modelOrEnsembleId
     *            a unique identifier in the form model/id or ensembke/id where
     *            id is a string of 24 alpha-numeric chars for the nodel or
     *            ensemble to attach the prediction.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a prediction for.
     * @param byName
     * @param args
     *            set of parameters for the new prediction. Required
     * @param waitTime
     *            time to wait for next check of FINISHED status for model
     *            before to start to create the prediction. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createPrediction(final String modelOrEnsembleId,
            JSONObject inputData, Boolean byName, String args,
            Integer waitTime, Integer retries) {
        return prediction.create(modelOrEnsembleId, inputData, byName, args,
                waitTime, retries);
    }

    /**
     * Creates a new prediction.
     * 
     * POST
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param modelOrEnsembleId
     *            a unique identifier in the form model/id or ensembke/id where
     *            id is a string of 24 alpha-numeric chars for the nodel or
     *            ensemble to attach the prediction.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a prediction for.
     * @param byName
     * @param args
     *            set of parameters for the new prediction. Required
     * @param waitTime
     *            time to wait for next check of FINISHED status for model
     *            before to start to create the prediction. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createPrediction(final String modelOrEnsembleId,
            JSONObject inputData, Boolean byName, JSONObject args,
            Integer waitTime, Integer retries) {
        return prediction.create(modelOrEnsembleId, inputData, byName, args,
                waitTime, retries);
    }

    /**
     * Retrieves a prediction.
     * 
     * GET /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param predictionId
     *            a unique identifier in the form prediction/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getPrediction(final String predictionId) {
        return prediction.get(predictionId);
    }

    /**
     * Retrieves a prediction.
     * 
     * GET /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param predictionJSON
     *            a prediction JSONObject
     * 
     */
    public JSONObject getPrediction(final JSONObject predictionJSON) {
        return prediction.get(predictionJSON);
    }

    /**
     * Checks whether a prediction's status is FINISHED.
     * 
     * @param predictionId
     *            a unique identifier in the form prediction/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public boolean predictionIsReady(final String predictionId) {
        return prediction.isReady(predictionId);
    }

    /**
     * Checks whether a prediction's status is FINISHED.
     * 
     * @param predictionJSON
     *            a prediction JSONObject
     * 
     */
    public boolean predictionIsReady(final JSONObject predictionJSON) {
        return prediction.isReady(predictionJSON);
    }

    /**
     * Lists all your predictions.
     * 
     * GET
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    public JSONObject listPredictions(final String queryString) {
        return prediction.list(queryString);
    }

    /**
     * Updates a prediction.
     * 
     * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param predictionId
     *            a unique identifier in the form prediction/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    public JSONObject updatePrediction(final String predictionId,
            final String changes) {
        return prediction.update(predictionId, changes);
    }

    /**
     * Updates a prediction.
     * 
     * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param predictionJSON
     *            a prediction JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     * 
     */
    public JSONObject updatePrediction(final JSONObject predictionJSON,
            final JSONObject changes) {
        return prediction.update(predictionJSON, changes);
    }

    /**
     * Deletes a prediction.
     * 
     * DELETE /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param predictionId
     *            a unique identifier in the form prediction/id where id is a
     *            string of 24 alpha-numeric chars
     * 
     */
    public JSONObject deletePrediction(final String predictionId) {
        return prediction.delete(predictionId);
    }

    /**
     * Deletes a prediction.
     * 
     * DELETE /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param predictionJSON
     *            a prediction JSONObject
     * 
     */
    public JSONObject deletePrediction(final JSONObject predictionJSON) {
        return prediction.delete(predictionJSON);
    }

    // ################################################################
    // #
    // # Anomaly scores
    // # https://bigml.com/developers/anomalyscores
    // #
    // ################################################################

    /**
     * Creates a new anomaly score.
     *
     * POST
     * /andromeda/anomalyscore?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where
     *            id is a string of 24 alpha-numeric chars for the anomaly
     *            to attach the anomaly score.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create an anomaly score for.
     * @param byName
     *
     * @param args
     *            set of parameters for the new anomaly score. Required
     * @param waitTime
     *            time to wait for next check of FINISHED status for anomaly
     *            before to start to create the anomaly score. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createAnomalyScore(final String anomalyId,
            JSONObject inputData, Boolean byName, JSONObject args,
            Integer waitTime, Integer retries) {
        return anomalyScore.create(anomalyId, inputData, byName, args,
                waitTime, retries);
    }

    /**
     * Creates a new anomaly score.
     *
     * POST
     * /andromeda/anomalyscore?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomaly
     *            a anomaly JSONObject
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create an anomaly score for.
     * @param byName
     *
     * @param args
     *            set of parameters for the new anomaly score. Required
     * @param waitTime
     *            time to wait for next check of FINISHED status for anomaly
     *            before to start to create the anomaly score. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createAnomalyScore(final JSONObject anomaly,
            JSONObject inputData, Boolean byName, JSONObject args,
            Integer waitTime, Integer retries) {
        String anomalyId = (String) anomaly.get("resource");
        return  createAnomalyScore(anomalyId, inputData, byName, args,
                waitTime, retries);
    }

    /**
     * Retrieves an anomaly score.
     *
     * GET /andromeda/anomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param anomalyScoreId
     *            a unique identifier in the form anomalyScore/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getAnomalyScore(final String anomalyScoreId) {
        return anomalyScore.get(anomalyScoreId);
    }

    /**
     * Retrieves an anomaly score.
     *
     * GET /andromeda/anomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param anomalyScoreJSON
     *            a anomaly score JSONObject
     *
     */
    public JSONObject getAnomalyScore(final JSONObject anomalyScoreJSON) {
        return anomalyScore.get(anomalyScoreJSON);
    }

    /**
     * Retrieves the anomaly score file.
     *
     * Downloads scores, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param anomalyScoreId
     *            a unique identifier in the form anomalyScore/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadAnomalyScore(
            final String anomalyScoreId, final String filename) {
        return anomalyScore.downloadAnomalyScore(anomalyScoreId,
                filename);
    }

    /**
     * Retrieves the anomaly score file.
     *
     * Downloads scores, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param anomalyScoreJSON
     *            an anomaly score JSONObject.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadAnomalyScore(
            final JSONObject anomalyScoreJSON, final String filename) {
        return anomalyScore.downloadAnomalyScore(anomalyScoreJSON,
                filename);
    }

    /**
     * Checks whether an anomaly score's status is FINISHED.
     *
     * @param anomalyScoreId
     *            a unique identifier in the form anomalyScore/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean anomalyScoreIsReady(final String anomalyScoreId) {
        return anomalyScore.isReady(anomalyScoreId);
    }

    /**
     * Checks whether an anomaly score's status is FINISHED.
     *
     * @param anomalyScoreJSON
     *            an anomaly score JSONObject
     *
     */
    public boolean anomalyScoreIsReady(final JSONObject anomalyScoreJSON) {
        return anomalyScore.isReady(anomalyScoreJSON);
    }

    /**
     * Lists all your anomaly scores.
     *
     * GET
     * /andromeda/anomalyscore?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listAnomalyScores(final String queryString) {
        return anomalyScore.list(queryString);
    }

    /**
     * Updates an anomaly score.
     *
     * PUT /andromeda/anomalyscore/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomalyScoreId
     *            a unique identifier in the form anomalyScore/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    public JSONObject updateAnomalyScore(final String anomalyScoreId,
            final String changes) {
        return anomalyScore.update(anomalyScoreId, changes);
    }

    /**
     * Updates an anomaly score.
     *
     * PUT /andromeda/anomalyscore/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomalyScoreJSON
     *            am anomaly score JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    public JSONObject updateAnomalyScore(final JSONObject anomalyScoreJSON,
            final JSONObject changes) {
        return anomalyScore.update(anomalyScoreJSON, changes);
    }

    /**
     * Deletes an anomaly score.
     *
     * DELETE /andromeda/anomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param anomalyScoreId
     *            a unique identifier in the form anomalyScore/id where id is a
     *            string of 24 alpha-numeric chars
     *
     */
    public JSONObject deleteAnomalyScore(final String anomalyScoreId) {
        return anomalyScore.delete(anomalyScoreId);
    }

    /**
     * Deletes an anomaly score.
     *
     * DELETE /andromeda/anomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param anomalyScoreJSON
     *            an anomaly score JSONObject
     *
     */
    public JSONObject deleteAnomalyScore(final JSONObject anomalyScoreJSON) {
        return anomalyScore.delete(anomalyScoreJSON);
    }

    // ################################################################
    // #
    // # Evaluations
    // # https://bigml.com/developers/evaluations
    // #
    // ################################################################

    /**
     * Creates a new evaluation.
     * 
     * POST
     * /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param modelOrEnsembleId
     *            a unique identifier in the form model/id or ensemble/id where
     *            id is a string of 24 alpha-numeric chars for the
     *            model/ensemble to attach the evaluation.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new evaluation. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for model
     *            before to start to create the evaluation. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createEvaluation(final String modelOrEnsembleId,
            final String datasetId, String args, Integer waitTime, Integer retries) {
        return evaluation.create(modelOrEnsembleId, datasetId, args, waitTime,
                retries);
    }

    /**
     * Creates a new evaluation.
     * 
     * POST
     * /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param modelOrEnsembleId
     *            a unique identifier in the form model/id or ensemble/id where
     *            id is a string of 24 alpha-numeric chars for the
     *            model/ensemble to attach the evaluation.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new evaluation. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for model
     *            before to start to create the evaluation. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createEvaluation(final String modelOrEnsembleId,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("seed") ) {
                args.put("seed", seed);
            }
        }

        return evaluation.create(modelOrEnsembleId, datasetId, args, waitTime,
                retries);
    }

    /**
     * Retrieves an evaluation.
     * 
     * An evaluation is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the evaluation values and state info available at the time it is
     * called.
     * 
     * GET /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param evaluationId
     *            a unique identifier in the form evaluation/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getEvaluation(final String evaluationId) {
        return evaluation.get(evaluationId);
    }

    /**
     * Retrieves an evaluation.
     * 
     * An evaluation is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the evaluation values and state info available at the time it is
     * called.
     * 
     * GET /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param evaluationJSON
     *            an evaluation JSONObject.
     * 
     */
    public JSONObject getEvaluation(final JSONObject evaluationJSON) {
        return evaluation.get(evaluationJSON);
    }

    /**
     * Check whether a evaluation' status is FINISHED.
     * 
     * @param evaluationId
     *            a unique identifier in the form evaluation/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public boolean evaluationIsReady(final String evaluationId) {
        return evaluation.isReady(evaluationId);
    }

    /**
     * Check whether a evaluation' status is FINISHED.
     * 
     * @param evaluationJSON
     *            an evaluation JSONObject.
     * 
     */
    public boolean evaluationIsReady(final JSONObject evaluationJSON) {
        return evaluation.isReady(evaluationJSON);
    }

    /**
     * Lists all your evaluations.
     * 
     * GET
     * /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    public JSONObject listEvaluations(final String queryString) {
        return evaluation.list(queryString);
    }

    /**
     * Updates an evaluation.
     * 
     * PUT /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param evaluationId
     *            a unique identifier in the form evauation/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the evaluation. Optional
     * 
     */
    public JSONObject updateEvaluation(final String evaluationId,
            final String changes) {
        return evaluation.update(evaluationId, changes);
    }

    /**
     * Updates an evaluation.
     * 
     * PUT /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param evaluationJSON
     *            an evaluation JSONObject
     * @param changes
     *            set of parameters to update the evaluation. Optional
     */
    public JSONObject updateEvaluation(final JSONObject evaluationJSON,
            final JSONObject changes) {
        return evaluation.update(evaluationJSON, changes);
    }

    /**
     * Deletes an evaluation.
     * 
     * DELETE /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param evaluationId
     *            a unique identifier in the form evaluation/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject deleteEvaluation(final String evaluationId) {
        return evaluation.delete(evaluationId);
    }

    /**
     * Deletes an evaluation.
     * 
     * DELETE /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param evaluationJSON
     *            an evaluation JSONObject.
     * 
     */
    public JSONObject deleteEvaluation(final JSONObject evaluationJSON) {
        return evaluation.delete(evaluationJSON);
    }

    // ################################################################
    // #
    // # Ensembles
    // # https://bigml.com/developers/ensembles
    // #
    // ################################################################

    /**
     * Creates a new ensemble.
     * 
     * POST /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new ensemble. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the ensemble. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createEnsemble(final String datasetId, String args,
            Integer waitTime, Integer retries) {
        return ensemble.create(datasetId, args, waitTime, retries);
    }

    /**
     * Creates a new ensemble.
     * 
     * POST /andromeda/ensemble?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new ensemble. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the ensemble. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createEnsemble(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("seed") ) {
                args.put("seed", seed);
            }
        }

        return ensemble.create(datasetId, args, waitTime, retries);
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
    public JSONObject createEnsemble(final List datasetsIds, String args,
            Integer waitTime, Integer retries) {
        return ensemble.create(datasetsIds, args, waitTime, retries);
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
    public JSONObject createEnsemble(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("seed") ) {
                args.put("seed", seed);
            }
        }

        return ensemble.create(datasetsIds, args, waitTime, retries);
    }

    /**
     * Retrieves an ensemble.
     * 
     * An ensemble is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the ensemble values and state info available at the time it is
     * called.
     * 
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param ensembleId
     *            a unique identifier in the form ensemble/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getEnsemble(final String ensembleId) {
        return ensemble.get(ensembleId);
    }

    /**
     * Retrieves an ensemble.
     * 
     * An ensemble is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the ensemble values and state info available at the time it is
     * called.
     * 
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param ensembleJSON
     *            an ensemble JSONObject.
     * 
     */
    public JSONObject getEnsemble(final JSONObject ensembleJSON) {
        return ensemble.get(ensembleJSON);
    }

    /**
     * Check whether a ensemble's status is FINISHED.
     * 
     * @param ensembleId
     *            a unique identifier in the form ensemble/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public boolean ensembleIsReady(final String ensembleId) {
        return ensemble.isReady(ensembleId);
    }

    /**
     * Check whether a ensemble's status is FINISHED.
     * 
     * @param ensembleJSON
     *            an ensemble JSONObject.
     * 
     */
    public boolean ensembleIsReady(final JSONObject ensembleJSON) {
        return ensemble.isReady(ensembleJSON);
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
    public JSONObject listEnsembles(final String queryString) {
        return ensemble.list(queryString);
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
    public JSONObject updateEnsemble(final String ensembleId,
            final String changes) {
        return ensemble.update(ensembleId, changes);
    }

    /**
     * Updates an ensemble.
     * 
     * PUT
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param ensembleJSON
     *            an ensemble JSONObject
     * @param changes
     *            set of parameters to update the ensemble. Optional
     */
    public JSONObject updateEnsemble(final JSONObject ensembleJSON,
            final JSONObject changes) {
        return ensemble.update(ensembleJSON, changes);
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
    public JSONObject deleteEnsemble(final String ensembleId) {
        return ensemble.delete(ensembleId);
    }

    /**
     * Deletes an ensemble.
     * 
     * DELETE
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param ensembleJSON
     *            an ensemble JSONObject.
     * 
     */
    public JSONObject deleteEnsemble(final JSONObject ensembleJSON) {
        return ensemble.delete(ensembleJSON);
    }

    // ################################################################
    // #
    // # Batch predictions
    // # https://bigml.com/developers/batch_predictions
    // #
    // ################################################################

    /**
     * Creates a new batch prediction.
     * 
     * POST /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param modelOrEnsembleId
     *            a unique identifier in the form model/id or ensemble/id where
     *            id is a string of 24 alpha-numeric chars for the
     *            model/ensemble to attach the evaluation.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new batch prediction. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the batch prediction.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createBatchPrediction(final String modelOrEnsembleId,
            final String datasetId, String args, Integer waitTime,
            Integer retries) {
        return batchPrediction.create(modelOrEnsembleId, datasetId, args,
                waitTime, retries);
    }

    /**
     * Creates a new batch prediction.
     * 
     * POST /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param modelOrEnsembleId
     *            a unique identifier in the form model/id or ensemble/id where
     *            id is a string of 24 alpha-numeric chars for the
     *            model/ensemble to attach the evaluation.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new batch prediction. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the batch prediction.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createBatchPrediction(final String modelOrEnsembleId,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {
        return batchPrediction.create(modelOrEnsembleId, datasetId, args,
                waitTime, retries);
    }

    /**
     * Retrieves a batch prediction.
     * 
     * The batch_prediction parameter should be a string containing the
     * batch_prediction id or the dict returned by create_batch_prediction. As
     * batch_prediction is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the function will return a dict that
     * encloses the batch_prediction values and state info available at the time
     * it is called.
     * 
     * GET /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param batchPredictionId
     *            a unique identifier in the form batchPrediction/id where id is
     *            a string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getBatchPrediction(final String batchPredictionId) {
        return batchPrediction.get(batchPredictionId);
    }

    /**
     * Retrieves a batch prediction.
     * 
     * The batch_prediction parameter should be a string containing the
     * batch_prediction id or the dict returned by create_batch_prediction. As
     * batch_prediction is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the function will return a dict that
     * encloses the batch_prediction values and state info available at the time
     * it is called.
     * 
     * GET /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param batchPredictionJSON
     *            a batch prediction JSONObject.
     * 
     */
    public JSONObject getBatchPrediction(final JSONObject batchPredictionJSON) {
        return batchPrediction.get(batchPredictionJSON);
    }

    /**
     * Retrieves the batch predictions file.
     * 
     * Downloads predictions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     * 
     * @param batchPredictionId
     *            a unique identifier in the form batchPrediction/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     * 
     */
    public JSONObject downloadBatchPrediction(final String batchPredictionId,
            final String filename) {
        return batchPrediction.downloadBatchPrediction(batchPredictionId,
                filename);
    }

    /**
     * Retrieves the batch predictions file.
     * 
     * Downloads predictions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     * 
     * @param batchPredictionJSON
     *            a batch prediction JSONObject.
     * @param filename
     *            Path to save file locally
     * 
     */
    public JSONObject downloadBatchPrediction(
            final JSONObject batchPredictionJSON, final String filename) {
        return batchPrediction.downloadBatchPrediction(batchPredictionJSON,
                filename);
    }

    /**
     * Check whether a batch prediction's status is FINISHED.
     * 
     * @param batchPredictionId
     *            a unique identifier in the form batchPrediction/id where id is
     *            a string of 24 alpha-numeric chars.
     * 
     */
    public boolean batchPredictionIsReady(final String batchPredictionId) {
        return batchPrediction.isReady(batchPredictionId);
    }

    /**
     * Check whether a batch prediction's status is FINISHED.
     * 
     * @param batchPredictionJSON
     *            a batchPrediction JSONObject.
     * 
     */
    public boolean batchPredictionIsReady(final JSONObject batchPredictionJSON) {
        return batchPrediction.isReady(batchPredictionJSON);
    }

    /**
     * Lists all your batch predictions.
     * 
     * GET /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    public JSONObject listBatchPredictions(final String queryString) {
        return batchPrediction.list(queryString);
    }

    /**
     * Updates a batch prediction.
     * 
     * PUT /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param batchPredictionId
     *            a unique identifier in the form batchPrediction/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the batch prediction. Optional
     * 
     */
    public JSONObject updateBatchPrediction(final String batchPredictionId,
            final String changes) {
        return batchPrediction.update(batchPredictionId, changes);
    }

    /**
     * Updates a batch prediction.
     * 
     * PUT /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param batchpredictionJSON
     *            a batch prediction JSONObject
     * @param changes
     *            set of parameters to update the batch prediction. Optional
     */
    public JSONObject updateBatchPrediction(
            final JSONObject batchpredictionJSON, final JSONObject changes) {
        return batchPrediction.update(batchpredictionJSON, changes);
    }

    /**
     * Deletes a batch prediction.
     * 
     * DELETE /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param batchPredictionId
     *            a unique identifier in the form batchPrediction/id where id is
     *            a string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject deleteBatchPrediction(final String batchPredictionId) {
        return batchPrediction.delete(batchPredictionId);
    }

    /**
     * Deletes a batch prediction.
     * 
     * DELETE /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param batchPredictionJSON
     *            a batch prediction JSONObject.
     * 
     */
    public JSONObject deleteBatchPrediction(final JSONObject batchPredictionJSON) {
        return batchPrediction.delete(batchPredictionJSON);
    }

    // ################################################################
    // #
    // # Batch anomaly scores
    // # https://bigml.com/developers/batch_anomalyscore
    // #
    // ################################################################

    /**
     * Creates a new batch anomaly score.
     *
     * POST /andromeda/batchanomalyscore?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param anomalyId
     *            a unique identifier in the form anomaly/id where
     *            id is a string of 24 alpha-numeric chars for the
     *            anomaly to attach the evaluation.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            batch anomaly score.
     * @param args
     *            set of parameters for the new batch prediction. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the batch prediction.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createBatchAnomalyScore(final String anomalyId,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {
        return batchAnomalyScore.create(anomalyId, datasetId, args,
                waitTime, retries);
    }

    /**
     * Retrieves a batch anomaly score.
     *
     * The batch_anomalyscore parameter should be a string containing the
     * batch_anomalyscore id or the dict returned by create_batch_anomalyscore. As
     * batch_anomalyscore is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the function will return a dict that
     * encloses the batch_anomalyscore values and state info available at the time
     * it is called.
     *
     * GET /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getBatchAnomalyScore(final String batchAnomalyScoreId) {
        return batchAnomalyScore.get(batchAnomalyScoreId);
    }

    /**
     * Retrieves a batch anomaly score.
     *
     * The batch_anomalyscore parameter should be a string containing the
     * batch_anomalyscore id or the dict returned by create_batch_anomalyscore. As
     * batch_anomalyscore is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the function will return a dict that
     * encloses the batch_anomalyscore values and state info available at the time
     * it is called.
     *
     * GET /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param batchAnomalyScoreJSON
     *            a batch anomaly score JSONObject.
     *
     */
    public JSONObject getBatchAnomalyScore(final JSONObject batchAnomalyScoreJSON) {
        return batchAnomalyScore.get(batchAnomalyScoreJSON);
    }

    /**
     * Retrieves the batch anomaly score file.
     *
     * Downloads scores, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchAnomalyScore(final String batchAnomalyScoreId,
            final String filename) {
        return batchAnomalyScore.downloadBatchAnomalyScore(batchAnomalyScoreId,
                filename);
    }

    /**
     * Retrieves the batch anomaly score file.
     *
     * Downloads scores, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchAnomalyScoreJSON
     *            a batch anomaly score JSONObject.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchAnomalyScore(
            final JSONObject batchAnomalyScoreJSON, final String filename) {
        return batchAnomalyScore.downloadBatchAnomalyScore(batchAnomalyScoreJSON,
                filename);
    }

    /**
     * Check whether a batch anomaly score's status is FINISHED.
     *
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public boolean batchAnomalyScoreIsReady(final String batchAnomalyScoreId) {
        return batchAnomalyScore.isReady(batchAnomalyScoreId);
    }

    /**
     * Check whether a batch anomaly score's status is FINISHED.
     *
     * @param batchAnomalyScoreJSON
     *            a batchanomalyscore JSONObject.
     *
     */
    public boolean batchAnomalyScoreIsReady(final JSONObject batchAnomalyScoreJSON) {
        return batchAnomalyScore.isReady(batchAnomalyScoreJSON);
    }

    /**
     * Lists all your batch anomaly scores.
     *
     * GET /andromeda/batchanomalyscore?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listBatchAnomalyScores(final String queryString) {
        return batchAnomalyScore.list(queryString);
    }

    /**
     * Updates a batch anomaly score.
     *
     * PUT /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the batch anomaly score. Optional
     *
     */
    public JSONObject updateBatchAnomalyScore(final String batchAnomalyScoreId,
            final String changes) {
        return batchAnomalyScore.update(batchAnomalyScoreId, changes);
    }

    /**
     * Updates a batch anomaly score.
     *
     * PUT /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchAnomalyScoreJSON
     *            a batch anomaly score JSONObject
     * @param changes
     *            set of parameters to update the batch anomaly score. Optional
     */
    public JSONObject updateBatchAnomalyScore(
            final JSONObject batchAnomalyScoreJSON, final JSONObject changes) {
        return batchAnomalyScore.update(batchAnomalyScoreJSON, changes);
    }

    /**
     * Deletes a batch anomaly score.
     *
     * DELETE /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchAnomalyScoreId
     *            a unique identifier in the form batchanomalyscore/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteBatchAnomalyScore(final String batchAnomalyScoreId) {
        return batchAnomalyScore.delete(batchAnomalyScoreId);
    }

    /**
     * Deletes a batch anomaly score.
     *
     * DELETE /andromeda/batchanomalyscore/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchAnomalyScoreJSON
     *            a batch anomaly score JSONObject.
     *
     */
    public JSONObject deleteBatchAnomalyScore(final JSONObject batchAnomalyScoreJSON) {
        return batchAnomalyScore.delete(batchAnomalyScoreJSON);
    }

    // ################################################################
    // #
    // # Clusters
    // # https://bigml.com/developers/clusters
    // #
    // ################################################################

    /**
     * Creates a new cluster.
     * 
     * POST /andromeda/cluster?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new ensemble. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the ensemble. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createCluster(final String datasetId, String args,
            Integer waitTime, Integer retries) {
        return cluster.create(datasetId, args, waitTime, retries);
    }

    /**
     * Creates a new cluster.
     * 
     * POST /andromeda/cluster?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            evaluation.
     * @param args
     *            set of parameters for the new ensemble. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the ensemble. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createCluster(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("seed") ) {
                args.put("seed", seed);
            }
            if( !args.containsKey("cluster_seed") ) {
                args.put("cluster_seed", seed);
            }
        }

        return cluster.create(datasetId, args, waitTime, retries);
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
        return cluster.create(datasetsIds, args, waitTime, retries);
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

        // Setting the seed automatically if it was informed during the initialization
        if( seed != null && !seed.equals("") ) {
            if( args == null ) {
                args = new JSONObject();
            }
            if( !args.containsKey("cluster_seed") ) {
                args.put("cluster_seed", seed);
            }
        }

        return cluster.create(datasetsIds, args, waitTime, retries);
    }

    /**
     * Retrieves a cluster.
     * 
     * A cluster is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the cluster values and state info available at the time it is
     * called.
     * 
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getCluster(final String clusterId) {
        return cluster.get(clusterId);
    }

    /**
     * Retrieves an cluster.
     * 
     * A cluster is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the ensemble values and state info available at the time it is
     * called.
     * 
     * GET
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param clusterJSON
     *            an cluster JSONObject.
     * 
     */
    public JSONObject getCluster(final JSONObject clusterJSON) {
        return cluster.get(clusterJSON);
    }

    /**
     * Check whether a cluster's status is FINISHED.
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public boolean clusterIsReady(final String clusterId) {
        return cluster.isReady(clusterId);
    }

    /**
     * Check whether a cluster's status is FINISHED.
     * 
     * @param clusterJSON
     *            an cluster JSONObject.
     * 
     */
    public boolean clusterIsReady(final JSONObject clusterJSON) {
        return cluster.isReady(clusterJSON);
    }

    /**
     * Lists all your clusters.
     * 
     * GET /andromeda/cluster?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    public JSONObject listClusters(final String queryString) {
        return cluster.list(queryString);
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
    public JSONObject updateCluster(final String clusterId, final String changes) {
        return cluster.update(clusterId, changes);
    }

    /**
     * Updates a cluster.
     * 
     * PUT
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param clusterJSON
     *            an cluster JSONObject
     * @param changes
     *            set of parameters to update the cluster. Optional
     */
    public JSONObject updateCluster(final JSONObject clusterJSON,
            final JSONObject changes) {
        return cluster.update(clusterJSON, changes);
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
    public JSONObject deleteCluster(final String clusterId) {
        return cluster.delete(clusterId);
    }

    /**
     * Deletes a cluster.
     * 
     * DELETE
     * /andromeda/cluster/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param clusterJSON
     *            an cluster JSONObject.
     * 
     */
    public JSONObject deleteCluster(final JSONObject clusterJSON) {
        return cluster.delete(clusterJSON);
    }

    // ################################################################
    // #
    // # Centroids
    // # https://bigml.com/developers/centroids
    // #
    // ################################################################

    /**
     * Creates a new centroid.
     * 
     * POST /andromeda/centroid?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars for the cluster.
     * @param args
     *            set of parameters for the new centroid. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for centroid before to start to create the centroid. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createCentroid(final String clusterId,
            JSONObject inputDataJSON, String args, Integer waitTime,
            Integer retries) {
        return centroid.create(clusterId, inputDataJSON, args, waitTime,
                retries);
    }

    /**
     * Creates a new centroid.
     * 
     * POST /andromeda/centroid?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars for the cluster.
     * @param args
     *            set of parameters for the new centroid. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for centroid before to start to create the centroid. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createCentroid(final String clusterId,
            JSONObject inputDataJSON, JSONObject args, Integer waitTime,
            Integer retries) {
        return centroid.create(clusterId, inputDataJSON, args, waitTime,
                retries);
    }

    /**
     * Retrieves a centroid.
     * 
     * A centroid is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the centroid values and state info available at the time it is
     * called.
     * 
     * GET
     * /andromeda/centroid/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param centroidId
     *            a unique identifier in the form centroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getCentroid(final String centroidId) {
        return centroid.get(centroidId);
    }

    /**
     * Retrieves a centroid.
     * 
     * A centroid is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the centroid values and state info available at the time it is
     * called.
     * 
     * GET
     * /andromeda/centroid/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     * 
     * @param centroidJSON
     *            a centroid JSONObject.
     * 
     */
    public JSONObject getCentroid(final JSONObject centroidJSON) {
        return centroid.get(centroidJSON);
    }

    /**
     * Check whether a centroid's status is FINISHED.
     * 
     * @param centroidId
     *            a unique identifier in the form centroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public boolean centroidIsReady(final String centroidId) {
        return centroid.isResourceReady(cluster.get(centroidId));
    }

    /**
     * Check whether a centroid's status is FINISHED.
     * 
     * @param centroidJSON
     *            a centroid JSONObject.
     * 
     */
    public boolean centroidIsReady(final JSONObject centroidJSON) {
        return centroid.isReady(centroidJSON);
    }

    /**
     * Lists all your centroids.
     * 
     * GET /andromeda/centroid?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    public JSONObject listCentroids(final String queryString) {
        return centroid.list(queryString);
    }

    /**
     * Updates a centroid.
     * 
     * PUT
     * /andromeda/centroid/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param centroidId
     *            a unique identifier in the form centroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the centroid. Optional
     * 
     */
    public JSONObject updateCentroid(final String centroidId,
            final String changes) {
        return centroid.update(centroidId, changes);
    }

    /**
     * Updates a centroid.
     * 
     * PUT
     * /andromeda/centroid/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param centroidJSON
     *            an centroid JSONObject
     * @param changes
     *            set of parameters to update the centroid. Optional
     * 
     */
    public JSONObject updateCentroid(final JSONObject centroidJSON,
            final JSONObject changes) {
        return centroid.update(centroidJSON, changes);
    }

    /**
     * Deletes a centroid.
     * 
     * DELETE
     * /andromeda/centroid/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param centroidId
     *            a unique identifier in the form centroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject deleteCentroid(final String centroidId) {
        return centroid.delete(centroidId);
    }

    /**
     * Deletes a centroid.
     * 
     * DELETE
     * /andromeda/centroid/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     * 
     * @param centroidJSON
     *            an centroid JSONObject.
     * 
     */
    public JSONObject deleteCentroid(final JSONObject centroidJSON) {
        return centroid.delete(centroidJSON);
    }

    // ################################################################
    // #
    // # BatchCentroids
    // # https://bigml.com/developers/batch_centroids
    // #
    // ################################################################

    /**
     * Creates a new batch_centroid.
     * 
     * POST /andromeda/batch_centroid?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars for the cluster.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset.
     * @param args
     *            set of parameters for the new batch_centroid. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for batch_centroid before to start to create the
     *            batch_centroid. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    @Deprecated
    public JSONObject createBatchCentroid(final String clusterId,
            final String datasetId, String args, Integer waitTime,
            Integer retries) {
        return batchCentroid.create(clusterId, datasetId, args, waitTime,
                retries);
    }

    /**
     * Creates a new batch_centroid.
     * 
     * POST /andromeda/batch_centroid?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param clusterId
     *            a unique identifier in the form cluster/id where id is a
     *            string of 24 alpha-numeric chars for the cluster.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset.
     * @param args
     *            set of parameters for the new batch_centroid. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for batch_centroid before to start to create the
     *            batch_centroid. Optional
     * @param retries
     *            number of times to try the operation. Optional
     * 
     */
    public JSONObject createBatchCentroid(final String clusterId,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {
        return batchCentroid.create(clusterId, datasetId, args, waitTime,
                retries);
    }

    /**
     * Retrieves a batch_centroid.
     * 
     * A batch_centroid is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the batch_centroid values and state info available at the time
     * it is called.
     * 
     * GET /andromeda/batch_centroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param batchCentroidId
     *            a unique identifier in the form batchcentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject getBatchCentroid(final String batchCentroidId) {
        return batchCentroid.get(batchCentroidId);
    }

    /**
     * Retrieves a batch_centroid.
     * 
     * A batch_centroid is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the batch_centroid values and state info available at the time
     * it is called.
     * 
     * GET /andromeda/batch_centroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     * 
     * @param batchCentroidJSON
     *            a batch_centroid JSONObject.
     * 
     */
    public JSONObject getBatchCentroid(final JSONObject batchCentroidJSON) {
        return batchCentroid.get(batchCentroidJSON);
    }

    /**
     * Retrieves the batch centroid file.
     * 
     * Downloads predictions, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     * 
     * @param batchCentroidId
     *            a unique identifier in the form batchCentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     * 
     */
    public JSONObject downloadBatchCentroid(final String batchCentroidId,
            final String filename) {
        return batchCentroid.downloadBatchCentroid(batchCentroidId, filename);
    }

    /**
     * Retrieves the dataset file.
     *
     * Downloads datasets, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchCentroidJSON
     *            a batch centroid JSONObject.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchCentroid(final JSONObject batchCentroidJSON,
                                            final String filename) {
        return batchCentroid.downloadBatchCentroid(batchCentroidJSON, filename);
    }

    /**
     * Check whether a batch_centroid's status is FINISHED.
     * 
     * @param batchCentroidId
     *            a unique identifier in the form batchcentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public boolean batcCentroidIsReady(final String batchCentroidId) {
        return batchCentroid.isResourceReady(cluster.get(batchCentroidId));
    }

    /**
     * Check whether a batch_centroid's status is FINISHED.
     * 
     * @param batchCentroidJSON
     *            a batch_centroid JSONObject.
     * 
     */
    public boolean batcCentroidIsReady(final JSONObject batchCentroidJSON) {
        return batchCentroid.isReady(batchCentroidJSON);
    }

    /**
     * Lists all your batch_centroid.
     * 
     * GET /andromeda/batch_centroid?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     * 
     * @param queryString
     *            query filtering the listing.
     * 
     */
    public JSONObject listBatchCentroids(final String queryString) {
        return centroid.list(queryString);
    }

    /**
     * Updates a batch_centroid.
     * 
     * PUT /andromeda/batch_centroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param batchCentroidId
     *            a unique identifier in the form batchcentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the centroid. Optional
     * 
     */
    public JSONObject updateBatchCentroid(final String batchCentroidId,
            final String changes) {
        return batchCentroid.update(batchCentroidId, changes);
    }

    /**
     * Updates a batch_centroid.
     * 
     * PUT /andromeda/batch_centroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     * 
     * @param batchCentroidJSON
     *            an batch_centroid JSONObject
     * @param changes
     *            set of parameters to update the batch_centroid. Optional
     * 
     */
    public JSONObject updateBatchCentroid(final JSONObject batchCentroidJSON,
            final JSONObject changes) {
        return batchCentroid.update(batchCentroidJSON, changes);
    }

    /**
     * Deletes a batch_centroid.
     * 
     * DELETE /andromeda/batch_centroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param batchCentroidId
     *            a unique identifier in the form batchCentroid/id where id is a
     *            string of 24 alpha-numeric chars.
     * 
     */
    public JSONObject deleteBatchCentroid(final String batchCentroidId) {
        return batchCentroid.delete(batchCentroidId);
    }

    /**
     * Deletes a batch_centroid.
     * 
     * DELETE /andromeda/batch_centroid/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     * 
     * @param batchCentroidJSON
     *            an batch_centroid JSONObject.
     * 
     */
    public JSONObject deleteBatchCentroid(final JSONObject batchCentroidJSON) {
        return batchCentroid.delete(batchCentroidJSON);
    }

}