package org.bigml.binding;

import org.bigml.binding.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A multiple vote prediction in compact format
 *
 *   Uses a number of predictions to generate a combined prediction.
 *   The input should be an ordered list of probability, counts or confidences
 *   for each of the classes in the objective field.
 */
public class MultiVoteList implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final int PRECISION = 5;

    /**
     * Logging
     */
    static Logger LOGGER = LoggerFactory.getLogger(MultiVoteList.class.getName());
    
    ArrayList<List<Double>> predictions;
    
    /**
     * Init method, builds a MultiVoteList with a list of predictions
     *   The constuctor expects a list of well formed predictions like:
     *   [0.2, 0.34, 0.48] which might correspond to confidences of
     *   three different classes in the objective field.
     *
     * @param predictionsArr {array|object} predictions Array of model's predictions
     */
    public MultiVoteList(ArrayList<List<Double>> predictions) {
    	if (predictions == null) {
    		predictions = new ArrayList<List<Double>>();
    	}
    	
    	this.predictions = predictions;
    }
    
    /**
     * Extending the extend method in lists
     */
    public void extend(ArrayList<List<Double>> predictionList) {
    	this.predictions.addAll(predictionList);
    }
    
    /**
     * Extending the extend method in lists
     */
    public void extend(MultiVoteList predictionList) {
    	this.predictions.addAll(predictionList.predictions);
    }
    
    /**
     * Extending the append method in lists
     */
    public void append(List<Double> prediction) {
    	this.predictions.add(prediction);
    }
    
    /**
     * Receives a list of lists. Each element is the list of 
     * probabilities or confidences associated to each class in the 
     * ensemble, as described in the `class_names` attribute and ordered 
     * in the same sequence. Returns the probability obtained by adding 
     * these predictions into a single one by adding their probabilities 
     * and normalizing.
     */
    public List<Double> combineToDistribution(Boolean normalize) {
    	if (normalize == null) {
    		normalize = true;
    	}
    	
    	double total = 0.0;
    	Double[] output = new Double[predictions.get(0).size()];
    	for (int i=0; i<predictions.get(0).size(); i++) {
    		output[i] = 0.0;
    	}
    	
    	for (Object distribution: predictions) {
    		List<Double> dist = (List<Double>) distribution;
    		for (int i=0; i<dist.size(); i++) {
    			Double voteValue = (Double) dist.get(i);
    			output[i] += voteValue;
    			total += voteValue;
    		}
    	}
    	
    	if (!normalize) {
    		total = predictions.size();
    	}
    	
    	for (int i=0; i<output.length; i++) {
    		Double value = (Double) output[i];
    		output[i] = Utils.roundOff(value / total, PRECISION);
    	}
    	
    	return Arrays.asList(output);
    }
    
}