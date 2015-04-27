package org.bigml.binding.localmodel;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.special.Erf;
import org.bigml.binding.Constants;
import org.bigml.binding.MissingStrategy;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;

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

    private static final JSONObject languageConversions;

    static {
        InputStream input = Tree.class.getResourceAsStream("/org/bigml/binding/localmodel/languageConversions.json");
        languageConversions = (JSONObject) JSONValue.parse(new InputStreamReader(input));
    }

    private final JSONObject fields;
    private final JSONObject root;
    private String id;
    private String parentId;
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
                final Object objective, final JSONObject rootDistribution,
                final String parentId, final Map<String, Tree> idsMap, final boolean subtree, Integer maxBins) {
        super();

        this.fields = fields;
        this.rootDistribution = rootDistribution;

        maxBins = (maxBins != null ? maxBins : 0);

        if (objective != null && objective instanceof List) {
            this.objectiveField = (String) ((List) objective).get(0);
        } else {
            this.objectiveField = (String) objective;
        }

        this.root = root;
        this.output = root.get("output");

        if (root.get("predicate") instanceof Boolean) {
            isPredicate = true;
        } else {
            JSONObject predicateObj = (JSONObject) root.get("predicate");
            predicate = new Predicate((String) Utils.getJSONObject(fields,
                    predicateObj.get("field") + ".optype"),
                    (String) predicateObj.get("operator"),
                    (String) predicateObj.get("field"),
                    predicateObj.get("value"),
                    (String) predicateObj.get("term"));
        }

        if( root.containsKey("id") ) {
            id = root.get("id").toString();
            this.parentId = parentId;

            // The idsMap is null when cloning
            if( idsMap != null ) {
                idsMap.put(id, this);
            }
        }

        children = new ArrayList<Tree>();
        JSONArray childrenObj = (JSONArray) root.get("children");
        if (childrenObj != null) {
            for (int i = 0; i < childrenObj.size(); i++) {
                JSONObject child = (JSONObject) childrenObj.get(i);
                Tree childTree = new Tree(child, fields, objectiveField, null, id, idsMap, subtree, maxBins);
                children.add(childTree);
            }
        }

        this.count = (Long) root.get("count");
        this.confidence = ((Number) root.get("confidence")).doubleValue();
        this.distribution = null;
        this.distributionUnit = null;

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
            this.maxBins = Math.max(maxBins, distribution.size());
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

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
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
     * Returns the information associated to each of the tree nodes in rows format
     */
    public List getNodesInfo(List<String> headers, boolean leavesOnly) {
        List rows = new ArrayList();

        List row = new ArrayList();
        Map<String, Long> categoryDict = new HashMap<String, Long>();

        if( !isRegression() ) {
            categoryDict = new HashMap<String, Long>();
            for (Object bin : this.distribution) {
                JSONArray binObject = (JSONArray) bin;
                categoryDict.put(binObject.get(0).toString(), ((Number) binObject.get(1)).longValue());
            }
        }

        for (String header : headers) {
            if( header.equals(Utils.getJSONObject(fields, objectiveField + ".name"))) {
                row.add(output);
                continue;
            }

            if( "confidence".equals(header) || "error".equals(header) ) {
                row.add(confidence);
                continue;
            }

            if( "impurity".equals(header) ) {
                row.add(impurity);
                continue;
            }

            if( isRegression() && header.startsWith("bin") ) {
                for (Object bin : this.distribution) {
                    JSONArray binObject = (JSONArray) bin;
                    row.add(binObject.get(0)); // Bin Value
                    row.add(((Number) binObject.get(1)).longValue()); // Bin Instances
                }

                break;
            }

            if( !isRegression() ) {
                row.add(categoryDict.get(header));
            }
        }

        while(row.size() < headers.size() ) {
            row.add(null);
        }

        if( !leavesOnly || (children == null || children.isEmpty()) ) {
            rows.add(row);
            return rows;
        }

        for (Tree child : children) {
            rows.addAll(child.getNodesInfo(headers, leavesOnly));
        }

        return rows;
    }

    /**
     * Creates a copy of the current tree node
     *
     * @return the copy of the tree node
     */
    protected Tree clone() {
        return new Tree(root, fields, objectiveField, rootDistribution, id, null, false, maxBins);
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
    protected String generateRules(final int depth, final Predicate.RuleLanguage language,
                                   final List<String> idsPath,
                                final boolean subtree) {
        String rules = "";

        List<Tree> children = filterNodes(this.children, idsPath, subtree);

        JSONObject conversions = (JSONObject) languageConversions.get(language.name());

        String conditionOperator = Utils.getJSONObject(conversions, "IF", "IF").toString();
        String conditionStart = Utils.getJSONObject(conversions, "IF_START", "").toString();
        String conditionEnd = Utils.getJSONObject(conversions, "IF_END", "").toString();
        String inclusiveOperator = Utils.getJSONObject(conversions, "AND", "AND").toString();
        String startBlockCharacter = Utils.getJSONObject(conversions, "START_BLOCK", "THEN").toString();
        String endBlockCharacter = (String) Utils.getJSONObject(conversions, "END_BLOCK", null);
        String endSentenceCharacter = Utils.getJSONObject(conversions, "END_SENTENCE", "").toString();

        if (children != null && children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                Tree child = children.get(i);
                rules += MessageFormat.format("{0} {1}{2} {3} {4}{5}\n",
                        new String(new char[depth]).replace("\0", INDENT),
                        conditionOperator,
                        conditionStart,
                        child.predicate.toRule(language, fields, "slug"),
                        conditionEnd,
                        child.children != null
                                && child.children.size() > 0 ? inclusiveOperator : startBlockCharacter);
                rules += child.generateRules(depth + 1, language, idsPath, subtree);

                if( endBlockCharacter != null ) {
                    rules += MessageFormat.format("{0} {1}\n",
                            new String(new char[depth]).replace("\0", INDENT),
                            endBlockCharacter);
                }
            }
        } else {
            String fieldName = (String) Utils.getJSONObject(fields,
                    objectiveField + ".slug");
            if( language == Predicate.RuleLanguage.PSEUDOCODE ) {
                rules += MessageFormat.format("{0} {1} = {2}{3}\n", new String(
                                new char[depth]).replace("\0", INDENT),
                        this.objectiveField != null ? fieldName : Utils.slugify("Prediction", null, null),
                        this.output,
                        endSentenceCharacter);
            } else {
                String result = this.output.toString();
                switch (language) {
                    case JAVA:
                        if( !isRegression() ) {
                            result = String.format("\"%s\"", result);
                        }
                        break;

                    case PYTHON:
                        if( !isRegression() ) {
                            result = String.format("'%s'", result);
                        }
                        break;
                }

                rules += MessageFormat.format("{0} return {1}{2}\n", new String(
                                new char[depth]).replace("\0", INDENT),
                                result,
                                endSentenceCharacter);
            }
        }

        return rules;
    }

    /**
     * Filters the contents of a nodesList. If any of the nodes is in the
     * ids list, the rest of nodes are removed. If none is in the ids list
     * we include or exclude the nodes depending on the subtree flag.
     */
    protected List<Tree> filterNodes(List<Tree> nodesList, List<String> ids, boolean subtree) {
        if( nodesList == null || nodesList.isEmpty() ) {
            return null;
        }

        List<Tree> nodes = new ArrayList<Tree>(nodesList);
        if( ids != null && !ids.isEmpty() ) {
            for (Tree node : nodes) {
                if( ids.contains(node.getId()) ) {
                    nodes = new ArrayList<Tree>();
                    nodes.add(node);
                    return nodes;
                }
            }
        }

        if( !subtree ) {
            return new ArrayList<Tree>();
        }

        return nodes;
    }

    /**
     * Prints out an IF-THEN rule version of the tree.
     */
    public String rules() {
        for (Object fieldId : fields.keySet()) {
            String slug = Utils.slugify(Utils.getJSONObject(fields, fieldId + ".name", "").toString(),
                    null, null);
            ((JSONObject) fields.get(fieldId)).put("slug", slug);
        }
        return generateRules(0, Predicate.RuleLanguage.PSEUDOCODE, null, true);
    }

    /**
     * Prints out an rule version of the tree in the informed language.
     */
    public String rules(Predicate.RuleLanguage language) {
        for (Object fieldId : fields.keySet()) {
            String slug = Utils.slugify(Utils.getJSONObject(fields, fieldId + ".name", "").toString(),
                    null, null);
            ((JSONObject) fields.get(fieldId)).put("slug", slug);
        }
        return generateRules(0, language, null, true);
    }

    /**
     * Prints out an rule version of the tree in the informed language.
     */
    public String rules(Predicate.RuleLanguage language, final List<String> idsPath,
                        final boolean subtree) {
        for (Object fieldId : fields.keySet()) {
            String slug = Utils.slugify(Utils.getJSONObject(fields, fieldId + ".name", "").toString(),
                    null, null);
            ((JSONObject) fields.get(fieldId)).put("slug", slug);
        }
        return generateRules(0, language, idsPath, subtree);
    }

    /**
     * Translate the model into a set of "if" java statements.
     * 
     * @param depth
     *            controls the size of indentation
     */
    public String getJavaBody(final List<String> idsPath, final boolean subtree) {
        return getJavaBody(0, "", null, null, idsPath, subtree);
    }

    protected String getJavaBody(final int depth, String body, List<String> conditions,
                                 List<String> cmv, final List<String> idsPath, final boolean subtree) {
        String instructions = "";

        if( cmv == null ) {
            cmv = new ArrayList<String>();
        }

        String alternate = "";
        if( body == null || body.length() == 0 ) {
            alternate = "else if";
        } else {
            if (conditions == null) {
                conditions = new ArrayList<String>();
            }
            alternate = "if";
        }

        String objectiveType = (String) Utils.getJSONObject(fields, objectiveField + ".optype", "");

        List<Tree> children = filterNodes(this.children, idsPath, subtree);

        if (children != null && children.size() > 0) {
            String fieldId = Utils.split(children);
            String fieldName = Utils.getJSONObject(fields, fieldId + ".name", "").toString();

            boolean hasMissingBranch = missingBranch(children) || noneValue(children);

            // the missing is singled out as a special case only when there's
            //  no missing branch in the children list
//            if( !hasMissingBranch &&
//                    !cmv.contains(fieldName)) {
//                conditions.add(String.format("%s == null", fieldName));
//                body += String.format("%s( %s ) {\n", alternate, Utils.join(conditions, " && "));
//                if( "numeric".equals(objectiveType) ) {
//                    body += String.format("return")
//                }
//            }


            for (int i = 0; i < children.size(); i++) {
                Tree child = children.get(i);
//                String fieldName = (String) Utils.getJSONObject(fields,
//                        child.predicate.getField() + ".name");
                String slug = Utils.slugify(fieldName, null, null);

                String comparison = JAVA_OPERATOR.get(child.predicate
                        .getOpType() + "-" + child.predicate.getOperator());
                instructions += MessageFormat.format("{0}if ({1} != null && "
                        + comparison + ") '{'\n",
                        new String(new char[depth]).replace("\0", INDENT),
                        slug, slug,
                        child.predicate.getValue() + "");

                instructions += child.getJavaBody(depth + 1, body, conditions, cmv, idsPath, subtree);
                instructions += new String(new char[depth]).replace("\0",
                        INDENT) + "}\n";
            }
        } else {
            String returnSentence = "{0} return {1};\n";
            if (objectiveType.equals("categorical")) {
                returnSentence = "{0} return \"{1}\";\n";
            }
            if (objectiveType.equals("numeric") ) {
                returnSentence = "{0} return {1}F;\n";
            }

//            String returnSentence = "{0} return {1};\n";
//            if (methodReturn.equals("String")) {
//                returnSentence = "{0} return \"{1}\";\n";
//            }
//            if (methodReturn.equals("Float")) {
//                returnSentence = "{0} return {1}F;\n";
//            }
//            if (methodReturn.equals("Boolean")) {
//                returnSentence = "{0} return new Boolean({1});\n";
//            }

            instructions += MessageFormat.format(returnSentence, new String(
                    new char[depth]).replace("\0", INDENT), this.output);
        }

        return instructions;
    }


    /**
     * Translate the model into a set of "if" statements in Tableau syntax
     *
     * @param depth controls the size of indentation. As soon as a value is missing
     *              that node is returned without further evaluation.
     */
//    public String getTableauBody(int depth, String body, List<String> conditions,
//                                 List<String> cmv, List<String> idsPath, boolean subtree) {
//
//        if( cmv == null ) {
//            cmv = new ArrayList<String>();
//        }
//
//        String alternate = "";
//        if( body == null || body.length() == 0 ) {
//            alternate = "ELSEIF";
//        } else {
//            if (conditions == null) {
//                conditions = new ArrayList<String>();
//            }
//            alternate = "IF";
//        }
//
//        List<Tree> children = filterNodes(this.children, idsPath, subtree);
//
//        if( children != null && !children.isEmpty() ) {
//
//        }
//    }

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
