package org.bigml.binding.localanomaly;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bigml.binding.localmodel.Predicate;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tree structure for the BigML local Anomaly Detector
 *
 * This module defines an auxiliary Tree structure that is used in the local
 * Anomaly Detector to score anomalies locally or embedded into your application
 * without needing to send requests to BigML.io.
 *
 * An anomaly tree-like predictive model.
 */
public class AnomalyTree {

    private JSONObject fields;

    private Predicates predicates;

    private String id;
    private String objectiveFieldId;

    private List<AnomalyTree> children;

    public AnomalyTree(JSONObject tree, String objectiveFieldId, JSONObject fields) {
        this.fields = fields;
        this.objectiveFieldId = objectiveFieldId;

        Object treePredicates = Utils.getJSONObject(tree, "predicates");
        if( treePredicates == null ) {
            throw new IllegalStateException("The predicates property is not available in the AnomalyTree instance");
        }

        if( treePredicates instanceof Boolean ) {
            List<Predicate> treePredicatesList = new ArrayList<Predicate>();
            treePredicatesList.add(new TruePredicate());
            this.predicates = new Predicates(treePredicatesList);
        } else if( treePredicates instanceof JSONArray ) {
            this.predicates = new Predicates((JSONArray) treePredicates);
        } else {
            throw new IllegalArgumentException(String.format("Invalid predicates type. Type found %s",
                    treePredicates.getClass().getName()));
        }

        if( tree.containsKey("id") ) {
            this.id = tree.get("id").toString();
        } else {
            this.id = null;
        }

        this.children = new ArrayList<AnomalyTree>();
        JSONArray treeChildren = (JSONArray) tree.get("children");

        if( treeChildren != null ) {
            for (Object treeChild : treeChildren) {
                children.add(new AnomalyTree((JSONObject) treeChild, objectiveFieldId, fields));
            }
        }
    }

    public boolean apply(JSONObject inputData) {
        return this.predicates.apply(inputData, fields);
    }

    public String toRule() {
        return this.predicates.toRule(fields);
    }

    /**
     * Lists a description of the model's fields.
     */
    public String listFields() {
        StringBuilder builder = new StringBuilder(String.format("<%-32s : %s>\n",
                Utils.getJSONObject(fields, String.format("%s.name", objectiveFieldId)),
                Utils.getJSONObject(fields, String.format("%s.optype", objectiveFieldId))));

        // TODO: order the fields first
        for (Object fieldId : fields.keySet()) {
            if( !objectiveFieldId.equals(fieldId) ) {
                JSONObject field = (JSONObject) fields.get(fieldId);
                builder.append(String.format("<%-32s : %s>\n",
                        Utils.getJSONObject(field, "name", "Unknown!!"),
                        Utils.getJSONObject(field, "optype", "Unknown!!")));
            }
        }

        return builder.toString();
    }

    /**
     * Returns the depth of the node that reaches the input data instance
     * when ran through the tree, and the associated set of rules.
     *
     * If a node has any children whose predicates are all true given
     * the instance, then the instance will flow through that child.
     * If the node has no children or no children with all valid predicates,
     * then it outputs the depth of the node.
     *
     * @return
     */
    public AnomalyDepth depth(JSONObject inputData) {
        return depth(inputData, null, 0);
    }

    protected AnomalyDepth depth(JSONObject inputData, List<String> path, int depth) {
        if( path == null ) {
            path = new ArrayList<String>();
        }

        // root node: if predicates are met, depth becomes 1, otherwise is 0
        if( depth == 0 ) {
            if( !this.predicates.apply(inputData, fields) ) {
                return new AnomalyDepth(path, depth);
            }

            depth++;
        }

        if( this.children != null ) {
            for (AnomalyTree child : this.children) {
                if( child.apply(inputData) ) {
                    path.add(child.toRule());
                    return child.depth(inputData, path, ++depth);
                }
            }
        }

        return new AnomalyDepth(path, depth);
    }

    public static final class AnomalyDepth {
        private List<String> path;
        private int depth;

        public AnomalyDepth(List<String> path, int depth) {
            this.depth = depth;
            this.path = path;
        }

        public List<String> getPath() {
            return path;
        }

        public int getDepth() {
            return depth;
        }
    }
}
