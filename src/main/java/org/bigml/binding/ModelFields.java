package org.bigml.binding;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A lightweight wrapper of the field information in the model, cluster
 * or anomaly objects
 */
public class ModelFields {

    // Logging
    Logger LOGGER = LoggerFactory.getLogger(ModelFields.class);

    public static String[] DEFAULT_MISSING_TOKENS = Fields.DEFAULT_MISSING_TOKENS;

    protected String objectiveFieldId;
    protected String objectiveFieldName;
    protected List<String> fieldsName;
    protected List<String> fieldsId;
    protected Map<String, String> fieldsIdByName;
    protected Map<String, String> fieldsNameById;

    protected String locale;
    protected List<String> missingTokens;
    protected JSONObject fields = null;
    protected JSONObject invertedFields = null;

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
     * The constructor can be instantiated with fields structure.
     *
     * @param fields the fields structure itself
     * @param objectiveFieldId the ID of the objective field
     * @param missingTokens the list of missing tokens to use. DEFAULT_MISSING_TOKENS will be used by default
     * @param dataLocale the locale of the data
     */
    public void initialize(JSONObject fields, String objectiveFieldId, String dataLocale,
                       List<String> missingTokens) {

        this.fields = new JSONObject();
        this.fields.putAll(fields);

        this.objectiveFieldId = objectiveFieldId;
        if( this.objectiveFieldId != null ) {
            this.objectiveFieldName = Utils.getJSONObject(fields, objectiveFieldId + ".name").toString();
        }

        uniquifyNames(this.fields);
        this.invertedFields = Utils.invertDictionary(fields, "name");

        this.locale = dataLocale;
        this.missingTokens = missingTokens;

        if( this.locale == null ) {
            locale = BigMLClient.DEFAUL_LOCALE.toString();
        }

        if( this.missingTokens == null ) {
            this.missingTokens = new ArrayList<String>(Arrays.asList(DEFAULT_MISSING_TOKENS));
        }
    }

    /**
     * Filters the keys given in input_data checking against model fields
     */
    public JSONObject filterInputData(Map<String, Object> inputData, Boolean byName) {

        Iterator<String> fieldIdItr = inputData.keySet().iterator();
        while(fieldIdItr.hasNext()) {
            String fieldId = fieldIdItr.next();
            Object value = inputData.get(fieldId);
            value = normalize(value);
            if( value == null ) {
                fieldIdItr.remove();
            }
        }

//        if( byName ) {
        // We no longer check that the input data keys match some of
        // the dataset fields. We only remove the keys that are not
        // used as predictors in the model
        JSONObject newInputData = new JSONObject();

        for (Object fieldId : inputData.keySet()) {
            Object value = inputData.get(fieldId);

            if( byName ) {
                fieldId = fieldsIdByName.get(fieldId.toString());
            }

            if( fieldsId.contains(fieldId) &&
                    (objectiveFieldId == null ||
                            !fieldId.equals(objectiveFieldId)) ) {
                newInputData.put(fieldId, value);
            }
        }

        return newInputData;
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
     * Cleans missing tokens
     *
     * @param value the value to normalize
     */
    public Object normalize(Object value) {
//        if( value instanceof String ) {
            return (missingTokens.contains(value) ? null : value);
//        }

//        return null;
    }

    /**
     * Checks expected type in input data values, strips affixes and casts
     */
    public Map<String, Object> cast(Map<String, Object> inputData) {
        for (String fieldId : inputData.keySet()) {
            Object value = inputData.get(fieldId);

            JSONObject field = (JSONObject) fields.get(fieldId);

            String optType = (String) Utils.getJSONObject(field, "optype");

            if( ("numeric".equals(optType) && value instanceof String) ||
                    (!"numeric".equals(optType) && !(value instanceof String)) ) {

                if( "numeric".equals(optType) ) {
                    value = stripAffixes(value.toString(), field);
                    inputData.put(fieldId, Double.parseDouble(value.toString()));
                } else if( "categorical".equals(optType) || "text".equals(optType) ) {
                    inputData.put(fieldId, Double.parseDouble(value.toString()));
                } else {
                    throw new IllegalStateException(
                            String.format("Mismatch input data type in field " +
                            "\"%s\" for value %s.", field.get("name"), value.toString()));
                }

            }
        }

        return inputData;
    }


    /**
     * Strips prefixes and suffixes if present
     */
    public Object stripAffixes(String value, JSONObject field) {

        if( field.containsKey("prefix") &&
                value.startsWith(field.get("prefix").toString()) ) {
            value =  value.substring(field.get("prefix").toString().length(),
                    value.length());
        }

        if( field.containsKey("suffix") &&
                value.endsWith(field.get("suffix").toString()) ) {
            value =  value.substring(0,
                    value.length() - field.get("suffix").toString().length());
        }

        return value;
    }

}
