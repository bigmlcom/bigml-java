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
    private String distributionUnit;
    private Integer maxBins = 0;
    private Double median;
    private double impurity = 0.0;
    private double confidence;
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
        JSONObject summary = null;
        if (distributionObj != null) {
            this.distribution = distributionObj;
        } else if( root.get("objective_summary") != null ) {
            summary = (JSONObject) root
                    .get("objective_summary");
            if (summary.get("bins") != null) {
                this.distribution = (JSONArray) summary.get("bins");
                this.distributionUnit = "bins";
            } else if (summary.get("counts") != null) {
                this.distribution = (JSONArray) summary.get("counts");
                this.distributionUnit = "counts";
            } else if (summary.get("categories") != null) {
                this.distribution = (JSONArray) summary
                        .get("categories");
                this.distributionUnit = "categories";
            }
        } else {
            summary = rootDistribution;
            if (summary.get("bins") != null) {
                this.distribution = (JSONArray) summary.get("bins");
                this.distributionUnit = "bins";
            } else if (summary.get("counts") != null) {
                this.distribution = (JSONArray) summary.get("counts");
                this.distributionUnit = "counts";
            } else if (summary.get("categories") != null) {
                this.distribution = (JSONArray) summary
                        .get("categories");
                this.distributionUnit = "categories";
            }
        }

        if( isRegression() ) {
            maxBins = distribution.size();
            median = null;

            if( summary != null ) {
                median = ((Number) summary.get("median")).doubleValue();
            }

            if( median == null ) {
                median = distributionMedian(distribution, count);
            }
        }

        if( !isRegression() && distribution != null ) {
            impurity = calculateGiniImpurity(distribution, count);
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

    public Double getMedian() {
        return median;
    }

    public Long getCount() {
        return count;
    }

    public double getImpurity() {
        return impurity;
    }

    public double getConfidence() {
        return confidence;
    }

    public JSONArray getDistribution() {
        return distribution;
    }

    public String getDistributionUnit() {
        return distributionUnit;
    }

    public Integer getMaxBins() {
        return maxBins;
    }

    public Object getOutput() {
        return output;
    }

    public List<Tree> getChildren() {
        return children;
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
            distributionMean = Utils.meanOfDistribution(distribution);
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
                                      JSONArray distribution) {
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
                                      JSONArray distribution, Long n, Double z) {
        double norm, z2, n2, wsSqrt, zDefault = DEFAULT_RZ;

        double p = 0.0;

        for (Object bin : distribution) {
            if( ((JSONArray) bin).get(0).equals(prediction) ) {
                p = ((Number) ((JSONArray) bin).get(1)).doubleValue();
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
        for (Object bin: distribution) {
            norm += ((Number) ((JSONArray) bin).get(1)).doubleValue();
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

    protected long calculateTotalInstances(JSONArray distribution) {
        long count = 0L;

        for (Object bin : distribution) {
            double instances = ((Number) ((JSONArray) bin).get(1)).doubleValue();
            count += instances;
        }

        return count;
    }


    /**
     * Returns a list that includes all the leaves of the tree.
     *
     * @param path a List of Strings empty array where the path
     * @return the list of leaf nodes
     */
    protected List<Tree> getLeaves(List<String> path, TreeNodeFilter filter) {
        List<Tree> leaves = new ArrayList<Tree>();

        if( path == null ) {
            path = new ArrayList<String>();
        }

        if( !isPredicate() ) {
            path.add(predicate.toRule(fields));
        }


        if( !children.isEmpty() ) {
            for (Tree child : children) {
                leaves.addAll(child.getLeaves(path, filter));
            }
        } else {
            if( filter == null || !filter.filter(this) ) {
                leaves.add(clone());
            }
        }

        return leaves;
    }

    /**
     * Returns a list that includes all the leaves of the tree.
     *
     * @return the list of leaf nodes
     */
    public List<Tree> getLeaves(TreeNodeFilter filter) {
        return getLeaves(null, filter);
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
    public Prediction predict(final JSONObject inputData, List<String> path,
                                           MissingStrategy strategy) {
        if (strategy == null) {
            strategy = MissingStrategy.LAST_PREDICTION;
        }

        if( path == null ) {
            path = new ArrayList<String>();
        }

        if( strategy == MissingStrategy.LAST_PREDICTION  ) {

            if (this.children != null && this.children.size() > 0) {
                for (int i = 0; i < this.children.size(); i++) {
                    Tree child = this.children.get(i);

                    if( child.predicate.apply(inputData, fields) ) {
                        path.add(child.predicate.toRule(fields));
                        return child.predict(inputData, path, strategy);
                    }
                }
            }

            return new Prediction(this.output, this.confidence, this.count,
                                (isRegression() ? this.median : null),
                                path, this.distribution, this.distributionUnit,
                                children);

        } else if( strategy == MissingStrategy.PROPORTIONAL  ) {
            TreeHolder lastNode = new TreeHolder();
            Map<Object, Number> finalDistribution = predictProportional(inputData, lastNode, path, false, false);

            if( isRegression() ) {
                // singular case:
                // when the prediction is the one given in a 1-instance node
                if( finalDistribution.size() == 1 ) {
                    long instances = finalDistribution.values().toArray(new Number[1])[0].longValue();
                    if(  instances == 1 ) {
                        return new Prediction(lastNode.getTree().getOutput(), lastNode.getTree().getConfidence(),
                                instances, lastNode.getTree().getMedian(),
                                path, lastNode.getTree().getDistribution(), lastNode.getTree().getDistributionUnit(),
                                lastNode.getTree().getChildren());
                    }
                }

                // when there's more instances, sort elements by their mean
                JSONArray distribution  = Utils.convertDistributionMapToSortedArray(finalDistribution);

                String distributionUnit = (distribution.size() > BINS_LIMIT ? "bins" : "counts");

                distribution = Utils.mergeBins(distribution, BINS_LIMIT);
                long totalInstances = calculateTotalInstances(distribution);

                double prediction = Utils.meanOfDistribution(distribution);

                double confidence = regressionError(unbiasedSampleVariance(distribution, prediction),
                        totalInstances, DEFAULT_RZ);

                return new Prediction(prediction, confidence, totalInstances,
                                distributionMedian(distribution, totalInstances),
                                path, distribution, distributionUnit,
                                lastNode.getTree().getChildren());
            } else {
                JSONArray distribution  = Utils.convertDistributionMapToSortedArray(finalDistribution);
                long totalInstances = calculateTotalInstances(distribution);

                return new Prediction(((JSONArray) distribution.get(0)).get(0),
                        wsConfidence(((JSONArray) distribution.get(0)).get(0), distribution,
                                totalInstances, DEFAULT_RZ),
                        totalInstances, null,
                        path, distribution, "categorical",
                        lastNode.getTree().getChildren());
            }
        } else {
            throw new UnsupportedOperationException(
                    String.format("Unsupported missing strategy %s", strategy.name()));
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
    protected Map<Object, Number> predictProportional(final JSONObject inputData, final TreeHolder lastNode, List<String> path,
                                                      Boolean missingFound, Boolean median) {
        if( path == null ) {
            path = new ArrayList<String>();
        }

        Map<Object, Number> finalDistribution = new HashMap<Object, Number>();

        // We are in a leaf node... the only thing we need to do is return distribution of the node as a Map object
        if( children.isEmpty() ) {
            lastNode.setTree(this);
            return Utils.mergeDistributions(new HashMap<Object, Number>(), Utils.convertDistributionArrayToMap(distribution));
        }

        if( isOneBranch(children, inputData) ) {
            for (Tree child : children) {
                if( child.getPredicate().apply(inputData, fields) ) {
                    String newRule = child.getPredicate().toRule(fields);
                    if( !path.contains(newRule) && !missingFound ) {
                        path.add(newRule);
                    }
                    return child.predictProportional(inputData, lastNode, path, missingFound, median);
                }
            }
        } else {
            // missing value found, the unique path stops
            missingFound = true;
            for (Tree child : children) {
                finalDistribution = Utils.mergeDistributions(finalDistribution,
                        child.predictProportional(inputData, lastNode, path, missingFound, median));
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

    /**
     * Returns the median value for a distribution
     *
     * @param distribution
     * @param count
     * @return
     */
    protected Double distributionMedian(JSONArray distribution, Long count) {

        int counter = 0;
        Double previousValue = null;
        for (Object binInfo : distribution) {
            Double value = ((Number) ((JSONArray) binInfo).get(0)).doubleValue();
            counter += ((Number) ((JSONArray) binInfo).get(1)).intValue();
            if( counter > (count / 2) ) {
                if( (count % 2 != 0) && ((counter - 1) == (count / 2)) &&
                        previousValue != null ) {
                    return (value + previousValue) / 2;
                }

                return value;
            }

            previousValue = value;
        }

        return null;
    }

    /**
     * Returns the gini impurity score associated to the distribution in the node
     *
     * @param distribution
     * @param count
     * @return
     */
    protected Double calculateGiniImpurity(JSONArray distribution, Long count) {

        double purity = 0.0;
        if( distribution == null ) {
            return null;
        }

        for (Object binInfo : distribution) {
            int instances = ((Number) ((JSONArray) binInfo).get(1)).intValue();
            purity += Math.pow(instances / count, 2);
        }

        return (1.0 - purity) / 2;
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
