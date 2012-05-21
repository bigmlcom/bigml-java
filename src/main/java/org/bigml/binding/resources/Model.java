package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete models.
 *
 * Full API documentation on the API can be found from BigML at: https://bigml.com/developers/models
 *
 *
 */
public class Model extends AbstractResource {

  // Logging
  Logger logger = LoggerFactory.getLogger(Model.class);

  /**
   * Constructor
   *
   */
  public Model() {
    this.bigmlUser = System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
  }

  /**
   * Constructor
   *
   */
  public Model(final String apiUser, final String apiKey) {
    this.bigmlUser = apiUser != null ? apiUser : System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = apiKey != null ? apiKey : System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
  }

  /**
   * Create a new model.
   *
   * POST /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param datsetId	a unique identifier in the form datset/id where id is a string of 24
   * alpha-numeric chars for the dataset to attach the model.
   * @param args	set of parameters for the new model. Optional
   * @param waitTime	time to wait for next check of FINISHED status for source before to start to
   * create the model. Optional
   *
   */
  public JSONObject create(final String datasetId, String args, Integer waitTime) {
    if (datasetId == null || datasetId.length() == 0 || !datasetId.matches(DATASET_RE)) {
      logger.info("Wrong dataset id");
      return null;
    }

    try {
      waitTime = waitTime != null ? waitTime : 3;
      if (waitTime > 0) {
        while (!BigMLClient.getInstance().datasetIsReady(datasetId)) {
          Thread.sleep(waitTime);
        }
      }

      JSONObject requestObject = new JSONObject();
      if (args != null) {
        requestObject = (JSONObject) JSONValue.parse(args);
      }
      requestObject.put("dataset", datasetId);
      return createResource(MODEL_URL, requestObject.toJSONString());
    } catch (Throwable e) {
      logger.error("Error creating model");
      return null;
    }
  }

  /**
   * Retrieve a model.
   *
   * GET /andromeda/model/4f67c0ee03ce89c74a000006?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId a unique identifier in the form model/id where id is a string of 24
   * alpha-numeric chars.
   *
   */
  public JSONObject get(final String modelId) {
    if (modelId == null || modelId.length() == 0 || !modelId.matches(MODEL_RE)) {
      logger.info("Wrong model id");
      return null;
    }

    return getResource(BIGML_URL + modelId);
  }

  /**
   * Retrieve a model.
   *
   * GET /andromeda/model/4f67c0ee03ce89c74a000006?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param model a unique identifier in the form model/id where id is a string of 24 alpha-numeric
   * chars.
   *
   */
  public JSONObject get(final JSONObject model) {
    String resourceId = (String) model.get("resource");
    return get(resourceId);
  }

  /**
   * Check whether a model' status is FINISHED.
   *
   * @param modelId a unique identifier in the form model/id where id is a string of 24
   * alpha-numeric chars.
   *
   */
  public boolean isReady(final String modelId) {
    return isResourceReady(get(modelId));
  }

  /**
   * Check whether a model' status is FINISHED.
   *
   * @param model a unique identifier in the form model/id where id is a string of 24 alpha-numeric
   * chars.
   *
   */
  public boolean isReady(final JSONObject model) {
    String resourceId = (String) model.get("resource");
    return isReady(resourceId);
  }

  /**
   * List all your models.
   *
   * GET /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString	query filtering the listing.
   *
   */
  public JSONObject list(final String queryString) {
    return listResources(MODEL_URL, queryString);
  }

  /**
   * Update a model.
   *
   * PUT /andromeda/model/4f67c0ee03ce89c74a000006?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param modelId a unique identifier in the form model/id where id is a string of 24
   * alpha-numeric chars.
   * @param json	set of parameters to update the source. Optional
   *
   */
  public JSONObject update(final String modelId, final String json) {
    if (modelId == null || modelId.length() == 0 || !modelId.matches(MODEL_RE)) {
      logger.info("Wrong model id");
      return null;
    }
    return updateResource(BIGML_URL + modelId, json);
  }

  /**
   * Update a model.
   *
   * PUT /andromeda/model/4f67c0ee03ce89c74a000006?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param model a unique identifier in the form model/id where id is a string of 24 alpha-numeric
   * chars.
   * @param json	set of parameters to update the source. Optional
   *
   */
  public JSONObject update(final JSONObject model, final JSONObject json) {
    String resourceId = (String) model.get("resource");
    return update(resourceId, json.toJSONString());
  }

  /**
   * Delete a model.
   *
   * DELETE
   * /andromeda/model/4f67c0ee03ce89c74a000006?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param modelId a unique identifier in the form model/id where id is a string of 24
   * alpha-numeric chars.
   *
   */
  public JSONObject delete(final String modelId) {
    if (modelId == null || modelId.length() == 0 || !modelId.matches(MODEL_RE)) {
      logger.info("Wrong model id");
      return null;
    }
    return deleteResource(BIGML_URL + modelId);
  }

  /**
   * Delete a model.
   *
   * DELETE
   * /andromeda/model/4f67c0ee03ce89c74a000006?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param model unique identifier in the form model/id where id is a string of 24 alpha-numeric
   * chars.
   *
   */
  public JSONObject delete(final JSONObject model) {
    String resourceId = (String) model.get("resource");
    return delete(resourceId);
  }
}
