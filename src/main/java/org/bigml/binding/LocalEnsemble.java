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

import java.util.Map;

import org.apache.log4j.Logger;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
  static Logger logger = Logger.getLogger(LocalEnsemble.class.getName());

  private String ensembleId;

  private String[] modelsIds;
  private final JSONArray models;
  private final MultiModel multiModel;

  public LocalEnsemble(JSONObject ensemble, String storage, int max)
      throws Exception {
    this(ensemble);
  }

  /**
   * Constructor
   *
   * @param ensemble
   *          the json representation for the remote ensemble
   */
  public LocalEnsemble(JSONObject ensemble) throws Exception {

    if (ensemble.get("objects") != null) {
      throw new IllegalArgumentException("Embedded objects unsupported");
    } else {
      this.ensembleId = (String) ensemble.get("resource");
      JSONArray modelsJson = (JSONArray) Utils.getJSONObject(ensemble,
          "object.models");
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
  }

  /**
   * Accessor to the full list of fields used by this ensemble. It's obtained
   * from the union of fields in all models of the ensemble.
   */
  public JSONObject getFields() {
    JSONObject result = new JSONObject();
    for (int i = 0; i < this.modelsIds.length; i++) {
      JSONObject model = (JSONObject) this.models.get(i);
      JSONObject fields = (JSONObject) Utils.getJSONObject(model,
          "object.model.fields");
      for (Object k : fields.keySet()) {
        if (null == result.get(k)) {
          result.put(k, fields.get(k));
        }
      }
    }
    return result;
  }

  /**
   * Makes a prediction based on the prediction made by every model.
   *
   * The method parameter is a numeric key to the following combination methods
   * in classifications/regressions: 0 - majority vote (plurality)/ average:
   * PLURALITY_CODE 1 - confidence weighted majority vote / error weighted:
   * CONFIDENCE_CODE 2 - probability weighted majority vote / average:
   * PROBABILITY_CODE
   */
  @Deprecated
  public Map<Object, Object> predict(final String inputData, Boolean byName,
      Integer method, Boolean withConfidence) throws Exception {
    if (method == null) {
      method = MultiVote.PLURALITY;
    }
    if (byName == null) {
      byName = true;
    }
    if (withConfidence == null) {
      withConfidence = false;
    }

    MultiVote votes = this.multiModel.generateVotes(inputData, byName,
        withConfidence);
    return votes.combine(method, withConfidence);
  }

  /**
   * Makes a prediction based on the prediction made by every model.
   *
   * The method parameter is a numeric key to the following combination methods
   * in classifications/regressions: 0 - majority vote (plurality)/ average:
   * PLURALITY_CODE 1 - confidence weighted majority vote / error weighted:
   * CONFIDENCE_CODE 2 - probability weighted majority vote / average:
   * PROBABILITY_CODE
   */
  public Map<Object, Object> predict(final JSONObject inputData,
                                       Boolean byName,
                                       Integer method,
                                       Boolean withConfidence)
        throws Exception {
        if (method == null) {
            method = MultiVote.PLURALITY;
        }
        if (byName == null) {
            byName = true;
        }
        if (withConfidence == null) {
            withConfidence = false;
        }

        MultiVote votes =
            this.multiModel.generateVotes(inputData, byName, withConfidence);
        return  votes.combine(method, withConfidence);
    }

    /**
     * Convenience version of predict that take as inputs a map from
     * field ids or names to their values as Java objects.  See also
     * predict(String, Boolean, Integer, Boolean).
     */
  public Map<Object, Object> predictWithMap(Map<String, Object> inputs,
                                       Boolean byName,
                                       Integer method,
                                       Boolean conf) throws Exception {
    JSONObject inputObj = (JSONObject) JSONValue.parse(JSONValue
        .toJSONString(inputs));
    return predict(inputObj, byName, method, conf);
    }
}
