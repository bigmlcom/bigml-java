package org.bigml.binding.resources;

import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.bigml.binding.AuthenticationException;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to create, retrieve, list, update, and delete sources, datasets, models and
 * predictions.
 *
 * Full API documentation on the API can be found from BigML at: https://bigml.com/developers
 *
 *
 */
public abstract class AbstractResource {

  // Logging
  Logger logger = LoggerFactory.getLogger(AbstractResource.class);

  public final static String SOURCE_PATH = "source";
  public final static String DATASET_PATH = "dataset";
  public final static String MODEL_PATH = "model";
  public final static String PREDICTION_PATH = "prediction";
  public final static String EVALUATION_PATH = "evaluation";
  public final static String ENSEMBLE_PATH = "ensemble";

  // Base Resource regular expressions
  static String SOURCE_RE = "^" + SOURCE_PATH + "/[a-f,0-9]{24}$";
  static String DATASET_RE = "^(public/|shared/|)" + DATASET_PATH + "/[a-f,0-9]{24}$";
  static String MODEL_RE = "^(public/|)" + MODEL_PATH + "/[a-f,0-9]{24}$|^|shared/" + MODEL_PATH + "/[a-zA-Z0-9]{27}$";
  static String PREDICTION_RE = "^" + PREDICTION_PATH + "/[a-f,0-9]{24}$";
  static String EVALUATION_RE = "^" + EVALUATION_PATH + "/[a-f,0-9]{24}$";
  static String ENSEMBLE_RE = "^" + ENSEMBLE_PATH + "/[a-f,0-9]{24}$";

  // Headers
  static String JSON = "application/json";

  // HTTP Status Codes from https://bigml.com/developers/status_codes
  public static int HTTP_OK = 200;
  public static int HTTP_CREATED = 201;
  public static int HTTP_ACCEPTED = 202;
  public static int HTTP_NO_CONTENT = 204;
  public static int HTTP_BAD_REQUEST = 400;
  public static int HTTP_UNAUTHORIZED = 401;
  public static int HTTP_PAYMENT_REQUIRED = 402;
  public static int HTTP_FORBIDDEN = 403;
  public static int HTTP_NOT_FOUND = 404;
  public static int HTTP_METHOD_NOT_ALLOWED = 405;
  public static int HTTP_LENGTH_REQUIRED = 411;
  public static int HTTP_REQUEST_ENTITY_TOO_LARGE = 413;
  public static int HTTP_UNSUPPORTED_MEDIA_TPE = 415;
  public static int HTTP_INTERNAL_SERVER_ERROR = 500;
  public static int HTTP_SERVICE_UNAVAILABLE = 500;

  // Resource status codes
  public static int WAITING = 0;
  public static int QUEUED = 1;
  public static int STARTED = 2;
  public static int IN_PROGRESS = 3;
  public static int SUMMARIZED = 4;
  public static int FINISHED = 5;
  public static int UPLOADING = 6;
  public static int FAULTY = -1;
  public static int UNKNOWN = -2;
  public static int RUNNABLE = -3;


  static HashMap<Integer, String> STATUSES = new HashMap<Integer, String>();
  static {
    STATUSES.put(WAITING, "WAITING");
    STATUSES.put(QUEUED, "QUEUED");
    STATUSES.put(STARTED, "STARTED");
    STATUSES.put(IN_PROGRESS, "IN_PROGRESS");
    STATUSES.put(SUMMARIZED, "SUMMARIZED");
    STATUSES.put(FINISHED, "FINISHED");
    STATUSES.put(UPLOADING, "UPLOADING");
    STATUSES.put(FAULTY, "FAULTY");
    STATUSES.put(UNKNOWN, "UNKNOWN");
    STATUSES.put(RUNNABLE, "RUNNABLE");
  }

//  BIGML_USERNAME=xxxx
//  BIGML_API_KEY=yyyyyyyyyyy
//  BIGML_AUTH="username=$BIGML_USERNAME;api_key=$BIGML_API_KEY"
  protected String bigmlUser;
  protected String bigmlApiKey;
  protected String bigmlAuth;

  protected boolean devMode;

  //Base URL
  protected String BIGML_URL;

  protected String SOURCE_URL;
  protected String DATASET_URL;
  protected String MODEL_URL;
  protected String PREDICTION_URL;
  protected String EVALUATION_URL;
  protected String ENSEMBLE_URL;


  protected void init() {
	  try {
		  BIGML_URL = BigMLClient.getInstance(devMode).getBigMLUrl();
		  SOURCE_URL = BIGML_URL + SOURCE_PATH;
		  DATASET_URL = BIGML_URL + DATASET_PATH;
		  MODEL_URL = BIGML_URL + MODEL_PATH;
		  PREDICTION_URL = BIGML_URL + PREDICTION_PATH;
		  EVALUATION_URL = BIGML_URL + EVALUATION_PATH;
		  ENSEMBLE_URL = BIGML_URL + ENSEMBLE_PATH;
	  } catch (AuthenticationException ae) {

	  }
  }


  /**
   * Create a new resource.
   */
  public JSONObject createResource(final String urlString, final String json) {
    int code = HTTP_INTERNAL_SERVER_ERROR;
    String resourceId = null;
    JSONObject resource = null;
    String location = urlString;

    JSONObject error = new JSONObject();
    JSONObject status = new JSONObject();
    status.put("code", code);
    status.put("message", "The resource couldn't be created");
    error.put("status", status);

    try {
      HttpClient httpclient = Utils.httpClient();
      HttpPost httppost = new HttpPost(urlString + bigmlAuth);
      httppost.setHeader("Content-Type", JSON);

      StringEntity reqEntity = new StringEntity(json);
      httppost.setEntity(reqEntity);

      HttpResponse response = httpclient.execute(httppost);
      HttpEntity resEntity = response.getEntity();
      code = response.getStatusLine().getStatusCode();
      if (code == HTTP_CREATED) {
        location = (String) response.getHeaders("location")[0].getValue();
        resource = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        resourceId = (String) resource.get("resource");
        error = new JSONObject();
      } else {
        if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED || code == HTTP_PAYMENT_REQUIRED || code == HTTP_NOT_FOUND) {
          error = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        } else {
          logger.info("Unexpected error (" + code + ")");
          code = HTTP_INTERNAL_SERVER_ERROR;
        }
      }
    } catch (Throwable e) {
      logger.error("Error creating resource", e);
    }

    JSONObject result = new JSONObject();
    result.put("code", code);
    result.put("resource", resourceId);
    result.put("location", location);
    result.put("object", resource);
    result.put("error", error);
    return result;
  }


  /**
   * Retrieve a resource.
   */
  public JSONObject getResource(final String urlString) {
	return getResource(urlString, null, null, null);
  }


  /**
   * Retrieve a resource.
   */
  public JSONObject getResource(final String urlString, final String queryString) {
    return getResource(urlString, queryString, null, null);
  }


  /**
   * Retrieve a resource.
   */
  public JSONObject getResource(final String urlString, final String queryString, final String apiUser, final String apiKey) {
    int code = HTTP_INTERNAL_SERVER_ERROR;
    JSONObject resource = null;
    String resourceId = null;
    String location = urlString;

    JSONObject error = new JSONObject();
    JSONObject status = new JSONObject();
    status.put("code", code);
    status.put("message", "The resource couldn't be retrieved");
    error.put("status", status);

    try {
      String query = queryString != null ? queryString : "";
      String auth = apiUser!=null && apiKey!=null ?
    	  "?username=" + apiUser + ";api_key=" + apiKey + ";" :
    	  bigmlAuth;

      HttpClient httpclient = Utils.httpClient();
      HttpGet httpget = new HttpGet(urlString + auth + query);

      httpget.setHeader("Accept", JSON);

      HttpResponse response = httpclient.execute(httpget);
      HttpEntity resEntity = response.getEntity();
      code = response.getStatusLine().getStatusCode();

      if (code == HTTP_OK) {
        resource = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        resourceId = (String) resource.get("resource");
        error = new JSONObject();
      } else {
        if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED || code == HTTP_NOT_FOUND) {
          error = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        } else {
          logger.info("Unexpected error (" + code + ")");
          code = HTTP_INTERNAL_SERVER_ERROR;
        }
      }

    } catch (Throwable e) {
      logger.error("Error getting resource", e);
    }

    JSONObject result = new JSONObject();
    result.put("code", code);
    result.put("resource", resourceId);
    result.put("location", location);
    result.put("object", resource);
    result.put("error", error);
    return result;
  }



  /**
   * List resources.
   */
  public JSONObject listResources(final String urlString, final String queryString) {
    int code = HTTP_INTERNAL_SERVER_ERROR;
    JSONObject meta = null;
    JSONArray resources = null;

    JSONObject error = new JSONObject();
    JSONObject status = new JSONObject();
    status.put("code", code);
    status.put("message", "The resource couldn't be listed");
    error.put("status", status);

    try {
      String query = queryString != null ? queryString : "";
      HttpClient httpclient = Utils.httpClient();
      HttpGet httpget = new HttpGet(urlString + bigmlAuth + query);

      HttpResponse response = httpclient.execute(httpget);
      HttpEntity resEntity = response.getEntity();
      code = response.getStatusLine().getStatusCode();

      if (code == HTTP_OK) {
        JSONObject resource = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        meta = (JSONObject) resource.get("meta");
        resources = (JSONArray) resource.get("objects");
        error = new JSONObject();
      } else {
        if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED || code == HTTP_NOT_FOUND) {
          error = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        } else {
          logger.info("Unexpected error (" + code + ")");
          code = HTTP_INTERNAL_SERVER_ERROR;
        }
      }
    } catch (Throwable e) {
      logger.error("Error listing resources ", e);
    }

    JSONObject result = new JSONObject();
    result.put("code", code);
    result.put("meta", meta);
    result.put("objects", resources);
    result.put("error", error);
    return result;
  }


  /**
   * Update a resource.
   */
  public JSONObject updateResource(final String urlString, final String json) {
    int code = HTTP_INTERNAL_SERVER_ERROR;
    JSONObject resource = null;
    String resourceId = null;
    String location = urlString;
    JSONObject error = new JSONObject();
    JSONObject status = new JSONObject();
    status.put("code", code);
    status.put("message", "The resource couldn't be updated");
    error.put("status", status);

    try {
      HttpClient httpclient = Utils.httpClient();
      HttpPut httpput = new HttpPut(urlString + bigmlAuth);

      httpput.setHeader("Content-Type", JSON);
      StringEntity reqEntity = new StringEntity(json);
      httpput.setEntity(reqEntity);

      HttpResponse response = httpclient.execute(httpput);
      HttpEntity resEntity = response.getEntity();
      code = response.getStatusLine().getStatusCode();

      if (code == HTTP_ACCEPTED) {
        resource = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        resourceId = (String) resource.get("resource");
        error = new JSONObject();
      } else {
        if (code == HTTP_UNAUTHORIZED || code == HTTP_PAYMENT_REQUIRED || code == HTTP_METHOD_NOT_ALLOWED) {
          error = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        } else {
          logger.info("Unexpected error (" + code + ")");
          code = HTTP_INTERNAL_SERVER_ERROR;
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      logger.error("Error updating resource", e);
    }

    JSONObject result = new JSONObject();
    result.put("code", code);
    result.put("resource", resourceId);
    result.put("location", location);
    result.put("object", resource);
    result.put("error", error);
    return result;
  }


  /**
   * Delete a resource.
   */
  public JSONObject deleteResource(final String urlString) {
    int code = HTTP_INTERNAL_SERVER_ERROR;

    JSONObject error = new JSONObject();
    JSONObject status = new JSONObject();
    status.put("code", code);
    status.put("message", "The resource couldn't be deleted");
    error.put("status", status);

    try {
      HttpClient httpclient = Utils.httpClient();
      HttpDelete httpdelete = new HttpDelete(urlString + bigmlAuth);
      HttpResponse response = httpclient.execute(httpdelete);
      HttpEntity resEntity = response.getEntity();
      code = response.getStatusLine().getStatusCode();

      if (code == HTTP_NO_CONTENT) {
        error = new JSONObject();
      } else {
        if (code == HTTP_BAD_REQUEST || code == HTTP_UNAUTHORIZED || code == HTTP_NOT_FOUND) {
          error = (JSONObject) JSONValue.parse(Utils.inputStreamAsString(resEntity.getContent()));
        } else {
          logger.info("Unexpected error (" + code + ")");
          code = HTTP_INTERNAL_SERVER_ERROR;
        }
      }
    } catch (Throwable e) {
      logger.error("Error deleting resource ", e);
    }

    JSONObject result = new JSONObject();
    result.put("code", code);
    result.put("error", error);
    return result;
  }


  /**
   * Return a dictionary of fields
   *
   */
  public JSONObject getFields(final String resourceId) {
    if (resourceId == null || resourceId.length() == 0
            || !(resourceId.matches(SOURCE_RE) || resourceId.matches(DATASET_RE) || resourceId.matches(MODEL_RE) || resourceId.matches(PREDICTION_RE))) {
      logger.info("Wrong resource id");
      return null;
    }

    JSONObject resource = get(BIGML_URL + resourceId);

    JSONObject obj = (JSONObject) resource.get("object");
    if (obj == null) {
      obj = (JSONObject) resource.get("error");
    }

    if ((Integer) resource.get("code") == HTTP_OK) {
      if (resourceId.matches(MODEL_RE)) {
        JSONObject model = (JSONObject) resource.get("model");
        return (JSONObject) model.get("fields");
      } else {
        return (JSONObject) obj.get("fields");
      }
    }
    return null;
  }


  /**
   * Maps status code to string.
   *
   */
  public String status(final String resourceId) {
    if (resourceId == null || resourceId.length() == 0
            || !(resourceId.matches(SOURCE_RE) || resourceId.matches(DATASET_RE) || resourceId.matches(MODEL_RE) || resourceId.matches(PREDICTION_RE))) {
      logger.info("Wrong resource id");
      return null;
    }
    JSONObject resource = get(BIGML_URL + resourceId);
    JSONObject obj = (JSONObject) resource.get("object");
    if (obj == null) {
      obj = (JSONObject) resource.get("error");
    }
    JSONObject status = (JSONObject) obj.get("status");
    Long code = (Long) status.get("code");
    if (STATUSES.get(code.intValue()) != null) {
      return STATUSES.get(code.intValue());
    } else {
      return "UNKNOWN";
    }
  }


  /**
   * Check whether a resource' status is FINISHED.
   *
   * @param resource a resource
   *
   */
  public boolean isResourceReady(final JSONObject resource) {
    if (resource == null) {
      return false;
    }
    JSONObject obj = (JSONObject) resource.get("object");
    if (obj == null) {
      obj = (JSONObject) resource.get("error");
    }

    if (obj == null) return false;

    JSONObject status = (JSONObject) obj.get("status");
    Number code = (Number) resource.get("code");
    Number statusCode = (Number) status.get("code");
    return (code != null && code.intValue() == HTTP_OK
            && statusCode != null && statusCode.intValue() == FINISHED);
  }


  // ################################################################
  // #
  // # Abstract methods
  // #
  // ################################################################

  /**
   * Retrieve a resource.
   *
   */
  abstract JSONObject get(final String resourceId);

  /**
   * Retrieve a resource.
   *
   */
  abstract JSONObject get(final JSONObject resource);

  /**
   * Check whether a resource' status is FINISHED.
   *
   */
  abstract boolean isReady(final String resourceId);

  /**
   * Check whether a resource' status is FINISHED.
   *
   */
  abstract boolean isReady(final JSONObject resource);

  /**
   * List all your resource.
   *
   */
  abstract public JSONObject list(final String queryString);

  /**
   * Update a resource.
   *
   */
  abstract public JSONObject update(final String resourceId, final String json);

  /**
   * Update a resource.
   *
   */
  abstract public JSONObject update(final JSONObject resource, final JSONObject json);

  /**
   * Delete a resource.
   *
   */
  abstract public JSONObject delete(final String resourceId);

  /**
   * Delete a resource.
   *
   */
  abstract public JSONObject delete(final JSONObject resource);

}
