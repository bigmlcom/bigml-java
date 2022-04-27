package org.bigml.binding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local Partial Component Analysis.
 *
 * This module defines a PCA to make predictions locally or embedded into your
 * application without needing to send requests to BigML.io.
 * 
 * This module cannot only save you a few credits, but also enormously reduce
 * the latency for each prediction and let you use your models offline.
 *
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the pca/id below):
 *
 *
 * import org.bigml.binding.LocalPca;
 * 
 * // API client 
 * BigMLClient api = new BigMLClient();
 *
 * JSONObject pca = api. getPca("pca/5026965515526876630001b2"); 
 * LocalPca localPca = new LocalPca(pca)
 *
 * JSONObject predictors = (JSONObject) JSONValue.parse( 
 * 	"{\"petal length\": 3, \"petal width\": 1," + 
 *   "\"sepal length\": 1, \"sepal width\": 0.5}" 
 * );
 * localPca.projection(predictors);
 * 
 */

public class LocalPca extends ModelFields implements Serializable {

	private static final long serialVersionUID = 1L;

	private static String PCA_RE = "^pca/[a-f,0-9]{24}$";

	/**
	 * Logging
	 */
	static Logger logger = LoggerFactory.getLogger(LocalPca.class.getName());

	private JSONArray inputFields = null;
	private JSONObject datasetFieldTypes;
	private JSONObject categoriesProbabilities;
	private int famdj;
	private JSONArray components;
	private JSONArray eigenvectors;
	private Boolean standardized = false;
	private JSONArray cumulativeVariance;
	private JSONArray variance;
	private JSONObject textStats;
	private String defaultNumericValue = null;
	
	public LocalPca(JSONObject pca) throws Exception {
        this(null, pca);
    }
	
	public LocalPca(BigMLClient bigmlClient, JSONObject pca) 
			throws Exception {
		
		super(bigmlClient, pca);
    	pca = this.model;

		try {
			this.inputFields = (JSONArray) Utils.getJSONObject(pca,
					"input_fields");
			this.datasetFieldTypes = (JSONObject) Utils.getJSONObject(pca,
					"dataset_field_types");
			int categorical = ((Number) datasetFieldTypes.get("categorical"))
					.intValue();
			int total = ((Number) datasetFieldTypes.get("total")).intValue();
			this.famdj = (categorical == total ? 1 : categorical);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (pca.containsKey("pca") && pca.get("pca") instanceof JSONObject) {

			JSONObject status = (JSONObject) Utils.getJSONObject(pca, "status");

			if (status != null && status.containsKey("code")
					&& AbstractResource.FINISHED == ((Number) status
							.get("code")).intValue()) {

				JSONObject pcaInfo = (JSONObject) Utils.getJSONObject(pca,
						"pca");

				JSONObject fields = (JSONObject) Utils.getJSONObject(pcaInfo,
						"fields", new JSONObject());

				this.defaultNumericValue = (String) pca.get("default_numeric_value");

				if (inputFields == null) {
					inputFields = new JSONArray();
					String[] inputFieldsArray = new String[fields.values()
							.size()];
					for (Object fieldId : fields.keySet()) {
						int columnNumber = ((Number) Utils.getJSONObject(fields,
								fieldId + ".column_number")).intValue();
						inputFieldsArray[columnNumber] = (String) fieldId;
					}
					inputFields.addAll(Arrays.asList(inputFieldsArray));
				}

				super.initialize((JSONObject) fields, null, null, null, true,
						true, false);

				this.categoriesProbabilities = new JSONObject();
				for (Object fieldId : categories.keySet()) {
					JSONObject field = (JSONObject) this.fields.get(fieldId);

					JSONArray cats = (JSONArray) Utils.getJSONObject(field,
							"summary.categories", new JSONArray());

					Double total = new Double(0);
					ArrayList<Double> probabilities = new ArrayList<Double>();
					for (Object cat : cats) {
						double probability = ((Number) ((JSONArray) cat).get(1))
								.doubleValue();
						probabilities.add(probability);
						total += probability;
					}

					double missingCount = ((Number) Utils.getJSONObject(fields,
							fieldId + ".summary.missing_count")).doubleValue();
					if (missingCount > 0) {
						probabilities.add(missingCount);
						total += missingCount;
					}

					if (total > 0) {
						for (int i = 0; i < probabilities.size(); i++) {
							probabilities.set(i, probabilities.get(i) / total);
						}
					}
					this.categoriesProbabilities.put(fieldId, probabilities);
				}

				this.components = (JSONArray) pcaInfo.get("components");
				this.eigenvectors = (JSONArray) pcaInfo.get("eigenvectors");
				this.cumulativeVariance = (JSONArray) pcaInfo
						.get("cumulative_variance");
				this.textStats = (JSONObject) pcaInfo.get("text_stats");
				this.standardized = (Boolean) pcaInfo.get("standardized");
				this.variance = (JSONArray) pcaInfo.get("variance");
			} else {
				throw new Exception("The pca isn't finished yet");
			}
		} else {
			throw new Exception(String.format("Cannot create the Pca instance. "
					+ "Could not find the 'pca' key in "
					+ "the resource:\n\n%s", pca));
		}

	}
	
	/**
	 * Returns reg expre for model Id.
	 */
    public String getModelIdRe() {
		return PCA_RE;
	}
    
    /**
	 * Returns bigml resource JSONObject.
	 */
    public JSONObject getBigMLModel(String modelId) {
		return (JSONObject) this.bigmlClient.getPca(modelId);
	}

	/**
	 * Returns the projection of input data in the new components.
	 * 
	 * @param inputData
	 *            Input data to be predicted
	 * @param maxComponents
	 * 			  Max number of components
	 * @param varianceThreshold
	 * 			  Threshold for variance
	 * @param full
	 *            Boolean that controls whether to include the prediction's
	 *            attributes. By default, only the prediction is produced. If
	 *            set to True, the rest of available information is added in a
	 *            dictionary format. The dictionary keys can be: - prediction:
	 *            the prediction value - probability: prediction's probability -
	 *            distribution: distribution of probabilities for each of the
	 *            objective field classes - unusedFields: list of fields in the
	 *            input data that
	 *
	 * @return projection for the input data
	 */
	public JSONObject projection(JSONObject inputData,
			Integer maxComponents, Double varianceThreshold, Boolean full) {

		if (full == null) {
			full = false;
		}

		// Checks and cleans inputData leaving the fields used in the model
		inputData = filterInputData(inputData, full);

		List<String> unusedFields = (List<String>) inputData
				.get("unusedFields");
		inputData = (JSONObject) inputData.get("newInputData");

		// Strips affixes for numeric values and casts to the final field type
		Utils.cast(inputData, this.fields);

		// Computes text and categorical field expansion
		Map<String, Object> uniqueTerms = uniqueTerms(inputData);
		
		// Creates an input vector with the values for all expanded fields.
		// The input mask marks the non-missing or categorical fields
		// The `missings` variable is a boolean indicating whether there's
		// non-categorical fields missing
		JSONObject expandedInput = expandInput(inputData, uniqueTerms);
		boolean missings = (Boolean) expandedInput.get("missings");
		ArrayList<Double> inputArray = (ArrayList<Double>) expandedInput
				.get("inputArray");
		ArrayList<Double> inputMask = (ArrayList<Double>) expandedInput
				.get("inputMask");
		
		ArrayList<List<Double>> components = new ArrayList<List<Double>>(
			this.eigenvectors.subList(0, this.eigenvectors.size()));
		
		if (maxComponents != null) {
			components = new ArrayList<List<Double>>(
					components.subList(0, maxComponents));
		}

		if (varianceThreshold != null) {
			for (int index = 0; index < this.cumulativeVariance
					.size(); index++) {
				Double cumulative = (Double) this.cumulativeVariance.get(index);
				if (cumulative > varianceThreshold) {
					components = new ArrayList<List<Double>>(
						components.subList(0, index + 1));
				}
			}
		}

		JSONArray inputs = new JSONArray();
		inputs.add(inputArray);
		
		ArrayList<List<Double>> dots = Utils.dot(components, inputs);
		ArrayList<Double> result = new ArrayList<Double>();
		for (List<Double> dot: dots) {
			result.add(dot.get(0));
		}
		
		if (missings) {
			ArrayList<Double> missingSums = missingFactors(inputMask);
			for (int i=0; i<result.size(); i++) {
				if (missingSums.get(i) > 0) {
					Double value = result.get(i);
					result.set(i, value / missingSums.get(i));
				}
			}
		}
		
		JSONObject projection = new JSONObject();
		for (int index=0; index < components.size(); index++) {
			projection.put("PC" + (index + 1), result.get(index));
		}
		return projection;
	}
	
	/**
	 * Returns the factors to divide the PCA values when input
     * data has missings
	 */
	private ArrayList<Double> missingFactors(ArrayList<Double> inputMask) {
		ArrayList<Double> sumEigenvectors = new ArrayList<Double>();
		
		for (Object rowObject: this.eigenvectors) {
			List<Double> row = (List<Double>) rowObject;
			List<Double> eigenvector = new ArrayList<Double>();
			for (int i=0; i<row.size(); i++) {
				eigenvector.add(((Number) row.get(i)).doubleValue() * 
								inputMask.get(i).doubleValue());
			}
			
			ArrayList<List<Double>> eigen = new ArrayList<List<Double>>();
			eigen.add(eigenvector);
			JSONArray eigenArray = new JSONArray();
			eigenArray.add(eigenvector);
			
			ArrayList<List<Double>> dots = Utils.dot(eigen, eigenArray);
			sumEigenvectors.add(dots.get(0).get(0));
		}
		return sumEigenvectors;
	}
	
	/**
	 * Returns the quantities to be used as mean and stddev to normalize
	 */
	private JSONObject getMeanStdev(JSONObject field, String fieldId,
			Integer index) {
		JSONObject result = new JSONObject();
		String optType = (String) Utils.getJSONObject(field, "optype");

		if ("categorical".equals(optType) && index != null) {
			Double mean = ((Number) ((ArrayList<Double>) this.categoriesProbabilities
					.get(fieldId)).get(index)).doubleValue();
			Double stddev = this.famdj * Math.sqrt(mean * this.famdj);

			result.put("mean", mean);
			result.put("stdev", stddev);
		} else {
			if ("numeric".equals(optType)) {
				Double mean = ((Number) Utils.getJSONObject(field,
						"summary.mean", 0)).doubleValue();
				Double stddev = ((Number) Utils.getJSONObject(field,
						"summary.standard_deviation", 0)).doubleValue();
				result.put("mean", mean);
				result.put("stdev", stddev);
			} else {
				JSONArray means = (JSONArray) Utils
						.getJSONObject(this.textStats, fieldId + ".means");
				JSONArray stddevs = (JSONArray) Utils.getJSONObject(
						this.textStats, fieldId + ".standard_deviations");

				result.put("mean", (Double) means.get(index));
				result.put("stdev",
						stddevs != null ? (Double) stddevs.get(index) : 0);
			}
		}
		return result;
	}

	/**
	 * Returns an array that represents the frequency of terms as ordered in the
	 * reference `terms` parameter.
	 */
	private ArrayList<Double> getTermsArray(List<String> terms,
			Map<String, Object> uniqueTerms, JSONObject field, String fieldId) {
		
		ArrayList<Double> termsArray = new ArrayList<Double>();

		Double[] termsArrayAux = new Double[terms.size()];
		Arrays.fill(termsArrayAux, 0.0);
		termsArray.addAll(Arrays.asList(termsArrayAux));

		String optType = (String) Utils.getJSONObject(field, "optype");
		double missingCount = ((Number) Utils.getJSONObject(field,
				"summary.missing_count")).doubleValue();
		
		if ("categorical".equals(optType) && missingCount > 0) {
			termsArray.add(uniqueTerms.keySet().contains(fieldId) ? 0.0 : 1.0);
		}
		
		try {
			Double frequency = (Double) uniqueTerms.get(fieldId);
			int index = terms.indexOf(fieldId);
			termsArray.set(index, frequency);
		} catch (Exception e) {
			if (uniqueTerms.get(fieldId) instanceof HashMap) {
				HashMap map = (HashMap) uniqueTerms.get(fieldId);
				for (Object key: map.keySet()) {
					Double frequency =  ((Number) map.get((String) key)).doubleValue();
					int index = terms.indexOf((String) key);
					termsArray.set(index, frequency);
				}	
			} else {
				JSONObject jsonObject = (JSONObject) uniqueTerms.get(fieldId);
				for (Object key: jsonObject.keySet()) {
					Double frequency =  ((Number) jsonObject.get((String) key)).doubleValue();
					int index = terms.indexOf((String) key);
					termsArray.set(index, frequency);
				}	
			}
		}
		return termsArray;
	}

	/**
	 * Creates an input array with the values in input_data and uniqueTerms and
	 * the following rules: - fields are ordered as inputFields - numeric fields
	 * contain the value or 0 if missing - categorial fields are one-hot encoded
	 * and classes are sorted as they appear in the field summary. If
	 * missing_count > 0 a last missing element is added set to 1 if the field
	 * is missing and o otherwise - text and items fields are expanded into
	 * their elements as found in the corresponding summmary information and
	 * their values treated as numerics.
	 */
	private JSONObject expandInput(JSONObject inputData,
			Map<String, Object> uniqueTerms) {

		ArrayList<Double> inputArray = new ArrayList<Double>();
		ArrayList<Double> inputMask = new ArrayList<Double>();
		boolean missings = false;

		for (Object fieldIdent : inputFields) {
			String fieldId = (String) fieldIdent;
			JSONObject field = (JSONObject) fields.get(fieldId);
			String optType = (String) Utils.getJSONObject(field, "optype");

			if ("numeric".equals(optType)) {
				inputMask.add(inputData.keySet().contains(fieldId) ? 1.0 : 0.0);
				Double value = 0.0;
				if (inputData.keySet().contains(fieldId)) {
					value = ((Number) Utils.getJSONObject(inputData, fieldId,
							0)).doubleValue();

					if (this.standardized) {
						JSONObject meanStdev = getMeanStdev(field,
								(String) fieldId, null);
						Double mean = (Double) meanStdev.get("mean");
						Double stdev = (Double) meanStdev.get("stdev");
						value -= mean;
						if (stdev > 0) {
							value /= stdev;
						}
					}
				} else {
					missings = true;
					value = 0.0;
				}
				inputArray.add(value);
			} else {
				List<String> terms = null;
				if ("categorical".equals(optType)) {
					terms = (List<String>) this.categories.get(fieldId);
				}
				if ("text".equals(optType)) {
					terms = this.tagClouds.get(fieldId);
				}
				if ("items".equals(optType)) {
					terms = this.items.get(fieldId);
				}

				ArrayList<Double> newInputs = new ArrayList<Double>();
				Double[] newInputsAux = new Double[terms.size()];
				if (uniqueTerms.keySet().contains(fieldId)) {
					newInputs = getTermsArray(terms, uniqueTerms, field,
							fieldId);
					newInputsAux = new Double[newInputs.size()];
					Arrays.fill(newInputsAux, 1.0);
					inputMask.addAll(Arrays.asList(newInputsAux));
				} else {
					Arrays.fill(newInputsAux, 0.0);
					newInputs.addAll(Arrays.asList(newInputsAux));

					if (!"categorical".equals(optType)) {
						missings = true;
						inputMask.addAll(Arrays.asList(newInputsAux));
					} else {
						Arrays.fill(newInputsAux, 1.0);
						inputMask.addAll(Arrays.asList(newInputsAux));
						double missingCount = ((Number) Utils
								.getJSONObject(field, "summary.missing_count"))
										.doubleValue();
						if (missingCount > 0) {
							newInputs.add(1.0);
							inputMask.add(1.0);
						}
					}
				}
				
				if (this.standardized) {
					for (int index = 0; index < newInputs.size(); index++) {
						Double frequency = newInputs.get(index);

						JSONObject meanStdev = getMeanStdev(field,
								(String) fieldId, index);
						Double mean = (Double) meanStdev.get("mean");
						Double stdev = (Double) meanStdev.get("stdev");

						newInputs.set(index, frequency - mean);
						if (stdev > 0) {
							newInputs.set(index, newInputs.get(index) / stdev);
						}
					}
					// indexes of non-missing values
				}

				inputArray.addAll(newInputs);
			}
		}

		JSONObject result = new JSONObject();
		result.put("inputArray", inputArray);
		result.put("missings", missings);
		result.put("inputMask", inputMask);
		return result;
	}

}