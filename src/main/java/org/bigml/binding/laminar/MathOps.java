package org.bigml.binding.laminar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Activation functions and helpers
 *
 */
public class MathOps {
	
	// This can be any x where np.exp(x) + 1 == np.exp(x)  Going up to 512
	// isn't strictly necessary, but hey, why not?
	private static final double HUGE_DOUBLE_EXP = 512;
	private static final float HUGE_FLOAT_EXP = 64;
    private static final float LARGE_FLOAT_EXP = 8;
	private static float LEAKY_RELU_ALPHA = 0.1f;
    private static final double SELU_ALPHA = 1.6732632423543772848170429916717;
    private static final double SELU_SCALE = 1.0507009873554804934193349852946;
	
	private static ArrayList<List<Double>> operation(
			String operator, ArrayList<List<Double>> mat, JSONArray vec) {
		
		ArrayList<List<Double>> out = new ArrayList<List<Double>>();
		for (int i=0; i<mat.size(); i++) {
			List<Double> row = (List<Double>) mat.get(i);
			
			List<Double> newRow = new ArrayList<Double>();
			for (int k=0; k<row.size(); k++) {
				Double val = ((Number) vec.get(k)).doubleValue();
				
				if ("+".equals(operator)) {
					newRow.add(row.get(k) + val);
				}
				if ("-".equals(operator)) {
					newRow.add(row.get(k) - val);
				}
				if ("*".equals(operator)) {
					newRow.add(row.get(k) * val);
				}
				if ("/".equals(operator)) {
					newRow.add(row.get(k) / val);
				}
			}
			out.add(newRow);
		}
		
		return out;
	}
	
	private static ArrayList<List<Double>> plus(
			ArrayList<List<Double>> mat, JSONArray vec) {
		return operation("+", mat, vec);
	}
	
	private static ArrayList<List<Double>> minus(
			ArrayList<List<Double>> mat, JSONArray vec) {
		return operation("-", mat, vec);
	}
	
	private static ArrayList<List<Double>> times(
			ArrayList<List<Double>> mat, JSONArray vec) {
		return operation("*", mat, vec);
	}
	
	private static ArrayList<List<Double>> divide(
			ArrayList<List<Double>> mat, JSONArray vec) {
		return operation("/", mat, vec);
	}
	
	public static ArrayList<List<Double>> dot(
			ArrayList<List<Double>> mat1, JSONArray mat2) {
		
		ArrayList<List<Double>> outMat = new ArrayList<List<Double>>();
		
		for (int i=0; i<mat1.size(); i++) {
			List<Double> row1 = (List<Double>) mat1.get(i);
			List<Double> newRow = new ArrayList<Double>();
			for (int j=0; j<mat2.size(); j++) {
				List<Double> row2 = (List<Double>) mat2.get(j);
				double sum = 0.0;
				for (int k=0; k<row1.size(); k++) {
					Double val = ((Number) row2.get(k)).doubleValue();
					sum += ((Number) row1.get(k)).doubleValue() * val;
				}
				
				newRow.add(sum);
			}
			outMat.add(newRow);
		}
		
		return outMat;
	}
	
	private static ArrayList<List<Double>> batchNorm(
			ArrayList<List<Double>> mat, JSONArray mean, 
			JSONArray stdev, JSONArray shift, JSONArray scale) {

		ArrayList<List<Double>> normVals = divide(minus(mat, mean), stdev);
		return plus(times(normVals, scale), shift);
	}
	
	
	public static ArrayList<List<Double>> destandardize(
			ArrayList<List<Double>> vec, Double mean, Double stdev) {
		
		ArrayList<List<Double>> out = new ArrayList<List<Double>>();
		for (int i=0; i<vec.size(); i++) {
			List<Double> row = (List<Double>) vec.get(i);
			
			List<Double> newRow = new ArrayList<Double>();
			for (int k=0; k<row.size(); k++) {
				newRow.add(row.get(k) * stdev + mean);
			}
			out.add(newRow);
		}
		
		return out;
	}
	
	
	private static ArrayList<List<Double>> toWidth(
			ArrayList<List<Double>> mat, int width) {
		
		int ntiles = 1;
		if (width > mat.get(0).size()) {
			ntiles = (int) Math.ceil( width / mat.get(0).size() );
		}
		
		ArrayList<List<Double>> output = new ArrayList<List<Double>>();
		for (List<Double> row: mat) {
			List<Double> newRow = new ArrayList<Double>();
			for (int i=0; i<width; i+=ntiles) {
				newRow.addAll(row);
			}
			output.add(newRow);
		}
		
		return output;
	}
	
	
	private static ArrayList<List<Double>> addResiduals(
			ArrayList<List<Double>> residuals, 
			ArrayList<List<Double>> identities) {
		
		ArrayList<List<Double>> output = new ArrayList<List<Double>>();
		
		ArrayList<List<Double>> toAdd = 
				toWidth(identities, residuals.get(0).size());
		
		for (int i=0; i<residuals.size(); i++) {
			List<Double> residualRow = (List<Double>) residuals.get(i);
			List<Double> toAddRow = (List<Double>) toAdd.get(i);
			
			List<Double> newRow = new ArrayList<Double>();
			for (int j=0; j<residualRow.size(); j++) {
				newRow.add(residualRow.get(j) + toAddRow.get(j));
			}
			output.add(newRow);
		}
		
		return output;
	}
	
	
	public static ArrayList<List<Double>> propagate(
			ArrayList<List<Double>> input, JSONArray layers) {
		
		ArrayList<List<Double>> identities = input;
		ArrayList<List<Double>> lastX = input;

		ArrayList residualsList = new ArrayList();
		for (Object layerObj: layers) {
			JSONObject layer = (JSONObject) layerObj;
			residualsList.add((Boolean) layer.get("residuals"));
		}
		
		boolean firstIdentities = false;
		if (residualsList.contains(true)) {
			firstIdentities = 
				!residualsList.subList(2, residualsList.size()).contains(true);
		}

		int i = 0;
		for (Object layerObj: layers) {
			JSONObject layer = (JSONObject) layerObj;
			JSONArray weights = (JSONArray) layer.get("weights");
			JSONArray mean = (JSONArray) layer.get("mean");
			JSONArray stdev = (JSONArray) layer.get("stdev");
			JSONArray scale = (JSONArray) layer.get("scale");
			JSONArray offset = (JSONArray) layer.get("offset");
			
			Boolean residuals = (Boolean) layer.get("residuals");
			String afn = (String) layer.get("activation_function");
			
			ArrayList<List<Double>> nextIn = dot(lastX, weights);
			
			if (mean != null && stdev != null) {
				nextIn = batchNorm(nextIn, mean, stdev, offset, scale);
			} else {
				nextIn = plus(nextIn, offset);
			}
			
			if (residuals != null && residuals) {
				nextIn = addResiduals(nextIn, identities);
				lastX = broadcast(afn, nextIn);
				identities = lastX;
			} else {
				lastX = broadcast(afn, nextIn);

				if (firstIdentities && i==0) {
					identities = lastX;
				}
			}
			i++;
		}
		
		return lastX;
	}
	
	
	private static ArrayList<List<Double>> broadcast(
			String afn, ArrayList<List<Double>> xs) {
		
		ArrayList<List<Double>> result = new ArrayList<List<Double>>();
		if (xs.size() == 0) {
			return result;
		}
		
		if ("identity".equals(afn) || "linear".equals(afn)) {
			return xs;
		}
		if ("softmax".equals(afn)) {
			return softmax(xs);
		}
		
		for (List<Double> row: xs) {
			List<Double> newRow = new ArrayList<Double>();
			for (Double d: row) {
				if ("tanh".equals(afn)) {
					newRow.add(Math.tanh(d));
				}
				if ("sigmoid".equals(afn)) {
					newRow.add(sigmoid(d));
				}
				if ("softplus".equals(afn)) {
					newRow.add(softplus(d));
				}
				if ("relu".equals(afn)) {
					newRow.add(relu(d));
				}
				if ("relu6".equals(afn)) {
					newRow.add(relu6(d));
				}
				if ("leaky_relu".equals(afn)) {
					newRow.add(leakyReLU(d));
				}
				if ("swish".equals(afn)) {
					newRow.add(swish(d));
				}
				if ("mish".equals(afn)) {
					newRow.add(mish(d));
				}
				if ("selu".equals(afn)) {
					newRow.add(selu(d));
				}
			}
			result.add(newRow);
		}

		return result;
	}
	
	private static ArrayList<List<Double>> softmax(
			ArrayList<List<Double>> xs) {
		
		double max = 0.0;
		for (List<Double> row: xs) {
			double maxRow = Collections.max(row);
			max = maxRow > max ? maxRow : max;
		}
		
		ArrayList<List<Double>> exps = new ArrayList<List<Double>>();
		for (List<Double> row: xs) {
			List<Double> newRow = new ArrayList<Double>();
			for (Double d: row) {
				newRow.add(Math.exp(d - max));
			}
			exps.add(newRow);
		}
		
		double sumex = 0.0;
		for (List<Double> exp: exps) {
			for (Double d: exp) {
				sumex += d;
			}
		}
		
		ArrayList<List<Double>> result = new ArrayList<List<Double>>();
		for (List<Double> exp: exps) {
			List<Double> newRow = new ArrayList<Double>();
			for (Double d: exp) {
				newRow.add(d / sumex);
			}
			result.add(newRow);
		}
		
		return result;
	}
	
	private static Double sigmoid(Double d) {
		if (d > 0) {
			if (d < HUGE_DOUBLE_EXP) {
				double exVal = Math.exp(d);
				return exVal / (exVal + 1);
			} else {
				return 1.0;
			}
		} else {
			if (-d < HUGE_DOUBLE_EXP) {
				return 1 / (1 + Math.exp(-d));
			} else {
				return 0.0;
			}
		}
	}
	
	private static Double softplus(Double d) {
		return d < HUGE_FLOAT_EXP ? Math.log((Math.exp(d) + 1)) : d;
	}
	
	private static Double relu(Double d) {
		return Math.max(0, d);
	}
	
	private static Double relu6(Double d) {
		return Math.min(6, Math.max(0, d));
	}
	
	private static Double swish(Double d) {
		if (d > 0) {
            if (d < HUGE_FLOAT_EXP) {
                float x = (float)Math.exp(d);
                return (d * x) / (x + 1);
            }
        }
        else {
            return (d / (1 + Math.exp(-d)));
        }
		return 0.0;
	}
	
	private static Double leakyReLU(Double d) {
		return d <= 0 ? d * LEAKY_RELU_ALPHA : d;
	}
	
	
	private static Double mish(Double d) {
		float x = 1;

        // tanh and softplus
        if (d < LARGE_FLOAT_EXP) {
            x = (float)(Math.tanh(Math.log(Math.exp(d) + 1)));
        }
        return d * x;
	}
	
	private static Double selu(Double d) {
		/*
		if (d <= 0) {
			return SELU_ALPHA * (Math.exp(d) -1) - SELU_ALPHA;
		} else {
			return SELU_SCALE * d;
		}*/
        
        if (d <= 0) {
        	d = SELU_ALPHA * (Math.exp(d) - 1) - SELU_ALPHA;
        }
        return d * SELU_SCALE;
	}
	
	public static ArrayList<List<Double>> sumAndNormalize(
			ArrayList<ArrayList<List<Double>>> inputs, boolean isRegression) {
		
		ArrayList<List<Double>> first = (ArrayList<List<Double>>) inputs.get(0);
		Double[] ysums = new Double[first.get(0).size()];
		for (int j=0; j<first.get(0).size(); j++) {
			ysums[j] = 0.0;
		}
		
		for (Object inputObj: inputs) {
			ArrayList<List<Double>> input = (ArrayList<List<Double>>) inputObj;
			for (int k=0; k<input.get(0).size(); k++) {
				ysums[k] += input.get(0).get(k);
			}
		}
		
		ArrayList<List<Double>> outDist = new ArrayList<List<Double>>();
		List<Double> dist = new ArrayList<Double>();
		
		double sum = 0.0;
		for (int j=0; j<ysums.length; j++) {
			sum += ysums[j];
		}
		
		for (int i=0; i<ysums.length; i++) {
			if (isRegression) {
				dist.add(ysums[i] / inputs.size());
			} else {
				dist.add(ysums[i] / sum);
			}
		}
		outDist.add(dist);
		return outDist;
	}
	
}