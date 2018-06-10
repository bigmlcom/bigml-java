package org.bigml.binding;

import org.bigml.binding.resources.*;
import org.bigml.binding.utils.CacheManager;
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
 * Entry point to create, retrieve, list, update, and delete sources, 
 * datasets, models, predictions, evaluations, ensembles, etc.
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
 * If storage is set to a directory name, the resources obtained in CRU
 * operations will be stored in the given directory.
 */
public class BigMLClient {
    // URLs below don't care about BigML.io version. They will hit last
    // (current)
    // BigML version
    final public static String BIGML_URL = "https://bigml.io/";
    
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
    private Project project;
    private Sample sample;
    private Correlation correlation;
    private StatisticalTest statisticalTest;
    private LogisticRegression logisticRegression;
    private Script script;
    private Execution execution;
    private Library library;
    private Association association;
    private AssociationSet associationSet;
    private TopicModel topicModel;
    private TopicDistribution topicDistribution;
    private BatchTopicDistribution batchTopicDistribution;
    private Configuration configuration;
    private TimeSeries timeSeries;
    private Forecast forecast;
    private Deepnet deepnet;
    private OptiML optiml;
    private Fusion fusion;

    private Properties props;
    private String storage;

    private CacheManager cacheManager;

    protected BigMLClient() {
    }

    public static BigMLClient getInstance() 
    		throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(null, null, null, null);
        }
        return instance;
    }

    public static BigMLClient getInstance(final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(null, null, null, storage);
        }
        return instance;
    }
    
    @Deprecated
    public static BigMLClient getInstance(final boolean devMode)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(null, null, null, null);
        }
        return instance;
    }
    
    @Deprecated
    public static BigMLClient getInstance(final String seed, 
    		final boolean devMode)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(null, null, null, null);
        }
        return instance;
    }
    
    @Deprecated
    public static BigMLClient getInstance(final String apiUser,
            final String apiKey, final boolean devMode)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(null, apiUser, apiKey, null);
        }
        return instance;
    }
    
    @Deprecated
    public static BigMLClient getInstance(final String apiUser,
            final String apiKey, final String seed, final boolean devMode)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(apiUser, apiKey, seed, null);
        }
        return instance;
    }
    
    @Deprecated
    public static BigMLClient getInstance(final String apiUser,
            final String apiKey, final boolean devMode, final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(apiUser, apiKey, null, storage);
        }
        return instance;
    }
    
    @Deprecated
    public static BigMLClient getInstance(final String apiUser,
            final String apiKey, final String seed, 
            final boolean devMode, final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(null, apiUser, apiKey, storage);
        }
        return instance;
    }
    
    @Deprecated
    public static BigMLClient getInstance(final String bigmlDomain, final String apiUser,
            final String apiKey, final String seed, final boolean devMode, final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(bigmlDomain, apiUser, apiKey, storage);
        }
        return instance;
    }
    
    public static BigMLClient getInstance(final String bigmlDomain, 
    		final String apiUser, final String apiKey, 
    		final String storage)
            throws AuthenticationException {
        if (instance == null) {
            instance = new BigMLClient();
            instance.init(bigmlDomain, apiUser, apiKey, storage);
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private String setting(final String setting) {
      String value = System.getProperty(setting);
      if (value == null)
        value = System.getenv(setting);
      return value;
    }
    
    
    /**
     * Initialization object.
     */
    private void init(final String bigmlDomain, final String apiUser,
                      final String apiKey, final String storage)
            throws AuthenticationException {
        this.bigmlDomain = bigmlDomain;
        this.storage = storage;
        initConfiguration();
        initBigmlSettings(apiUser, apiKey);
        initResources();
    }

    private void initBigmlSettings(final String apiUser,
                                   final String apiKey) 
      throws AuthenticationException {

        this.bigmlUser = apiUser != null ? apiUser : this.setting("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : this.setting("BIGML_API_KEY");
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

    }

    private void initConfiguration() {
        try {
            props = new Properties();
            FileInputStream fis = new FileInputStream(new File(
                    "src/main/resources/binding.properties"));
            props.load(fis);
            fis.close();

            if( bigmlDomain != null && bigmlDomain.length() > 0 ) {
                bigmlUrl = (bigmlDomain + (bigmlDomain.endsWith("/") ? "" : "/"));
            } else {
                bigmlUrl = props.getProperty("BIGML_URL", BIGML_URL);
            }

        } catch (Throwable e) {
            // logger.error("Error loading configuration", e);
            bigmlUrl = BIGML_URL;
        }
    }

    private void initResources() {
        
    	// TODO: the use of the cache is making some resources not to be
    	// reloaded correctly, for example, a cluster after creating
    	// datasets from it, so clusters_datasets is not retrieved as expected.
    	// We deactivate the use of the cache for now.
    	
    	// Lets create the storage folder in it was informed
        //this.cacheManager = new CacheManager(storage);
        this.cacheManager = null;

        source = new Source(this.bigmlUser, this.bigmlApiKey, cacheManager);
        dataset = new Dataset(this.bigmlUser, this.bigmlApiKey, cacheManager);
        model = new Model(this.bigmlUser, this.bigmlApiKey, cacheManager);
        prediction = new Prediction(this.bigmlUser, this.bigmlApiKey, cacheManager);
        evaluation = new Evaluation(this.bigmlUser, this.bigmlApiKey, cacheManager);
        ensemble = new Ensemble(this.bigmlUser, this.bigmlApiKey, cacheManager);
        anomaly = new Anomaly(this.bigmlUser, this.bigmlApiKey, cacheManager);
        anomalyScore = new AnomalyScore(this.bigmlUser, this.bigmlApiKey, cacheManager);
        batchAnomalyScore = new BatchAnomalyScore(this.bigmlUser, this.bigmlApiKey, cacheManager);
        batchPrediction = new BatchPrediction(this.bigmlUser, this.bigmlApiKey, cacheManager);
        cluster = new Cluster(this.bigmlUser, this.bigmlApiKey, cacheManager);
        centroid = new Centroid(this.bigmlUser, this.bigmlApiKey, cacheManager);
        batchCentroid = new BatchCentroid(this.bigmlUser, this.bigmlApiKey, cacheManager);
        project = new Project(this.bigmlUser, this.bigmlApiKey, cacheManager);
        sample = new Sample(this.bigmlUser, this.bigmlApiKey, cacheManager);
        correlation = new Correlation(this.bigmlUser, this.bigmlApiKey, cacheManager);
        statisticalTest = new StatisticalTest(this.bigmlUser, this.bigmlApiKey, cacheManager);
        logisticRegression = new LogisticRegression(this.bigmlUser, this.bigmlApiKey, cacheManager);
        script = new Script(this.bigmlUser, this.bigmlApiKey, cacheManager);
        execution = new Execution(this.bigmlUser, this.bigmlApiKey, cacheManager);
        library = new Library(this.bigmlUser, this.bigmlApiKey, cacheManager);
        association = new Association(this.bigmlUser, this.bigmlApiKey, cacheManager);
        associationSet = new AssociationSet(this.bigmlUser, this.bigmlApiKey, cacheManager);
        topicModel = new TopicModel(this.bigmlUser, this.bigmlApiKey, cacheManager);
        topicDistribution = new TopicDistribution(this.bigmlUser, this.bigmlApiKey, cacheManager);
        batchTopicDistribution = new BatchTopicDistribution(this.bigmlUser, this.bigmlApiKey, cacheManager);
        configuration = new Configuration(this.bigmlUser, this.bigmlApiKey, cacheManager);
        timeSeries = new TimeSeries(this.bigmlUser, this.bigmlApiKey, cacheManager);
        forecast = new Forecast(this.bigmlUser, this.bigmlApiKey, cacheManager);
        deepnet = new Deepnet(this.bigmlUser, this.bigmlApiKey, cacheManager);
        optiml = new OptiML(this.bigmlUser, this.bigmlApiKey, cacheManager);
        fusion = new Fusion(this.bigmlUser, this.bigmlApiKey, cacheManager);
    }

    public String getBigMLUrl() {
        return bigmlUrl;
    }
    
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    // ################################################################
    // #
    // # Sources
    // # https://bigml.com/api/sources
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
    public JSONObject createSource(final String fileName, String name,
            JSONObject sourceParser) {
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
     * @param args
     *            set of parameters for the new model. Optional
     */
    public JSONObject createSource(final String fileName, String name,
            JSONObject sourceParser, JSONObject args) {
        return source.createLocalSource(fileName, name, sourceParser, args);
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
        return source.createRemoteSource(url, sourceParser, args);
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
     * Creates a source using a BatchPrediction ID.
     *
     * POST /andromeda/source?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json;
     *
     * @param batchPredictionId
     *            the resource ID of the batch prediction resource
     * @param sourceParser
     *            set of parameters to create the source. Optional
     * @param args
     *            set of parameters for the new model. Optional
     */
    public JSONObject createSourceFromBatchPrediction(final String batchPredictionId,
            final JSONObject sourceParser, final JSONObject args) {

        return source.createSourceFromBatchPrediction(batchPredictionId, sourceParser, args);
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
     */
    public JSONObject createSourceFromBatchAnomalyScore(final String batchAnomalyScoreId,
            final JSONObject sourceParser) {

        return source.createSourceFromBatchAnomalyScore(batchAnomalyScoreId, sourceParser);
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
     * @param args
     *            set of parameters for the new model. Optional
     */
    public JSONObject createSourceFromBatchAnomalyScore(final String batchAnomalyScoreId,
            final JSONObject sourceParser, final JSONObject args) {

        return source.createSourceFromBatchAnomalyScore(batchAnomalyScoreId, sourceParser, args);
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
    public JSONObject deleteSource(final JSONObject sourceJSON) {
        return source.delete(sourceJSON);
    }

    // ################################################################
    // #
    // # Datasets
    // # https://bigml.com/api/datasets
    // #
    // ################################################################

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
     * Creates a remote dataset.
     *
     * Uses a lists of remote datasets to create a new dataset using the
     * arguments in `args`.
     *
     * If `wait_time` is higher than 0 then the dataset creation
     * request is not sent until the `source` has been created successfuly.
     *
     * @param datasetsIds
     *            list of dataset Ids.
     * @param args
     *            set of parameters for the new dataset. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for source
     *            before to start to create the dataset. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createDataset(final List datasetsIds, JSONObject args,
            Integer waitTime, Integer retries) {
        return dataset.create(datasetsIds, args, waitTime, retries);
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
    // # https://bigml.com/api/models
    // #
    // ################################################################

    /**
     * Creates a new model.
     *
     * POST /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param resourceId
     *            a unique identifier in the form [dataset|cluster]/id
     *            where id is a string of 24 alpha-numeric chars for the
     *            remote resource to attach the model.
     * @param args
     *            set of parameters for the new model. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for source
     *            before to start to create the model. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createModel(final String resourceId, 
    		JSONObject args, Integer waitTime, Integer retries) {

        return model.create(resourceId, args, waitTime, retries);
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
    public JSONObject createModel(final List datasetsIds, 
    		JSONObject args, Integer waitTime, Integer retries) {

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
    // # https://bigml.com/api/anomalies
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
    public JSONObject createAnomaly(final String datasetId, 
    		JSONObject args, Integer waitTime, Integer retries) {

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
    public JSONObject createAnomaly(final List datasetsIds, 
    		JSONObject args, Integer waitTime, Integer retries) {

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
     * Retrieves an anomaly.
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
     *            an anomaly JSONObject
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
     * Checks whether an anomaly's status is FINISHED.
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
     * Updates an anomaly.
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
    // # https://bigml.com/api/predictions
    // #
    // ################################################################

    /**
     * Creates a new prediction.
     *
     * POST
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param modelId
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
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
    public JSONObject createPrediction(final String modelId,
            JSONObject inputData, Boolean byName, JSONObject args,
            Integer waitTime, Integer retries) {
        return prediction.create(modelId, inputData, byName, args,
                waitTime, retries);
    }

    /**
     * Creates a new prediction.
     *
     * POST
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param modelId
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a prediction for.
     * @param args
     *            set of parameters for the new prediction. Required
     * @param waitTime
     *            time to wait for next check of FINISHED status for model
     *            before to start to create the prediction. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createPrediction(final String modelId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {
        return prediction.create(modelId, inputData, false, args,
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
    // # https://bigml.com/api/anomalyscores
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
            JSONObject inputData, JSONObject args, Integer waitTime,
            Integer retries) {
        return anomalyScore.create(anomalyId, inputData, false, args,
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
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {
        String anomalyId = (String) anomaly.get("resource");
        return  createAnomalyScore(anomalyId, inputData, args, 
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
     *            an anomaly score JSONObject
     *
     */
    public JSONObject getAnomalyScore(final JSONObject anomalyScoreJSON) {
        return anomalyScore.get(anomalyScoreJSON);
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
    // # https://bigml.com/api/evaluations
    // #
    // ################################################################

    /**
     * Creates a new evaluation.
     *
     * POST
     * /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param modelId
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
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
    public JSONObject createEvaluation(final String modelId,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {

        return evaluation.create(modelId, datasetId, args, waitTime, retries);
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
    // # https://bigml.com/api/ensembles
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
    public JSONObject createEnsemble(final String datasetId, 
    		JSONObject args, Integer waitTime, Integer retries) {

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
    public JSONObject createEnsemble(final List datasetsIds, 
    		JSONObject args, Integer waitTime, Integer retries) {

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
    // # https://bigml.com/api/batch_predictions
    // #
    // ################################################################

    /**
     * Creates a new batch prediction.
     *
     * POST /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param modelId
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
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
    public JSONObject createBatchPrediction(final String modelId,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {
        return batchPrediction.create(modelId, datasetId, args,
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
    // # https://bigml.com/api/batch_anomalyscore
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
    // # https://bigml.com/api/clusters
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
    public JSONObject createCluster(final String datasetId, 
    		JSONObject args, Integer waitTime, Integer retries) {

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
    public JSONObject create(final List datasetsIds, 
    		JSONObject args, Integer waitTime, Integer retries) {

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
    // # https://bigml.com/api/centroids
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
    // # https://bigml.com/api/batch_centroids
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

    // ################################################################
    // #
    // # Samples
    // # https://bigml.com/api/sample
    // #
    // ################################################################

    /**
     * Creates a remote sample.
     *
     * Uses remote `dataset` to create a new sample using the arguments in
     * `args`. If `wait_time` is higher than 0 then the sample creation request
     * is not sent until the `dataset` has been created successfuly.
     *
     * POST /andromeda/sample?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a string
     *            of 24 alpha-numeric chars for the sample to attach the
     *            dataset.
     * @param args
     *            set of parameters for the new sample. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the sample. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createSample(final String datasetId, JSONObject args,
                                    Integer waitTime, Integer retries) {
        return sample.create(datasetId, args, waitTime, retries);
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
    public JSONObject getSample(final String sampleId) {
        return sample.get(sampleId);
    }

    /**
     * Returns the ids of the fields that contain errors and their number.
     *
     * @param sampleId the sample id of the sample to be inspected
     */
    public Map<String, Long> getErrorCountsInSample(final String sampleId) {
        return sample.getErrorCounts(getSample(sampleId));
    }

    /**
     * Returns the ids of the fields that contain errors and their number.
     *
     * @param sampleJSON the sample JSON object to be inspected
     */
    public Map<String, Long> getErrorCountsInSample(final JSONObject sampleJSON) {
        return sample.getErrorCounts(sampleJSON);
    }

    /**
     * Retrieves a sample.
     *
     * GET
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param sampleJSON
     *            a sample JSONObject
     *
     */
    public JSONObject getSample(final JSONObject sampleJSON) {
        return sample.get(sampleJSON);
    }

    /**
     * Retrieves an sample.
     *
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
    public JSONObject getSample(final String sampleId, final String queryString) {
        return getSample(sampleId, queryString, null, null);
    }

    /**
     * Retrieves an sample.
     *
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
    public JSONObject getSample(final String sampleId, final String queryString,
                                 final String apiUser, final String apiKey) {
        return sample.get(sampleId, queryString, apiUser, apiKey);
    }

    /**
     * Check whether a sample's status is FINISHED.
     *
     * @param sampleId
     *            a unique identifier in the form sample/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean sampleIsReady(final String sampleId) {
        return sample.isReady(sampleId);
    }

    /**
     * Checks whether a sample's status is FINISHED.
     *
     * @param sampleJSON
     *            a sample JSONObject
     *
     */
    public boolean sampleIsReady(final JSONObject sampleJSON) {
        return sample.isReady(sampleJSON);
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
    public JSONObject listSamples(final String queryString) {
        return sample.list(queryString);
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
     *            set of parameters to update the source. Optional
     *
     */
    public JSONObject updateSample(final String sampleId, final String changes) {
        return sample.update(sampleId, changes);
    }

    /**
     * Updates a sample.
     *
     * PUT
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param sampleJSON
     *            a sample JSONObject
     * @param changes
     *            set of parameters to update the sample. Optional
     *
     */
    public JSONObject updateSample(final JSONObject sampleJSON,
                                    final JSONObject changes) {
        return sample.update(sampleJSON, changes);
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
    public JSONObject deleteSample(final String sampleId) {
        return sample.delete(sampleId);
    }

    /**
     * Deletes a sample.
     *
     * DELETE
     * /andromeda/sample/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param sampleJSON
     *            a sample JSONObject
     *
     */
    public JSONObject deleteSample(final JSONObject sampleJSON) {
        return sample.delete(sampleJSON);
    }

    // ################################################################
    // #
    // # Projects
    // # https://bigml.com/api/project
    // #
    // ################################################################

    /**
     * Creates a remote project.
     *
     * Create a new project using the arguments in `args`.
     *
     * POST /andromeda/project?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param args
     *            set of parameters for the new sample. Optional
     *
     */
    public JSONObject createProject(JSONObject args) {
        return project.create(args);
    }

    /**
     * Retrieves a project.
     *
     * GET
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    public JSONObject getProject(final String projectId) {
        return project.get(projectId);
    }

    /**
     * Retrieves a project.
     *
     * GET
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param projectJSON
     *            a project JSONObject
     *
     */
    public JSONObject getProject(final JSONObject projectJSON) {
        return project.get(projectJSON);
    }

    /**
     * Retrieves an project.
     *
     *
     * GET /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject getProject(final String projectId, final String queryString) {
        return getProject(projectId, queryString, null, null);
    }

    /**
     * Retrieves an project.
     *
     *
     * GET /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject getProject(final String projectId, final String queryString,
                                final String apiUser, final String apiKey) {
        return project.get(projectId, queryString, apiUser, apiKey);
    }

    /**
     * Check whether a project's status is FINISHED.
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean projectIsReady(final String projectId) {
        return project.isReady(projectId);
    }

    /**
     * Checks whether a project's status is FINISHED.
     *
     * @param projectJSON
     *            a project JSONObject
     *
     */
    public boolean projectIsReady(final JSONObject projectJSON) {
        return project.isReady(projectJSON);
    }

    /**
     * Lists all your projects.
     *
     * GET /andromeda/project?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listProjects(final String queryString) {
        return project.list(queryString);
    }

    /**
     * Updates a project.
     *
     * PUT
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the project. Optional
     *
     */
    public JSONObject updateProject(final String projectId, final String changes) {
        return project.update(projectId, changes);
    }

    /**
     * Updates a project.
     *
     * PUT
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param projectJSON
     *            a project JSONObject
     * @param changes
     *            set of parameters to update the sample. Optional
     *
     */
    public JSONObject updateProject(final JSONObject projectJSON,
                                   final JSONObject changes) {
        return project.update(projectJSON, changes);
    }

    /**
     * Deletes a project.
     *
     * DELETE
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param projectId
     *            a unique identifier in the form project/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteProject(final String projectId) {
        return project.delete(projectId);
    }

    /**
     * Deletes a project.
     *
     * DELETE
     * /andromeda/project/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param projectJSON
     *            a project JSONObject
     *
     */
    public JSONObject deleteProject(final JSONObject projectJSON) {
        return project.delete(projectJSON);
    }


    // ################################################################
    // #
    // # Correlations
    // # https://bigml.com/api/correlations
    // #
    // ################################################################

    /**
     * Creates a new correlation.
     *
     * POST /andromeda/correlation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            correlation.
     * @param args
     *            set of parameters for the new correlation. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the correlation. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createCorrelation(final String datasetId, 
    		JSONObject args, Integer waitTime, Integer retries) {

        return correlation.create(datasetId, args, waitTime, retries);
    }

    /**
     * Retrieves a correlation.
     *
     * A correlation is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the correlation values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getCorrelation(final String correlationId) {
        return correlation.get(correlationId);
    }

    /**
     * Retrieves an correlation.
     *
     * A correlation is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the ensemble values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param correlationJSON
     *            an correlation JSONObject.
     *
     */
    public JSONObject getCorrelation(final JSONObject correlationJSON) {
        return correlation.get(correlationJSON);
    }

    /**
     * Check whether a correlation's status is FINISHED.
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean correlationIsReady(final String correlationId) {
        return correlation.isReady(correlationId);
    }

    /**
     * Check whether a correlation's status is FINISHED.
     *
     * @param correlationJSON
     *            an correlation JSONObject.
     *
     */
    public boolean correlationIsReady(final JSONObject correlationJSON) {
        return correlation.isReady(correlationJSON);
    }

    /**
     * Lists all your correlations.
     *
     * GET /andromeda/correlation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listCorrelations(final String queryString) {
        return correlation.list(queryString);
    }

    /**
     * Updates a correlation.
     *
     * PUT
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the correlation. Optional
     *
     */
    public JSONObject updateCorrelation(final String correlationId, final String changes) {
        return correlation.update(correlationId, changes);
    }

    /**
     * Updates a correlation.
     *
     * PUT
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param correlationJSON
     *            an correlation JSONObject
     * @param changes
     *            set of parameters to update the correlation. Optional
     */
    public JSONObject updateCorrelation(final JSONObject correlationJSON,
            final JSONObject changes) {
        return correlation.update(correlationJSON, changes);
    }

    /**
     * Deletes a correlation.
     *
     * DELETE
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param correlationId
     *            a unique identifier in the form correlation/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteCorrelation(final String correlationId) {
        return correlation.delete(correlationId);
    }

    /**
     * Deletes a correlation.
     *
     * DELETE
     * /andromeda/correlation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param correlationJSON
     *            an correlation JSONObject.
     *
     */
    public JSONObject deleteCorrelation(final JSONObject correlationJSON) {
        return correlation.delete(correlationJSON);
    }

    // ################################################################
    // #
    // # StatisticalTests
    // # https://bigml.com/api/statisticaltests
    // #
    // ################################################################

    /**
     * Creates a new statisticaltest.
     *
     * POST /andromeda/statisticaltest?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            statisticaltest.
     * @param args
     *            set of parameters for the new statisticaltest. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the statisticaltest. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createStatisticalTest(final String datasetId, 
    		JSONObject args, Integer waitTime, Integer retries) {

        return statisticalTest.create(datasetId, args, waitTime, retries);
    }

    /**
     * Retrieves a statisticaltest.
     *
     * A statisticaltest is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the statisticaltest values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/ensemble/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getStatisticalTest(final String statisticaltestId) {
        return statisticalTest.get(statisticaltestId);
    }

    /**
     * Retrieves an statisticaltest.
     *
     * A statisticaltest is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the ensemble values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param statisticaltestJSON
     *            an statisticaltest JSONObject.
     *
     */
    public JSONObject getStatisticalTest(final JSONObject statisticaltestJSON) {
        return statisticalTest.get(statisticaltestJSON);
    }

    /**
     * Check whether a statisticaltest's status is FINISHED.
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean statisticalTestIsReady(final String statisticaltestId) {
        return statisticalTest.isReady(statisticaltestId);
    }

    /**
     * Check whether a statisticaltest's status is FINISHED.
     *
     * @param statisticaltestJSON
     *            an statisticaltest JSONObject.
     *
     */
    public boolean statisticalTestIsReady(final JSONObject statisticaltestJSON) {
        return statisticalTest.isReady(statisticaltestJSON);
    }

    /**
     * Lists all your statisticaltests.
     *
     * GET /andromeda/statisticaltest?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listStatisticalTests(final String queryString) {
        return statisticalTest.list(queryString);
    }

    /**
     * Updates a statisticaltest.
     *
     * PUT
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the statisticaltest. Optional
     *
     */
    public JSONObject updateStatisticalTest(final String statisticaltestId, final String changes) {
        return statisticalTest.update(statisticaltestId, changes);
    }

    /**
     * Updates a statisticaltest.
     *
     * PUT
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param statisticaltestJSON
     *            an statisticaltest JSONObject
     * @param changes
     *            set of parameters to update the statisticaltest. Optional
     */
    public JSONObject updateStatisticalTest(final JSONObject statisticaltestJSON,
            final JSONObject changes) {
        return statisticalTest.update(statisticaltestJSON, changes);
    }

    /**
     * Deletes a statisticaltest.
     *
     * DELETE
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param statisticaltestId
     *            a unique identifier in the form statisticaltest/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteStatisticalTest(final String statisticaltestId) {
        return statisticalTest.delete(statisticaltestId);
    }

    /**
     * Deletes a statisticaltest.
     *
     * DELETE
     * /andromeda/statisticaltest/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param statisticaltestJSON
     *            an statisticaltest JSONObject.
     *
     */
    public JSONObject deleteStatisticalTest(final JSONObject statisticaltestJSON) {
        return statisticalTest.delete(statisticaltestJSON);
    }



    // ################################################################
    // #
    // # LogisticRegression
    // # https://bigml.com/api/logisticregressions
    // #
    // ################################################################

    /**
     * Creates a new logistic regression.
     *
     * POST /andromeda/logisticregression?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            logisticr egression.
     * @param args
     *            set of parameters for the new logistic regression. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the logistic regression. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createLogisticRegression(final String datasetId, 
    		JSONObject args, Integer waitTime, Integer retries) {

        return logisticRegression.create(datasetId, args, waitTime, retries);
    }

    /**
     * Creates an logistic regression from a list of `datasets`.
     *
     * POST /andromeda/logisticregression?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            logistic regression.
     * @param args
     *            set of parameters for the new logistic regression. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the logistic regression.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createLogisticRegression(final List datasetsIds, 
    		JSONObject args, Integer waitTime, Integer retries) {

        return logisticRegression.create(datasetsIds, args, waitTime, retries);
    }

    /**
     * Retrieves a logistic regression.
     *
     * A logistic regression is an evolving object that is processed until it
     * reaches the FINISHED or FAULTY state, the method will return a JSONObject
     * that encloses the logistic Regression values and state info available at
     * the time it is called.
     *
     * GET
     * /andromeda/logisticregression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param logisticRegressionId
     *            a unique identifier in the form logisticregression/id where id
     *            is a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getLogisticRegression(final String logisticRegressionId) {
        return logisticRegression.get(logisticRegressionId);
    }

    /**
     * Retrieves a logisticRegression.
     *
     * A logisticRegression is an evolving object that is processed until it
     * reaches the FINISHED or FAULTY state, the method will return a JSONObject
     * that encloses the logistic Regression values and state info available at
     * the time it is called.
     *
     * GET
     * /andromeda/logisticRegression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param logisticRegressionJSON
     *            a logisticRegression JSONObject.
     *
     */
    public JSONObject getLogisticRegression(final JSONObject logisticRegressionJSON) {
        return logisticRegression.get(logisticRegressionJSON);
    }

    /**
     * Check whether a logisticRegression's status is FINISHED.
     *
     * @param logisticRegressionId
     *            a unique identifier in the form logisticregression/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean logisticRegressionIsReady(final String logisticRegressionId) {
        return logisticRegression.isReady(logisticRegressionId);
    }

    /**
     * Check whether a logisticRegression's status is FINISHED.
     *
     * @param logisticRegressionJSON
     *            a logisticRegression JSONObject.
     *
     */
    public boolean logisticRegressionIsReady(final JSONObject logisticRegressionJSON) {
        return logisticRegression.isReady(logisticRegressionJSON);
    }

    /**
     * Lists all your logisticRegressions.
     *
     * GET /andromeda/logisticRegression?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listLogisticRegressions(final String queryString) {
        return logisticRegression.list(queryString);
    }

    /**
     * Updates a logisticRegression.
     *
     * PUT
     * /andromeda/logisticRegression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param logisticRegressionId
     *            a unique identifier in the form logisticregression/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the logistic regression. Optional
     *
     */
    public JSONObject updateLogisticRegression(final String logisticRegressionId,
            final String changes) {
        return logisticRegression.update(logisticRegressionId, changes);
    }

    /**
     * Updates a logisticRegression.
     *
     * PUT
     * /andromeda/logisticRegression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param logisticRegressionJSON
     *            a logisticRegression JSONObject
     * @param changes
     *            set of parameters to update the logisticRegression. Optional
     */
    public JSONObject updateLogisticRegression(final JSONObject logisticRegressionJSON,
            final JSONObject changes) {
        return logisticRegression.update(logisticRegressionJSON, changes);
    }

    /**
     * Deletes a logisticRegression.
     *
     * DELETE
     * /andromeda/logisticRegression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param logisticRegressionId
     *            a unique identifier in the form logisticregression/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteLogisticRegression(final String logisticRegressionId) {
        return logisticRegression.delete(logisticRegressionId);
    }

    /**
     * Deletes a logisticRegression.
     *
     * DELETE
     * /andromeda/logisticRegression/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param logisticRegressionJSON
     *            a logisticRegression JSONObject.
     *
     */
    public JSONObject deleteLogisticRegression(final JSONObject logisticRegressionJSON) {
        return logisticRegression.delete(logisticRegressionJSON);
    }


    // ################################################################
    // #
    // # Whizzml Script
    // # https://bigml.com/api/scripts
    // #
    // ################################################################

    /**
     * Creates a whizzml script from its source code.
     *
     * POST /andromeda/script?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param source
     *            source code for the script. It can be either
     *              - string: source code
     *              - script id: the ID for an existing whizzml script
     *              - path: the path to a file containing the source code
     * @param args
     *            set of parameters for the new script. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the script. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createScript(final String source, JSONObject args,
        Integer waitTime, Integer retries) {

        return script.create(source, args, waitTime, retries);
    }

    /**
     * Retrieves a whizzml script.
     *
     * GET
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getScript(final String scriptId) {
        return script.get(scriptId);
    }

    /**
     * Retrieves a whizzml script.
     *
     * GET
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param scriptJSON
     *            a script JSONObject
     *
     */
    public JSONObject getScript(final JSONObject scriptJSON) {
        return script.get(scriptJSON);
    }

    /**
     * Checks whether a whizzml script's status is FINISHED.
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean scriptIsReady(final String scriptId) {
        return script.isReady(scriptId);
    }

    /**
     * Checks whether a whizzml script status is FINISHED.
     *
     * @param scriptJSON
     *            a script JSONObject
     *
     */
    public boolean scriptIsReady(final JSONObject scriptJSON) {
        return script.isReady(scriptJSON);
    }

    /**
     * Lists all your whizzml libraries.
     *
     * GET /andromeda/script?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listScripts(final String queryString) {
        return script.list(queryString);
    }

    /**
     * Updates a whizzml script.
     *
     * PUT
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the script. Optional
     *
     */
    public JSONObject updateScript(final String scriptId, final String changes) {
        return script.update(scriptId, changes);
    }

    /**
     * Updates a whizzml script.
     *
     * PUT
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param scriptJSON
     *            a script JSONObject
     * @param changes
     *            set of parameters to update the script. Optional
     *
     */
    public JSONObject updateScript(final JSONObject scriptJSON,
            final JSONObject changes) {
        return script.update(scriptJSON, changes);
    }

    /**
     * Deletes a whizzml script.
     *
     * DELETE
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param scriptId
     *            a unique identifier in the form script/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteScript(final String scriptId) {
        return script.delete(scriptId);
    }

    /**
     * Deletes a script.
     *
     * DELETE
     * /andromeda/script/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param scriptJSON
     *            a script JSONObject
     *
     */
    public JSONObject deleteScript(final JSONObject scriptJSON) {
        return script.delete(scriptJSON);
    }


    // ################################################################
    // #
    // # Whizzml Execution
    // # https://bigml.com/api/executions
    // #
    // ################################################################

    /**
     * Creates a whizzml execution for a script.
     *
     * POST /andromeda/execution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param script
     *            a unique identifier in the form script/id where id is a string
     *            of 24 alpha-numeric chars for the script to attach the execution.
     * @param args
     *            set of parameters for the new execution. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the execution. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createExecution(final String script, JSONObject args,
        Integer waitTime, Integer retries) {

        return execution.create(script, args, waitTime, retries);
    }

    /**
     * Creates a whizzml execution for a list of scripts.
     *
     * POST /andromeda/execution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param scripts
     *            a list of identifiers in the form script/id where id is a string
     *            of 24 alpha-numeric chars for the script to attach the execution.
     * @param args
     *            set of parameters for the new execution. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the execution. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createExecution(List scripts, JSONObject args,
                                      Integer waitTime, Integer retries) {
        return execution.create(scripts, args, waitTime, retries);
    }

    /**
     * Retrieves a whizzml execution.
     *
     * GET
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param executionId
     *            a unique identifier in the form execution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getExecution(final String executionId) {
        return execution.get(executionId);
    }

    /**
     * Retrieves a whizzml execution.
     *
     * GET
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param executionJSON
     *            a execution JSONObject
     *
     */
    public JSONObject getExecution(final JSONObject executionJSON) {
        return execution.get(executionJSON);
    }

    /**
     * Checks whether a whizzml execution's status is FINISHED.
     *
     * @param executionId
     *            a unique identifier in the form execution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean executionIsReady(final String executionId) {
        return execution.isReady(executionId);
    }

    /**
     * Checks whether a whizzml execution status is FINISHED.
     *
     * @param executionJSON
     *            a execution JSONObject
     *
     */
    public boolean executionIsReady(final JSONObject executionJSON) {
        return execution.isReady(executionJSON);
    }

    /**
     * Lists all your whizzml executions.
     *
     * GET /andromeda/execution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listExecutions(final String queryString) {
        return execution.list(queryString);
    }

    /**
     * Updates a whizzml execution.
     *
     * PUT
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param executionId
     *            a unique identifier in the form execution/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the execution. Optional
     *
     */
    public JSONObject updateExecution(final String executionId, final String changes) {
        return execution.update(executionId, changes);
    }

    /**
     * Updates a whizzml execution.
     *
     * PUT
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param executionJSON
     *            a execution JSONObject
     * @param changes
     *            set of parameters to update the execution. Optional
     *
     */
    public JSONObject updateExecution(final JSONObject executionJSON,
            final JSONObject changes) {
        return execution.update(executionJSON, changes);
    }

    /**
     * Deletes a whizzml execution.
     *
     * DELETE
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param executionId
     *            a unique identifier in the form execution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteExecution(final String executionId) {
        return execution.delete(executionId);
    }

    /**
     * Deletes a whizzml execution.
     *
     * DELETE
     * /andromeda/execution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param executionJSON
     *            a execution JSONObject
     *
     */
    public JSONObject deleteExecution(final JSONObject executionJSON) {
        return execution.delete(executionJSON);
    }


    // ################################################################
    // #
    // # Whizzml Library
    // # https://bigml.com/api/libraries
    // #
    // ################################################################

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
    public JSONObject createLibrary(final String source, JSONObject args,
        Integer waitTime, Integer retries) {

        return library.create(source, args, waitTime, retries);
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
    public JSONObject getLibrary(final String libraryId) {
        return library.get(libraryId);
    }

    /**
     * Retrieves a whizzml library.
     *
     * GET
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param executionJSON
     *            a library JSONObject
     *
     */
    public JSONObject getLibrary(final JSONObject executionJSON) {
        return library.get(executionJSON);
    }

    /**
     * Checks whether a whizzml library's status is FINISHED.
     *
     * @param libraryId
     *            a unique identifier in the form library/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean libraryIsReady(final String libraryId) {
        return library.isReady(libraryId);
    }

    /**
     * Checks whether a whizzml library status is FINISHED.
     *
     * @param executionJSON
     *            a library JSONObject
     *
     */
    public boolean libraryIsReady(final JSONObject executionJSON) {
        return library.isReady(executionJSON);
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
    public JSONObject listLibraries(final String queryString) {
        return library.list(queryString);
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
    public JSONObject updateLibrary(final String libraryId, final String changes) {
        return library.update(libraryId, changes);
    }

    /**
     * Updates a whizzml library.
     *
     * PUT
     * /andromeda/library/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param executionJSON
     *            a library JSONObject
     * @param changes
     *            set of parameters to update the library. Optional
     *
     */
    public JSONObject updateLibrary(final JSONObject executionJSON,
            final JSONObject changes) {
        return library.update(executionJSON, changes);
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
    public JSONObject deleteLibrary(final String libraryId) {
        return library.delete(libraryId);
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
    public JSONObject deleteLibrary(final JSONObject executionJSON) {
        return library.delete(executionJSON);
    }


    // ################################################################
    // #
    // # Associations
    // # https://bigml.com/api/associations
    // #
    // ################################################################

    /**
     * Creates a new association.
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
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the association. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createAssociation(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        return association.create(datasetId, args, waitTime, retries);
    }

    /**
     * Retrieves an association.
     *
     * An association is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the association values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getAssociation(final String associationId) {
        return association.get(associationId);
    }

    /**
     * Retrieves an association.
     *
     * An association is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the association values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param associationJSON
     *            an association JSONObject.
     *
     */
    public JSONObject getAssociation(final JSONObject associationJSON) {
        return association.get(associationJSON);
    }

    /**
     * Check whether an association's status is FINISHED.
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean associationIsReady(final String associationId) {
        return association.isReady(associationId);
    }

    /**
     * Check whether an association's status is FINISHED.
     *
     * @param associationJSON
     *            an association JSONObject.
     *
     */
    public boolean associationIsReady(final JSONObject associationJSON) {
        return association.isReady(associationJSON);
    }

    /**
     * Lists all your association.
     *
     * GET /andromeda/association?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listAssociations(final String queryString) {
        return association.list(queryString);
    }

    /**
     * Updates an association.
     *
     * PUT
     * /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the association. Optional
     *
     */
    public JSONObject updateAssociation(final String associationId, final String changes) {
        return association.update(associationId, changes);
    }

    /**
     * Updates an association.
     *
     * PUT
     * /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationJSON
     *            an association JSONObject
     * @param changes
     *            set of parameters to update the association. Optional
     */
    public JSONObject updateAssociation(final JSONObject associationJSON,
            final JSONObject changes) {
        return association.update(associationJSON, changes);
    }

    /**
     * Deletes an association.
     *
     * DELETE
     * /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param associationId
     *            a unique identifier in the form association/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteAssociation(final String associationId) {
        return association.delete(associationId);
    }

    /**
     * Deletes an association.
     *
     * DELETE
     * /andromeda/association/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param associationJSON
     *            an association JSONObject.
     *
     */
    public JSONObject deleteAssociation(final JSONObject associationJSON) {
        return association.delete(associationJSON);
    }


    // ################################################################
    // #
    // # AssociationSets
    // # https://bigml.com/api/associationsets
    // #
    // ################################################################

    /**
     * Creates a new association set.
     *
     * POST /andromeda/associationset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form association/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            association set.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create an association set for.
     * @param args
     *            set of parameters for the new association. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for association
     *            before to start to create the association set. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createAssociationSet(final String associationId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {

        return associationSet.create(associationId, inputData, args,
                waitTime, retries);
    }

    /**
     * Retrieves an association set.
     *
     * An associationset is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the association set values and state info available at the time
     * it is called.
     *
     * GET
     * /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param associationSetId
     *            a unique identifier in the form associationset/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getAssociationSet(final String associationSetId) {
        return associationSet.get(associationSetId);
    }

    /**
     * Retrieves an association set.
     *
     * An associationset is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the association values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param associationsetJSON
     *            an association set JSONObject.
     *
     */
    public JSONObject getAssociationSet(final JSONObject associationsetJSON) {
        return associationSet.get(associationsetJSON);
    }

    /**
     * Check whether an association set's status is FINISHED.
     *
     * @param associationSetId
     *            a unique identifier in the form associationset/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public boolean associationSetIsReady(final String associationSetId) {
        return associationSet.isReady(associationSetId);
    }

    /**
     * Check whether an association set's status is FINISHED.
     *
     * @param associationSetJSON
     *            an association set JSONObject.
     *
     */
    public boolean associationSetIsReady(final JSONObject associationSetJSON) {
        return associationSet.isReady(associationSetJSON);
    }

    /**
     * Lists all your associationset.
     *
     * GET /andromeda/associationset?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listAssociationSets(final String queryString) {
        return associationSet.list(queryString);
    }

    /**
     * Updates an association set.
     *
     * PUT
     * /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationSetId
     *            a unique identifier in the form associationset/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the association set. Optional
     *
     */
    public JSONObject updateAssociationSet(final String associationSetId, final String changes) {
        return associationSet.update(associationSetId, changes);
    }

    /**
     * Updates an association set.
     *
     * PUT
     * /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param associationSetJSON
     *            an associationset JSONObject
     * @param changes
     *            set of parameters to update the association set. Optional
     */
    public JSONObject updateAssociationSet(final JSONObject associationSetJSON,
            final JSONObject changes) {
        return associationSet.update(associationSetJSON, changes);
    }

    /**
     * Deletes an association set.
     *
     * DELETE
     * /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param associationSetId
     *            a unique identifier in the form associationset/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteAssociationSet(final String associationSetId) {
        return associationSet.delete(associationSetId);
    }

    /**
     * Deletes an association set.
     *
     * DELETE
     * /andromeda/associationset/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param associationSetJSON
     *            an association set JSONObject.
     *
     */
    public JSONObject deleteAssociationSet(final JSONObject associationSetJSON) {
        return associationSet.delete(associationSetJSON);
    }

    // ################################################################
    // #
    // # Topic Models
    // # https://bigml.com/api/topicmodels
    // #
    // ################################################################

    /**
     * Creates a new topic model.
     *
     * POST /andromeda/topicmodel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            topic model.
     * @param args
     *            set of parameters for the new topic model. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the topic model. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createTopicModel(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        return topicModel.create(datasetId, args, waitTime, retries);
    }

    /**
     * Creates a topic model from a list of `datasets`.
     *
     * POST /andromeda/topicmodel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            topic model.
     * @param args
     *            set of parameters for the new topic model. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the topic model.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createTopicModel(final List datasetsIds, 
    		JSONObject args, Integer waitTime, Integer retries) {

        return topicModel.create(datasetsIds, args, waitTime, retries);
    }

    /**
     * Retrieves a topicmodel.
     *
     * A topic model is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the topic model values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getTopicModel(final String topicModelId) {
        return topicModel.get(topicModelId);
    }

    /**
     * Retrieves a topicmodel.
     *
     * A topic model is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the topic model values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param topicModelJSON
     *            a topicmodel JSONObject.
     *
     */
    public JSONObject getTopicModel(final JSONObject topicModelJSON) {
        return topicModel.get(topicModelJSON);
    }

    /**
     * Check whether a topicmodel's status is FINISHED.
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean topicModelIsReady(final String topicModelId) {
        return topicModel.isReady(topicModelId);
    }

    /**
     * Check whether a topicmodel's status is FINISHED.
     *
     * @param topicModelJSON
     *            a topicmodel JSONObject.
     *
     */
    public boolean topicModelIsReady(final JSONObject topicModelJSON) {
        return topicModel.isReady(topicModelJSON);
    }

    /**
     * Lists all your topic models.
     *
     * GET /andromeda/topicmodel?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listTopicModels(final String queryString) {
        return topicModel.list(queryString);
    }

    /**
     * Updates a topicmodel.
     *
     * PUT
     * /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the topic model. Optional
     *
     */
    public JSONObject updateTopicModel(final String topicModelId, final String changes) {
        return topicModel.update(topicModelId, changes);
    }

    /**
     * Updates a topicmodel.
     *
     * PUT
     * /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicModelJSON
     *            a topicmodel JSONObject
     * @param changes
     *            set of parameters to update the topic model. Optional
     */
    public JSONObject updateTopicModel(final JSONObject topicModelJSON,
            final JSONObject changes) {
        return topicModel.update(topicModelJSON, changes);
    }

    /**
     * Deletes a topicmodel.
     *
     * DELETE
     * /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteTopicModel(final String topicModelId) {
        return topicModel.delete(topicModelId);
    }

    /**
     * Deletes a topicmodel.
     *
     * DELETE
     * /andromeda/topicmodel/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param topicModelJSON
     *            a topicmodel JSONObject.
     *
     */
    public JSONObject deleteTopicModel(final JSONObject topicModelJSON) {
        return topicModel.delete(topicModelJSON);
    }

    // ################################################################
    // #
    // # Topic distributions
    // # https://bigml.com/api/topicdistributions
    // #
    // ################################################################

    /**
     * Creates a new topic distribution.
     *
     * POST
     * /andromeda/topicdistribution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where
     *            id is a string of 24 alpha-numeric chars for the topic model
     *            to attach the topic distribution.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a topic distribution for.
     * @param args
     *            set of parameters for the new topic distribution. Required
     * @param waitTime
     *            time to wait for next check of FINISHED status for topic model
     *            before to start to create the topic distribution. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createTopicDistribution(final String topicModelId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {
        return topicDistribution.create(topicModelId, inputData, args,
                waitTime, retries);
    }

    /**
     * Retrieves a topic distribution.
     *
     * GET /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param topicDistributionId
     *            a unique identifier in the form topicDistribution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getTopicDistribution(final String topicDistributionId) {
        return topicDistribution.get(topicDistributionId);
    }

    /**
     * Retrieves a topic distribution.
     *
     * GET /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param topicDistributionJSON
     *            a topic distribution JSONObject
     *
     */
    public JSONObject getTopicDistribution(final JSONObject topicDistributionJSON) {
        return topicDistribution.get(topicDistributionJSON);
    }

    /**
     * Checks whether a topic distribution's status is FINISHED.
     *
     * @param topicDistributionId
     *            a unique identifier in the form topicDistribution/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean topicDistributionIsReady(final String topicDistributionId) {
        return topicDistribution.isReady(topicDistributionId);
    }

    /**
     * Checks whether a topic distribution's status is FINISHED.
     *
     * @param topicDistributionJSON
     *            a topic distribution JSONObject
     *
     */
    public boolean topicDistributionIsReady(final JSONObject topicDistributionJSON) {
        return topicDistribution.isReady(topicDistributionJSON);
    }

    /**
     * Lists all your topic distributions.
     *
     * GET
     * /andromeda/topicdistribution?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listTopicDistributions(final String queryString) {
        return topicDistribution.list(queryString);
    }

    /**
     * Updates a topic distribution.
     *
     * PUT /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicDistributionId
     *            a unique identifier in the form topicDistribution/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    public JSONObject updateTopicDistribution(final String topicDistributionId,
            final String changes) {
        return topicDistribution.update(topicDistributionId, changes);
    }

    /**
     * Updates a topic distribution.
     *
     * PUT /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicDistributionJSON
     *            am topic distribution JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    public JSONObject updateTopicDistribution(final JSONObject topicDistributionJSON,
            final JSONObject changes) {
        return topicDistribution.update(topicDistributionJSON, changes);
    }

    /**
     * Deletes a topic distribution.
     *
     * DELETE /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param topicDistributionId
     *            a unique identifier in the form topicDistribution/id where id is a
     *            string of 24 alpha-numeric chars
     *
     */
    public JSONObject deleteTopicDistribution(final String topicDistributionId) {
        return topicDistribution.delete(topicDistributionId);
    }

    /**
     * Deletes a topic distribution.
     *
     * DELETE /andromeda/topicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param topicDistributionJSON
     *            a topic distribution JSONObject
     *
     */
    public JSONObject deleteTopicDistribution(final JSONObject topicDistributionJSON) {
        return topicDistribution.delete(topicDistributionJSON);
    }

    // ################################################################
    // #
    // # Batch topic distributions
    // # https://bigml.com/api/batch_topicdistribution
    // #
    // ################################################################

    /**
     * Creates a new batch topic distribution.
     *
     * POST /andromeda/batchtopicdistribution?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param topicModelId
     *            a unique identifier in the form topicmodel/id where
     *            id is a string of 24 alpha-numeric chars for the
     *            topic model to attach the evaluation.
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            batch topic distribution.
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
    public JSONObject createBatchTopicDistribution(final String topicModelId,
            final String datasetId, JSONObject args, Integer waitTime,
            Integer retries) {
        return batchTopicDistribution.create(topicModelId, datasetId, args,
                waitTime, retries);
    }

    /**
     * Retrieves a batch topic distribution.
     *
     * The batch_topic distribution parameter should be a string containing the
     * batch_topic distribution id or the dict returned by create_batch_topic distribution. As
     * batch_topic distribution is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the function will return a dict that
     * encloses the batch_topic distribution values and state info available at the time
     * it is called.
     *
     * GET /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getBatchTopicDistribution(final String batchTopicDistributionId) {
        return batchTopicDistribution.get(batchTopicDistributionId);
    }

    /**
     * Retrieves a batch topic distribution.
     *
     * The batch_topic distribution parameter should be a string containing the
     * batch_topic distribution id or the dict returned by create_batch_topic distribution. As
     * batch_topic distribution is an evolving object that is processed until it reaches
     * the FINISHED or FAULTY state, the function will return a dict that
     * encloses the batch_topic distribution values and state info available at the time
     * it is called.
     *
     * GET /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param batchTopicDistributionJSON
     *            a batch topic distribution JSONObject.
     *
     */
    public JSONObject getBatchTopicDistribution(final JSONObject batchTopicDistributionJSON) {
        return batchTopicDistribution.get(batchTopicDistributionJSON);
    }

    /**
     * Retrieves the batch topic distribution file.
     *
     * Downloads scores, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchTopicDistribution(final String batchTopicDistributionId,
            final String filename) {
        return batchTopicDistribution.downloadBatchTopicDistribution(batchTopicDistributionId,
                filename);
    }

    /**
     * Retrieves the batch topic distribution file.
     *
     * Downloads scores, that are stored in a remote CSV file. If a path is
     * given in filename, the contents of the file are downloaded and saved
     * locally. A file-like object is returned otherwise.
     *
     * @param batchTopicDistributionJSON
     *            a batch topic distribution JSONObject.
     * @param filename
     *            Path to save file locally
     *
     */
    public JSONObject downloadBatchTopicDistribution(
            final JSONObject batchTopicDistributionJSON, final String filename) {
        return batchTopicDistribution.downloadBatchTopicDistribution(batchTopicDistributionJSON,
                filename);
    }

    /**
     * Check whether a batch topic distribution's status is FINISHED.
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public boolean batchTopicDistributionIsReady(final String batchTopicDistributionId) {
        return batchTopicDistribution.isReady(batchTopicDistributionId);
    }

    /**
     * Check whether a batch topic distribution's status is FINISHED.
     *
     * @param batchTopicDistributionJSON
     *            a batch topic distribution JSONObject.
     *
     */
    public boolean batchTopicDistributionIsReady(final JSONObject batchTopicDistributionJSON) {
        return batchTopicDistribution.isReady(batchTopicDistributionJSON);
    }

    /**
     * Lists all your batch topic distributions.
     *
     * GET /andromeda/batchtopicdistribution?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listBatchTopicDistributions(final String queryString) {
        return batchTopicDistribution.list(queryString);
    }

    /**
     * Updates a batch topic distribution.
     *
     * PUT /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is
     *            a string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the batch topic distribution. Optional
     *
     */
    public JSONObject updateBatchTopicDistribution(final String batchTopicDistributionId,
            final String changes) {
        return batchTopicDistribution.update(batchTopicDistributionId, changes);
    }

    /**
     * Updates a batch topic distribution.
     *
     * PUT /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param batchTopicDistributionJSON
     *            a batch topic distribution JSONObject
     * @param changes
     *            set of parameters to update the batch topic distribution. Optional
     */
    public JSONObject updateBatchTopicDistribution(
            final JSONObject batchTopicDistributionJSON, final JSONObject changes) {
        return batchTopicDistribution.update(batchTopicDistributionJSON, changes);
    }

    /**
     * Deletes a batch topic distribution.
     *
     * DELETE /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchTopicDistributionId
     *            a unique identifier in the form batchtopicdistribution/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteBatchTopicDistribution(final String batchTopicDistributionId) {
        return batchTopicDistribution.delete(batchTopicDistributionId);
    }

    /**
     * Deletes a batch topic distribution.
     *
     * DELETE /andromeda/batchtopicdistribution/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param batchTopicDistributionJSON
     *            a batch topic distribution JSONObject.
     *
     */
    public JSONObject deleteBatchTopicDistribution(final JSONObject batchTopicDistributionJSON) {
        return batchTopicDistribution.delete(batchTopicDistributionJSON);
    }

    // ################################################################
    // #
    // # Configurations
    // # https://bigml.com/api/configuration
    // #
    // ################################################################

    /**
     * Creates a remote configuration.
     *
     * Create a new configuration using the arguments in `args`.
     *
     * POST /andromeda/configuration?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param args
     *            set of parameters for the new sample. Optional
     *
     */
    public JSONObject createConfiguration(JSONObject args) {
        return configuration.create(args);
    }

    /**
     * Retrieves a configuration.
     *
     * GET
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a string
     *            of 24 alpha-numeric chars.
     *
     */
    public JSONObject getConfiguration(final String configurationId) {
        return configuration.get(configurationId);
    }

    /**
     * Retrieves a configuration.
     *
     * GET
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param configurationJSON
     *            a configuration JSONObject
     *
     */
    public JSONObject getConfiguration(final JSONObject configurationJSON) {
        return configuration.get(configurationJSON);
    }

    /**
     * Retrieves a configuration.
     *
     *
     * GET /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     *
     */
    public JSONObject getConfiguration(final String configurationId, final String queryString) {
        return getConfiguration(configurationId, queryString, null, null);
    }

    /**
     * Retrieves a configuration.
     *
     *
     * GET /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a string
     *            of 24 alpha-numeric chars.
     * @param queryString
     *            query for filtering.
     * @param apiUser
     *            API user
     * @param apiKey
     *            API key
     *
     */
    public JSONObject getConfiguration(final String configurationId, final String queryString,
                                final String apiUser, final String apiKey) {
        return configuration.get(configurationId, queryString, apiUser, apiKey);
    }

    /**
     * Check whether a configuration's status is FINISHED.
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean configurationIsReady(final String configurationId) {
        return configuration.isReady(configurationId);
    }

    /**
     * Checks whether a configuration's status is FINISHED.
     *
     * @param configurationJSON
     *            a configuration JSONObject
     *
     */
    public boolean configurationIsReady(final JSONObject configurationJSON) {
        return configuration.isReady(configurationJSON);
    }

    /**
     * Lists all your configurations.
     *
     * GET /andromeda/configuration?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listConfigurations(final String queryString) {
        return configuration.list(queryString);
    }

    /**
     * Updates a configuration.
     *
     * PUT
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the configuration. Optional
     *
     */
    public JSONObject updateConfiguration(final String configurationId, final String changes) {
        return configuration.update(configurationId, changes);
    }

    /**
     * Updates a configuration.
     *
     * PUT
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param configurationJSON
     *            a configuration JSONObject
     * @param changes
     *            set of parameters to update the sample. Optional
     *
     */
    public JSONObject updateConfiguration(final JSONObject configurationJSON,
                                   final JSONObject changes) {
        return configuration.update(configurationJSON, changes);
    }

    /**
     * Deletes a configuration.
     *
     * DELETE
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param configurationId
     *            a unique identifier in the form configuration/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteConfiguration(final String configurationId) {
        return configuration.delete(configurationId);
    }

    /**
     * Deletes a configuration.
     *
     * DELETE
     * /andromeda/configuration/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param configurationJSON
     *            a configuration JSONObject
     *
     */
    public JSONObject deleteConfiguration(final JSONObject configurationJSON) {
        return configuration.delete(configurationJSON);
    }


    // ################################################################
    // #
    // # TimeSeries
    // # https://bigml.com/api/timeseries
    // #
    // ################################################################

    /**
     * Creates a new timeseries.
     *
     * POST /andromeda/timeseries?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            timeseries.
     * @param args
     *            set of parameters for the new timeseries. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the timeseries. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createTimeSeries(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        return timeSeries.create(datasetId, args, waitTime, retries);
    }

    /**
     * Creates a timeseries from a list of `datasets`.
     *
     * POST /andromeda/timeseries?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetsIds
     *            list of identifiers in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            timeseries.
     * @param args
     *            set of parameters for the new timeseries. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for source before to start to create the timeseries.
     *            Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createTimeSeries(final List datasetsIds, 
    		JSONObject args, Integer waitTime, Integer retries) {

        return timeSeries.create(datasetsIds, args, waitTime, retries);
    }

    /**
     * Retrieves a timeseries.
     *
     * A timeseries is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the timeseries values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getTimeSeries(final String timeSeriesId) {
        return timeSeries.get(timeSeriesId);
    }

    /**
     * Retrieves a timeseries.
     *
     * A timeseries is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the timeseries values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param timeSeriesJSON
     *            a timeseries JSONObject.
     *
     */
    public JSONObject getTimeSeries(final JSONObject timeSeriesJSON) {
        return timeSeries.get(timeSeriesJSON);
    }

    /**
     * Check whether a timeseries' status is FINISHED.
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean timeSeriesIsReady(final String timeSeriesId) {
        return timeSeries.isReady(timeSeriesId);
    }

    /**
     * Check whether a timeseries' status is FINISHED.
     *
     * @param timeSeriesJSON
     *            a timeseries JSONObject.
     *
     */
    public boolean timeSeriesIsReady(final JSONObject timeSeriesJSON) {
        return timeSeries.isReady(timeSeriesJSON);
    }

    /**
     * Lists all your timeseries.
     *
     * GET /andromeda/timeseries?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listTimeSeries(final String queryString) {
        return timeSeries.list(queryString);
    }

    /**
     * Updates a timeseries.
     *
     * PUT
     * /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the timeseries. Optional
     *
     */
    public JSONObject updateTimeSeries(final String timeSeriesId, final String changes) {
        return timeSeries.update(timeSeriesId, changes);
    }

    /**
     * Updates a timeseries.
     *
     * PUT
     * /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param timeSeriesJSON
     *            a timeseries JSONObject
     * @param changes
     *            set of parameters to update the timeseries. Optional
     */
    public JSONObject updateTimeSeries(final JSONObject timeSeriesJSON,
            final JSONObject changes) {
        return timeSeries.update(timeSeriesJSON, changes);
    }

    /**
     * Deletes a timeseries.
     *
     * DELETE
     * /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteTimeSeries(final String timeSeriesId) {
        return timeSeries.delete(timeSeriesId);
    }

    /**
     * Deletes a timeseries.
     *
     * DELETE
     * /andromeda/timeseries/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param timeSeriesJSON
     *            a timeseries JSONObject.
     *
     */
    public JSONObject deleteTimeSeries(final JSONObject timeSeriesJSON) {
        return timeSeries.delete(timeSeriesJSON);
    }


    // ################################################################
    // #
    // # Forecasts
    // # https://bigml.com/api/forecasts
    // #
    // ################################################################

    /**
     * Creates a new forecast.
     *
     * POST
     * /andromeda/forecast?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param timeSeriesId
     *            a unique identifier in the form timeseries/id where
     *            id is a string of 24 alpha-numeric chars for the tiemseries
     *            to attach the forecast.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a forecast for.
     * @param args
     *            set of parameters for the new forecast. Required
     * @param waitTime
     *            time to wait for next check of FINISHED status for timeseries
     *            before to start to create the forecast. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createForecast(final String timeSeriesId,
            JSONObject inputData, JSONObject args,
            Integer waitTime, Integer retries) {
        return forecast.create(timeSeriesId, inputData, args,
                waitTime, retries);
    }

    /**
     * Retrieves a forecast.
     *
     * GET /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param forecastId
     *            a unique identifier in the form forecast/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getForecast(final String forecastId) {
        return forecast.get(forecastId);
    }

    /**
     * Retrieves a forecast.
     *
     * GET /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param forecastJSON
     *            a forecast JSONObject
     *
     */
    public JSONObject getForecast(final JSONObject forecastJSON) {
        return forecast.get(forecastJSON);
    }

    /**
     * Checks whether a forecast's status is FINISHED.
     *
     * @param forecastId
     *            a unique identifier in the form forecast/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean forecastIsReady(final String forecastId) {
        return forecast.isReady(forecastId);
    }

    /**
     * Checks whether a forecast's status is FINISHED.
     *
     * @param forecastJSON  a forecast JSONObject
     *
     */
    public boolean forecastIsReady(final JSONObject forecastJSON) {
        return forecast.isReady(forecastJSON);
    }

    /**
     * Lists all your forecasts.
     *
     * GET
     * /andromeda/forecast?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listForecasts(final String queryString) {
        return forecast.list(queryString);
    }

    /**
     * Updates a forecast.
     *
     * PUT /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param forecastId
     *            a unique identifier in the form forecast/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the forecast. Optional
     *
     */
    public JSONObject updateForecast(final String forecastId,
            final String changes) {
        return forecast.update(forecastId, changes);
    }

    /**
     * Updates a forecast.
     *
     * PUT /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param forecastJSON
     *            am forecast JSONObject
     * @param changes
     *            set of parameters to update the forecast. Optional
     *
     */
    public JSONObject updateForecast(final JSONObject forecastJSON,
            final JSONObject changes) {
        return forecast.update(forecastJSON, changes);
    }

    /**
     * Deletes a forecast.
     *
     * DELETE /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param forecastId
     *            a unique identifier in the form forecast/id where id is a
     *            string of 24 alpha-numeric chars
     *
     */
    public JSONObject deleteForecast(final String forecastId) {
        return forecast.delete(forecastId);
    }

    /**
     * Deletes a forecast.
     *
     * DELETE /andromeda/forecast/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param forecastJSON
     *            a forecast JSONObject
     *
     */
    public JSONObject deleteForecast(final JSONObject forecastJSON) {
        return forecast.delete(forecastJSON);
    }


    // ################################################################
    // #
    // # Deepnets
    // # https://bigml.com/api/deepnets
    // #
    // ################################################################

    /**
     * Creates a new deepnet.
     *
     * POST /andromeda/deepnet?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is a
     *            string of 24 alpha-numeric chars for the dataset to attach the
     *            deepnet.
     * @param args
     *            set of parameters for the new deepnet. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for dataset
     *            before to start to create the deepnet. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createDeepnet(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        return deepnet.create(datasetId, args, waitTime, retries);
    }

    /**
     * Retrieves a deepnet.
     *
     * A deepnet is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the deepnet values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/deepnet/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param deepnetId
     *            a unique identifier in the form deepnet/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getDeepnet(final String deepnetId) {
        return deepnet.get(deepnetId);
    }

    /**
     * Retrieves a deepnet.
     *
     * A deepnet is an evolving object that is processed until it reaches the
     * FINISHED or FAULTY state, the method will return a JSONObject that
     * encloses the deepnet values and state info available at the time it is
     * called.
     *
     * GET
     * /andromeda/deepnet/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param deepnetJSON
     *            a deepnet JSONObject.
     *
     */
    public JSONObject getDeepnet(final JSONObject deepnetJSON) {
        return deepnet.get(deepnetJSON);
    }

    /**
     * Check whether a deepnet's status is FINISHED.
     *
     * @param deepnetId
     *            a unique identifier in the form deepnet/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean deepnetIsReady(final String deepnetId) {
        return deepnet.isReady(deepnetId);
    }

    /**
     * Check whether a deepnet's status is FINISHED.
     *
     * @param deepnetJSON
     *            a deepnet JSONObject.
     *
     */
    public boolean deepnetIsReady(final JSONObject deepnetJSON) {
        return deepnet.isReady(deepnetJSON);
    }

    /**
     * Lists all your deepnet.
     *
     * GET /andromeda/deepnet?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listDeepnets(final String queryString) {
        return deepnet.list(queryString);
    }

    /**
     * Updates a deepnet.
     *
     * PUT
     * /andromeda/deepnet/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param deepnetId
     *            a unique identifier in the form deepnet/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the deepnet. Optional
     *
     */
    public JSONObject updateDeepnet(final String deepnetId, final String changes) {
        return deepnet.update(deepnetId, changes);
    }

    /**
     * Updates a deepnet.
     *
     * PUT
     * /andromeda/deepnet/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param deepnetJSON
     *            a deepnet JSONObject
     * @param changes
     *            set of parameters to update the deepnet. Optional
     */
    public JSONObject updateDeepnet(final JSONObject deepnetJSON,
            final JSONObject changes) {
        return deepnet.update(deepnetJSON, changes);
    }

    /**
     * Deletes a deepnet.
     *
     * DELETE
     * /andromeda/deepnet/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param deepnetId
     *            a unique identifier in the form deepnet/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteDeepnet(final String deepnetId) {
        return deepnet.delete(deepnetId);
    }

    /**
     * Deletes a deepnet.
     *
     * DELETE
     * /andromeda/deepnet/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param deepnetJSON
     *            a deepnet JSONObject.
     *
     */
    public JSONObject deleteDeepnet(final JSONObject deepnetJSON) {
        return deepnet.delete(deepnetJSON);
    }

    
    // ################################################################
    // #
    // # OptiMLs
    // # https://bigml.com/api/optimls
    // #
    // ################################################################

    /**
     * Creates a new optiml.
     *
     * POST /andromeda/optiml?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param datasetId
     *            a unique identifier in the form dataset/id where id is
     *            a string of 24 alpha-numeric chars for the dataset to 
     *            attach the optiML.
     * @param args
     *            set of parameters for the new optiml. Optional
     * @param waitTime
     *            time to wait for next check of FINISHED status for 
     *            dataset before to start to create the optiml. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createOptiML(final String datasetId, JSONObject args,
            Integer waitTime, Integer retries) {

        return optiml.create(datasetId, args, waitTime, retries);
    }
    

    /**
     * Retrieves an optiML.
     *
     * An optiML is an evolving object that is processed until it reaches 
     * the FINISHED or FAULTY state, the method will return a JSONObject 
     * that encloses the optiML values and state info available at the 
     * time it is called.
     *
     * GET
     * /andromeda/optiml/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param optimlId
     *            a unique identifier in the form optiml/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getOptiML(final String optimlId) {
        return optiml.get(optimlId);
    }

    /**
     * Retrieves an optiML.
     *
     * An optiML is an evolving object that is processed until it reaches 
     * the FINISHED or FAULTY state, the method will return a JSONObject 
     * that encloses the optiml values and state info available at the 
     * time it is called.
     *
     * GET
     * /andromeda/optiml/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io
     *
     * @param optimlJSON
     *            an optiML JSONObject.
     *
     */
    public JSONObject getOptiML(final JSONObject optimlJSON) {
        return optiml.get(optimlJSON);
    }

    
    /**
     * Check whether an optiML's status is FINISHED.
     *
     * @param optimlId
     *            a unique identifier in the form optiml/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public boolean optiMLIsReady(final String optimlId) {
        return optiml.isReady(optimlId);
    }

    /**
     * Check whether an optiML's status is FINISHED.
     *
     * @param optimlJSON
     *            an optiml JSONObject.
     *
     */
    public boolean optimlIsReady(final JSONObject optimlJSON) {
        return optiml.isReady(optimlJSON);
    }    
    
    /**
     * Lists all your optiMLs.
     *
     * GET /andromeda/optiml?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listOptiMLs(final String queryString) {
        return optiml.list(queryString);
    }
    
    /**
     * Updates an optiml.
     *
     * PUT
     * /andromeda/optiml/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param optimlId
     *            a unique identifier in the form optiml/id where id is 
     *            a string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the optiml. Optional
     *
     */
    public JSONObject updateOptiML(final String optimlId, 
    			final String changes) {
        return optiml.update(optimlId, changes);
    }

    /**
     * Updates an optiML.
     *
     * PUT
     * /andromeda/optiml/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param optimlJSON
     *            an optiml JSONObject
     * @param changes
     *            set of parameters to update the optiml. Optional
     */
    public JSONObject updateOptiML(final JSONObject optimlJSON,
            final JSONObject changes) {
        return optiml.update(optimlJSON, changes);
    }    
    
    /**
     * Deletes an optiML.
     *
     * DELETE
     * /andromeda/optiml/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param optimlId
     *            a unique identifier in the form optiml/id where id is
     *            a string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteOptiML(final String optimlId) {
        return optiml.delete(optimlId);
    }

    /**
     * Deletes an optiML.
     *
     * DELETE
     * /andromeda/optiml/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param optimlJSON
     *            an optiml JSONObject.
     *
     */
    public JSONObject deleteOptiML(final JSONObject optimlJSON) {
        return optiml.delete(optimlJSON);
    }
    
    
 // ################################################################
    // #
    // # Fusion
    // # https://bigml.com/api/fusions
    // #
    // ################################################################

    /**
     * Creates a fusion from a list of models.
     *
     * POST /andromeda/fusion?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param modelsIds
     *            list of identifiers in the form xxx/id, where xxx is
     *            one of the model types availables and id is a string
     *            of 24 alpha-numeric chars for the model to include in
     *            the fusion resource.
     * @param args
     *            set of parameters for the new fusion. Optional
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED 
     *            status for every submodel before to start to create 
     *            the fusion. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject createFusion(final List<String> modelsIds,
        JSONObject args, Integer waitTime, Integer retries) {

        return fusion.create(modelsIds, args, waitTime, retries);
    }

    /**
     * Retrieves a fusion.
     *
     * GET
     * /andromeda/fusion/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param fusionId
     *            a unique identifier in the form fusion/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject getFusion(final String fusionId) {
        return fusion.get(fusionId);
    }

    /**
     * Retrieves a fusion.
     *
     * GET
     * /andromeda/fusion/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param fusionJSON
     *            a fusion JSONObject
     *
     */
    public JSONObject getFusion(final JSONObject fusionJSON) {
        return fusion.get(fusionJSON);
    }

    /**
     * Checks whether a fusion's status is FINISHED.
     *
     * @param fusionId
     *            a unique identifier in the form fusion/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public boolean fusionIsReady(final String fusionId) {
        return fusion.isReady(fusionId);
    }

    /**
     * Checks whether a fusion's status is FINISHED.
     *
     * @param fusionJSON
     *            a fusion JSONObject
     *
     */
    public boolean fusionIsReady(final JSONObject fusionJSON) {
        return fusion.isReady(fusionJSON);
    }

    /**
     * Lists all your fusion.
     *
     * GET /andromeda/fusion?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    public JSONObject listFusions(final String queryString) {
        return fusion.list(queryString);
    }

    /**
     * Updates a fusion.
     *
     * PUT
     * /andromeda/fusion/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param fusionId
     *            a unique identifier in the form fusion/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the fusion. Optional
     *
     */
    public JSONObject updateFusion(final String fusionId, final String changes) {
        return fusion.update(fusionId, changes);
    }

    /**
     * Updates a fusion.
     *
     * PUT
     * /andromeda/fusion/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param fusionJSON
     *            a fusion JSONObject
     * @param changes
     *            set of parameters to update the fusion. Optional
     *
     */
    public JSONObject updateFusion(final JSONObject fusionJSON,
            final JSONObject changes) {
        return fusion.update(fusionJSON, changes);
    }

    /**
     * Deletes a fusion.
     *
     * DELETE
     * /andromeda/fusion/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param fusionId
     *            a unique identifier in the form fusion/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    public JSONObject deleteFusion(final String fusionId) {
        return fusion.delete(fusionId);
    }

    /**
     * Deletes a fusion.
     *
     * DELETE
     * /andromeda/fusion/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1
     *
     * @param fusionJSON
     *            a fusion JSONObject
     *
     */
    public JSONObject deleteFusion(final JSONObject fusionJSON) {
        return fusion.delete(fusionJSON);
    }
}
