package org.bigml.binding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A multiple vote prediction
 *
 * Uses a number of predictions to generate a combined prediction.
 */
public class MultiVote implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
    /**
     * Logging
     */
    static Logger LOGGER = LoggerFactory.getLogger(MultiVote.class.getName());
    
    public final static String[] PREDICTION_HEADERS = new String[] { 
    		"prediction", "confidence", "order", "distribution", "count" };
    
    private final static String[] COMBINATION_WEIGHTS = new String[] {
            null , "confidence", "probability", null, "weight" };
    
    
    private final static String[][] WEIGHT_KEYS = new String[][] { {},
            { "confidence" }, { "distribution", "count" }, {}, { "weight" } };
            
    private final static String[] WEIGHT_LABELS = new String[] { "plurality",
            "confidence", "probability", "threshold" };

    final static int BINS_LIMIT = 32;
    
    final static String BOOSTING_CLASS = "class";
    
    public HashMap<Object, Object>[] predictions;
    public boolean boosting = false;
    public JSONArray boostingOffsets;
    
    
    /**
     * MultiVote: combiner class for ensembles voting predictions.
     */
    public MultiVote() {
        this(null, null);
    }
        
    /**
     * MultiVote: combiner class for ensembles voting predictions.
     *
     * @param predictionsArr {array|object} predictions Array of model's predictions
     * @param boostingOffsets {array|object}
     */
    public MultiVote(HashMap<Object, Object>[] predictionsArr, JSONArray boostingOffsets) {
        int i, len;
        if (predictionsArr == null) {
            predictionsArr = new HashMap[0];
        }
        predictions = predictionsArr;
        
        boosting = boostingOffsets != null && !boostingOffsets.isEmpty();
        this.boostingOffsets = boostingOffsets;
        
        boolean allOrdered = true;
        for (i = 0, len = predictions.length; i < len; i++) {
            if (!predictions[i].containsKey("order")) {
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
    
    public HashMap<Object, Object>[] getPredictions() {
        return predictions;
    }
    
    /**
     * Check if this is a regression model
     *
     * @return {boolean} True if all the predictions are numbers.
     */
    private boolean isRegression() {
    	int index, len;
    	HashMap<Object, Object> prediction;
    	
    	if (boosting) {
    		for (index = 0, len = this.predictions.length; index < len; index++) {
                prediction = this.predictions[index];
                if (prediction.get("class") == null) {
                    return true;
                }
    		}
    		return false;
    	}
    	
        for (index = 0, len = this.predictions.length; index < len; index++) {
            prediction = this.predictions[index];
            if (!(prediction.get("prediction") instanceof Number)) {
                return false;
            }
        }
        return true;
    };
    
    /**
     * Return the next order to be assigned to a prediction
     *
     * Predictions in MultiVote are ordered in arrival sequence when
     * added using the constructor or the append and extend methods.
     * This order is used to break even cases in combination
     * methods for classifications.
     *
     * @return the next order to be assigned to a prediction
     */
    private int nextOrder() {
        if( predictions != null && predictions.length > 0 ) {
            return ((Number) predictions[predictions.length - 1].get("order")).intValue() + 1;
        }

        return 0;
    }
    
    
    /**
     * Adds a new prediction into a list of predictions
     *
     * prediction_info should contain at least:
     *      - prediction: whose value is the predicted category or value
     *
     * for instance:
     *      {'prediction': 'Iris-virginica'}
     *
     * it may also contain the keys:
     *      - confidence: whose value is the confidence/error of the prediction
     *      - distribution: a list of [category/value, instances] pairs
     *                      describing the distribution at the prediction node
     *      - count: the total number of instances of the training set in the
     *                  node
     *
     * @param predictionInfo the prediction to be appended
     * @return the this instance
     */
    public MultiVote append(HashMap<Object, Object> predictionInfo) {

        if( predictionInfo == null || predictionInfo.isEmpty() ||
                !predictionInfo.containsKey("prediction") ) {
            throw new IllegalArgumentException("Failed to add the prediction.\\n" +
                    "The minimal key for the prediction is 'prediction'" +
                    ":\n{'prediction': 'Iris-virginica'");
        }

        int order = nextOrder();
        predictionInfo.put("order", order);
        HashMap<Object, Object>[] temp = predictions.clone();
        predictions = new HashMap[predictions.length + 1];
        System.arraycopy(temp, 0, predictions, 0, temp.length);
        predictions[order] = predictionInfo;

        return this;
    }
    
    /**
     * Adds a new prediction into a list of predictions
     *
     * predictionHeaders should contain the labels for the predictionRow
     *  values in the same order.
     *
     * predictionHeaders should contain at least the following string
     *      - 'prediction': whose associated value in predictionRow
     *                      is the predicted category or value
     *
     * for instance:
     *      predictionRow = ['Iris-virginica']
     *      predictionHeaders = ['prediction']
     *
     * it may also contain the following headers and values:
     *      - 'confidence': whose associated value in prediction_row
     *                      is the confidence/error of the prediction
     *      - 'distribution': a list of [category/value, instances] pairs
     *                      describing the distribution at the prediction node
     *      - 'count': the total number of instances of the training set in the
     *                      node
     *
     * @param predictionRow the list of predicted values and extra info
     * @param predictionHeaders the name of each value in the predictionRow
     * @return the this instance
     */
    public MultiVote appendRow(List<Object> predictionRow,
                               List<String> predictionHeaders) {

        if( predictionHeaders == null ) {
            predictionHeaders = Arrays.asList(PREDICTION_HEADERS);
        }

        if( predictionRow.size() != predictionHeaders.size() ) {
            throw new IllegalArgumentException("WARNING: failed to add the prediction.\\n" +
                    "The row must have label 'prediction' at least. And the number" +
                    " of headers must much with the number of elements in the row.");
        }

        List<Object> mutablePredictionRow = new ArrayList<Object>(predictionRow);
        List<String> mutablePredictionHeaders = new ArrayList<String>(predictionHeaders);

        int index = -1;
        int order = nextOrder();
        try {
            index = mutablePredictionHeaders.indexOf("order");
            mutablePredictionRow.set(index, order);
        } catch (Exception ex) {
            mutablePredictionHeaders.add("order");
            mutablePredictionRow.add(order);
        }

        HashMap<Object, Object> predictionInfo = new HashMap<Object, Object>();
        for (int i = 0; i < mutablePredictionHeaders.size(); i++) {
            predictionInfo.put(mutablePredictionHeaders.get(i),
                    mutablePredictionRow.get(i));
        }

        HashMap<Object, Object>[] temp = predictions.clone();
        predictions = new HashMap[predictions.length + 1];
        System.arraycopy(temp, 0, predictions, 0, temp.length);
        predictions[order] = predictionInfo;

        return this;
    }
    
    /**
     * Given a multi vote instance (a list of predictions), extends the list
     * with another list of predictions and adds the order information.
     *
     * For instance, predictions_info could be:
     *
     *  [{'prediction': 'Iris-virginica', 'confidence': 0.3},
     *      {'prediction': 'Iris-versicolor', 'confidence': 0.8}]
     *
     *  where the expected prediction keys are: prediction (compulsory),
     *  confidence, distribution and count.
     *
     * @param votes	a MultiVote object
     */
    public void extend(MultiVote votes) {
        if( votes.predictions != null && votes.predictions.length > 0  ) {
            int order = nextOrder();

            List<HashMap<Object,Object>> predictionsList =
                    new ArrayList<HashMap<Object, Object>>(Arrays.asList(predictions));

            for (HashMap<Object, Object> prediction : votes.predictions) {
                prediction.put("order", (order + 1));
                predictionsList.add(prediction);
            }

            predictions = (HashMap<Object, Object>[]) predictionsList.toArray( new HashMap[predictionsList.size()] );
        }
    }
    
    /**
     * Given a list of predictions, extends the list with another list of
     *  predictions and adds the order information. For instance,
     *  predictionsInfo could be:
     *
     *      [{'prediction': 'Iris-virginica', 'confidence': 0.3},
     *       {'prediction': 'Iris-versicolor', 'confidence': 0.8}]
     *
     * where the expected prediction keys are: prediction (compulsory),
     * confidence, distribution and count.
     *
     * @param predictionsInfo the list of predictions we want to append
     * @return the this instance
     */
    public MultiVote extend(List<HashMap<Object, Object>> predictionsInfo) {

        if( predictionsInfo == null || predictionsInfo.isEmpty() ) {
            throw new IllegalArgumentException("WARNING: failed to add the predictions.\\n" +
                    "No predictions informed.");
        }

        int order = nextOrder();

        for (int i = 0; i < predictionsInfo.size(); i++) {
            HashMap<Object, Object> prediction = predictionsInfo.get(i);
            prediction.put("order", order + i);
            append(prediction);
        }
        return this;
    }
    
    /**
     * Given a list of predictions, extends the list with another list of
     *  predictions and adds the order information. For instance,
     *  predictionsInfo could be:
     *
     *      [{'prediction': 'Iris-virginica', 'confidence': 0.3},
     *       {'prediction': 'Iris-versicolor', 'confidence': 0.8}]
     *
     * where the expected prediction keys are: prediction (compulsory),
     * confidence, distribution and count.
     *
     * @param predictionsRows the list of predictions (in list format) we want to append
     * @param predictionsHeader	the list of names of predictions
     * @return the this instance
     */
    public MultiVote extendRows(List<List<Object>> predictionsRows,
                                List<String> predictionsHeader) {

        if( predictionsHeader == null ) {
            predictionsHeader = Arrays.asList(PREDICTION_HEADERS);
        }

        int order = nextOrder();
        int index = predictionsHeader.indexOf("order");
        if( index < 0 ) {
            index = predictionsHeader.size();
            predictionsHeader.add("order");
        }


        for( int iPrediction = 0; iPrediction < predictionsRows.size(); iPrediction++ ) {
            List<Object> predictionRow = predictionsRows.get(iPrediction);

            if( index == predictionRow.size() ) {
                predictionRow.add(order + 1);
            } else {
                predictionRow.set(index, order);
            }

            appendRow(predictionRow, predictionsHeader);
        }

        return this;
    }
    
    
    /**
     * Singles out the votes for a chosen category and returns a prediction
     * for this category iff the number of votes reaches at least the given
     * threshold.
     *
     * @param threshold the number of the minimum positive predictions needed for
     *                    a final positive prediction.
     * @param category the positive category
     * @return MultiVote instance
     */
    protected MultiVote singleOutCategory(Integer threshold, String category) {
        if( threshold == null || category == null || category.length() == 0 ) {
            throw new IllegalArgumentException("No category and threshold information was" +
                    " found. Add threshold and category info." +
                    " E.g. {\"threshold\": 6, \"category\":" +
                    " \"Iris-virginica\"}.");
        }

        if( threshold > predictions.length ) {
            throw new IllegalArgumentException(String.format(
                    "You cannot set a threshold value larger than " +
                    "%s. The ensemble has not enough models to use" +
                    " this threshold value.", predictions.length)
            );
        }

        if( threshold < 1 ) {
            throw new IllegalArgumentException("The threshold must be a positive value");
        }

        List categoryPredictions = new ArrayList();
        List restOfPredictions = new ArrayList();

        for (HashMap<Object, Object> prediction : predictions) {
            if( category.equals(prediction.get("prediction")) ) {
                categoryPredictions.add(prediction);
            } else {
                restOfPredictions.add(prediction);
            }
        }

        if( categoryPredictions.size() >= threshold ) {
            return new MultiVote((HashMap<Object, Object>[]) categoryPredictions.toArray(
                    new HashMap[categoryPredictions.size()]), null);
        } else {
            return new MultiVote((HashMap<Object, Object>[]) restOfPredictions.toArray(
                    new HashMap[categoryPredictions.size()]), null);
        }
    }
    
    
    /**
     * Checks the presence of each of the keys in each of the predictions
     *
     * @param predictions {array} predictions Array of prediction objects
     * @param keys {array} keys Array of key strings
     */
    private static boolean checkKeys(HashMap<Object, Object>[] predictions,
            String[] keys) {
        HashMap prediction;
        String key;
        int index, kindex, len;
        for (index = 0, len = predictions.length; index < len; index++) {
            prediction = predictions[index];
            for (kindex = 0; kindex < keys.length; kindex++) {
                key = keys[kindex];
                if (!prediction.containsKey(key)) {
                    throw new Error(
                            "Not enough data to use the selected prediction method.");
                }
            }
        }
        return true;
    }
    
    
    /**
     * Normalizes error to a [0, top_range] range and builds probabilities
     *
     * @param topRange {number} The top range of error to which the original error is
     *        normalized.
     * @return {number} The normalization factor as the sum of the normalized
     *         error weights.
     */
    public Double normalizeError(Double topRange) {
        int index, len;
        Double error, errorRange, delta, maxError = -1.0d, minError = Double.MAX_VALUE, normalizeFactor = 0.0d;
        HashMap<Object, Object> prediction;
        for (index = 0, len = this.predictions.length; index < len; index++) {
            prediction = this.predictions[index];
            if (!prediction.containsKey("confidence")) {
                throw new Error(
                        "Not enough data to use the selected prediction method.");
            }
            error = ((Number) prediction.get("confidence")).doubleValue();
            maxError = Math.max(error, maxError);
            minError = Math.min(error, minError);
        }
        errorRange = maxError - minError;
        normalizeFactor = 0.0d;
        if (errorRange > 0.0d) {
            /*
             * Shifts and scales predictions errors to [0, top_range]. Then
             * builds e^-[scaled error] and returns the normalization factor to
             * fit them between [0, 1]
             */
            for (index = 0, len = this.predictions.length; index < len; index++) {
                prediction = this.predictions[index];
                delta = (minError - ((Number) prediction.get("confidence"))
                        .doubleValue());
                this.predictions[index].put("errorWeight",
                        Math.exp(delta / errorRange * topRange));
                normalizeFactor += (Double) this.predictions[index]
                        .get("errorWeight");
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
     * Wilson score interval computation of the distribution for the prediction
     *
     * @param prediction {object} prediction Value of the prediction for which confidence
     *        is computed
     * @param distribution {array} distribution Distribution-like structure of predictions
     *        and the associated weights (only for categoricals). (e.g.
     *        {'Iris-setosa': 10, 'Iris-versicolor': 5})
     * @param n {integer} n Total number of instances in the distribution. If
     *        absent, the number is computed as the sum of weights in the
     *        provided distribution
     * @param z {float} z Percentile of the standard normal distribution
     * 
     * @return the Wilson score interval
     */
    protected static double wsConfidence(Object prediction,
            HashMap<String, Double> distribution, Integer n, Double z) {
    	
    	double norm, z2, n2, wsSqrt, p = distribution.get(prediction)
                .doubleValue(), zDefault = 1.96d;
        if (z == null) {
            z = zDefault;
        }
        if (p < 0) {
            throw new Error("The distribution weight must be a positive value");
        }
        if (n != null && n < 1) {
            throw new Error(
                    "The total of instances in the distribution must be"
                            + " a positive integer");
        }
        norm = 0.0d;
        for (String key : distribution.keySet()) {
            norm += distribution.get(key).doubleValue();
        }
        if (norm == 0.0d) {
            throw new Error("Invalid distribution norm: "
                    + distribution.toString());
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
        
        return Utils.roundOff((p + (z2 / (2 * n)) - (z * wsSqrt)) / (1 + (z2 / n)), Constants.PRECISION);
    }
    
    
    /**
     * Average for regression models' predictions
     *
     */
    private HashMap<Object, Object> avg() {

        int i, len, total = this.predictions.length;
        double result = 0.0d, confidence = 0.0d, medianResult = 0.0d;
        HashMap<Object, Object> average = new HashMap<Object, Object>();
        long instances = 0;

        for (i = 0, len = this.predictions.length; i < len; i++) {
            result += ((Number) this.predictions[i].get("prediction"))
                    .doubleValue();
            
            if (this.predictions[i].containsKey("median")) {
            	medianResult += ((Number) this.predictions[i].get("median"))
                		.doubleValue();
            }
            
            confidence += ((Number) this.predictions[i].get("confidence"))
                    .doubleValue();

            instances += ((Number) this.predictions[i].get("count"))
                    .longValue();
        }

        if( total > 0 ) {
            average.put("prediction", result / total);
            average.put("confidence", confidence / total);
            average.put("median", medianResult / total);
        } else {
            average.put("prediction", Double.NaN);
            average.put("confidence", 0.0d);
            average.put("median", Double.NaN);
        }

        average.putAll(getGroupedDistribution(this));
        average.put("count", instances);
        return average;
    }
    
    /**
     * Returns the prediction combining votes using error to compute weight
     *
     * @return {{'prediction': {string|number}, 'confidence': {number}}} The
     *         combined error is an average of the errors in the MultiVote
     *         predictions.
     */
    public HashMap<Object, Object> errorWeighted() {
        this.checkKeys(this.predictions, new String[] { "confidence" });
        int index, len;
        HashMap<Object, Object> newPrediction = new HashMap<Object, Object>();
        Double combinedError = 0.0d, topRange = 10.0d, result = 0.0d, medianResult = 0.0d, normalization_factor = this
                .normalizeError(topRange);
        Long instances = 0L;

        if (normalization_factor == 0.0d) {
            newPrediction.put("prediction", Double.NaN);
            newPrediction.put("confidence", 0.0d);
            return newPrediction;
        }

        for (index = 0, len = this.predictions.length; index < len; index++) {
            HashMap<Object, Object> prediction = this.predictions[index];

            result += ((Number) prediction.get("prediction")).doubleValue()
                    * ((Number) prediction.get("errorWeight")).doubleValue();
            
            if (prediction.get("median") != null) {
	            medianResult += ((Number) prediction.get("median")).doubleValue()
	                    * ((Number) prediction.get("errorWeight")).doubleValue();
            }
            
            instances += ((Number) prediction.get("count")).longValue();

            combinedError += ((Number) prediction.get("confidence"))
                    .doubleValue()
                    * ((Number) prediction.get("errorWeight")).doubleValue();
         }

        newPrediction.put("prediction", result / normalization_factor);
        newPrediction.put("confidence", combinedError / normalization_factor);
        newPrediction.put("count", instances);
        if (medianResult > 0.0) {
        	newPrediction.put("median", medianResult / normalization_factor);
        }

        newPrediction.putAll(getGroupedDistribution(this));

        return newPrediction;
    };
    
    
    /**
     * Average for regression models' predictions
     *
     */
    private Double weightedSum(HashMap<Object, Object>[] predictions, String key) {
    	Map<Object, Object> prediction = new HashMap<Object, Object>();
    	double weightedSum = 0;
    	
    	int index, len;
    	for (index = 0, len = predictions.length; index < len; index++) {
            prediction = predictions[index];
            Double pred = (Double) prediction.get("prediction");
            Double weight = (Double) prediction.get(key);
            
            weightedSum += pred * weight;
    	}
    	
    	return weightedSum;
    }
    
    
    /**
     * Returns the softmax values from a distribution given as a 
     * dictionary like:
     * 		{"category": {"probability": probability, "order": order}}
     */
    private HashMap<Object, Object> softmax(HashMap<Object, Object> predictions) {
    	double total = 0;
    	
    	HashMap<Object, Object> normalized = new HashMap<Object, Object>();
    	for (Map.Entry<Object, Object> entry : predictions.entrySet()) {
            String key = (String) entry.getKey();
            HashMap<Object, Object> catInfo = (HashMap<Object, Object>) entry.getValue();
            
            Double probability = Math.exp((Double) catInfo.get("probability"));
            
            HashMap<Object, Object> pred = new HashMap<Object, Object>();
            pred.put("probability", probability);
        	pred.put("order", (Integer) catInfo.get("order"));
        	normalized.put(key, pred);
            
        	total += probability;
    	}
    	
    	if (total != 0) {
    		for (Map.Entry<Object, Object> entry : normalized.entrySet()) {
                String key = (String) entry.getKey();
                HashMap<Object, Object> catInfo = (HashMap<Object, Object>) entry.getValue();
                catInfo.put("probability", ((Double) catInfo.get("probability")) / total);
    		}
    		return normalized;
    	}
    	
    	return new HashMap<Object, Object>();
    }
    
    
    /**
     * Combines the predictions for a boosted classification ensemble
     * Applies the regression boosting combiner, but per class. Tie breaks
     * use the order of the categories in the ensemble summary to decide.
     */
    private HashMap<Object, Object> classifictionBoostingCombiner(Map options) {
    	HashMap<Object, Object> prediction = new HashMap<Object, Object>();
        
    	int index, len;
    	Map<String, Object> groupedPredictions = new HashMap<String, Object>();
        for (index = 0, len = this.predictions.length; index < len; index++) {
            prediction = this.predictions[index];
            if (prediction.get(BOOSTING_CLASS) != null) {
            	String objectiveClass = (String) prediction.get(BOOSTING_CLASS);
            	if (!groupedPredictions.containsKey(objectiveClass)) {
            		List<Map<Object, Object>> classList = new ArrayList<Map<Object, Object>>();
            		groupedPredictions.put(objectiveClass, classList);
            	}
            	((List<Map<Object, Object>>) groupedPredictions.get(objectiveClass)).add(prediction);
            }
        }
        
        List<String> categories = new ArrayList<String>();
        for (Object cats: (JSONArray) options.get("categories")) {
        	JSONArray cat = (JSONArray) cats;
        	categories.add((String) cat.get(0));
        }

        HashMap<Object, Object> predictions = new HashMap<Object, Object>();
        for (Map.Entry<String, Object> entry : groupedPredictions.entrySet()) {
            String key = entry.getKey();
            ArrayList value = (ArrayList) entry.getValue();
            
            Double boostingOffset = null;
            for (Object bOffset: (JSONArray) boostingOffsets) {
            	JSONArray offset = (JSONArray) bOffset;
            	if (key.equals((String) offset.get(0))) {
            		boostingOffset = (Double) offset.get(1);
            		break;
            	}
            }
            
            HashMap<Object, Object>[] preds = new HashMap[value.size()];
            for (index = 0, len = preds.length; index < len; index++) {
            	preds[index] = (HashMap<Object, Object>) value.get(index);
            }
            
            HashMap<Object, Object> pred = new HashMap<Object, Object>();
            pred.put("probability", 
        			 	weightedSum(preds, "weight") + boostingOffset);
        	pred.put("order", categories.indexOf(key));
            predictions.put(key, pred);
        }
        
        predictions = softmax(predictions);
        
        String predictionName = (String) predictions.keySet().toArray()[0];
        HashMap<Object, Object> predictionInfo = (HashMap<Object, Object>) predictions.get(predictionName);
        
        for (Map.Entry<Object, Object> entry : predictions.entrySet()) {
            String key = (String) entry.getKey();
            HashMap<Object, Object> predInfo = (HashMap<Object, Object>) entry.getValue();
            
            Double predProbability = (Double) predInfo.get("probability");
            Double predictionProbability = (Double) predictionInfo.get("probability");
            
            if (predProbability > predictionProbability) {
            	predictionName = key;
            	predictionInfo = predInfo;
            } else {
            	if (predProbability == predictionProbability) {
            		if ((Integer) predInfo.get("order") <= (Integer) predictionInfo.get("order")) {
            			predictionName = key;
            			predictionInfo = predInfo;
            		}
            	}
            }
        }
        prediction = new HashMap<Object, Object>();
        prediction.put("prediction", predictionName);
        prediction.put("probability", Utils.roundOff(
        		(Double) predictionInfo.get("probability"), Constants.PRECISION));
        
    	return prediction;
    }
    
    
    /**
     * Creates a new predictions array based on the training data probability
     * 
     * @return the predictions based on the training data probability
     */
    public HashMap<Object, Object>[] probabilityWeight() {
        int index, len, total, order;

        Map<Object, Object> prediction = new HashMap<Object, Object>();
        List<Map<Object, Object>> predictionsList = new ArrayList<Map<Object, Object>>();

        for (index = 0, len = this.predictions.length; index < len; index++) {
            prediction = this.predictions[index];
            if (!prediction.containsKey("distribution")
                    || !prediction.containsKey("count")) {
                throw new Error(
                        "Probability weighting is not available because"
                                + " distribution information is missing.");
            }

            total = prediction.get("count") instanceof Long ? ((Long) prediction
                    .get("count")).intValue() : (Integer) prediction
                    .get("count");

            if (total < 1) {
                throw new Error(
                        "Probability weighting is not available because"
                                + " distribution seems to have " + total
                                + " as number of instances in a node");
            }

            order = (Integer) prediction.get("order");
            
            HashMap distribution = (HashMap) prediction.get("distribution");
            for (Object key : distribution.keySet()) {
            	Map<Object, Object> newPred = new HashMap<Object, Object>(); 
                newPred.put("prediction", key);
                newPred.put("probability", ((Integer) distribution.get(key) / (double) total));
                newPred.put("count", distribution.get(key));
                newPred.put("order", order);
	                
	            predictionsList.add(newPred);
    		}
            
        }
        HashMap<Object, Object>[] predictions = new HashMap[predictionsList.size()];
        for (index = 0, len = predictions.length; index < len; index++) {
            predictions[index] = (HashMap<Object, Object>) predictionsList
                    .get(index);
        }
        return predictions;
    };
    
    
    /**
     * Builds a distribution based on the predictions of the MultiVote
     *
     * @param weightLabel {string} weightLabel Label of the value in the prediction object
     *        whose sum will be used as count in the distribution
     *        
     * @return the distribution based on the predictions of the MultiVote
     */
    public Object[] combineDistribution(String weightLabel) {
        int index, len;
        int total = 0;
        HashMap<Object, Object> prediction = new HashMap<Object, Object>();
        HashMap<String, Double> distribution = new HashMap<String, Double>();
        Object[] combinedDistribution = new Object[2];

        if( weightLabel == null || weightLabel.trim().length() == 0 ) {
            weightLabel = WEIGHT_LABELS[PredictionMethod.PROBABILITY.getCode()];
        }


        for (index = 0, len = this.predictions.length; index < len; index++) {
            prediction = this.predictions[index];

            if (!prediction.containsKey(weightLabel)) {
                throw new Error(
                        "Not enough data to use the selected prediction"
                                + " method. Try creating your model anew.");
            }

            String predictionName = (String) prediction.get("prediction");
            if (!distribution.containsKey(predictionName)) {
                distribution.put(predictionName, 0.0);
            }

            distribution.put(predictionName, distribution.get(predictionName)
                    + (Double) prediction.get(weightLabel));
            total += (Integer) prediction.get("count");
        }

        combinedDistribution[0] = distribution;
        combinedDistribution[1] = total;
        return combinedDistribution;
    }
    
    
    /**
     * Returns the prediction combining votes by using the given weight
     *
     * @param weightLabel {string} weightLabel Type of combination method: 'plurality':
     *        plurality (1 vote per prediction) 'confidence': confidence
     *        weighted (confidence as a vote value) 'probability': probability
     *        weighted (probability as a vote value)
     *
     *        Will also return the combined confidence, as a weighted average of
     *        the confidences of the votes.
     * 
     * @return the prediction combining votes by using the given weight
     */
    public HashMap<Object, Object> combineCategorical(String weightLabel) {
        
        int index, len;
        double weight = 1.0;
        Object category;
        HashMap<Object, Object> prediction = new HashMap<Object, Object>();
        HashMap<Object, Object> mode = new HashMap<Object, Object>();
        ArrayList tuples = new ArrayList();
        
        for (index = 0, len = this.predictions.length; index < len; index++) {
            prediction = this.predictions[index];

            if ( weightLabel != null ) {
                if (Arrays.asList(WEIGHT_LABELS).indexOf(weightLabel) == -1) {
                    throw new Error("Wrong weightLabel value.");
                }
                if ( !prediction.containsKey(weightLabel) ) {
                	throw new Error(
                            "Not enough data to use the selected prediction"
                                    + " method. Try creating your model anew.");
                } else {
                    weight = (Double) prediction.get(weightLabel);
                }
            }

            category = prediction.get("prediction");

            HashMap<String, Object> categoryHash = new HashMap<String, Object>();
            if (mode.get(category) != null) {
                categoryHash.put("count",
                        ((Double) ((HashMap) mode.get(category)).get("count"))
                                + weight);
                categoryHash.put("order",
                        ((HashMap) mode.get(category)).get("order"));
            } else {
                categoryHash.put("count", weight);
                categoryHash.put("order", prediction.get("order"));
            }

            mode.put(category, categoryHash);
        }
        
        for (Object key : mode.keySet()) {
            if (mode.get(key) != null) {
                Object[] tuple = new Object[] { key, mode.get(key) };
                tuples.add(tuple);
            }
        }

        Collections.sort(tuples, new TupleComparator());
        
        Object[] tuple = (Object[]) tuples.get(0);
        Object predictionName = (Object) tuple[0];

        HashMap<Object, Object> output = new HashMap<Object, Object>();
        output.put("prediction", predictionName);
        
        if (this.predictions[0].get("confidence") != null) {
            return this.weightedConfidence(predictionName, weightLabel);
        }

        // If prediction had no confidence, compute it from distribution
        Object[] distributionInfo = this.combineDistribution(weightLabel);
        int count = (Integer) distributionInfo[1];
        HashMap<String, Double> distribution = (HashMap<String, Double>) distributionInfo[0];

        double combinedConfidence = wsConfidence(predictionName, distribution,
                count, null);
        output.put("probability", combinedConfidence);
        return output;
    }

    /**
     * Compute the combined weighted confidence from a list of predictions
     *
     * @param combinedPrediction {object} combinedPrediction Prediction object
     * @param weightLabel {string} weightLabel Label of the value in the prediction object
     *        that will be used to weight confidence
     *        
     * @return the combined weighted confidence
     */
    public HashMap<Object, Object> weightedConfidence(
            Object combinedPrediction, Object weightLabel) {
        int index, len;
        Double finalConfidence = 0.0;
        double weight = 1.0;
        double totalWeight = 0.0;
        HashMap<Object, Object> prediction = null;
        ArrayList predictionsList = new ArrayList();

        for (index = 0, len = this.predictions.length; index < len; index++) {
            if (this.predictions[index].get("prediction").equals(
                    combinedPrediction)) {
                predictionsList.add(this.predictions[index]);
            }
        }
        // Convert to array
        HashMap<Object, Object>[] predictions = (HashMap<Object, Object>[]) new HashMap[predictionsList
                .size()];
        for (index = 0, len = predictions.length; index < len; index++) {
            predictions[index] = (HashMap<Object, Object>) predictionsList
                    .get(index);
        }

        if (weightLabel != null) {
            for (index = 0, len = this.predictions.length; index < len; index++) {
                prediction = this.predictions[index];
                if (prediction.get("confidence") == null
                        || prediction.get(weightLabel) == null) {
                    throw new Error(
                            "Not enough data to use the selected prediction"
                                    + " method. Lacks ' + weightLabel + ' information");
                }
            }
        }

        for (index = 0, len = predictions.length; index < len; index++) {
            prediction = predictions[index];

            if (weightLabel != null) {
                weight = ((Number) prediction.get("confidence")).doubleValue();
            }
            finalConfidence += weight
                    * ((Number) prediction.get("confidence")).doubleValue();
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
     * Returns a distribution formed by grouping the distributions of each predicted node.
     * 
     * @param multiVoteInstance
     * 			a multiVote instance
     * 
     * @return the distribution formed by grouping the distributions of each predicted node
     */
    protected static Map<String, Object> getGroupedDistribution(MultiVote multiVoteInstance) {
        Map<Object, Number> joinedDist = new HashMap<Object, Number>();
        String distributionUnit = "counts";

        for (HashMap<Object, Object> prediction : multiVoteInstance.getPredictions()) {
            HashMap<Object, Number> predictionDist = null;
            Object distribution = prediction.get("distribution");
            
            if( distribution instanceof Map ) {
                predictionDist = (HashMap<Object, Number>) distribution;
            } else {
                predictionDist = (HashMap<Object, Number>) Utils.convertDistributionArrayToMap((JSONArray) distribution);
            }

            joinedDist = Utils.mergeDistributions(joinedDist, predictionDist);

            if( "counts".equals(distributionUnit) && joinedDist.size() > BINS_LIMIT) {
                distributionUnit = "bins";
            }

            joinedDist = Utils.mergeBins(joinedDist, BINS_LIMIT);
        }

        Map<String, Object> distributionInfo = new HashMap<String, Object>();
        distributionInfo.put("distribution", Utils.convertDistributionMapToSortedArray(joinedDist));
        distributionInfo.put("distributionUnit", distributionUnit);

        return distributionInfo;
    }
    
    
    /**
     * Reduces a number of predictions voting for classification and averaging
     * predictions for regression using the PLURALITY method and without confidence
     *
     * @return {{"prediction": prediction}}
     */
    public HashMap<Object, Object> combine() {
        return combine((PredictionMethod) null, null);
    }
    
    
    /**
     * Reduces a number of predictions voting for classification and
     * averaging predictions for regression.
     * 
     * @param method
     * 			PLURALITY|CONFIDENCE|PROBABILITY|THRESHOLD prediction method
     * @param options
     * 			a map with options for specific prediction method
     * 
     * @return a map of predictions
     */
    public HashMap<Object, Object> combine(PredictionMethod method, Map options) {
    	
    	if (method == null) {
            method = PredictionMethod.PLURALITY;
        }
    	
    	// there must be at least one prediction to be combined
        if (this.predictions.length == 0) {
            throw new Error("No predictions to be combined.");
        }
        
        // and all predictions should have the weight-related keys
        String[] keys = WEIGHT_KEYS[method.getCode()];
        if (keys.length > 0) {
            checkKeys(this.predictions, keys);
        }
        
        if (this.boosting) {
        	for (HashMap<Object, Object> prediction : predictions) {
        		if( !prediction.containsKey("boosting") ) {
                    prediction.put("boosting", 0.0);
                }
        	}
        	
        	if (this.isRegression()) {
        		// sum all gradients weighted by their "weight" plus the
        		// boosting offset
        		HashMap<Object, Object> prediction = new HashMap<Object, Object>();
            	prediction.put("prediction", 
            			weightedSum(predictions, "weight") + 
            			(Double) this.boostingOffsets.get(0));
            	return prediction;
        	} else {
        		return classifictionBoostingCombiner(options);
        	}
        } else {
        	if (this.isRegression()) {
        		for (HashMap<Object, Object> prediction : predictions) {
                    if( !prediction.containsKey("confidence") ) {
                        prediction.put("confidence", 0.0);
                    }
                }
        		
        		if (method == PredictionMethod.CONFIDENCE) {
                    return this.errorWeighted();
                }
                return this.avg();
        	}
        }
        
        MultiVote multiVote = null;
        if (method == PredictionMethod.THRESHOLD) {
            Integer threshold = (Integer) options.get("threshold");
            String category = (String) options.get("category");

            multiVote = singleOutCategory(threshold, category);
        } else if (method == PredictionMethod.PROBABILITY) {
        	multiVote = new MultiVote(this.probabilityWeight(), null);
        } else {
            multiVote = this;
        }
        
        return multiVote.combineCategorical(COMBINATION_WEIGHTS[method.getCode()]);
    }
    
    
    /**
     * Comparator
     */
    class TupleComparator implements Comparator<Object[]> {

	    @Override
	    public int compare(Object[] o1, Object[] o2) {
	        HashMap hash1 = (HashMap) o1[1];
	        HashMap hash2 = (HashMap) o2[1];
	        double weight1 = (Double) hash1.get("count");
	        double weight2 = (Double) hash2.get("count");
	        int order1 = (Integer) hash1.get("order");
	        int order2 = (Integer) hash2.get("order");
	        return weight1 > weight2 ? -1 : (weight1 < weight2 ? 1
	                : order1 < order2 ? -1 : 1);
	    }
	}
}
