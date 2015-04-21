package org.bigml.binding.localmodel;

import org.json.simple.JSONArray;

import java.util.HashMap;
import java.util.List;

/**
 * Class for the Tree Prediction object
 *
 * This module defines an auxiliary Prediction object that is used in the Tree module
 * to store all the available prediction info.
 *
 * A Prediction object containing the predicted Node info or the subtree grouped
 * prediction info for proportional missing strategy
 */
public class Prediction extends HashMap<Object, Object> {

    public Prediction() {
        super(9);
    }

    public Prediction(Object prediction, Double confidence, Long count, Double median,
                      List<String> path, JSONArray distribution, String distributionUnit,
                      List<Tree> children ) {
        super(9);

        setPrediction(prediction);
        setConfidence(confidence);
        setCount(count);
        setMedian(median);
        setPath(path);
        setDistribution(distribution);
        setDistributionUnit(distributionUnit);
        setChildren(children);
    }

    public Object getPrediction() {
        return this.get("prediction");
    }

    public void setPrediction(Object output) {
        this.put("prediction", output);
    }

    public Double getConfidence() {
        return (Double) this.get("confidence");
    }

    public void setConfidence(Double confidence) {
        this.put("confidence", confidence);
    }

    public Long getCount() {
        return (Long) this.get("count");
    }

    public void setCount(Long count) {
        this.put("count", count);
    }

    public Double getMedian() {
        return (Double) this.get("median");
    }

    public void setMedian(Double median) {
        this.put("median", median);
    }

    public Double getProbability() {
        return (Double) this.get("probability");
    }

    public void setProbability(Double probability) {
        this.put("probability", probability);
    }

    public List<String> getPath() {
        return (List<String>) this.get("path");
    }

    public void setPath(List<String> path) {
        this.put("path", path);
    }

    public JSONArray getDistribution() {
        return (JSONArray) this.get("distribution");
    }

    public void setDistribution(JSONArray distribution) {
        this.put("distribution", distribution);
    }

    public String getDistributionUnit() {
        return (String) this.get("distributionUnit");
    }

    public void setDistributionUnit(String distributionUnit) {
        this.put("distributionUnit", distributionUnit);
    }

    public List<Tree> getChildren() {
        return (List<Tree>) this.get("children");
    }

    public void setChildren(List<Tree> children) {
        this.put("children", children);
    }

    /**
     * The field that determines next split in the tree
     */
    public String getNext() {
        return (String) this.get("next");
    }

    public void setNext(String nextField) {
        this.put("next", nextField);
    }

}
