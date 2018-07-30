package org.bigml.binding.localmodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A tree-like predictive model.
 * 
 */
public abstract class AbstractTree {
	
	protected JSONObject fields;
	protected JSONObject tree;
	protected String id;
	protected String objectiveField;
	protected Object output;
	protected boolean isPredicate;
	protected Predicate predicate;
	protected Long count;
    
    
    public AbstractTree(final JSONObject tree, final JSONObject fields, 
    					final Object objective) {
    	
    	super();
    	
    	this.fields = fields;
    	
        if (objective != null && objective instanceof List) {
            this.objectiveField = (String) ((List) objective).get(0);
        } else {
            this.objectiveField = (String) objective;
        }
        
        this.tree = tree;
        this.output = tree.get("output");
        
        if (tree.get("predicate") instanceof Boolean) {
            isPredicate = true;
        } else {
            JSONObject predicateObj = (JSONObject) tree.get("predicate");
            predicate = new Predicate((String) Utils.getJSONObject(fields,
                    predicateObj.get("field") + ".optype"),
                    (String) predicateObj.get("operator"),
                    (String) predicateObj.get("field"),
                    predicateObj.get("value"),
                    (String) predicateObj.get("term"));
        }
        
        if( tree.containsKey("id") ) {
            id = tree.get("id").toString();
        }
        
        this.count = (Long) tree.get("count");
    }
    
    
    public String getId() {
        return id;
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

    public Long getCount() {
        return count;
    }

    public Object getOutput() {
        return output;
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
     * Returns the field that is used by the node to make a decision.
     *
     * @param children
     */
    public static String split(List children) {
        Set<String> fields = new HashSet<String>();
        for (AbstractTree child : (List<AbstractTree>) children) {
            if( !child.isPredicate() ) {
                fields.add(child.getPredicate().getField());
            }
        }

        return fields.size() > 0 ? fields.toArray(new String[fields.size()])[0] : null;
    }
    
    
    /**
     * Check if there's only one branch to be followed
     *
     * @param children
     * @param inputData
     */
    protected boolean isOneBranch(List children, JSONObject inputData) {
        boolean missing = inputData.containsKey(split(children));
        return missing || missingBranch(children) || noneValue(children);
    }
    
    /**
     * Checks if the missing values are assigned to a special branch
     *
     * @param children
     */
    protected boolean missingBranch(final List children) {
        for (AbstractTree child : (List<AbstractTree>) children) {
            if( child.getPredicate().isMissing() ) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean noneValue(final List children) {
        for (AbstractTree child : (List<AbstractTree>) children) {
            if( child.getPredicate().getValue() == null ) {
                return true;
            }
        }
        return false;
    }
}    