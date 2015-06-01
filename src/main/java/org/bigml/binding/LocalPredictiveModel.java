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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bigml.binding.localmodel.Predicate;
import org.bigml.binding.localmodel.Prediction;
import org.bigml.binding.localmodel.Tree;
import org.bigml.binding.localmodel.TreeNodeFilter;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * A lightweight wrapper around a Tree model.
 * 
 * Uses a BigML remote model to build a local version that can be used to
 * generate prediction locally.
 * 
 */
public class LocalPredictiveModel extends BaseModel implements PredictionConverter {

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

    public static Double DEFAULT_IMPURITY = 0.2;

    private JSONObject root;
    private Tree tree;
    private Map<String, Tree> idsMap;
    private Map<String, List<String>> terms = new HashMap<String, List<String>>();
    private int maxBins = 0;


    /**
     * Constructor
     * 
     * @param model
     *            the json representation for the remote model
     */
    public LocalPredictiveModel(JSONObject model) throws Exception {
        super(model);

        try {
            String prefix = Utils.getJSONObject(model, "object") != null ? "object."
                    : "";

            this.root = (JSONObject) Utils.getJSONObject(model, prefix
                    + "model.root");

            this.idsMap = new HashMap<String, Tree>();

            this.tree = new Tree(root, this.fields, objectiveField,
                    (JSONObject) Utils.getJSONObject(model, prefix + "model.distribution.training"), null, idsMap, true, 0);

            if( this.tree.isRegression() )
                this.maxBins = this.tree.getMaxBins();

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
     * Checks if the tree is a regression problem
     */
    public boolean isRegression() {
        return tree.isRegression();
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
    public List<Tree> getImpureLeaves(Double impurityThreshold) {
        final Double impurityThresholdToUse = (impurityThreshold == null ?
                DEFAULT_IMPURITY : impurityThreshold);

        return this.tree.getLeaves(new TreeNodeFilter() {
            @Override
            public boolean filter(Tree node) {
                Double nodeImpurity = node.getImpurity();
                return (nodeImpurity != null && nodeImpurity > impurityThresholdToUse);
            }
        });
    }

    /**
     * Makes a prediction based on a number of field values.
     * 
     * The input fields must be keyed by field name.
     * 
     */
    public Prediction predict(final String args)
            throws InputDataParseException {
        return predict(args, false);
    }

    /**
     * Makes a prediction based on a number of field values using a Last Prediction Strategy
     *
     * By default the input fields must be keyed by field name but you can use `by_name`
     *  to input them directly keyed by id.
     *
     */
    public Prediction predict(final String args, Boolean byName) throws InputDataParseException {
        if (byName == null) {
            byName = true;
        }

        JSONObject argsData = (JSONObject) JSONValue.parse(args);
        if (!args.equals("") && !args.equals("") && argsData == null) {
            throw new InputDataParseException("Input data format not valid");
        }
        JSONObject inputData = argsData;


        return predict(inputData, byName);
    }

    /**
     * Makes a prediction based on a number of field values.
     * 
     * The input fields must be keyed by field name.
     */
    public Prediction predict(final JSONObject args)
            throws InputDataParseException {
        return predict(args, false, MissingStrategy.LAST_PREDICTION, null).get(0);
    }

    /**
     * Makes a prediction based on a number of field values using a Last Prediction Strategy
     * 
     * The input fields must be keyed by field name.
     */
    public Prediction predict(final JSONObject args, Boolean byName)
            throws InputDataParseException {
        return predict(args, byName, MissingStrategy.LAST_PREDICTION, null).get(0);
    }

    /**
     * Makes a prediction based on a number of field values using the specified Missing Strategy
     *
     * The input fields must be keyed by field name.
     */
    public Prediction predict(final JSONObject args, Boolean byName, MissingStrategy strategy)
            throws InputDataParseException {
        return predict(args, byName, strategy, null).get(0);
    }

    /**
     * Makes a multiple predictions based on a number of field values using the Last Prediction strategy
     *
     * The input fields must be keyed by field name.
     */
    public List<Prediction> predict(final JSONObject args, Boolean byName, Object multiple)
            throws InputDataParseException {
        return predict(args, byName, MissingStrategy.LAST_PREDICTION, multiple);
    }

    /**
     * Makes a prediction based on a number of field values.
     *
     * By default the input fields must be keyed by field name but you can use
     *  `byName` to input them directly keyed by id.
     *
     * inputData: Input data to be predicted
     *
     * byName: Boolean, True if input_data is keyed by names
     *
     * missingStrategy: LAST_PREDICTION|PROPORTIONAL missing strategy for
     *                  missing fields
     *
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
     *
     * The value of this argument can either be an integer
     *  (maximum number of categories to be returned), or the
     *  literal 'all', that will cause the entire distribution
     *  in the node to be returned.
     *
     */
    public List<Prediction> predict(final JSONObject args, Boolean byName, MissingStrategy strategy, Object multiple)
            throws InputDataParseException {

        List<Prediction> outputs = new ArrayList<Prediction>();

        if (byName == null) {
            byName = true;
        }

        if (strategy == null) {
            strategy = MissingStrategy.LAST_PREDICTION;
        }

        if (args == null) {
            throw new InputDataParseException("Input data format not valid");
        }
        JSONObject inputData = args;


        // Checks and cleans inputData leaving the fields used in the model
        inputData = filterInputData(inputData, byName);

        Integer multipleNum = Integer.MAX_VALUE;
        Boolean multipleAll = false;

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
            } else {
                throw new IllegalArgumentException("The value of the \"multiple\"" +
                        " argument  can either be an integer" +
                        " (maximum number of categories to be returned), or the" +
                        " literal 'all', that will cause the entire distribution" +
                        " in the node to be returned.");
            }
        }

        // Strips affixes for numeric values and casts to the final field type
        Utils.cast(inputData, fields);

        Prediction predictionInfo = tree.predict(inputData, null, strategy);

        JSONArray  distribution = predictionInfo.getDistribution();
        Long instances = predictionInfo.getCount();

        if( multiple != null && !tree.isRegression() ) {
            for( int iDistIndex = 0; iDistIndex < distribution.size(); iDistIndex++ ) {
                JSONArray distElement = (JSONArray) distribution.get(iDistIndex);
                if( multipleAll || iDistIndex < multipleNum ) {
                    predictionInfo = new Prediction();
                    // Category
                    Object category = distElement.get(0);
                    predictionInfo.setPrediction(category);
                    predictionInfo.setConfidence(Tree.wsConfidence(category, distribution));
                    predictionInfo.setProbability(((Number) distElement.get(1)).doubleValue() / instances);
                    predictionInfo.setCount(((Number) distElement.get(1)).longValue());

                    outputs.add(predictionInfo);
                }
            }

            return outputs;
        } else {
            List<Tree> children = predictionInfo.getChildren();
            String field = (children == null || children.size() == 0 ? null : children.get(0).getPredicate().getField());
            if( field != null && fields.containsKey(field) ) {
                field = fieldsNameById.get(field);
            }
            predictionInfo.setNext(field);

            outputs.add(predictionInfo);

            return outputs;
        }
    }

    /**
     * Convenience version of predict that take as inputs a map from field ids
     * or names to their values as Java objects. See also predict(String,
     * Boolean, Integer, Boolean).
     */
    public Prediction predictWithMap(
            final Map<String, Object> inputs, Boolean byName, Boolean withConfidence)
            throws InputDataParseException {

        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, byName, MissingStrategy.LAST_PREDICTION, null).get(0);
    }

    public Prediction predictWithMap(
            final Map<String, Object> inputs, Boolean byName, MissingStrategy missingStrategy)
            throws InputDataParseException {

        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, byName, missingStrategy, null).get(0);
    }

    public Prediction predictWithMap(
            final Map<String, Object> inputs, Boolean byName)
            throws InputDataParseException {
        return predictWithMap(inputs, byName, MissingStrategy.LAST_PREDICTION);
    }

    public Prediction predictWithMap(
            final Map<String, Object> inputs) throws InputDataParseException {
        return predictWithMap(inputs, false, MissingStrategy.LAST_PREDICTION);
    }

    /**
     * Builds the list of ids that go from a given id to the tree root
     */
    public List<String> getIdsPath(String filterId) {
        List<String> idsPath = null;

        if( (filterId != null && filterId.length() > 0 ) &&
                (tree.getId() != null && tree.getId().length() > 0) ) {
            if( !idsMap.containsKey(filterId) ) {
                throw new IllegalArgumentException(
                        String.format("The given id for the filter does " +
                                "not exist. Filter Id: %s", filterId));
            } else {
                idsPath = new ArrayList<String>();
                idsPath.add(filterId);
                String lastId = filterId;
                while(idsMap.containsKey(lastId) && idsMap.get(lastId).getParentId() != null) {
                    idsPath.add(idsMap.get(lastId).getParentId());
                    lastId = idsMap.get(lastId).getParentId();
                }
            }
        }

        return idsPath;
    }


    /**
     * Returns a IF-THEN rule set that implements the model.
     */
    public String rules() {
        return tree.rules(Predicate.RuleLanguage.PSEUDOCODE);
    }

    /**
     * Returns a IF-THEN rule set that implements the model.
     */
    public String rules(Predicate.RuleLanguage language) {
        return tree.rules(language);
    }

    /**
     * Returns a IF-THEN rule set that implements the model.
     */
    public String rules(Predicate.RuleLanguage language, final String filterId, boolean subtree) {
        List<String> idsPath = getIdsPath(filterId);
        return tree.rules(language, idsPath, subtree);
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
     * Average for the confidence of the predictions resulting from
     * running the training data through the model
     */
    public double getAverageConfidence() {
        double total = 0.0, cumulativeConfidence = 0.0;
        Map<Object, GroupPrediction> groups = getGroupPrediction();
        for (GroupPrediction groupPrediction : groups.values()) {
            for (PredictionDetails predictionDetails : groupPrediction.getDetails()) {
                cumulativeConfidence +=  predictionDetails.getLeafPredictionsCount() *
                        predictionDetails.getConfidence();
                total += predictionDetails.getLeafPredictionsCount();
            }
        }

        return (total == 0.0 ? Double.NaN : cumulativeConfidence);
    }

    /**
     * The tree nodes information in a row format
     */
    public List getNodesInfo(List<String> headers, boolean leavesOnly) {
        return tree.getNodesInfo(headers, leavesOnly);
    }

    /**
     * Outputs the node structure to in array format, including the
     * header names in the first row
     */
    public List<List> getTreeArray(boolean leavesOnly) {
        List<String> headerNames = new ArrayList<String>();

        // Adding the objective field name
        headerNames.add(Utils.getJSONObject(fields, objectiveField + ".name").toString());
        if( isRegression() ) {
            headerNames.add("error");
            for(int index = 0; index < maxBins; index++) {
                headerNames.add(String.format("bin%s_value", index));
                headerNames.add(String.format("bin%s_instances", index));
            }
        } else {
            headerNames.add("confidence");
            headerNames.add("impurity");

            // Adding the category of the bins
            for (Object bin : tree.getDistribution()) {
                JSONArray binObject = (JSONArray) bin;
                headerNames.add(binObject.get(0).toString());
            }
        }

        List<List> nodes = getNodesInfo(headerNames, leavesOnly);
        nodes.add(0, headerNames);

        return nodes;
    }

    /**
     * Outputs the node structure to in CSV file, including the
     */
    public void exportTreeCSV(String outputFilePath, boolean leavesOnly) throws IOException {
        List<List> rows = getTreeArray(leavesOnly);

        Writer treeFile = null;
        try {
            treeFile = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFilePath), "UTF-8"));
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Cannot find %s directory.", outputFilePath));
        }

        List headers = rows.remove(0);
        final CSVPrinter printer = CSVFormat.DEFAULT.withHeader((String[])
                headers.toArray(new String[headers.size()])).print(treeFile);

        try {
            printer.printRecords(rows);
        } catch (Exception e) {
            throw new IOException("Error generating the CSV !!!");
        }

        try {
            treeFile.flush();
            treeFile.close();
        } catch (IOException e) {
            throw new IOException("Error while flushing/closing fileWriter !!!");
        }

    }

    /**
     * Groups in categories or bins the predicted data
     *
     * @return Map - contains a map grouping counts in 'total' and 'details' lists.
     *
     *      'total' key contains a 3-element list.
     *              - common segment of the tree for all instances
     *              - data count
     *              - predictions count
     *      'details' key contains a list of elements. Each element is a 3-element list:
     *              - complete path of the tree from the root to the leaf
     *              - leaf predictions count
     *              - confidence
     *              - impurity
     */
    public Map<Object, GroupPrediction> getGroupPrediction() {

        Map<Object, GroupPrediction> groups = new HashMap<Object, GroupPrediction>();

        JSONArray distribution = tree.getDistribution();
        for (Object item : distribution) {
            JSONArray distItem = (JSONArray) item;

            GroupPrediction newGroupPrediction = new GroupPrediction();
            newGroupPrediction.setTotalData(((Number) distItem.get(1)).intValue());

            groups.put(distItem.get(0),newGroupPrediction);
        }

        List<Predicate> path = new ArrayList<Predicate>();
        getDepthFirstSearch(groups, tree, path);

        return groups;
    }


    /**
     * Adds instances to groups array
     *
     * Used by getGroupPrediction()
     */
    protected void addToGroups(Map<Object, GroupPrediction> groups, List<Predicate> path, Object output,
                             long count, double confidence, double impurity) {
        GroupPrediction groupPrediction = groups.get(output);
        if( groupPrediction == null ) {
            groupPrediction = new GroupPrediction();
            groups.put(output, groupPrediction);
        }

        PredictionDetails newPrediction = new PredictionDetails();
        newPrediction.setPath(path);
        newPrediction.setLeafPredictionsCount(count);
        newPrediction.setConfidence(confidence);
        newPrediction.setImpurity(impurity);

        groupPrediction.addPrediction(newPrediction);
    }

    /**
     * Search for leafs' values and instances
     *
     * Used by getGroupPrediction()
     */
    protected long getDepthFirstSearch(Map<Object, GroupPrediction> groups, Tree tree, List<Predicate> path) {
        if( path == null ) {
            path= new ArrayList<Predicate>();
        }

        if( !tree.isPredicate() ) {
            path.add(tree.getPredicate());
            if( tree.getPredicate().getTerm() != null ) {
                String field = tree.getPredicate().getField();
                String term = tree.getPredicate().getTerm();
                if( !terms.containsKey(field) ) {
                    terms.put(field, new ArrayList<String>());
                }

                if( !terms.get(field).contains(term) ) {
                    terms.get(field).add(term);
                }
            }
        }

        if( tree.getChildren().size() == 0 ) {
            addToGroups(groups, path, tree.getOutput(), tree.getCount(), tree.getConfidence(), tree.getImpurity());
            return tree.getCount();
        } else {
            List<Tree> children = new ArrayList<Tree>(tree.getChildren());
            Collections.reverse(children);

            int childrenSum = 0;
            for (Tree child : children) {
                childrenSum += getDepthFirstSearch(groups, child, new ArrayList<Predicate>(path));
            }

            if( childrenSum < tree.getCount() ) {
                addToGroups(groups, path, tree.getOutput(), tree.getCount() - childrenSum,
                        tree.getConfidence(), tree.getImpurity());
            }

            return tree.getCount();
        }
    }

    /**
     * Returns training data distribution
     */
    public JSONArray getDataDistribution() {

        JSONArray distribution = new JSONArray();
        distribution.addAll(tree.getDistribution());

        Collections.sort(distribution, new Comparator<JSONArray>() {
            public int compare(JSONArray o1, JSONArray o2) {
                Object o1Val = o1.get(0);
                Object o2Val = o2.get(0);

                if (o1Val instanceof Number) {
                    o1Val = ((Number) o1Val).doubleValue();
                    o2Val = ((Number) o2Val).doubleValue();
                }

//                if(o2Val.getClass().equals(o1Val.getClass()) )  {
//                    // both numbers are instances of the same type!
//                    if (o1Val instanceof Comparable) {
//                        // and they implement the Comparable interface
                return ((Comparable) o1Val).compareTo(o2Val);
//                    }
//                }
                // for all different Number types, let's check there double values
//                if (o1Num.doubleValue() < o2Num.doubleValue())
//                    return -1;
//                if (o1Num.doubleValue() > o2Num.doubleValue())
//                    return 1;
//                return 0;
            }
        });

        return distribution;
    }

    /**
     * Returns model predicted distribution
     */
    public JSONArray getPredictionDistribution() {
        return getPredictionDistribution(null);
    }

    /**
     * Returns model predicted distribution
     */
    public JSONArray getPredictionDistribution(Map<Object, GroupPrediction> groups) {

        if( groups == null ) {
            groups = getGroupPrediction();
        }

        JSONArray predictions = new JSONArray();

        for (Object group : groups.keySet()) {
            long totalPredictions = groups.get(group).getTotalPredictions();

            // remove groups that are not predicted
            if( totalPredictions > 0 ) {
                JSONArray prediction = new JSONArray();
                prediction.add(group);
                prediction.add(totalPredictions);
                predictions.add(prediction);
            }
        }

        Collections.sort(predictions, new Comparator<JSONArray>() {
            public int compare(JSONArray o1, JSONArray o2) {
                Object o1Val = o1.get(0);
                Object o2Val = o2.get(0);

                if (o1Val instanceof Number) {
                    o1Val = ((Number) o1Val).doubleValue();
                    o2Val = ((Number) o2Val).doubleValue();
                }

//                if (((Object) o2Num).getClass().equals(((Object) o1Num).getClass())) {
//                    // both numbers are instances of the same type!
//                    if (o1Num instanceof Comparable) {
//                        // and they implement the Comparable interface
                return ((Comparable) o1Val).compareTo(o2Val);
//                    }
//                }
                // for all different Number types, let's check there double values
//                if (o1Num.doubleValue() < o2Num.doubleValue())
//                    return -1;
//                if (o1Num.doubleValue() > o2Num.doubleValue())
//                    return 1;
//                return 0;
            }
        });

        return predictions;
    }

    /**
     * Prints summary grouping distribution as class header and details
     *
     * @param out
     */
    public String   summarize(Boolean addFieldImportance) throws IOException {

        StringBuilder summarize = new StringBuilder();

        if( addFieldImportance == null ) {
            addFieldImportance = false;
        }

        JSONArray distribution = getDataDistribution();
        summarize.append("Data distribution:\n");
        summarize.append(Utils.printDistribution(distribution).toString());
        summarize.append("\n\n");

        Map<Object, GroupPrediction> groups = getGroupPrediction();
        JSONArray predictions = getPredictionDistribution(groups);
        summarize.append("Predicted distribution:\n");
        summarize.append(Utils.printDistribution(predictions).toString());
        summarize.append("\n\n");

        if( addFieldImportance ) {
            summarize.append("Field importance:\n");
            int count = 1;
            for (Object fieldItem : fieldImportance) {
                String fieldId = ((JSONArray) fieldItem).get(0).toString();
                double importance = ((Number) ((JSONArray) fieldItem).get(1)).doubleValue();
                summarize.append(String.format("    %s. %s: %.2f%%\n", count++,
                        Utils.getJSONObject(fields, fieldId + ".name"), (Utils.roundOff(importance, 4) * 100)));
            }
        }

        extractCommonPath(groups);

        for (Object groupInstances : predictions) {
            Object group = ((JSONArray) groupInstances).get(0);
            GroupPrediction groupPrediction = groups.get(group);

            List<PredictionDetails> details = groupPrediction.getDetails();
            List<Predicate> predicates = groupPrediction.getTotalCommonSegments();

            List<String> path = new ArrayList<String>(predicates.size());
            for (Predicate predicate : predicates) {
                path.add(predicate.toRule(fields));
            }

            double dataPerGroup = (groupPrediction.getTotalData() * 1.0) / tree.getCount();
            double predPerGroup = (groupPrediction.getTotalPredictions() * 1.0) / tree.getCount();

            summarize.append(String.format("\n\n%s : (data %.2f%% / prediction %.2f%%) %s\n",
                    group, Utils.roundOff(dataPerGroup, 4) * 100, Utils.roundOff(predPerGroup, 4) * 100, Utils.join(path, " and ")));

            if( details.size() == 0 ) {
                summarize.append("    The model will never predict this class\n");
            } else {
                for(int index = 0; index < details.size(); index++) {
                    PredictionDetails predictionDetails = details.get(index);

                    double predPerSubgroup = (predictionDetails.getLeafPredictionsCount() * 1.0) /
                            groupPrediction.getTotalPredictions();

                    List<Predicate> gPredicates = predictionDetails.getPath();
                    List<String> sPath = new ArrayList<String>(gPredicates.size());
                    for (Predicate predicate : gPredicates) {
                        sPath.add(predicate.toRule(fields));
                    }

                    String pathChain = (sPath.size() == 0 ? "(root node)" :
                            Utils.join(sPath, " and "));

                    summarize.append(String.format("    · %.2f%%: %s%s\n",
                            Utils.roundOff(predPerSubgroup, 4) * 100, pathChain,
                                    confidenceError(predictionDetails.getConfidence(),
                                            predictionDetails.getImpurity())));
                }
            }
        }

        return summarize.toString();
    }

    /**
     * Extracts the common segment of the prediction path for a group
     *
     * Used by summarize()
     *
     * @param groups
     */
    private void extractCommonPath(Map<Object, GroupPrediction> groups) {

        for (Object group : groups.keySet()) {
            List<PredictionDetails> details = groups.get(group).getDetails();

            List<Predicate> commonPath = new ArrayList<Predicate>();

            if( details.size() > 0 ) {
                PredictionDetails minPathLength = Collections.min(details, new Comparator<PredictionDetails>() {
                    @Override
                    public int compare(PredictionDetails o1, PredictionDetails o2) {
                        int o1Length = o1.getPath().size();
                        int o2Length = o2.getPath().size();

                        // We use this approach when comparing to maintain the same
                        //  order as the python counterpart
                        if (o1Length <= o2Length)
                            return -1;
//                        if (o1Length > o2Length )

                        return 1;
//                        return 0;
                    }
                });

                int mdcLength = minPathLength.getPath().size();
                for(int i = 0; i < mdcLength; i++ ) {
                    Predicate testCommonPath = details.get(0).getPath().get(i);
                    String testCommonPathRule = testCommonPath.toRule(fields);
                    for (PredictionDetails subgroup : details) {
                        String rule = subgroup.getPath().get(i).toRule(fields);

                        if( !testCommonPathRule.equals(rule) ) {
                            i = mdcLength;
                            break;
                        }
                    }

                    if( i < mdcLength ) {
                        commonPath.add(testCommonPath);
                    }
                }
            }

            groups.get(group).setTotalCommonSegments(commonPath);
            if( details.size() > 0 ) {
                Collections.sort(details, new Comparator<PredictionDetails>() {
                    @Override
                    public int compare(PredictionDetails o1, PredictionDetails o2) {
                        long o1Count = o1.getLeafPredictionsCount();
                        long o2Count = o2.getLeafPredictionsCount();

                        // We use this approach when comparing to maintain the same
                        //  order as the python counterpart
                        if ( o1Count <= o2Count )
                            return -1;
//                        if ( o1Count > o2Count )
                        return 1;
//                        return 0;
                    }
                });

                Collections.reverse(details);
            }
        }
    }

    /**
     * Returns confidence for categorical objective fields and error for numeric objective fields
     */
    private String confidenceError(Object value, Double impurity) {
        if( value == null ) {
           return "";
        }

        String impurityLiteral = "";
        if( impurity != null && impurity > 0.0 ) {
            impurityLiteral = String.format("; impurity: %.2f%%", Utils.roundOff(impurity, 4));
        }

        String objectiveType = (String) ((JSONObject) fields.get(tree.getObjectiveField())).get("optype");
        if( "numeric".equals(objectiveType) ) {
            DecimalFormat df = new DecimalFormat("0");
            df.setMaximumFractionDigits(5);
            return String.format(" [Error: %s]", df.format(value));
        } else {
            return String.format(" [Confidence: %.2f%%%s]",
                    ( Utils.roundOff(((Number) value).doubleValue(), 4) * 100), impurityLiteral);
        }
    }

    // TODO: hadoop_python_mapper



//    /**
//     * Prints distribution data
//     */
//    private void printDistribution(JSONArray distribution, OutputStream out) throws IOException {
//
//        // Reduce distribution
//        long total = 0;
//        for (Object group : distribution) {
//            JSONArray groupItem = (JSONArray) group;
//            total += ((Number) groupItem.get(1)).longValue();
//        }
//
//        for (Object group : distribution) {
//            JSONArray groupItem = (JSONArray) group;
//            long numOfInstances = ((Number) groupItem.get(1)).longValue();
//            out.write(String.format("    %s: %4.2f%% (%d instance%s)%n",
//                    groupItem.get(0),
//                    (((numOfInstances * 1.0) / total) * 100),
//                    groupItem.get(1),
//                    numOfInstances == 1 ? "" : "s").getBytes());
//        }
//    }

//    private void printImportance(OutputStream out) throws IOException {
//
//    }



    private enum DataTypeEnum {
        DOUBLE, FLOAT, INTEGER,
        INT8, INT16, INT32, INT64,
        DAY, MONTH, YEAR,
        HOUR, MINUTE, SECOND, MILLISECOND,
        DAYOFWEEK, DAYOFMONTH
    }

    public static class GroupPrediction implements Serializable {

        private List<Predicate> totalCommonSegments = new ArrayList<Predicate>();
        private long totalData = 0;
        private long totalPredictions = 0;

        private List<PredictionDetails> details = new ArrayList<PredictionDetails>();

        public List<Predicate> getTotalCommonSegments() {
            return totalCommonSegments;
        }

        public void setTotalCommonSegments(List<Predicate> totalCommonSegments) {
            this.totalCommonSegments = totalCommonSegments;
        }

        public long getTotalData() {
            return totalData;
        }

        public void setTotalData(long totalData) {
            this.totalData = totalData;
        }

        public long getTotalPredictions() {
            return totalPredictions;
        }

        public void setTotalPredictions(long totalPredictions) {
            this.totalPredictions = totalPredictions;
        }

        public List<PredictionDetails> getDetails() {
            return details;
        }

        public void setDetails(List<PredictionDetails> details) {
            this.details = details;
        }

        public void addPrediction(PredictionDetails newPrediction) {
            details.add(newPrediction);
            totalPredictions += newPrediction.getLeafPredictionsCount();
        }
    }

    public static class PredictionDetails implements Serializable {

        private List<Predicate> path = new ArrayList<Predicate>();
        private long leafPredictionsCount = 0;
        private double confidence = 0.0;
        private double impurity = 0.0;

        public List<Predicate> getPath() {
            return path;
        }

        public void setPath(List<Predicate> path) {
            this.path = path;
        }

        public long getLeafPredictionsCount() {
            return leafPredictionsCount;
        }

        public void setLeafPredictionsCount(long leafPredictionsCount) {
            this.leafPredictionsCount = leafPredictionsCount;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public double getImpurity() {
            return impurity;
        }

        public void setImpurity(double impurity) {
            this.impurity = impurity;
        }
    }
}
