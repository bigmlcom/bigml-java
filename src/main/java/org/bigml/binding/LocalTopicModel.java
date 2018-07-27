package org.bigml.binding;

import java.io.*;
import java.util.*;

import org.apache.commons.math3.random.MersenneTwister;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Stemmer;
import org.bigml.binding.utils.StemmerInterface;
import org.bigml.binding.utils.Utils;


/**
 * A local Predictive Topic Model.
 *
 * This module allows you to download and use Topic models for local
 * predicitons. Specifically, the function topic_model.distribution allows you
 * to pass in input text and infers a generative distribution over the topics in
 * the learned topic model.
 *
 * 
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the topicmodel/id
 * below):
 *
 * // API client BigMLClient api = new BigMLClient();
 *
 * // Retrieve a remote topicmodel by id 
 * JSONObject topicmodel = api.
 * 		getTopicModel( "topicmodel/5026965515526876630001b2");
 *
 * // A lightweight wrapper around a TopicModel resource 
 * localTopicModel localTopicModel = new localTopicModel(topicmodel);
 *
 */
public class LocalTopicModel extends ModelFields implements Serializable {

	private static final long serialVersionUID = 1L;

	final static int MAXIMUM_TERM_LENGTH = 30;
	final static int MIN_UPDATES = 16;
	final static int MAX_UPDATES = 512;
	final static int SAMPLES_PER_TOPIC = 128;

	// Logging
	Logger logger = LoggerFactory.getLogger(LocalTopicModel.class);

	private StemmerInterface stemmer;
	private long seed;
	private Boolean caseSensitive = false;
	private Boolean bigrams = false;
	private Integer ntopics;
	private Double[] temp;
	private Double[][] phi;
	private HashMap<String, Integer> termToIndex;
	private JSONArray topics;

	private Double alpha;
	private Double ktimesalpha;

	public LocalTopicModel(JSONObject topicModel) throws Exception {
		super();

		if (topicModel.get("resource") == null) {
			throw new Exception(
					"Cannot create the topicModel instance. Could not "
							+ "find the 'resource' key in the resource");
		}

		if (topicModel.containsKey("object")
				&& topicModel.get("object") instanceof Map) {
			topicModel = (JSONObject) topicModel.get("object");
		}

		if (topicModel.containsKey("topic_model")
				&& topicModel.get("topic_model") instanceof Map) {
			JSONObject status = (JSONObject) topicModel.get("status");

			if (status != null && status.containsKey("code")
					&& AbstractResource.FINISHED == ((Number) status
							.get("code")).intValue()) {

				JSONObject model = (JSONObject) Utils.getJSONObject(topicModel,
						"topic_model");

				this.topics = (JSONArray) model.get("topics");

				String lang = (String) model.get("language");
				this.stemmer = Stemmer.getStemmer(lang);
				
				JSONArray termSet = (JSONArray) model.get("termset");
				this.termToIndex = new HashMap<String, Integer>();

				for (int i = 0; i < termSet.size(); i++) {
					termToIndex.put(stem((String) termSet.get(i)), i);
				}

				JSONArray assignments = (JSONArray) model
						.get("term_topic_assignments");

				this.seed = Math.abs((Long) model.get("hashed_seed"));
				this.caseSensitive = (Boolean) model.get("case_sensitive");
				this.bigrams = (Boolean) model.get("bigrams");
				this.ntopics = ((JSONArray) assignments.get(0)).size();

				this.alpha = (Double) model.get("alpha");
				this.ktimesalpha = this.ntopics * this.alpha;

				Double beta = (Double) model.get("beta");

				this.temp = new Double[this.ntopics];
				for (int i = 0; i < this.ntopics; i++) {
					this.temp[i] = 0d;
				}

				int nterms = termToIndex.size();
				
				Long[] sums = new Long[this.ntopics];
				this.phi = new Double[this.ntopics][nterms];
				
				for (int i = 0; i < this.ntopics; i++) {
					long sum = 0;
					for (int j = 0; j < assignments.size(); j++) {
						sum += (Long) ((JSONArray) assignments.get(j)).get(i);
					}
					sums[i] = sum;
				}
				
				for (int i = 0; i < this.ntopics; i++) {
					for (int j = 0; j < nterms; j++) {
						this.phi[i][j] = 0.0;
					}
				}
				
				for (int i = 0; i < this.ntopics; i++) {
					Double norm = sums[i] + nterms * beta;
					for (int j = 0; j < nterms; j++) {
						Long t = (Long) ((JSONArray) assignments.get(j)).get(i);
						this.phi[i][j] = (t + beta) / norm;
					}
				}
				
				super.initialize((JSONObject) model.get("fields"), null, null,
						null);
			} else {
				throw new Exception("The topicModel isn't finished yet");
			}

		} else {
			throw new Exception(String.format(
					"Cannot create the topic model instance. Could not"
							+ " find the 'topic_model' key in the"
							+ " resource:\n\n%s",
					((JSONObject) topicModel.get("model")).keySet()));
		}
	}
	
	
	/**
     * Returns the distribution of topics given the input text.
     *
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create an associationset for.
     *
     *            {"petal length": 4.4,
     *             "sepal length": 5.1,
     *             "petal width": 1.3,
     *             "sepal width": 2.1,
     *             "species": "Iris-versicolor"}
     */
    public JSONArray distribution(JSONObject inputData) 
    		throws Exception {
    	
    	// Checks and cleans input_data leaving the fields used in the model
        inputData = filterInputData(inputData);
        
        StringBuilder text = new StringBuilder();
        for (Object entry: inputData.keySet()) {
        	text.append(inputData.get(entry).toString());
        	text.append(" ");
        }
        return distributionForText(text.toString());
    }
    
    /**
     * Returns the topic distribution of the given 'text'.
     *
     * @param text
     */
    public JSONArray distributionForText(String text) throws Exception {
    	ArrayList<Integer> doc = tokenize(text);
    	Double[] topicsProbability = infer(doc);
    	
    	JSONArray distribution = new JSONArray();
    	for (int i=0; i<topicsProbability.length; i++) {
    		JSONObject jsonObject = new JSONObject();
    		String name = (String) ((JSONObject) this.topics.get(i)).get("name");
    		jsonObject.put("name", name);
    		jsonObject.put("probability", topicsProbability[i]);
    		distribution.add(jsonObject);
    	}
    	return distribution;
    }
    
    /**
     * Returns the stem of the given term
     */
    private String stem(String term) {
    	return this.stemmer.getStem(term);
    }
    
    /**
     * Takes two terms and appends the index of their concatenation to the
     * provided list of output terms
     */
    private void appendBigram(ArrayList<Integer> outTerms, String first, 
    		String second) {
    	
    	if (this.bigrams != null && first != null && second != null) {
    		String bigram = stem(first + " " + second);
    		
    		if (this.termToIndex.containsKey(bigram)) {
    			outTerms.add(this.termToIndex.get(bigram));
    		}
    	}
    	
    }
    
    /**
     * Tokenizes the input string `text` into a list of integers, one for
     * each term term present in the 'this.termToIndex'. Uses word stemming 
     * if applicable.
     * 
     */
    private ArrayList<Integer> tokenize(String text) {
    	ArrayList<Integer> outTerms = new ArrayList<Integer>();
    	
    	String lastTerm = null;
    	String termBefore = null;
    	boolean spaceWasSep = false;
    	boolean sawChar = false;
    	
    	int index = 0;
    	int length = text.length();
    	
    	while (index < length) {
    		appendBigram(outTerms, termBefore, lastTerm);
    		
    		char ch = text.charAt(index);
    		StringBuilder buf = new StringBuilder();
    		sawChar = false; 
    		
    		if (!Character.isLetterOrDigit(ch)) {
    			sawChar = true; 
    		}
    		
    		while (!Character.isLetterOrDigit(ch) && index < length) {
    			index++;
    			ch = index < length ? text.charAt(index) : 0;
    		}
    		
    		while (index < length && 
    				(Character.isLetterOrDigit(ch) || ch == '\'') &&
    				 buf.toString().length() < MAXIMUM_TERM_LENGTH) {
    			buf.append(ch);
    			index++;
    			ch = index < length ? text.charAt(index) : 0;
    		}
    		
    		if (buf.toString().length() > 0) {
    			String termOut = buf.toString();
    			
    			if (!this.caseSensitive) {
    				termOut = termOut.toLowerCase();
    			}
    			
    			termBefore = spaceWasSep && !sawChar ? lastTerm : null;
    			lastTerm = termOut;
    			
    			if (ch == ' ' || ch == '\n') {
    				spaceWasSep = true;
    			}
    			
    			String tstem = stem(termOut);
    			if (this.termToIndex.containsKey(tstem)) {
        			outTerms.add(this.termToIndex.get(tstem));
        		}
    			
    			index++;
    		}
    	}
    	
    	appendBigram(outTerms, termBefore, lastTerm);
    	
    	return outTerms;
    }
    
    
    /**
     * Samples topics for the terms in the given `document` for `updates`
     * iterations, using the given set of topic `assigments` for
     * the current document and a `normalizer` term derived from
     * the dirichlet hyperparameters
     */
    private Integer[] sampleTopics(ArrayList<Integer> doc, 
    		Integer[] assignments, double normalizer, int updates, 
    		MersenneTwister rng) {
    	
    	Integer[] counts = new Integer[this.ntopics];
    	for (int i = 0; i < this.ntopics; i++) {
			counts[i] = 0;
		}
    	
    	for (int i = 0; i < updates; i++) {

    		for(Integer term: doc) {
    		
    			for (int k = 0; k < this.ntopics; k++) {
    				double topicTerm = this.phi[k][term];
    				double topicDocument = (assignments[k] + this.alpha) / normalizer;
    				this.temp[k] = topicTerm * topicDocument;
    			}
    			
    			for (int k = 1; k < this.ntopics; k++) {
    				this.temp[k] += this.temp[k -1];
    			}
    			
    			double randomValue = rng.nextDouble() * 
    								 this.temp[this.temp.length-1];
    			
    			int topic = 0;
    			while (this.temp[topic] < randomValue && topic < this.ntopics) {
    				topic++;
    			}
    			
    			counts[topic] += 1;
    		}
    		
    	}
    	
    	return counts;
    }
    
	
    /**
     * Samples topics for the terms in the given 'document' assuming uniform
     * topic assignments for `updates` iterations. Used to initialize the 
     * gibbs sampler.
     */
    private Integer[] sampleUniform(
    		ArrayList<Integer> doc, int updates, MersenneTwister rng) {
    	
    	Integer[] counts = new Integer[this.ntopics];
    	for (int i = 0; i < this.ntopics; i++) {
			counts[i] = 0;
		}
    	
    	for (int i = 0; i < updates; i++) {

    		for(Integer term: doc) {
    			
    			for (int k = 0; k < this.ntopics; k++) {
    				this.temp[k] = this.phi[k][term];
    			}
    			
    			for (int k = 1; k < this.ntopics; k++) {
    				this.temp[k] += this.temp[k -1];
    			}
    			
    			double randomValue = rng.nextDouble() * 
    								 this.temp[this.temp.length-1];
    			
    			int topic = 0;
    			while (this.temp[topic] < randomValue && 
    					randomValue < this.ntopics) {
    				topic++;
    			}
    			
    			counts[topic] += 1;
    		}
    		
    	}
    	
    	return counts;
    }
    
    
    /**
     * Infer a topic distribution for a document, presented as a list of
     * term indices.
     */
    private Double[] infer(ArrayList<Integer> doc) {
    	
    	Collections.sort(doc);
    	int updates = 0;
    	
    	if (doc.size() > 0) {
    		updates = SAMPLES_PER_TOPIC * this.ntopics / doc.size();
    		updates = Math.min(MAX_UPDATES, Math.max(MIN_UPDATES, updates));
    	}
    	
    	int[] randomInit = new int[1];
        randomInit[0] = (int) this.seed;  
        MersenneTwister rng = new MersenneTwister(randomInit);
    	
        double normalizer = (doc.size() * updates) + this.ktimesalpha;
    	
    	// Initialization
    	Integer[] uniformCounts = sampleUniform(doc, updates, rng);
    	
    	// Burn-in
    	Integer[] burnCounts = sampleTopics(doc, uniformCounts,
    			normalizer, updates, rng);
    	
    	// Sampling
    	Integer[] sampleCounts = sampleTopics(doc, burnCounts,
    			normalizer, updates, rng);
    		
    	Double[] result = new Double[this.ntopics];
    	for (int k = 0; k < this.ntopics; k++) {
    		result[k] = (sampleCounts[k] + this.alpha)  / normalizer;
		}
    	
    	return result;
    }
    
}
