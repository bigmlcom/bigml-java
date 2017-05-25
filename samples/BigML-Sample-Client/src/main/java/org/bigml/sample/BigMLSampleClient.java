/**
 * Requires BigML Java bindings
 * You can get it from: {@link https://github.com/bigmlcom/bigml-java}
 * 	- Clone it: <code>git clone https://github.com/bigmlcom/bigml-java.git</code>
 * 	- Or download it from: {@link https://github.com/bigmlcom/bigml-java/archive/master.zip}
 *
 * Once you have the source code, you can install it using Maven:
 * 	<code>mvn install</code>
 *
 * or just include it in your project class path.
 *
 */
package org.bigml.sample;

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

    // BigML's DEV mode enabled
    private static final boolean DEV_MODE = true;

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

            api = BigMLClient.getInstance();
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return;
        }

        // First example: Prediction workflow
        predictionWorkflow(api);


        // Common part for all workflows: Creating a Source and a Dataset
        // from a local file
        JSONObject dataset = createDataset(api, "data/iris.csv");


        // Second example: Local Predictions
        // To create a local prediction, you will need an existing model.
        // This is going to be the JSON that the LocalPredictiveModel
        // needs to predict

        JSONObject model = createModel(api, dataset)
        predictLocally(model);
        // Third example: Evaluation Workflow
        evaluationWorkflow(api, dataset);

        // Fourth example: Creating unsupervised models
        creatingUnsupervisedModels(api, dataset);

        System.out.println("BigML sample finished.");
        if (DEV_MODE) {
            System.out
                    .println("*** Remember that the resources created in this "
                            + "sample are placed in your DEV mode environment at BigML.com ***");
        }

        // Fifth example: Creating Topic Distribution
        topicDistributionWorkflow(api);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        source = api.getSource(source);

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        dataset = api.getDataset(dataset);


        // This code is not needed in the Prediction workflow, but can be
        // useful to inspect the dataset fields structure:
        // JSONObject fields = (JSONObject) Utils.getJSONObject(dataset,
        //                                                      "object.fields");
        // System.out.print(fields);


        // Creating a `Model` from the previous `Dataset`
        // -----------------------------------------------
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        model = api.getModel(model);

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
        boolean byName = true;
        JSONObject inputData = new JSONObject();
        inputData.put("sepal length", 5);
        inputData.put("sepal width", 2.5);

        // This is a remote prediction, that is a prediction made by BigML and
        // it will consume BigML credits (~0.01)
        JSONObject remotePrediction = api.createPrediction(
                (String) model.get("resource"), // model ID
                inputData,                      // data to predict for
                byName,                         // do we use names in input?
                emptyArgs,                      // model creation args
                null,                           // wait for dataset
                null);                          // retries

        // Predictions are synchronous, you can immediately get the results.
        // No need to wait.

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        source = api.getSource(source);

        JSONObject dataset = api.createDataset((String) source.get("resource"),
                                               emptyArgs, // creation args
                                               null,      // wait for Source
                                               null);     // retries

        // Dataset creation is asynchronous. Wait for it to be finished.
        while (!api.datasetIsReady(dataset)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
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
                // TODO Auto-generated catch block
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


        // The `LocalPredictiveModel` is the class that encapsulates the
        // `Model` information and provides the `predict` method.
        // The `Prediction` class stores the local prediction result

        LocalPredictiveModel localModel = null;
        Prediction localPrediction = null;
        try {
            localModel = new LocalPredictiveModel(
                    (JSONObject) model.get("object"));

            // Using the `predict` method to predict
            localPrediction = localModel.predict(
                    inputData,                     // data to predict for
                    byName,                        // do we use field names?
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


        // Creating a training `Model` from the previous `Dataset`
        // -------------------------------------------------------
        // First argument is the `Dataset`ID
        // Use 80% of data setting `sample_rate` to 0.8 and `seed`
        // to a particular string to do deterministic sampling

        JSONObject samplingArgs = new JSONObject();
        samplingArgs.add("sample_rate", 0.8);
        samplingArgs.add("seed", "My seed");

        JSONObject model = api.createModel((String) dataset.get("resource"),
                                           samplingArgs, // creation args
                                           null,         // wait for Dataset
                                           null);        // retries

        // Model creation is asynchronous. Wait for it to be finished.
        while (!api.modelIsReady(model)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // obtain the finished resource
        model = api.getModel(model);

        // Creating an `Evaluation` from the previous sampled `Dataset`
        // ------------------------------------------------------------
        // First argument is the `Model`ID
        // Second argument is the `Dataset`ID
        // Use the complementary 20% of data by
        // setting `sample_rate` to 0.8 and `out_of_bag` to true. The `seed`
        // must be the same in the model to ensure complementarity


        JSONObject outOfBagArgs = new JSONObject();
        outOfBagArgs.add("sample_rate", 0.8);
        outOfBagArgs.add("seed", "My seed");
        outOfBagArgs.add("out_of_bag", true);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // obtain the finished resource
        evaluation = api.getEvaluation(evaluation);

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // obtain the finished resource
        cluster = api.getCluster(cluster);

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // obtain the finished resource
        anomaly = api.getAnomaly(anomaly);

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
         * BigML API, that will hadle these creation calls. That's been passed
         * as the method argument.
         */


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

        // Providing the text for the document that we need to analyze
        JSONObject inputData = new JSONObject();
        inputData.put("000005", "hotel shower double heater");

        // This is a remote topic distribution
        JSONObject remoteTopicDistribution = api.createTopicDistribution(
                (String) topicModel.get("resource"), inputData, emptyArgs,
                null, null);
        remoteTopicDistribution = api.getTopicDistribution(remoteTopicDistribution);

        // The Topic Distribution object will contain information about the
        // Topics related to the text in your input data
        // JSONObject distributionOutput = (JSONObject) Utils.getJSONObject(
        //       remoteTopicDistribution, "object.topic_distribution");
        // System.out.println(distributionOutput);
    }
}
