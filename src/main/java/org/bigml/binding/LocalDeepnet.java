package org.bigml.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bigml.mimir.Predictor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * A local Predictive Deepnet.
 *
 * This module defines a Deepnet to make predictions locally or
 * embedded into your application without needing to send requests to
 * BigML.io.
 *
 * This module cannot only save you a few credits, but also enormously
 * reduce the latency for each prediction and let you use your models
 * offline.
 *
 * Example usage (assuming that you have previously set up the 
 * BIGML_USERNAME and BIGML_API_KEY environment variables and that you 
 * own the deepnet/id below):
 *
 * import org.json.simple.JSONValue;
 * import org.bigml.binding.LocalDeepnet;
 * 
 * // API client
 * BigMLClient api = new BigMLClient();
 *
 * JSONObject deepnet = api.getDeepnet("deepnet/5026965515526876630001b2");
 * LocalDeepnet localDeepnet = new LocalDeepnet(deepnet);
 * JSONObject inputData = (JSONObject) JSONValue.parse( "{\"petal length\": 3, \"petal width\": 1}" );
 * HashMap<String, Object> prediction = localDeepnet.predict(inputData, null, null, true);
 * 
 */

public class LocalDeepnet extends ModelFields implements SupervisedModelInterface {

	private static final long serialVersionUID = 1L;
	
	private static String DEEPNET_RE = "^deepnet/[a-f,0-9]{24}$";
	
	/**
	 * Logging
	 */
	static Logger logger = LoggerFactory.getLogger(
			LocalDeepnet.class.getName());
	
	private String deepnetId;
	private JSONArray inputFields = null;
	private String objectiveField = null;
	private JSONArray objectiveFields = null;
	private Boolean regression = false;
	private List<String> classNames = new ArrayList<String>();
	private String defaultNumericValue = null;

		
	public LocalDeepnet(JSONObject deepnet) throws Exception {
        this(null, deepnet);
    }
	
	public LocalDeepnet(BigMLClient bigmlClient, JSONObject deepnet) 
			throws Exception {
		
		super(bigmlClient, deepnet);
		deepnet = this.model;
		
		deepnetId = (String) deepnet.get("resource");
		
		inputFields = (JSONArray) Utils.getJSONObject(
				deepnet, "input_fields");
		
		if (deepnet.containsKey("deepnet")
				&& deepnet.get("deepnet") instanceof JSONObject) {

			JSONObject status = (JSONObject) Utils.getJSONObject(
					deepnet, "status");

			if (status != null && status.containsKey("code")
					&& AbstractResource.FINISHED == ((Number) status
							.get("code")).intValue()) {
				
				objectiveField = (String) Utils.getJSONObject(
						deepnet, "objective_field");
				objectiveFields = (JSONArray) Utils.getJSONObject(
						deepnet, "objective_fields");
				
				
				JSONObject deepnetInfo = (JSONObject) Utils
						.getJSONObject(deepnet, "deepnet");
				
				JSONObject fields = (JSONObject) Utils.getJSONObject(
						deepnetInfo, "fields", new JSONObject());

				this.defaultNumericValue = (String) deepnet.get("default_numeric_value");

				// initialize ModelFields
				objectiveField = objectiveField != null ?
						objectiveField : (String) objectiveFields.get(0);
				super.initialize((JSONObject) fields, objectiveField, 
						null, null, true, true, false);
				
				String optype = (String) Utils.getJSONObject(
						fields, objectiveFieldId + ".optype", "");
				regression = optype.equals(Constants.OPTYPE_NUMERIC);
				
				JSONArray categories = (JSONArray) Utils.getJSONObject(
						(JSONObject) fields.get(objectiveField), 
            			"summary.categories", new JSONArray());
				
				if (!regression) {
					for (Object cat: categories) {
						classNames.add((String) ((JSONArray) cat).get(0));
					}
					Collections.sort(classNames);
				}
			} else {
				throw new Exception(
						"The deepnet isn't finished yet");
			}

		} else {
			throw new Exception(String
					.format("Cannot create the Deepnet instance. "
							+ "Could not find the 'deepnet' key in "
							+ "the resource:\n\n%s", deepnet));
		}	
	}
	
	/**
	 * Returns reg expre for model Id.
	 */
    public String getModelIdRe() {
		return DEEPNET_RE;
	}
    
    /**
	 * Returns bigml resource JSONObject.
	 */
    public JSONObject getBigMLModel(String modelId) {
		return (JSONObject) this.bigmlClient.getDeepnet(modelId);
	}
	
	/**
	 * Returns the resourceId
	 */
	public String getResourceId() {
		return deepnetId;
	}
	
	/**
	 * Returns the class names
	 */
	public List<String> getClassNames() {
		return classNames;
	}
	
	
	/**
	 * Makes a prediction based on a number of field values.
	 * 
	 * @param inputData		Input data to be predicted
	 * @param operatingPoint
	 * 			In classification models, this is the point of the
     *          ROC curve where the model will be used at. The
     *          operating point can be defined in terms of:
     *                - the positive class, the class that is important to
     *                  predict accurately
     *                - the probability threshold,
     *                  the probability that is stablished
     *                  as minimum for the positive_class to be predicted.
     *          The operating point is then defined as a map with
     *          two attributes, e.g.:
     *                  {"positive_class": "Iris-setosa",
     *                   "probability_threshold": 0.5}
	 * @param operatingKind		 
	 * 			"probability". Sets the property that decides the 
	 * 			prediction. Used only if no operating point is used
	 * 
	 * @param full
	 * 			Boolean that controls whether to include the prediction's
     *          attributes. By default, only the prediction is produced. If set
     *          to True, the rest of available information is added in a
     *          dictionary format. The dictionary keys can be:
     *             - prediction: the prediction value
     *             - probability: prediction's probability
     *             - distribution: distribution of probabilities for each
     *                             of the objective field classes
     *             - unused_fields: list of fields in the input data that
     * 
     * @return prediction for the input data
	 */
	public HashMap<String, Object> predict(
			JSONObject inputData, JSONObject operatingPoint, 
			String operatingKind, Boolean full) throws Exception {
		
		if (full == null) {
			full = false;
		}
		
		// Checks and cleans inputData leaving the fields used in the model
        inputData = filterInputData(inputData, full);
        
        List<String> unusedFields = (List<String>) 
        		inputData.get("unusedFields");
		inputData = (JSONObject) inputData.get("newInputData");
		
		// Strips affixes for numeric values and casts to the final field type
        Utils.cast(inputData, fields);
        
        // When operating_point is used, we need the probabilities
        // of all possible classes to decide, so se use
        // the `predict_probability` method
        if (operatingPoint != null) {
        	if (regression) {
        		throw new IllegalArgumentException(
        				"The operating_point argument can only be" +
                        " used in classifications.");
        	}

        	return predictOperating(inputData, operatingPoint);
        }
		
        if (operatingKind != null) {
        	if (regression) {
        		throw new IllegalArgumentException(
        				"The operating_kind argument can only be" +
                        " used in classifications.");
        	}
        	
        	return predictOperatingKind(inputData, operatingKind);
        }
        
        HashMap<String, Object> prediction = new HashMap<String, Object>();
        HashMap<String, Object> instance =
            new ObjectMapper().readValue(inputData.toString(), HashMap.class);

		Predictor predictor = Predictor.getPredictor(this.model.toJSONString());
        Double[] predict = ArrayUtils.toObject(predictor.predict(instance));

        ArrayList<List<Double>> pred = new ArrayList<List<Double>>();
        pred.add((List<Double>) Arrays.asList(predict));
		prediction = toPrediction(pred);

        if (full) {
        	prediction.put("unused_fields", unusedFields);
        }
        return prediction;
	}
	
	/**
	 * Predicts a probability for each possible output class, based on
     * input values. The input fields must be a dictionary keyed by
     * field name or field ID.
     *
     * @param inputData	Input data to be predicted
	 */
	private JSONArray predictProbability(JSONObject inputData) {
		try {
			return predictProbability(inputData, null);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Predicts a probability for each possible output class, based on
     * input values. The input fields must be a dictionary keyed by
     * field name or field ID.
     *
     * @param inputData	Input data to be predicted
	 */
	public JSONArray predictProbability(
			JSONObject inputData, MissingStrategy missingStrategy) 
			throws Exception {
		
		HashMap<String, Object> prediction = 
				predict(inputData, null, null, true);
		
		JSONArray distribution = (JSONArray) prediction.get("distribution");
		Utils.sortPredictions(distribution, "probability", "category");
		return distribution;
		
	}
	
	/**
	 * Computes the prediction based on a user-given operating point.
	 */
	private HashMap<String, Object> predictOperating(
			JSONObject inputData, JSONObject operatingPoint) {

		String[] operatingKinds = {"probability"};
		Object[] operating = Utils.parseOperatingPoint(
				operatingPoint, operatingKinds, classNames);

		String kind = (String) operating[0];
		Double threshold = (Double) operating[1];
		String positiveClass = (String) operating[2];
		
		JSONArray predictions = predictProbability(inputData);
		for (Object pred: predictions) {
			JSONObject prediction = (JSONObject) pred;
			String category = (String) prediction.get("category");
			
			if (category.equals(positiveClass) &&
					(Double) prediction.get(kind) > threshold) {
				return prediction;
			}
		}
		
		HashMap<String, Object> prediction 
			= (HashMap<String, Object>) predictions.get(0);
		String category = (String) prediction.get("category");
		if (category.equals(positiveClass)) {
			prediction = (HashMap<String, Object>) predictions.get(1);
		}
		
		prediction.put("prediction", prediction.get("category"));
		prediction.remove("category");
		return prediction;
	}

	
	/**
	 * Computes the prediction based on a user-given operating kind.
	 */
	private HashMap<String, Object> predictOperatingKind(
			JSONObject inputData, String operatingKind) {

		JSONArray predictions = null;
		String kind = operatingKind.toLowerCase();
		if (kind.equals("probability")) {
			predictions = predictProbability(inputData);
		} else {
			throw new IllegalArgumentException(
           		 	"Only probability is allowed as operating kind " +
                    "for deepnets.");
		}
		HashMap<String, Object> prediction 
			= (HashMap<String, Object>) predictions.get(0);
		prediction.put("prediction", prediction.get("category"));
		prediction.remove("category");
		return prediction;
	}

	/**
	 * Structuring prediction in a dictionary output
	 */
	private HashMap<String, Object> toPrediction(ArrayList<List<Double>> pred) {

		Double probability = Collections.max(pred.get(0));
		int index = pred.get(0).indexOf(probability);
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("probability", probability);
		
		if (classNames != null && classNames.size() > 0) {
			result.put("prediction", classNames.get(index));
			
			// Chooses the most probable category as prediction
	        JSONArray distribution = new JSONArray();
	        for (int i=0; i<classNames.size(); i++) {
	        	JSONObject probabilityCategory = new JSONObject();
	        	probabilityCategory.put("category", classNames.get(i));
	        	probabilityCategory.put("probability", Utils.roundOff(
	        			pred.get(0).get(i), Constants.PRECISION));

	        	distribution.add(probabilityCategory);
	        }

	        result.put("distribution", distribution);
		}

		return result;
	}

}