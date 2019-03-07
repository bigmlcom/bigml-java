/**
 * A local Ensemble object.
 *
 * This module defines an Ensemble to make predictions locally using its
 * associated models.
 *
 * This module can not only save you a few credits, but also enormously
 * reduce the latency for each prediction and let you use your models
 * offline.
 *
 * Example usage (assuming that you have previously set up the 
 * BIGML_USERNAME and BIGML_API_KEY environment variables and that you 
 * own the ensemble/id below):
 *
 *
 * import org.bigml.binding.LocalEnsemble;
 * 
 * // API client
 * BigMLClient api = new BigMLClient();
 * 
 * JSONObject ensemble = api.
 * 		getEnsemble("ensemble/5b39e6b9c7736e583400214c");
 * LocalEnsemble localEnsemble = new LocalEnsemble(ensemble)
 *
 * JSONObject predictors = JSONValue.parse("
 * 		{\"petal length\": 3, \"petal width\": 0.5,
 * 		 \"sepal length\": 1, \"sepal width\": 0.5}");
 *
 * localEnsemble.predict(predictors)
 * 
 */
package org.bigml.binding;

import java.io.IOException;
import java.util.*;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local predictive Ensemble.
 * 
 * Uses a number of BigML remote models to build an ensemble local version that
 * can be used to generate prediction.
 * 
 */
public class LocalEnsemble extends ModelFields implements SupervisedModelInterface {

	private static final long serialVersionUID = 1L;

	static String ENSEMBLE_RE = "^ensemble/[a-f,0-9]{24}$";

	private static final int BOOSTING = 1;

	private static final String[] OPERATING_POINT_KINDS = { "probability",
			"confidence", "votes" };

	/**
	 * Logging
	 */
	static Logger logger = LoggerFactory
			.getLogger(LocalEnsemble.class.getName());

	private String ensembleId;
	private String objectiveField = null;
	private JSONObject boosting = null;
	private JSONArray models;
	private JSONObject model = null;
	private List<JSONArray> modelsSplit = new ArrayList<JSONArray>();
	private String[] modelsIds;
	private JSONArray distributions;
	private JSONArray distribution;
	private JSONObject importance;
	private MultiModel multiModel;
	private Boolean regression = false;
	private JSONArray boostingOffsets;
	private List<String> classNames = new ArrayList<String>();
	private Map<String, String> fieldNames = new HashMap<String, String>();

	public LocalEnsemble(JSONObject ensemble) throws Exception {
		this(ensemble, null);
	}

	public LocalEnsemble(JSONObject ensemble, Integer maxModels)
			throws Exception {

		super((JSONObject) Utils.getJSONObject(ensemble, "ensemble.fields",
				new JSONObject()));

		// checks whether the information needed for local predictions
		// is in the first argument
		if (!checkModelFields(ensemble)) {
			// if the fields used by the ensemble are not available,
			// use only ID to retrieve it again
			ensembleId = (String) ensemble.get("resource");
			boolean validId = ensembleId.matches(ENSEMBLE_RE);
			if (!validId) {
				throw new Exception(
						ensembleId + " is not a valid resource ID.");
			}
		}

		if (!(ensemble.containsKey("resource")
				&& ensemble.get("resource") != null)) {
			BigMLClient client = new BigMLClient(null, null,
					BigMLClient.STORAGE);
			ensemble = client.getEnsemble(ensembleId);

			if ((String) ensemble.get("resource") == null) {
				throw new Exception(
						ensembleId + " is not a valid resource ID.");
			}
		}

		if (ensemble.containsKey("object")
				&& ensemble.get("object") instanceof JSONObject) {
			ensemble = (JSONObject) ensemble.get("object");
		}

		ensembleId = (String) ensemble.get("resource");

		if (ensemble.containsKey("ensemble")
				&& ensemble.get("ensemble") instanceof JSONObject) {

			JSONObject status = (JSONObject) Utils.getJSONObject(ensemble,
					"status");

			if (status != null && status.containsKey("code")
					&& AbstractResource.FINISHED == ((Number) status
							.get("code")).intValue()) {

				int ensebleType = ((Long) ensemble.get("type")).intValue();
				if (ensebleType == BOOSTING) {
					boosting = (JSONObject) Utils.getJSONObject(ensemble,
							"boosting");
				}

				JSONArray modelsJson = (JSONArray) ensemble.get("models");
				distributions = (JSONArray) ensemble.get("distributions");
				importance = (JSONObject) ensemble.get("importance");

				int mn = modelsJson.size();
				modelsIds = new String[mn];
				for (int i = 0; i < mn; i++) {
					modelsIds[i] = (String) modelsJson.get(i);
				}

				JSONObject fields = (JSONObject) Utils.getJSONObject(ensemble,
						"ensemble.fields", new JSONObject());

				objectiveField = (String) Utils.getJSONObject(ensemble,
						"objective_field");

				// initialize ModelFields
				super.initialize((JSONObject) fields, objectiveField, null,
						null, true, true, true);

			} else {
				throw new Exception("The lensemble isn't finished yet");
			}
		} else {
			throw new Exception(
					String.format("Cannot create the Ensemble instance. "
							+ "Could not find the 'ensemble' key in "
							+ "the resource:\n\n%s", ensemble));
		}

		init(ensemble, maxModels);
	}

	/**
	 * Constructor with a list of model references and the number of max models
	 * to use
	 *
	 * @param modelsIds
	 *            the model/id of each model to be used in the ensemble
	 * @param maxModels
	 *            the maximum number of models we will use in the ensemble null
	 *            if we do not want a maxModels value
	 */
	public LocalEnsemble(List modelsIds, Integer maxModels) throws Exception {
		this.modelsIds = (String[]) modelsIds
				.toArray(new String[modelsIds.size()]);

		init(null, maxModels);
	}

	protected void init(JSONObject ensemble, Integer maxModels)
			throws Exception {
		BigMLClient bigmlClient = new BigMLClient();
		models = new JSONArray();
		for (String id : modelsIds) {
			models.add(bigmlClient.getModel(id));
		}
		model = (JSONObject) models.get(0);
		int numberOfModels = models.size();

		maxModels = maxModels != null ? maxModels : numberOfModels;
		int[] items = Utils.getRange(0, numberOfModels, maxModels);
		for (int item : items) {
			if (item + maxModels <= numberOfModels) {
				JSONArray arrayOfModels = new JSONArray();
				arrayOfModels.addAll(models.subList(item, item + maxModels));
				modelsSplit.add(arrayOfModels);
			}
		}

		if (distributions != null) {
			distributions = new JSONArray();
			for (Object model : models) {
				JSONObject treDist = (JSONObject) Utils.getJSONObject(
						(JSONObject) model, "model.tree.distribution");

				if (treDist != null) {
					JSONObject categories = new JSONObject();
					categories.put("categories", treDist);
					JSONObject info = new JSONObject();
					info.put("training", categories);
					distributions.add(info);
				} else {
					distributions = new JSONArray();
					break;
				}
			}

			if (distributions.size() == 0) {
				for (Object model : models) {
					distributions.add((JSONObject) Utils.getJSONObject(
							(JSONObject) model, "object.model.distribution"));
				}
			}
		}

		if (boosting == null) {
			addModelsAttrs(model, maxModels);
		}

		if (fields == null) {
			calculateFields();

			objectiveField = (String) Utils.getJSONObject(model,
					"object.objective_field");
		}

		if (fields != null) {
			JSONObject summary = (JSONObject) Utils.getJSONObject(fields,
					objectiveField + ".summary");

			if (summary != null) {
				if (summary.get("bins") != null) {
					distribution = (JSONArray) summary.get("bins");
				} else if (summary.get("counts") != null) {
					distribution = (JSONArray) summary.get("counts");
				} else if (summary.get("categories") != null) {
					distribution = (JSONArray) summary.get("categories");
				}
			}
		}

		String optype = (String) Utils.getJSONObject(fields,
				objectiveField + ".optype");
		regression = "numeric".equals(optype);
		if (boosting != null) {
			if (regression) {
				Double boostingOffset = ((Number) ensemble.get("initial_offset")).doubleValue();
				boostingOffsets = new JSONArray();
				boostingOffsets.add(boostingOffset);
			} else {
				boostingOffsets = (JSONArray) ensemble.get("initial_offsets");
			}
		}
		
		if (!regression) {
			JSONObject summary = (JSONObject) Utils.getJSONObject(
					(JSONObject) fields.get(objectiveField), "summary");
			if (summary != null) {
				JSONArray categories = (JSONArray) Utils.getJSONObject(summary,
						"categories", new JSONArray());

				for (Object cat : categories) {
					classNames.add((String) ((JSONArray) cat).get(0));
				}
				Collections.sort(classNames);
			}
		}

		if (modelsSplit.size() == 1) {
			multiModel = new MultiModel(models, fields, classNames);
		}
	}
	
	/**
	 * Returns the resourceId
	 */
	public String getResourceId() {
		return ensembleId;
	}
	
	/**
	 * Returns the class names
	 */
	public List<String> getClassNames() {
		return classNames;
	}

	/**
	 * Calculates the full list of fields used by this ensemble. It's obtained
	 * from the union of fields in all models of the ensemble.
	 */
	protected void calculateFields() {
		fields = new JSONObject();
		fieldNames.clear();

		for (int i = 0; i < this.modelsIds.length; i++) {
			JSONObject model = (JSONObject) this.models.get(i);
			JSONObject fields = (JSONObject) Utils.getJSONObject(model,
					"object.model.fields");
			for (Object k : fields.keySet()) {
				if (null != fields.get(k)) {
					String fieldName = (String) ((JSONObject) fields.get(k))
							.get("name");
					this.fields.put(k, fields.get(k));
					this.fieldNames.put((String) k, fieldName);
				}
			}
		}
	}

	/**
	 * Adds the boosting and fields info when the ensemble is built from a list
	 * of models. They can be either Model objects or the model dictionary info
	 * structure.
	 */
	private void addModelsAttrs(JSONObject model, Integer maxModels) {

		boolean boostedEnsemble = (Boolean) Utils.getJSONObject(model,
				"object.boosted_ensemble", false);
		if (boostedEnsemble) {
			this.boosting = (JSONObject) Utils.getJSONObject(model,
					"object.boosting", null);
		}

		if (this.boosting != null) {
			throw new IllegalArgumentException(
					"Failed to build the local ensemble. Boosted"
							+ "ensembles cannot be built from a list of "
							+ "boosting models.");
		}

		if (fields == null) {
			allModelsFields(maxModels);
			objectiveFieldId = (String) Utils.getJSONObject(model,
					"object.objective_field", null);
		}

	}

	/**
	 * Retrieves the fields used as predictors in all the ensemble models
	 */
	private void allModelsFields(Integer maxModels) {
		try {
			for (Object split : modelsSplit.get(0)) {
				LocalPredictiveModel localModel = new LocalPredictiveModel(
						(JSONObject) split);
				fields.putAll(localModel.getFields());
			}
		} catch (Exception e) {}
	}

	/**
	 * Computes the predicted distributions and combines them to give the final
	 * predicted distribution. Depending on the method parameter probability,
	 * votes or the confidence are used to weight the models.
	 * 
	 */
	private List<Double> combineDistributions(JSONObject inputData,
			MissingStrategy missingStrategy, PredictionMethod method)
			throws Exception {

		if (method == null) {
			method = PredictionMethod.PROBABILITY;
		}
		
		MultiVoteList votes = null;

		if (modelsSplit != null && modelsSplit.size() > 1) {
			// If there's more than one chunk of models, they must be
			// sequentially used to generate the votes for the prediction
			votes = new MultiVoteList(null);

			for (Object split : modelsSplit) {
				JSONArray models = (JSONArray) split;
				MultiModel multiModel = new MultiModel(models, fields,
						classNames);
				MultiVoteList modelVotes = multiModel.generateVotesDistribution(
						inputData, missingStrategy, method);
				votes.extend(modelVotes);
			}
		} else {
			// When only one group of models is found you use the
			// corresponding multimodel to predict
			votes = multiModel.generateVotesDistribution(inputData,
					missingStrategy, method);
		}

		return votes.combineToDistribution(false);
	}

	/**
	 * Computes field importance based on the field importance information of
	 * the individual models in the ensemble.
	 */
	public List<JSONArray> getFieldImportanceData() {

		Map<String, Double> fieldImportance = new HashMap<String, Double>();

		if (importance != null) {
			fieldImportance = importance;
		} else {
			boolean useDistribution = false;
			List<JSONArray> importances = new ArrayList<JSONArray>();

			if (distributions != null && distributions.size() > 0) {
				useDistribution = true;
				for (Object item : distributions) {
					JSONObject itemObj = (JSONObject) item;
					useDistribution &= itemObj.containsKey("importance");
					if (!useDistribution)
						break;
					else {
						importances.add((JSONArray) itemObj.get("importance"));
					}
				}
			}

			if (useDistribution) {
				for (JSONArray importance : importances) {
					JSONArray importanceInfo = (JSONArray) importance;

					for (Object fieldInfo : importanceInfo) {
						JSONArray fieldInfoArr = (JSONArray) fieldInfo;
						String fieldId = (String) fieldInfoArr.get(0);
						if (!fieldImportance.containsKey(fieldId)) {
							fieldImportance.put(fieldId, 0.0);
							String fieldName = (String) ((JSONObject) fields
									.get(fieldId)).get("name");

							JSONObject fieldNameObj = new JSONObject();
							fieldNameObj.put("name", fieldName);
						}

						fieldImportance.put(fieldId,
								fieldImportance.get(fieldId)
										+ ((Number) fieldInfoArr.get(1))
												.doubleValue());
					}
				}
			} else {
				for (Object model : models) {
					JSONObject modelObj = (JSONObject) model;
					JSONArray fieldImportanceInfo = (JSONArray) Utils
							.getJSONObject(modelObj, "object.model.importance");
					;
					for (Object fieldInfo : fieldImportanceInfo) {
						JSONArray fieldInfoArr = (JSONArray) fieldInfo;
						String fieldId = (String) fieldInfoArr.get(0);

						if (!fieldImportance.containsKey(fieldId)) {
							fieldImportance.put(fieldId, 0.0);

							String fieldName = (String) ((JSONObject) fields
									.get(fieldId)).get("name");

							JSONObject fieldNameObj = new JSONObject();
							fieldNameObj.put("name", fieldName);
						}

						fieldImportance.put(fieldId,
								fieldImportance.get(fieldId)
										+ ((Number) fieldInfoArr.get(1))
												.doubleValue());
					}
				}
			}

			for (String fieldName : fieldImportance.keySet()) {
				fieldImportance.put(fieldName,
						fieldImportance.get(fieldName) / models.size());
			}

		}

		List<JSONArray> fieldImportanceOrdered = new ArrayList<JSONArray>();
		for (String fieldName : fieldImportance.keySet()) {
			JSONArray fieldInfo = new JSONArray();
			fieldInfo.add(fieldName);
			fieldInfo.add(fieldImportance.get(fieldName));
			fieldImportanceOrdered.add(fieldInfo);
		}

		Collections.sort(fieldImportanceOrdered, new Comparator<JSONArray>() {
			@Override
			public int compare(JSONArray jsonArray, JSONArray jsonArray2) {
				return (((Number) jsonArray.get(1))
						.doubleValue() > ((Number) jsonArray2.get(1))
								.doubleValue() ? -1 : 1);
			}
		});

		return fieldImportanceOrdered;
	}

	/**
	 * Returns the required data distribution by adding the distributions in the
	 * models
	 */
	public JSONArray getDataDistribution(String distributionType) {
		if (distributionType == null) {
			distributionType = "training";
		}

		JSONObject categories = new JSONObject();
		if (distributions != null && distributions.size() > 0) {
			for (Object item : distributions) {
				JSONObject modelDist = (JSONObject) item;
				JSONObject summary = (JSONObject) modelDist
						.get(distributionType);

				JSONArray dist = new JSONArray();
				if (summary != null) {
					if (summary.get("bins") != null) {
						dist = (JSONArray) summary.get("bins");
					} else if (summary.get("counts") != null) {
						dist = (JSONArray) summary.get("counts");
					} else if (summary.get("categories") != null) {
						dist = (JSONArray) summary.get("categories");
					}
				}

				for (Object distr : dist) {
					JSONArray distInfo = (JSONArray) distr;
					String category = (String) distInfo.get(0);
					Long instances = (Long) distInfo.get(1);

					if (categories.containsKey(category)) {
						Long current = (Long) categories.get(category);
						categories.put(category, current + instances);
					} else {
						categories.put(category, instances);
					}
				}

			}
		}

		JSONArray distribution = new JSONArray();
		for (Object cat : categories.keySet()) {
			String category = (String) cat;
			JSONArray item = new JSONArray();
			item.add(category);
			item.add(categories.get(category));
			distribution.add(item);
		}

		sortDistribution(distribution);
		return distribution;
	}

	/**
	 * Prints ensemble summary. Only field importance at present.
	 *
	 */
	public String summarize() throws IOException {
		StringBuilder summarize = new StringBuilder();

		JSONArray distribution = getDataDistribution("training");
		if (!distribution.isEmpty()) {
			summarize.append("Data distribution:\n");
			summarize.append(Utils.printDistribution(distribution).toString());
			summarize.append("\n\n");
		}

		if (this.boosting == null) {
			JSONArray predictions = getDataDistribution("predictions");
			if (!predictions.isEmpty()) {
				summarize.append("Predicted distribution:\n");
				summarize.append(
						Utils.printDistribution(distribution).toString());
				summarize.append("\n\n");
			}
		}

		summarize.append("Field importance:\n");

		distribution = new JSONArray();
		for (Object fieldItem : importance.keySet()) {
			String fieldId = (String) fieldItem;
			double value = (Double) importance.get(fieldId);

			JSONArray item = new JSONArray();
			item.add(fieldId);
			item.add(value);
			distribution.add(item);
		}

		sortDistribution(distribution);

		int count = 1;
		for (Object fieldItem : distribution) {
			JSONArray field = (JSONArray) fieldItem;

			String fieldId = (String) field.get(0);
			double value = (Double) field.get(1);
			summarize.append(String.format("    %s. %s: %.2f%%\n", count++,
					Utils.getJSONObject(fields, fieldId + ".name"),
					(Utils.roundOff(value, 4) * 100)));
		}

		return summarize.toString();
	}

	/**
	 * Sorting utility
	 * 
	 */
	private void sortDistribution(JSONArray distribution) {
		Collections.sort(distribution, new Comparator<JSONArray>() {
			@Override
			public int compare(JSONArray o1, JSONArray o2) {
				Object o1Val = o1.get(1);
				Object o2Val = o2.get(1);

				if (o1Val instanceof Number) {
					o1Val = ((Number) o1Val).doubleValue();
					o2Val = ((Number) o2Val).doubleValue();
				}

				return ((Comparable) o2Val).compareTo(o1Val);
			}
		});
	}

	/**
	 * Makes a prediction based on the prediction made by every model.
	 *
	 * @param inputData
	 *            Input data to be predicted.
	 * @param method
	 *            **deprecated**. Please check the operating_kind` attribute.
	 *            Numeric key code for the following combination methods in
	 *            classifications/regressions: 0 - majority vote (plurality)/
	 *            average: PLURALITY_CODE 1 - confidence weighted majority vote
	 *            / error weighted: CONFIDENCE_CODE 2 - probability weighted
	 *            majority vote / average: PROBABILITY_CODE 3 - threshold
	 *            filtered vote / doesn't apply: THRESHOLD_CODE
	 * @param options Options to be used in threshold filtered votes.
	 * @param missingStrategy numeric key for the individual model's prediction
	 *         method. See the model predict method.
	 * @param operatingPoint
	 *            In classification models, this is the point of the ROC curve
	 *            where the model will be used at. The operating point can be
	 *            defined in terms of: - the positive_class, the class that is
	 *            important to predict accurately - its kind: probability,
	 *            confidence or voting - its threshold: the minimum established
	 *            for the positive_class to be predicted. The operating_point is
	 *            then defined as a map with three attributes, e.g.:
	 *            {"positive_class": "Iris-setosa", "kind": "probability",
	 *            "threshold": 0.5}
	 * @param operatingKind
	 *            probability", "confidence" or "votes". Sets the property that
	 *            decides the prediction. Used only if no operating_point is
	 *            used
	 * @param median
	 *            Uses the median of each individual model's predicted node as
	 *            individual prediction for the specified combination method.
	 * @param full
	 *            Boolean that controls whether to include the prediction's
	 *            attributes. By default, only the prediction is produced. If
	 *            set to True, the rest of available information is added in a
	 *            dictionary format. The dictionary keys can be: - prediction:
	 *            the prediction value - probability: prediction's probability -
	 *            distribution: distribution of probabilities for each of the
	 *            objective field classes - unused_fields: list of fields in the
	 *            input data that
	 * 
	 */
	public HashMap<String, Object> predict(JSONObject inputData, 
			PredictionMethod method, Map options, 
			MissingStrategy missingStrategy,
			JSONObject operatingPoint, String operatingKind, 
			Boolean median, Boolean full) throws Exception {
		
		if (missingStrategy == null) {
			missingStrategy = MissingStrategy.LAST_PREDICTION;
		}

		if (median == null) {
			median = false;
		}

		if (full == null) {
			full = false;
		}

		// Checks and cleans inputData leaving the fields used in the model
		inputData = filterInputData(inputData, full);

		List<String> unusedFields = (List<String>) inputData
				.get("unusedFields");
		inputData = (JSONObject) inputData.get("newInputData");

		// Strips affixes for numeric values and casts to the final field type
		Utils.cast(inputData, fields);

		if (median && method == null) {
			// predictions with median are only available with old combiners
			method = PredictionMethod.PLURALITY;
		}

		if (method == null && operatingPoint == null && operatingKind == null
				&& !median) {
			// operating_point has precedence over operating_kind. If no
			// combiner is set, default operating kind is "probability"
			operatingKind = "probability";
		}
		
		// Operating Point
		if (operatingPoint != null) {
			if (regression) {
				throw new IllegalArgumentException(
						"The operatingPoint argument can only be"
								+ " used in classifications.");
			}

			return predictOperating(inputData, missingStrategy, operatingPoint);
		}

		if (operatingKind != null) {
			if (regression) {
				// for regressions, operating_kind defaults to the old combiners
				method = "confidence".equals(operatingKind)
						? PredictionMethod.CONFIDENCE
						: PredictionMethod.PLURALITY;
				
				return predict(inputData, method, options, missingStrategy,
						null, null, null, full);
			} else {
				// predict operating point
				return predictOperatingKind(inputData, missingStrategy,
						operatingKind);
			}
		}
		
		MultiVote votes = null;
		if (modelsSplit != null && modelsSplit.size() > 1) {
			// If there's more than one chunk of models, they must be
			// sequentially used to generate the votes for the prediction
			votes = new MultiVote();
			for (Object split : modelsSplit) {
				JSONArray models = (JSONArray) split;
				MultiModel multiModel = new MultiModel(models, fields, null);

				MultiVote modelVotes = multiModel.generateVotes(inputData,
						missingStrategy, unusedFields);
				votes.extend(modelVotes);
			}
		} else {
			// When only one group of models is found you use the
			// corresponding multimodel to predict
			MultiVote votesSplit = this.multiModel.generateVotes(inputData,
					missingStrategy, unusedFields);
			
			votes = new MultiVote(votesSplit.predictions, boostingOffsets);
		}

		if (this.boosting != null && !this.regression) {
			options = new HashMap();
			JSONArray categories = (JSONArray) Utils.getJSONObject(
					(JSONObject) fields.get(objectiveField),
					"summary.categories", new JSONArray());
			options.put("categories", categories);
		}

		HashMap<Object, Object> results = votes.combine(method, options);
		
		HashMap<String, Object> prediction = new HashMap<String, Object>();
		for (Object key : results.keySet()) {
			prediction.put((String) key, results.get(key));
		}

		if (full) {
			prediction.put("unused_fields", unusedFields);
		}

		return prediction;
	}

	/**
	 * Computes the prediction based on a user-given operating point.
	 */
	private HashMap<String, Object> predictOperating(JSONObject inputData,
			MissingStrategy missingStrategy, JSONObject operatingPoint)
			throws Exception {

		if (missingStrategy == null) {
			missingStrategy = MissingStrategy.LAST_PREDICTION;
		}

		Object[] operating = Utils.parseOperatingPoint(operatingPoint,
				OPERATING_POINT_KINDS, classNames);

		String kind = (String) operating[0];
		Double threshold = (Double) operating[1];
		String positiveClass = (String) operating[2];

		if (!Arrays.asList(OPERATING_POINT_KINDS).contains(kind)) {
			throw new IllegalArgumentException(String.format(
					"Allowed operating kinds are %", OPERATING_POINT_KINDS));
		}

		JSONArray predictions = null;
		if (kind.equals("probability")) {
			predictions = predictProbability(inputData, missingStrategy);
		}
		if (kind.equals("confidence")) {
			predictions = predictConfidence(inputData, missingStrategy);
		}
		if (kind.equals("votes")) {
			predictions = predictVotes(inputData, missingStrategy);
		}

		for (Object pred : predictions) {
			HashMap<String, Object> prediction 
				= (HashMap<String, Object>) pred;
			String category = (String) prediction.get("category");

			prediction.put("prediction", prediction.get("category"));
			prediction.remove("category");

			if (category.equals(positiveClass)
					&& (Double) prediction.get(kind) > threshold) {
				return prediction;
			}
		}

		HashMap<String, Object> prediction 
			= (HashMap<String, Object>) predictions.get(0);
		String category = (String) prediction.get("prediction");
		if (category.equals(positiveClass)) {
			prediction = (HashMap<String, Object>) predictions.get(1);
		}

		return prediction;
	}

	/**
	 * Computes the prediction based on a user-given operating kind, i.e,
	 * confidence, probability or votes.
	 */
	private HashMap<String, Object> predictOperatingKind(JSONObject inputData,
			MissingStrategy missingStrategy, String operatingKind)
			throws Exception {

		if (missingStrategy == null) {
			missingStrategy = MissingStrategy.LAST_PREDICTION;
		}

		String kind = operatingKind.toLowerCase();

		if (boosting != null && !"probability".equals(kind)) {
			throw new IllegalArgumentException(
					"Only probability is allowed as operating kind "
							+ "for boosted ensembles.");
		}

		if (!Arrays.asList(OPERATING_POINT_KINDS).contains(kind)) {
			throw new IllegalArgumentException(String.format(
					"Allowed operating kinds are %", OPERATING_POINT_KINDS));
		}

		JSONArray predictions = null;
		if (kind.equals("probability")) {
			predictions = predictProbability(inputData, missingStrategy);
		}
		if (kind.equals("confidence")) {
			predictions = predictConfidence(inputData, missingStrategy);
		}
		if (kind.equals("votes")) {
			predictions = predictVotes(inputData, missingStrategy);
		}

		HashMap<String, Object> prediction 
			= (HashMap<String, Object>) predictions.get(0);
		prediction.put("prediction", prediction.get("category"));
		prediction.remove("category");

		return prediction;
	}


	/**
	 * For classification models, Predicts a probability for each possible
	 * output class, based on input values. The input fields must be a
	 * dictionary keyed by field name or field ID.
	 * 
	 * For regressions, the output is a single element list containing the
	 * prediction.
	 *
	 * @param inputData
	 *            Input data to be predicted
	 * @param missingStrategy
	 *            LAST_PREDICTION|PROPORTIONAL missing strategy for missing
	 *            fields
	 */
	public JSONArray predictProbability(JSONObject inputData,
			MissingStrategy missingStrategy) throws Exception {

		JSONArray predictions = new JSONArray();
		HashMap<String, Object> prediction = null;
		if (regression) {
			prediction = predict(inputData, PredictionMethod.PROBABILITY, null,
					missingStrategy, null, null, null, true);
			predictions.add(prediction);
		} else {
			if (boosting != null) {
				prediction = predict(inputData, PredictionMethod.PLURALITY,
						null, missingStrategy, null, null, null, true);
				JSONArray probabilities = (JSONArray) prediction
						.get("probabilities");
				predictions.add(probabilities);
			} else {
				List<Double> output = combineDistributions(inputData,
						missingStrategy, null);
				
				for (int i = 0; i < classNames.size(); i++) {
					prediction = new JSONObject();
					prediction.put("category", (String) classNames.get(i));
					prediction.put("probability", output.get(i));
					predictions.add(prediction);
				}
			}
		}

		Utils.sortPredictions(predictions, "probability", "category");
		return predictions;

	}

	/**
	 * For classification models, Predicts a confidence for each possible output
	 * class, based on input values. The input fields must be a dictionary keyed
	 * by field name or field ID.
	 * 
	 * For regressions, the output is a single element list containing the
	 * prediction.
	 *
	 * @param inputData
	 *            Input data to be predicted
	 * @param missingStrategy
	 *            LAST_PREDICTION|PROPORTIONAL missing strategy for missing
	 *            fields
	 */
	private JSONArray predictConfidence(JSONObject inputData,
			MissingStrategy missingStrategy) throws Exception {

		if (boosting != null) {
			// we use boosting probabilities as confidences also
			return predictProbability(inputData, missingStrategy);
		}

		JSONArray predictions = new JSONArray();
		HashMap<String, Object> prediction = null;
		if (regression) {
			prediction = predict(inputData, PredictionMethod.CONFIDENCE, null,
					missingStrategy, null, null, null, true);
			predictions.add(prediction);
		} else {
			List<Double> output = combineDistributions(inputData,
					missingStrategy, PredictionMethod.CONFIDENCE);

			for (int i = 0; i < classNames.size(); i++) {
				prediction = new JSONObject();
				prediction.put("category", (String) classNames.get(i));
				prediction.put("confidence", output.get(i));
				predictions.add(prediction);
			}
		}

		Utils.sortPredictions(predictions, "confidence", "category");
		return predictions;
	}

	/**
	 * For classification models, Predicts the votes for each possible output
	 * class, based on input values. The input fields must be a dictionary keyed
	 * by field name or field ID.
	 * 
	 * For regressions, the output is a single element list containing the
	 * prediction.
	 *
	 * @param inputData
	 *            Input data to be predicted
	 * @param missingStrategy
	 *            LAST_PREDICTION|PROPORTIONAL missing strategy for missing
	 *            fields
	 */
	private JSONArray predictVotes(JSONObject inputData,
			MissingStrategy missingStrategy) throws Exception {

		JSONArray predictions = new JSONArray();
		HashMap<String, Object> prediction = null;
		if (regression) {
			prediction = predict(inputData, PredictionMethod.PLURALITY, null,
					missingStrategy, null, null, null, true);
			predictions.add(prediction);
		} else {
			if (boosting != null) {
				throw new IllegalArgumentException(
						"Votes cannot be computed for boosted ensembles.");
			} else {
				List<Double> output = combineDistributions(inputData,
						missingStrategy, PredictionMethod.PLURALITY);

				for (int i = 0; i < classNames.size(); i++) {
					prediction = new JSONObject();
					prediction.put("category", (String) classNames.get(i));
					prediction.put("votes", output.get(i));
					predictions.add(prediction);
				}
			}
		}

		Utils.sortPredictions(predictions, "votes", "category");
		return predictions;
	}

}
