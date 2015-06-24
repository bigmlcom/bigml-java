/*
  An local Ensemble object.

  This module defines an Ensemble to make predictions locally using its
  associated models.

  This module can not only save you a few credits, but also enormously
  reduce the latency for each prediction and let you use your models
  offline.

  import org.bigml.binding.BigMLClient;
  import org.bigml.binding.resources.Ensemble;


  # creating ensemble
  Ensemble ensemble = BigMLClient.getInstance().createEnsemble('dataset/5143a51a37203f2cf7000972')

  # Ensemble object to predict
  LocalEnsemble localEnsemble = LocalEnsemble(ensemble)
  localEnsemble.predict("{\"petal length\": 3, \"petal width\": 1}")
 */

package org.bigml.binding;

import java.util.*;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local predictive Ensemble.
 * 
 * Uses a number of BigML remote models to build an ensemble local version that
 * can be used to generate predictions locally.
 * 
 */
public class LocalEnsemble {

    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(LocalEnsemble.class.getName());

    private String ensembleId;

    private String[] modelsIds;
    private final JSONArray models;
    private JSONObject fields;
    private Map<String, String> fieldNames = new HashMap<String, String>();

    private final List<JSONArray> models_split = new ArrayList<JSONArray>();

    private final MultiModel multiModel;

    private JSONArray distribution = null;

    public LocalEnsemble(JSONObject ensemble, String storage, Integer max)
            throws Exception {
        this(ensemble, max);
    }

    /**
     * Constructor with an Ensemble reference
     * 
     * @param ensemble
     *            the json representation for the remote ensemble
     */
    public LocalEnsemble(JSONObject ensemble) throws Exception {

        if (ensemble.get("objects") != null) {
            throw new IllegalArgumentException("Embedded objects unsupported");
        } else {
            String prefix = Utils.getJSONObject(ensemble, "object") != null ? "object."
                    : "";

            this.ensembleId = (String) Utils.getJSONObject(ensemble,
                    prefix + "resource");

            JSONArray modelsJson = (JSONArray) Utils.getJSONObject(ensemble,
                    prefix + "models");
            distribution = (JSONArray) Utils.getJSONObject(ensemble,
                    prefix + "distributions");


            int mn = modelsJson.size();
            modelsIds = new String[mn];
            for (int i = 0; i < mn; i++) {
                modelsIds[i] = (String) modelsJson.get(i);
            }
        }

        BigMLClient bigmlClient = BigMLClient.getInstance();
        models = new JSONArray();

        for (String id : modelsIds) {
            models.add(bigmlClient.getModel(id));
        }

        multiModel = new MultiModel(models);

        calculateFields();
    }

    /**
     * Constructor with an Ensemble reference and the number
     *  of max models to use
     *
     * @param ensemble
     *            the json representation for the remote ensemble
     */
    public LocalEnsemble(JSONObject ensemble, Integer maxModels) throws Exception {

        if (ensemble.get("objects") != null) {
            throw new IllegalArgumentException("Embedded objects unsupported");
        } else {
            String prefix = Utils.getJSONObject(ensemble, "object") != null ? "object."
                    : "";

            this.ensembleId = (String) Utils.getJSONObject(ensemble,
                    prefix + "resource");
            JSONArray modelsJson = (JSONArray) Utils.getJSONObject(ensemble,
                    prefix + "models");
            distribution = (JSONArray) Utils.getJSONObject(ensemble,
                    prefix + "distributions");

            int mn = modelsJson.size();
            modelsIds = new String[mn];
            for (int i = 0; i < mn; i++) {
                modelsIds[i] = (String) modelsJson.get(i);
            }
        }

        BigMLClient bigmlClient = BigMLClient.getInstance();
        models = new JSONArray();

        for (String id : modelsIds) {
            models.add(bigmlClient.getModel(id));
        }
        int numberOfModels = models.size();

        if( maxModels != null && maxModels > 1 ) {
            int[] items = Utils.getRange(0, numberOfModels, maxModels);
            for (int item : items) {
                JSONArray arrayOfModels = new JSONArray();
                arrayOfModels.addAll(models.subList(item, item+maxModels));
                models_split.add(arrayOfModels);
            }
        }

        multiModel = new MultiModel(models);

        calculateFields();
    }

    /**
     * Constructor with a list of model references and the number
     *  of max models to use
     *
     * @param modelsIds
     *            the model/id of each model to be used in the ensemble
     * @param maxModels
     *            the maximum number of models we will use in the ensemble
     *            null if we do not want a maxModels value
     */
    public LocalEnsemble(List modelsIds, Integer maxModels) throws Exception {
        this.modelsIds = (String[]) modelsIds.toArray(new String[modelsIds.size()]);
        BigMLClient bigmlClient = BigMLClient.getInstance();
        models = new JSONArray();

        for (String id : this.modelsIds) {
            models.add(bigmlClient.getModel(id));
        }

        int numberOfModels = models.size();

        if( maxModels != null && maxModels > 1 ) {
            int[] items = Utils.getRange(0, numberOfModels, maxModels);
            for (int item : items) {
                JSONArray arrayOfModels = new JSONArray();
                arrayOfModels.addAll(models.subList(item, item+maxModels));
                models_split.add(arrayOfModels);
            }
        }

        multiModel = new MultiModel(models);

        calculateFields();
    }


    public List<JSONArray> getFieldImportanceData() {
        Map<String, Double> fieldImportance = new HashMap<String, Double>();

        boolean useDistribution = false;
        List<JSONObject> importances = new ArrayList<JSONObject>();
        if( distribution != null && distribution.size() > 0 )  {
            useDistribution = true;
            for (Object item : distribution) {
                JSONObject itemObj = (JSONObject) item;
                useDistribution &= itemObj.containsKey("importance");
                if( !useDistribution )
                    break;
                else
                    importances.add((JSONObject) itemObj.get("importance"));
            }
        }

        if( useDistribution ) {
            for (JSONObject importance : importances) {
                for (Object fieldInfo : importance.keySet()) {
                    JSONArray fieldInfoArr = (JSONArray) fieldInfo;
                    String fieldId = (String) fieldInfoArr.get(0);
                    if( !fieldImportance.containsKey(fieldId)) {
                        fieldImportance.put(fieldId, 0.0);
                        String fieldName = (String) ((JSONObject)
                                fields.get(fieldId)).get("name");

                        JSONObject fieldNameObj = new JSONObject();
                        fieldNameObj.put("name", fieldName);
                    }

                    fieldImportance.put(fieldId, fieldImportance.get(fieldId) +
                            ((Number)fieldInfoArr.get(1)).doubleValue());
                }
            }
        } else {
            for (Object model : models) {
                JSONObject modelObj = (JSONObject) model;
                JSONArray fieldImportanceInfo = (JSONArray) Utils.getJSONObject(modelObj,
                        "object.model.importance");;
                for (Object fieldInfo : fieldImportanceInfo) {
                    JSONArray fieldInfoArr = (JSONArray) fieldInfo;
                    String fieldId = (String) fieldInfoArr.get(0);

                    if( !fieldImportance.containsKey(fieldId)) {
                        fieldImportance.put(fieldId, 0.0);
                        String fieldName = (String) ((JSONObject)
                                fields.get(fieldId)).get("name");

                        JSONObject fieldNameObj = new JSONObject();
                        fieldNameObj.put("name", fieldName);
                    }

                    fieldImportance.put(fieldId, fieldImportance.get(fieldId) +
                            ((Number)fieldInfoArr.get(1)).doubleValue());
                }
            }
        }

        for (String fieldName : fieldImportance.keySet()) {
            fieldImportance.put(fieldName, fieldImportance.get(fieldName) / models.size());
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
                return (((Number) jsonArray.get(1)).doubleValue() >
                        ((Number) jsonArray2.get(1)).doubleValue() ? -1 : 1);
            }
        });

        return fieldImportanceOrdered;
    }

    /**
     * Accessor to the full list of fields used by this ensemble. It's obtained
     * from the union of fields in all models of the ensemble.
     */
    public JSONObject getFields() {
        return fields;
    }

    /**
     * Accessor to the full list of fields used by this ensemble. It's obtained
     * from the union of fields in all models of the ensemble.
     */
    public Map<String, String> getFieldNames() {
        return fieldNames;
    }

    /**
     * Calculates the full list of fields used by this ensemble. It's obtained
     * from the union of fields in all models of the ensemble.
     */
    protected void  calculateFields() {
        fields = new JSONObject();
        fieldNames.clear();

        for (int i = 0; i < this.modelsIds.length; i++) {
            JSONObject model = (JSONObject) this.models.get(i);
            JSONObject fields = (JSONObject) Utils.getJSONObject(model,
                    "object.model.fields");
            for (Object k : fields.keySet()) {
                if (null != fields.get(k)) {
                    String fieldName = (String) ((JSONObject) fields.get(k)).get("name");
                    this.fields.put(k, fields.get(k));
                    this.fieldNames.put((String) k, fieldName);
                }
            }
        }
    }

    /**
     * Makes a prediction based on the prediction made by every model.
     * 
     * The method parameter is a numeric key to the following combination
     * methods in classifications/regressions: 0 - majority vote (plurality)/
     * average: PLURALITY_CODE 1 - confidence weighted majority vote / error
     * weighted: CONFIDENCE_CODE 2 - probability weighted majority vote /
     * average: PROBABILITY_CODE
     */
    @Deprecated
    public Map<Object, Object> predict(final String inputData, Boolean byName,
            Integer method, Boolean withConfidence) throws Exception {
        if (method == null) {
            method = PredictionMethod.PLURALITY.getCode();
        }

        PredictionMethod intMethod = PredictionMethod.valueOf(method);

        if (byName == null) {
            byName = true;
        }
        if (withConfidence == null) {
            withConfidence = false;
        }

        JSONObject inputDataObj = (JSONObject) JSONValue.parse(inputData);
        MultiVote votes = this.multiModel.generateVotes(inputDataObj, byName,
                null, withConfidence);
        return votes.combine(intMethod, withConfidence, null, null, null, null, null);
    }

    /**
     * Makes a prediction based on the prediction made by every model.
     * 
     * The method parameter is a numeric key to the following combination
     * methods in classifications/regressions: 0 - majority vote (plurality)/
     * average: PLURALITY_CODE 1 - confidence weighted majority vote / error
     * weighted: CONFIDENCE_CODE 2 - probability weighted majority vote /
     * average: PROBABILITY_CODE
     */
    public Map<Object, Object> predict(final JSONObject inputData,
            Boolean byName, PredictionMethod method, Boolean withConfidence)
            throws Exception {
       return predict(inputData, byName, method, withConfidence, null, null, null, null, null, null);
    }

    /**
     * Makes a prediction based on the prediction made by every model.
     *
     * The method parameter is a numeric key to the following combination
     * methods in classifications/regressions: 0 - majority vote (plurality)/
     * average: PLURALITY_CODE 1 - confidence weighted majority vote / error
     * weighted: CONFIDENCE_CODE 2 - probability weighted majority vote /
     * average: PROBABILITY_CODE
     */
    public Map<Object, Object> predict(final JSONObject inputData,
            Boolean byName, PredictionMethod method, Boolean withConfidence, Map options,
            MissingStrategy missingStrategy, Boolean addConfidence, Boolean addDistribution,
            Boolean addCount, Boolean addMedian)
            throws Exception {

        if (method == null) {
            method = PredictionMethod.PLURALITY;
        }

        if (byName == null) {
            byName = true;
        }
        if (withConfidence == null) {
            withConfidence = false;
        }

        if (addMedian == null) {
            addMedian = false;
        }

        MultiVote votes = null;

        if( models_split != null && !models_split.isEmpty() ) {
            votes = new MultiVote();
            for (JSONArray splitModels : models_split) {
                MultiModel splitMultiModel = new MultiModel(splitModels);
                MultiVote splitVotes = splitMultiModel.generateVotes(inputData,
                        byName, missingStrategy, addMedian);

                if( addMedian ) {
                    for (HashMap<Object, Object> prediction : splitVotes.getPredictions()) {
                        prediction.put("prediction", prediction.get("median"));
                    }
                }

                votes.extend(splitVotes);
            }
        } else {
            // When only one group of models is found you use the
            // corresponding multimodel to predict
            votes = this.multiModel.generateVotes(inputData, byName, missingStrategy, addMedian);
            if( addMedian ) {
                for (HashMap<Object, Object> prediction : votes.getPredictions()) {
                    prediction.put("prediction", prediction.get("median"));
                }
            }
        }

        return votes.combine(method, withConfidence, addConfidence, addDistribution, addCount, addMedian, options);

    }

    /**
     * Convenience version of predict that take as inputs a map from field ids
     * or names to their values as Java objects. See also predict(String,
     * Boolean, Integer, Boolean).
     */
    @Deprecated
    public Map<Object, Object> predictWithMap(Map<String, Object> inputs,
            Boolean byName, Integer method, Boolean conf) throws Exception {
        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));

        if ( method == null ) {
            method = PredictionMethod.PLURALITY.getCode();
        }

        PredictionMethod intMethod = PredictionMethod.valueOf(method);

        return predict(inputObj, byName, intMethod, conf);
    }

    /**
     * Convenience version of predict that take as inputs a map from field ids
     * or names to their values as Java objects. See also predict(String,
     * Boolean, Integer, Boolean).
     */
    public Map<Object, Object> predictWithMap(Map<String, Object> inputs,
            Boolean byName, PredictionMethod method, Boolean conf) throws Exception {
        JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
                .toJSONString(inputs));
        return predict(inputObj, byName, method, conf);
    }
}
