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

import java.util.HashMap;

import org.bigml.binding.AuthenticationException;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.InputDataParseException;
import org.bigml.binding.LocalPredictiveModel;
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
		BigMLClient bigml = null;
		try {
			bigml = BigMLClient.getInstance(BIGML_USERNAME, BIGML_API_KEY,
					DEV_MODE);
		} catch (AuthenticationException e) {
			e.printStackTrace();
			return;
		}
		// Create a datasource by upload a file
		JSONObject resource = bigml.createSource("data/iris.csv",
				"My Test Source", null);

		// Create a dataset using the datasource created
		resource = bigml.createDataset((String) resource.get("resource"), null,
				null, null);

		// Create a model using the dataset created above
		JSONObject model = bigml.createModel((String) resource.get("resource"),
				null, null, null);

		// Create a prediction using created model
		/*
		 * Build the input data object: We can build the input data object using
		 * BigML's field IDs (e.g. "000001") as keys but here we'll use the
		 * field name
		 */
		boolean byName = true;
		JSONObject inputData = new JSONObject();
		inputData.put("petal width", 1.9);
		inputData.put("petal length", 3.5);

		// This is a remote prediction, that is a prediction made by BigML and
		// it will consume BigML credits (~0.01)
		JSONObject remotePrediction = bigml.createPrediction(
				(String) model.get("resource"), inputData, byName, null, null,
				null);
		while (!bigml.predictionIsReady(remotePrediction)) {
			try {
				remotePrediction = bigml.getPrediction(remotePrediction);
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
		model = bigml.getModel(model);
		LocalPredictiveModel localModel = null;
    HashMap<Object, Object> localPrediction = null;
		try {
			localModel = new LocalPredictiveModel(
					(JSONObject) model.get("object"));

      localPrediction = localModel.predict(inputData, byName, true);

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