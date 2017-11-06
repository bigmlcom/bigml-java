package org.bigml.binding;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.bigml.binding.localassociation.*;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;

/**
 * A local Association Rules object.
 *
 * This module defines an Association Rule object as extracted from a given
 * dataset. It shows the items discovered in the dataset and the association
 * rules between these items.
 *
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the association/id
 * below):
 *
 * // API client BigMLClient api = BigMLClient.getInstance();
 *
 * // Retrieve a remote association by id JSONObject association =
 * api.getAssociation( "association/551aa203af447f5484000ec0");
 *
 * // A lightweight wrapper around an Association resurce LocalAssociation
 * localAssociation = new LocalAssociation(association);
 *
 * // Get rules
 * localAssociation.rules();
 *
 */
public class LocalAssociation extends ModelFields implements Serializable {

	private static final long serialVersionUID = 1L;

	final static String DEFAULT_SEARCH_STRATEGY = "leverage";
    final static int DEFAULT_K = 100;
	final static String INDENT = "    ";
	static List<String> ASSOCIATION_METRICS = new ArrayList<String>();
	static {
		ASSOCIATION_METRICS.add("lhs_cover");
		ASSOCIATION_METRICS.add("support");
		ASSOCIATION_METRICS.add("confidence");
		ASSOCIATION_METRICS.add("leverage");
		ASSOCIATION_METRICS.add("lift");
		ASSOCIATION_METRICS.add("p_value");
	}

	static HashMap<String, String> METRIC_LITERALS =
        new HashMap<String, String>();
	static {
		METRIC_LITERALS.put("confidence", "Confidence");
		METRIC_LITERALS.put("support", "Support");
		METRIC_LITERALS.put("leverage", "Leverage");
		METRIC_LITERALS.put("lhs_cover", "Coverage");
		METRIC_LITERALS.put("p_value", "p-value");
		METRIC_LITERALS.put("lift", "Lift");
	}

    static HashMap<String, String> METRIC_RULE_PROPERTIES =
        new HashMap<String, String>();
    static {
        METRIC_RULE_PROPERTIES.put("lhs_cover", "lhsCover");
        METRIC_RULE_PROPERTIES.put("support", "support");
        METRIC_RULE_PROPERTIES.put("confidence", "confidence");
        METRIC_RULE_PROPERTIES.put("leverage", "leverage");
        METRIC_RULE_PROPERTIES.put("p_value", "pValue");
        METRIC_RULE_PROPERTIES.put("lift", "lift");
        METRIC_RULE_PROPERTIES.put("rhs_cover", "rhsCover");
    }

    static String[] RULE_HEADERS = {"Rule ID", "Antecedent", "Consequent",
        "Antecedent Coverage %", "Antecedent Coverage", "Support %", "Support",
        "Confidence", "Leverage", "Lift", "p-value", "Consequent Coverage %",
        "Consequent Coverage"};

    private static List<String> SCORES =
        ASSOCIATION_METRICS.subList(0, ASSOCIATION_METRICS.size()-1);
    private static String SCORES_AVAILABLE = Utils.join(SCORES, ", ");

    private static List<String> NO_ITEMS = new ArrayList<String>();
    static {
        NO_ITEMS.add("numeric");
        NO_ITEMS.add("categorical");
    }

	private List<AssociationRule> rules;
	private List<AssociationItem> items;

	public LocalAssociation(JSONObject association) throws Exception {
		super();

		if (association.get("resource") == null) {
			throw new Exception(
					"Cannot create the Association instance. Could not find " +
                    "the 'resource' key in the resource");
		}

		if (association.containsKey("object") && association.get("object") instanceof Map) {
			association = (JSONObject) association.get("object");
		}

		if (association.containsKey("associations") && association.get("associations") instanceof Map) {
			JSONObject status = (JSONObject) association.get("status");
			if (status != null && status.containsKey("code")
					&& AbstractResource.FINISHED == ((Number) status.get("code")).intValue()) {

				JSONObject associations = (JSONObject)
                    Utils.getJSONObject(association, "associations");

				super.initialize((JSONObject) associations.get("fields"), null, null, null);

				if (associations.get("rules") != null) {
					rules = new ArrayList<AssociationRule>();
					for (Object rule : (JSONArray) associations.get("rules")) {
						JSONObject ruleInfo = (JSONObject) rule;
						rules.add(new AssociationRule(ruleInfo));
					}
				}

				JSONObject fields = (JSONObject) associations.get("fields");
				if (associations.get("items") != null) {
					items = new ArrayList<AssociationItem>();
					int index = 0;
					for (Object item : (JSONArray) associations.get("items")) {
						JSONObject itemInfo = (JSONObject) item;
						items.add(new AssociationItem(index++, itemInfo, fields));
					}
				}
			} else {
				throw new Exception("The association isn't finished yet");
			}
		} else {
			throw new Exception(String.format("Cannot create the Association instance. Could not"
					+ " find the 'associations' key in the" + " resource:\n\n%s",
					((JSONObject) association.get("model")).keySet()));
		}
	}



    /**
     * Returns the Consequents for the rules whose LHS best match the provided
     * items. Cosine similarity is used to score the match.
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
     * @param k
     *           maximum number of item predictions to return (Default 100)
     * @param scoreBy
     *          Code for the metric used in scoring (default search_strategy)
     *              leverage
     *              confidence
     *              support
     *              lhs-cover
     *              lift
     * @param by_name
     *          If True, input_data is keyed by field name, field_id is used
     *          otherwise.
     */
    public List associationSet(JSONObject inputData, Integer k, String scoreBy,
        Boolean byName) throws Exception {

        if (k == null) {
            k = DEFAULT_K;
        }

        if (byName == null) {
            byName = true;
        }

        if (scoreBy != null && !SCORES.contains(scoreBy)) {
            throw new Exception(
                String.format("The available values of scoreBy are: %s",
                              SCORES_AVAILABLE));
        }

        inputData = filterInputData(inputData, byName);

        List<Integer> itemsIndexes = new ArrayList<Integer>();

        // retrieving the items in inputData
        for (AssociationItem item : items(null, null, inputData, null)) {
            itemsIndexes.add(item.getIndex());
        }

        if (scoreBy == null) {
            scoreBy = DEFAULT_SEARCH_STRATEGY;
        }

        // Key: rhs[0]-rhs[1]
        HashMap<String, Map> predictions = new HashMap<String, Map>();

        for (AssociationRule rule: this.rules) {
            List<Integer> lhsList = Arrays.asList(rule.getLhs());

            // checking that the field in the rhs is not in the input data
            Integer rhsValue = rule.getRhs()[0];
            AssociationItem item = items.get(rhsValue.intValue());
            String fieldType = (String) Utils.getJSONObject(
                super.fields, String.format("%s.optype",item.getFieldId()));

            // if the rhs corresponds to a non-itemized field and this field
            // is already in input_data, don't add rhs
            if (NO_ITEMS.contains(fieldType) && inputData.get(item.getFieldId())!=null) {
                continue;
            }

            // if an itemized content is in input_data, don't add it to the
            // prediction
            if (!NO_ITEMS.contains(fieldType) && itemsIndexes.contains(rhsValue)) {
                continue;
            }

            double cosine = 0;
            for (int itemIndex: itemsIndexes) {
                if (lhsList.contains(itemIndex)) {
                    cosine++;
                }
            }

            if (cosine > 0) {
                cosine = cosine /
                    (Math.sqrt(itemsIndexes.size()) * Math.sqrt(lhsList.size()));

                String predictionKey = Utils.join(Arrays.asList(rule.getRhs()), "-");

                HashMap<String, Object> rhsMap = new HashMap<String, Object>();
                if (predictions.get(predictionKey) == null) {
                    rhsMap.put("rhs", rule.getRhs()[0]);
                    rhsMap.put("score", 0.0);
                    rhsMap.put("rules", new ArrayList<String>());
                    predictions.put(predictionKey, rhsMap);
                }

                rhsMap = (HashMap<String, Object>) predictions.get(predictionKey);

                Field field = rule.getClass().getDeclaredField(
                    (String) LocalAssociation.METRIC_RULE_PROPERTIES.get(scoreBy));
                field.setAccessible(true);
                Double scoreByValue = (Double) field.get(rule);

                Double score = (Double) rhsMap.get("score");
                score += cosine * scoreByValue.doubleValue();
                rhsMap.put("score", score);

                List<String> rules = (List<String>) rhsMap.get("rules");
                rules.add(rule.getRuleId());
            }

        }

        // choose the best k predictions
        List predictinsValues = new ArrayList( predictions.values() );
        Collections.sort(predictinsValues, new AssociationSetMapComparator());
        predictinsValues = predictinsValues.subList(0,
            Math.min(k, predictinsValues.size()));

        List<HashMap<String, Object>> finalPredictions =
            new ArrayList<HashMap<String, Object>>();

        for (Object prediction: predictinsValues) {
            HashMap pred = (HashMap) prediction;
            AssociationItem item = items.get((Integer) pred.get("rhs"));
            pred.remove("rhs");

            Map itemJson = item.toJson();
            itemJson.remove("description");
            itemJson.remove("binEnd");
            itemJson.remove("binStart");

            pred.put("item", itemJson);
            finalPredictions.add(pred);
        }

        return finalPredictions;
    }


	/**
	 * Returns the items array, previously selected by the field corresponding
	 * to the given field name or a user-defined function (if set).
	 *
	 */
	public List<AssociationItem> items(String field, List<String> names,
        Map inputMap, ItemFilter itemFilter) throws Exception {



		String fieldId = null;
		if (field != null) {
			if (this.fields.get(field) != null) {
				fieldId = field;
			} else {
				if (this.invertedFields.get(field) != null) {
					fieldId = (String) this.invertedFields.get(field);
				} else {
					throw new Exception(
						String.format("Failed to find a field name or ID" +
                                      "corresponding to %s.", field));
				}
			}
		}

		List<AssociationItem> items = new ArrayList<AssociationItem>();
		for (AssociationItem item : this.items) {
			if ((fieldId == null || item.getFieldId().equals(fieldId)) &&
                (names == null || names.contains(item.getName())) &&
                (inputMap == null || item.matches(inputMap.get(item.getFieldId())) &&
                (itemFilter == null || !itemFilter.filter(item)))) {
				items.add(item);
			}
		}

		return items;
	}

	/**
	 * Returns the rules array, previously selected by the leverage or a
	 * user-defined filter function (if set).
	 *
	 * @param minLeverage
	 *            Minum leverage value
     * @param Confidence
     *            Minum confidence value
	 * @param minPvalue
	 *            Minum p_value value
	 * @param itemList
	 *            List of Item objects. Any of them should be in the rules
	 * @param ruleFilter
	 *            Function used as filter
	 *
	 */
	public List<AssociationRule> rules(Double minLeverage, Double minConfidence,
        Double minPvalue, List itemList, RuleFilter ruleFilter)
        throws Exception {

		List<AssociationRule> rules = new ArrayList<AssociationRule>();
		for (AssociationRule rule : this.rules) {
			if ((minLeverage == null || rule.getLeverage() >= minLeverage) &&
				(minConfidence == null || rule.getConfidence() >= minConfidence) &&
                (minPvalue == null || rule.getPValue() >= minPvalue) &&
                checkItemList(rule, itemList) &&
				(ruleFilter == null || !ruleFilter.filter(rule))) {
				rules.add(rule);
			}
		}
		return rules;
	}

	/*
	 * Checking if any of the items list is in a rule
	 */
	private boolean checkItemList(AssociationRule rule, List itemList)
        throws Exception {

		if (itemList == null || itemList.size() == 0) {
			return true;
		}

		List<Integer> items = new ArrayList<Integer>();
		if (itemList.get(0) instanceof AssociationItem) {
			for (Object item : itemList) {
				AssociationItem associationItem = (AssociationItem) item;
				items.add(associationItem.getIndex());
			}
		} else {
			if (itemList.get(0) instanceof String) {
				for (AssociationItem item : items(null, itemList, null, null)) {
					items.add(item.getIndex());
				}
			}
		}

		for (Integer itemIndex : rule.getLhs()) {
			if (items.contains(itemIndex)) {
				return true;
			}
		}

		for (Integer itemIndex : rule.getRhs()) {
			if (items.contains(itemIndex)) {
				return true;
			}
		}

		return false;
	}


    /**
     * Stores the rules in CSV format in the user-given file. The rules
     * can be previously selected using the arguments in rules
     */
    public void rulesCsv(String outputFilePath, Double minLeverage,
        Double minConfidence, Double minPvalue, List<AssociationItem> itemList,
        RuleFilter ruleFilter) throws Exception {

        if (outputFilePath == null) {
            throw new Exception("A valid file name is required to store the rules");
        }

        Writer rulesFile = null;
        try {
            rulesFile = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFilePath), "UTF-8"));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                String.format("Cannot find %s directory.", outputFilePath));
        }

        List<AssociationRule> rules = rules(
            minLeverage, minConfidence, minPvalue, itemList, ruleFilter);

        List rows = new ArrayList();
        for (AssociationRule rule: rules) {
            rows.add(rule.toCsv());
        }

        final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(
            RULE_HEADERS).print(rulesFile);

        try {
            printer.printRecords(rows);
        } catch (Exception e) {
            throw new IOException("Error generating the CSV !!!");
        }

        try {
            rulesFile.flush();
            rulesFile.close();
        } catch (IOException e) {
            throw new IOException("Error while flushing/closing fileWriter !!!");
        }
    }


	/**
	 * Transforms the lhs and rhs index information to a human-readable rule text.
	 */
	public List describe(List ruleCsv) throws Exception {
		// lhs items and rhs items (second and third element in the row)
		// substitution by description
		for (int i = 1; i <= 2; i++) {
			StringBuilder description = new StringBuilder();
			Double[] values = (Double[]) ruleCsv.get(i);
			for (Double itemIndex : values) {
				AssociationItem item = this.items.get(itemIndex.intValue());
				// if there's just one field, we don't use the item description
				// to avoid repeating the field name constantly.

				if (fields.size() == 1 && !item.getComplement()) {
					description.append(String.format("%s & ", item.getName()));
				} else {
					description.append(String.format("%s & ", item.describe()));
				}

				String s = description.toString();
				ruleCsv.set(i, s.substring(0, s.length() - 3));
			}
		}

		return ruleCsv;
	}

	/**
	 * Prints a summary of the obtained rules
	 */
	public String summarize(Integer limit, Double minLeverage,
        Double minConfidence, Double minPvalue, List<AssociationItem> itemList,
		RuleFilter ruleFilter) throws Exception {

		if (limit == null) {
			limit = 10;
		}

		StringBuilder summary = new StringBuilder();

		// groups the rules by its metrics
		List<AssociationRule> rules = rules(
            minLeverage, minConfidence, minPvalue, itemList, ruleFilter);
		summary.append(String.format("Total number of rules: %s\n", rules.size()));

		for (String metric : ASSOCIATION_METRICS) {
			summary.append(String.format("\n\nTop %s by %s:\n\n", limit, METRIC_LITERALS.get(metric)));

			List<AssociationRule> topRules = new ArrayList<AssociationRule>(rules);
			Collections.sort(topRules, new AssociationRuleFieldComparator(metric));
			topRules = topRules.subList(0, limit * 2);

			List<String> outRules = new ArrayList<String>();
			List<String> refRules = new ArrayList<String>();
			int counter = 0;

			for (AssociationRule rule : topRules) {
				List ruleNow = describe(rule.toCsv());
				String metricString = getMetricString(rule, false);

				String operator = "->";
				String ruleIdString = String.format("Rule %s: ", rule.getRuleId());

				for (AssociationRule item : topRules) {
					String metricStringItem = getMetricString(item, true);
					if (compareArrays(rule.getRhs(), item.getLhs()) &&
                        compareArrays(rule.getLhs(), item.getRhs()) &&
					    metricString.equals(metricStringItem)) {

						ruleIdString = String.format("Rules %s, %s: ", rule.getRuleId(), item.getRuleId());
						operator = "<->";
					}
				}

				String outRule = String.format("%s %s %s [%s]",
                    ruleNow.get(1), operator, ruleNow.get(2), metricString);
				String reverseRule = String.format("%s %s %s [%s]",
                    ruleNow.get(2), operator, ruleNow.get(1), metricString);

				if (operator.equals("->") || !refRules.contains(reverseRule)) {
					refRules.add(outRule);
					outRule = String.format("%s%s%s", INDENT + INDENT, ruleIdString, outRule);
					outRules.add(outRule);
					counter++;
					if (counter > limit) {
						break;
					}
				}
			}

            summary.append(Utils.join(outRules, "\n"));
		}

		summary.append("\n");
		return summary.toString();
	}


	/**
	 * Returns the string that describes the values of metrics for a rule.
	 */
	private String getMetricString(AssociationRule rule, Boolean reverse)
		throws Exception{

		if (reverse == null) {
			reverse = false;
		}

		StringBuilder metricString = new StringBuilder();

		for (String metric : ASSOCIATION_METRICS) {
			String metricKey = metric;
			if (reverse && metric.equals("lhs_cover")) {
				metricKey = "rhs_cover";
			}

			Field field = rule.getClass().getDeclaredField(
                (String) METRIC_RULE_PROPERTIES.get(metricKey));
			field.setAccessible(true);
			Object metricValue = field.get(rule);

			if (metricValue.getClass().isArray()) {
				Double[] value = (Double[]) metricValue;
				metricString.append(String.format("%s=%.2f%% (%s); ", METRIC_LITERALS.get(metric),
						(Utils.roundOff(((Number) value[0]).doubleValue(), 4) * 100), value[1]));
			} else {
				if (metric.equals("confidence")) {
					metricString.append(String.format("%s=%.2f%%; ", METRIC_LITERALS.get(metric),
							(Utils.roundOff(((Number) metricValue).doubleValue(), 4) * 100)));
				} else {
					metricString.append(String.format("%s=%s; ", METRIC_LITERALS.get(metric), metricValue));
				}
			}
		}

		String s = metricString.toString();
		return s.substring(0, s.length() - 2);
	}

    /**
     * Compare arrays
     */
    private boolean compareArrays(Integer[] arr1, Integer[] arr2) {
        Arrays.sort(arr1);
        Arrays.sort(arr2);
        return Arrays.equals(arr1, arr2);
    }
}



class AssociationSetMapComparator implements Comparator<Map>{
    @Override
    public int compare(Map m1, Map m2) {
        return ((Double) m2.get("score")).compareTo( (Double) m1.get("score") );
    }
}


class AssociationRuleFieldComparator implements Comparator<AssociationRule>{
    private String metric;

    public AssociationRuleFieldComparator(String metric) {
        this.metric = metric;
    }

    @Override
    public int compare(AssociationRule r1, AssociationRule r2) {
    	try {
            String propertyName =
                (String) LocalAssociation.METRIC_RULE_PROPERTIES.get(metric);

            Field field1 = r1.getClass().getDeclaredField(propertyName);
            field1.setAccessible(true);

            Field field2 = r2.getClass().getDeclaredField(propertyName);
            field2.setAccessible(true);

            Double value1, value2 = 0.0;

            if (metric.equals("lhs_cover") || metric.equals("rhs_cover") ||
                metric.equals("support")) {
                value1 = ((Double[]) field1.get(r1))[0];
                value2 = ((Double[]) field2.get(r2))[0];
            } else {
    	        value1 = (Double) field1.get(r1);
                value2 = (Double) field2.get(r2);
            }

            return value2.compareTo(value1);
		} catch (Exception e) {
			return 1;
		}
    }
}
