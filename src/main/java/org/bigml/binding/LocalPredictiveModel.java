/*
  A local Predictive Model.

  This module defines a Model to make predictions locally or
  embedded into your application without needing to send requests to
  BigML.io.

  This module cannot only save you a few credits, but also enormously
  reduce the latency for each prediction and let you use your models
  offline.

  You can also visualize your predictive model in IF-THEN rule format
  and even generate a java function that implements the model.

  Example usage (assuming that you have previously set up the BIGML_USERNAME
  and BIGML_API_KEY environment variables and that you own the model/id below):

  import org.bigml.binding.BigMLClient;
  import org.bigml.binding.resources.Model;

  BigMLClient bigmlClient = new BigMLClient();

  Model model = new Model(bigmlClient.getModel('model/5026965515526876630001b2'));
  model.predict("{\"petal length\": 3, \"petal width\": 1}");

  You can also see model in a IF-THEN rule format with:

  model.rules()

  Or auto-generate a java function code for the model with:

  model.java()
 */
package org.bigml.binding;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import org.bigml.binding.localmodel.Tree;
import org.bigml.binding.localmodel.TreeNodeFilter;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A lightweight wrapper around a Tree model.
 * 
 * Uses a BigML remote model to build a local version that can be used to
 * generate prediction locally.
 * 
 */
public class LocalPredictiveModel implements PredictionConverter {

    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(LocalPredictiveModel.class
            .getName());

    // Map operator str to its corresponding java operator
    static HashMap<String, String> JAVA_TYPES = new HashMap<String, String>();
    static {
        JAVA_TYPES.put(Constants.OPTYPE_CATEGORICAL + "-string", "String");
        JAVA_TYPES.put(Constants.OPTYPE_TEXT + "-string", "String");
        JAVA_TYPES.put(Constants.OPTYPE_DATETIME + "-string", "String");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-double", "Double");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-float", "Float");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-integer", "Float");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-int8", "Float");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-int16", "Float");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-int32", "Float");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-int64", "Float");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-day", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-month", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-year", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-hour", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-minute", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-second", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-millisecond", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-day-of-week", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-day-of-month", "Integer");
        JAVA_TYPES.put(Constants.OPTYPE_NUMERIC + "-boolean", "Boolean");
    }

    private JSONObject fields;
    private JSONObject root;
    private Tree tree;
    private String objectiveField;

    /**
     * Constructor
     * 
     * @param model
     *            the json representation for the remote model
     */
    public LocalPredictiveModel(JSONObject model) throws Exception {
        super();

        try {
            if (model.get("resource") == null) {
                throw new Exception(
                        "Cannot create the Model instance. Could not find the 'model' key in the resource");
            }
            if (!BigMLClient.getInstance().modelIsReady(model)) {
                throw new Exception("The model isn't finished yet");
            }

            String prefix = Utils.getJSONObject(model, "object") != null ? "object."
                    : "";

            this.fields = (JSONObject) Utils.getJSONObject(model, prefix
                    + "model.fields");
            if (Utils.getJSONObject(model, prefix + "model.model_fields") != null) {
                this.fields = (JSONObject) Utils.getJSONObject(model, prefix
                        + "model.model_fields");

                JSONObject modelFields = (JSONObject) Utils.getJSONObject(
                        model, prefix + "model.fields");
                Iterator iter = this.fields.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    if (modelFields.get(key) == null) {
                        throw new Exception(
                                "Some fields are missing to generate a local model. Please, provide a model with the complete list of fields.");
                    }
                }

                iter = this.fields.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    JSONObject field = (JSONObject) this.fields.get(key);
                    JSONObject modelField = (JSONObject) modelFields.get(key);
                    field.put("summary", modelField.get("summary"));
                    field.put("name", modelField.get("name"));
                }
            }

            this.root = (JSONObject) Utils.getJSONObject(model, prefix
                    + "model.root");

            String objectiveField;
            Object objectiveFields = Utils.getJSONObject(model, prefix
                    + "objective_fields");
            objectiveField = objectiveFields instanceof JSONArray ? (String) ((JSONArray) objectiveFields)
                    .get(0) : (String) objectiveFields;

            // Check duplicated field names
            uniquifyVarnames();

            this.tree = new Tree(root, this.fields, objectiveField,
                    (JSONObject) Utils.getJSONObject(model, prefix + "model.distribution.training"));
        } catch (Exception e) {
            logger.error("Invalid model structure", e);
            throw new InvalidModelException();
        }
    }

    /**
     * Describes and return the fields for this model.
     */
    public JSONObject fields() {
        return tree.listFields();
    }

    /**
     * Returns a list that includes all the leaves of the model.
     *
     * @return all the leave nodes
     */
    public List<Tree> getLeaves() {
        return this.tree.getLeaves(null);
    }

    /**
     * Returns a list that includes all the leaves of the model.
     *
     * @param filter should be a function that returns a boolean
     * when applied to each leaf node.
     *
     * @return all the leave nodes after apply the filter
     */
    public List<Tree> getLeaves(TreeNodeFilter filter) {
        return this.tree.getLeaves(filter);
    }

    /**
     * Returns True if the gini impurity of the node distribution
     * goes above the impurity threshold.
     *
     * @param impurityThreshold the degree of impurity
     *
     * @return all the leave nodes after apply the impurity threshold
     */
    public List<Tree> getImpureLeaves(final Double impurityThreshold) {
        return this.tree.getLeaves(new TreeNodeFilter() {
            @Override
            public boolean filter(Tree node) {
                Double nodeImpurity = node.getImpurity();
                return (nodeImpurity != null && nodeImpurity > impurityThreshold);
            }
        });
    }

    /**
     * Makes a prediction based on a number of field values.
     * 
     * The input fields must be keyed by field name.
     * 
     */
    @Deprecated
    public HashMap<Object, Object> predict(final String args)
            throws InputDataParseException {
        return predict(args, null);
    }

    /**
     * Makes a prediction based on a number of field values.
     * 
     * The input fields must be keyed by field name.
     */
    public HashMap<Object, Object> predict(final JSONObject args)
            throws InputDataParseException {
        return predict(args, null);
    }

    /**
     * Makes a prediction based on a number of field values.
     * 
     * The input fields must be keyed by field name.
     * 
     */
    @Deprecated
    public HashMap<Object, Object> predict(final String args, Boolean byName)
            throws InputDataParseException {
        return predict(args, byName, null);
    }

    /**
     * Makes a prediction based on a number of field values.
     * 
     * The input fields must be keyed by field name.
     */
    public HashMap<Object, Object> predict(final JSONObject args, Boolean byName)
            throws InputDataParseException {
        return predict(args, byName, null);
    }

    /**
     * Makes a prediction based on a number of field values using a Last Prediction Strategy
     *
     * By default the input fields must be keyed by field name but you can use `by_name`
     *  to input them directly keyed by id.
     * 
     */
    @Deprecated
    public HashMap<Object, Object> predict(final String args, Boolean byName,
            Boolean withConfidence) throws InputDataParseException {
        if (byName == null) {
            byName = true;
        }
        if (withConfidence == null) {
            withConfidence = false;
        }

        JSONObject argsData = (JSONObject) JSONValue.parse(args);
        if (!args.equals("") && !args.equals("") && argsData == null) {
            throw new InputDataParseException("Input data format not valid");
        }
        JSONObject inputData = argsData;


        return predict(inputData, byName, withConfidence);
    }

    /**
     * Makes a prediction based on a number of field values using a Last Prediction Strategy
     * 
     */
    public HashMap<Object, Object> predict(final JSONObject args,
            Boolean byName, Boolean withConfidence)
            throws InputDataParseException {
      return predict(args, byName, null, null, withConfidence, MissingStrategy.LAST_PREDICTION,
              null, null, null, null, null, null).get(0);
    }

    /**
     * Makes a prediction based on a number of field values using a Last Prediction Strategy
     *
     */
    public HashMap<Object, Object> predict(final JSONObject args,
            Boolean byName, MissingStrategy strategy, Boolean withConfidence)
            throws InputDataParseException {
      return predict(args, byName, null, null, withConfidence, strategy,
              null, null, null, null, null, null).get(0);
    }

    /**
     * Makes a prediction based on a number of field values.
     *
     * By default the input fields must be keyed by field name but you can use
     *  `byName` to input them directly keyed by id.
     *
     * inputData: Input data to be predicted
     * byName: Boolean, True if input_data is keyed by names
     * printPath: Boolean, if True the rules that lead to the prediction
     *              are printed
     * out: output handler
     * withConfidence: Boolean, if True, all the information in the node
     *                  (prediction, confidence, distribution and count)
     *                  is returned in a list format
     * missingStrategy: LAST_PREDICTION|PROPORTIONAL missing strategy for
     *                  missing fields
     * addConfidence: Boolean, if True adds confidence to the dict output
     * addPath: Boolean, if True adds path to the dict output
     * addDistribution: Boolean, if True adds distribution info to the
     *                      dict output
     * addCount: Boolean, if True adds the number of instances in the
     *                      node to the dict output
     * addMedian: Boolean, if True adds the median of the values in
     *                      the distribution
     * multiple: For categorical fields, it will return the categories
     *           in the distribution of the predicted node as a
     *      list of dicts:
     *          [{'prediction': 'Iris-setosa',
     *              'confidence': 0.9154
     *              'probability': 0.97
     *              'count': 97},
     *           {'prediction': 'Iris-virginica',
     *              'confidence': 0.0103
     *              'probability': 0.03,
     *              'count': 3}]
     * The value of this argument can either be an integer
     *  (maximum number of categories to be returned), or the
     *  literal 'all', that will cause the entire distribution
     *  in the node to be returned.
     *
     */
    public List<HashMap<Object, Object>> predict(final JSONObject args,
            Boolean byName, Boolean printPath, OutputStream out,
            Boolean withConfidence, MissingStrategy strategy,
            Boolean addConfidence, Boolean addPath, Boolean addDistribution,
            Boolean addCount, Boolean addMedian, Object multiple)
            throws InputDataParseException {

        List<HashMap<Object, Object>> outputs = new ArrayList<HashMap<Object, Object>>();

        if (byName == null) {
            byName = true;
        }
        if (printPath == null) {
            printPath = false;
        }
        if (out == null) {
            out = System.out;
        }
        if (withConfidence == null) {
            withConfidence = false;
        }

        if (strategy == null) {
            strategy = MissingStrategy.LAST_PREDICTION;
        }

        if (addPath == null) {
            addPath = false;
        }
        if (addConfidence == null) {
            addConfidence = false;
        }
        if (addDistribution == null) {
            addDistribution = false;
        }
        if (addCount == null) {
            addCount = false;
        }
        if (addMedian == null) {
            addMedian = false;
        }

        if (args == null) {
            throw new InputDataParseException("Input data format not valid");
        }
        JSONObject inputData = args;


        // Checks and cleans inputData leaving the fields used in the model
        inputData = filterInputData(inputData, byName);

        // Lets work always using the field names instead of the keys
        if (!byName) {
            inputData = new JSONObject();
            Iterator iter = args.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String fieldName = (String) Utils.getJSONObject(fields, key
                        + ".name");
                inputData.put(fieldName, args.get(key));
            }
        }

        Integer multipleNum = null;
        Boolean multipleAll = null;

        if( multiple != null ) {
            if( multiple instanceof String ) {
                if( "all".equals(multiple)  ) {
                    multipleAll = true;
                } else {
                    throw new IllegalArgumentException("The value of the \"multiple\"" +
                            " argument  can either be an integer" +
                            " (maximum number of categories to be returned), or the" +
                            " literal 'all', that will cause the entire distribution" +
                            " in the node to be returned.");
                }
            } else if( multiple instanceof Number ) {
                multipleNum = ((Number) multiple).intValue();
            }
        }

        // Strips affixes for numeric values and casts to the final field type
        Utils.cast(inputData, fields);

        HashMap<Object, Object> predictionInfo = tree.predict(inputData, null, strategy);

        Object prediction = predictionInfo.get("prediction");
        List<String> path = (List<String>) predictionInfo.get("path");
        Double confidence = (Double) predictionInfo.get("confidence");
        List<JSONArray>  distribution = (List<JSONArray>) predictionInfo.get("distribution");
        Long instances = (Long) predictionInfo.get("instances");
        Double median = (Double) predictionInfo.get("median");
        Object distribution_unit = predictionInfo.get("distribution_unit");

        if( printPath ) {
            try {
                out.write((Utils.join(path, " AND ") + " => " + prediction + "\n").getBytes());
                out.flush();
            } catch (IOException e) {
                logger.error("Unable to print the path into the outputstream.", e);
            }
        }

        HashMap<Object, Object> output = new HashMap<Object, Object>();
        output.put("prediction", prediction);

        if( withConfidence ) {
            output.put("confidence", confidence);
            output.put("distribution", distribution);
            output.put("instances", instances);
        }

        if( multiple != null && !tree.isRegression() ) {
            Long totalInstances = instances;

            for( int iDistIndex = 0; iDistIndex < distribution.size(); iDistIndex++ ) {
                JSONArray distElement = distribution.get(iDistIndex);
                if( multipleAll || iDistIndex < multipleNum ) {
                    predictionInfo = new HashMap<Object, Object>();
                    // Category
                    Object category = distElement.get(0);
                    predictionInfo.put("prediction", category);
                    predictionInfo.put("confidence",
                            Tree.wsConfidence(category, distribution));
                    predictionInfo.put("probability",
                            ((Integer) distElement.get(1)) / totalInstances);
                    predictionInfo.put("count", distElement.get(1));

                    outputs.add(predictionInfo);
                }
            }

            return outputs;
        } else {
            if( addConfidence ) {
                output.put("confidence", confidence);
            }
            if( addPath ) {
                output.put("path", new ArrayList<String>(path));
            }
            if( addDistribution ) {
                output.put("distribution", distribution);
                output.put("distribution_unit", distribution_unit);
            }
            if( addCount ) {
                output.put("count", instances);
            }
            if( tree.isRegression() && addMedian ) {
                output.put("median", median);
            }

            outputs.add(output);

            return outputs;
        }
    }

    /**
     * Convenience version of predict that take as inputs a map from field ids
     * or names to their values as Java objects. See also predict(String,
     * Boolean, Integer, Boolean).
     */
    public HashMap<Object, Object> predictWithMap(
            final Map<String, Object> inputs, Boolean byName, Boolean withConfidence)
            throws InputDataParseException {

        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, byName, null, null, withConfidence, MissingStrategy.LAST_PREDICTION,
                null, null, null, null, null, null).get(0);
    }

    public HashMap<Object, Object> predictWithMap(
            final Map<String, Object> inputs, Boolean byName, Boolean withConfidence, MissingStrategy missingStrategy)
            throws InputDataParseException {

        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, byName, null, null, withConfidence, missingStrategy,
                null, null, null, null, null, null).get(0);
    }

    public HashMap<Object, Object> predictWithMap(
            final Map<String, Object> inputs, Boolean byName)
            throws InputDataParseException {
        return predictWithMap(inputs, byName, null, MissingStrategy.LAST_PREDICTION);
    }

    public HashMap<Object, Object> predictWithMap(
            final Map<String, Object> inputs) throws InputDataParseException {
        return predictWithMap(inputs, null, null, MissingStrategy.LAST_PREDICTION);
    }

    /**
     * Returns a IF-THEN rule set that implements the model.
     * 
     * @param depth
     *            controls the size of indentation
     */
    public String rules(final int depth) {
        return tree.rules(depth);
    }


    /**
     * Given a prediction string, returns its value in the required type
     *
     * @param valueAsString the prediction value as string
     * @return
     */
    public Object toPrediction(String valueAsString, Locale locale) {
        locale = (locale != null ? locale : BigMLClient.DEFAUL_LOCALE);

        String objectiveFieldName = tree.getObjectiveField();
        if( "numeric".equals(Utils.getJSONObject(fields, objectiveFieldName + ".optype")) ) {
            String dataTypeStr = (String) Utils.getJSONObject(fields, objectiveFieldName + ".'datatype'");
            DataTypeEnum dataType = DataTypeEnum.valueOf(dataTypeStr.toUpperCase().replace("-",""));
            switch (dataType) {
                case DOUBLE:
                case FLOAT:
                    try {
                        NumberFormat formatter = DecimalFormat.getInstance(locale);
                        return formatter.parse(valueAsString).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return valueAsString;
                    }

                case INTEGER:
                case INT8:
                case INT16:
                case INT32:
                case INT64:
                case DAY:
                case MONTH:
                case YEAR:
                case HOUR:
                case MINUTE:
                case SECOND:
                case MILLISECOND:
                case DAYOFMONTH:
                case DAYOFWEEK:
                    try {
                        NumberFormat formatter = NumberFormat.getInstance(locale);
                        return formatter.parse(valueAsString).longValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return valueAsString;
                    }
            }
        }

        return valueAsString;
    }

    /**
     * Writes a java method that implements the model.
     * 
     */
    /*
     * public String java() { Iterator iter = fields.keySet().iterator(); if
     * (!iter.hasNext()) { return null; }
     * 
     * String methodReturn = "Object"; String methodName = ""; String
     * objectiveFieldName = (String) Utils.getJSONObject(fields,
     * tree.getObjectiveField()+".name");
     * 
     * String methodParams = ""; while (iter.hasNext()) { String key = (String)
     * iter.next();
     * 
     * String dataType = (String) Utils.getJSONObject(fields, key+".datatype");
     * String opType = (String) Utils.getJSONObject(fields, key+".optype");
     * String name = (String) Utils.getJSONObject(fields, key+".name");
     * 
     * if (objectiveFieldName.equals(name)) { methodName = objectiveFieldName;
     * methodReturn = JAVA_TYPES.get(opType+"-"+dataType); } else { methodParams
     * += "final " + JAVA_TYPES.get(opType+"-"+dataType) + " " +
     * Utils.slugify(name) + ", "; }
     * 
     * } methodParams = methodParams.substring(0, methodParams.length()-2);
     * 
     * return MessageFormat.format(
     * "public {0} predict_{1}({2}) '{'\n{3}\n    return null;\n'}'\n",
     * methodReturn, Utils.slugify(methodName), methodParams, tree.javaBody(1,
     * methodReturn)); }
     */

    /*
     * Tests if the fields names are unique. If they aren't, a transformation is
     * applied to ensure unicity.
     */
    private void uniquifyVarnames() {
        HashSet<String> uniqueNames = new HashSet<String>();
        Iterator iter = this.fields.keySet().iterator();
        while (iter.hasNext()) {
            uniqueNames.add((String) iter.next());
        }
        if (uniqueNames.size() < this.fields.size()) {
            transformRepeatedNames();
        }
        return;
    }

    /*
     * If a field name is repeated, it will be transformed adding its column
     * number. If that combination is also a field name, the field id will be
     * added.
     */
    private void transformRepeatedNames() {
        // The objective field treated first to avoid changing it.
        String objectiveFieldId = (String) Utils.getJSONObject(this.fields,
                objectiveField + ".name");
        HashSet<String> uniqueNames = new HashSet<String>();
        uniqueNames.add(objectiveFieldId);

        Iterator iter = this.fields.keySet().iterator();
        ArrayList<String> fieldIds = new ArrayList<String>();
        while (iter.hasNext()) {
            String fieldId = (String) iter.next();
            if (!fieldId.equals(objectiveFieldId)) {
                fieldIds.add(fieldId);
            }
        }

        for (String fieldId : fieldIds) {
            JSONObject fieldJson = (JSONObject) Utils.getJSONObject(
                    this.fields, fieldId);
            String newName = (String) Utils.getJSONObject(fieldJson, "name");
            if (uniqueNames.contains(newName)) {
                newName = MessageFormat.format("{0}{1}", fieldJson.get("name"),
                        fieldJson.get("column_number"));
                if (uniqueNames.contains(newName)) {
                    newName = MessageFormat.format("{0}_{1}", newName, fieldId);
                }
                fieldJson.put("name", newName);
            }
            uniqueNames.add(newName);
        }
    }

    /**
     * Filters the keys given in inputData checking against model fields
     *
     * @param inputData
     * @param byName
     * @return
     */
    public JSONObject filterInputData(JSONObject inputData, boolean byName) {

        List<String> emptyFields = new ArrayList<String>();
        for (Object key : inputData.keySet()) {
            if( inputData.get(key) == null ) {
                emptyFields.add(key.toString());
            }
        }

        for (String emptyField : emptyFields) {
            inputData.remove(emptyField);
        }

        JSONObject newInputData = new JSONObject();

        if( byName ) {
            JSONObject invertedFields = Utils.invertDictionary(fields);
            for (Object inputDataKey : inputData.keySet()) {
                if( invertedFields.containsKey(inputDataKey) ) {
                    newInputData.put(inputDataKey, inputData.get(inputDataKey));
                }
            }
        } else {
            for (Object inputDataKey : inputData.keySet()) {
                if( fields.containsKey(inputDataKey) ) {
                    newInputData.put(inputDataKey, inputData.get(inputDataKey));
                }
            }
        }

        return newInputData;
    }


    private enum DataTypeEnum {
        DOUBLE, FLOAT, INTEGER,
        INT8, INT16, INT32, INT64,
        DAY, MONTH, YEAR,
        HOUR, MINUTE, SECOND, MILLISECOND,
        DAYOFWEEK, DAYOFMONTH
    }
}
