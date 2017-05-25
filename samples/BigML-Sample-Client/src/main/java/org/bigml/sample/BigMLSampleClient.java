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
         * Prediction workflow
         *
         * This example shows a complete prediction workflow
         * The steps to create a prediction are:
         * 1. Create a `Source` from your local or remote data
         * 2. Create a `Dataset` from the `Source`
         * 3. Create a `Model` from the `Dataset`
         * 4. Create a `Prediction` from the `Model` for new `input data`
         *
         * To follow these steps, you need to create a connection to the
         * BigML API, that will hadle these creation calls.
         */


        // The BigMLClient class stores the information that you need to
        // access BigML's API:
        //   - API domain (default: bigml.io)
        //   - credentials (BIGML_USERNAME and BIGML_API_KEY)
        //   - environment (development mode or production mode)
        // Its methods are wrappers to the REST API calls

        BigMLClient api = null;
        JSONObject emptyArgs = null;

        try {

            // Instantiating BigMLClient with the properties BIGML_USERNAME and
            // BIGML_API_KEY that should previously be set the
            // binding.properties file

            api = BigMLClient.getInstance();
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return;
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


        /**
         * End of the Prediction workflow
         *
         */

        // -------------------------------------------------------------- //

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


        JSONObject samplingArgs = new JSONObject();
        samplingArgs.add("sample_rate", 0.8);
        samplingArgs.add("seed", "My seed");
        samplingArgs.add("out_of_bag", true);
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

        /**
         * End of Evaluation of a model
         *
         */

        // ----------------------------------------------------------------- //


        /**
         * Creating unsupervised models: `Cluster` and `Anomaly`
         *
         * Example to create Clusters and an Anomaly detector
         *
         */

        // Creating a `Cluster` from the original `Dataset`
        // ------------------------------------------------
        // First argument is the `Dataset`ID

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

        /**
         * End of Creating unsupervised models
         *
         */

        // ----------------------------------------------------------------- //


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

        // Retrieve the model that should be Finished and ready to predict with
        model = api.getModel(model);

        // The `LocalPredictiveModel` is the class that encapsulates the
        // `Model` information and provides the `predict` method.
        // The `Prediction` class stores the local prediction result

        LocalPredictiveModel localModel = null;
        Prediction localPrediction = null;
        try {
            localModel = new LocalPredictiveModel(
                    (JSONObject) model.get("object"));

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

        System.out.println("BigML sample finished.");
        if (DEV_MODE) {
            System.out
                    .println("*** Remember that the resources created in this "
                            + "sample are placed in your DEV mode environment at BigML.com ***");
        }
    }
}
