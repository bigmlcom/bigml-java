/*
 A Multiple Local Predictive Model.

This module defines a Multiple Model to make predictions locally using multiple
local models.

This module cannot only save you a few credits, but also enormously
reduce the latency for each prediction and let you use your models
offline.

import org.bigml.binding.BigMLClient;
import org.bigml.binding.MultiModel;

BigMLClient bigmlClient = BigMLCliente.getInstance();
JSONObject models = bigmlClient.listModels("my_tag");
MultiModel model = new MultiModel(models);
model.predict({"petal length": 3, "petal width": 1})

 */
package org.bigml.binding;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MultiModel {
	
	/**
	 * Logging
	 */
	static Logger logger = Logger.getLogger(MultiModel.class.getName());
	
	private JSONArray models;
	private MultiVote votes;
	
	
	/** 
	 * Constructor
	 * 
	 * @param ensemble	the json representation for the remote ensemble
	 */
	public MultiModel(Object models) throws Exception {
		super();
		
		if (models instanceof JSONArray) {
			this.models = (JSONArray) models;
		} else {
			this.models = new JSONArray();
			this.models.add(models);
		}
	}
	
	
	/**
	 * Lists all the model/ids that compound the multi model.
	 */
	public JSONArray listModels() {
		return this.models;
	}
	
	
	/**
	 * Makes a prediction based on the prediction made by every model.
	 *
     * The method parameter is a numeric key to the following combination
     * methods in classifications/regressions:
     * 		0 - majority vote (plurality)/ average: PLURALITY_CODE
     *      1 - confidence weighted majority vote / error weighted:
     *             CONFIDENCE_CODE
     *      2 - probability weighted majority vote / average:
     *             PROBABILITY_CODE
	 */
	public HashMap<String, Object> predict(final String inputData, Boolean byName, Integer method, Boolean withConfidence) throws Exception {
		if (method == null) {
			method = MultiVote.PLURALITY;
		}
		if (byName == null) {
			byName = true;
		}
		if (withConfidence == null) {
			withConfidence = false;
		}
		
		votes = this.generateVotes(inputData, byName, withConfidence);

	    return votes.combine(method, withConfidence);
	}
	
	
	/**
	 * Generates a MultiVote object that contains the predictions
	 * made by each of the models.
	 */
	public MultiVote generateVotes(final String inputData, Boolean byName, Boolean withConfidence) throws Exception {
		if (byName == null) {
			byName = true;
		}
		if (withConfidence == null) {
			withConfidence = false;
		}

		HashMap<String, Object>[] votes = (HashMap<String, Object>[]) new HashMap[models.size()];
		for (int i=0; i<models.size(); i++) {
			JSONObject model = (JSONObject) models.get(i);
			LocalPredictiveModel localModel = new LocalPredictiveModel(model);
			
			HashMap<String, Object> prediction = (HashMap<String, Object>) localModel.predict(inputData, byName, withConfidence);
			HashMap<String, Integer> distributionHash = new HashMap<String, Integer>();
			JSONArray predictionsArray = (JSONArray) prediction.get("distribution");
			int count = 0;
			for (int j=0; j<predictionsArray.size(); j++) {
				JSONArray pred = (JSONArray) predictionsArray.get(j);
				distributionHash.put((String) pred.get(0), ((Long) pred.get(1)).intValue());
				count += ((Long) pred.get(1)).intValue();
			}
			prediction.put("distribution", distributionHash);
			prediction.put("count", count);
			votes[i] = prediction;
		}
		
		return new MultiVote(votes);
	}
	
}
