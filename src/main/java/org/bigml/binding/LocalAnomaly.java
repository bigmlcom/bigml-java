package org.bigml.binding;

import org.bigml.binding.localanomaly.AnomalyTree;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A local Predictive Anomaly Detector.
 *
 * This module defines an Anomaly Detector to score anomalies in a dataset
 * locally or embedded into your application without needing to send requests to
 * BigML.io.
 *
 * This module cannot only save you a few credits, but also enormously reduce
 * the latency for each prediction and let you use your models offline.
 *
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the model/id below):
 *
 * // API client BigMLClient api = new BigMLClient();
 *
 * // Retrieve a remote anomaly by id JSONObject jsonAnomaly =
 * api.getAnomaly("anomaly/551aa203af447f5484000ec0");
 *
 * // A lightweight wrapper around an Anomaly resource LocalAnomaly localAnomaly
 * = new LocalAnomaly(jsonAnomaly);
 *
 * // Input data JSONObject inputData = (JSONObject)
 * JSONValue.parse("{\"src_bytes\": 350}");
 *
 * // Calculate score localAnomaly.score(inputData);
 *
 */
public class LocalAnomaly extends ModelFields implements Serializable {

	private static final long serialVersionUID = 1L;

	private static String ANOMALY_RE = "^anomaly/[a-f,0-9]{24}$";

	private static Double DEPTH_FACTOR = 0.5772156649;

	private JSONArray inputFields;
	private Integer sampleSize = null;
	private Double meanDepth = null;
	private Double normalizationFactor = null;
	private Double nodesMeanDepth = null;
	private Double norm = null;
	private Double expectedMeanDepth = null;
	private List<JSONObject> topAnomalies;
	private List<AnomalyTree> iforest;
	private String defaultNumericValue = null;
	private JSONArray idFields = new JSONArray();

	/*
	 * Computing the normalization factor for simple anomaly detectors
	 */
	private static Double normFactor(Integer sampleSize, Double meanDepth) {
		if (meanDepth != null) {

			double defaultDepth = meanDepth;
			if (sampleSize != 1) {
				defaultDepth = (2
						* (DEPTH_FACTOR + Math.log(sampleSize - 1) - ((float) (sampleSize - 1) / sampleSize)));
			}
			return Math.min(meanDepth, defaultDepth);
		}

		return null;
	}

	public LocalAnomaly(JSONObject anomaly) throws Exception {
		this(null, anomaly);
	}

	public LocalAnomaly(BigMLClient bigmlClient, JSONObject anomaly) throws Exception {

		super(bigmlClient, anomaly);
		anomaly = this.model;

		if (anomaly.get("sample_size") != null) {
			this.sampleSize = ((Number) anomaly.get("sample_size")).intValue();
		}
		this.inputFields = (JSONArray) anomaly.get("input_fields");
		this.defaultNumericValue = (String) anomaly.get("default_numeric_value");

		if (anomaly.get("id_fields") != null) {
			this.idFields = (JSONArray) anomaly.get("id_fields");
		}

		if (anomaly.containsKey("model") && anomaly.get("model") instanceof Map) {
			JSONObject model = (JSONObject) anomaly.get("model");
			super.initialize((JSONObject) model.get("fields"), null, null, null);

			if (model.containsKey("top_anomalies") && model.get("top_anomalies") instanceof List) {
				if (model.get("mean_depth") != null) {
					this.meanDepth = ((Number) model.get("mean_depth")).doubleValue();
				}
				if (model.get("normalization_factor") != null) {
					this.normalizationFactor = ((Number) model.get("normalization_factor")).doubleValue();
				}
				if (model.get("nodes_mean_depth") != null) {
					this.nodesMeanDepth = ((Number) model.get("nodes_mean_depth")).doubleValue();
				}

				JSONObject status = (JSONObject) anomaly.get("status");
				if (status != null && status.containsKey("code")
						&& AbstractResource.FINISHED == ((Number) status.get("code")).intValue()) {

					this.expectedMeanDepth = null;
					if (this.meanDepth == null || this.sampleSize == null) {
						throw new Exception("The anomaly data is not complete. Score will not be available.");
					} else {
						if (this.normalizationFactor != null) {
							this.norm = this.normalizationFactor;
						} else {
							this.norm = normFactor(this.sampleSize, this.meanDepth);
						}
					}

					this.iforest = new ArrayList<AnomalyTree>();
					List iforest = (List) Utils.getJSONObject(anomaly, "model.trees", new JSONArray());
					if (!iforest.isEmpty()) {
						for (Object anomalyTree : iforest) {
							this.iforest.add(new AnomalyTree((JSONObject) ((JSONObject) anomalyTree).get("root"),
									objectiveFieldId, fields));
						}
					}
					this.topAnomalies = (List<JSONObject>) Utils.getJSONObject(anomaly, "model.top_anomalies",
							new JSONArray());
				} else {
					throw new Exception("The anomaly isn't finished yet");
				}
			} else {
				throw new Exception(String.format("Cannot create the Anomaly instance. Could not"
						+ " find the 'top_anomalies' key in the" + " resource:\n\n%s",
						((JSONObject) anomaly.get("model")).keySet()));
			}

		}
	}

	/**
	 * Returns reg expre for model Id.
	 */
	public String getModelIdRe() {
		return ANOMALY_RE;
	}

	/**
	 * Returns bigml resource JSONObject.
	 */
	public JSONObject getBigMLModel(String modelId) {
		return (JSONObject) this.bigmlClient.getAnomaly(modelId);
	}

	/**
	 * Returns the anomaly score given by the iforest
	 *
	 * To produce an anomaly score, we evaluate each tree in the iforest for its
	 * depth result (see the depth method in the AnomalyTree object for details). We
	 * find the average of these depths to produce an `observed_mean_depth`. We
	 * calculate an `expected_mean_depth` using the `sample_size` and `mean_depth`
	 * parameters which come as part of the forest message. We combine those values
	 * as seen below, which should result in a value between 0 and 1.
	 * 
	 * @param inputData
	 * 				an object with field's id/value pairs representing the
	 *              instance you want to get the anomaly score for
	 *              
	 * @return the anomaly score for the input data
	 */
	public double score(JSONObject inputData) {
		// Corner case with only one record
		if (this.sampleSize == 1 && this.normalizationFactor == null) {
			return 1.0;
		}

		// Checks and cleans input_data leaving the fields used in the model
		inputData = filterInputData(inputData);

		// Strips affixes for numeric values and casts to the final field type
		Utils.cast(inputData, fields);

		int depthSum = 0;

		if (this.iforest == null || this.iforest.isEmpty()) {
			throw new IllegalStateException(
					"We could not find the iforest information to " + "compute the anomaly score. Please, rebuild your "
							+ "Anomaly object from a complete anomaly detector " + "resource.");
		}

		for (AnomalyTree anomalyTree : this.iforest) {
			depthSum += anomalyTree.depth(inputData).getDepth();
		}

		double observedMeanDepth = ((double) depthSum) / ((double) this.iforest.size());
		return Math.pow(2, (-observedMeanDepth / this.norm));
	}

	/**
	 * Returns the LISP expression needed to filter the subset of top anomalies.
	 * When include is set to True, only the top anomalies are selected by the
	 * filter. If set to False, only the rest of the dataset is selected.
	 * 
	 * @param include	boolean indicating if include anomalies filters in response
	 * 
	 * @return the lisp expression needed to filter the subset of top anomalies
	 */
	public String filter(boolean include) {
		List<String> anomalyFilters = new ArrayList<String>();

		for (JSONObject anomaly : topAnomalies) {
			List<String> filterRules = new ArrayList<String>();
			List row = (List) Utils.getJSONObject(anomaly, "row", new JSONArray());
			for (int index = 0; index < row.size(); index++) {
				String fieldId = (String) this.inputFields.get(index);
				Object value = row.get(index);
				if (value == null) {
					filterRules.add(String.format("(missing? \"%s\")", fieldId));
				} else {
					String optType = (String) Utils.getJSONObject(super.fields, String.format("%s.optype", fieldId));
					if ("categorical".equals(optType) || "text".equals(optType)) {
						value = String.format("\"%s\"", value.toString());
					}
					filterRules.add(String.format("(= (f \"%s\") %s)", fieldId, value));
				}
			}
			anomalyFilters.add(String.format("(and %s)", Utils.join(filterRules, " ")));
		}

		String anomaliesFilter = Utils.join(anomalyFilters, " ");
		if (include) {
			if (anomalyFilters.size() == 1) {
				return anomaliesFilter;
			}

			return String.format("(or %s)", anomaliesFilter);
		} else {
			return String.format("(not (or %s))", anomaliesFilter);
		}
	}

	/**
	 * Checks whether input data is missing a numeric field and fills it with
	 * the average quantity set in default_numeric_value
	 */
	public JSONObject fillNumericDefaults(JSONObject inputData) {
		for (Object fieldId : fields.keySet()) {
			JSONObject field = (JSONObject) fields.get(fieldId);
			String optype = (String) Utils.getJSONObject(this.fields, fieldId + ".optype");

			if (!idFields.contains(fieldId) &&
				"numeric".equals(optype) &&
				inputData.get(fieldId) == null &&
				this.defaultNumericValue != null) {

				double defaultValue = 0.0;
				if (!"zero".equals(this.defaultNumericValue)) {
					defaultValue = ((Number) Utils.getJSONObject(field,
						"summary." + this.defaultNumericValue, 0)).doubleValue();
				}
				inputData.put(fieldId, defaultValue);
			}
		}
		return inputData;
	}

}