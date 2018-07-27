package org.bigml.binding;

import org.apache.commons.text.StringEscapeUtils;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ModelFields resource.
 * 
 * This module defines a ModelFields class to hold the information 
 * associated to the fields of the model resource in BigML.
 * It becomes the starting point for the Model class, that is used for 
 * local predictions.
 * 
 */
public class ModelFields implements Serializable {

    private static final long serialVersionUID = 1L;

    // Logging
    Logger LOGGER = LoggerFactory.getLogger(ModelFields.class);

    private static String DEFAULT_LOCALE = "en_US.UTF-8";
    
    public static String[] DEFAULT_MISSING_TOKENS = Fields.DEFAULT_MISSING_TOKENS;
    
    public static HashMap<String, String> FIELDS_PARENT = 
    		new HashMap<String, String>();
    static {
    	FIELDS_PARENT.put("cluster", "clusters");
    	FIELDS_PARENT.put("logisticregression", "logistic_regression");
    	FIELDS_PARENT.put("ensemble", "ensemble");
    	FIELDS_PARENT.put("deepnet", "deepnet");
    }
    

    protected String objectiveFieldId;
    protected String objectiveFieldName;
    protected List<String> fieldsName;
    protected List<String> fieldsId;
    protected Map<String, String> fieldsIdByName;
    protected Map<String, String> fieldsNameById;

    protected List<String> missingTokens;
    protected JSONObject fields = null;
    protected JSONObject invertedFields = null;
    protected String dataLocale = null;
    
    protected Boolean missingNumerics = null;
    protected JSONObject termForms = new JSONObject();
    protected Map<String, List<String>> tagClouds = 
    		new HashMap<String, List<String>>();
    protected JSONObject termAnalysis = new JSONObject();
    protected JSONObject itemAnalysis = new JSONObject();
    protected Map<String, List<String>> items = 
    		new HashMap<String, List<String>>();
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
     * The constructor can be instantiated with the fields structure.
     * The structure is checked and fields structure is returned if a resource type is matched.
     *
     * @param fields the resource that hold the fields structure
     */
    public ModelFields(JSONObject fields) {
        initialize(fields, null, null, null);
    }

    /**
     * The constructor can be instantiated with the fields structure.
     * The structure is checked and fields structure is returned if a resource type is matched.
     *
     * @param fields the resource that hold the fields structure
     */
    public ModelFields(JSONObject fields, String objectiveFieldId, String dataLocale,
                       List<String> missingTokens) {
        initialize(fields, objectiveFieldId, dataLocale, missingTokens);
    }

    /**
     * The constructor can be instantiated with fields structure.
     *
     * @param fields the fields structure itself
     * @param objectiveFieldId the ID of the objective field
     * @param missingTokens the list of missing tokens to use. DEFAULT_MISSING_TOKENS will be used by default
     * @param dataLocale the locale of the data
     */
    protected void initialize(
    		JSONObject fields, String objectiveFieldId, String dataLocale,
            List<String> missingTokens) {
    	
    	initialize(fields, objectiveFieldId, dataLocale, missingTokens,
    			   false, false, false);
    }
    
    /**
     * The constructor can be instantiated with fields structure.
     *
     * @param fields the fields structure itself
     * @param objectiveFieldId the ID of the objective field
     * @param missingTokens the list of missing tokens to use. DEFAULT_MISSING_TOKENS will be used by default
     * @param dataLocale the locale of the data
     */
    protected void initialize(JSONObject fields, String objectiveFieldId, 
    		String dataLocale, List<String> missingTokens, Boolean terms, 
    		Boolean categories, Boolean numerics) {

        this.fields = new JSONObject();
        this.fields.putAll(fields);

        this.objectiveFieldId = objectiveFieldId;
        if( this.objectiveFieldId != null ) {
            this.objectiveFieldName = Utils.getJSONObject(
            		fields, objectiveFieldId + ".name").toString();
        }

        uniquifyNames(this.fields);
        this.invertedFields = Utils.invertDictionary(fields, "name");

        this.missingTokens = missingTokens;
        if( this.missingTokens == null ) {
            this.missingTokens = new ArrayList<String>(
            		Arrays.asList(DEFAULT_MISSING_TOKENS));
        }
        
        this.dataLocale = dataLocale;
        if( this.dataLocale == null ) {
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
            	termForms.put(fieldId, 
            		Utils.getJSONObject(field, "summary.term_forms", new JSONObject()));
            	
            	List<String> fieldTagClouds = new ArrayList<String>();
            	JSONArray tags = (JSONArray) Utils.getJSONObject(field, "summary.tag_cloud", new JSONArray());
                for (Object tag : tags) {
                	JSONArray tagArr = (JSONArray) tag;
                	fieldTagClouds.add(tagArr.get(0).toString());
                }
                tagClouds.put(fieldId.toString(), fieldTagClouds);
            	
            	termAnalysis.put(fieldId, Utils.getJSONObject(field, "term_analysis", new JSONObject()));
            }
            
            if ("items".equals(field.get("optype"))) {
                List<String> fieldItems = new ArrayList<String>();
                JSONArray itemsArray = (JSONArray) Utils.getJSONObject(field, "summary.items", new JSONArray());
                for (Object item : itemsArray) {
                	JSONArray itemArr = (JSONArray) item;
                	fieldItems.add(itemArr.get(0).toString());
                }
                items.put(fieldId.toString(), fieldItems);
            	
            	itemAnalysis.put(fieldId, Utils.getJSONObject(field, "item_analysis", new JSONObject()));
            }
            
            if (categories && "categorical".equals(field.get("optype"))) {
            	JSONArray cats = (JSONArray) Utils.getJSONObject(
            			field, "summary.categories", new JSONArray());
            	
            	JSONArray categoriesList = new JSONArray();
            	for (Object category : cats) {
            		categoriesList.add(((JSONArray) category).get(0));
            	}
            	this.categories.put(fieldId, categoriesList);
            }
            
            if (numerics && this.missingNumerics != null &&
            		"numeric".equals(field.get("optype"))) {
            	this.numericFields.put(fieldId, true);
            }

        }
    }
    

    /**
     * Checks the model structure to see if it contains all the needed keys
     * 
     */
    protected boolean checkModelStructure(JSONObject model) {
    	return checkModelStructure(model, "model");
    }
    
    
    /**
     * Checks the model structure to see if it contains all the needed keys
     */
    protected boolean checkModelStructure(JSONObject model, String innerKey) {
        return model.containsKey("resource") && 
        		model.get("resource") != null &&
                (model.containsKey("object") &&
                    Utils.getJSONObject(model, "object." + innerKey, null) != null ||
                    model.containsKey(innerKey) );
    }
    
    /**
     * Checks the model structure to see whether it contains the required
     * fields information
     */
    protected boolean checkModelFields(JSONObject model) {
    	if (!model.containsKey("resource") ||
        		model.get("resource") == null) {
    		return false;
    	}
    	
    	String resource = (String) model.get("resource");
    	String innerKey = "model";
    	if (FIELDS_PARENT.containsKey(resource.split("/")[0])) {
    		innerKey = FIELDS_PARENT.get(resource.split("/")[0]);
    	}
    	
    	if (checkModelStructure(model, innerKey)) {
    		model = (JSONObject) Utils.getJSONObject(model, "object", model);
    		
    		JSONObject modelObj = (JSONObject) Utils.getJSONObject(
    				model, innerKey, new JSONObject());
    		
    		JSONObject fields = (JSONObject) Utils.getJSONObject(
    				model, "fields", modelObj.get("fields"));
    		
    		// models only need model_fields to work. The rest of 
    		// resources will need all fields to work
    		JSONObject modelFields = (JSONObject) modelObj.get("model_fields");
    		if (modelFields == null) {
    			JSONObject fieldsMeta = (JSONObject) Utils.getJSONObject(
        				model, "fields_meta", modelObj.get("fields_meta"));
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
     * Filters the keys given in input_data checking against model fields.
     *
     * @param inputData
     * @return
     */
    protected JSONObject filterInputData(JSONObject inputData) {
    	JSONObject filteredInputData = filterInputData(inputData, false);
    	return (JSONObject) filteredInputData.get("newInputData");
    }
    
    
    /**
     * Filters the keys given in input_data checking against model fields.
     * 
     * If `addUnusedFields` is set to True, it also provides 
     * information about the ones that are not used.
     *
     * @param inputData
     * @param addUnusedFields
     * @return
     */
    protected JSONObject filterInputData(JSONObject inputData, 
    									 Boolean addUnusedFields) {
    	
    	if (addUnusedFields == null) {
    		addUnusedFields = false;
    	}
    	
    	// remove all missing values
    	Iterator<String> fieldIdItr = inputData.keySet().iterator();
        while(fieldIdItr.hasNext()) {
            String fieldId = fieldIdItr.next();
            Object value = inputData.get(fieldId);
            value = normalize(value);
            if( value == null ) {
                fieldIdItr.remove();
            }
        }

        JSONObject newInputData = new JSONObject();
        List<String> unusedFields = new ArrayList<String>();
        for (Object fieldId : inputData.keySet()) {
            Object value = inputData.get(fieldId);

            if( fieldsIdByName.containsKey(fieldId) ) {
                fieldId = fieldsIdByName.get(fieldId.toString());
            }
            
            if( fieldsId.contains(fieldId) &&
                    (objectiveFieldId == null ||
                            !fieldId.equals(objectiveFieldId)) ) {
                newInputData.put(fieldId, value);
            } else {
            	unusedFields.add((String) fieldId);
            }
        }
        
        JSONObject result = new JSONObject();
        result.put("newInputData", newInputData);
        result.put("unusedFields", unusedFields);

        return result;
    }

    /**
     * Tests if the fields names are unique. If they aren't, a
     * transformation is applied to ensure unicity.
     */
    protected void uniquifyNames(JSONObject fields) {

        fieldsName = new ArrayList<String>(fields.size());
        fieldsId = new ArrayList<String>(fields.size());

        fieldsIdByName = new HashMap<String, String>();
        fieldsNameById = new HashMap<String, String>();

        for (Object fieldId : fields.keySet()) {
            fieldsId.add(fieldId.toString());

            String name = Utils.getJSONObject((JSONObject)
                    fields.get(fieldId), "name").toString();
            fieldsName.add(name);

            fieldsIdByName.put(name, fieldId.toString());
            fieldsNameById.put(fieldId.toString(), name);
        }

        Set<String> uniqueNames = new TreeSet<String>(fieldsName);
        if( uniqueNames.size() < fieldsName.size() ) {
            transformRepeatedNames(fields);
        }
    }

    /**
     * If a field name is repeated, it will be transformed adding its
     * column number. If that combination is also a field name, the
     * field id will be added.
     */
    protected void transformRepeatedNames(JSONObject fields) {
        Set<String> uniqueNames = new TreeSet<String>(fieldsName);
        fieldsName = new ArrayList<String>();
        fieldsIdByName = new HashMap<String, String>();
        fieldsNameById = new HashMap<String, String>();

        if( objectiveFieldId == null ) {
            String name = Utils.getJSONObject(fields, objectiveFieldId + ".name").toString();
            fieldsName.add( name );
            fieldsIdByName.put(name, objectiveFieldId);
            fieldsIdByName.put(objectiveFieldId, name);
        }

        for (String fieldId : fieldsId) {
            if( objectiveFieldId != null && fieldId.equals(objectiveFieldId) ) {
                continue;
            }

            String name = Utils.getJSONObject(fields, fieldId + ".name").toString();
            int columnNumber = ((Number) Utils.getJSONObject(fields, fieldId + ".column_number")).intValue();
            if( fieldsName.contains(name) ) {
                name = String.format("%s%d", name, columnNumber);
                if( fieldsName.contains(name) ) {
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
     * @param value the value to normalize
     */
    protected <T> T normalize(T value) {
//        if( value instanceof String ) {
            return (missingTokens.contains(value) ? null : value);
//        }

//        return null;
    }
    

//    /**
//     * Strips prefixes and suffixes if present
//     */
//    public Object stripAffixes(String value, JSONObject field) {
//
//        if( field.containsKey("prefix") &&
//                value.startsWith(field.get("prefix").toString()) ) {
//            value =  value.substring(field.get("prefix").toString().length(),
//                    value.length());
//        }
//
//        if( field.containsKey("suffix") &&
//                value.endsWith(field.get("suffix").toString()) ) {
//            value =  value.substring(0,
//                    value.length() - field.get("suffix").toString().length());
//        }
//
//        return value;
//    }
    
    /**
     * Parses the input data to find the list of unique terms in the
     * tag cloud
     */
    protected Map<String, Object> uniqueTerms(Map<String, Object> inputData) {
    	Map<String, Object> uniqueTerms = new HashMap<String, Object>();
        for (Object fieldId : termForms.keySet()) {
        	
            if( inputData.containsKey(fieldId.toString()) ) {
                Object inputDataField = inputData.get(fieldId.toString());
                inputDataField = (inputDataField != null ? inputDataField : "");

                if( inputDataField instanceof String ) {
                    boolean caseSensitive = (Boolean) Utils.getJSONObject(termAnalysis,
                            fieldId + ".case_sensitive", Boolean.TRUE);
                    String tokenMode = (String) Utils.getJSONObject(termAnalysis,
                            fieldId + ".token_mode", "all");

                    List<String> terms = new ArrayList<String>();
                    if( !Utils.TM_FULL_TERM.equals(tokenMode) ) {
                        terms = parseTerms(inputDataField.toString(), caseSensitive);
                    }

                    if( !Utils.TM_TOKENS.equals(tokenMode) ) {
                        terms.add((caseSensitive ? inputDataField.toString() :
                                ((String) inputDataField).toLowerCase()));
                    }
                    uniqueTerms.put(fieldId.toString(), uniqueTerms(terms,
                            (JSONObject) termForms.get(fieldId),
                            tagClouds.get(fieldId.toString())) );
                } else {
                    uniqueTerms.put(fieldId.toString(), inputDataField);
                }

                inputData.remove(fieldId.toString());
            }   
        }
        

        //the same for items fields
        for (Object fieldId : itemAnalysis.keySet()) {
        	
        	if( inputData.containsKey(fieldId.toString()) ) {
                Object inputDataField = inputData.get(fieldId.toString());
                inputDataField = (inputDataField != null ? inputDataField : "");
                
                if (inputDataField instanceof String) {
                	String separator = (String) Utils.getJSONObject(
                			itemAnalysis, fieldId + ".separator", " ");
                	String regexp = (String) Utils.getJSONObject(
                			itemAnalysis, fieldId + ".separator_regexp", "");
                	
                	if (regexp == null) {
                		regexp = StringEscapeUtils.escapeJava(separator);
                	}
                	if ("$".equals(regexp)) {
                		regexp = "\\$";
                	}
                	
                	List<String> terms = parseItems(
                			inputDataField.toString(), regexp);
                	
                	uniqueTerms.put(fieldId.toString(), 
                			uniqueTerms(terms,
                						new JSONObject(),
                						items.get(fieldId.toString())) );
                	
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
                data .put(inputDataField, 1);
                uniqueTerms.put(fieldId.toString(), data);
                inputData.remove(fieldId.toString());
    		}
    		
    	}
    	
        return uniqueTerms;
    }
    
    /**
     * Extracts the unique terms that occur in one of the alternative forms in
     *  term_forms or in the tag cloud.
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
    		
    		if( tagClouds.indexOf(term.toString()) != -1) {
    			if (!termsSet.containsKey(term.toString())) {
    				termsSet.put(term.toString(), 0);
    			}
    			Integer value = termsSet.get(term.toString());
    			termsSet.put(term.toString(), value+1);
    		} else if( extendForms.containsKey(term.toString()) ) {
    			term = extendForms.get(term.toString());
    			if (!termsSet.containsKey(term.toString())) {
    				termsSet.put(term.toString(), 0);
    			}
    			Integer value = termsSet.get(term.toString());
    			termsSet.put(term.toString(), value+1);
            }
    	}

        return termsSet;
    }

    /**
     * Returns the list of parsed terms
     */
    protected List<String> parseTerms(String text, Boolean caseSensitive) {
        if( caseSensitive == null ) {
            caseSensitive = Boolean.TRUE;
        }

        List<String> terms = new ArrayList<String>();

        String expression = "(\\b|_)([^\b_\\s]+?)(\\b|_)";

        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(text);
        // check all occurrence
        while (matcher.find()) {
            String term = matcher.group();
            terms.add( (caseSensitive ? term : term.toLowerCase()) );
        }

        return terms;
    }
    
    /**
     * Returns the list of parsed items
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
}
