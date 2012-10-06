/*
 A local Predictive Model.

This module defines a Model to make predictions locally or
embedded into your application without needing to send requests to
BigML.io.

This module cannot only save you a few credits, but also enormously
reduce the latency for each prediction and let you use your models
offline.

You can also visualize your predictive model in IF-THEN rule format
and even generate a python function that implements the model.

Example usage (assuming that you have previously set up the BIGML_USERNAME
and BIGML_API_KEY environment variables and that you own the model/id below):

from bigml.api import BigML
from bigml.model import Model

api = BigML()

model = Model(api.get_model('model/5026965515526876630001b2'))
model.predict({"petal length": 3, "petal width": 1})

You can also see model in a IF-THEN rule format with:

model.rules()

 */
package org.bigml.binding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


/**
 * A lightweight wrapper around a Tree model.
 *
 * Uses a BigML remote model to build a local version that can be used
 * to generate prediction locally.
 *
 */
public class LocalPredictiveModel {
	
 /**
  * Logging
  */
  static Logger logger = Logger.getLogger(LocalPredictiveModel.class.getName());
  
  // Map operator str to its corresponding java operator
  static HashMap<String, String> JAVA_TYPES = new HashMap<String, String>();
  static {
    JAVA_TYPES.put("string", "String");
    JAVA_TYPES.put("double", "Double");
    JAVA_TYPES.put("long", "Long");
    JAVA_TYPES.put("float", "Float");
    JAVA_TYPES.put("char", "Char");
    JAVA_TYPES.put("boolean", "Boolean");
    JAVA_TYPES.put("int8", "Float");
    JAVA_TYPES.put("int16", "Float");
    JAVA_TYPES.put("int32", "Float");
  }

  private JSONObject fields;
  private JSONObject root;
  private Tree tree;
  
  
 /** 
  * Constructor
  * 
  * @param model	the json representation for the remote model
  */
  public LocalPredictiveModel(JSONObject model) throws Exception {
    super();

    try {
      if (!BigMLClient.getInstance().modelIsReady(model)) {
        throw new Exception("The model isn't finished yet");
      }
      this.fields = (JSONObject) Utils.getJSONObject(model, "object.model.fields");
      this.root = (JSONObject) Utils.getJSONObject(model, "object.model.root");
			
      String objectiveField;
      Object objectiveFields = Utils.getJSONObject(model, "object.objective_fields");  
      objectiveField = objectiveFields instanceof JSONArray ?
    		  		(String) ((JSONArray) objectiveFields).get(0) :
    		  		(String) objectiveFields;
			
      this.tree = new Tree(root, fields, objectiveField);
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
   * Makes a prediction based on a number of field values.
   *
   * The input fields must be keyed by field name.
   */
   public String predict(final String args) {
    return predict(args, null);
   }

   
  /**
   * Makes a prediction based on a number of field values.
   *
   * The input fields must be keyed by field name.
   */
  public String predict(final String args, Boolean byName) {
    if (byName == null) {
      byName = true;
    }
    
    JSONObject argsData = (JSONObject) JSONValue.parse(args);
    JSONObject inputData = argsData;
    if (!byName) {
      inputData = new JSONObject();
      Iterator iter = argsData.keySet().iterator();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        String fieldName = (String) Utils.getJSONObject(fields, key+".name"); 
        inputData.put(fieldName, argsData.get(key));
      }
    }
    
    return tree.predict(inputData);
  }


  /**
   * Returns a IF-THEN rule set that implements the model.
   *
   * @param depth	controls the size of indentation
   */
  public String rules(final int depth) {
    return tree.rules(depth);
  }
  

  /**
   * Writes a java method that implements the model.
   *
   */
  public String java() {
    Iterator iter = fields.keySet().iterator();
    if (!iter.hasNext()) {
      return null;
    }

    String key = (String) iter.next();
    String methodName = (String) Utils.getJSONObject(fields, key+".name");
    String dataType = (String) Utils.getJSONObject(fields, key+".datatype");
    String methodReturn = JAVA_TYPES.get(dataType);
    
    String methodParams = "";
    while (iter.hasNext()) {
      key = (String) iter.next();
      String name = (String) Utils.getJSONObject(fields, key+".name");
      dataType = (String) Utils.getJSONObject(fields, key+".datatype");
      methodParams += "final " + JAVA_TYPES.get(dataType) + " " + Utils.slugify(name) + ", ";
    }
    methodParams = methodParams.substring(0, methodParams.length()-2);
	  
    return MessageFormat.format("public {0} predict_{1}({2}) '{'\n{3}\n    return null;\n'}'\n",
			  	methodReturn,
			  	Utils.slugify(methodName),
			  	methodParams,
			  	tree.javaBody(1, methodReturn));
  }

}





/**
 * A predicate to be evaluated in a tree's node.
 * 
 */
class Predicate {
	
  private String operator;
  private String field;
  private Long value;
	
 /**
  * Constructor
  */
  public Predicate(final String operator, final String field, final Long value) {
    super();
		
    this.operator = operator;
    this.field = field;
    this.value = value;
  }

  public String getOperator() {
    return operator;
  }

  public String getField() {
    return field;
 }

  public Long getValue() {
    return value;
  }
  
  
 /**
  * Builds rule string from a predicate
  * 
  */
  public String toRule(final JSONObject fields) {
	   return MessageFormat.format("%s %s %s\n",
		  		(String) Utils.getJSONObject(fields, this.field+".name"),
				this.operator,
				this.value);
  }
	
}






/**
 * A tree-like predictive model.
 * 
 */
class Tree {
	
 /**
  * Logging
  */
  static Logger logger = Logger.getLogger(Tree.class.getName());
    
  final static String INDENT = "    ";
  
  final static String OPERATOR_LT = "<";
  final static String OPERATOR_LE = "<=";
  final static String OPERATOR_EQ = "=";
  final static String OPERATOR_NE = "!=";
  final static String OPERATOR_GE = ">=";
  final static String OPERATOR_GT = ">";
  
  // Map operator str to its corresponding java operator
  static HashMap<String, String> JAVA_OPERATOR = new HashMap<String, String>();
  
  static {
	  JAVA_OPERATOR.put(OPERATOR_LT, "<");
	  JAVA_OPERATOR.put(OPERATOR_LE, "<=");
	  JAVA_OPERATOR.put(OPERATOR_EQ, "=");
	  JAVA_OPERATOR.put(OPERATOR_NE, "!=");
	  JAVA_OPERATOR.put(OPERATOR_GE, ">=");
	  JAVA_OPERATOR.put(OPERATOR_GT, ">");
  }
  
  
  private JSONObject fields;
  private JSONObject root;
  private String objectiveField;
  private String output;
  private boolean isPredicate;
  private Predicate predicate;
  private List<Tree> children;
  private Long count;
  private JSONArray distribution;
	
    
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
	this.output = (String) root.get("output");
	this.count = (Long) root.get("count");

	if (root.get("predicate") instanceof Boolean) {
		isPredicate = true;
	} else {
		JSONObject predicateObj = (JSONObject) root.get("predicate");
		predicate = new Predicate(
					(String) predicateObj.get("operator"),
					(String) predicateObj.get("field"),
					(Long) predicateObj.get("value"));
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
  
	
 /**
  * Makes a prediction based on a number of field values.
  * 
  * The input fields must be keyed by Id.
  * 
  * .predict({"petal length": 1})
  * 
  */
  public String predict(final JSONObject inputData) {
	  
	if (this.children!=null && this.children.size()>0) {
	  for (int i=0; i<this.children.size(); i++) {
		Tree child = (Tree) this.children.get(i);
		
		String field = child.predicate.getField();
		
		Long inputValue = (Long) inputData.get(((JSONObject)fields.get(field)).get("name"));
		if (inputValue==null) {
			continue;
		}

		String operator = child.predicate.getOperator();
		Long value = child.predicate.getValue();
		
		if (operator.equals(OPERATOR_LT) && inputValue<value) {
			return child.predict(inputData);
		}
		if (operator.equals(OPERATOR_LE) && inputValue<=value) {
			return child.predict(inputData);
		}
		if (operator.equals(OPERATOR_EQ) && inputValue==value) {
			return child.predict(inputData);
		}
		if (operator.equals(OPERATOR_NE) && inputValue!=value) {
			return child.predict(inputData);
		}
		if (operator.equals(OPERATOR_GE) && inputValue>=value) {
			return child.predict(inputData);
		}
		if (operator.equals(OPERATOR_GT) && inputValue>value) {
			return child.predict(inputData);
		}
				
	  }
	}
    return (String) this.output;
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
		instructions += MessageFormat.format("{0}if ({1} != null && {2} {3} {4}) '{'\n",
							StringUtils.repeat(INDENT, depth),
							Utils.slugify(fieldName),
							Utils.slugify(fieldName),
							JAVA_OPERATOR.get(child.predicate.getOperator()),
							child.predicate.getValue()+"");
					
		instructions += child.javaBody(depth + 1, methodReturn);
		instructions += StringUtils.repeat(INDENT, depth) + "}\n";
	  }
    } else {
      String returnSentence = methodReturn.equals("String") ? "{0} return \"{1}\";\n" : "{0} return {1};\n";
      instructions += MessageFormat.format(returnSentence,
    		  StringUtils.repeat(INDENT, depth),
    		  this.output);			
    }

    return instructions;
  }
	
}