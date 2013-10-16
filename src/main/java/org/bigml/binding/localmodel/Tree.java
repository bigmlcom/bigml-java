package org.bigml.binding.localmodel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bigml.binding.Constants;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A tree-like predictive model.
 * 
 */
public class Tree {
	
 /**
  * Logging
  */
  static Logger logger = Logger.getLogger(Tree.class.getName());
    
  final static String INDENT = "    ";
  
  // Map operator str to its corresponding java operator
  static HashMap<String, String> JAVA_OPERATOR = new HashMap<String, String>();
  static {
	  JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC+"-"+Constants.OPERATOR_LT, "{2} < {3}");
	  JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC+"-"+Constants.OPERATOR_LE, "{2} <= {3}");
	  JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC+"-"+Constants.OPERATOR_EQ, "{2} = {3}");
	  JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC+"-"+Constants.OPERATOR_NE, "{2} != {3}");
	  JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC+"-"+Constants.OPERATOR_NE2, "{2} != {3}");
	  JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC+"-"+Constants.OPERATOR_GE, "{2} >= {3}");
	  JAVA_OPERATOR.put(Constants.OPTYPE_NUMERIC+"-"+Constants.OPERATOR_GT, "{2} > {3}");
	  JAVA_OPERATOR.put(Constants.OPTYPE_CATEGORICAL+"-"+Constants.OPERATOR_EQ, "\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_CATEGORICAL+"-"+Constants.OPERATOR_NE, "!\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_CATEGORICAL+"-"+Constants.OPERATOR_NE2, "!\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_TEXT+"-"+Constants.OPERATOR_EQ, "\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_TEXT+"-"+Constants.OPERATOR_NE, "!\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_TEXT+"-"+Constants.OPERATOR_NE2, "!\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME+"-"+Constants.OPERATOR_EQ, "\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME+"-"+Constants.OPERATOR_NE, "!\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME+"-"+Constants.OPERATOR_NE2, "!\"{2}\".equals({3})");
	  JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME+"-"+Constants.OPERATOR_LT, "\"{2}\".compareTo({3})<0");
	  JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME+"-"+Constants.OPERATOR_LE, "\"{2}\".compareTo({3})<=0");
	  JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME+"-"+Constants.OPERATOR_GE, "\"{2}\".compareTo({3})>=0");
	  JAVA_OPERATOR.put(Constants.OPTYPE_DATETIME+"-"+Constants.OPERATOR_GT, "\"{2}\".compareTo({3})>0");
  }
  
  
  private JSONObject fields;
  private JSONObject root;
  private String objectiveField;
  private Object output;
  private boolean isPredicate;
  private Predicate predicate;
  private List<Tree> children;
  private Long count;
  private JSONArray distribution;
  private double confidence;	
    
 /**
  * Constructor
  */
  public Tree(final JSONObject root, 
			  final JSONObject fields, 
			  final Object objective) {
	super();
	
	this.fields = fields;
	
	if (objective!=null && objective instanceof List) {
		this.objectiveField = (String) ((List) objective).get(0);
	} else {
		this.objectiveField = (String) objective;
	}
	
	this.root = root;
	this.output = root.get("output");
	this.count = (Long) root.get("count"); 
	this.confidence = (Double) root.get("confidence");
	
	
	if (root.get("predicate") instanceof Boolean) {
		isPredicate = true;
	} else {
		JSONObject predicateObj = (JSONObject) root.get("predicate");
		predicate = new Predicate(
					(String) Utils.getJSONObject(fields, predicateObj.get("field")+".optype"),
					(String) predicateObj.get("operator"),
					(String) predicateObj.get("field"),
					(Object) predicateObj.get("value"),
					(String) predicateObj.get("term"));
	}
	
	children = new ArrayList<Tree>();
	JSONArray childrenObj = (JSONArray) root.get("children");
	if (childrenObj!=null) {
		for (int i=0; i<childrenObj.size(); i++) {
			JSONObject child = (JSONObject) childrenObj.get(i);
			Tree childTree = new Tree(child, fields, objectiveField);
			children.add(childTree);
		}
	}
	
	
	JSONArray distributionObj = (JSONArray) root.get("distribution");
	if (distributionObj!=null) {
		this.distribution = distributionObj;
	} else {
		JSONObject objectiveSummaryObj = (JSONObject) root.get("objective_summary");
		if (objectiveSummaryObj!=null && objectiveSummaryObj.get("categories")!=null) {
			this.distribution = (JSONArray) objectiveSummaryObj.get("categories");
		} else {
			JSONObject summary = (JSONObject) Utils.getJSONObject(fields, objectiveField+".summary");
			if (summary.get("bins")!=null) {
				this.distribution = (JSONArray) summary.get("bins");
			} else {
				if (summary.get("counts")!=null) {
					this.distribution = (JSONArray) summary.get("counts");
				} else {
					if (summary.get("categories")!=null) {
						this.distribution = (JSONArray) summary.get("categories");
					}
					
				}
				
			}
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

 
 /**
  * Makes a prediction based on a number of field values.
  * 
  * The input fields must be keyed by Id.
  * 
  * .predict({"petal length": 1})
  * 
  */
  public Object predict(final JSONObject inputData) {
	  return predict(inputData, false);
	  
  }

/**
  * Makes a prediction based on a number of field values.
  * 
  * The input fields must be keyed by Id.
  * 
  * .predict({"petal length": 1})
  * 
  */
  public Object predict(final JSONObject inputData, Boolean withConfidence) {
	if (withConfidence == null) {
		withConfidence = false;
	}  
	  
	if (this.children!=null && this.children.size()>0) {
	  for (int i=0; i<this.children.size(); i++) {
		Tree child = (Tree) this.children.get(i);
		
		String field = child.predicate.getField();
		Object inputValue = (Object) inputData.get(((JSONObject)fields.get(field)).get("name"));
		if (inputValue==null) {
			continue;
		}

		String opType = child.predicate.getOpType();
		String operator = child.predicate.getOperator();
		Object value = (Object) child.predicate.getValue();
		String term = child.predicate.getTerm();
		
		if (operator.equals(Constants.OPERATOR_EQ) && inputValue.equals(value)) {
			return child.predict(inputData, withConfidence);
		}
		if ((operator.equals(Constants.OPERATOR_NE) || operator.equals(Constants.OPERATOR_NE2)) && !inputValue.equals(value)) {
			return child.predict(inputData, withConfidence);
		}
		if (operator.equals(Constants.OPERATOR_LT)) {
			if (opType.equals(Constants.OPTYPE_DATETIME) && ((String) inputValue).compareTo((String) value)<0) {
				return child.predict(inputData, withConfidence);
			}
			if (opType.equals(Constants.OPTYPE_TEXT) && termMatches(inputData, (String) inputValue, field, term) < ((Number)value).doubleValue()) {
				return child.predict(inputData, withConfidence);
			}
			if (!opType.equals(Constants.OPTYPE_DATETIME) && !opType.equals(Constants.OPTYPE_TEXT) && ((Number)inputValue).doubleValue()<((Number)value).doubleValue()) {
				return child.predict(inputData, withConfidence);
			}
		}
		if (operator.equals(Constants.OPERATOR_LE)) {
			if (opType.equals(Constants.OPTYPE_DATETIME) && ((String) inputValue).compareTo((String) value)<=0) {
				return child.predict(inputData, withConfidence);
			}
			if (opType.equals(Constants.OPTYPE_TEXT) && termMatches(inputData, (String) inputValue,field, term)<=((Number)value).doubleValue()) {
				return child.predict(inputData, withConfidence);
			}
			if (!opType.equals(Constants.OPTYPE_DATETIME) && !opType.equals(Constants.OPTYPE_TEXT) && ((Number)inputValue).doubleValue()<=((Number)value).doubleValue()) {
				return child.predict(inputData, withConfidence);
			}
		}
		if (operator.equals(Constants.OPERATOR_GE)) {
			if (opType.equals(Constants.OPTYPE_DATETIME) && ((String) inputValue).compareTo((String) value)>=0) {
				return child.predict(inputData, withConfidence);
			}
			if (opType.equals(Constants.OPTYPE_TEXT) && termMatches(inputData, (String) inputValue, field, term)>=((Number)value).doubleValue()) {
				return child.predict(inputData);
			}
			if (!opType.equals(Constants.OPTYPE_DATETIME) && !opType.equals(Constants.OPTYPE_TEXT) && ((Number)inputValue).doubleValue()>=((Number)value).doubleValue()) {
				return child.predict(inputData, withConfidence);
			}
		}
		if (operator.equals(Constants.OPERATOR_GT)) {
			if (opType.equals(Constants.OPTYPE_DATETIME) && ((String) inputValue).compareTo((String) value)>0) {
				return child.predict(inputData, withConfidence);
			}
			if (opType.equals(Constants.OPTYPE_TEXT) && termMatches(inputData, (String) inputValue, field, term) > ((Number)value).doubleValue()) {
				return child.predict(inputData, withConfidence);
			}
			if (!opType.equals(Constants.OPTYPE_DATETIME) && !opType.equals(Constants.OPTYPE_TEXT) && ((Number)inputValue).doubleValue()>((Number)value).doubleValue()) {
				return child.predict(inputData, withConfidence);
			}
		}
				
	  }
	}
	
	if (withConfidence) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("count", this.count);
		result.put("prediction", this.output);
		result.put("confidence", this.confidence);
		result.put("distribution", this.distribution);
		return result;
	}
	
    return this.output;
  }
  
  
  private int termMatches(JSONObject inputData, String text, String fieldLabel, String term) {
	  
	  // Checking Full Terms Only
	  String tokenMode = (String) Utils.getJSONObject(this.fields, fieldLabel+".term_analysis.token_mode");
	  if (tokenMode.equals("full_terms_only")) {
		  return text.equalsIgnoreCase(term) ? 1 : 0;
	  }
	  
	  // All and Tokens only
	  
	  int flags = Pattern.CASE_INSENSITIVE;
	  JSONObject termForms = (JSONObject) Utils.getJSONObject(this.fields, fieldLabel+".summary.term_forms");
	  
	  HashMap<String, Boolean> caseSensitive = new HashMap<String, Boolean>();
	  Iterator iter = inputData.keySet().iterator();
	  while (iter.hasNext()) {
		String key = (String) iter.next();
	    caseSensitive.put(key.toLowerCase(), false);
	  }
	 
	  JSONArray relatedTerms = (JSONArray) termForms.get(term);
	  String regexp = "(\\b|_)" + term + "(\\b|_)";
	  for (int i=0; relatedTerms!=null && i<relatedTerms.size(); i++) {
		  regexp +=  "|(\\b|_)" + (String) relatedTerms.get(i) + "(\\b|_)";
	  }
	  
	  Pattern pattern = Pattern.compile(regexp, flags);
	  Matcher matcher = pattern.matcher(text);
	  int count = 0;
	  while (matcher.find()) {
		  count++;
	  }
	  
	  return count;	  
  }
  
	
 /**
  * Translates a tree model into a set of IF-THEN rules.
  * 
  *  @param depth	controls the size of indentation
  */
  public String generateRules(final int depth) {
    String rules = "";
    if (this.children!=null && this.children.size()>0) {
    	for (int i=0; i<this.children.size(); i++) {
			Tree child = (Tree) this.children.get(i);
			String fieldName = (String) Utils.getJSONObject(fields, child.predicate.getField()+".name");
			rules += MessageFormat.format("{0} IF {1} {2} {3} {4}\n",
								StringUtils.repeat(INDENT, depth),
								fieldName,
								child.predicate.getOperator(),
								child.predicate.getValue(),
								child.children!=null && child.children.size()>0 ? "AND" : "THEN");
				
			rules += child.generateRules(depth + 1);
    	}
    } else {
      String fieldName = (String) Utils.getJSONObject(fields, objectiveField+".name"); 
      rules += MessageFormat.format("{0} {1} = {2}\n",
    		  		StringUtils.repeat(INDENT, depth),
					this.objectiveField!=null ? fieldName : "Prediction",
					this.output);			
    }

    return rules;
  }
	

 /**
  * Prints out an IF-THEN rule version of the tree.
  * 
  * @param depth	controls the size of indentation
  */
  public String rules(final int depth) {
    return generateRules(depth);
  }
    
  
  /**
   * Translate the model into a set of "if" java statements.
   * 
   * @param depth	controls the size of indentation
   */
  public String javaBody(final int depth, final String methodReturn) {
    String instructions = "";
    if (this.children!=null && this.children.size()>0) {
	  for (int i=0; i<this.children.size(); i++) {
		Tree child = (Tree) this.children.get(i);
		String fieldName = (String) Utils.getJSONObject(fields, child.predicate.getField()+".name");
		
		String comparison = JAVA_OPERATOR.get(child.predicate.getOpType() + "-" + child.predicate.getOperator());
		instructions += MessageFormat.format("{0}if ({1} != null && " + comparison + ") '{'\n",
				StringUtils.repeat(INDENT, depth),
				Utils.slugify(fieldName),
				Utils.slugify(fieldName),
				child.predicate.getValue()+"");
		
		instructions += child.javaBody(depth + 1, methodReturn);
		instructions += StringUtils.repeat(INDENT, depth) + "}\n";
	  }
    } else {
      String returnSentence = "{0} return {1};\n";
      if ( methodReturn.equals("String")) {
    	  returnSentence = "{0} return \"{1}\";\n";
      }
      if ( methodReturn.equals("Float")) {
    	  returnSentence = "{0} return {1}F;\n";
      }
      if ( methodReturn.equals("Boolean")) {
    	  returnSentence = "{0} return new Boolean({1});\n";
      }
      
      instructions += MessageFormat.format(returnSentence,
    		  StringUtils.repeat(INDENT, depth),
    		  this.output);			
    }

    return instructions;
  }
  
}
