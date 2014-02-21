package org.bigml.binding.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete batch predictions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/developers/batch_predictions
 *
 *
 */
public class BatchPrediction extends AbstractResource {

  // Logging
  Logger logger = LoggerFactory.getLogger(BatchPrediction.class);

  public final static String DOWNLOAD_DIR = "/download";

  /**
   * Constructor
   *
   */
  public BatchPrediction() {
    this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
    this.devMode = false;
    super.init();
  }


  /**
   * Constructor
   *
   */
  public BatchPrediction(final String apiUser, final String apiKey, final boolean devMode) {
    this.bigmlUser = apiUser != null ? apiUser : System.getProperty("BIGML_USERNAME");
    this.bigmlApiKey = apiKey != null ? apiKey : System.getProperty("BIGML_API_KEY");
    bigmlAuth = "?username=" + this.bigmlUser + ";api_key=" + this.bigmlApiKey + ";";
    this.devMode = devMode;
    super.init();
  }

  /**
   * Creates a new batch prediction.
   *
   * POST /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY; HTTP/1.1
   * Host: bigml.io
   * Content-Type: application/json
   *
   *  @param modelOrEnsembleId  a unique identifier in the form model/id or ensemble/id where id is a string of 24
   *                            alpha-numeric chars for the model/ensemble to attach the evaluation.
   * @param datasetId           a unique identifier in the form dataset/id where id is a string of 24
   *                            alpha-numeric chars for the dataset to attach the evaluation.
   * @param args                set of parameters for the new batch prediction. Optional
   * @param waitTime            time (milliseconds) to wait for next check of FINISHED status for model
   *                            before to start to create the batch prediction. Optional
   * @param retries             number of times to try the operation. Optional
   *
   */
  public JSONObject create(final String modelOrEnsembleId, final String datasetId, String args, Integer waitTime, Integer retries) {
    if (modelOrEnsembleId == null || modelOrEnsembleId.length() == 0 ||
        !(modelOrEnsembleId.matches(MODEL_RE) || modelOrEnsembleId.matches(ENSEMBLE_RE)) ) {
        logger.info("Wrong model or ensemble id");
        return null;
    }
    if (datasetId == null || datasetId.length() == 0 || !datasetId.matches(DATASET_RE)) {
        logger.info("Wrong dataset id");
        return null;
    }

    try {
      waitTime = waitTime != null ? waitTime : 3000;
      retries = retries != null ? retries : 10;
      if (waitTime > 0) {
        int count = 0;

        if (modelOrEnsembleId.matches(MODEL_RE)) {
          while (count < retries
              && !BigMLClient.getInstance(this.devMode).modelIsReady(
                  modelOrEnsembleId)) {
            Thread.sleep(waitTime);
            count++;
          }
        }

        if (modelOrEnsembleId.matches(ENSEMBLE_RE)) {
          while (count < retries
              && !BigMLClient.getInstance(this.devMode).ensembleIsReady(
                  modelOrEnsembleId)) {
            Thread.sleep(waitTime);
            count++;
          }
        }

        count = 0;
        while (count < retries
            && !BigMLClient.getInstance(this.devMode).datasetIsReady(datasetId)) {
          Thread.sleep(waitTime);
          count++;
        }
      }

      JSONObject requestObject = new JSONObject();
      if (args != null) {
        requestObject = (JSONObject) JSONValue.parse(args);
      }

      if (modelOrEnsembleId.matches(MODEL_RE)) {
        requestObject.put("model", modelOrEnsembleId);
      }
      if (modelOrEnsembleId.matches(ENSEMBLE_RE)) {
        requestObject.put("ensemble", modelOrEnsembleId);
      }
      requestObject.put("dataset", datasetId);

      return createResource(BATCH_PREDICTION_URL, requestObject.toJSONString());
    } catch (Throwable e) {
      logger.error("Error creating batch prediction");
      return null;
    }
  }

  /**
   * Retrieves a batch prediction.
   *
   * GET /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; Host: bigml.io
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchPrediction/id where id is a
   *          string of 24 alpha-numeric chars.
   *
   */
  @Override
  public JSONObject get(final String batchPredictionId) {
    if (batchPredictionId == null || batchPredictionId.length() == 0
        || !batchPredictionId.matches(BATCH_PREDICTION_RE)) {
      logger.info("Wrong batch prediction id");
      return null;
    }

    return getResource(BIGML_URL + batchPredictionId);
  }

  /**
   * Retrieves a batch prediction.
   *
   * GET /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; Host: bigml.io
   *
   * @param batchPrediction
   *          a batch prediction JSONObject.
   *
   */
  @Override
  public JSONObject get(final JSONObject batchPrediction) {
    String resourceId = (String) batchPrediction.get("resource");
    return get(resourceId);
  }

  /**
   * Retrieves the batch predictions file.
   *
   * Downloads predictions, that are stored in a remote CSV file. If a path is
   * given in filename, the contents of the file are downloaded and saved
   * locally. A file-like object is returned otherwise.
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchPrediction/id where id is a
   *          string of 24 alpha-numeric chars.
   * @param filename
   *          Path to save file locally
   *
   */
  public JSONObject downloadBatchPrediction(final String batchPredictionId,
      final String filename) {

    if (batchPredictionId == null || batchPredictionId.length() == 0
        || !batchPredictionId.matches(BATCH_PREDICTION_RE)) {
      logger.info("Wrong batch prediction id");
      return null;
    }

    String url = BIGML_URL + batchPredictionId + DOWNLOAD_DIR;
    return download(url, filename);
  }


  /**
   * Retrieves the batch predictions file.
   *
   * Downloads predictions, that are stored in a remote CSV file. If a path is
   * given in filename, the contents of the file are downloaded and saved
   * locally. A file-like object is returned otherwise.
   *
   * @param batchPrediction
   *          a batch prediction JSONObject.
   * @param filename
   *          Path to save file locally
   *
   */
  public JSONObject downloadBatchPrediction(final JSONObject batchPrediction,
      final String filename) {

    String resourceId = (String) batchPrediction.get("resource");

    if (resourceId != null) {
      String url = BIGML_URL + resourceId + DOWNLOAD_DIR;
      return download(url, filename);
    }
    return null;
  }

  /**
   * Check whether a batch prediction's status is FINISHED.
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchPrediction/id where id is a
   *          string of 24 alpha-numeric chars.
   *
   */
  @Override
  public boolean isReady(final String batchPredictionId) {
    return isResourceReady(get(batchPredictionId));
  }

  /**
   * Check whether a batch prediction's status is FINISHED.
   *
   * @param batchPrediction
   *          a batchPrediction JSONObject.
   *
   */
  @Override
  public boolean isReady(final JSONObject batchPrediction) {
    String resourceId = (String) batchPrediction.get("resource");
    return isReady(resourceId);
  }

  /**
   * Lists all your batch predictions.
   *
   * GET /andromeda/batchprediction?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; Host: bigml.io
   *
   * @param queryString
   *          query filtering the listing.
   *
   */
  @Override
  public JSONObject list(final String queryString) {
    return listResources(BATCH_PREDICTION_URL, queryString);
  }

  /**
   * Updates a batch prediction.
   *
   * PUT /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param evaluationId
   *          a unique identifier in the form batchprediction/id where id is a
   *          string of 24 alpha-numeric chars.
   * @param changes
   *          set of parameters to update the evaluation. Optional
   *
   */
  @Override
  public JSONObject update(final String batchPredictionId, final String changes) {
    if (batchPredictionId == null || batchPredictionId.length() == 0
        || !batchPredictionId.matches(BATCH_PREDICTION_RE)) {
      logger.info("Wrong batch prediction id");
      return null;
    }
    return updateResource(BIGML_URL + batchPredictionId, changes);
  }

  /**
   * Updates a batch prediction.
   *
   * PUT /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io Content-Type: application/json
   *
   * @param batchPrediction
   *          a batchPrediction JSONObject
   * @param changes
   *          set of parameters to update the batch prediction. Optional
   *
   */
  @Override
  public JSONObject update(final JSONObject batchPrediction,
      final JSONObject changes) {
    String resourceId = (String) batchPrediction.get("resource");
    return update(resourceId, changes.toJSONString());
  }

  /**
   * Deletes a batch prediction.
   *
   * DELETE /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1
   *
   * @param batchPredictionId
   *          a unique identifier in the form batchprediction/id where id is a
   *          string of 24 alpha-numeric chars.
   *
   */
  @Override
  public JSONObject delete(final String batchPredictionId) {
    if (batchPredictionId == null || batchPredictionId.length() == 0
        || !batchPredictionId.matches(BATCH_PREDICTION_RE)) {
      logger.info("Wrong batch prediction id");
      return null;
    }
    return deleteResource(BIGML_URL + batchPredictionId);
  }

  /**
   * Deletes a batch prediction.
   *
   * DELETE /andromeda/batchprediction/id?username=$BIGML_USERNAME;api_key=
   * $BIGML_API_KEY; HTTP/1.1
   *
   * @param batchPrediction
   *          a batchPrediction JSONObject.
   *
   */
  @Override
  public JSONObject delete(final JSONObject batchPrediction) {
    String resourceId = (String) batchPrediction.get("resource");
    return delete(resourceId);
  }



  /**
   * Retrieves a remote file.
   *
   * Uses HTTP GET to download a file object with a BigML `url`.
   *
   */
  private JSONObject download(final String url, final String fileName) {
    int code = HTTP_INTERNAL_SERVER_ERROR;

    JSONObject error = new JSONObject();
    String csv = "";
    try {
      HttpClient httpclient = Utils.httpClient();
      HttpGet httpget = new HttpGet(url + bigmlAuth);
      httpget.setHeader("Accept", JSON);

      HttpResponse response = httpclient.execute(httpget);
      HttpEntity resEntity = response.getEntity();
      code = response.getStatusLine().getStatusCode();

      csv = Utils.inputStreamAsString(resEntity.getContent());
      if (code == HTTP_OK) {
        if (fileName != null) {
          File file = new File(fileName);
          if (!file.exists()) {

          }
          BufferedWriter output = new BufferedWriter(new FileWriter(file));
          output.write(csv);
          output.close();
        }
      } else {
        if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED
            || code == HTTP_NOT_FOUND) {
          error = (JSONObject) JSONValue.parse(Utils
              .inputStreamAsString(resEntity.getContent()));
          logger.info("Error downloading:" + code);
        } else {
          logger.info("Unexpected error (" + code + ")");
          code = HTTP_INTERNAL_SERVER_ERROR;
        }
      }

    } catch (Throwable e) {
      logger.error("Error downloading batch prediction", e);
    }

    JSONObject result = new JSONObject();
    result.put("code", code);
    result.put("error", error);
    result.put("csv", csv);
    return result;

  }

}