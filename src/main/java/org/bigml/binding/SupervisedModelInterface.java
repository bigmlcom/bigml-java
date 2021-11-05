package org.bigml.binding;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Interface for supervised local models.
 */
public interface SupervisedModelInterface {
	
	/**
	 * Returns the resourceId
	 * 
	 * @return the resource id
	 */
	String getResourceId();
	
	/**
	 * Returns the class names
	 * 
	 * @return the class names
	 */
	List<String> getClassNames();
	
    /**
	 * For classification models, predicts a probability for each possible output
	 * class, based on input values. The input fields must be a dictionary keyed by
	 * field name or field ID.
	 * 
	 * @param inputData       	Input data to be predicted
	 * @param missingStrategy	LAST_PREDICTION|PROPORTIONAL missing strategy for
     *                     		missing fields
	 * 
	 * @return list of probabilities for each possible output classes
	 * @throws Exception a generic exception
	 */
	JSONArray predictProbability(
			JSONObject inputData, MissingStrategy missingStrategy) throws Exception;
}


