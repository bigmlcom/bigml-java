package org.bigml.binding.localassociation;

import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * Object encapsulating an association rule as described in
 * https://bigml.com/developers/associations
 *
 */
public class AssociationRule {

    private JSONObject rule;
    private String ruleId;
    private Double confidence;
    private Double leverage;
    private Integer[] lhs;
    private Double[] lhsCover;
    private Double pValue;
    private Integer[] rhs;
    private Double[] rhsCover;
    private Double lift;
    private Double[] support;

    /**
     * Constructor
     * 
     * @param ruleInfo	the json for the association rule
     */
    public AssociationRule(final JSONObject ruleInfo) {
        super();

        this.rule = ruleInfo;
        this.ruleId = (String) ruleInfo.get("id");
        this.confidence = ((Number) ruleInfo.get("confidence")).doubleValue();
        this.leverage = ((Number) ruleInfo.get("leverage")).doubleValue();
        this.lhs = parseIntegerJSONArray((JSONArray) ruleInfo.get("lhs"));
        this.lhsCover = parseDoubleJSONArray((JSONArray) ruleInfo.get("lhs_cover"));
        this.pValue = ((Number) ruleInfo.get("p_value")).doubleValue();
        this.rhs = parseIntegerJSONArray((JSONArray) ruleInfo.get("rhs"));
        this.rhsCover = parseDoubleJSONArray((JSONArray) ruleInfo.get("rhs_cover"));
        this.lift = ((Number) ruleInfo.get("lift")).doubleValue();
        this.support = parseDoubleJSONArray((JSONArray) ruleInfo.get("support"));
    }

    public JSONObject getRule() {
        return this.rule;
    }

    public String getRuleId() {
        return this.ruleId;
    }

    public Double getConfidence() {
        return this.confidence;
    }

    public Double getLeverage() {
        return this.leverage;
    }

    public Integer[] getLhs() {
        return this.lhs;
    }

    public Double[] getLhsCover() {
        return this.lhsCover;
    }

    public Double getPValue() {
        return this.pValue;
    }

    public Integer[] getRhs() {
        return this.rhs;
    }

    public Double[] getRhsCover() {
        return this.rhsCover;
    }

    public Double getLift() {
        return this.lift;
    }

    public Double[] getSupport() {
        return this.support;
    }


    /**
     * Transforming the rule to CSV formats
     * Metrics ordered as in ASSOCIATION_METRICS in LocalAssociation.java
     * 
     * @return a list with the csv representation of the rule
     */
    public List<Object> toCsv() {
        List<Object> output = new ArrayList<Object>();
        output.add(this.ruleId);
        output.add(this.lhs);
        output.add(this.rhs);
        output.add(this.lhsCover!=null ? this.lhsCover[0]: null);
        output.add(this.lhsCover!=null ? this.lhsCover[1]: null);
        output.add(this.support!=null ? this.support[0]: null);
        output.add(this.support!=null ? this.support[1]: null);
        output.add(this.confidence);
        output.add(this.leverage);
        output.add(this.lift);
        output.add(this.pValue);
        output.add(this.rhsCover!=null ? this.rhsCover[0]: null);
        output.add(this.rhsCover!=null ? this.rhsCover[1]: null);
        return output;
    }

    /**
     * Transforming the rule to JSON
     * 
     * @return a map with the json representation of the rule
     */
    public Map<String, Object> toJson() {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("id", ruleId);
        output.put("confidence", confidence);
        output.put("leverage", leverage);
        output.put("lhs", lhs);
        output.put("lhsCover", lhsCover);
        output.put("pValue", pValue);
        output.put("rhs", rhs);
        output.put("rhsCover", rhsCover);
        output.put("lift", lift);
        output.put("support", support);
        return output;
    }


    /*
     * Transforming the rule in a LISP flatline filter to select the
     * rows in the dataset that fulfill the rule
     */
    public String toLispRule(final List<AssociationItem> itemList) {
        StringBuilder output = new StringBuilder();
        output.append("(and ");
        for (Integer index: this.lhs) {
            AssociationItem item = (AssociationItem) itemList.get(index);
            output.append(item.toLispRule() + " ");
        }
        for (Integer index: this.rhs) {
            AssociationItem item = (AssociationItem) itemList.get(index);
            output.append(item.toLispRule() + " ");
        }
        output.append(")");
        return output.toString();
    }


    private Double[] parseDoubleJSONArray(JSONArray array) {
        Double[] result = new Double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = ((Number) array.get(i)).doubleValue();
        }
        return result;
    }

    private Integer[] parseIntegerJSONArray(JSONArray array) {
        Integer[] result = new Integer[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = ((Number) array.get(i)).intValue();
        }
        return result;
    }

}
