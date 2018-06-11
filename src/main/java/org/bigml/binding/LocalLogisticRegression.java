package org.bigml.binding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bigml.binding.resources.AbstractResource;

/**
 * A local Predictive Logistic Regression.
 *
 * This module defines a Logistic Regression to make predictions locally or
 * embedded into your application without needing to send requests to BigML.io.
 *
 * This module cannot only save you a few credits, but also enormously reduce
 * the latency for each prediction and let you use your logistic regressions
 * offline.
 *
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the
 * logisticregression/id below):
 *
 *
 * import org.bigml.binding.LocalLogisticRegression;
 *
 * JSONObject logisticRegression = BigMLClient.getInstance().
 * 		getLogisticRegression("logisticregression/5026965515526876630001b2");
 * LocalLogisticRegression logistic =
 * 		LocalLogisticRegression(logisticRegression)
 *
 * JSONObject predictors = JSONValue.parse("
 * 		{\"petal length\": 3, \"petal width\": 1,
 * 		 \"sepal length\": 1, \"sepal width\": 0.5}");
 *
 * logistic.predict(predictors)
 * 
 */
public class LocalLogisticRegression extends ModelFields {

	private static final long serialVersionUID = 1L;

	static String LOGISTICREGRESSION_RE = "^logisticregression/[a-f,0-9]{24}$";

	static HashMap<String, String> EXPANSION_ATTRIBUTES = new HashMap<String, String>();
	static {
		EXPANSION_ATTRIBUTES.put("categorical", "categories");
		EXPANSION_ATTRIBUTES.put("text", "tag_cloud");
		EXPANSION_ATTRIBUTES.put("items", "items");
	}

	protected static final String[] OPTIONAL_FIELDS = { 
    		"categorical", "text", "items", "datetime" };

	protected static final int PRECISION = 5;
	
	/**
	 * Logging
	 */
	static Logger logger = LoggerFactory
			.getLogger(LocalLogisticRegression.class.getName());

	private String logisticRegressionId;

	private JSONObject datasetFieldTypes = null;
	private JSONArray inputFields = null;
	private String objectiveField = null;
	private JSONArray objectiveFields = null;

	private JSONObject coefficients = null;
	private Boolean bias;
	private Double c;
	private Double eps;
	private Boolean normalize;
	private Boolean balanceFields;
	private String regularization;
	private JSONObject fieldCodings;
	private List<String> classNames = new ArrayList<String>();
	
	
	public LocalLogisticRegression(JSONObject logistic) throws Exception {
		super((JSONObject) Utils.getJSONObject(
				logistic, "logistic_regression.fields", new JSONObject()));
		
		// checks whether the information needed for local predictions 
		// is in the first argument
		if (!checkModelFields(logistic)) {
			// if the fields used by the logistic regression are not
			// available, use only ID to retrieve it again
			logisticRegressionId = (String) logistic.get("resource");
			boolean validId = logisticRegressionId.matches(
					LOGISTICREGRESSION_RE);
			if (!validId) {
				throw new Exception(
					logisticRegressionId + " is not a valid resource ID.");
			}
		}
		
		if (!(logistic.containsKey("resource")
				&& logistic.get("resource") != null)) {
			BigMLClient client = BigMLClient.getInstance(
					BigMLClient.STORAGE);
			logistic = client.getLogisticRegression(logisticRegressionId);
			
			if ((String) logistic.get("resource") == null) {
				throw new Exception(
					logisticRegressionId + " is not a valid resource ID.");
			}
		}
		
		if (logistic.containsKey("object") &&
				logistic.get("object") instanceof JSONObject) {
			logistic = (JSONObject) logistic.get("object");
		}
		
		logisticRegressionId = (String) logistic.get("resource");
		
		// Check json structure
		datasetFieldTypes = (JSONObject) Utils.getJSONObject(logistic,
				"dataset_field_types");
		inputFields = (JSONArray) Utils.getJSONObject(logistic, "input_fields");
		objectiveField = (String) Utils.getJSONObject(logistic,
				"objective_field");
		objectiveFields = (JSONArray) Utils.getJSONObject(logistic,
				"objective_fields");

		if (datasetFieldTypes == null || inputFields == null
				|| (objectiveField == null && objectiveFields == null)) {
			throw new Exception(
					"Failed to find the logistic regression expected "
							+ "JSON structure. Check your arguments.");
		}
		
		if (logistic.containsKey("logistic_regression")
				&& logistic.get("logistic_regression") instanceof JSONObject) {

			JSONObject status = (JSONObject) Utils.getJSONObject(logistic,
					"status");

			if (status != null && status.containsKey("code")
					&& AbstractResource.FINISHED == ((Number) status
							.get("code")).intValue()) {
				
				JSONObject logisticInfo = (JSONObject) Utils
						.getJSONObject(logistic, "logistic_regression");
				
				// Check if old format for coefficents
				JSONArray coefficientsList = (JSONArray) Utils.getJSONObject(
						logisticInfo, "coefficients", new JSONArray());
				if (coefficientsList.get(0) instanceof String) {
					throw new Exception(
						"Detected old format of logistic regression detected.");
				}
				
				
				JSONObject fields = (JSONObject) Utils.getJSONObject(
						logisticInfo, "fields", new JSONObject());
				
				if (inputFields == null) {
					inputFields = new JSONArray();
					String[] inputFieldsArray = new String[fields.values().size()];
					for (Object fieldId : fields.keySet()) {
						int columnNumber = ((Number) Utils.getJSONObject(
								fields, fieldId + ".column_number")).intValue();
						inputFieldsArray[columnNumber] = (String) fieldId;
		            }
					inputFields.addAll(Arrays.asList(inputFieldsArray));
				}
				
				coefficients = new JSONObject();
				for (int i=0; i<coefficientsList.size(); i++) {
					JSONArray coeff = (JSONArray) coefficientsList.get(i);
					coefficients.put((String) coeff.get(0), 
									 (JSONArray) coeff.get(1));
				}
				
				bias = (Boolean) Utils.getJSONObject(logisticInfo, "bias", true);
				c = ((Number) Utils.getJSONObject(logisticInfo, "c")).doubleValue();
				eps = ((Number) Utils.getJSONObject(logisticInfo, "eps")).doubleValue();
				
				normalize = (Boolean) Utils.getJSONObject(logisticInfo, "normalize");
				balanceFields = (Boolean) Utils.getJSONObject(
						logisticInfo, "balance_fields");
				regularization = (String) Utils.getJSONObject(
						logisticInfo, "regularization");
				
				// old models have no such attribute, so we set it to 
				// False in this case
				missingNumerics = (Boolean) Utils.getJSONObject(
						logisticInfo, "missing_numerics");
				
				// initializae ModelFields
				super.initialize((JSONObject) fields, null, null, null,
								 true, true, true);
				
				Object fieldCodingsObj = (Object) Utils.getJSONObject(logisticInfo,
						"field_codings");
				if (fieldCodingsObj!=null && fieldCodingsObj instanceof JSONArray) {
					formatFieldCodings((JSONArray) fieldCodingsObj);
				} else {
					fieldCodings = (JSONObject) Utils.getJSONObject(logisticInfo,
							"field_codings", new JSONObject());
				}
				
				for (Object field : fieldCodings.keySet()) {
					String fieldId = (String) field;
					if (!fields.containsKey(fieldId) && 
						this.invertedFields.containsKey(fieldId)) {
						
						JSONObject fieldObj = (JSONObject) fieldCodings.get(fieldId);
						fieldObj.put(this.invertedFields.get(fieldId), 
									 fieldCodings.get(fieldId));
						fieldCodings.remove(fieldId);
					}
				}
				
				JSONArray categories = (JSONArray) Utils.getJSONObject(
						(JSONObject) fields.get(objectiveField), 
            			"summary.categories", new JSONArray());
				
				if (coefficients.keySet().size() > categories.size()) {
					classNames.add("");
				}
				for (Object cat: categories) {
					classNames.add((String) ((JSONArray) cat).get(0));
				}
				
			} else {
				throw new Exception(
						"The logistic regression isn't finished yet");
			}

		} else {
			throw new Exception(String
					.format("Cannot create the LogisticRegression instance. "
							+ "Could not find the 'logistic_regression' key in "
							+ "the resource:\n\n%s", logistic));
		}

	}

	
	/**
	 * Predicts a probability for each possible output class, based on
     * input values. The input fields must be a dictionary keyed by 
     * field name or field ID.
     * 
     * @param inputData	Input data to be predicted
	 */
	private JSONArray predictProbability(JSONObject inputData) {
		JSONObject prediction = predict(inputData, null, null, true, false);
		JSONArray distribution = (JSONArray) prediction.get("distribution");
		sortPredictions(distribution);
		return distribution;
	}
	
	
	/**
	 * Computes the prediction based on a user-given operating point.
	 */
	private JSONObject predictOperating(
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
			String category = (String) prediction.get("prediction");		
			if (category.equals(positiveClass) &&
					(Double) prediction.get(kind) > threshold) {
				return prediction;
			}
		}
		return  (JSONObject) predictions.get(0);
	}
	
	
	/**
	 * Computes the prediction based on a user-given operating kind.
	 */
	private JSONObject predictOperatingKind(
			JSONObject inputData, String operatingKind) {
		
		JSONArray predictions = null;
		String kind = operatingKind.toLowerCase();
		if (kind.equals("probability")) {
			predictions = predictProbability(inputData);
		} else {
			throw new IllegalArgumentException(
           		 	"Only probability is allowed as operating kind " +
                    "for logistic regressions.");
		}
		
		return (JSONObject) predictions.get(0);
	}
	
	/**
	 * Returns the class prediction and the probability distribution
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
	 * 
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
	 */
	public JSONObject predict(
			JSONObject inputData, JSONObject operatingPoint, 
			String operatingKind, Boolean full, boolean byName) {
		
		if (full == null) {
			full = false;
		}
		
		// Checks and cleans inputData leaving the fields used in the model
        inputData = filterInputData(inputData, full, byName);
        
        List<String> unusedFields = (List<String>) 
        		inputData.get("unusedFields");
		inputData = (JSONObject) inputData.get("newInputData");
		
		// Strips affixes for numeric values and casts to the final field type
        Utils.cast(inputData, fields);
        
        // When operating_point is used, we need the probabilities
        // of all possible classes to decide, so se use
        // the `predict_probability` method
        if (operatingPoint != null) {
        	return predictOperating(inputData, operatingPoint);
        }
        
        if (operatingKind != null) {
        	return predictOperatingKind(inputData, operatingKind);
        }
        
        // In case that missing_numerics is False, checks that all numeric
        // fields are present in input data.
        if (missingNumerics == null) {
        	for (Object fieldId : fields.keySet()) {
                JSONObject field = (JSONObject) fields.get(fieldId);
                if( Arrays.binarySearch(OPTIONAL_FIELDS, field.get("optype")) == -1 &&
                    !inputData.containsKey(fieldId) ) {
                     throw new IllegalArgumentException(
                    		 "Failed to predict a centroid. Input data " +
                             "must contain values for all numeric " +
                             "fields to get a logistic regression prediction.");
                }
        		
        	}
        }
        
        if (balanceFields != null && balanceFields==true) {
        	balanceInput(inputData, fields);
        }
        
        // Computes text and categorical field expansion
        Map<String, Object> uniqueTerms = uniqueTerms(inputData);
        
        // Computes the contributions for each category
        JSONObject probabilities = new JSONObject();
        double total = 0;
        
        for (Object coeff : coefficients.keySet()) {
        	String category = (String) coeff;
        	double probability = categoryProbability(
        			inputData, uniqueTerms, category);
        	
        	JSONArray objectiveFieldCategory = 
        		(JSONArray) categories.get(objectiveField);
        	int order = objectiveFieldCategory.indexOf(category);
        	if (order == -1) {
        		if (category.equals("")) {
        			order = objectiveFieldCategory.size();
        		}
        	}
        	
        	JSONObject probabilityCategory = new JSONObject();
        	probabilityCategory.put("prediction", category);
        	probabilityCategory.put("probability", probability);
        	probabilityCategory.put("order", order);
   
        	probabilities.put(category, probabilityCategory);
        	total += probability;
        }
        
        // Normalizes the contributions to get a probability
        for (Object category: probabilities.keySet()) {
        	JSONObject probabilityCategory = (JSONObject)
        			probabilities.get(category);
        	double probability = ((Number) 
        		probabilityCategory.get("probability")).doubleValue();
        	probability /= total;
        	probabilityCategory.put("probability", 
        			Utils.roundOff(probability, PRECISION));
        }
        
        // Chooses the most probable category as prediction
        JSONArray distribution = new JSONArray();
        for (Object category: probabilities.keySet()) {
        	JSONObject probabilityCategory = (JSONObject)
        			probabilities.get(category);
        	probabilityCategory.remove("order");
        	distribution.add(probabilityCategory);
        }
        
        sortPredictions(distribution);
        JSONObject prediction = (JSONObject) distribution.get(0);
        
        JSONObject result = new JSONObject();
        result.put("prediction", (String) prediction.get("prediction"));
        result.put("probability", (Double) prediction.get("probability"));
        result.put("distribution", distribution);
        
        if (full) {
        	result.put("unused_fields", unusedFields);
        }
        
		return result;
	}
	
	
	/**
	 * Computes the probability for a concrete category
	 * 
	 */
	private double categoryProbability(JSONObject numericInputs,
			Map<String, Object> uniqueTerms, String category) {
		
		double probability = 0.0;
		double norm2 = 0.0;
		
		// numeric input data
		for (Object field: numericInputs.keySet()) {
			String fieldId = (String) field;
			JSONArray coefficients = getCoefficients(category, fieldId);
			double value = ((Number) numericInputs.get(fieldId)).doubleValue();
			double coeff = ((Number) coefficients.get(0)).doubleValue();
			
			probability += coeff * value;
			if (normalize) {
				norm2 += Math.pow(value, 2);
			}
		}
		
		// text, items and categories
		for (Object field: uniqueTerms.keySet()) {
			String fieldId = (String) field;
			
			if (inputFields.contains(fieldId)) {
				Map<String, Integer> uniqueTerm = (Map<String, Integer>) 
						uniqueTerms.get(fieldId);
				
				JSONArray coefficients = getCoefficients(category, fieldId);
				
				for (Object term: uniqueTerm.keySet()) {
					
					int occurrences = ((Number) uniqueTerm.get(term)).intValue();
					
					try {
						boolean oneHot = true;
						
						Integer index = null;
						if (tagClouds.containsKey(fieldId)) {
							index = ((List) tagClouds.get(fieldId)).indexOf(term);
						} else {
							if (items.containsKey(fieldId)) {
								index = ((List) items.get(fieldId)).indexOf(term);
							} else {
								JSONObject fieldCoding = (JSONObject) fieldCodings.get(fieldId);
								
								if (categories.containsKey(fieldId) &&
										(!fieldCodings.containsKey(fieldId) || 
										 "dummy".equals( (String) fieldCoding.keySet().toArray()[0] ))) {
									index = ((JSONArray) categories.get(fieldId)).indexOf(term);
								} else {
									if (categories.containsKey(fieldId)) {
										oneHot = false;
										index = ((JSONArray) categories.get(fieldId)).indexOf(term);
										int coeffIndex = 0;
										
										JSONArray contributions = (JSONArray) fieldCoding.values().toArray()[0];
										
										for (Object contribValue: contributions) {
											JSONArray contribution = (JSONArray) contribValue;
											double contrib = ((Number) 
													contribution.get(index)).doubleValue();
											double coeff = ((Number) 
												coefficients.get(coeffIndex)).doubleValue();
											
											probability += coeff * contrib * occurrences;
											coeffIndex++;
										}
									}
								}
							}
						}
						
						if (oneHot) {
							double coeff = ((Number) 
								coefficients.get(index)).doubleValue();
							probability += coeff * occurrences;
						}
						norm2 += Math.pow(occurrences, 2);
					} catch (Exception e) {}
				}
				
			}
		}
				
		// missings
		for (Object field: inputFields) {
			String fieldId = (String) field;
			boolean contribution = false;
			JSONArray coefficients = getCoefficients(category, fieldId);
			
			try {
				if (numericFields.containsKey(fieldId) &&
						!numericInputs.containsKey(fieldId)) {
					double coeff = ((Number) 
							coefficients.get(1)).doubleValue();
					probability += coeff;
			        contribution = true;
				} else {
					boolean uniqueTerm = 
							!uniqueTerms.containsKey(fieldId) ||
							uniqueTerms.get(fieldId)==null ||
							((HashMap) uniqueTerms.get(fieldId)).keySet().size() == 0;
					
					if (tagClouds.containsKey(fieldId) && uniqueTerm) {
						double coeff = ((Number) coefficients.get(
								tagClouds.get(fieldId).size())).doubleValue();
						probability += coeff;
						contribution = true;
					} else {
						if (items.containsKey(fieldId) && uniqueTerm) {
							double coeff = ((Number) coefficients.get(
									items.get(fieldId).size())).doubleValue();
							probability += coeff;
							contribution = true;
						} else {
							if (categories.containsKey(fieldId) &&  
									!objectiveField.equals(fieldId) &&
									!uniqueTerms.containsKey(fieldId)) {
								
								JSONObject fieldCoding = (JSONObject) fieldCodings.get(fieldId);
								
								if (!fieldCodings.containsKey(fieldId) || 
										"dummy".equals( (String) fieldCoding.keySet().toArray()[0] )) {
									
									double coeff = ((Number) 
											coefficients.get(((List) categories.get(fieldId)).size())).doubleValue();
									
									probability += coeff;
								} else {
									// codings are given as arrays of coefficients. The
				                    // last one is for missings and the previous ones are
				                    // one per category as found in summary
									int coeffIndex = 0;
									
									JSONArray contributions = (JSONArray) fieldCoding.values().toArray()[0];
									
									for (Object contribValue: contributions) {
										JSONArray constribution = (JSONArray) contribValue;
										double coeff = ((Number) 
											coefficients.get(coeffIndex)).doubleValue();
										double value = ((Number) 
												constribution.get(constribution.size()-1)).doubleValue();
										probability += coeff * value;
										coeffIndex++;
									}
								}
								contribution = true;
							}
						}
					}
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (contribution && normalize) {
				norm2 += 1;
			}   
		}
		
		// the bias term is the last in the coefficients list
		JSONArray catCoeff = (JSONArray) coefficients.get(category);
		probability += ((Number) ((JSONArray) 
				catCoeff.get(catCoeff.size()-1)).get(0)).doubleValue();
		
		if (bias) {
			norm2 += 1;
		}
		
		if (normalize) {
			try {
				probability /= Math.sqrt(norm2);
			} catch (Exception e) {
				// this should never happen
				probability = 0.0;
			}
		}
		
		try {
			probability = 1 / (1 + Math.exp(-probability));
		} catch (Exception e) {
			probability = probability < 0 ? 0 : 1;
		}
		
		// truncate probability to 5 digits, as in the backend
		return Utils.roundOff(probability, 5);
	}
	
	
	/**
	 * Balancing the values in the input_data using the corresponding
	 * field scales
	 */
	private void balanceInput(JSONObject inputData, JSONObject fields) {
		for (Object fieldId : inputData.keySet()) {
            JSONObject field = (JSONObject) fields.get(fieldId);
            
            if ("numeric".equals(field.get("optype"))) {
            	JSONObject summary = (JSONObject) field.get("summary");
            	
            	Double mean = (Double) Utils.getJSONObject(
            			summary, "mean", 0);
            	Double stddev = (Double) Utils.getJSONObject(
            			summary, "standard_deviation", 0);
            	
            	// if stddev is not positive, we only substract the mean
            	double value = ((Number) inputData.get(fieldId)).doubleValue();
            	if (stddev <= 0) {
            		inputData.put(fieldId, (value - mean));
            	} else {
            		inputData.put(fieldId, ((value - mean) / stddev));
            	}
            }
		}
	}
	
	/**
	 * Returns the set of coefficients for the given category and fieldIds
	 */
	private JSONArray getCoefficients(String category, String fieldId) {
		int coeffIndex = inputFields.indexOf(fieldId);
		return (JSONArray) ((JSONArray) coefficients.get(category))
				.get(coeffIndex);
	}
	
	/**
	 * Changes the field codings format to the dict notation
	 *
	 */
	 private void formatFieldCodings(JSONArray fieldCodingsArray) {
	 	fieldCodings = new JSONObject(); 
	 	for (int i=0; i<fieldCodingsArray.size(); i++) { 
	 		JSONObject element = (JSONObject)
 				fieldCodingsArray.get(i); 
	 		String fieldId = (String) element.get("field");
	  		String coding = (String) element.get("coding"); 
	  		JSONObject elemObject = new JSONObject(); 
	  		if (coding.equals("dummy")) { 
	  			elemObject.put(coding,element.get("dummy_class")); 
	  		} else { 
	  			elemObject.put(coding,element.get("coefficients"));
	  		}

	  		fieldCodings.put(fieldId, elemObject); 
	  	} 
	 }
	 
	 /**
	  * Sorting utility
	  * 
	  */
	 private void sortPredictions(JSONArray predictions) {
		Collections.sort(predictions, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
            	Double o1p = (Double) o1.get("probability");
            	Double o2p = (Double) o2.get("probability");
            	
            	if (o1p.doubleValue() == o2p.doubleValue()) {
            		return ((String) o1.get("prediction")).
                    		compareTo(((String) o2.get("prediction")));
            	}
            	
                return o2p.compareTo(o1p);
            }
        });
	}
}
