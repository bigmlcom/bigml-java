package org.bigml.binding;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.resources.Dataset;
import org.bigml.binding.resources.Model;
import org.bigml.binding.resources.Source;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A class to deal with the fields of a resource.
 *
 * This module helps to map between ids, names, and column_numbers in the
 * fields of source, dataset, or model. Also to validate your input data
 * for predictions or to list all the fields from a resource.
 *
 * from bigml.api import BigML
 * from bigml.fields import Fields
 *
 * api = BigML()
 *
 * source = api.get_source("source/50a6bb94eabcb404d3000174")
 * fields = Fields(source['object']['fields'])
 *
 * dataset = api.get_dataset("dataset/50a6bb96eabcb404cd000342")
 * fields = Fields(dataset['object']['fields'])
 *
 * # Note that the fields in a model come one level deeper
 * model = api.get_model("model/50a6bbac035d0706db0008f8")
 * fields = Fields(model['object']['model']['fields'])
 *
 * prediction = api.get_prediction("prediction/50a69688035d0706dd00044d")
 * fields =  Fields(prediction['object']['fields'])
 */
public class Fields {

    // Logging
    Logger LOGGER = LoggerFactory.getLogger(Fields.class);

    protected static Class[] RESOURCES_WITH_FIELDS = new Class[] { Source.class, Dataset.class, Model.class};

    public static String[] DEFAULT_MISSING_TOKENS = {
            "", "N/A", "n/a", "NULL", "null", "-", "#DIV/0",
            "#REF!", "#NAME?", "NIL", "nil", "NA", "na",
            "#VALUE!", "#NULL!", "NaN", "#N/A", "#NUM!", "?"
    };

    private Locale locale;
    private JSONObject fields = null;
    private JSONObject fieldsByName = null;
    private Map<Long, String> fieldsByColumnNumber = null;
    private List<String> missingTokens = null;
    private List<Long> fieldsColumns = null;
    private List<String> filteredFields = null;

    private List<String> rowIds;
    private List<String> headers;
    private Object objectiveField;
    private Boolean objectiveFieldPresent;
    private List<Long> filteredIndexes;

    /**
     * Returns the field structure for a resource, its locale and
     *  missing_tokens
     *
     * @param resource the BigML resource object
     */
    public static FieldsStructure getFieldsStructure(JSONObject resource){
        AbstractResource resourceInstance = null;
        for (Class resourceClass : RESOURCES_WITH_FIELDS) {
            try {
                AbstractResource checkingResource = (AbstractResource) resourceClass.newInstance();
                if( checkingResource.isInstance(resource) ) {
                    resourceInstance = checkingResource;
                }
            } catch (Exception e) {
                // Never happen
            }
        }

        FieldsStructure fieldsStructure = new FieldsStructure();
        if( resourceInstance != null ) {
            if( resourceInstance instanceof Source ) {

                fieldsStructure.setLocale(Utils.findLocale((String)
                        Utils.getJSONObject(resource, "object.source_parser.locale"), true));
                fieldsStructure.setMissingTokens((JSONArray) Utils.getJSONObject(resource,
                        "object.source_parser.missing_tokens"));
            } else {
                fieldsStructure.setLocale(Utils.findLocale((String)
                        Utils.getJSONObject(resource, "object.locale"), true));
                fieldsStructure.setMissingTokens((JSONArray) Utils.getJSONObject(resource,
                        "object.missing_tokens"));
            }

            if( resourceInstance instanceof Model ) {
                fieldsStructure.setFields((JSONObject)
                        Utils.getJSONObject(resource, "object.model.fields"));
            } else {
                fieldsStructure.setFields((JSONObject)
                        Utils.getJSONObject(resource, "object.fields"));
            }
        }

        return fieldsStructure;
    }


    /**
     * The constructor can be instantiated with resources or a fields
     * structure. The structure is checked and fields structure is returned
     * if a resource type is matched.
     *
     * @param resourceOrField the resource that hold the fields or the fields itself
     */
    public Fields(JSONObject resourceOrField) {
        this(resourceOrField, null, null, false, null, false, null);
    }

    /**
     * The constructor can be instantiated with resources or a fields
     * structure. The structure is checked and fields structure is returned
     * if a resource type is matched.
     *
     * @param resourceOrField the resource that hold the fields or the fields itself
     * @param missingTokens the list of missing tokens to use. DEFAULT_MISSING_TOKENS will be used by default
     * @param dataLocale the locale of the data
     * @param verbose
     * @param objectiveField the name of the objective field
     * @param objectiveFieldPresent if the objetive field is present in the fields
     * @param includeFields the fields to be included if we only want only a subset
     */
    public Fields(JSONObject resourceOrField, List<String> missingTokens, String dataLocale,
                  Boolean verbose, Object objectiveField, Boolean objectiveFieldPresent,
                  List<Long> includeFields) {


        // We first check if the argument is a resource instance
        if( resourceOrField.containsKey("resource") ) {
            FieldsStructure fieldsStructure = getFieldsStructure(resourceOrField);
            this.fields = fieldsStructure.getFields();
            Locale resourceLocale = fieldsStructure.getLocale();
            JSONArray resourceMissingTokens = fieldsStructure.getMissingTokens();

            if( dataLocale == null ) {
                dataLocale = resourceLocale.toString();
            }

            if( missingTokens == null ) {
                missingTokens = resourceMissingTokens;
            }

        } else {
            // If the resource structure is not in the expected set, fields
            //  structure is assumed
            this.fields = resourceOrField;
            if( dataLocale == null ) {
                dataLocale = BigMLClient.DEFAUL_LOCALE.toString();
            }

            if( missingTokens == null ) {
                missingTokens = new ArrayList<String>(
                        Arrays.asList(DEFAULT_MISSING_TOKENS));
            }
        }

        if( this.fields == null ) {
            throw new IllegalStateException("No fields structure was found.");
        }

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug(String.format("resource_or_fields: %s", JSONObject.toJSONString(resourceOrField)));
            LOGGER.debug(String.format("missing_tokens: %s", Arrays.toString(missingTokens.toArray())));
            LOGGER.debug(String.format("data_locale: %s", dataLocale.toString()));
            LOGGER.debug(String.format("objective_field: %s", objectiveField));
            LOGGER.debug(String.format("objective_field_present: %s", objectiveFieldPresent));
        }

        this.fieldsByName = Utils.invertDictionary(this.fields, "name");
        JSONObject fieldsByColumnNumberTmp = Utils.invertDictionary(this.fields, "column_number");

        this.fieldsByColumnNumber = new HashMap<Long, String>();
        for (Object columnNumber : fieldsByColumnNumberTmp.keySet()) {
            fieldsByColumnNumber.put((Long) columnNumber,
                    ((JSONObject) fieldsByColumnNumberTmp.get(columnNumber)).get("fieldID").toString());
        }

        this.locale = Utils.findLocale(dataLocale, true);

        this.missingTokens = missingTokens;

        this.fieldsColumns = new ArrayList<Long>(fieldsByColumnNumber.keySet());
        Collections.sort(this.fieldsColumns);

        // Ids of the fields to be included
        this.filteredFields = new ArrayList<String>();
        if( includeFields != null ) {
            for (Object fieldName : this.fields.keySet()) {
                if( includeFields.contains( fieldName.toString() ) ) {
                    this.filteredFields.add( fieldName.toString() );
                }
            }
        }

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug(String.format("fields: %s", Arrays.toString(fieldsColumns.toArray())));
            LOGGER.debug(String.format("fields_by_column_number: %s", JSONObject.toJSONString(fieldsByColumnNumber)));
            LOGGER.debug(String.format("missing_tokens: %s", Arrays.toString(missingTokens.toArray())));
            LOGGER.debug(String.format("data_locale: %s", dataLocale.toString()));
        }

        // To be updated in update_objective_field
        this.rowIds = null;
        this.headers = null;
        this.objectiveField = null;
        this.objectiveFieldPresent = null;
        this.filteredIndexes = null;

        updateObjectiveField(objectiveField, objectiveFieldPresent, headers);

    }

    /**
     * Updates objective_field and headers info
     *
     * Permits to update the objective_field, objective_field_present and
     * headers info from the constructor and also in a per row basis.
     *
     * @param objectiveField the index of the objective field
     * @param objectiveFieldPresent if the objective field is present in the fields list
     */
    protected void updateObjectiveField(Object objectiveField, Boolean objectiveFieldPresent,
                                        List<String> headers) {

        // If no objective field, select the last column, else store its column
        if( objectiveField == null ) {
            this.objectiveField = fieldsColumns.get(fieldsColumns.size() - 1);
        } else if( objectiveField instanceof String ) {
            this.objectiveField = getFieldColumnNumber(objectiveField.toString());
        } else {
            this.objectiveField = objectiveField;
        }

        String objectiveFieldID = fieldsByColumnNumber.get(this.objectiveField);
        filteredFields.remove(objectiveFieldID);

        rowIds = new ArrayList<String>();

        this.objectiveFieldPresent = objectiveFieldPresent;
        if( headers == null ) {
            // The row is supposed to contain the fields sorted by column number
            for (Long fieldColumnIndex : fieldsColumns) {
                if( !fieldColumnIndex.equals(this.objectiveField) ) {
                    rowIds.add(fieldsByColumnNumber.get(fieldColumnIndex));
                }
            }

            this.headers = this.rowIds;
        } else {
            this.rowIds = new ArrayList<String>(headers.size());
            for (String header : headers) {
                this.rowIds.add(getFieldId(header));
            }
            this.headers = new ArrayList<String>(headers);
        }

        filteredIndexes = new ArrayList<Long>();
        for (String filteredField : filteredFields) {
            long index = rowIds.indexOf(filteredField);
            filteredIndexes.add(index);
        }
    }

    /**
     * Returns the list of columns Ids
     */
    public List<String> getColumnsIds() {
        return new ArrayList<String>(fields.keySet());
    }

    /**
     * Returns the list of columns Names
     */
    public List<String> getColumnsNames() {
        return new ArrayList<String>(fieldsByName.keySet());
    }

    /**
     * Returns the field object using its fieldID
     */
    public JSONObject getFieldById(String fieldID) {
        return (JSONObject) fields.get(fieldID);
    }

    /**
     * Returns the field object using its Name
     */
    public JSONObject getFieldByName(String fieldName) {
        return (JSONObject) fieldsByName.get(fieldName);
    }

    /**
     * Returns a field id
     *
     * @param fieldName the field key
     */
    public String getFieldId(String fieldName) {
        JSONObject field = (JSONObject) fieldsByName.get(fieldName);
        if( field == null ) {
            field = (JSONObject) fields.get(fieldName);
        }
        return (field != null ? field.get("fieldID").toString() : null);
    }

    /**
     * Returns a field name
     *
     * @param fieldID the field ID
     */
    public String getFieldName(String fieldID) {
        JSONObject field = (JSONObject) fields.get(fieldID);
        return (field != null ? field.get("name").toString() : null);
    }

    /**
     * Returns a field column number
     *
     * @param fieldID the field key
     */
    public Long getFieldColumnNumber(String fieldID) {
        JSONObject field = (JSONObject) fields.get(fieldID);
        if( field == null ) {
            field = (JSONObject) fieldsByName.get(fieldID);
        }

        return (field != null ? (Long) field.get("column_number") : null);
    }

    /**
     * Returns the number of fields
     */
    public int getLength() {
        return fields.size();
    }

    /**
     * Lists a description of the fields
     *
     * @param out the string builder used to append the fields description
     *              to already existent text
     */
    public StringBuilder listFields(StringBuilder out) {
        out = (out != null ? out : new StringBuilder());

        for (Long fieldIndex : fieldsColumns) {
            String fieldID = fieldsByColumnNumber.get(fieldIndex);
            JSONObject field = (JSONObject) fields.get(fieldID);

            out.append(String.format("[%-32s: %-16s: %-8s]\\n", field.get("name"),
                    field.get("optype"), fieldIndex));
        }

        return out;
    }

    /**
     * Returns fields where attribute preferred is set to True or where
     *  it isn't set at all.
     */
    public Map<String, JSONObject> getPreferredFields() {
        Map<String, JSONObject> preferredFields = new HashMap<String, JSONObject>();

        for (Object fieldObj : fields.values()) {
            JSONObject field = (JSONObject) fieldObj;
            if( !field.containsKey("preferred") || ((Boolean) field.get("preferred"))) {
                preferredFields.put(field.get("fieldID").toString(), field);
            }
        }

        return preferredFields;
    }

    /**
     * Pairs a list of values with their respective field ids.
     *
     *
     *
     * @param row
     * @param headers
     * @param objectiveField is the column_number of the objective field.
     * @param objectiveFieldPresent   must be True is the objective_field column
     *                                      is present in the row.
     */
    public Map<String, Object> pair(JSONArray row, List<String> headers,
                                    Object objectiveField, Boolean objectiveFieldPresent) {

        if( objectiveFieldPresent == null ) {
            objectiveFieldPresent = false;
        }

        // Try to get objective field form Fields or use the last column
        if( objectiveField == null ) {
            if( this.objectiveField == null ) {
                objectiveField= fieldsColumns.get(fieldsColumns.size() - 1);
            } else {
                objectiveField = this.objectiveField;
            }
        }

        // If objective fields is a name or an id, retrieve column number
        if( objectiveField instanceof String ) {
            objectiveField = getFieldColumnNumber(objectiveField.toString());
        }


        // Try to guess if objective field is in the data by using headers or
        // comparing the row length to the number of fields
        if( objectiveFieldPresent == null ) {
            if( headers != null ) {
                String fieldName = getFieldName(fieldsByColumnNumber.get(objectiveField));
                objectiveFieldPresent = headers.contains(fieldName);
            } else {
                objectiveFieldPresent = row.size() == getLength();
            }
        }

        // If objective field, its presence or headers have changed, update
        if( !objectiveField.equals(this.objectiveField) ||
                objectiveFieldPresent != this.objectiveFieldPresent ||
                (headers != null && !headers.equals(this.headers)) ) {
            updateObjectiveField(objectiveField, objectiveFieldPresent, headers);
        }

        JSONArray normalizedRow = new JSONArray();
        for (Object rowValue : row) {
            normalizedRow.add(normalize(rowValue));
        }

        return toInputData(normalizedRow);
    }


    /**
     * Builds dict with field, value info only for the included headers
     *
     * @param row the input row with values
     */
    public Map<String, Object> toInputData(JSONArray row) {
        Map<String, Object> pair = new HashMap<String, Object>();
        for (Long filteredIndex : filteredIndexes) {
            pair.put(this.headers.get(filteredIndex.intValue()), row.get(filteredIndex.intValue()));
        }

        return pair;
    }

    /**
     * Validates whether types for input data match types in the
     *  fields definition.
     *
     * @param out the string builder used to append the validation info
     *              to already existent text
     */
    public StringBuilder validateInputData(JSONObject inputData, StringBuilder out) {
        for (Object name : inputData.keySet()) {
            if( fieldsByName.containsKey(name) ) {
                out.append(String.format("[%-32s: %-16s: %-16s: ", name,
                        inputData.get(name).getClass().getName(),
                        ((JSONObject) fieldsByName.get(name)).get("optype")));
                String optType = (String) ((JSONObject) fieldsByName.get(name)).get("optype");
                if( inputData.get(name).getClass().
                        isAssignableFrom(Utils.getJavaType(optType)) ) {
                    out.append("OK\n");
                } else {
                    out.append("WRONG\n");
                }
            } else {
                out.append(String.format("Field '%s' does not exist\n", name));
            }
        }

        return out;
    }


    /**
     * Cleans missing tokens
     *
     * @param value the value to normalize
     */
    public Object normalize(Object value) {
        if( value instanceof String ) {
            return (missingTokens.contains(value) ? null : value);
        }

        return null;
    }

    /**
     * Returns the ids for the fields that contain missing values
     */
    public Map<String, Long> getMissingCounts() {
        Map<String, Long> missingCounts = new HashMap<String, Long>();

        for (Object fieldID : fields.keySet()) {
            JSONObject field = (JSONObject) fields.get(fieldID);
            JSONObject summary = (JSONObject) field.get("summary");
            if( summary != null ) {
                Long missingCount = (Long) summary.get("missing_count");
                if( missingCount != null && missingCount > 0 ) {
                    missingCounts.put(fieldID.toString(), missingCount);
                }
            }
        }

        if( missingCounts.size() == 0 ) {
            throw new IllegalStateException("The structure has not enough information " +
                    "to extract the fields containing missing values." +
                    "Only datasets and models have such information. " +
                    "You could retry the get remote call " +
                    " with 'limit=-1' as query string.");
        }

        return missingCounts;
    }

    /**
     * Returns the summary information for the field
     */
    public JSONObject getStats(String fieldName) {
        JSONObject field = (JSONObject) fieldsByName.get(fieldName);
        return (JSONObject) field.get("summary");
    }

    public static final class FieldsStructure {
        private JSONObject fields;
        private Locale locale;
        private JSONArray missingTokens;

        public JSONObject getFields() {
            return fields;
        }

        public void setFields(JSONObject fields) {
            this.fields = fields;
        }

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        public JSONArray getMissingTokens() {
            return missingTokens;
        }

        public void setMissingTokens(JSONArray missingTokens) {
            this.missingTokens = missingTokens;
        }
    }
}
