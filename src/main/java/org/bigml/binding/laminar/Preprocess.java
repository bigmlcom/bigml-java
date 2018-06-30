package org.bigml.binding.laminar;

import java.util.ArrayList;
import java.util.List;

import org.bigml.binding.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Auxiliary functions for preprocessing
 *
 */
public class Preprocess {
	
	private static final String MEAN = "mean";
	private static final String STANDARD_DEVIATION = "stdev";
	private static final String ZERO = "zero_value";
	private static final String ONE = "one_value";
	
	
	private static ArrayList<Double> oneHot(
			ArrayList<Object> vector, JSONArray possibleValues) {

		ArrayList<Double> outvec = new ArrayList<Double>();
		for (int i=0; i<possibleValues.size(); i++) {
			outvec.add(0.0);
		}
		
		for (int i=0; i<vector.size(); i++) {
			String value = (String) vector.get(i);
			int index = possibleValues.indexOf(value);
			if (index != -1) {
				outvec.set(index, 1.0);
			}
		}
		
		return outvec;
	}
	
	
	private static ArrayList<Double> standardize(
			ArrayList<Double> vector, Double mean, Double stdev) {
		
		ArrayList<Double> newvec = new ArrayList<Double>();
		for (int i=0; i<vector.size(); i++) {
			if (vector.get(i) != null) {
				Double value = (Double) vector.get(i);
				
				Double standarized = value - mean;
				if (stdev > 0) {
					standarized = standarized / stdev;
				}
				newvec.add(standarized);
			} else {
				newvec.add(0.0);
			}
		}
		
		return newvec;
	}
	
	private static ArrayList<Double> binarize(
			ArrayList<Double> vector, Double zero, Double one) {
		
		for (int i=0; i<vector.size(); i++) {
			Double value = (Double) vector.get(i);
			if (zero == 0) {
				if (value == one) {
					vector.set(i, 1.0);  
				}
				if (value != one && value != 1.0) {
					vector.set(i, 0.0);  
				}
			} else {
				if (value != one) {
					vector.set(i, 0.0);  
				}
				if (value == one) {
					vector.set(i, 1.0);  
				}
			}
		}
		return vector;
	}
	
	private static ArrayList<Double> transform(
			ArrayList<Object> vector, JSONObject spec) {
		
		ArrayList<Double> output = new ArrayList<Double>();
		
		String type = (String) spec.get("type");
		
		// Check valid spec type
		if ( !(Constants.OPTYPE_NUMERIC.equals(type) || 
				Constants.OPTYPE_CATEGORICAL.equals(type)) ) {
			throw new IllegalArgumentException(
					String.format("%s is not a valid spec type!", type));
		}
		
		if (Constants.OPTYPE_NUMERIC.equals(type)) {
			ArrayList<Double> vectorD = new ArrayList<Double>();
			for (Object value: vector) {
				vectorD.add((Double) value);
			}
			
			// Check spec format
			if ( !spec.containsKey(STANDARD_DEVIATION) && 
				 !spec.containsKey(ZERO) ) {
					throw new IllegalArgumentException(
							String.format("%s is not a valid numeric spec!", spec));
			}
			
			if (spec.containsKey(STANDARD_DEVIATION)) {
				Double mean = ((Number) spec.get(MEAN)).doubleValue();
				Double stdev = ((Number) spec.get(STANDARD_DEVIATION)).doubleValue();
				output = standardize(vectorD, mean, stdev);
			} else {
				if (spec.containsKey(ZERO)) {
					Double low = ((Number) spec.get(ZERO)).doubleValue();
					Double high = ((Number) spec.get(ONE)).doubleValue();
					output = binarize(vectorD, low, high);
				}
			}
		}
		
		if (Constants.OPTYPE_CATEGORICAL.equals(type)) {
			JSONArray values = (JSONArray) spec.get("values");
			output = oneHot(vector, values);
		}
		
		return output;
	}
	
	
	private static List<Double> treePredict(
			JSONArray tree, List<Double> point) {
		
		JSONArray last = (JSONArray) tree.get(tree.size()-1);
		while (last != null && last.size() > 0) {
			int firstNode = ((Number) tree.get(0)).intValue();
			Double pointValue = (Double) point.get(firstNode);
			Double secondNode = ((Number) tree.get(1)).doubleValue();
			
			tree = (pointValue <= secondNode ? 
					(JSONArray) tree.get(2) : (JSONArray) tree.get(3));
			
			last = (JSONArray) tree.get(tree.size()-1);	
		}
		
		return (JSONArray) tree.get(0);
	}
	
	
	private static ArrayList<List<Double>> getEmbedding(
			ArrayList<List<Double>> input, JSONArray model) {
		
		List<Double> preds = new ArrayList<Double>();
		for (Object tree: model) {
			ArrayList<List<Double>> treePreds = new ArrayList<List<Double>>();	
			for (List<Double> row: input) {
				treePreds.add(treePredict((JSONArray) tree, row));
			}
			
			List<Double> firstPred = treePreds.get(0);
			
			if (preds.size() == 0) {
				preds.addAll(treePreds.get(0));
			} else {
				for (int i=0; i<preds.size(); i++) {
					Double current = ((Number) preds.get(i)).doubleValue();
					Double newValue = ((Number) firstPred.get(i)).doubleValue();
					preds.set(i, current + newValue);
				}
			}
		}
		
		if (preds != null && preds.size() > 1) {
			double norm = 0.0;
			for (Double d: preds) {
				norm += d;
			}
			
			for (int i=0; i<preds.size(); i++) {
				Double current = ((Number) preds.get(i)).doubleValue();
				preds.set(i, current / norm);
			}
		} else {
			for (int i=0; i<preds.size(); i++) {
				Double current = ((Number) preds.get(i)).doubleValue();
				preds.set(i, current / model.size());
			}
		}
		
		ArrayList<List<Double>> output = new ArrayList<List<Double>>();
		output.add(preds);
		return output;
	}
	
	
	public static ArrayList<List<Double>> treeTransform(
			ArrayList<List<Double>> input, JSONArray trees) {
		
		ArrayList<List<Double>> outdata = new ArrayList<List<Double>>();
		for (Object tree: trees) {
			JSONArray featureRange = (JSONArray) ((JSONArray) tree).get(0);
			JSONArray model = (JSONArray) ((JSONArray) tree).get(1);
			
			int sidx = ((Number) featureRange.get(0)).intValue();
			int eidx = ((Number) featureRange.get(1)).intValue();
			
			ArrayList<List<Double>> rowData = new ArrayList<List<Double>>();
			
			for (int i=0; i<input.size(); i++) {
				List<Double> row = input.get(i);
				rowData.add(row.subList(sidx, eidx));
			}
			
			ArrayList<List<Double>> outarray = getEmbedding(rowData, model) ;
			
			if (outdata.size() > 0) {
				outdata.get(0).addAll(outarray.get(0));
			} else {
				outdata = outarray;
			}
		}
		outdata.get(0).addAll(input.get(0));
		return outdata;
	}
	
	
	public static ArrayList<List<Double>> preprocess(
			List<Object> columns, JSONArray specs) {
		
		ArrayList<List<Double>> outdata = new ArrayList<List<Double>>();
		ArrayList<Double> output = new ArrayList<Double>();
		
		for (Object specObj: specs) {
			JSONObject spec = (JSONObject) specObj;
			
			int index = ((Number) spec.get("index")).intValue();
			
			Object value = null;
			if (columns.get(index) !=  null) {
				if (columns.get(index) instanceof Number) {
					value = ((Number) columns.get(index)).doubleValue();
				} else {
					value = columns.get(index);
				}
			}
			
			ArrayList<Object> column = new ArrayList<Object>();		
			column.add(value);
			
			output.addAll(transform(column, spec));
		}
		outdata.add(output);
		return outdata;
	}
	
}
