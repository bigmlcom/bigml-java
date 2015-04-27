package org.bigml.binding;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;

/**
 * A lightweight wrapper of the basic model information
 *
 * Uses a BigML remote model to build a local version that contains the
 * main features of a model, except its tree structure.
 */
public class BaseModel extends ModelFields {

    protected JSONObject model;
    protected String description;
    protected JSONArray fieldImportance;

    protected String locale;

    protected String resourceId;
    protected String objectiveField;

    public BaseModel(JSONObject model) throws Exception {
        super();

        this.model = model;

        this.initialize();
    }

    protected void initialize() throws Exception {

        if( checkModelStructure(model) ) {
            resourceId = (String) model.get("resource");
        } else {
            throw new Exception(
                    "Cannot create the BaseModel instance. Could not find the 'model' key in the resource");
        }

        String prefix = Utils.getJSONObject(model, "object") != null ? "object."
                : "";

        JSONObject status = (JSONObject) Utils.getJSONObject(model, prefix + "status");
        if( status != null &&
                status.containsKey("code") &&
                AbstractResource.FINISHED == ((Number) status.get("code")).intValue() ) {

            JSONObject fields = (JSONObject) Utils.getJSONObject(model, prefix
                    + "model.fields");

            if (Utils.getJSONObject(model, prefix + "model.model_fields") != null) {
                fields = (JSONObject) Utils.getJSONObject(model, prefix
                        + "model.model_fields");

                JSONObject modelFields = (JSONObject) Utils.getJSONObject(
                        model, prefix + "model.fields");
                Iterator iter = fields.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    if (modelFields.get(key) == null) {
                        throw new Exception(
                                "Some fields are missing to generate a local model. Please, provide a model with the complete list of fields.");
                    }
                }

                iter = fields.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    JSONObject field = (JSONObject) fields.get(key);
                    JSONObject modelField = (JSONObject) modelFields.get(key);
                    field.put("summary", modelField.get("summary"));
                    field.put("name", modelField.get("name"));
                }
            }

            Object objectiveFields = Utils.getJSONObject(model, prefix
                    + "objective_fields");
            objectiveField = objectiveFields instanceof JSONArray ? (String) ((JSONArray) objectiveFields)
                    .get(0) : (String) objectiveFields;

            super.initialize(fields, objectiveField, null, null);

            this.description = (String) Utils.getJSONObject(model, "description", "");
            JSONArray modelFieldImportance = (JSONArray) Utils.getJSONObject(model, prefix + "model.importance", null);

            if (modelFieldImportance != null) {
                fieldImportance = new JSONArray();

                for (Object element : modelFieldImportance) {
                    JSONArray elementItem = (JSONArray) element;
                    if (fields.containsKey(elementItem.get(0).toString())) {
                        fieldImportance.add(elementItem);
                    }
                }
            }

            this.locale = (String) Utils.getJSONObject(model, prefix + "locale",
                    BigMLClient.DEFAUL_LOCALE.toString());

        } else {
            throw new IllegalStateException("The model isn't finished yet");
        }
    }

    public String getDescription() {
        return description;
    }

    public JSONArray getFieldImportance() {
        return fieldImportance;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getObjectiveField() {
        return objectiveField;
    }
}