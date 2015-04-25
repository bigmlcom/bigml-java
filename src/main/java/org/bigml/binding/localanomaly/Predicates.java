package org.bigml.binding.localanomaly;

import org.bigml.binding.localmodel.Predicate;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Predicates structure for the BigML local AnomalyTree
 *
 * This module defines an auxiliary Predicates structure that is used in the
 * AnomalyTree to save the node's predicates info.
 *
 */
public class Predicates {

    /**
     * A list of predicates to be evaluated in an anomaly tree's node.
     */
    private List<Predicate> predicates;

    public Predicates(List treePredicates) {
        if( treePredicates != null ) {
            if( treePredicates instanceof JSONArray ) {
                this.predicates = new ArrayList<Predicate>();
                for (Object predicate : treePredicates) {
                    if( predicate instanceof Boolean ) {
                        this.predicates.add(new TruePredicate());
                    } else {
                        JSONObject predicateJSON = (JSONObject) predicate;
                        this.predicates.add(new Predicate(
                                (String) predicateJSON.get("optype"),
                                (String) predicateJSON.get("op"),
                                (String) predicateJSON.get("field"),
                                predicateJSON.get("value"),
                                (String)  predicateJSON.get("term")));
                    }
                }
            } else {
                this.predicates = new ArrayList<Predicate>(treePredicates);
            }
        } else {
            this.predicates = new ArrayList<Predicate>();
        }
    }

    /**
     * Builds rule string from a predicate using the
     * fields NAME property as the label for the operand
     *
     * @param fields a map that contains all the information
     *               associated to each field ind the model
     */
    public String toRule(JSONObject fields) {
        return toRule(fields, "name");
    }

    /**
     * Builds rule string from a predicates list
     *
     * @param fields a map that contains all the information
     *               associated to each field ind the model
     * @param label which attribute of the field to use as identifier
     *              of the field associated to this predicate
     */
    public String toRule(JSONObject fields, String label) {
        StringBuffer rule = new StringBuffer();
        for (Predicate predicate : predicates) {
            if( predicate instanceof TruePredicate )
                continue;

            if( rule.length() > 0 ) {
                rule.append(" and ");
            }

            rule.append(predicate.toRule(fields, label));
        }

        return rule.toString();
    }

    /**
     * Applies the operators defined in each of the predicates to
     * the provided input data
     *
     * @return true if all the predicates were true when applied
     */
    public boolean apply(JSONObject inputData, JSONObject fields) {
        List<Boolean> result = new ArrayList<Boolean>(predicates.size());

        for (Predicate predicate : predicates) {
            if( predicate instanceof TruePredicate )
                result.add(Boolean.TRUE);
            else
                result.add(predicate.apply(inputData, fields));
        }

        return Utils.sameElement(result, true);
    }

}
