package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete evaluations.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/evaluations
 *
 *
 */
public class Evaluation extends AbstractResource {

  // Logging
  Logger logger = LoggerFactory.getLogger(Evaluation.class);
  
  /**
   * Constructor
   *
   */
  public Evaluation() {
    this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
    this.devMode = false;
    super.init();
  }

  
  /**
   * Constructor
   *
   */
  public Evaluation(final String apiUser, final String apiKey, final boolean devMode) {
    this.bigmlUser = apiUser != null ? apiUser : System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = apiKey != null ? apiKey : System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
    this.devMode = devMode;
    super.init();
  }
  
  
  /**
   * Create a new evaluation.
   *
   * POST /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
   * Host: bigml.io
   * Content-Type: application/json
   *
   * @param modelId		a unique identifier in the form model/id where id is a string of 24
   *                	alpha-numeric chars for the model to attach the evaluation.
   * @param datasetId	a unique identifier in the form dataset/id where id is a string of 24
   * 					alpha-numeric chars for the dataset to attach the evaluation.
   * @param args		set of parameters for the new evaluation. Optional
   * @param waitTime	time to wait for next check of FINISHED status for model before to start to
   * 					create the evaluation. Optional
   * @param retries		number of times to try the operation. Optional
   * 
   */
  public JSONObject create(final String modelId, final String datasetId, String args, Integer waitTime, Integer retries) {
    if (modelId == null || modelId.length() == 0 || !modelId.matches(MODEL_RE)) {
      logger.info("Wrong model id");
      return null;
    }
    if (datasetId == null || datasetId.length() == 0 || !datasetId.matches(DATASET_RE)) {
      logger.info("Wrong dataset id");
      return null;
    }

    try {
      waitTime = waitTime != null ? waitTime : 3;
      retries = retries != null ? retries : 10;
      if (waitTime > 0) {
    	int count = 0;
        while (count<retries && !BigMLClient.getInstance(this.devMode).modelIsReady(modelId)) {
          Thread.sleep(waitTime);
          count++;
        }
        
        count = 0;
        while (count<retries && !BigMLClient.getInstance(this.devMode).datasetIsReady(datasetId)) {
          Thread.sleep(waitTime);
          count++;
        }
      }

      JSONObject requestObject = new JSONObject();
      if (args != null) {
        requestObject = (JSONObject) JSONValue.parse(args);
      }
      requestObject.put("model", modelId);
      requestObject.put("dataset", datasetId);

      return createResource(EVALUATION_URL, requestObject.toJSONString());
    } catch (Throwable e) {
      logger.error("Error creating evaluation");
      return null;
    }
  }

  
  /**
   * Retrieves an evaluation.
   * 
   * An evaluation is an evolving object that is processed until it
   * reaches the FINISHED or FAULTY state, the method will return a
   * JSONObject that encloses the evaluation values and state info
   * available at the time it is called.
   *
   * GET /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param evaluationId 	a unique identifier in the form evaluation/id where id 
   * 						is a string of 24 alpha-numeric chars.
   *
   */
  public JSONObject get(final String evaluationId) {
    if (evaluationId == null || evaluationId.length() == 0 || !evaluationId.matches(EVALUATION_RE)) {
      logger.info("Wrong evaluation id");
      return null;
    }

    return getResource(BIGML_URL + evaluationId);
  }

  
  /**
   * Retrieves an evaluation.
   * 
   * An evaluation is an evolving object that is processed until it
   * reaches the FINISHED or FAULTY state, the method will return a
   * JSONObject that encloses the evaluation values and state info
   * available at the time it is called.
   *
   * GET /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param evaluation 	an evaluation JSONObject.
   *
   */
  public JSONObject get(final JSONObject evaluation) {
    String evaluationId = (String) evaluation.get("resource");
    return get(evaluationId);
  }

  
  /**
   * Check whether an evaluation's status is FINISHED.
   *
   * @param evaluationId 	a unique identifier in the form evaluation/id where id 
   * 						is a string of 24 alpha-numeric chars.
   *
   */
  public boolean isReady(final String evaluationId) {
    return isResourceReady(get(evaluationId));
  }

  
  /**
   * Check whether an evaluation's status is FINISHED.
   *
   * @param evaluation 	an evaluation JSONObject.
   *
   */
  public boolean isReady(final JSONObject evaluation) {
    String resourceId = (String) evaluation.get("resource");
    return isReady(resourceId);
  }

  
  /**
   * Lists all your evaluations.
   *
   * GET /andromeda/evaluation?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; 
   * Host: bigml.io
   *
   * @param queryString	query filtering the listing.
   *
   */
  public JSONObject list(final String queryString) {
    return listResources(EVALUATION_URL, queryString);
  }

  
  /**
   * Updates an evaluation.
   *
   * PUT /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io 
   * Content-Type: application/json
   *
   * @param evaluationId 	a unique identifier in the form evaluation/id where id 
   * 						is a string of 24 alpha-numeric chars.
   * @param changes			set of parameters to update the evaluation. Optional
   *
   */
  public JSONObject update(final String evaluationId, final String changes) {
    if (evaluationId == null || evaluationId.length() == 0 || !evaluationId.matches(EVALUATION_RE)) {
      logger.info("Wrong evaluation id");
      return null;
    }
    return updateResource(BIGML_URL + evaluationId, changes);
  }

  
  /**
   * Updates an evaluation.
   *
   * PUT /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io 
   * Content-Type: application/json
   *
   * @param evaluation 	an evaluation JSONObject
   * @param changes		set of parameters to update the evaluation. Optional
   *
   */
  public JSONObject update(final JSONObject evaluation, final JSONObject changes) {
    String resourceId = (String) evaluation.get("resource");
    return update(resourceId, changes.toJSONString());
  }

  
  /**
   * Deletes an evaluation.
   *
   * DELETE /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param evaluationId 	a unique identifier in the form evaluation/id where id is a 
   * 						string of 24 alpha-numeric chars.
   *
   */
  public JSONObject delete(final String evaluationId) {
    if (evaluationId == null || evaluationId.length() == 0 || !evaluationId.matches(EVALUATION_RE)) {
      logger.info("Wrong evaluation id");
      return null;
    }
    return deleteResource(BIGML_URL + evaluationId);
  }
  
  
  /**
   * Deletes an evaluation.
   *
   * DELETE /andromeda/evaluation/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param evaluation 	an evaluation JSONObject.
   *
   */
  public JSONObject delete(final JSONObject evaluation) {
    String resourceId = (String) evaluation.get("resource");
    return delete(resourceId);
  }
  
}