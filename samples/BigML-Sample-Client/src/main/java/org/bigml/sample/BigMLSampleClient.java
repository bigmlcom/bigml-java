/**
 * Requires BigML Java bindings
 * You can get it from: {@link https://github.com/bigmlcom/bigml-java}
 *     - Clone it: <code>git clone https://github.com/bigmlcom/bigml-java.git</code>
 *     - Or download it from: {@link https://github.com/bigmlcom/bigml-java/archive/master.zip}
 *
 * Once you have the source code, you can install it using Maven:
 *     <code>mvn install</code>
 *
 * or just include it in your project class path.
 *
 */
package org.bigml.sample;

import java.util.HashMap;
import java.util.Map;

import org.bigml.binding.AuthenticationException;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.InputDataParseException;
import org.bigml.binding.LocalPredictiveModel;
import org.bigml.binding.MissingStrategy;
import org.bigml.binding.localmodel.Prediction;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BigMLSampleClient {

    // Set BigML's DEV mode to true if you want to work in development mode
    private static final boolean DEV_MODE = false;
    // Set it to false if you don't want console messages
    private static final boolean DEBUG = true;
    // Used in debugging messages to point to the created resources in the Web Dashboard
    private static final String DASHBOARD_URL = "https://bigml.com/dashboard/";

    /**
     * A simple Java Class to integrate the BigML API
     *
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        /**
         * To work with BigML's API you need a connection object that
         * will handle the API requests. This object will be common to all
         * the examples in this file
         */

        // The BigMLClient class stores the information that you need to
        // access BigML's API:
        //   - API domain (default: bigml.io)
        //   - credentials (BIGML_USERNAME and BIGML_API_KEY)
        //   - environment (development mode or production mode)
        // Its methods are wrappers to the REST API calls

        BigMLClient api = null;
        try {

            // Instantiating BigMLClient with the properties BIGML_USERNAME and
            // BIGML_API_KEY that should previously be set the
            // binding.properties file

            api = BigMLClient.getInstance(DEV_MODE);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return;
        }

        // * First example: Prediction workflow
        predictionWorkflow(api);

        if (DEBUG) {
            System.out.println("* Preparing for examples: Creating a Dataset");
        }

        // Common part for all workflows: Creating a Source and a Dataset
        // from a local file
        JSONObject dataset = createDataset(api, "data/iris.csv");

        if (DEBUG) { // auxiliary console message
            System.out.println("Dataset created: " +
                    DASHBOARD_URL + (String)dataset.get("resource"));
        }

        // * Second example: Local Predictions
        // To create a local prediction, you will need an existing model.
        // This is going to be the JSON that the LocalPredictiveModel
        // needs to predict

        JSONObject model = createModel(api, dataset);
        predictLocally(model);

        // * Third example: Evaluation Workflow
        evaluationWorkflow(api, dataset);

        // * Fourth example: Creating unsupervised models
        creatingUnsupervisedModels(api, dataset);

        // * Fifth example: Creating Topic Distribution
        topicDistributionWorkflow(api);

        // * Sixth example: Changing the field types properties
        changingFields(api);

        // * Seventh example: Creating a simple WhizzML script and executing it
        runWhizzML(api);


        System.out.println("BigML sample finished.");
        if (DEV_MODE) {
            System.out
                    .println("*** Remember that the resources created in this "
                            + "sample are placed in your DEV mode environment at BigML.com ***");
        }
    }

    public static void predictionWorkflow(final BigMLClient api) {
        /**
         * Prediction workflow
         *
         * This example shows a complete prediction workflow
         * The steps to create a prediction are:
         * 1. Create a `Source` from your local or remote data
         * 2. Create a `Dataset` from the `Source`
         * 3. Create a `Model` from the `Dataset` (decision tree)
         * 4. Create a `Prediction` from the `Model` for new `input data`
         *
         * To follow these steps, you need to create a connection to the
         * BigML API, that will handle these creation calls.
         */
        if (DEBUG) {
            System.out.println("* First example: Prediction Workflow");
        }

        // Creating a `Source` by uploading a local file
        // ---------------------------------------------

        // Defining the arguments that we'll use when creating the Source
        JSONObject sourceParser = new JSONObject();
        JSONArray missingTokens = new JSONArray();
        // Modifying the source parser attribute by
        // setting the missingTokens to the string "N/A"
        missingTokens.add("N/A");
        sourceParser.put("missing_tokens", missingTokens);

        JSONObject emptyArgs = null; // the rest of arguments are by default

        // Source creation
        JSONObject source = api.createSource("data/iris.csv", // local file
                                             "Iris Source",   // Source name
                                             sourceParser,    // parsing args
                                             emptyArgs);      // rest of args

        // Source creation is asynchronous. Wait for it to be finished.
        while (!api.sourceIsReady(source)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        source = api.getSource(source);

        if (DEBUG) { // auxiliary console message
            System.out.println("Source created: " +
                    DASHBOARD_URL + (String)source.get("resource"));
        }

        // This code is not needed in the Prediction workflow, but can be
        // useful to inspect the inferred fields structure:
        // JSONObject fields = (JSONObject) Utils.getJSONObject(source,
        //                                                      "object.fields");
        // System.out.print(fields);

        // Creating a `Dataset` from the previous `Source`
        // -----------------------------------------------
        // First argument is the `Source`ID

        JSONObject dataset = api.createDataset((String) source.get("resource"),
                                               emptyArgs, // creation args
                                               null,      // wait for Source
                                               null);     // retries

        // Dataset creation is asynchronous. Wait for it to be finished.
        while (!api.datasetIsReady(dataset)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        dataset = api.getDataset(dataset);

        if (DEBUG) { // auxiliary console message
            System.out.println("Dataset created: " +
                    DASHBOARD_URL + (String)dataset.get("resource"));
        }

        // This code is not needed in the Prediction workflow, but can be
        // useful to inspect the dataset fields structure:
        // JSONObject fields = (JSONObject) Utils.getJSONObject(dataset,
        //                                                      "object.fields");
        // System.out.print(fields);

        // Creating a `Model` from the previous `Dataset`
        // -----------------------------------------------
        // First argument is the `Dataset`ID
        // Second argument contains the configuration arguments for the model
        // In predictive models it is mandatory to specify the `objective_field`
        // (the name or ID of the field to be predicted). If that's not set,
        // BigML will use the last categorical or numeric field in your dataset
        // as `objective field`.

        JSONObject modelConf = new JSONObject();
        modelConf.put("objective_field", "species"); // this model should predict `species` and
        modelConf.put("balance_objective", true);    // the instances in your dataset will be
                                                     // automatically balanced to build the
                                                     // the model

        JSONObject model = api.createModel((String) dataset.get("resource"),
                                           modelConf, // creation args
                                           null,      // wait for Dataset
                                           null);     // retries

        // Model creation is asynchronous. Wait for it to be finished.
        while (!api.modelIsReady(model)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        model = api.getModel(model);

        if (DEBUG) { // auxiliary console message
            System.out.println("Model created: " +
                    DASHBOARD_URL + (String)model.get("resource"));
        }

        // This code is not needed in the Prediction workflow, but can be
        // useful to inspect the model fields structure:
        // fields = (JSONObject) Utils.getJSONObject(model,
        //                                          "object.model.fields");
        // System.out.print(fields);

        // And this would print the tree structure in the model
        // JSONObject tree = (JSONObject) Utils.getJSONObject(model, "object.model.root");
        // System.out.print(tree);


        // Create a `Prediction` using the previous `Model`
        // ------------------------------------------------
        // First argument is the `Model`ID
        // Second argument is the `Input data`

        // Input data object: We can build the input data object using
        // BigML's field IDs (e.g. "000001") as keys or, like here, the
        // field name
        JSONObject inputData = new JSONObject();
        inputData.put("sepal length", 5);
        inputData.put("sepal width", 2.5);

        // This is a remote prediction, that is a prediction made by BigML and
        // it will consume BigML credits (~0.01)
        JSONObject remotePrediction = api.createPrediction(
                (String) model.get("resource"), // model ID
                inputData,                      // data to predict for
                emptyArgs,                      // model creation args
                null,                           // wait for dataset
                null);                          // retries

        // Predictions are synchronous, you can immediately get the results.
        // No need to wait.

        if (DEBUG) { // auxiliary console message
            System.out.println("Prediction created: " +
                    DASHBOARD_URL + (String)remotePrediction.get("resource"));
        }

        // Extracting the prediction information from the `Prediction` object
        String predictionOutput = (String) Utils.getJSONObject(
                remotePrediction, "object.output");
        System.out.println("Remote prediction result: " + predictionOutput);
    }

    public static JSONObject createDataset(final BigMLClient api,
                                           final String filename) {

        JSONObject emptyArgs = null;
        // Source creation
        JSONObject source = api.createSource(filename, // local file
                                             "Example Source",   // Source name
                                             emptyArgs,       // parsing args
                                             emptyArgs);      // rest of args

        // Source creation is asynchronous. Wait for it to be finished.
        while (!api.sourceIsReady(source)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        source = api.getSource(source);

        if (DEBUG) { // auxiliary console message
            System.out.println("Source created: " +
                    DASHBOARD_URL + (String)source.get("resource"));
        }

        JSONObject dataset = api.createDataset((String) source.get("resource"),
                                               emptyArgs, // creation args
                                               null,      // wait for Source
                                               null);     // retries

        // Dataset creation is asynchronous. Wait for it to be finished.
        while (!api.datasetIsReady(dataset)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // obtain the finished resource
        return api.getDataset(dataset);
    }

    public static JSONObject createModel(final BigMLClient api,
                                         final JSONObject dataset) {

        JSONObject emptyArgs = null;
        // First argument is the `Dataset`ID
        JSONObject model = api.createModel((String) dataset.get("resource"),
                                           emptyArgs, // creation args
                                           null,      // wait for Dataset
                                           null);     // retries

        // Model creation is asynchronous. Wait for it to be finished.
        while (!api.modelIsReady(model)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        return api.getModel(model);
    }

    public static void predictLocally(final JSONObject model) {

        /**
         * Creating local predictions
         *
         * It is possible to build a LocalPredictiveModel using a previously
         * created model. A LocalPredictiveModel allows you to predict in your
         * own machines, with no latencies, as there will be no
         * HTTPS round trips per prediction, and for FREE, this won't
         * consume BigML's credits.
         *
         * @see org.bigml.binding.LocalPredictiveModel
         */

        if (DEBUG) {
            System.out.println("* Second example: Predicting locally");
        }

        // The `LocalPredictiveModel` is the class that encapsulates the
        // `Model` information and provides the `predict` method.
        // The `Prediction` class stores the local prediction result

        LocalPredictiveModel localModel = null;
        Prediction localPrediction = null;

        // Input data object: We can build the input data object using
        // BigML's field IDs (e.g. "000001") as keys or, like here, the
        // field name
        JSONObject inputData = new JSONObject();
        inputData.put("sepal length", 5);
        inputData.put("sepal width", 2.5);

        try {
            localModel = new LocalPredictiveModel(
                    (JSONObject) model.get("object"));

            if (DEBUG) { // auxiliary console message
                System.out.println("Model downloaded: " +
                        DASHBOARD_URL + (String)model.get("resource"));
                System.out.println("Ready to predict locally.");
            }

            // Using the `predict` method to predict
            localPrediction = localModel.predict(
                    inputData,                     // data to predict for
                    MissingStrategy.PROPORTIONAL); // strategy for missings

            System.out.println("Local prediction result: "
                    + localPrediction.get("prediction") + ". With confidence: "
                    + localPrediction.get("confidence"));

        } catch (InputDataParseException e) {
            System.err.println("inputData can't be converted into a String");
            e.printStackTrace();
        } catch (Exception e) {
            System.err
                    .println("The model can't be converted into a LocalPredictiveModel");
            e.printStackTrace();
        }
    }

    public static void evaluationWorkflow(final BigMLClient api,
                                          final JSONObject dataset) {

        /**
         * Evaluation of a model
         *
         * Example of an evaluation with a 80-20% split of your `Dataset`.
         * The model is created with the 80% of data and the evaluation
         * is done using the complementary 20%
         *
         */

        if (DEBUG) {
            System.out.println("* Third example: Evaluating a Model");
        }

        // Creating a training `Model` from the previous `Dataset`
        // -------------------------------------------------------
        // First argument is the `Dataset`ID
        // Use 80% of data setting `sample_rate` to 0.8 and `seed`
        // to a particular string to do deterministic sampling

        JSONObject samplingArgs = new JSONObject();
        samplingArgs.put("sample_rate", 0.8);
        samplingArgs.put("seed", "My seed");

        JSONObject model = api.createModel((String) dataset.get("resource"),
                                           samplingArgs, // creation args
                                           null,         // wait for Dataset
                                           null);        // retries

        // Model creation is asynchronous. Wait for it to be finished.
        while (!api.modelIsReady(model)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        model = api.getModel(model);

        if (DEBUG) { // auxiliary console message
            System.out.println("Model created: " +
                    DASHBOARD_URL + (String)model.get("resource"));
        }

        // Creating an `Evaluation` from the previous sampled `Dataset`
        // ------------------------------------------------------------
        // First argument is the `Model`ID
        // Second argument is the `Dataset`ID
        // Use the complementary 20% of data by
        // setting `sample_rate` to 0.8 and `out_of_bag` to true. The `seed`
        // must be the same in the model to ensure complementarity


        JSONObject outOfBagArgs = new JSONObject();
        outOfBagArgs.put("sample_rate", 0.8);
        outOfBagArgs.put("seed", "My seed");
        outOfBagArgs.put("out_of_bag", true);
        JSONObject evaluation = api.createEvaluation(
                (String)model.get("resource"),   // model ID
                (String)dataset.get("resource"), // dataset ID
                outOfBagArgs,                    // test sampling args
                null,                            // wait for model and dataset
                null);                           // retries


        // Evaluation creation is asynchronous. Wait for it to be finished.
        while (!api.evaluationIsReady(evaluation)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // obtain the finished resource
        evaluation = api.getEvaluation(evaluation);

        if (DEBUG) { // auxiliary console message
            System.out.println("Evaluation created: " +
                    DASHBOARD_URL + (String)evaluation.get("resource"));
        }

        // The result of the evaluation. It contains the model's performance
        // compared to predicting the mode or predicting at random
        // JSONObject result = (JSONObject) Utils.getJSONObject(evaluation,
        //                                                      "object.result");
        // System.out.print(result);
    }

        /**
         * Creating unsupervised models: `Cluster` and `Anomaly`
         *
         * Example to create Clusters and an Anomaly detector
         *
         */

        // Creating a `Cluster` from the original `Dataset`
        // ------------------------------------------------
        // First argument is the `Dataset`ID

    public static void creatingUnsupervisedModels(final BigMLClient api,
                                                  final JSONObject dataset) {


        if (DEBUG) {
            System.out.println("* Fourth example: Creating unsupervised models");
        }

        JSONObject emptyArgs = null; // the arguments are by default

        JSONObject cluster = api.createCluster(
                (String) dataset.get("resource"), // dataset ID
                emptyArgs,                        // creation args
                null,                             // wait for dataset
                null);                            // retries

        // Cluster creation is asynchronous. Wait for it to be finished.
        while (!api.clusterIsReady(cluster)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // obtain the finished resource
        cluster = api.getCluster(cluster);

        if (DEBUG) { // auxiliary console message
            System.out.println("Cluster created: " +
                    DASHBOARD_URL + (String)cluster.get("resource"));
        }

        // The  `Cluster` properties describe the groups defined in the
        // clustering procedure
        // JSONObject object = (JSONObject) Utils.getJSONObject(cluster, "object");
        //  System.out.print(object);

        // Creating an `Anomaly` detector from the original `Dataset`
        // ----------------------------------------------------------
        // First argument is the `Dataset`ID

        JSONObject anomaly = api.createAnomaly(
                (String) dataset.get("resource"), // dataset ID
                emptyArgs,                        // creation args
                null,                             // wait for dataset
                null);                            // retries

        // Anomaly creation is asynchronous. Wait for it to be finished.
        while (!api.anomalyIsReady(anomaly)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // obtain the finished resource
        anomaly = api.getAnomaly(anomaly);

        if (DEBUG) { // auxiliary console message
            System.out.println("Anomaly created: " +
                    DASHBOARD_URL + (String)anomaly.get("resource"));
        }

        // The `Anomaly` object will contain the top anomalies detected
        // object = (JSONObject) Utils.getJSONObject(anomaly, "object");
        // System.out.print(object);
    }

    public static void topicDistributionWorkflow(final BigMLClient api) {

        /**
         * Creating a Topic Distribution from a local file
         * This examples shows how to predict which topics are related to
         * a particular text document
         * The steps to create a Topic Distribution are:
         * 1. Create a `Source` from your local or remote data
         * 2. Create a `Dataset` from the `Source`
         * 3. Create a `Topic Model` from the `Dataset`
         * 4. Create a `Topic Distribution` using the document input data
         *
         * To follow these steps, you need a connection to the
         * BigML API, that will handle these creation calls. That's been passed
         * as the method argument.
         */


        if (DEBUG) {
            System.out.println("* Fifth example: Topic Distribution Workflow");
        }

        JSONObject emptyArgs = null;

        // Create a datasource for topic model
        JSONObject source = api.createSource("data/airbnb.csv", "Airbnb Source", null, null);

        while (!api.sourceIsReady(source)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        JSONObject changes = new JSONObject(){{
            put("fields", new HashMap<String, Map>() {{
                put("000005", new HashMap<String, String>() {{
                    put("optype", "text");
                }});
            }});
        }};

        api.updateSource(source, changes);
        source = api.getSource(source);

        if (DEBUG) { // auxiliary console message
            System.out.println("Source created: " +
                    DASHBOARD_URL + (String)source.get("resource"));
        }

        // Create a dataset
        JSONObject dataset = api.createDataset((String) source.get("resource"),
                emptyArgs, null, null);

        while (!api.datasetIsReady(dataset)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        dataset = api.getDataset(dataset);

        if (DEBUG) { // auxiliary console message
            System.out.println("Dataset created:" +
                    DASHBOARD_URL + (String)dataset.get("resource"));
        }

        // Create a Topic Model using the dataset created above
        JSONObject topicModel = api.createTopicModel((String) dataset.get("resource"),
                emptyArgs, null, null);

        while (!api.topicModelIsReady(topicModel)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        topicModel = api.getTopicModel(topicModel);

        if (DEBUG) { // auxiliary console message
            System.out.println("Topic Model created: " +
                    DASHBOARD_URL + (String)topicModel.get("resource"));
        }

        // Providing the text for the document that we need to analyze
        JSONObject inputData = new JSONObject();
        inputData.put("000005", "hotel shower double heater");

        // This is a remote topic distribution
        JSONObject remoteTopicDistribution = api.createTopicDistribution(
                (String) topicModel.get("resource"), inputData, emptyArgs,
                null, null);
        remoteTopicDistribution = api.getTopicDistribution(remoteTopicDistribution);

        if (DEBUG) { // auxiliary console message
            System.out.println("Topic Distribution created:" +
                    DASHBOARD_URL + (String)remoteTopicDistribution.get("resource"));
        }

        // The Topic Distribution object will contain information about the
        // Topics related to the text in your input data
        // JSONObject distributionOutput = (JSONObject) Utils.getJSONObject(
        //       remoteTopicDistribution, "object.topic_distribution");
        // System.out.println(distributionOutput);
    }

    public static void changingFields(final BigMLClient api) {
        /**
         * Changing the properties of a field, like the type it has been assigned,
         * is done by updating the `Source` resource in BigML.
         * The example shows how to:
         * 1. Create a `Source` from your local data
         * 2. Change the field's type (or associated properties) for some of the fields
         *
         * To follow these steps, you need a connection to the
         * BigML API, that will handle these creation calls. That's been passed
         * as the method argument.
         */

        if (DEBUG) {
            System.out.println("* Sixth example: Changing fields' properties");
        }

        JSONObject emptyArgs = null;
        // Source creation
        JSONObject source = api.createSource("data/airbnb.csv", // local file
                                             "Fields change example Source", // Source name
                                             emptyArgs,                      // parsing args
                                             emptyArgs);                     // rest of args

        // Source creation is asynchronous. Wait for it to be finished.
        while (!api.sourceIsReady(source)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        source = api.getSource(source);

        if (DEBUG) { // auxiliary console message
            System.out.println("Source created: " +
                    DASHBOARD_URL + (String)source.get("resource"));
        }

        // Change the field types: the first field is changed to `categorical` and
        // the text field options are also modified for the `comments` field (field ID "000005")
        // for the analysis of the field to be using the text bag-of-words case sensitive method.
        String changes = "{\"fields\": {\"000000\": {\"optype\": \"categorical\"}," +
                         "\"000005\":{\"optype\": \"text\"," +
                         " \"term_analysis\": {\"case_sensitive\": true}}}}";
        // update call
        source = api.updateSource(
                (String) source.get("resource"), // source ID
                changes);                        // updated attributes
        // wait for the changes to be applied
        while (!api.sourceIsReady(source)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        source = api.getSource(source);

        if (DEBUG) { // auxiliary console message
            System.out.println("Source updated: " +
                    DASHBOARD_URL + (String)source.get("resource"));
        }

        // Creating a dataset from this source, we get some non-preferred fields
        JSONObject dataset = api.createDataset((String) source.get("resource"),
                emptyArgs, // creation args
                null,      // wait for Source
                null);     // retries

        // Dataset creation is asynchronous. Wait for it to be finished.
        while (!api.datasetIsReady(dataset)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (DEBUG) { // auxiliary console message
            System.out.println("Dataset updated: " +
                    DASHBOARD_URL + (String)dataset.get("resource"));
        }

        // Changing the field `reviewer_name` (field ID "000004") to preferred
        changes = "{\"fields\": {\"000004\": {\"preferred\": true}}}";
        // update call
        dataset = api.updateDataset(
                (String) dataset.get("resource"), // dataset ID
                changes);                         // updated attributes
        // wait for the changes to be applied
        while (!api.datasetIsReady(dataset)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        dataset = api.getDataset(dataset);

    }

    public static void runWhizzML(final BigMLClient api) {
        /**
         * Creating a simple WhizzML script which adds two variables and executing
         * it. The example shows how to:
         * 1. Create a `Script` and defining its inputs and outputs
         * 2. Create an `Execution` for some particular inputs
         *
         * To follow these steps, you need a connection to the
         * BigML API, that will handle these creation calls. That's been passed
         * as the method argument.
         */

        if (DEBUG) {
            System.out.println("* Seventh example: Creating a Script and executing it");
        }

        JSONObject scriptArgs = new JSONObject(); // the arguments to create the script
        String sourceCode = "(define sum (+ a b))";
        scriptArgs.put("source_code", sourceCode);
        JSONArray scriptInputs = new JSONArray(); // the inputs are an array of JSONObjects
        JSONObject input1 = new JSONObject();
        JSONObject input2 = new JSONObject();
        input1.put("name", "a");
        input1.put("type", "number"); // adding first input
        scriptInputs.add(input1);
        input2.put("name", "b");
        input2.put("type", "number"); // adding second input
        scriptInputs.add(input2);
        JSONArray scriptOutputs = new JSONArray(); // the outputs are an array of JSONObjects
        JSONObject output = new JSONObject();
        output.put("name", "sum");
        output.put("type", "number");
        scriptOutputs.add(output);
        scriptArgs.put("inputs", scriptInputs);
        scriptArgs.put("outputs", scriptOutputs);
        scriptArgs.put("name", "simple sum script");
        System.out.println(scriptArgs.toString());
        // Script creation
        JSONObject script = api.createScript(sourceCode,  // WhizzML source code
                                             scriptArgs,  // script args: inputs and outputs
                                             null,        // wait time
                                             null);       // retries

        // Script creation is asynchronous. Wait for it to be finished.
        while (!api.scriptIsReady(script)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        script = api.getScript(script);

        if (DEBUG) { // auxiliary console message
            System.out.println("Script created: " +
                    DASHBOARD_URL + (String)script.get("resource"));
        }

        // Execute the `Script` by creating an execution.
        // Concrete values for the inputs need to be provided as args
           JSONObject execution = null;
        JSONObject executionArgs = new JSONObject();
        JSONArray executionInputs = new JSONArray(); // inputs need to be an array of arrays
        JSONArray exeInput1 = new JSONArray();
        JSONArray exeInput2 = new JSONArray();
        exeInput1.add("a");
        exeInput1.add(2);
        executionInputs.add(exeInput1); // setting first variable `a` to 2
        exeInput2.add("b");
        exeInput2.add(3);
        executionInputs.add(exeInput2); // setting second variable `b` to 3
        executionArgs.put("inputs", executionInputs);
        System.out.println(executionArgs.toString());
        // execution call
        execution = api.createExecution(
                (String) script.get("resource"), // script ID
                executionArgs,                   // execution arguments: inputs
                null,                            // wait for script time
                null);                           // retries
        // Execution creation is asynchronous. Wait for it to be finished.
        while (!api.executionIsReady(execution)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        execution = api.getExecution(execution);

        if (DEBUG) { // auxiliary console message
            System.out.println("Execution created: " +
                    DASHBOARD_URL + (String)execution.get("resource"));
            System.out.println("Result: " +  ((JSONObject) ((JSONObject) execution.get("object")).get("execution")).get("result"));
        }
    }
}
