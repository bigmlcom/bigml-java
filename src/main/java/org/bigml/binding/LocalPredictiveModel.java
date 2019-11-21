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
import org.bigml.binding.localmodel.AbstractTree;
import org.bigml.binding.localmodel.BoostedTree;
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
public class LocalPredictiveModel extends BaseModel
	implements PredictionConverter, SupervisedModelInterface {

    private static final long serialVersionUID = 1L;
    
    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(LocalPredictiveModel.class
            .getName());
    
    private static String MODEL_RE = "^model/[a-f,0-9]{24}$";

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

    private static final String[] OPERATING_POINT_KINDS = {
    		"probability", "confidence" };
   
    private String modelId;
    private BigMLClient bigmlClient;
    private JSONObject root;
    private Tree tree;
    private BoostedTree boostedTree;
    private Map<String, Tree> idsMap;
    private Map<String, List<String>> terms = new HashMap<String, List<String>>();
    private int maxBins = 0;
    
    private Boolean regression = false;
    private JSONObject boosting = null;
    private List<String> classNames = new ArrayList<String>();
    private List<String> objectiveCategories = new ArrayList<String>();
    
    
    public LocalPredictiveModel(JSONObject model) throws Exception {
        this(null, model);
    }
    
    public LocalPredictiveModel(
    		BigMLClient bigmlClient, JSONObject model) throws Exception {
        
    	super(model);
    	
    	this.bigmlClient =
            (bigmlClient != null)
                ? bigmlClient
                : new BigMLClient(null, null, BigMLClient.STORAGE);
        
        try {
        	modelId = (String) model.get("resource");
        	boolean validId = modelId.matches(MODEL_RE);
			if (!validId) {
				throw new Exception(
						modelId + " is not a valid resource ID.");
			}
        	
        	if (!(model.containsKey("resource")
    				&& model.get("resource") != null)) {
    			model = this.bigmlClient.getModel(modelId);
    			
    			if ((String) model.get("resource") == null) {
    				throw new Exception(
    						modelId + " is not a valid resource ID.");
    			}
    		}
        	
        	if (model.containsKey("object") &&
        			model.get("object") instanceof JSONObject) {
        		model = (JSONObject) model.get("object");
    		}
        	
        	// boosting models are to be handled using the BoostedTree
            // class
        	boolean boostedEnsemble = (Boolean) Utils.getJSONObject(
        			model, "boosted_ensemble", false);
        	if (boostedEnsemble) {
        		this.boosting = (JSONObject) Utils.getJSONObject(
        				model, "boosting", null);
        	}
        	
        	String optype = (String) Utils.getJSONObject(
					fields, objectiveField + ".optype");
        	
        	this.regression = 
        			(!isBoosting() && "numeric".equals(optype) ) ||
        			(isBoosting() && boosting.get("objective_class") == null);
        	
            this.root = (JSONObject) Utils.getJSONObject(model, "model.root");

            this.idsMap = new HashMap<String, Tree>();
            
            if (isBoosting()) {
            	this.boostedTree = new BoostedTree(
            			root, this.fields, objectiveField);
            } else {
            	// will store global information in the tree: regression and
                // max_bins number
            	JSONObject distribution = (JSONObject) Utils.getJSONObject(
            			model, "model.distribution.training");
            	JSONObject treeInfo = new JSONObject();
            	treeInfo.put("max_bins", maxBins);
            	this.tree = new Tree(root, this.fields, objectiveField,
            			distribution, null, idsMap, true, treeInfo);
            	
            	if (this.tree.isRegression()) {
                    this.maxBins = this.tree.getMaxBins();
            	} else {
            		JSONArray rootDist = (JSONArray) this.tree.getDistribution();
            		for (Object dist: rootDist) {
            			classNames.add((String) ((JSONArray) dist).get(0));
            		}
            		Collections.sort(classNames);
            		
            		JSONArray categories = (JSONArray) Utils.getJSONObject(
    						(JSONObject) fields.get(objectiveField), 
                			"summary.categories", new JSONArray());
            		
            		for (Object category: categories) {
            			objectiveCategories.add((String) ((JSONArray) category).get(0));
            		}
            	}
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error("Invalid model structure", e);
            throw new InvalidModelException();
        }
    }
    
    
    /**
	 * Returns the class names
	 */
	public List<String> getClassNames() {
		return classNames;
	}
    
    
    /**
     * Correction term based on the training dataset distribution
     * 
     */
    private HashMap<String, Double> laplacianTerm() {
    	HashMap<String, Double> categoryMap = new HashMap<String, Double>();
    	
    	JSONArray rootDist = (JSONArray) this.tree.getDistribution();
    	if (this.tree.getWeighted()) {
    		for (Object dist: rootDist) {
    			JSONArray category = (JSONArray) dist;
    			String cat = (String) category.get(0);
    			categoryMap.put(cat, 0.0);
    		}
    		
    	} else {
    		double total = 0.0;
    		for (Object dist: rootDist) {
    			total += ((Number) ((JSONArray) dist).get(1)).doubleValue();
    		}
    		
    		for (Object dist: rootDist) {
    			JSONArray category = (JSONArray) dist;
    			String cat = (String) category.get(0);
    			Double value = ((Number)category.get(1)).doubleValue();
    			categoryMap.put(cat, value / total);
    		}
    	}
    	
    	return categoryMap;
    }
    
    /**
     * Describes and return the fields for this model.
     */
    public JSONObject fields() {
    	return isBoosting() ? boostedTree.listFields() : tree.listFields();
    }
    
    /**
     * Sets the fields for this model.
     */
    public void setFields(JSONObject fields) {
    	this.fields = fields;
    }
    
    /**
     * Sets the classNames for this model.
     */
    public void setClassNames(List<String> classNames) {
    	this.classNames = classNames;
    }
    
    /**
     * Checks if the tree is a regression problem
     */
    public boolean isRegression() {
    	return tree.isRegression();
    }
    
    /**
     * Checks if the tree is a boosting problem
     */
    public boolean isBoosting() {
        return this.boosting != null && this.boosting.size() > 0;
    }
    
    /**
     * Checks if the tree is a boosting problem
     */
    public JSONObject getBoosting() {
        return this.boosting;
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
     * Returns a list that includes all the leaves of the model.
     *
     * @return all the leave nodes
     */
    public List<BoostedTree> getBoostedLeaves() {
    	return this.boostedTree.getLeaves();
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
    	if (isBoosting() || isRegression()) {
    		throw new IllegalArgumentException(
    				"This method is available for non-boosting " +
    				"categorization models only.");
    	}
    	
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
        return predict(args);
    }
    
    /**
     * Makes a prediction based on a number of field values.
     *
     * The input fields must be keyed by field name.
     */
    public Prediction predict(final JSONObject args)
            throws Exception {
        
        return (Prediction) predict(args, MissingStrategy.LAST_PREDICTION, null, null, true);
    }
    
    /**
     * Makes a prediction based on a number of field values using the 
     * specified Missing Strategy
     *
     * The input fields must be keyed by field name.
     */
    public Prediction predict(final JSONObject args, MissingStrategy strategy)
            throws Exception {
        return predict(args, strategy, null, null, true, null);
    }
    
    

    /**
     * Makes a prediction based on a number of field values using a 
     * Last Prediction Strategy
     *
     * By default the input fields must be keyed by field name but you 
     * can use `byName` to input them directly keyed by id.
     *
     */
    @Deprecated
    public Prediction predict(final String args, Boolean byName) 
    		throws InputDataParseException {
    	
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
     * Makes a prediction based on a number of field values using a 
     * Last Prediction Strategy
     *
     * The input fields must be keyed by field name.
     */
    @Deprecated
    public Prediction predict(final JSONObject args, Boolean byName)
            throws InputDataParseException {
        return predict(args, byName, MissingStrategy.LAST_PREDICTION, null).get(0);
    }

    /**
     * Makes a prediction based on a number of field values using the 
     * specified Missing Strategy
     *
     * The input fields must be keyed by field name.
     */
    @Deprecated
    public Prediction predict(final JSONObject args, Boolean byName, MissingStrategy strategy)
            throws Exception {
        return predict(args, strategy, null, null, true, null);
    }
    
    /**
     * Makes a multiple predictions based on a number of field values using the Last Prediction strategy
     *
     * The input fields must be keyed by field name.
     * 
     * @deprecated
     */
    public List<Prediction> predict(final JSONObject args, Boolean byName, Object multiple)
            throws InputDataParseException {
        return predict(args, byName, MissingStrategy.LAST_PREDICTION, multiple);
    }
    
    /**
     * Makes a multiple predictions based on a number of field values using the Last Prediction strategy
     *
     * The input fields must be keyed by field name.
     */
    public List<Prediction> predict(final JSONObject args, Object multiple)
            throws InputDataParseException {
        return predict(args, MissingStrategy.LAST_PREDICTION, multiple);
    }
    
    /**
     * Convenience version of predict that take as inputs a map from field ids
     * or names to their values as Java objects. See also predict(String,
     * Boolean, Integer, Boolean).
     */
    @Deprecated
    public Prediction predictWithMap(
            final Map<String, Object> inputs, Boolean byName, Boolean withConfidence)
            throws Exception {

        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, MissingStrategy.LAST_PREDICTION, null, null, true);
    }
    
    @Deprecated
    public Prediction predictWithMap(
            final Map<String, Object> inputs, Boolean byName, MissingStrategy missingStrategy)
            throws Exception {

        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, missingStrategy, null, null, true, null);
    }
    
    public Prediction predictWithMap(
            final Map<String, Object> inputs, MissingStrategy missingStrategy)
            throws Exception {

        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, missingStrategy, null, null, true, null);
    }
    
    @Deprecated
    public Prediction predictWithMap(
            final Map<String, Object> inputs, Boolean byName)
            throws Exception {
    	
        return predictWithMap(inputs, byName, MissingStrategy.LAST_PREDICTION);
    }
    
    public Prediction predictWithMap(
            final Map<String, Object> inputs) throws Exception {
        
        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, MissingStrategy.LAST_PREDICTION, null, null, true);
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
     *  @deprecated
     */
    public List<Prediction> predict(final JSONObject args, Boolean byName, MissingStrategy strategy, Object multiple)
            throws InputDataParseException {
    	return predict(args, strategy, multiple);
    }
    

    /**
     * Makes a prediction based on a number of field values.
     *
     * By default the input fields must be keyed by field name but you can use
     *  `byName` to input them directly keyed by id.
     *
     * inputData: Input data to be predicted
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
     */
    public List<Prediction> predict(final JSONObject args, MissingStrategy strategy, Object multiple)
            throws InputDataParseException {

        List<Prediction> outputs = new ArrayList<Prediction>();

        if (strategy == null) {
            strategy = MissingStrategy.LAST_PREDICTION;
        }

        if (args == null) {
            throw new InputDataParseException("Input data format not valid");
        }
        JSONObject inputData = args;


        // Checks and cleans inputData leaving the fields used in the model
        inputData = filterInputData(inputData);

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
    
    public Prediction predict(
			JSONObject inputData, MissingStrategy missingStrategy, 
			JSONObject operatingPoint, String operatingKind, Boolean full) 
			throws Exception {
    	return predict(inputData, missingStrategy, operatingPoint, 
    				  operatingKind, full, null);
    }
    
    /**
	 * Makes a prediction based on a number of field values.
	 * 
	 * @param inputData			Input data to be predicted
	 * @param missingStrategy  LAST_PREDICTION|PROPORTIONAL missing strategy for
     *                     		missing fields
	 * @param operatingPoint
	 * 			In classification models, this is the point of the
     *          ROC curve where the model will be used at. The
     *          operating point can be defined in terms of:
     *                - the positive class, the class that is important to
     *                  predict accurately
     *                - the probability_threshold (or confidence_threshold),
     *                      the probability (or confidence) that is stablished
     *                      as minimum for the positive_class to be predicted.
     *          The operating_point is then defined as a map with
     *          two attributes, e.g.:
     *                  {"positive_class": "Iris-setosa",
     *                   "probability_threshold": 0.5}
     *              or
     *              	{"positive_class": "Iris-setosa",
     *              	"confidence_threshold": 0.5}
	 * @param operatingKind		 
	 * 			"probability" or "confidence". Sets the property that 
	 * 			decides the prediction. Used only if no operating_point 
	 * 			is used
	 * 
	 * @param full
	 * 			Boolean that controls whether to include the prediction's
     *          attributes. By default, only the prediction is produced. If set
     *          to True, the rest of available information is added in a
     *          dictionary format. The dictionary keys can be:
     *             - prediction: the prediction value
     *             - confidence: prediction's confidence
     *             - probability: prediction's probability
     *             - path: rules that lead to the prediction
     *             - count: number of training instances supporting the
     *                      prediction
     *             - next: field to check in the next split
     *             - min: minim value of the training instances in the
     *             		  predicted node
     *             - max: maximum value of the training instances in the
     *              	  predicted node
     *             - median: median of the values of the training instances
     *               		 in the predicted node 
     *             - unused_fields: list of fields in the input data that
     *             					are not being used in the model
	 */
    public Prediction predict(
			JSONObject inputData, MissingStrategy missingStrategy, 
			JSONObject operatingPoint, String operatingKind, Boolean full, 
			List<String> unusedFields) throws Exception {
		
    	if (missingStrategy == null) {
    		missingStrategy = MissingStrategy.LAST_PREDICTION;
        }
    	
    	if (full == null) {
			full = false;
		}
    	
    	// Checks and cleans inputData leaving the fields used in the model
        inputData = filterInputData(inputData, full);
        
        if (unusedFields == null) {
        	unusedFields = (List<String>) inputData.get("unusedFields");
        }
		inputData = (JSONObject) inputData.get("newInputData");
		
		// Strips affixes for numeric values and casts to the final field type
        Utils.cast(inputData, fields);
    	
        // When operating_point is used, we need the probabilities
        // (or confidences) of all possible classes to decide, so se use
        // the `predict_probability` or `predict_confidence` methods
        if (operatingPoint != null) {
        	if (regression) {
        		throw new IllegalArgumentException(
        				"The operating_point argument can only be" +
                        " used in classifications.");
        	}
        	
        	return predictOperating(inputData, missingStrategy, operatingPoint);
        }
        
        if (operatingKind != null) {
        	if (regression) {
        		throw new IllegalArgumentException(
        				"The operating_kind argument can only be" +
                        " used in classifications.");
        	}
        	
        	return predictOperatingKind(inputData, missingStrategy, operatingKind);
        }
        
        Prediction prediction = isBoosting() ?
        		this.boostedTree.predict(inputData, null, missingStrategy) : 
        		this.tree.predict(inputData, null, missingStrategy);
        
        if (isBoosting() && missingStrategy == MissingStrategy.PROPORTIONAL) {
        	// output has to be recomputed and comes in a different format
        	
        	HashMap pred = (HashMap) prediction.get("prediction");
        	
        	Double gSum = (Double) pred.get("g_sum");
        	Double hSum = (Double) pred.get("h_sum");
            Long population = ((Number) prediction.get("count")).longValue();
            List<String> path = (List<String>) prediction.get("path");
        	
            Long lambda = (Long) this.boosting.get("lambda");
            
            prediction =  new Prediction(
            		(- gSum / (hSum +  lambda)), population, path, null);
        }
        
        // next
        List children = (List) prediction.get("children");
        String field = (children == null || children.size() == 0 ? 
        		null : ((AbstractTree) children.get(0)).getPredicate().getField());
        if( field != null && fields.containsKey(field) ) {
            field = fieldsNameById.get(field);
        }
        prediction.setNext(field);
        prediction.remove("children");
        
        if (!isBoosting() && !isRegression()) {
        	String pred = (String) prediction.get("prediction");
        	HashMap<String, Double> probabilities = probabilities(
        			(JSONArray) prediction.get("distribution"));
        	prediction.put("probability", probabilities.get(pred));
        }
       
        if (full) {
        	prediction.put("unused_fields", unusedFields);
        }
        
		return prediction;
	}
    
    
    /**
     * Computes the probability of a distribution using a Laplacian correction
     */
    private HashMap<String, Double> probabilities(JSONArray distribution) {
    	HashMap<String, Double> categoryMap = laplacianTerm();
    	double total = this.tree.getWeighted() ? 0 : 1;
    	for (Object item : distribution) {
            JSONArray distInfo = (JSONArray) item;
            String cat = (String) distInfo.get(0);
            Double value = ((Number) distInfo.get(1)).doubleValue();
            
            categoryMap.put(cat, categoryMap.get(cat) + value);
            total += value;
    	}
    	
    	for (String key : categoryMap.keySet()) {
    		categoryMap.put(key, categoryMap.get(key) / total);
    	}

    	return categoryMap;
    }
    
    
    /**
     * 
     */
    private JSONArray toOutput(HashMap<String, Double> categoryMap, String key) {
    	JSONArray output = new JSONArray();
    	
    	for (String name: classNames) {
    		Prediction element = new Prediction();
    		element.put("category", name);
    		element.put(key, Utils.roundOff(categoryMap.get(name), Constants.PRECISION));
    		output.add(element);
    	}
    	
    	return output;
    }
    
    
    /**
	 * For classification models, Predicts a probability for
     * each possible output class, based on input values.  The input
     * fields must be a dictionary keyed by field name or field ID.
     * 
     * For regressions, the output is a single element list
     * containing the prediction.
     * 
     * @param inputData			Input data to be predicted
     * @param missingStrategy	LAST_PREDICTION|PROPORTIONAL missing strategy
     *                        	for missing fields
	 */
    public JSONArray predictProbability(
			JSONObject inputData, MissingStrategy missingStrategy) 
    		throws Exception {
    	JSONArray output = new JSONArray();
    	
		Prediction prediction = null;
		if (isBoosting() || isRegression()) {
			prediction = predict(inputData, missingStrategy, 
								 null, null, true);
			output.add(prediction);
		} else {
			prediction = predict(inputData, missingStrategy, 
					 			 null, null, true);
			HashMap<String, Double> categoryMap = probabilities(
        			(JSONArray) prediction.get("distribution"));
			output = toOutput(categoryMap, "probability");
		}
		
		return output;
	}

    /**
	 * For classification models, Predicts a confidence for
     * each possible output class, based on input values.  The input
     * fields must be a dictionary keyed by field name or field ID.
     * 
     * For regressions, the output is a single element list
     * containing the prediction.
     *
     * @param inputData			Input data to be predicted
     * @param missingStrategy	LAST_PREDICTION|PROPORTIONAL missing strategy
     *                        	for missing fields
	 */
    public JSONArray predictConfidence(
			JSONObject inputData, MissingStrategy missingStrategy) 
    		throws Exception {
		
    	JSONArray output = new JSONArray();
    	
		Prediction prediction = null;
		if (isRegression()) {
			prediction = predict(inputData, missingStrategy, 
								 null, null, true);
			output.add(prediction);
		} else {
			if (isBoosting()) {
				throw new IllegalArgumentException(
        				"This method is available for non-boosting" +
                        " models only.");
			}
		}
		
		HashMap<String, Double> categoryMap = new HashMap<String, Double>();
		JSONArray distribution = tree.getDistribution();
		for (Object item : distribution) {
            JSONArray distInfo = (JSONArray) item;
            categoryMap.put((String) distInfo.get(0), 0.0);
		}
		
		prediction = predict(inputData, missingStrategy, 
				 null, null, true);
		distribution = (JSONArray) prediction.get("distribution");
		
		for (Object item : distribution) {
            JSONArray distInfo = (JSONArray) item;
            String name = (String) distInfo.get(0);
            categoryMap.put(name, Tree.wsConfidence(name, distribution));
		}
		
		return toOutput(categoryMap, "confidence");
    }
    
    /**
	 * Computes the prediction based on a user-given operating point.
	 */
	private Prediction predictOperating(
			JSONObject inputData, MissingStrategy missingStrategy, 
			JSONObject operatingPoint) throws Exception {
		
		if (missingStrategy == null) {
       		missingStrategy = MissingStrategy.LAST_PREDICTION;
        }
		
		Object[] operating = Utils.parseOperatingPoint(
				operatingPoint, OPERATING_POINT_KINDS, classNames);
		String kind = (String) operating[0];
		Double threshold = (Double) operating[1];
		String positiveClass = (String) operating[2];
		
		JSONArray predictions = null;		
   		if (kind.equals("probability")) {
   			predictions = predictProbability(inputData, missingStrategy);
   		} else {
   			predictions = predictConfidence(inputData, missingStrategy);
   		}
   		
   		for (Object pred: predictions) {
   			Prediction prediction = (Prediction) pred;
			String category = (String) prediction.get("category");
			if (category.equals(positiveClass) &&
					(Double) prediction.get(kind) > threshold) {
				prediction.put("prediction", prediction.get("category"));
		   		prediction.remove("category");
				return prediction;
			}
		}
   		
   		Prediction prediction = (Prediction) predictions.get(0);
   		String category = (String) prediction.get("category");
		if (category.equals(positiveClass)) {
			prediction = (Prediction) predictions.get(1);
		}
   		prediction.put("prediction", prediction.get("category"));
   		prediction.remove("category");
   		
   		return prediction;
	}
    
    
    /**
   	 * Computes the prediction based on a user-given operating kind.
   	 */
   	private Prediction predictOperatingKind(
   			JSONObject inputData, MissingStrategy missingStrategy, 
   			String operatingKind) throws Exception {
   		
   		if (missingStrategy == null) {
       		missingStrategy = MissingStrategy.LAST_PREDICTION;
        }
   		
   		String kind = operatingKind.toLowerCase();
   		if (!Arrays.asList(OPERATING_POINT_KINDS).contains(kind)) {
   			throw new IllegalArgumentException(
   					String.format("Allowed operating kinds are %", OPERATING_POINT_KINDS));
   		}
   		
   		JSONArray predictions = null;		
   		if (kind.equals("probability")) {
   			predictions = predictProbability(inputData, missingStrategy);
   		} else {
   			predictions = predictConfidence(inputData, missingStrategy);
   		}
   		
   		sortPredictions(predictions, kind);
   		
   		Prediction prediction = (Prediction) predictions.get(0);
   		prediction.put("prediction", prediction.get("category"));
   		prediction.remove("category");
   		
   		return prediction;	
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
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
        return tree.rules(Predicate.RuleLanguage.PSEUDOCODE);
    }

    /**
     * Returns a IF-THEN rule set that implements the model.
     */
    public String rules(Predicate.RuleLanguage language) {
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
        return tree.rules(language);
    }

    /**
     * Returns a IF-THEN rule set that implements the model.
     */
    public String rules(Predicate.RuleLanguage language, final String filterId, boolean subtree) {
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
        List<String> idsPath = getIdsPath(filterId);
        return tree.rules(language, idsPath, subtree);
    }
    
    /**
     * Given a prediction string, returns its value in the required type
     *
     * @param valueAsString the prediction value as string
     */
    @Override
    public Object toPrediction(String valueAsString, Locale locale) {
        locale = (locale != null ? locale : BigMLClient.DEFAUL_LOCALE);

        String objectiveFieldName = isBoosting() ?
        		boostedTree.getObjectiveField() : 
        		tree.getObjectiveField();
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
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
        return tree.getNodesInfo(headers, leavesOnly);
    }

    /**
     * Outputs the node structure to in array format, including the
     * header names in the first row
     */
    private List<List> getTreeArray(boolean leavesOnly) {
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
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
    public void exportTreeCSV(String outputFilePath, boolean leavesOnly) 
    		throws IOException {
    	
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
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
    	
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
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
    private void addToGroups(Map<Object, GroupPrediction> groups, List<Predicate> path, Object output,
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
    private long getDepthFirstSearch(Map<Object, GroupPrediction> groups, 
    								 Tree tree, List<Predicate> path) {
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
    	
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
        JSONArray distribution = new JSONArray();
        distribution.addAll(tree.getDistribution());

        Collections.sort(distribution, new Comparator<JSONArray>() {
            @Override
            public int compare(JSONArray o1, JSONArray o2) {
                Object o1Val = o1.get(0);
                Object o2Val = o2.get(0);

                if (o1Val instanceof Number) {
                    o1Val = ((Number) o1Val).doubleValue();
                    o2Val = ((Number) o2Val).doubleValue();
                }

                return ((Comparable) o1Val).compareTo(o2Val);
            }
        });

        return distribution;
    }

    /**
     * Returns model predicted distribution
     */
    public JSONArray getPredictionDistribution() {
    	
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
        return getPredictionDistribution(null);
    }

    /**
     * Returns model predicted distribution
     */
    public JSONArray getPredictionDistribution(Map<Object, GroupPrediction> groups) {
    	
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
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
            @Override
            public int compare(JSONArray o1, JSONArray o2) {
                Object o1Val = o1.get(0);
                Object o2Val = o2.get(0);

                if (o1Val instanceof Number) {
                    o1Val = ((Number) o1Val).doubleValue();
                    o2Val = ((Number) o2Val).doubleValue();
                }
                return ((Comparable) o1Val).compareTo(o2Val);
            }
        });

        return predictions;
    }

    /**
     * Prints summary grouping distribution as class header and details
     *
     */
    public String summarize(Boolean addFieldImportance) throws IOException {
    	
    	if (isBoosting()) {
    		throw new IllegalArgumentException(
    				"This method is not available for boosting models. ");
    	}
    	
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
    // TODO: tableau


    
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
    
   	
   	/**
	  * Sorts the categories in the predicted node according to the
      *  given criteria
	  * 
	  */
	 private void sortPredictions(JSONArray predictions, final String property) {
		Collections.sort(predictions, new Comparator<Prediction>() {
         @Override
         public int compare(Prediction o1, Prediction o2) {
         	Double o1p = (Double) o1.get(property);
         	Double o2p = (Double) o2.get(property);
         	
         	if (o1p.doubleValue() == o2p.doubleValue()) {
         		return ((String) o1.get("category")).
                 		compareTo(((String) o2.get("category")));
         	}
         	
             return o2p.compareTo(o1p);
         }
     });
	}

}
