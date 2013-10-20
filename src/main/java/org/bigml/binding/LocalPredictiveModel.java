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
model.predict({"petal length": 3, "petal width": 1});

You can also see model in a IF-THEN rule format with:

model.rules()

Or auto-generate a java function code for the model with:

model.java()
 */
package org.bigml.binding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.bigml.binding.localmodel.Tree;
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
	  JAVA_TYPES.put(Constants.OPTYPE_CATEGORICAL+"-string", "String");
	  JAVA_TYPES.put(Constants.OPTYPE_TEXT+"-string", "String");
	  JAVA_TYPES.put(Constants.OPTYPE_DATETIME+"-string", "String");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-double", "Double");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-float", "Float");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-integer", "Float");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-int8", "Float");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-int16", "Float");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-int32", "Float");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-int64", "Float");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-day", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-month", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-year", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-hour", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-minute", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-second", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-millisecond", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-day-of-week", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-day-of-month", "Integer");
	  JAVA_TYPES.put(Constants.OPTYPE_NUMERIC+"-boolean", "Boolean");
  }
  
  private JSONObject fields;
  private JSONObject root;
  private Tree tree;
  private String objectiveField;
  
  
 /** 
  * Constructor
  * 
  * @param model	the json representation for the remote model
  */
  public LocalPredictiveModel(JSONObject model) throws Exception {
    super();

    try {
      if (model.get("resource")==null) {
    	  throw new Exception("Cannot create the Model instance. Could not find the 'model' key in the resource");
      }
      if (!BigMLClient.getInstance().modelIsReady(model)) {
        throw new Exception("The model isn't finished yet");
      }
      
      String prefix = Utils.getJSONObject(model, "object")!=null ? "object." : "";
      
      this.fields = (JSONObject) Utils.getJSONObject(model, prefix+"model.fields");
      if (Utils.getJSONObject(model, prefix+"model.model_fields")!=null) {
    	  this.fields = (JSONObject) Utils.getJSONObject(model, prefix+"model.model_fields");
    	  
    	  JSONObject modelFields = (JSONObject) Utils.getJSONObject(model, prefix+"model.fields");
    	  Iterator iter = this.fields.keySet().iterator();
    	  while (iter.hasNext()) {
			String key = (String) iter.next();
			if (modelFields.get(key)==null) {
				throw new Exception("Some fields are missing to generate a local model. Please, provide a model with the complete list of fields.");
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
      
      this.root = (JSONObject) Utils.getJSONObject(model, prefix+"model.root");
		
      String objectiveField;
      Object objectiveFields = Utils.getJSONObject(model, prefix+"objective_fields");  
      objectiveField = objectiveFields instanceof JSONArray ?
    		  		(String) ((JSONArray) objectiveFields).get(0) :
    		  		(String) objectiveFields;
		
      // Check duplicated field names		  		
      uniquifyVarnames();	 		
    		  		
      this.tree = new Tree(root, this.fields, objectiveField);
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
   public Object predict(final String args) {
    return predict(args, null);
   }

   
   /**
    * Makes a prediction based on a number of field values.
    *
    * The input fields must be keyed by field name.
    */
   public Object predict(final String args, Boolean byName) {
	   return predict(args, byName, null);
   }
   
   
  /**
   * Makes a prediction based on a number of field values.
   *
   * The input fields must be keyed by field name.
   */
  public Object predict(final String args, Boolean byName, Boolean withConfidence) {
    if (byName == null) {
      byName = true;
    }
    if (withConfidence == null) {
    	withConfidence = false;
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
    
    return tree.predict(inputData, withConfidence);
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
  /*public String java() {
    Iterator iter = fields.keySet().iterator();
    if (!iter.hasNext()) {
      return null;
    }

    String methodReturn = "Object";
    String methodName = "";
    String objectiveFieldName = (String) Utils.getJSONObject(fields, tree.getObjectiveField()+".name");
    
    String methodParams = "";
    while (iter.hasNext()) {
      String key = (String) iter.next();
      
      String dataType = (String) Utils.getJSONObject(fields, key+".datatype");
      String opType = (String) Utils.getJSONObject(fields, key+".optype");
      String name = (String) Utils.getJSONObject(fields, key+".name");
      
      if (objectiveFieldName.equals(name)) {
    	  methodName = objectiveFieldName;
    	  methodReturn = JAVA_TYPES.get(opType+"-"+dataType);
      } else {
          methodParams += "final " + JAVA_TYPES.get(opType+"-"+dataType) + " " + Utils.slugify(name) + ", ";
      }
      
    }
    methodParams = methodParams.substring(0, methodParams.length()-2);
	  
    return MessageFormat.format("public {0} predict_{1}({2}) '{'\n{3}\n    return null;\n'}'\n",
			  	methodReturn,
			  	Utils.slugify(methodName),
			  	methodParams,
			  	tree.javaBody(1, methodReturn));
  }*/
  
  
  
  /*
   * Tests if the fields names are unique. If they aren't, a 
   * transformation is applied to ensure unicity.
   */
  private void uniquifyVarnames() {
	  HashSet<String> uniqueNames = new HashSet<String>();
	  Iterator iter = this.fields.keySet().iterator();
	  while (iter.hasNext()) {
		uniqueNames.add((String) iter.next());
	  }
	  if (uniqueNames.size()<this.fields.size()) {
		  transformRepeatedNames();
	  }
	  return;
  }
  
  /*
   * If a field name is repeated, it will be transformed adding its
   * column number. If that combination is also a field name, the
   * field id will be added.
   */
  private void transformRepeatedNames() {
	  // The objective field treated first to avoid changing it.
	  String objectiveFieldId = (String) Utils.getJSONObject(this.fields, objectiveField+".name");
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
		JSONObject fieldJson = (JSONObject) Utils.getJSONObject(this.fields, fieldId);
		String newName = (String) Utils.getJSONObject(fieldJson, "name");
		if (uniqueNames.contains(newName)) {
			newName = MessageFormat.format("{0}{1}", fieldJson.get("name"), fieldJson.get("column_number"));
			if (uniqueNames.contains(newName)) {
				newName = MessageFormat.format("{0}_{1}", newName, fieldId);
			}
			fieldJson.put("name", newName);
		}
		uniqueNames.add(newName);
	  }
  }
  
}