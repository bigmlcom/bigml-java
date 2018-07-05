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
	 */
	String getResourceId();
	
	/**
	 * Returns the class names
	 */
	List<String> getClassNames();
	
    /**
     * For classification models, Predicts a probability for
     * each possible output class, based on input values.  The input
     * fields must be a dictionary keyed by field name or field ID.
     */
	JSONArray predictProbability(
			JSONObject inputData, MissingStrategy missingStrategy) throws Exception;
}


