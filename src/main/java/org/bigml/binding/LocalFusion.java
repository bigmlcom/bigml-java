package org.bigml.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local Predictive Fusion.
 *
 * This module defines a Fusion to make predictions locally using its
 * associated models.
 *
 * This module can not only save you a few credits, but also enormously
 * reduce the latency for each prediction and let you use your models
 * offline.
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the
 * fusion/id below):
 *
 *
 * import org.bigml.binding.LocalFusion;
 *
 * // API client
 * BigMLClient api = new BigMLClient();
 *
 * JSONObject fusion = api.
 * 		getFusion("fusion/5026965515526876630001b2");
 * LocalFusion localFusion = new LocalFusion(fusion)
 *
 * JSONObject predictors = JSONValue.parse("{\"petal length\": 3, \"petal width\": 1}");
 *
 * localFusion.predict(predictors)
 * 
 */
public class LocalFusion extends ModelFields implements SupervisedModelInterface {

	private static final long serialVersionUID = 1L;
	
	static String FUSION_RE = "^fusion/[a-f,0-9]{24}$";
	
	private static final String[] OPERATING_POINT_KINDS = {"probability"};
	
	private static final String[] LOCAL_SUPERVISED = { 
			"model", "ensemble", "logisticregression", "deepnet",
            "fusion" };
	
	
	/**
	 * Logging
	 */
	static Logger logger = LoggerFactory.getLogger(
			LocalFusion.class.getName());

	private String fusionId;
	private BigMLClient bigmlClient;
	private String objectiveField = null;
	private JSONArray modelsIds;
	private List<Double> weights = new ArrayList<Double>();
	private final List<JSONArray> modelsSplit = new ArrayList<JSONArray>();
	private Boolean regression = false;
	private List<String> classNames = new ArrayList<String>();
	private Boolean missingNumerics = true;
	
	public LocalFusion(JSONObject fusion) 
			throws Exception {
		this(null, fusion, null);
	}
	
	
	public LocalFusion(BigMLClient bigmlClient, JSONObject fusion, Integer maxModels) 
			throws Exception {
		
		super((JSONObject) Utils.getJSONObject(
				fusion, "fusion.fields", new JSONObject()));
		
		this.bigmlClient =
            (bigmlClient != null)
                ? bigmlClient
                : new BigMLClient(null, null, BigMLClient.STORAGE);
		
		// checks whether the information needed for local predictions 
		// is in the first argument
		if (!checkModelFields(fusion)) {
			// if the fields used by the logistic regression are not
			// available, use only ID to retrieve it again
			fusionId = (String) fusion.get("resource");
			boolean validId = fusionId.matches(FUSION_RE);
			if (!validId) {
				throw new Exception(
						fusionId + " is not a valid resource ID.");
			}
		}
		
		if (!(fusion.containsKey("resource")
				&& fusion.get("resource") != null)) {
			fusion = this.bigmlClient.getFusion(fusionId);
			
			if ((String) fusion.get("resource") == null) {
				throw new Exception(
						fusionId + " is not a valid resource ID.");
			}
		}
		
		if (fusion.containsKey("object") &&
				fusion.get("object") instanceof JSONObject) {
			fusion = (JSONObject) fusion.get("object");
		}
		
		fusionId = (String) fusion.get("resource");
		
		if (fusion.containsKey("fusion")
				&& fusion.get("fusion") instanceof JSONObject) {

			JSONObject status = (JSONObject) Utils.getJSONObject(fusion,
					"status");

			if (status != null && status.containsKey("code")
					&& AbstractResource.FINISHED == ((Number) status
							.get("code")).intValue()) {
				
				JSONObject fusionInfo = (JSONObject) Utils
						.getJSONObject(fusion, "fusion");
				
				modelsIds =  new JSONArray();
				for (Object modelId: (JSONArray) fusion.get("models")) {
					String model = null;
					if (modelId instanceof String) {
						model = (String) modelId;
					} else {
						model = (String) ((JSONObject) modelId).get("id");
						
						try {
							weights.add(((Number) ((JSONObject) modelId).get("weight")).doubleValue());
						} catch (Exception e) {
							weights = new ArrayList<Double>();
						}
					}
					
					modelsIds.add(model);
					
					String type = model.split("/")[0];
					if (!Arrays.asList(LOCAL_SUPERVISED).contains(type)) {
						throw new IllegalArgumentException(
								String.format("The resource %s has not an allowed supervised model type.", OPERATING_POINT_KINDS));
					}
		    	}
				
				missingNumerics = (Boolean) Utils.getJSONObject(fusion, "missing_numerics", true);
				
				JSONObject fields = (JSONObject) Utils.getJSONObject(
						fusionInfo, "fields", new JSONObject());
	            
	            // initialize ModelFields
				super.initialize((JSONObject) fields, null, null, null,
								 true, true, true);
				
				objectiveField = (String) Utils.getJSONObject(
						fusion, "objective_field");
				
				// Apply maxModels
		    	int numberOfModels = modelsIds.size();
		    	if( maxModels != null) {
		            int[] items = Utils.getRange(0, numberOfModels, maxModels);
		            for (int item : items) {
		                if( item+maxModels <= numberOfModels ) {
		                    JSONArray arrayOfModels = new JSONArray();
		                    arrayOfModels.addAll(modelsIds.subList(item, item + maxModels));
		                    modelsSplit.add(arrayOfModels);
		                }
		            }
		        } else {
		        	modelsSplit.add(modelsIds);
		        }
		    	
		    	String optype = (String) Utils.getJSONObject(
						fields, objectiveField + ".optype");
		    	
		    	regression = "numeric".equals(optype);
		    	if (!regression) {
					JSONArray categories = (JSONArray) Utils.getJSONObject(
							(JSONObject) fields.get(objectiveField), 
			    			"summary.categories", new JSONArray());
					
					for (Object cat: categories) {
						classNames.add((String) ((JSONArray) cat).get(0));
					}
					Collections.sort(classNames);
				}
		    	
			} else {
				throw new Exception(
						"The Fusion isn't finished yet");
			}

		} else {
			throw new Exception(String
					.format("Cannot create the Fusion instance. "
							+ "Could not find the 'fusion' key in "
							+ "the resource:\n\n%s", fusion));
		}
	}
	
	/**
	 * Returns the resourceId
	 */
	public String getResourceId() {
		return fusionId;
	}
	
	/**
	 * Returns the class names
	 */
	public List<String> getClassNames() {
		return classNames;
	}
	
	/**
	 * For classification models, Predicts a probability for
     * each possible output class, based on input values.  The input
     * fields must be a dictionary keyed by field name or field ID.
     * 
     * For regressions, the output is a single element list
     * containing the prediction.
     * 
     * @param inputData			Input data to be predicted
     * @param missingStrategy	LAST_PREDICTION|PROPORTIONAL missing strategy
     *                        	for missing fields
	 */
	public JSONArray predictProbability(
			JSONObject inputData, MissingStrategy missingStrategy) 
		throws Exception {
		
		if (missingStrategy == null) {
    		missingStrategy = MissingStrategy.LAST_PREDICTION;
        }
		
		MultiVoteList votes = new MultiVoteList(null);
		
		if (!this.missingNumerics) {
			Utils.checkNoMissingNumerics(inputData, this.fields, null);
		}
		
		BigMLClient bigmlClient = new BigMLClient();
		
		for (Object modelSplit: modelsSplit) {
			MultiVoteList votesSplit = new MultiVoteList(null);
			
			List<SupervisedModelInterface> models = new ArrayList<SupervisedModelInterface>();
			
			for (Object modelId: (JSONArray) modelSplit) {
				String type = ((String) modelId).split("/")[0];
				JSONObject model = null;
				
				if ("model".equals(type)) {
					model = bigmlClient.getModel((String) modelId);
					models.add(new LocalPredictiveModel(model));
				}
				if ("ensemble".equals(type)) {
					model = bigmlClient.getEnsemble((String) modelId);
					models.add(new LocalEnsemble(model));
				}
				if ("logisticregression".equals(type)) {
					model = bigmlClient.getLogisticRegression((String) modelId);
					models.add(new LocalLogisticRegression(model));
				}
				if ("deepnet".equals(type)) {
					model = bigmlClient.getDeepnet((String) modelId);
					models.add(new LocalDeepnet(model));
				}
				if ("fusion".equals(type)) {
					model = bigmlClient.getFusion((String) modelId);
					models.add(new LocalFusion(model));
				}
			}
			
			JSONArray predictions;
			for (SupervisedModelInterface model: models) {
				try {
					predictions = model.predictProbability(inputData, missingStrategy);
				} catch (Exception e) {
					// logistic regressions can raise this error if they
					// have missing_numerics=False and some numeric missings
					// are found
					continue;
				}
				
				List<Double> predictionList = new ArrayList<Double>();
            	for (Object pred : predictions) {
            		JSONObject p = (JSONObject) pred;
            		predictionList.add((Double) p.get("probability"));
            	}
				
            	if (!this.weights.isEmpty()) {
					predictionList = weight(predictionList, model.getResourceId());
				}
            	
            	// we need to check that all classes in the fusion
				// are also in the composing model
				if (!this.regression && !this.classNames.equals(model.getClassNames())) {
					try {
						predictionList = rearrangePrediction(model.getClassNames(), this.classNames, predictionList);
					} catch (Exception e) {}
				}
				
				votesSplit.append(predictionList);
			}
			
			votes.extend(votesSplit);
		}
		
		JSONArray output = new JSONArray();
		if (this.regression) {
			double totalWeight = 1;
			if (!this.weights.isEmpty()) {
				totalWeight = 0;
				for (Double w: this.weights) {
					totalWeight += w;
				}
			}
			
			double sum = 0.0;
			for (Object votesPreds: votes.predictions) {
				List<Double> preds = (List<Double>) votesPreds;
				for (Double p: preds) {
					sum += p;
				}
			}
			
			float divisor = ((Double) (votes.predictions.size() * totalWeight)).floatValue();
			
			JSONObject prediction = new JSONObject();
			prediction.put("prediction", sum / divisor);
			output.add(prediction);
		} else {
			List<Double> probabilities = votes.combineToDistribution(true);
			for (int i = 0; i < classNames.size(); i++) {
				JSONObject prediction = new JSONObject();
				prediction.put("prediction", (String) classNames.get(i));
				prediction.put("probability", probabilities.get(i));
				output.add(prediction);
			}
		}
		
		return output;
	}
	
	
	/**
	 * Weighs the prediction according to the weight associated to the
	 * current model in the fusion.
	 */
	private List<Double> weight(List<Double> predictions, String modelId) {
		for (Double probability: predictions) {
			probability *= this.weights.get(this.modelsIds.indexOf(modelId));
		}
		return predictions;
	}
	
	/**
	 * Rearranges the probabilities in a compact array when the
	 * list of classes in the destination resource does not match the
	 * ones in the origin resource.
	 */
	private List<Double> rearrangePrediction(
			List<String> originClasses, List<String> destinationClasses, List<Double> predictions) {
		
		List<Double> newPrediction = new ArrayList<Double>();
		for (String className: destinationClasses) {
			int originIndex = originClasses.indexOf(className);
			if (originIndex > -1) {
				newPrediction.add((Double) predictions.get(originIndex));
			} else {
				newPrediction.add(0.0);
			}
		}
		return newPrediction;
	}
	
	
	/**
	 * Computes the prediction based on a user-given operating point.
	 */
	private HashMap<String, Object> predictOperating(
			JSONObject inputData, MissingStrategy missingStrategy, 
			JSONObject operatingPoint) throws Exception {

		if (missingStrategy == null) {
    		missingStrategy = MissingStrategy.LAST_PREDICTION;
        }
		
		// only probability is allowed as operating kind
		Object[] operating = Utils.parseOperatingPoint(
				operatingPoint, OPERATING_POINT_KINDS, classNames);

		String kind = (String) operating[0];
		Double threshold = (Double) operating[1];
		String positiveClass = (String) operating[2];
		
		if (!Arrays.asList(OPERATING_POINT_KINDS).contains(kind)) {
			throw new IllegalArgumentException(
					String.format("Allowed operating kinds are %", OPERATING_POINT_KINDS));
		}
		
		JSONArray predictions = predictProbability(
				inputData, missingStrategy);
		
		for (Object pred: predictions) {
			HashMap<String, Object> prediction = (HashMap<String, Object>) pred;
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
			prediction = (JSONObject) predictions.get(1);
		}
		
		prediction.put("prediction", prediction.get("category"));
		prediction.remove("category");
		return  prediction;
	}
	
	
	/**
	 * Makes a prediction based on a number of field values.
	 * 
	 * @param inputData			Input data to be predicted
	 * @param missingStrategy	numeric key for the individual model's
     *                           prediction method. See the model predict
     *                           method.
	 * @param operatingPoint
	 * 			In classification models, this is the point of the
     *                    ROC curve where the model will be used at. The
     *                    operating point can be defined in terms of:
     *                    - the positive_class, the class that is important to
     *                      predict accurately
     *                    - the probability_threshold,
     *                      the probability that is stablished
     *                      as minimum for the positive_class to be predicted.
     *                    The operating_point is then defined as a map with
     *                    two attributes, e.g.:
     *                      {"positive_class": "Iris-setosa",
     *                       "probability_threshold": 0.5} 
	 * @param full
	 * 		   Boolean that controls whether to include the prediction's
     *         attributes. By default, only the prediction is produced. If set
     *         to True, the rest of available information is added in a
     *         dictionary format. The dictionary keys can be:
     *             - prediction: the prediction value
     *             - probability: prediction's probability
     *             - unused_fields: list of fields in the input data that
     *                              are not being used in the model
     *    
	 */
	public HashMap<String, Object> predict(
			JSONObject inputData, MissingStrategy missingStrategy, 
			JSONObject operatingPoint, Boolean full) 
			throws Exception {
		
		if (missingStrategy == null) {
    		missingStrategy = MissingStrategy.LAST_PREDICTION;
        }

		if (full == null) {
			full = false;
		}
		
		// Checks and cleans inputData leaving the fields used in the model
        inputData = filterInputData(inputData, full);
        
        List<String> unusedFields = (List<String>) 
        		inputData.get("unusedFields");
		inputData = (JSONObject) inputData.get("newInputData");
		
		if (!this.missingNumerics) {
			Utils.checkNoMissingNumerics(inputData, this.fields, null);
		}
		
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
        	
        	HashMap<String, Object> prediction = predictOperating(
        			inputData, missingStrategy, operatingPoint);
        	return prediction;
        }
        
        JSONArray predictions = predictProbability(
        		inputData, missingStrategy);
        
        if (!regression) {
        	Utils.sortPredictions(predictions, "probability", "prediction");
        }
        
        HashMap<String, Object> prediction 
        	= (HashMap<String, Object>) predictions.get(0);
        
        // adding unused fields, if any
        if (full) {
        	prediction.put("unused_fields", unusedFields);
        }

		return prediction;  
	}

}
