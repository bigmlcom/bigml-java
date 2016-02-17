/**
 * Requires BigML Java bindings
 * You can get it from: {@link https://github.com/javinp/bigml-java}
 * 	- Clone it: <code>git clone https://github.com/javinp/bigml-java.git</code>
 * 	- Or download it from: {@link https://github.com/javinp/bigml-java/archive/master.zip}
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
import org.json.simple.JSONObject;

public class BigMLSampleClient {

    // BigML's DEV mode enabled
    private static final boolean DEV_MODE = true;

    // BigML's credentials
     private static final String BIGML_USERNAME = "set-your-bigml-username";
     private static final String BIGML_API_KEY = "set-your-bigml-api-key";

    /**
     * A simple Java Class to integrate the BigML API
     *
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        BigMLClient api = null;
        JSONObject emptyArgs = null;

        try {
            api = BigMLClient.getInstance(BIGML_USERNAME, BIGML_API_KEY,
                    DEV_MODE);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return;
        }
        // Create a datasource by upload a file
        JSONObject source = api.createSource("data/iris.csv", "Iris Source",
                emptyArgs);

        while (!api.sourceIsReady(source)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        source = api.getSource(source);
        JSONObject fields = (JSONObject) Utils.getJSONObject(source, "object.fields");
//        System.out.print(fields);

        // Create a dataset using the datasource created
        JSONObject dataset = api.createDataset((String) source.get("resource"),
                emptyArgs, null, null);

        while (!api.datasetIsReady(dataset)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        dataset = api.getDataset(dataset);
        fields = (JSONObject) Utils.getJSONObject(dataset, "object.fields");
//        System.out.print(fields);

        // Create a model using the dataset created above
        JSONObject model = api.createModel((String) dataset.get("resource"),
                emptyArgs, null, null);

        while (!api.modelIsReady(model)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        model = api.getModel(model);
        fields = (JSONObject) Utils.getJSONObject(model, "object.model.fields");
        JSONObject tree = (JSONObject) Utils.getJSONObject(model, "object.model.root");
//        System.out.print(fields);
//        System.out.print(tree);

        // Create a cluster using the dataset created above
        JSONObject cluster = api.createCluster((String) dataset.get("resource"),
                emptyArgs, null, null);

        while (!api.clusterIsReady(cluster)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        cluster = api.getCluster(cluster);
        JSONObject object = (JSONObject) Utils.getJSONObject(cluster, "object");
//        System.out.print(object);

        // Create a cluster using the dataset created above
        JSONObject anomaly = api.createAnomaly((String) dataset.get("resource"),
                emptyArgs, null, null);

        while (!api.anomalyIsReady(anomaly)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        anomaly = api.getAnomaly(anomaly);
        object = (JSONObject) Utils.getJSONObject(anomaly, "object");
//        System.out.print(object);

        JSONObject evaluation = api.createEvaluation(
                (String)model.get("resource"), (String)dataset.get("resource"),
                emptyArgs, null, null);

        while (!api.evaluationIsReady(evaluation)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        evaluation = api.getEvaluation(evaluation);
        JSONObject result = (JSONObject) Utils.getJSONObject(evaluation, "object.result");
//        System.out.print(result);

        // Create a prediction using created model
        /*
         * Build the input data object: We can build the input data object using
         * BigML's field IDs (e.g. "000001") as keys but here we'll use the
         * field name
         */
        boolean byName = true;
        JSONObject inputData = new JSONObject();
        inputData.put("sepal length", 5);
        inputData.put("sepal width", 2.5);

        // This is a remote prediction, that is a prediction made by BigML and
        // it will consume BigML credits (~0.01)
        JSONObject remotePrediction = api.createPrediction(
                (String) model.get("resource"), inputData, byName, emptyArgs,
                null, null);
        while (!api.predictionIsReady(remotePrediction)) {
            try {
                remotePrediction = api.getPrediction(remotePrediction);
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err
                        .println("Something wen't wrong while checking prediction status");
                e.printStackTrace();
            }
        }
        String predictionOutput = (String) Utils.getJSONObject(
                remotePrediction, "object.output");
        System.out.println("Prediction result: " + predictionOutput);

        /**
         * It is possible to build a LocalPredictiveModel using a previously
         * created model. A LocalPredictiveModel allow you to predict faster,
         * you're predicting without HTTPS round trips, and for FREE, this won't
         * consume BigML's credits.
         *
         * @see org.bigml.binding.LocalPredictiveModel
         */
        // Retrieve the model that should be Finished and ready to predict with
        // it
        model = api.getModel(model);
        LocalPredictiveModel localModel = null;
        Prediction localPrediction = null;
        try {
            localModel = new LocalPredictiveModel(
                    (JSONObject) model.get("object"));

            localPrediction = localModel.predict(inputData, byName,
                    MissingStrategy.PROPORTIONAL);

            System.out.println("Prediction result: "
                    + localPrediction.get("prediction") + ". With confidence: "
                    + localPrediction.get("confidence"));

        } catch (InputDataParseException e) {
            System.err.println("inputData cant be converted into a String");
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
