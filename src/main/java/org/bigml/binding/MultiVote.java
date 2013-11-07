package org.bigml.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;

public class MultiVote {

    private final static String[] COMBINATION_WEIGHTS = new String[] {"confidence", "distribution"};
    private final static String[][] WEIGHT_KEYS = new String[][] {{}, {"confidence"}, {"distribution", "count"}};
    private final static String[] WEIGHT_LABELS = new String[] {null, "confidence", "probability"};

    public final static int PLURALITY = 0;
    public final static int CONFIDENCE = 1;
    public final static int PROBABILITY = 2;

    public HashMap<Object, Object>[] predictions;


    /**
     * MultiVote: combiner class for ensembles voting predictions.
     * @constructor
     * @param {array|object} predictions Array of model's predictions
     */
    public MultiVote(HashMap<Object, Object>[] predictionsArr) {
        int i, len;
        if (predictionsArr == null) {
        	predictionsArr = (HashMap<Object, Object>[]) new HashMap[0];
        }
        predictions = predictionsArr;

        boolean allOrdered = true;
        for (i = 0, len = predictions.length; i < len; i++) {
            if(!predictions[i].containsKey("order")) {
                allOrdered = false;
                break;
            }
        }
        if (!allOrdered) {
            for (i = 0, len = predictions.length; i < len; i++) {
                predictions[i].put("order", i);
            }
        }
    }


    /**
     * Check if this is a regression model
     * @return {boolean} True if all the predictions are numbers.
     */
    private boolean is_regression() {
        int index, len;
        HashMap<Object, Object> prediction;
        for (index = 0, len = this.predictions.length; index < len; index++) {
            prediction = this.predictions[index];
            if (!(prediction.get("prediction") instanceof Number)) {
                return false;
            }
        }
        return true;
    };


    /**
     * Checks the presence of each of the keys in each of the predictions
     *
     * @param {array} predictions Array of prediction objects
     * @param {array} keys Array of key strings
     */
    private static boolean checkKeys(HashMap<Object, Object>[] predictions, String[] keys) {
        HashMap prediction;
        String key;
        int index, kindex, len;
        for (index = 0, len = predictions.length; index < len; index++) {
              prediction = predictions[index];
                for (kindex = 0; kindex < keys.length; kindex++) {
                  key = keys[kindex];
                    if (!prediction.containsKey(key)) {
                        throw new Error("Not enough data to use the selected prediction method.");
                    }
                }
            }
        return true;
    }


    /**
     * Wilson score interval computation of the distribution for the prediction
     *
     * @param {object} prediction Value of the prediction for which confidence is
     *        computed
     * @param {array} distribution Distribution-like structure of predictions and
     *        the associated weights (only for categoricals).
     *        (e.g. {'Iris-setosa': 10, 'Iris-versicolor': 5})
     * @param {integer} n Total number of instances in the distribution. If
     *         absent, the number is computed as the sum of weights in the
     *         provided distribution
     * @param {float} z Percentile of the standard normal distribution
     */
    private static double wsConfidence(Object prediction, HashMap<String, Double> distribution, Integer n, Double z) {
    	double norm, z2, n2, wsSqrt,
            p = distribution.get(prediction).doubleValue(), zDefault = 1.96d;
        if (z == null) {
            z = zDefault;
        }
        if (p < 0) {
            throw new Error("The distribution weight must be a positive value");
        }
        if (n != null && n < 1) {
            throw new Error("The total of instances in the distribution must be" +
                            " a positive integer");
        }
        norm = 0.0d;
        for (String key : distribution.keySet()) {
            norm += distribution.get(key).doubleValue();
        }
        if (norm == 0.0d) {
            throw new Error("Invalid distribution norm: " + distribution.toString());
        }
        if (norm != 1.0d) {
            p = p / norm;
        }
        if (n == null) {
            n = (int) norm;
        }
        z2 = z * z;
        n2 = n * n;
        wsSqrt = Math.sqrt((p * (1 - p) / n) + (z2 / (4 * n2)));
        return (p + (z2 / (2 * n)) - (z * wsSqrt)) / (1 + (z2 / n));
    }


    /**
     * Average for regression models' predictions
     *
     */
    private HashMap<Object, Object> avg() {
        int i, len, total = this.predictions.length;
        double result = 0.0d, confidence = 0.0d;
        HashMap<Object, Object> average = new HashMap<Object, Object>();

        for (i = 0, len = this.predictions.length; i < len; i++) {
          result += ((Number) this.predictions[i].get("prediction")).doubleValue();
          confidence += ((Number) this.predictions[i].get("confidence")).doubleValue();
        }
        average.put("prediction", new Double(result / total));
        average.put("confidence", new Double(confidence / total));
        return average;
    }


    /**
     * Returns the prediction combining votes using error to compute weight
     * @return {{'prediction': {string|number}, 'confidence': {number}}} The
     *         combined error is an average of the errors in the MultiVote
     *         predictions.
     */
    public HashMap<Object, Object> errorWeighted() {
    	this.checkKeys(this.predictions, new String[] {"confidence"});
        int index, len;
        HashMap<Object, Object> prediction = new HashMap<Object, Object>();
        Double combined_error = 0.0d, topRange = 10.0d,
            result = 0.0d, normalization_factor = this.normalizeError(topRange);

        if (normalization_factor == 0.0d) {
            prediction.put("prediction", Double.NaN);
            prediction.put("confidence", 0.0d);
        }
        for (index = 0, len = this.predictions.length; index < len; index++) {
            prediction = this.predictions[index];
            result += ((Number) prediction.get("prediction")).doubleValue() *
            		  ((Number) prediction.get("errorWeight")).doubleValue();
            combined_error += ((Number) prediction.get("confidence")).doubleValue() *
            				  ((Number) prediction.get("errorWeight")).doubleValue();
        }
        prediction.put("prediction", result / normalization_factor);
        prediction.put("confidence", combined_error / normalization_factor);
        return prediction;
    };


    /**
     * Normalizes error to a [0, top_range] range and builds probabilities
     * @param {number} The top range of error to which the original error is
     *        normalized.
     * @return {number} The normalization factor as the sum of the normalized
     *         error weights.
     */
    public Double normalizeError(Double topRange) {
      int  index, len;
      Double error, errorRange, delta, maxError = -1.0d,
          minError = Double.MAX_VALUE, normalizeFactor = 0.0d;
      HashMap<Object, Object> prediction;
      for (index = 0, len = this.predictions.length; index < len; index++) {
          prediction = this.predictions[index];
          if (!prediction.containsKey("confidence")) {
              throw new Error("Not enough data to use the selected prediction method.");
          }
          error = ((Number) prediction.get("confidence")).doubleValue();
          maxError = Math.max(error, maxError);
          minError = Math.min(error, minError);
      }
      errorRange = maxError - minError;
      normalizeFactor = 0.0d;
      if (errorRange > 0.0d) {
          /* Shifts and scales predictions errors to [0, top_range].
           * Then builds e^-[scaled error] and returns the normalization
           * factor to fit them between [0, 1]
           */
            for (index = 0, len = this.predictions.length; index < len; index++) {
                prediction = this.predictions[index];
                delta = (minError - ((Number) prediction.get("confidence")).doubleValue());
                this.predictions[index].put("errorWeight", Math.exp(delta / errorRange * topRange));
                normalizeFactor += (Double) this.predictions[index].get("errorWeight");
            }
        } else {
            for (index = 0, len = this.predictions.length; index < len; index++) {
                prediction = this.predictions[index];
                this.predictions[index].put("errorWeight", 1.0d);
            }
            normalizeFactor = new Double(this.predictions.length);
        }
        return normalizeFactor;
    };



   /**
	* Creates a new predictions array based on the training data probability
	*/
    public HashMap<Object, Object>[] probabilityWeight() {
    	int index, len, total, order, instances;

    	HashMap<Object, Object> prediction = new HashMap<Object, Object>();
    	HashMap<String, Object> distribution;
    	ArrayList predictionsList = new ArrayList();

    	for (index = 0, len = this.predictions.length; index < len; index++) {
    		prediction = this.predictions[index];
    		if (!prediction.containsKey("distribution") || !prediction.containsKey("count")) {
    			throw new Error("Probability weighting is not available because" +
    	                      " distribution information is missing.");
    	    }

    		total = prediction.get("count") instanceof Long ?
    					((Long) prediction.get("count")).intValue() :
    						(Integer) prediction.get("count");

    		if (total < 1) {
    	      throw new Error("Probability weighting is not available because" +
    	                      " distribution seems to have " + total +
    	                      " as number of instances in a node");
    	    }

    		order = (Integer) prediction.get("order");
    		distribution = (HashMap<String, Object>) prediction.get("distribution");

    		for (String key : distribution.keySet()) {
    			instances = (Integer) distribution.get(key);
    			HashMap<String, Object> predictionHash = new HashMap<String, Object>();
    			predictionHash.put("prediction", key);
    			predictionHash.put("probability",  (double)instances / total);
    			predictionHash.put("count", instances);
    			predictionHash.put("order", order);

    			predictionsList.add(predictionHash);
    		}
    	}
    	HashMap<Object, Object>[] predictions = (HashMap<Object, Object>[]) new HashMap[predictionsList.size()];
    	for (index = 0, len = predictions.length; index < len; index++) {
    		predictions[index] = (HashMap<Object, Object>) predictionsList.get(index);
    	}
        return predictions;
    };


    /**
     * Returns the prediction combining votes by using the given weight
     *
     * @param {string} weightLabel Type of combination method:
     *        'plurality':   plurality (1 vote per prediction)
     *        'confidence':  confidence weighted (confidence as a vote value)
     *        'probability': probability weighted (probability as a vote value)
     *
     * Will also return the combined confidence, as a weighted average of
     * the confidences of the votes.
     */
    public HashMap<Object, Object> combineCategorical(Object weightLabel, Boolean withConfidence) {
    	if (withConfidence == null) {
    		withConfidence = false;
    	}

    	int index, len;
    	double weight = 1.0;
    	Object category;
    	HashMap<Object, Object> prediction = new HashMap<Object, Object>();
    	HashMap<Object, Object> mode = new HashMap<Object, Object>();
    	ArrayList tuples = new ArrayList();

    	for (index = 0, len = this.predictions.length; index < len; index++) {
    		prediction = this.predictions[index];

    		if (weightLabel!=null && !weightLabel.equals("plurality")) {
    			if (ArrayUtils.indexOf(WEIGHT_LABELS, weightLabel) == -1) {
    		        throw new Error("Wrong weightLabel value.");
    		    }
    			if (prediction.get(weightLabel)==null) {
    		        throw new Error("Not enough data to use the selected prediction" +
    		                        " method. Try creating your model anew.");
    		    }
    		    weight = (Double) prediction.get(weightLabel);
    		}

    		category = prediction.get("prediction");

    		HashMap<String, Object> categoryHash = new HashMap<String, Object>();
    		if (mode.get(category)!=null) {
    		 	categoryHash.put("count", ((Double)((HashMap)mode.get(category)).get("count"))+weight);
    		 	categoryHash.put("order", ((HashMap)mode.get(category)).get("order"));
		    } else {
		    	categoryHash.put("count", weight);
		    	categoryHash.put("order", prediction.get("order"));
		    }

    		mode.put(category, categoryHash);
    	}

    	for (Object key : mode.keySet()) {
    		if (mode.get(key)!=null) {
    			Object[] tuple = new Object[]{key, mode.get(key)};
    			tuples.add(tuple);
    	    }
    	}


    	Collections.sort(tuples, new TupleComparator());
    	Object[] tuple = (Object[]) tuples.get(0);
    	Object predictionName = (Object) tuple[0];

    	if (this.predictions[0].get("confidence")!=null) {
    		return this.weightedConfidence(predictionName, weightLabel);
    	}

    	// If prediction had no confidence, compute it from distribution
    	Object[] distributionInfo = this.combineDistribution(weightLabel);
    	int count = (Integer) distributionInfo[1];
    	HashMap<String, Double>  distribution = (HashMap<String, Double>) distributionInfo[0];

    	double combinedConfidence = wsConfidence(predictionName, distribution, count, null);

    	HashMap<Object, Object> result = new HashMap<Object, Object>();
    	result.put("prediction", predictionName);
    	result.put("confidence", combinedConfidence);

    	return result;
    }


    /**
     * Compute the combined weighted confidence from a list of predictions
     *
     * @param {object} combinedPrediction Prediction object
     * @param {string} weightLabel Label of the value in the prediction object
     *        that will be used to weight confidence
     */
    public HashMap<Object, Object> weightedConfidence(Object combinedPrediction, Object weightLabel) {
    	int index, len;
    	Double finalConfidence = 0.0;
    	double weight = 1.0;
    	double totalWeight = 0.0;
    	HashMap<Object, Object> prediction = null;
    	ArrayList predictionsList = new ArrayList();

    	for (index = 0, len = this.predictions.length; index < len; index++) {
    		if (this.predictions[index].get("prediction").equals(combinedPrediction)) {
    			predictionsList.add(this.predictions[index]);
    		}
    	}
    	// Convert to array
    	HashMap<Object, Object>[] predictions = (HashMap<Object, Object>[]) new HashMap[predictionsList.size()];
    	for (index = 0, len = predictions.length; index < len; index++) {
    		predictions[index] = (HashMap<Object, Object>) predictionsList.get(index);
    	}

    	if (weightLabel != null) {
    		for (index = 0, len = this.predictions.length; index < len; index++) {
        		prediction = this.predictions[index];
        		if (prediction.get("confidence")==null || prediction.get(weightLabel)==null) {
    		        throw new Error("Not enough data to use the selected prediction" +
    		                        " method. Lacks ' + weightLabel + ' information");
    		      }
    		}
    	}

    	for (index = 0, len = predictions.length; index < len; index++) {
    	    prediction = predictions[index];

    	    if (weightLabel != null) {
    	    	weight = ((Number) prediction.get("confidence")).doubleValue();
    	    }
    	    finalConfidence += weight * ((Number) prediction.get("confidence")).doubleValue();
    	    totalWeight += weight;
    	 }

    	 if (totalWeight > 0) {
    	    finalConfidence = finalConfidence / totalWeight;
    	 } else {
    	    finalConfidence = null;
    	 }

    	 HashMap<Object, Object> result = new HashMap<Object, Object>();
     	 result.put("prediction", combinedPrediction);
     	 result.put("confidence", finalConfidence);

     	 return result;
    }


    /**
     * Builds a distribution based on the predictions of the MultiVote
	 *
	 * @param {string} weightLabel Label of the value in the prediction object
	 *        whose sum will be used as count in the distribution
	 */
    public Object[] combineDistribution(Object weightLabel) {
    	int index, len;
    	int total=0;
    	HashMap<Object, Object> prediction = new HashMap<Object, Object>();
    	HashMap<String, Double> distribution = new HashMap<String, Double>();
    	Object[] combinedDistribution = new Object[2];

    	for (index = 0, len = this.predictions.length; index < len; index++) {
    		prediction = this.predictions[index];

    		if (!prediction.containsKey(weightLabel)) {
      	      throw new Error("Not enough data to use the selected prediction" +
      	                      " method. Try creating your model anew.");
      	    }

    		String predictionName = (String) prediction.get("prediction");
    		if (!distribution.containsKey(prediction.get("prediction"))) {
    			distribution.put(predictionName, 0.0);
    		}

    		distribution.put(predictionName, distribution.get(predictionName)+(Double)prediction.get(weightLabel));
    		total += (Integer) prediction.get("count");
    	}

    	combinedDistribution[0] = distribution;
    	combinedDistribution[1] = total;
    	return combinedDistribution;
    }


    /**
     * Reduces a number of predictions voting for classification and
     * averaging predictions for regression.
     *
     * @param {0|1|2} method Code associated to the voting method (plurality,
     *        confidence weighted or probability weighted).
     * @return {{"prediction": prediction, "confidence": combinedConfidence}}
     */
    public HashMap<Object, Object> combine(Integer method, Boolean withConfidence) {
        if (method == null) {
            method = PLURALITY;
        }
        if (withConfidence == null) {
        	withConfidence = false;
        }

        // there must be at least one prediction to be combined
        if (this.predictions.length == 0) {
            throw new Error("No predictions to be combined.");
        }

        String[] keys = WEIGHT_KEYS[method];
        // and all predictions should have the weight-related keys
        if (keys.length > 0) {
            checkKeys(this.predictions, keys);
        }

        if (this.is_regression()) {
        	if (method == CONFIDENCE) {
                return this.errorWeighted();
            }
            return this.avg();
        }

        MultiVote multiVote = this;
        if (method == PROBABILITY) {
        	multiVote = new MultiVote(this.probabilityWeight());
        }

        return multiVote.combineCategorical(WEIGHT_LABELS[method], withConfidence);
    }

}


/**
 * Comparator
 */
class TupleComparator implements Comparator<Object[]> {

    public int compare(Object[] o1, Object[] o2) {
    	HashMap hash1 = (HashMap) o1[1];
    	HashMap hash2 = (HashMap) o2[1];
    	double weight1 = (Double) hash1.get("count");
    	double weight2 = (Double) hash2.get("count");
    	int order1 = (Integer) hash1.get("order");
    	int order2 = (Integer) hash2.get("order");
    	return weight1>weight2 ? -1 : (weight1<weight2 ? 1 : order1 < order2 ? -1 : 1);
    }
}

