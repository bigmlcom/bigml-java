package org.bigml.binding.localmodel;

import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.special.Erf;
import org.bigml.binding.Constants;
import org.bigml.binding.MissingStrategy;
import org.bigml.binding.MultiVote;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tree-like predictive model.
 * 
 */
public class Tree {

    /**
     * Logging
     */
    static Logger LOGGER = LoggerFactory.getLogger(Tree.class.getName());

    final static String INDENT = "    ";

    final static int BINS_LIMIT = 32;
    final static double DEFAULT_RZ = 1.96;

    // Map operator str to its corresponding java operator
    static HashMap<String, String> JAVA_OPERATOR = new HashMap<String, String>();
    static {
        JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC + "-"
                + Constants.OPERATOR_LT, "{2} < {3}");
        JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC + "-"
                + Constants.OPERATOR_LE, "{2} <= {3}");
        JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC + "-"
                + Constants.OPERATOR_EQ, "{2} = {3}");
        JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC + "-"
                + Constants.OPERATOR_NE, "{2} != {3}");
        JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC + "-"
                + Constants.OPERATOR_NE2, "{2} != {3}");
        JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC + "-"
                + Constants.OPERATOR_GE, "{2} >= {3}");
        JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC + "-"
                + Constants.OPERATOR_GT, "{2} > {3}");
        JAVA_OPERATOR.put(Constants.OPTYPE_CATEGORICAL + "-"
                + Constants.OPERATOR_EQ, "\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_CATEGORICAL + "-"
                + Constants.OPERATOR_NE, "!\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_CATEGORICAL + "-"
                + Constants.OPERATOR_NE2, "!\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_TEXT + "-" + Constants.OPERATOR_EQ,
                "\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_TEXT + "-" + Constants.OPERATOR_NE,
                "!\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_TEXT + "-" + Constants.OPERATOR_NE2,
                "!\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME + "-"
                + Constants.OPERATOR_EQ, "\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME + "-"
                + Constants.OPERATOR_NE, "!\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME + "-"
                + Constants.OPERATOR_NE2, "!\"{2}\".equals({3})");
        JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME + "-"
                + Constants.OPERATOR_LT, "\"{2}\".compareTo({3})<0");
        JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME + "-"
                + Constants.OPERATOR_LE, "\"{2}\".compareTo({3})<=0");
        JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME + "-"
                + Constants.OPERATOR_GE, "\"{2}\".compareTo({3})>=0");
        JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME + "-"
                + Constants.OPERATOR_GT, "\"{2}\".compareTo({3})>0");
    }

    private final JSONObject fields;
    private final JSONObject root;
    private String objectiveField;
    private final Object output;
    private boolean isPredicate;
    private Predicate predicate;
    private final List<Tree> children;
    private final Long count;
    private JSONArray distribution;
    private final double confidence;
    private JSONObject rootDistribution;

    /**
     * Constructor
     */
    public Tree(final JSONObject root, final JSONObject fields,
            final Object objective, final JSONObject rootDistribution) {
        super();

        this.fields = fields;
        this.rootDistribution = rootDistribution;

        if (objective != null && objective instanceof List) {
            this.objectiveField = (String) ((List) objective).get(0);
        } else {
            this.objectiveField = (String) objective;
        }

        this.root = root;
        this.output = root.get("output");
        this.count = (Long) root.get("count");
        this.confidence = ((Number) root.get("confidence")).doubleValue();

        if (root.get("predicate") instanceof Boolean) {
            isPredicate = true;
        } else {
            JSONObject predicateObj = (JSONObject) root.get("predicate");
            predicate = new Predicate((String) Utils.getJSONObject(fields,
                    predicateObj.get("field") + ".optype"),
                    (String) predicateObj.get("operator"),
                    (String) predicateObj.get("field"),
                    (String) ((JSONObject) fields.get(predicateObj.get("field"))).get("name"),
                    predicateObj.get("value"),
                    (String) predicateObj.get("term"));
        }

        children = new ArrayList<Tree>();
        JSONArray childrenObj = (JSONArray) root.get("children");
        if (childrenObj != null) {
            for (int i = 0; i < childrenObj.size(); i++) {
                JSONObject child = (JSONObject) childrenObj.get(i);
                Tree childTree = new Tree(child, fields, objectiveField, null);
                children.add(childTree);
            }
        }

        JSONArray distributionObj = (JSONArray) root.get("distribution");
        if (distributionObj != null) {
            this.distribution = distributionObj;
        } else if( root.get("objective_summary") != null ) {
            JSONObject objectiveSummaryObj = (JSONObject) root
                    .get("objective_summary");
            if (objectiveSummaryObj.get("bins") != null) {
                this.distribution = (JSONArray) objectiveSummaryObj.get("bins");
            } else if (objectiveSummaryObj.get("counts") != null) {
                this.distribution = (JSONArray) objectiveSummaryObj.get("counts");
            } else if (objectiveSummaryObj.get("categories") != null) {
                this.distribution = (JSONArray) objectiveSummaryObj
                        .get("categories");
            }
        } else {
            JSONObject summary = rootDistribution;
            if (summary.get("bins") != null) {
                this.distribution = (JSONArray) summary.get("bins");
            } else if (summary.get("counts") != null) {
                this.distribution = (JSONArray) summary.get("counts");
            } else if (summary.get("categories") != null) {
                this.distribution = (JSONArray) summary
                        .get("categories");
            }
        }
    }

    /**
     * List a description of the model's fields.
     * 
     */
    public JSONObject listFields() {
        return fields;
    }

    public String getObjectiveField() {
        return objectiveField;
    }

    public boolean isPredicate() {
        return isPredicate;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    /**
     * Checks if the node's value is a category
     *
     * @param node the node to be checked
     * @return true if the node's value is a category
     */
    protected boolean isClassification(Tree node) {
        return node.output instanceof String;
    }

    /**
     * Checks if the subtree structure can be a regression
     *
     * @return true if it's a regression or false if it's a classification
     */
    public boolean isRegression() {
        if( isClassification(this) ) {
            return false;
        }

        if( children.isEmpty() ) {
            return true;
        } else {
            for (Tree child : children) {
                if (isClassification(child) ) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Merges the bins of a regression distribution to the given limit number
     *
     * @param distribution
     * @param limit
     * @return
     */
    protected List<JSONArray> mergeBins(List<JSONArray> distribution, int limit) {
        int length = distribution.size();
        if( limit < 1 || length <= limit || length < 2 ) {
            return distribution;
        }

        int indexToMerge = 2;
        double shortest = Double.MAX_VALUE;
        for(int index = 1; index < length; index++) {
            double distance = ((Number) distribution.get(index).get(0)).doubleValue() -
                    ((Number) distribution.get(index - 1).get(0)).doubleValue();

            if( distance < shortest ) {
                shortest = distance;
                indexToMerge = index;
            }
        }

        List<JSONArray> newDistribution = new ArrayList<JSONArray>(distribution.subList(0, indexToMerge - 1));

        JSONArray left = distribution.get(indexToMerge - 1);

        JSONArray right = distribution.get(indexToMerge);

        JSONArray newBin = new JSONArray();
        newBin.add(0, ( ((((Number) left.get(0)).doubleValue() * ((Number) left.get(1)).doubleValue()) +
                (((Number) right.get(0)).doubleValue() * ((Number) right.get(1)).doubleValue())) /
                (((Number) left.get(1)).doubleValue() + ((Number) right.get(1)).doubleValue()) ) );
        newBin.add(1, ((Number) left.get(1)).longValue() + ((Number) right.get(1)).longValue());


        newDistribution.add(newBin);

        if( indexToMerge < (length - 1) ) {
            newDistribution.addAll(new ArrayList<JSONArray>(distribution.subList(indexToMerge + 1, distribution.size())));
        }

        return mergeBins(newDistribution, limit);
    }

    /**
     * Computes the mean of a distribution in the [[point, instances]] syntax
     *
     * @param distribution
     * @return
     */
    public double mean(List<JSONArray> distribution) {
        double addition = 0.0f;
        long count = 0;

        for (JSONArray bin : distribution) {
            double point = ((Number) bin.get(0)).doubleValue();
            long instances = ((Number) bin.get(1)).longValue();

            addition += point * instances;
            count += instances;
        }

        if( count > 0 ) {
            return addition / count;
        }

        return Double.NaN;
    }


    /**
     * Computes the variance error
     *
     * @param distributionVariance
     * @param population
     * @param rz
     * @return
     */
    protected double regressionError(double distributionVariance, long population, double rz) {
        if( population > 0 ) {
            ChiSquaredDistribution chi2 = new ChiSquaredDistribution(population);
            double ppf = chi2.inverseCumulativeProbability(1 - Erf.erf(rz / Math.sqrt(2)) );
            if( ppf != 0 ) {
                double error = distributionVariance * (population - 1) / ppf;
                error = error * Math.pow((Math.sqrt(population) + rz), 2);
                return Math.sqrt(error/population);
            }
        }

        return Double.NaN;
    }

    /**
     * Computes the standard deviation of a distribution in the
     *  [[point, instances]] syntax
     *
     * @param distribution
     * @param distributionMean
     * @return
     */
    protected double unbiasedSampleVariance(List<JSONArray> distribution, Double distributionMean) {
        double addition = 0.0f;
        double count = 0.0f;

        if( distributionMean == null ) {
            distributionMean = mean(distribution);
        }

        for (JSONArray bin : distribution) {
            double point = ((Number) bin.get(0)).doubleValue();
            double instances = ((Number) bin.get(1)).doubleValue();

            addition += Math.pow((point - distributionMean), 2) * instances;
            count += instances;
        }

        if( count > 1 ) {
            return addition / (count - 1);
        }

        return Double.NaN;
    }


    /**
     * Wilson score interval computation of the distribution for the prediction
     *
     * @param prediction {object} prediction Value of the prediction for which confidence
     *        is computed
     * @param distribution {{array}} distribution Distribution-like structure of predictions
     *        and the associated weights (only for categoricals). (e.g.
     *        {'Iris-setosa': 10, 'Iris-versicolor': 5})
     */
    public static double wsConfidence(Object prediction,
                                      List<JSONArray> distribution) {
        return wsConfidence(prediction, distribution, null, null);
    }


    /**
     * Wilson score interval computation of the distribution for the prediction
     *
     * @param prediction {object} prediction Value of the prediction for which confidence
     *        is computed
     * @param distribution {{array}} distribution Distribution-like structure of predictions
     *        and the associated weights (only for categoricals). (e.g.
     *        {'Iris-setosa': 10, 'Iris-versicolor': 5})
     * @param n {integer} n Total number of instances in the distribution. If
     *        absent, the number is computed as the sum of weights in the
     *        provided distribution
     * @param z {float} z Percentile of the standard normal distribution
     */
    public static double wsConfidence(Object prediction,
                                      List<JSONArray> distribution, Long n, Double z) {
        double norm, z2, n2, wsSqrt, zDefault = DEFAULT_RZ;

        double p = 0.0;

        for (JSONArray element : distribution) {
            if( element.get(0).equals(prediction) ) {
                p = ((Number) element.get(1)).doubleValue();
                break;
            }
        }

        if (z == null) {
            z = zDefault;
        }
        if (p < 0) {
            throw new Error("The distribution weight must be a positive value");
        }
        if (n != null && n < 1) {
            throw new Error(
                    "The total of instances in the distribution must be"
                            + " a positive integer");
        }
        norm = 0.0d;
        for (JSONArray element : distribution) {
            norm += ((Number) element.get(1)).doubleValue();
        }
        if (norm == 0.0d) {
            throw new Error("Invalid distribution norm: "
                    + distribution.toString());
        }
        if (norm != 1.0d) {
            p = p / norm;
        }
        if (n == null) {
            n = (long) norm;
        }
        z2 = z * z;
        n2 = n * n;
        wsSqrt = Math.sqrt((p * (1 - p) / n) + (z2 / (4 * n2)));
        return (p + (z2 / (2 * n)) - (z * wsSqrt)) / (1 + (z2 / n));
    }

    protected long calculateTotalInstances(List<JSONArray> distribution) {
        long count = 0L;

        for (JSONArray bin : distribution) {
            double instances = ((Number) bin.get(1)).doubleValue();
            count += instances;
        }

        return count;
    }


    /**
     * Returns a list that includes all the leaves of the tree.
     *
     * @return the list of leaf nodes
     */
    protected List<Tree> getLeaves() {
        List<Tree> leaves = new ArrayList<Tree>();

        if( !children.isEmpty() ) {
            for (Tree child : children) {
                leaves.addAll(child.getLeaves());
            }
        } else {
            leaves.add(clone());
        }

        return leaves;
    }

    /**
     * Creates a copy of the current tree node
     *
     * @return the copy of the tree node
     */
    protected Tree clone() {
        return new Tree(root, fields, objectiveField, rootDistribution);
    }

    /**
     * Makes a prediction based on a number of field values.
     * 
     * The input fields must be keyed by Id.
     * 
     * .predict({"petal length": 1})
     * 
     */
    public HashMap<Object, Object> predict(final JSONObject inputData) {
        return predict(inputData, null, MissingStrategy.LAST_PREDICTION);

    }

    /**
     * Makes a prediction based on a number of field values.
     * 
     * The input fields must be keyed by Id.
     * 
     * .predict({"petal length": 1})
     * 
     */
    public HashMap<Object, Object> predict(final JSONObject inputData, List<String> path,
                                           MissingStrategy strategy) {
        if (strategy == null) {
            strategy = MissingStrategy.LAST_PREDICTION;
        }

        if( path == null ) {
            path = new ArrayList<String>();
        }

        if( strategy == MissingStrategy.LAST_PREDICTION  ) {

            if (this.children != null && this.children.size() > 0) {
                LOGGER.debug("Has children!");
                for (int i = 0; i < this.children.size(); i++) {
                    LOGGER.debug("Child iteration");
                    Tree child = this.children.get(i);

                    String field = child.predicate.getField();
                    Object inputValue = inputData.get(((JSONObject) fields
                            .get(field)).get("name"));
                    if (inputValue == null) {
                        continue;
                    }

                    if( child.predicate.apply(inputData, fields) ) {
                        LOGGER.debug("Predicate applies!");
                        path.add(child.predicate.toRule(fields));
                        LOGGER.debug("Path:" + Arrays.toString(path.toArray()));
                        return child.predict(inputData, path, strategy);
                    }
                }
            }

            LOGGER.debug(String.format("No children. Output: %s ", output));
            LOGGER.debug(String.format("No children. Path: %s ", Arrays.toString(path.toArray())));
            LOGGER.debug(String.format("No children. Confidence: %f ", confidence));
            LOGGER.debug(String.format("No children. Distribution: %s ", distribution.toJSONString()));
            LOGGER.debug(String.format("No children. Instances: %d ", count));

            HashMap<Object, Object> result = new HashMap<Object, Object>();
            result.put("count", this.count);
            result.put("prediction", this.output);
            result.put("path", path);
            result.put("confidence", this.confidence);
            result.put("distribution", this.distribution);
            return result;

        } else {
            TreeHolder lastNode = new TreeHolder();
            Map<Object, Number> finalDistribution = predictProportional(inputData, lastNode, path, false);

            if( isRegression() ) {
                // singular case:
                // when the prediction is the one given in a 1-instance node
                if( finalDistribution.size() == 1 ) {
                    if( finalDistribution.values().toArray(new Number[1])[0].intValue() == 1 ) {
                        HashMap<Object, Object> result = new HashMap<Object, Object>();
                        result.put("count", 1);
                        result.put("prediction", lastNode.getTree().output);
                        result.put("path", path);
                        result.put("confidence", lastNode.getTree().confidence);
                        result.put("distribution", lastNode.getTree().distribution);
                        return result;
                    }
                }

                // when there's more instances, sort elements by their mean
                List<JSONArray> distribution  = convertDistributionMapToSortedArray(predicate, finalDistribution);

                distribution = mergeBins(distribution, BINS_LIMIT);

                double prediction = mean(distribution);

                long totalInstances = calculateTotalInstances(distribution);
                double confindence = regressionError(unbiasedSampleVariance(distribution, prediction),
                        totalInstances, DEFAULT_RZ);

                HashMap<Object, Object> result = new HashMap<Object, Object>();
                result.put("count", totalInstances);
                result.put("prediction", prediction);
                result.put("path", path);
                result.put("confidence", confindence);
                result.put("distribution", distribution);
                return result;
            } else {
                List<JSONArray> distribution  = convertDistributionMapToSortedArray(predicate, finalDistribution);
                long totalInstances = calculateTotalInstances(distribution);
                HashMap<Object, Object> result = new HashMap<Object, Object>();
                result.put("count", totalInstances);
                result.put("prediction", distribution.get(0).get(0));
                result.put("path", path);
                result.put("confidence", wsConfidence(
                            distribution.get(0).get(0), distribution, totalInstances, DEFAULT_RZ));
                result.put("distribution", distribution);
                return result;
            }
        }
    }

    /**
     * Makes a prediction based on a number of field values averaging
     *  the predictions of the leaves that fall in a subtree.
     *
     * Each time a splitting field has no value assigned, we consider
     *  both branches of the split to be true, merging their predictions.
     *  The function returns the merged distribution and the last node
     *  reached by a unique path.
     *
     * @param inputData
     * @param path
     * @param missingFound
     * @return
     */
    protected Map<Object, Number> predictProportional(final JSONObject inputData, final TreeHolder lastNode, List<String> path, Boolean missingFound) {
        if( path == null ) {
            path = new ArrayList<String>();
        }

        Map<Object, Number> finalDistribution = new HashMap<Object, Number>();

        // We are in a leaf node... the only thing we need to do is return distribution of the node as a Map object
        if( children.isEmpty() ) {
            lastNode.setTree(this);
            return mergeDistributions(new HashMap<Object, Number>(), convertDistributionArrayToMap(distribution));
        }

        if( isOneBranch(children, inputData) ) {
            for (Tree child : children) {
                if( child.getPredicate().apply(inputData, fields) ) {
                    String newRule = child.getPredicate().toRule(fields);
                    if( !path.contains(newRule) && !missingFound ) {
                        path.add(newRule);
                    }
                    return child.predictProportional(inputData, lastNode, path, missingFound);
                }
            }
        } else {
            // missing value found, the unique path stops
            missingFound = true;
            for (Tree child : children) {
                finalDistribution = mergeDistributions(finalDistribution,
                        child.predictProportional(inputData, lastNode, path, missingFound));
            }

            lastNode.setTree(this);
            return  finalDistribution;
        }


        return null;
    }


    /**
     * Check if there's only one branch to be followed
     *
     * @param children
     * @param inputData
     * @return
     */
    protected boolean isOneBranch(final List<Tree> children, final JSONObject inputData) {
        boolean missing = inputData.containsKey(Utils.split(children));
        return missing || missingBranch(children) || noneValue(children);
    }

    /**
     * Checks if the missing values are assigned to a special branch
     *
     * @param children
     * @return
     */
    protected boolean missingBranch(final List<Tree> children) {
        for (Tree child : children) {
            if( child.getPredicate().isMissing() ) {
                return true;
            }
        }
        return false;
    }

    protected boolean noneValue(final List<Tree> children) {
        for (Tree child : children) {
            if( child.getPredicate().getValue() == null ) {
                return true;
            }
        }
        return false;
    }

//    private int termMatches(JSONObject inputData, String text,
//            String fieldLabel, String term) {
//
//        // Checking Full Terms Only
//        String tokenMode = (String) Utils.getJSONObject(this.fields, fieldLabel
//                + ".term_analysis.token_mode");
//        if (tokenMode.equals("full_terms_only")) {
//            return text.equalsIgnoreCase(term) ? 1 : 0;
//        }
//
//        // All and Tokens only
//        Boolean isCaseSensitive = (Boolean) Utils.getJSONObject(this.fields,
//                fieldLabel + ".term_analysis.case_sensitive");
//        int flags = isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
//
//        JSONObject termForms = (JSONObject) Utils.getJSONObject(this.fields,
//                fieldLabel + ".summary.term_forms");
//
//        HashMap<String, Boolean> caseSensitive = new HashMap<String, Boolean>();
//        Iterator iter = inputData.keySet().iterator();
//        while (iter.hasNext()) {
//            String key = (String) iter.next();
//            caseSensitive.put(key.toLowerCase(), false);
//        }
//
//        JSONArray relatedTerms = (JSONArray) termForms.get(term);
//        String regexp = "(\\b|_)" + Pattern.quote(term) + "(\\b|_)";
//        for (int i = 0; relatedTerms != null && i < relatedTerms.size(); i++) {
//            regexp += "|(\\b|_)" + (String) relatedTerms.get(i) + "(\\b|_)";
//        }
//
//        Pattern pattern = Pattern.compile(regexp, flags);
//        Matcher matcher = pattern.matcher(text);
//        int count = 0;
//        while (matcher.find()) {
//            count++;
//        }
//
//        return count;
//    }

    /**
     * Adds up a new distribution structure to a map formatted distribution
     *
     * @param distribution
     * @param newDistribution
     * @return
     */
    protected Map<Object, Number> mergeDistributions(Map<Object, Number> distribution, Map<Object, Number> newDistribution) {
        for (Object value : newDistribution.keySet()) {
            if( !distribution.containsKey(value) ) {
                distribution.put(value, 0);
            }
            distribution.put(value, distribution.get(value).intValue() + newDistribution.get(value).intValue());
        }

        return distribution;
    }

    /**
     * We switch the Array to a Map structure in order to be more easily manipulated
     *
     * @param distribution current distribution as an JSONArray instance
     * @return the distribution as a Map instance
     */

    protected Map<Object, Number> convertDistributionArrayToMap(JSONArray distribution) {
        Map<Object, Number> newDistribution = new HashMap<Object, Number>();
        for (Object distValueObj : distribution) {
            JSONArray distValueArr = (JSONArray) distValueObj;
            newDistribution.put(distValueArr.get(0), (Number) distValueArr.get(1));
        }

        return newDistribution;
    }

    /**
     * We switch the Array to a Map structure in order to be more easily manipulated
     *
     * @param distribution current distribution as an JSONArray instance
     * @return the distribution as a Map instance
     */

    protected List<JSONArray> convertDistributionMapToSortedArray(final Predicate predicate, Map<Object, Number> distribution) {
        List<JSONArray> newDistribution = new ArrayList<JSONArray>(distribution.size());

        String opType = Constants.OPTYPE_NUMERIC;

        for (Object key : distribution.keySet()) {
            JSONArray element = new JSONArray();
            element.add(key);
            element.add(distribution.get(key));
            newDistribution.add(element);

            if( key instanceof Number ) {
                opType = Constants.OPTYPE_NUMERIC;
            } else if( key instanceof String ) {
                opType = Constants.OPTYPE_TEXT;
            }
        }

        if( distribution != null && !distribution.isEmpty() ) {
            final String finalOpType = opType;

            Collections.sort(newDistribution, new Comparator<JSONArray>() {
                @Override
                public int compare(JSONArray jsonArray1, JSONArray jsonArray2) {
                    if( Constants.OPTYPE_NUMERIC.equals(finalOpType) ) {
                        return Double.compare( ((Number) jsonArray1.get(0)).doubleValue(),
                                ((Number) jsonArray2.get(0)).doubleValue());
                    } else if( Constants.OPTYPE_TEXT.equals(finalOpType) ) {
                        return ((String) jsonArray1.get(0)).compareTo( (String) jsonArray2.get(0));
                    } else { // OPTYPE_DATETIME
                        // TODO: implement this
                        throw new UnsupportedOperationException();
                    }
                }
            });
        }

        return newDistribution;
    }

    /**
     * Translates a tree model into a set of IF-THEN rules.
     * 
     * @param depth
     *            controls the size of indentation
     */
    public String generateRules(final int depth) {
        String rules = "";
        if (this.children != null && this.children.size() > 0) {
            for (int i = 0; i < this.children.size(); i++) {
                Tree child = this.children.get(i);
                String fieldName = (String) Utils.getJSONObject(fields,
                        child.predicate.getField() + ".name");
                rules += MessageFormat.format("{0} IF {1} {2} {3} {4}\n",
                        new String(new char[depth]).replace("\0", INDENT),
                        fieldName, child.predicate.getOperator(),
                        child.predicate.getValue(), child.children != null
                                && child.children.size() > 0 ? "AND" : "THEN");

                rules += child.generateRules(depth + 1);
            }
        } else {
            String fieldName = (String) Utils.getJSONObject(fields,
                    objectiveField + ".name");
            rules += MessageFormat.format("{0} {1} = {2}\n", new String(
                    new char[depth]).replace("\0", INDENT),
                    this.objectiveField != null ? fieldName : "Prediction",
                    this.output);
        }

        return rules;
    }

    /**
     * Prints out an IF-THEN rule version of the tree.
     * 
     * @param depth
     *            controls the size of indentation
     */
    public String rules(final int depth) {
        return generateRules(depth);
    }

    /**
     * Translate the model into a set of "if" java statements.
     * 
     * @param depth
     *            controls the size of indentation
     */
    public String javaBody(final int depth, final String methodReturn) {
        String instructions = "";
        if (this.children != null && this.children.size() > 0) {
            for (int i = 0; i < this.children.size(); i++) {
                Tree child = this.children.get(i);
                String fieldName = (String) Utils.getJSONObject(fields,
                        child.predicate.getField() + ".name");

                String comparison = JAVA_OPERATOR.get(child.predicate
                        .getOpType() + "-" + child.predicate.getOperator());
                instructions += MessageFormat.format("{0}if ({1} != null && "
                        + comparison + ") '{'\n",
                        new String(new char[depth]).replace("\0", INDENT),
                        Utils.slugify(fieldName), Utils.slugify(fieldName),
                        child.predicate.getValue() + "");

                instructions += child.javaBody(depth + 1, methodReturn);
                instructions += new String(new char[depth]).replace("\0",
                        INDENT) + "}\n";
            }
        } else {
            String returnSentence = "{0} return {1};\n";
            if (methodReturn.equals("String")) {
                returnSentence = "{0} return \"{1}\";\n";
            }
            if (methodReturn.equals("Float")) {
                returnSentence = "{0} return {1}F;\n";
            }
            if (methodReturn.equals("Boolean")) {
                returnSentence = "{0} return new Boolean({1});\n";
            }

            instructions += MessageFormat.format(returnSentence, new String(
                    new char[depth]).replace("\0", INDENT), this.output);
        }

        return instructions;
    }


    protected static class TreeHolder {
        private Tree tree;

        public Tree getTree() {
            return tree;
        }

        public void setTree(Tree tree) {
            this.tree = tree;
        }
    }
}
