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
 * This module defines an Anomaly Detector to score anomalies in a dataset locally
 * or embedded into your application without needing to send requests to BigML.io.
 *
 * This module cannot only save you a few credits, but also enormously
 * reduce the latency for each prediction and let you use your models
 * offline.
 *
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the model/id below):
 *
 * // API client
 * BigMLClient api = new BigMLClient();
 *
 * // Retrieve a remote anomaly by id
 * JSONObject jsonAnomaly = api.getAnomaly("anomaly/551aa203af447f5484000ec0");
 *
 * // A lightweight wrapper around an Anomaly resurce
 * LocalAnomaly localAnomaly = new LocalAnomaly(jsonAnomaly);
 *
 * // Input data
 * JSONObject inputData = (JSONObject) JSONValue.parse("{\"src_bytes\": 350}");
 *
 * // Calculate score
 * localAnomaly.score(inputData);
 *
 */
public class LocalAnomaly extends ModelFields implements Serializable {

    private static final long serialVersionUID = 1L;

    private JSONObject anomaly;

    private String anomalyId;

    private JSONArray inputFields;

    private Integer sampleSize = null;

    private Double meanDepth = null;

    private Double expectedMeanDepth = null;

    private List<JSONObject> topAnomalies;

    private List<AnomalyTree> iforest;

    public LocalAnomaly(JSONObject anomalyData) throws Exception {
        super();

        if (anomalyData.get("resource") == null) {
            throw new Exception(
                    "Cannot create the Anomaly instance. Could not find the 'resource' key in the resource");
        }

        anomalyId = (String) anomalyData.get("resource");

        anomaly = anomalyData;

        if (anomaly.containsKey("object") && anomaly.get("object") instanceof Map) {
            anomaly = (JSONObject) anomaly.get("object");
        }

        if (anomaly.get("sample_size") != null) {
            this.sampleSize = ((Number) anomaly.get("sample_size")).intValue();
        }
        this.inputFields = (JSONArray) anomaly.get("input_fields");

        if (anomaly.containsKey("model") && anomaly.get("model") instanceof Map) {
            JSONObject model = (JSONObject) anomaly.get("model");
            super.initialize((JSONObject) model.get("fields"), null, null, null);

            if (model.containsKey("top_anomalies") && model.get("top_anomalies") instanceof List) {
                if (model.get("mean_depth") != null) {
                    this.meanDepth = ((Number) model.get("mean_depth")).doubleValue();
                }

                JSONObject status = (JSONObject) anomaly.get("status");
                if( status != null &&
                        status.containsKey("code") &&
                        AbstractResource.FINISHED == ((Number) status.get("code")).intValue() ) {

                    this.expectedMeanDepth = null;
                    if (this.meanDepth == null || this.sampleSize == null) {
                        throw new Exception("The anomaly data is not complete. Score will not be available.");
                    } else {
                        double defaultDepth = (
                                2 * (0.5772156649 +
                                        Math.log(this.sampleSize - 1) -
                                        ((float) (this.sampleSize - 1) / this.sampleSize)));
                        this.expectedMeanDepth = Math.min(this.meanDepth, defaultDepth);
                    }

                    this.iforest = new ArrayList<AnomalyTree>();
                    List iforest = (List) Utils.getJSONObject(anomaly, "model.trees", new JSONArray());
                    if (!iforest.isEmpty()) {
                        for (Object anomalyTree : iforest) {
                            this.iforest.add(new AnomalyTree((JSONObject) ((JSONObject) anomalyTree).get("root"),
                                    objectiveFieldId, fields));
                        }
                    }
                    this.topAnomalies = (List<JSONObject>) Utils.getJSONObject(anomaly, "model.top_anomalies", new JSONArray());
                } else {
                    throw new Exception("The anomaly isn't finished yet");
                }
            } else {
                throw new Exception(String.format("Cannot create the Anomaly instance. Could not" +
                        " find the 'top_anomalies' key in the" +
                        " resource:\n\n%s", ((JSONObject) anomaly.get("model")).keySet()));
            }

        }
    }

    /**
     * Returns the anomaly score given by the iforest
     *
     * To produce an anomaly score, we evaluate each tree in the iforest
     * for its depth result (see the depth method in the AnomalyTree
     * object for details). We find the average of these depths
     * to produce an `observed_mean_depth`. We calculate an
     * `expected_mean_depth` using the `sample_size` and `mean_depth`
     * parameters which come as part of the forest message.
     * We combine those values as seen below, which should result in a
     * value between 0 and 1.
     */
    public double score(JSONObject inputData, boolean byName) {
        // Checks and cleans input_data leaving the fields used in the model
        inputData = filterInputData(inputData, byName);

        // Strips affixes for numeric values and casts to the final field type
        Utils.cast(inputData, fields);

        int depthSum = 0;

        if( this.iforest == null || this.iforest.isEmpty() ) {
            throw new IllegalStateException("We could not find the iforest information to " +
                    "compute the anomaly score. Please, rebuild your " +
                    "Anomaly object from a complete anomaly detector " +
                    "resource.");
        }

        for (AnomalyTree anomalyTree : this.iforest) {
            depthSum += anomalyTree.depth(inputData).getDepth();
        }

        double observedMeanDepth = ((double) depthSum) / ((double) this.iforest.size());
        return Math.pow(2, (- observedMeanDepth / this.expectedMeanDepth));
    }

    /**
     * Returns the LISP expression needed to filter the subset of
     * top anomalies. When include is set to True, only the top
     * anomalies are selected by the filter. If set to False, only the
     * rest of the dataset is selected.
     */
    public String filter(boolean include) {
        List<String> anomalyFilters = new ArrayList<String>();

        for (JSONObject anomaly : topAnomalies) {
            List<String> filterRules = new ArrayList<String>();
            List row = (List) Utils.getJSONObject(anomaly, "row", new JSONArray());
            for(int index = 0; index < row.size(); index++) {
                String fieldId = (String) this.inputFields.get(index);
                Object value = row.get(index);
                if( value == null ) {
                    filterRules.add(String.format("(missing? \"%s\")", fieldId));
                } else {
                    String optType = (String) Utils.getJSONObject(super.fields, String.format("%s.optype",fieldId));
                    if ( "categorical".equals(optType) || "text".equals(optType) ) {
                        value = String.format("\"%s\"", value.toString());
                    }
                    filterRules.add(String.format("(= (f \"%s\") %s)", fieldId, value));
                }
            }
            anomalyFilters.add(String.format("(and %s)", Utils.join(filterRules, " ")));
        }

        String anomaliesFilter = Utils.join(anomalyFilters, " ");
        if( include ) {
            if( anomalyFilters.size() == 1) {
                return anomaliesFilter;
            }

            return String.format("(or %s)", anomaliesFilter);
        } else {
            return String.format("(not (or %s))", anomaliesFilter);
        }
    }
}
