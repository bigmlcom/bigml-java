package org.bigml.binding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bigml.binding.utils.Utils;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.laminar.MathOps;

import org.apache.commons.math3.distribution.TDistribution;


/**
 * A local Predictive Linear Regression.
 *
 * This module defines a Linear Regression to make predictions locally
 * or embedded into your application without needing to send requests to
 * BigML.io.
 *
 * This module cannot only save you a few credits, but also enormously reduce
 * the latency for each prediction and let you use your linear regressions
 * offline.
 *
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the
 * linearregression/id below):
 *
 *
 * import org.bigml.binding.LocalLinearRegression;
 *
 *  // API client
 * BigMLClient api = new BigMLClient();
 *
 * JSONObject linearRegression = api.
 *      getLinearRegression("linearregression/5026965515526876630001b2");
 * LocalLinearRegression linear =
 *      LocalLinearRegression(linearRegression);
 *
 * JSONObject predictors = JSONValue.parse("
 *      {\"petal length\": 3, \"petal width\": 1,
 *       \"sepal length\": 1, \"sepal width\": 0.5}");
 *
 * linear.predict(predictors, true)
 *
 */
public class LocalLinearRegression extends ModelFields {

    private static final long serialVersionUID = 1L;

    static String LINEARREGRESSION_RE = "^linearregression/[a-f,0-9]{24}$";

    static HashMap<String, String> EXPANSION_ATTRIBUTES = new HashMap<String, String>();
    static {
        EXPANSION_ATTRIBUTES.put("categorical", "categories");
        EXPANSION_ATTRIBUTES.put("text", "tag_clouds");
        EXPANSION_ATTRIBUTES.put("items", "items");
    }

    protected static final String[] OPTIONAL_FIELDS = {
            "categorical", "text", "items", "datetime" };

    private final String DUMMY = "dummy";
    private final String CONTRAST = "contrast";
    private final String OTHER = "other";

    private final Double ALPHA_FACTOR =  0.975; // alpha = 0.05


    /**
     * Logging
     */
    static Logger logger = LoggerFactory
            .getLogger(LocalLogisticRegression.class.getName());

    private String linearRegressionId;
    private JSONArray inputFields = null;
    private JSONObject datasetFieldTypes = null;
    private String objectiveField = null;
    private JSONArray objectiveFields = null;
    private String weightField;
    private JSONArray coefficients = null;
    private Boolean bias;
    private JSONObject fieldCodings;
    private JSONObject stats = null;
    private JSONArray invXtx = null;
    private Double tcrit = null;
    private Double meanSquaredError = null;
    private Long numberOfParameters = null;
    private Long numberOfSamples = null;
    private String defaultNumericValue = null;

    public LocalLinearRegression(JSONObject linear) throws Exception {
        this(null, linear);
    }

    public LocalLinearRegression(BigMLClient bigmlClient,
                                 JSONObject linear) throws Exception {
    	
    	super(bigmlClient, linear);
    	linear = this.model;

        linearRegressionId = (String) linear.get("resource");

        // Check json structure
        inputFields = (JSONArray) Utils.getJSONObject(linear, "input_fields");
        datasetFieldTypes = (JSONObject) Utils.getJSONObject(linear,
                "dataset_field_types");
        weightField = (String) Utils.getJSONObject(linear, "weight_field");
        objectiveField = (String) Utils.getJSONObject(linear,
                "objective_field");
        objectiveFields = (JSONArray) Utils.getJSONObject(linear,
                "objective_fields");
        if (datasetFieldTypes == null || inputFields == null
                || (objectiveField == null && objectiveFields == null)) {
            throw new Exception(
                    "Failed to find the linear regression expected "
                            + "JSON structure. Check your arguments.");
        }

        if (linear.containsKey("linear_regression")
                && linear.get("linear_regression") instanceof JSONObject) {

            JSONObject status = (JSONObject) Utils.getJSONObject(linear,
                    "status");

            if (status != null && status.containsKey("code")
                    && AbstractResource.FINISHED == ((Number) status
                            .get("code")).intValue()) {

                JSONObject linearInfo = (JSONObject) Utils
                        .getJSONObject(linear, "linear_regression");

                JSONObject fields = (JSONObject) Utils.getJSONObject(
                        linearInfo, "fields", new JSONObject());

                this.defaultNumericValue = (String) linear.get("default_numeric_value");

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

                coefficients = (JSONArray) Utils.getJSONObject(
                        linearInfo, "coefficients", new JSONArray());

                bias = (Boolean) Utils.getJSONObject(linearInfo, "bias", true);

                // initialize ModelFields
                super.initialize((JSONObject) fields, null, null, null,
                                 true, true, true);

                Object fieldCodingsObj = (Object) Utils.getJSONObject(
                        linearInfo, "field_codings");
                if (fieldCodingsObj!=null && fieldCodingsObj instanceof JSONArray) {
                    formatFieldCodings((JSONArray) fieldCodingsObj);
                } else {
                    fieldCodings = (JSONObject) Utils.getJSONObject(
                        linearInfo, "field_codings", new JSONObject());
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

                this.numberOfParameters = ((Number) Utils.getJSONObject(
                        linearInfo, "number_of_parameters")).longValue();

                stats = (JSONObject) Utils.getJSONObject(
                    linearInfo, "stats", new JSONObject());

                if (stats != null && stats.containsKey("xtx_inverse")) {
                    this.invXtx = (JSONArray) Utils.getJSONObject(stats, "xtx_inverse");
                    this.meanSquaredError = ((Number) Utils.getJSONObject(
                    	stats, "mean_squared_error")).doubleValue();
                    this.numberOfSamples = ((Number) Utils.getJSONObject(
                    		stats, "number_of_samples")).longValue();

                    // to be used in predictions
                    TDistribution tdist = new TDistribution(
                        this.numberOfSamples - this.numberOfParameters);

                    this.tcrit = tdist.inverseCumulativeProbability(ALPHA_FACTOR);
                }

            } else {
                throw new Exception(
                        "The linear regression isn't finished yet");
            }

        } else {
            throw new Exception(String
                    .format("Cannot create the LinearRegression instance. "
                            + "Could not find the 'linear_regression' key in "
                            + "the resource:\n\n%s", linear));
        }

    }
    
    /**
	 * Returns reg expre for model Id.
	 */
    public String getModelIdRe() {
		return LINEARREGRESSION_RE;
	}
    
    /**
	 * Returns bigml resource JSONObject.
	 */
    public JSONObject getBigMLModel(String modelId) {
		return (JSONObject) this.bigmlClient.getLinearRegression(modelId);
	}

    /**
     * Returns the resourceId
     */
    public String getResourceId() {
        return linearRegressionId;
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
     * Returns an array that represents the frequency of terms as ordered in the
     * reference `terms` parameter.
     */
    private ArrayList<Double> getTermsArray(List<String> terms,
            Map<String, Object> uniqueTerms, JSONObject field, String fieldId) {

        ArrayList<Double> termsArray = new ArrayList<Double>();

        Double[] termsArrayAux = new Double[terms.size()];
        Arrays.fill(termsArrayAux, 0.0);
        termsArray.addAll(Arrays.asList(termsArrayAux));

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
     * Returns the prediction and the confidence intervals
     *
     * input_data: Input data to be predicted
     */
    private ArrayList<Double> categoricalEncoding(
            ArrayList<Double> newInputs, String fieldId, boolean compact) {

        JSONObject fieldCoding = (JSONObject) fieldCodings.get(fieldId);
        JSONArray projections = (JSONArray) Utils.getJSONObject(fieldCoding, CONTRAST);
        if (projections == null) {
            projections = (JSONArray) Utils.getJSONObject(fieldCoding, OTHER);
        }

        if (projections != null) {
            JSONArray inputs = new JSONArray();
            inputs.add(newInputs);

            ArrayList<List<Double>> dots = MathOps.dot(projections, inputs);
            for (List<Double> dot: dots) {
                newInputs.add(dot.get(0));
            }
        }

        if (compact && fieldCoding.get(DUMMY) != null) {
            String dummyClass = (String) fieldCoding.get(DUMMY);
            int index = ((List<String>) this.categories.get(fieldId)).indexOf(dummyClass);

            ArrayList<Double> catNewInputs = new ArrayList<Double>(newInputs.subList(0, index));
            if (newInputs.size() > (index+1)) {
                catNewInputs.addAll(newInputs.subList(index + 1, newInputs.size()));
            }
            newInputs = catNewInputs;
        }

        return newInputs;
    }


    /**
     * Computes the confidence interval for the prediction
     */
    private HashMap<String, Object> confidenceBounds(ArrayList<Double> inputArray) {
        HashMap<String, Object> confidenceBoounds = new HashMap<String, Object>();

        JSONArray inputs = new JSONArray();
        inputs.add(inputArray);

        double product = ((List<Double>)MathOps.dot(MathOps.dot(inputs, this.invXtx), inputs).get(0)).get(0);

        double confidenceInterval = 0;
        double predictionInterval = 0;

        try {
            if (this.meanSquaredError != 0) {
                confidenceInterval = this.tcrit * Math.sqrt(this.meanSquaredError * product);
                predictionInterval = this.tcrit * Math.sqrt(this.meanSquaredError * (product + 1));
            }
        } catch (Exception e) {}

        confidenceBoounds.put("confidenceInterval", confidenceInterval);
        confidenceBoounds.put("predictionInterval", predictionInterval);
        return confidenceBoounds;
    }


    /**
     * Creates an input array with the values in inputData and
     * uniqueTerms and the following rules:
     *   - fields are ordered as input_fields
     *   - numeric fields contain the value or 0 if missing
     *   - categorial fields are one-hot encoded and classes are sorted as
     *     they appear in the field summary. If missing_count > 0 a last
     *     missing element is added set to 1 if the field is missing and 0
     *     otherwise
     *   - text and items fields are expanded into their elements as found
     *     in the corresponding summmary information and their values treated
     *     as numerics.
     */
    private ArrayList<Double> expandInput(JSONObject inputData,
            Map<String, Object> uniqueTerms, boolean compact) {

        ArrayList<Double> inputArray = new ArrayList<Double>();

        for (Object fieldIdent : inputFields) {
            String fieldId = (String) fieldIdent;
            JSONObject field = (JSONObject) fields.get(fieldId);
            String optType = (String) Utils.getJSONObject(field, "optype");

            boolean missings = false;
            ArrayList<Double> newInputs = new ArrayList<Double>();

            if ("numeric".equals(optType)) {
                Double value = 0.0;
                if (inputData.keySet().contains(fieldId)) {
                    value = ((Number) Utils.getJSONObject(inputData, fieldId,
							0)).doubleValue();
                } else {
                    missings = true;
                    value = 0.0;
                }
                newInputs.add(value);
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

                if (uniqueTerms.keySet().contains(fieldId)) {
                    newInputs = getTermsArray(terms, uniqueTerms, field,
                            fieldId);
                } else {
                    Double[] newInputsAux = new Double[terms.size()];
                    Arrays.fill(newInputsAux, 0.0);
                    newInputs.addAll(Arrays.asList(newInputsAux));
                    missings = true;
                }
            }

            Integer missingCount = ((Number) Utils.getJSONObject(
                    (JSONObject) field, "summary.missing_count", 0)).intValue();
            JSONObject fieldCoding = (JSONObject) fieldCodings.get(fieldId);

            if (missingCount > 0 ||
                (optType.equals("categorical") && fieldCoding.get("dummy") == null )) {
                newInputs.add(missings ? 1.0 : 0.0);
            }

            if ("categorical".equals(optType)) {
                newInputs = categoricalEncoding(newInputs, fieldId, compact);
            }

            inputArray.addAll(newInputs);
        }

        if (this.bias) {
            inputArray.add(1.0);
        }

        return inputArray;
    }


     /**
     * Returns the prediction and the confidence intervals
     *
     * @param inputData     Input data to be predicted
     * @param full
     *          Boolean that controls whether to include the prediction's
     *          attributes. By default, only the prediction is produced. If set
     *          to True, the rest of available information is added in a
     *          dictionary format. The dictionary keys can be:
     *             - prediction: the prediction value
     *             - unused_fields: list of fields in the input data that
     *
     */
    public HashMap<String, Object> predict(
            JSONObject inputData, Boolean full) {

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

        // Computes text and categorical field expansion
        Map<String, Object> uniqueTerms = uniqueTerms(inputData);

        // Creates an input vector with the values for all expanded fields.
        ArrayList<Double> inputArray = expandInput(inputData, uniqueTerms, false);
        ArrayList<Double> compactInputArray = expandInput(inputData, uniqueTerms, true);

        JSONArray coefficientsList = new JSONArray();
        coefficientsList.add(Utils.flattenList(this.coefficients));
        JSONArray inputs = new JSONArray();
        inputs.add(inputArray);

        ArrayList<List<Double>> dots = MathOps.dot(coefficientsList, inputs);
        double prediction = dots.get(0).get(0);

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("prediction", prediction);

        if (full) {
            result.put("unused_fields", unusedFields);
        }

        if (full && this.invXtx != null) {
            result.put("confidence_bounds", confidenceBounds(compactInputArray));
        }

        return result;
    }
}
