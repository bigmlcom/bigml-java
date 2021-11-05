package org.bigml.binding;

import org.apache.commons.text.StringEscapeUtils;
import org.bigml.binding.utils.Chronos;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ModelFields resource.
 * 
 * This module defines a ModelFields class to hold the information associated to
 * the fields of the model resource in BigML. It becomes the starting point for
 * the Model class, that is used for local predictions.
 * 
 */
public abstract class ModelFields implements Serializable {

	private static final long serialVersionUID = 1L;

	// Logging
	static Logger LOGGER = LoggerFactory.getLogger(ModelFields.class);
	
	private static String DEFAULT_LOCALE = "en_US.UTF-8";

	public static String[] DEFAULT_MISSING_TOKENS = Fields.DEFAULT_MISSING_TOKENS;

	public static HashMap<String, String> FIELDS_PARENT = new HashMap<String, String>();
	static {
		FIELDS_PARENT.put("cluster", "clusters");
		FIELDS_PARENT.put("logisticregression", "logistic_regression");
		FIELDS_PARENT.put("ensemble", "ensemble");
		FIELDS_PARENT.put("deepnet", "deepnet");
		FIELDS_PARENT.put("linearregression", "linear_regression");
		FIELDS_PARENT.put("association", "associations");
	}
	
	protected String modelId;
	protected JSONObject model;
	protected BigMLClient bigmlClient;

	protected String objectiveFieldId;
	protected String objectiveFieldName;
	protected List<String> fieldsName;
	protected List<String> fieldsId;
	protected Map<String, String> fieldsIdByName;
	protected Map<String, String> fieldsNameById;

	protected List<String> missingTokens;
	protected JSONObject fields = null;
	protected JSONObject modelFields = null;
	protected JSONObject invertedFields = null;
	protected String dataLocale = null;

	protected Boolean missingNumerics = null;
	protected JSONObject termForms = new JSONObject();
	protected Map<String, List<String>> tagClouds = new HashMap<String, List<String>>();
	protected JSONObject termAnalysis = new JSONObject();
	protected JSONObject itemAnalysis = new JSONObject();
	protected Map<String, List<String>> items = new HashMap<String, List<String>>();
	protected JSONObject categories = new JSONObject();
	protected JSONObject numericFields = new JSONObject();

	/**
	 * The constructor can be instantiated with nothing inside.
	 *
	 * We will need to invoke the initialize in overridden classes
	 */
	protected ModelFields() {
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param bigmlClient	the client with connection to BigML
	 * @param model			the model
	 * 
	 * @throws Exception a generic exception
	 */
	protected ModelFields(BigMLClient bigmlClient, JSONObject model) 
			throws Exception {
		
		// checks whether the information needed for local predictions 
 		// is in the model argument
 		if (!checkModelFields(model)) {
 			// if the fields used by the model are not
 			// available, use only ID to retrieve it again
 			modelId = (String) model.get("resource");
 			boolean validId = modelId.matches(getModelIdRe());
 			if (!validId) {
 				throw new Exception(
 					modelId + " is not a valid resource ID.");
 			}
 		}
 		
 		if (!(model.containsKey("resource")
 				&& model.get("resource") != null)) {
 			initBigML(bigmlClient);
 			model = getBigMLModel(modelId);

 			if ((String) model.get("resource") == null) {
 				throw new Exception(
 					modelId + " is not a valid resource ID.");
 			}
 		}
 		
 		if (model.containsKey("object") &&
 				model.get("object") instanceof JSONObject) {
 			model = (JSONObject) model.get("object");
 		}
 		
 		if (model.containsKey("object") && 
 				model.get("object") instanceof Map) {
 			model = (JSONObject) model.get("object");
        }
 		
 		this.model = model; 
	}
	
	/**
	 * Inits BigMLClient
	 * 
	 * @param bigmlClient	BigMLClient
	 * 
	 * @throws Exception	Exception
	 */
	protected void initBigML(BigMLClient bigmlClient) throws Exception {

		this.bigmlClient =
            (bigmlClient != null)
                ? bigmlClient
                : new BigMLClient(null, null, BigMLClient.STORAGE);
	}

	/**
	 * The constructor can be instantiated with fields structure.
	 *
	 * @param fields
	 *            the fields structure itself
	 * @param objectiveFieldId
	 *            the ID of the objective field
	 * @param missingTokens
	 *            the list of missing tokens to use. DEFAULT_MISSING_TOKENS will
	 *            be used by default
	 * @param dataLocale
	 *            the locale of the data
	 */
	protected void initialize(JSONObject fields, String objectiveFieldId,
			String dataLocale, List<String> missingTokens) {

		initialize(fields, objectiveFieldId, dataLocale, missingTokens, false,
				false, false);
	}

	/**
	 * The constructor can be instantiated with fields structure.
	 *
	 * @param fields
	 *            the fields structure itself
	 * @param objectiveFieldId
	 *            the ID of the objective field
	 * @param dataLocale
	 *            the locale of the data
	 * @param missingTokens
	 *            the list of missing tokens to use. DEFAULT_MISSING_TOKENS will
	 *            be used by default
	 * @param terms
	 * 			  whether include terms or not
	 * @param categories
	 * 			  whether include categories or not
	 * @param numerics
	 * 			  whether include numerics or not
	 * 
	 */
	protected void initialize(JSONObject fields, String objectiveFieldId,
			String dataLocale, List<String> missingTokens, Boolean terms,
			Boolean categories, Boolean numerics) {

		this.fields = new JSONObject();
		this.fields.putAll(fields);

		this.objectiveFieldId = objectiveFieldId;
		if (this.objectiveFieldId != null) {
			this.objectiveFieldName = Utils
					.getJSONObject(fields, objectiveFieldId + ".name")
					.toString();
		}

		uniquifyNames(this.fields);
		this.invertedFields = Utils.invertDictionary(fields, "name");

		this.missingTokens = missingTokens;
		if (this.missingTokens == null) {
			this.missingTokens = new ArrayList<String>(
					Arrays.asList(DEFAULT_MISSING_TOKENS));
		}

		this.dataLocale = dataLocale;
		if (this.dataLocale == null) {
			this.dataLocale = DEFAULT_LOCALE;
		}

		if (categories) {
			this.categories = new JSONObject();
		}

		if (terms || categories || numerics) {
			addTerms(categories, numerics);
		}

	}

	/**
	 * Adds the terms information of text and items fields
	 * 
	 */
	private void addTerms(boolean categories, boolean numerics) {
		for (Object fieldId : fields.keySet()) {
			JSONObject field = (JSONObject) fields.get(fieldId);

			if ("text".equals(field.get("optype"))) {
				termForms.put(fieldId, Utils.getJSONObject(field,
						"summary.term_forms", new JSONObject()));

				List<String> fieldTagClouds = new ArrayList<String>();
				JSONArray tags = (JSONArray) Utils.getJSONObject(field,
						"summary.tag_cloud", new JSONArray());
				for (Object tag : tags) {
					JSONArray tagArr = (JSONArray) tag;
					fieldTagClouds.add(tagArr.get(0).toString());
				}
				tagClouds.put(fieldId.toString(), fieldTagClouds);

				termAnalysis.put(fieldId, Utils.getJSONObject(field,
						"term_analysis", new JSONObject()));
			}

			if ("items".equals(field.get("optype"))) {
				List<String> fieldItems = new ArrayList<String>();
				JSONArray itemsArray = (JSONArray) Utils.getJSONObject(field,
						"summary.items", new JSONArray());
				for (Object item : itemsArray) {
					JSONArray itemArr = (JSONArray) item;
					fieldItems.add(itemArr.get(0).toString());
				}
				items.put(fieldId.toString(), fieldItems);

				itemAnalysis.put(fieldId, Utils.getJSONObject(field,
						"item_analysis", new JSONObject()));
			}

			if (categories && "categorical".equals(field.get("optype"))) {
				JSONArray cats = (JSONArray) Utils.getJSONObject(field,
						"summary.categories", new JSONArray());

				JSONArray categoriesList = new JSONArray();
				for (Object category : cats) {
					categoriesList.add(((JSONArray) category).get(0));
				}
				this.categories.put(fieldId, categoriesList);
			}

			if (numerics && this.missingNumerics != null
					&& "numeric".equals(field.get("optype"))) {
				this.numericFields.put(fieldId, true);
			}

		}
	}

	/**
	 * Checks the model structure to see if it contains model keys
	 * 
	 * @param model	the model to check
	 * 
	 * @return whether the model structure contains model key
	 */
	protected boolean checkModelStructure(JSONObject model) {
		return checkModelStructure(model, "model");
	}

	/**
	 * Checks the model structure to see if it contains all the needed keys
	 * 
	 * @param model		the model to check
	 * @param innerKey	the key to check in resource
	 * 
	 * @return whether the model contains the key
	 */
	protected boolean checkModelStructure(JSONObject model, String innerKey) {
		return model.containsKey("resource") && model.get("resource") != null
				&& (model.containsKey("object") && Utils.getJSONObject(model,
						"object." + innerKey, null) != null
						|| model.containsKey(innerKey));
	}

	/**
	 * Checks the model structure to see whether it contains the required fields
	 * information
	 * 
	 * @param model	the model
	 * 
	 * @return whether the model contains fields
	 */
	protected boolean checkModelFields(JSONObject model) {
		if (!model.containsKey("resource") || model.get("resource") == null) {
			return false;
		}

		String resource = (String) model.get("resource");
		String innerKey = "model";
		if (FIELDS_PARENT.containsKey(resource.split("/")[0])) {
			innerKey = FIELDS_PARENT.get(resource.split("/")[0]);
		}

		if (checkModelStructure(model, innerKey)) {
			model = (JSONObject) Utils.getJSONObject(model, "object", model);

			JSONObject modelObj = (JSONObject) Utils.getJSONObject(model,
					innerKey, new JSONObject());

			JSONObject fields = (JSONObject) Utils.getJSONObject(model,
					"fields", modelObj.get("fields"));

			// models only need model_fields to work. The rest of
			// resources will need all fields to work
			JSONObject modelFields = (JSONObject) modelObj.get("model_fields");
			if (modelFields == null) {
				JSONObject fieldsMeta = (JSONObject) Utils.getJSONObject(model,
						"fields_meta", modelObj.get("fields_meta"));
				try {
					return fieldsMeta.get("count") == fieldsMeta.get("total");
				} catch (Exception e) {
					// stored old models will not have the fields_meta info,
					// sowe return True to avoid failing in this case
					return true;
				}
			} else {
				if (fields == null) {
					return false;
				}

				Iterator iter = modelFields.keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					if (!fields.containsKey(key)) {
						return false;
					}
				}

				return true;
			}
		}

		return false;
	}

	/**
	 * Fills the value set as default for numeric missing fields if user
	 * created the model with the default_numeric_value option
	 * 
	 * @param inputData	
	 * 				an object with field's id/value pairs representing the
	 *              instance you want to fill with numeric defaults
	 *
	 * @return the input data filled with numeric defaults
	 */
	public JSONObject fillNumericDefaults(JSONObject inputData) {
		try {
			Field objField = this.getClass().getDeclaredField("defaultNumericValue");
			objField.setAccessible(true);
			String value = (String) objField.get(this);

			if (value != null) {
				for (Object fieldId : fields.keySet()) {
					JSONObject field = (JSONObject) fields.get(fieldId);
					String optype = (String) Utils.getJSONObject(this.fields, fieldId + ".optype");

					if ((this.modelFields ==null || this.modelFields.containsKey(fieldId)) &&
						(this.objectiveFieldId == null || fieldId != this.objectiveFieldId) &&
						"numeric".equals(optype) &&
						inputData.get(fieldId) == null) {

						double defaultValue = ((Number) Utils.getJSONObject(field,
								"summary." + value, 0)).doubleValue();
						inputData.put(fieldId, defaultValue);
					}
				}
			}
		} catch (Exception e) {}

		return inputData;
	}


	/**
	 * Filters the keys given in input_data checking against model fields.
	 *
	 * @param inputData	
	 * 				an object with field's id/value pairs representing the
	 *              instance you want to filter
	 *              
	 * @return the filtered input data
	 */
	protected JSONObject filterInputData(JSONObject inputData) {
		JSONObject filteredInputData = filterInputData(inputData, false);
		return (JSONObject) filteredInputData.get("newInputData");
	}

	/**
	 * From a dictionary of fields, returns another dictionary
	 * with the subfields from each datetime field
	 */
	private Map<String, JSONArray> getDatetimeFormats() {
		JSONObject fields = this.fields;
		if (this.modelFields != null) {
			fields = this.modelFields;
		}

		Map<String, JSONArray> formats = new HashMap<String, JSONArray>();
		for (Object fieldId : fields.keySet()) {
            JSONObject field = (JSONObject) fields.get(fieldId);
            String optype = (String) Utils.getJSONObject(field, "optype");

            if ("datetime".equals(optype)) {
                formats.put(
                   (String) field.get("name"), (JSONArray) Utils.getJSONObject(field, "time_formats"));
            }
		}

        return formats;
	}


	/**
	 * From a dictionary of fields, returns another dictionary
	 * with the subfields from each datetime field
	 */
	private Map<String, JSONObject> getDatetimeSubfields(JSONObject fields) {
		Map<String, JSONObject> subfields = new HashMap<String, JSONObject>();

		for (Object fieldId : fields.keySet()) {
            JSONObject field = (JSONObject) fields.get(fieldId);
            String optype = (String) Utils.getJSONObject(field, "parent_optype");
            if ("datetime".equals(optype)) {
                String fid = (String) Utils.getJSONObject(field, "fieldID");
                String datatype = (String) Utils.getJSONObject(field, "datatype");
                JSONArray parentIds = (JSONArray) field.get("parent_ids");
                if (parentIds == null || parentIds.size() == 0) {
                    continue;
                }
                String parentId = (String) parentIds.get(0);
                String parentName = (String) Utils.getJSONObject(fields, parentId + ".name");
	            if (parentName == null) {
                    parentName = (String) Utils.getJSONObject(modelFields, parentId + ".name");
                }

	            JSONObject subfield = new JSONObject();
	            subfield.put(fid, datatype);

	            if (subfields.containsKey(parentName)) {
	                JSONObject sub = (JSONObject) subfields.get(parentName);
                    sub.putAll(subfield);
	            } else {
                    subfields.put(parentName, subfield);
	            }
            }
		}

		return subfields;
	}


	/**
	 * Retrieves all the values of the subfields from a given date
	 */
	private Map<String, Integer> expandDate(Object date, JSONObject subfields, JSONArray formats) {
		Map<String, Integer> expanded = new HashMap<String, Integer>();

		GregorianCalendar cal = new GregorianCalendar();
		try {
			Date parsedDate = Chronos.parse((String) date, formats);
			if (parsedDate == null) {
				return expanded;
			}
			cal.setTime(parsedDate);
		} catch (Exception e) {
			return expanded;
		}

		for (Object key : subfields.keySet()) {
			String fieldId = (String) key;
			String datePeriod = (String) subfields.get(fieldId);

			HashMap<String, Integer> dateTypes = new HashMap<String, Integer>();
			dateTypes.put("era", Calendar.ERA);
			dateTypes.put("year", Calendar.YEAR);
			dateTypes.put("month", Calendar.MONTH);
			dateTypes.put("day-of-month", Calendar.DAY_OF_MONTH);
			dateTypes.put("day-of-week", Calendar.DAY_OF_WEEK);
			dateTypes.put("week-of-month", Calendar.WEEK_OF_MONTH);
			dateTypes.put("day-of-week-in-month", Calendar.DAY_OF_WEEK_IN_MONTH);
			dateTypes.put("am-pm", Calendar.AM_PM);
			dateTypes.put("hour", Calendar.HOUR_OF_DAY);
			dateTypes.put("hour-of-day", Calendar.HOUR_OF_DAY);
			dateTypes.put("minute", Calendar.MINUTE);
			dateTypes.put("second", Calendar.SECOND);
			dateTypes.put("millisecond", Calendar.MILLISECOND);

			Integer value = cal.get(dateTypes.get(datePeriod));
			if ("month".equals(datePeriod)) {
				value += 1;
			}
			if ("day-of-week".equals(datePeriod)) {
				value = (value == 1 ? 7 : value-1);
			}

			expanded.put(fieldId, value);
		}

		return expanded;
	}


	/**
	 * Returns the values for all the subfields from all the datetime
	 * fields in input_data.
	 *
	 * @param inputData		
	 * 				an object with field's id/value pairs representing the
	 *              instance you want to expand
	 *              
	 * @return the expanded inputData
	 */
	protected Map<String, Object> expandDatetimeFields(JSONObject inputData) {
		Map<String, Object> expanded = new HashMap<String, Object>();
		Map<String, JSONArray> timeFormats = getDatetimeFormats();
		Map<String, JSONObject> subfields = getDatetimeSubfields(this.fields);

		for (Object nameObj : inputData.keySet()) {
			String name = (String) nameObj;
			Object date = inputData.get(name);
			if (subfields.containsKey(name)) {
				JSONArray formats = timeFormats.get(name);
				expanded.putAll(expandDate(date, subfields.get(name), formats));
			}
		}

		return expanded;
	}


	/**
	 * Filters the keys given in input_data checking against model fields.
	 * 
	 * If `addUnusedFields` is set to True, it also provides information about
	 * the ones that are not used.
	 *
	 * @param inputData			an object with field's id/value pairs representing the
	 *              			instance you want to filter
	 * @param addUnusedFields	if include unused fields
	 * 
	 * @return	filtered input data
	 */
	protected JSONObject filterInputData(JSONObject inputData,
			Boolean addUnusedFields) {

		if (addUnusedFields == null) {
			addUnusedFields = false;
		}

		Map<String, Object> datetimeFields = expandDatetimeFields(inputData);

		// remove all missing values
		Iterator<String> fieldIdItr = inputData.keySet().iterator();
		while (fieldIdItr.hasNext()) {
			String fieldId = fieldIdItr.next();
			Object value = inputData.get(fieldId);
			value = normalize(value);
			if (value == null) {
				fieldIdItr.remove();
			}
		}

		JSONObject newInputData = new JSONObject();
		List<String> unusedFields = new ArrayList<String>();
		for (Object fieldId : inputData.keySet()) {
			Object value = inputData.get(fieldId);

			if (fieldsIdByName.containsKey(fieldId)) {
				fieldId = fieldsIdByName.get(fieldId.toString());
			}

			if (fieldsId.contains(fieldId) && (objectiveFieldId == null
					|| !fieldId.equals(objectiveFieldId))) {

				String optype = (String) Utils.getJSONObject(this.fields, fieldId + ".optype");
				if (!"datetime".equals(optype)) {
					newInputData.put(fieldId, value);
				} else {
					unusedFields.add((String) fieldId);
				}
			} else {
				unusedFields.add((String) fieldId);
			}
		}

		// Add the expanded dates in date_fields to the input_data
	    // provided by the user (only if the user didn't specify it)
		for (Object nameObj : datetimeFields.keySet()) {
			String name = (String) nameObj;
			Integer value = (Integer) datetimeFields.get(name);
			if (!newInputData.containsKey(name)) {
				newInputData.put(name, value);
			}
		}

		// We fill the input with the chosen default, if selected
		newInputData = fillNumericDefaults(newInputData);

		JSONObject result = new JSONObject();
		result.put("newInputData", newInputData);
		result.put("unusedFields", unusedFields);

		return result;
	}

	/**
	 * Tests if the fields names are unique. If they aren't, a transformation is
	 * applied to ensure unicity.
	 * 
	 * @param fields	the fields
	 */
	protected void uniquifyNames(JSONObject fields) {

		fieldsName = new ArrayList<String>(fields.size());
		fieldsId = new ArrayList<String>(fields.size());

		fieldsIdByName = new HashMap<String, String>();
		fieldsNameById = new HashMap<String, String>();

		for (Object fieldId : fields.keySet()) {
			fieldsId.add(fieldId.toString());

			String name = Utils
					.getJSONObject((JSONObject) fields.get(fieldId), "name")
					.toString();
			fieldsName.add(name);

			fieldsIdByName.put(name, fieldId.toString());
			fieldsNameById.put(fieldId.toString(), name);
		}

		Set<String> uniqueNames = new TreeSet<String>(fieldsName);
		if (uniqueNames.size() < fieldsName.size()) {
			transformRepeatedNames(fields);
		}
	}

	/**
	 * If a field name is repeated, it will be transformed adding its column
	 * number. If that combination is also a field name, the field id will be
	 * added.
	 * 
	 * @param fields	the fields
	 */
	protected void transformRepeatedNames(JSONObject fields) {
		Set<String> uniqueNames = new TreeSet<String>(fieldsName);
		fieldsName = new ArrayList<String>();
		fieldsIdByName = new HashMap<String, String>();
		fieldsNameById = new HashMap<String, String>();

		if (objectiveFieldId == null) {
			String name = Utils
					.getJSONObject(fields, objectiveFieldId + ".name")
					.toString();
			fieldsName.add(name);
			fieldsIdByName.put(name, objectiveFieldId);
			fieldsIdByName.put(objectiveFieldId, name);
		}

		for (String fieldId : fieldsId) {
			if (objectiveFieldId != null && fieldId.equals(objectiveFieldId)) {
				continue;
			}

			String name = Utils.getJSONObject(fields, fieldId + ".name")
					.toString();
			int columnNumber = ((Number) Utils.getJSONObject(fields,
					fieldId + ".column_number")).intValue();
			if (fieldsName.contains(name)) {
				name = String.format("%s%d", name, columnNumber);
				if (fieldsName.contains(name)) {
					name = String.format("%s_%d", name, fieldId);
				}

				((JSONObject) fields.get(fieldId)).put("name", name);
			}
			uniqueNames.add(name);
			fieldsName.add(name);
			fieldsIdByName.put(name, fieldId);
			fieldsIdByName.put(fieldId, name);
		}
	}

	/**
	 * Transforms to unicode and cleans missing tokens
	 *
	 * @param value	the value to normalize
	 * @param <T>	the class
	 *            
	 * @return	the normalized value
	 */
	protected <T> T normalize(T value) {
		// if( value instanceof String ) {
		return (missingTokens.contains(value) ? null : value);
		// }

		// return null;
	}

	protected Map<String, Object> uniqueTerms(Map<String, Object> inputData) {
		Map<String, Object> uniqueTerms = new HashMap<String, Object>();
		
		for (Object fieldId : termForms.keySet()) {
			if (inputData.containsKey(fieldId.toString())) {
				Object inputDataField = inputData.get(fieldId.toString());
				inputDataField = (inputDataField != null ? inputDataField : "");

				if (inputDataField instanceof String) {
					boolean caseSensitive = (Boolean) Utils.getJSONObject(
							termAnalysis, fieldId + ".case_sensitive",
							Boolean.TRUE);
					String tokenMode = (String) Utils.getJSONObject(
							termAnalysis, fieldId + ".token_mode", "all");

					List<String> terms = new ArrayList<String>();
					if (!Utils.TM_FULL_TERM.equals(tokenMode)) {
						terms = parseTerms(inputDataField.toString(),
								caseSensitive);
					}
					
					String fullTerm = (caseSensitive ? 
							inputDataField.toString() : 
							((String) inputDataField).toLowerCase());
					
					// We add fullTerm if needed. Note that when there's
                    // only one term in the input_data, fullTerm and term are
                    // equal. Then fullTerm will not be added to avoid
                    // duplicated counters for the term.
					if (Utils.TM_FULL_TERM.equals(tokenMode) || 
						terms.size() == 0 ||
						(Utils.TM_ALL.equals(tokenMode) && 
						!terms.get(0).equals(fullTerm))) {
						terms.add(fullTerm);
					}
					
					uniqueTerms.put(fieldId.toString(),
							uniqueTerms(terms,
									(JSONObject) termForms.get(fieldId),
									tagClouds.get(fieldId.toString())));
				} else {
					uniqueTerms.put(fieldId.toString(), inputDataField);
				}

				inputData.remove(fieldId.toString());
			}
		}
		
		// the same for items fields
		for (Object fieldId : itemAnalysis.keySet()) {

			if (inputData.containsKey(fieldId.toString())) {
				Object inputDataField = inputData.get(fieldId.toString());
				inputDataField = (inputDataField != null ? inputDataField : "");

				if (inputDataField instanceof String) {
					String separator = (String) Utils.getJSONObject(
							itemAnalysis, fieldId + ".separator", " ");
					String regexp = (String) Utils.getJSONObject(itemAnalysis,
							fieldId + ".separator_regexp", "");

					if (regexp == null) {
						regexp = StringEscapeUtils.escapeJava(separator);
					}
					if ("$".equals(regexp)) {
						regexp = "\\$";
					}

					List<String> terms = parseItems(inputDataField.toString(),
							regexp);

					uniqueTerms.put(fieldId.toString(), uniqueTerms(terms,
							new JSONObject(), items.get(fieldId.toString())));

				} else {
					uniqueTerms.put(fieldId.toString(), inputDataField);
				}

				inputData.remove(fieldId.toString());
			}
		}

		for (Object fieldId : categories.keySet()) {
			if (inputData.containsKey(fieldId.toString())) {
				Object inputDataField = inputData.get(fieldId.toString());
				inputDataField = (inputDataField != null ? inputDataField : "");
				JSONObject data = new JSONObject();
				data.put(inputDataField, 1);
				uniqueTerms.put(fieldId.toString(), data);
				inputData.remove(fieldId.toString());
			}

		}

		return uniqueTerms;
	}
	

	/**
	 * Extracts the unique terms that occur in one of the alternative forms in
	 * termForms or in the tag cloud.
	 * 
	 * @param terms			the list of terms to extract the info for
	 * @param termForms		the term forms
	 * @param tagClouds		list of tag cloud
	 * 
	 * @return a map with occurrences per unique term
	 */
	protected Map<String, Integer> uniqueTerms(List<String> terms,
			JSONObject termForms, List<String> tagClouds) {

		Map<String, String> extendForms = new HashMap<String, String>();
		for (Object term : termForms.keySet()) {
			JSONArray forms = (JSONArray) termForms.get(term);
			for (Object form : forms) {
				extendForms.put(form.toString(), term.toString());
			}
			extendForms.put(term.toString(), term.toString());
		}

		Map<String, Integer> termsSet = new HashMap<String, Integer>();
		for (Object term : terms) {

			if (tagClouds.indexOf(term.toString()) != -1) {
				if (!termsSet.containsKey(term.toString())) {
					termsSet.put(term.toString(), 0);
				}
				Integer value = termsSet.get(term.toString());
				termsSet.put(term.toString(), value + 1);
			} else if (extendForms.containsKey(term.toString())) {
				term = extendForms.get(term.toString());
				if (!termsSet.containsKey(term.toString())) {
					termsSet.put(term.toString(), 0);
				}
				Integer value = termsSet.get(term.toString());
				termsSet.put(term.toString(), value + 1);
			}
		}

		return termsSet;
	}

	/**
	 * Returns the list of parsed terms
	 * 
	 * @param text			the text to parse
	 * @param caseSensitive	if use case sensitive parsing or not
	 * 
	 * @return the list of parsed terms
	 */
	protected List<String> parseTerms(String text, Boolean caseSensitive) {
		if (caseSensitive == null) {
			caseSensitive = Boolean.TRUE;
		}

		List<String> terms = new ArrayList<String>();

		String expression = "(\\b|_)([^\b_\\s]+?)(\\b|_)";

		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(text);
		// check all occurrence
		while (matcher.find()) {
			String term = matcher.group();
			terms.add((caseSensitive ? term : term.toLowerCase()));
		}

		return terms;
	}

	/**
	 * Returns the list of parsed items
	 * 
	 * @param text		the text to parse
	 * @param regexp	the regexp to use to parse items
	 * 
	 * @return the list of parsed items
	 */
	protected List<String> parseItems(String text, String regexp) {
		if (text != null) {
			return Arrays.asList(text.split(regexp));
		}
		return null;
	}

	public List<String> getMissingTokens() {
		return missingTokens;
	}

	public JSONObject getFields() {
		return fields;
	}
	
	/**
	 * Returns reg expression for model Id.
	 * 
	 * @return regep for model Id
	 */
	public abstract String getModelIdRe();
	
	/**
	 * Returns JSONObject resource.
	 * 
	 * @param modelId	the id of the model
	 * 
	 * @return the JSONObject resource
	 */
	public abstract JSONObject getBigMLModel(String modelId);
}
