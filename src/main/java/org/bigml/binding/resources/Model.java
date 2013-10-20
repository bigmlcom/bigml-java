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
    this.devMode = false;
    super.init();
  }

  /**
   * Constructor
   *
   */
  public Model(final String apiUser, final String apiKey, final boolean devMode) {
    this.bigmlUser = apiUser != null ? apiUser : System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = apiKey != null ? apiKey : System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
    this.devMode = devMode;
    super.init();
  }

  
  /**
   * Creates a new model.
   *
   * POST /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 Host: bigml.io
   * Content-Type: application/json
   *
   * @param datsetId	a unique identifier in the form datset/id where id is a string of 24
   * 					alpha-numeric chars for the dataset to attach the model.
   * @param args		set of parameters for the new model. Optional
   * @param waitTime	time to wait for next check of FINISHED status for source before to start to
   * 					create the model. Optional
   * @param retries		number of times to try the operation. Optional
   *
   */
  public JSONObject create(final String datasetId, String args, Integer waitTime, Integer retries) {
    if (datasetId == null || datasetId.length() == 0 || !(datasetId.matches(DATASET_RE))) {
      logger.info("Wrong dataset id");
      return null;
    }

    try {
      waitTime = waitTime != null ? waitTime : 3;
      retries = retries != null ? retries : 10;
      if (waitTime > 0) {
        int count = 0;
        while (count<retries && !BigMLClient.getInstance(this.devMode).datasetIsReady(datasetId)) {
          Thread.sleep(waitTime);
          count++;
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
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public JSONObject get(final String modelId) {
    return get(modelId, null, null);
  }
  
  
  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param apiUser		API user
   * @param apiKey		API key
   *
   */
  public JSONObject get(final String modelId, final String apiUser, final String apiKey) {
    if (modelId == null || modelId.length() == 0 || !(modelId.matches(MODEL_RE))) {
      logger.info("Wrong model id");
      return null;
    }

    return getResource(BIGML_URL + modelId, null, apiUser, apiKey);
  }

  
  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param model	a model JSONObject
   *
   */
  public JSONObject get(final JSONObject model) {
    String resourceId = (String) model.get("resource");
    return get(resourceId, null, null);
  }
  
  
  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param model	a model JSONObject
   * @param apiUser		API user
   * @param apiKey		API key
   *
   */
  public JSONObject get(final JSONObject model, final String apiUser, final String apiKey) {
    String resourceId = (String) model.get("resource");
    return get(resourceId, apiUser, apiKey);
  }
  
  
  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param queryString query for filtering.
   *
   */
  public JSONObject get(final String modelId, final String queryString) {
    return get(modelId, queryString, null, null);
  }
  
  
  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param queryString query for filtering.
   * @param apiUser		API user
   * @param apiKey		API key
   *
   */
  public JSONObject get(final String modelId, final String queryString, final String apiUser, final String apiKey) {
    if (modelId == null || modelId.length() == 0 ||  !(modelId.matches(MODEL_RE))) {
      logger.info("Wrong model id");
      return null;
    }

    return getResource(BIGML_URL + modelId, queryString, apiUser, apiKey);
  }

  
  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param model	a model JSONObject
   * @param queryString query for filtering
   *
   */
  public JSONObject get(final JSONObject model, final String queryString) {
    String resourceId = (String) model.get("resource");
    return get(resourceId, queryString, null, null);
  }
  
  
  /**
   * Retrieves a model.
   *
   * GET /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * Host: bigml.io
   *
   * @param model	a model JSONObject
   * @param queryString query for filtering
   * @param apiUser		API user
   * @param apiKey		API key
   *
   */
  public JSONObject get(final JSONObject model, final String queryString, final String apiUser, final String apiKey) {
    String resourceId = (String) model.get("resource");
    return get(resourceId, queryString, apiUser, apiKey);
  }

  
  /**
   * Checks whether a model's status is FINISHED.
   *
   * @param modelId	a unique identifier in the form model/id where id is a string of 24
   * 				alpha-numeric chars.
   *
   */
  public boolean isReady(final String modelId) {
    return isResourceReady(get(modelId));
  }

  
  /**
   * Checks whether a model's status is FINISHED.
   *
   * @param model	a model JSONObject
   *
   */
  public boolean isReady(final JSONObject model) {
    String resourceId = (String) model.get("resource");
    return isReady(resourceId);
  }

  
  /**
   * Lists all your models.
   *
   * GET /andromeda/model?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString		query filtering the listing.
   *
   */
  public JSONObject list(final String queryString) {
    return listResources(MODEL_URL, queryString);
  }

  
  /**
   * Updates a model.
   *
   * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io 
   * Content-Type: application/json
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   * @param changes		set of parameters to update the source. Optional
   *
   */
  public JSONObject update(final String modelId, final String changes) {
    if (modelId == null || modelId.length() == 0 || !(modelId.matches(MODEL_RE))) {
      logger.info("Wrong model id");
      return null;
    }
    return updateResource(BIGML_URL + modelId, changes);
  }

  
  /**
   * Updates a model.
   *
   * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io 
   * Content-Type: application/json
   *
   * @param model		a model JSONObject
   * @param changes		set of parameters to update the source. Optional
   *
   */
  public JSONObject update(final JSONObject model, final JSONObject changes) {
    String resourceId = (String) model.get("resource");
    return update(resourceId, changes.toJSONString());
  }

  
  /**
   * Deletes a model.
   *
   * DELETE
   * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param modelId 	a unique identifier in the form model/id where id is a string of 24
   * 					alpha-numeric chars.
   *
   */
  public JSONObject delete(final String modelId) {
    if (modelId == null || modelId.length() == 0 || !(modelId.matches(MODEL_RE))) {
      logger.info("Wrong model id");
      return null;
    }
    return deleteResource(BIGML_URL + modelId);
  }

        		  
  /**
   * Deletes a model.
   *
   * DELETE
   * /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param model	a model JSONObject
   *
   */
  public JSONObject delete(final JSONObject model) {
    String resourceId = (String) model.get("resource");
    return delete(resourceId);
  }
  
}