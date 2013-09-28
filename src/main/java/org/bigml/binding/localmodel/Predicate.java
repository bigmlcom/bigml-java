package org.bigml.binding.localmodel;

import java.text.MessageFormat;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;

/**
 * A predicate to be evaluated in a tree's node.
 * 
 */
public class Predicate {
	
  private String opType;	
  private String operator;
  private String field;
  private Object value;
  private String term;	
  
 /**
  * Constructor
  */
  public Predicate(final String opType, final String operator, final String field, final Object value, final String term) {
    super();
		
    this.opType = opType;
    this.operator = operator;
    this.field = field;
    this.value = value;
    this.term = term;
  }
  
  public String getOpType() {
    return opType;
  }
  
  public String getOperator() {
    return operator;
  }

  public String getField() {
    return field;
  }

  public Object getValue() {
    return value;
  }
  
  public String getTerm() {
	return term;
  }
  
  
 /**
  * Builds rule string from a predicate
  * 
  */
  public String toRule(final JSONObject fields) {
	   return MessageFormat.format("%s %s %s\n",
		  		(String) Utils.getJSONObject(fields, this.field+".name"),
				this.operator,
				this.value.toString());
  }
	
}
