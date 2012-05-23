package org.bigml.binding.resources;

import org.bigml.binding.BigMLClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete predictions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/predictions
 *
 *
 */
public class Prediction extends AbstractResource {

  // Logging
  Logger logger = LoggerFactory.getLogger(Prediction.class);

  /**
   * Constructor
   *
   */
  public Prediction() {
    this.bigmlUser = System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
  }

  /**
   * Constructor
   *
   */
  public Prediction(final String apiUser, final String apiKey) {
    this.bigmlUser = apiUser != null ? apiUser : System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = apiKey != null ? apiKey : System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
  }

  /**
   * Create a new prediction.
   *
   * POST /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1 Host:
   * bigml.io Content-Type: application/json
   *
   * @param modelId	a unique identifier in the form model/id where id is a string of 24
   * alpha-numeric chars for the source to attach the prediction.
   * @param args	set of parameters for the new prediction. Required
   * @param waitTime	time to wait for next check of FINISHED status for model before to start to
   * create the prediction. Optional
   *
   */
  public JSONObject create(final String modelId, String args, Integer waitTime) {
    if (modelId == null || modelId.length() == 0 || !modelId.matches(MODEL_RE)) {
      logger.info("Wrong model id");
      return null;
    }

    try {
      waitTime = waitTime != null ? waitTime : 3;
      if (waitTime > 0) {
        while (!BigMLClient.getInstance().modelIsReady(modelId)) {
          Thread.sleep(waitTime);
        }
      }

      JSONObject requestObject = new JSONObject();
      if (args != null) {
        requestObject = (JSONObject) JSONValue.parse(args);
      }
      requestObject.put("model", modelId);
      return createResource(PREDICTION_URL, requestObject.toJSONString());
    } catch (Throwable e) {
      logger.error("Error creating prediction", e);
      return null;
    }

  }

  /**
   * Retrieve a prediction.
   *
   * GET
   * /andromeda/prediction/4f6a014b03ce89584500000f?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param predictionId a unique identifier in the form prediction/id where id is a string of 24
   * alpha-numeric chars.
   *
   */
  public JSONObject get(final String predictionId) {
    if (predictionId == null || predictionId.length() == 0 || !predictionId.matches(PREDICTION_RE)) {
      logger.info("Wrong prediction id");
      return null;
    }

    return getResource(BIGML_URL + predictionId);
  }

  /**
   * Retrieve a prediction.
   *
   * GET
   * /andromeda/prediction/4f6a014b03ce89584500000f?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io
   *
   * @param prediction a unique identifier in the form prediction/id where id is a string of 24
   * alpha-numeric chars.
   *
   */
  public JSONObject get(final JSONObject prediction) {
    String resourceId = (String) prediction.get("resource");
    return get(resourceId);
  }

  /**
   * Check whether a prediction' status is FINISHED.
   *
   * @param predictionId a unique identifier in the form prediction/id where id is a string of 24
   * alpha-numeric chars.
   *
   */
  public boolean isReady(final String predictionId) {
    return isResourceReady(get(predictionId));
  }

  /**
   * Check whether a prediction' status is FINISHED.
   *
   * @param prediction a unique identifier in the form prediction/id where id is a string of 24
   * alpha-numeric chars.
   *
   */
  public boolean isReady(final JSONObject prediction) {
    String resourceId = (String) prediction.get("resource");
    return isReady(resourceId);
  }

  /**
   * List all your predictions.
   *
   * GET /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString	query filtering the listing.
   *
   */
  public JSONObject list(final String queryString) {
    return listResources(PREDICTION_URL, queryString);
  }

  /**
   * Update a prediction.
   *
   * PUT /andromeda/model/4f6a014b03ce89584500000f?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param predictionId a unique identifier in the form prediction/id where id is a string of 24
   * alpha-numeric chars.
   * @param json	set of parameters to update the source. Optional
   *
   */
  public JSONObject update(final String predictionId, final String json) {
    if (predictionId == null || predictionId.length() == 0 || !predictionId.matches(PREDICTION_RE)) {
      logger.info("Wrong prediction id");
      return null;
    }
    return updateResource(BIGML_URL + predictionId, json);
  }

  /**
   * Update a prediction.
   *
   * PUT /andromeda/model/4f6a014b03ce89584500000f?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param prediction a unique identifier in the form prediction/id where id is a string of 24
   * alpha-numeric chars.
   * @param json	set of parameters to update the source. Optional
   *
   */
  public JSONObject update(final JSONObject prediction, final JSONObject json) {
    String resourceId = (String) prediction.get("resource");
    return update(resourceId, json.toJSONString());
  }

  /**
   * Delete a prediction.
   *
   * DELETE
   * /andromeda/prediction/4f6a014b03ce89584500000f?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param predictionId a unique identifier in the form prediction/id where id is a string of 24
   * alpha-numeric chars
   *
   */
  public JSONObject delete(final String predictionId) {
    if (predictionId == null || predictionId.length() == 0 || !predictionId.matches(PREDICTION_RE)) {
      logger.info("Wrong prediction id");
      return null;
    }
    return deleteResource(BIGML_URL + predictionId);
  }

  /**
   * Delete a prediction.
   *
   * DELETE
   * /andromeda/prediction/4f6a014b03ce89584500000f?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
   * HTTP/1.1
   *
   * @param prediction a unique identifier in the form prediction/id where id is a string of 24
   * alpha-numeric chars
   *
   */
  public JSONObject delete(final JSONObject prediction) {
    String resourceId = (String) prediction.get("resource");
    return delete(resourceId);
  }
}
