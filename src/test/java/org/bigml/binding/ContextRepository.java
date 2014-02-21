package org.bigml.binding;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@org.springframework.stereotype.Repository
public class ContextRepository {

	int status;
	String location = null;
	JSONObject source = null;
	JSONObject dataset = null;
	JSONObject model = null;
	JSONObject prediction = null;
	JSONObject evaluation = null;
	JSONObject ensemble = null;
	JSONArray models = null;
  JSONObject batchPrediction = null;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public JSONObject getSource() {
		return source;
	}

	public void setSource(JSONObject source) {
		this.source = source;
	}

	public JSONObject getDataset() {
		return dataset;
	}

	public void setDataset(JSONObject dataset) {
		this.dataset = dataset;
	}
	public JSONObject getModel() {
		return model;
	}

	public void setModel(JSONObject model) {
		this.model = model;
	}

	public JSONObject getPrediction() {
		return prediction;
	}

	public void setPrediction(JSONObject prediction) {
		this.prediction = prediction;
	}

	public JSONObject getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(JSONObject evaluation) {
		this.evaluation = evaluation;
	}

	public JSONObject getEnsemble() {
		return ensemble;
	}

	public void setEnsemble(JSONObject ensemble) {
		this.ensemble = ensemble;
	}

	public JSONArray getModels() {
		return models;
	}

	public void setModels(JSONArray models) {
		this.models = models;
	}

  public JSONObject getBatchPrediction() {
    return batchPrediction;
  }

  public void setBatchPrediction(JSONObject batchPrediction) {
    this.batchPrediction = batchPrediction;
  }

}