/*
  Tree structure for the BigML local boosted Model

  This module defines an auxiliary Tree structure that is used in the local
  boosted Ensemble to predict locally or embedded into your application
  without needing to send requests to BigML.io.
*/
package org.bigml.binding.localmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bigml.binding.MissingStrategy;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A boosted tree-like predictive model.
 * 
 */
public class BoostedTree extends AbstractTree {
		
    /**
     * Logging
     */
    static Logger LOGGER = LoggerFactory.getLogger(
    		BoostedTree.class.getName());

    private final List<BoostedTree> children;
    private final Double g_sum;
    private final Double h_sum;
    
    
    /**
     * Constructor
     */
    public BoostedTree(final JSONObject root, final JSONObject fields,
                final Object objective) {
        super(root, fields, objective);
        
        children = new ArrayList<BoostedTree>();
        JSONArray childrenObj = (JSONArray) root.get("children");
        if (childrenObj != null) {
            for (int i = 0; i < childrenObj.size(); i++) {
                JSONObject child = (JSONObject) childrenObj.get(i);
                BoostedTree childTree = new BoostedTree(child, fields, objectiveField);
                children.add(childTree);
            }
        }
        
        this.g_sum = ((Number) root.get("g_sum")).doubleValue();
        this.h_sum = ((Number) root.get("h_sum")).doubleValue();
    }


    public List<BoostedTree> getChildren() {
        return children;
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
     * There are two possible
     *   strategies to predict when the value for the splitting field
     *   is missing:
     *       0 - LAST_PREDICTION: the last issued prediction is returned.
     *       1 - PROPORTIONAL: we consider all possible outcomes and create
     *                         an average prediction.
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
                    BoostedTree child = this.children.get(i);

                    if( child.predicate.apply(inputData, fields) ) {
                        path.add(child.predicate.toRule(fields));
                        return child.predict(inputData, path, strategy);
                    }
                }
            }
            
            return new Prediction(this.output, this.count, path, children);
        } else if( strategy == MissingStrategy.PROPORTIONAL  ) {
            TreeHolder lastNode = new TreeHolder();
            Map<Object, Object> finalDistribution = predictProportional(
            		inputData, lastNode, path, false);
            
            return new Prediction(finalDistribution,
            		((Number) finalDistribution.get("count")).longValue(), 
                    path, lastNode.getTree().getChildren());
        } else {
            throw new UnsupportedOperationException(
                    String.format("Unsupported missing strategy %s", strategy.name()));
        }
    }
    
    
    /**
     * Makes a prediction based on a number of field values averaging
     * the predictions of the leaves that fall in a subtree.
     *
     * Each time a splitting field has no value assigned, we consider
     * both branches of the split to be true, merging their predictions.
     * The function returns the merged distribution and the last node
     * reached by a unique path.
     *
     * @param inputData
     * @param path
     * @param missingFound
     */
    protected Map<Object, Object> predictProportional(
    		final JSONObject inputData, final TreeHolder lastNode, 
    		List<String> path, Boolean missingFound) {
        
    	if( path == null ) {
            path = new ArrayList<String>();
        }

    	Map<Object, Object> finalDistribution = 
    			new HashMap<Object, Object>();

        // We are in a leaf node... the only thing we need to do is return 
    	// distribution of the node as a Map object
        if( children.isEmpty() ) {
        	finalDistribution.put("g_sum", g_sum);
        	finalDistribution.put("h_sum", h_sum);
        	finalDistribution.put("count", count);
        	
            lastNode.setTree(this);
            return finalDistribution;
        }
        
        String optype = (String) Utils.getJSONObject(
				fields, objectiveField + ".optype");
        
        if (isOneBranch(children, inputData) || optype.equals("text") || 
        	optype.equals("items")) {
        	for (BoostedTree child : children) {
                if( child.getPredicate().apply(inputData, fields) ) {
                    String newRule = child.getPredicate().toRule(fields);
                    if( !path.contains(newRule) && !missingFound ) {
                        path.add(newRule);
                    }
                    return child.predictProportional(inputData, lastNode, path, missingFound);
                }
            }
        } else {
        	//  missing value found, the unique path stops
        	missingFound = true;
        	double gSum = 0.0;
        	double hSum = 0.0;
        	int population = 0;
        	
            for (BoostedTree child : children) {
            	Map<Object, Object> distribution = 
            		child.predictProportional(inputData, lastNode, path, missingFound);
                gSum += (Double) distribution.get("g_sum");
                hSum += (Double) distribution.get("h_sum");
                population += ((Number) distribution.get("count")).longValue();
            }
            
            finalDistribution.put("g_sum", gSum);
        	finalDistribution.put("h_sum", hSum);
        	finalDistribution.put("count", population);
        	
            lastNode.setTree(this);
            return finalDistribution;
        }
        
        return null;
    }
    
    
    /**
     * Returns a list that includes all the leaves of the tree.
     *
     * @param path a List of Strings empty array where the path
     * @return the list of leaf nodes
     */
    protected List<BoostedTree> getLeaves(List<String> path) {
        List<BoostedTree> leaves = new ArrayList<BoostedTree>();

        if( path == null ) {
            path = new ArrayList<String>();
        }

        if( !isPredicate() ) {
            path.add(predicate.toRule(fields));
        }

        if( !children.isEmpty() ) {
            for (BoostedTree child : children) {
                leaves.addAll(child.getLeaves(path));
            }
        } else {
            leaves.add(clone());
        }
        
        return leaves;
    }
    
    /**
     * Returns a list that includes all the leaves of the tree.
     *
     * @return the list of leaf nodes
     */
    public List<BoostedTree> getLeaves() {
        return getLeaves(null);
    }
    
    /**
     * Creates a copy of the current boosted tree node
     *
     * @return the copy of the boosted tree node
     */
    protected BoostedTree clone() {
        return new BoostedTree(tree, fields, objectiveField);
    }
    
    
    protected static class TreeHolder {
        private BoostedTree tree;

        public BoostedTree getTree() {
            return tree;
        }

        public void setTree(BoostedTree tree) {
            this.tree = tree;
        }
    }
}
