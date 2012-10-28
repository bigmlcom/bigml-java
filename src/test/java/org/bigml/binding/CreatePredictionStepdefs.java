package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class CreatePredictionStepdefs {

  // Logging
  Logger logger = LoggerFactory.getLogger(CreatePredictionStepdefs.class);
  int status;
  String location = null;
  JSONObject source = null;
  JSONObject dataset = null;
  JSONObject model = null;
  JSONObject prediction = null;
  JSONObject evaluation = null;

  @Given("^the resource has been created")
  public void the_resource_has_been_created() {
    assertEquals(status, AbstractResource.HTTP_CREATED);
  }

  // Source steps
  @Given("^I create a data source uploading a \"([^\"]*)\" file$")
  public void I_create_a_data_source_uploading_a_file(String fileName) throws AuthenticationException {
    JSONObject resource = BigMLClient.getInstance().createSource(fileName, "new source", null);

    // update status
    status = (Integer) resource.get("code");
    location = (String) resource.get("location");
    source = (JSONObject) resource.get("object");

    the_resource_has_been_created();
  }

  @Given("^I wait until the resource status code is either (\\d) or (\\d) less than (\\d+)")
  public void I_wait_until_source_status_code_is(int code1, int code2, int secs) throws AuthenticationException {
    Long code = (Long) ((JSONObject) source.get("status")).get("code");
    GregorianCalendar start = new GregorianCalendar();
    start.add(Calendar.SECOND, secs);
    Date end = start.getTime();

    while (code.intValue() != code1 && code.intValue() != code2) {
      try {
        Thread.sleep(3);
      } catch (InterruptedException e) {
      }
      assertTrue("Time exceded ", end.after(new Date()));
      I_get_the_source((String) source.get("resource"));
      code = (Long) ((JSONObject) source.get("status")).get("code");
    }
    assertEquals(code.intValue(), code1);
  }

  @Given("^I wait until the source is ready less than (\\d+) secs$")
  public void I_wait_until_the_source_is_ready_less_than_secs(int secs) throws AuthenticationException {
    I_wait_until_source_status_code_is(AbstractResource.FINISHED, AbstractResource.FAULTY, secs);
  }

  @Given("^I get the source \"(.*)\"")
  public void I_get_the_source(String sourceId) throws AuthenticationException {
    JSONObject resource = BigMLClient.getInstance().getSource(sourceId);
    Integer code = (Integer) resource.get("code");
    assertEquals(code.intValue(), AbstractResource.HTTP_OK);
    source = (JSONObject) resource.get("object");
  }

  // Dataset steps
  @Given("^I create a dataset$")
  public void I_create_a_dataset() throws AuthenticationException {
    String sourceId = (String) source.get("resource");
    JSONObject resource = BigMLClient.getInstance().createDataset(sourceId, null, 5);
    status = (Integer) resource.get("code");
    location = (String) resource.get("location");
    dataset = (JSONObject) resource.get("object");
    the_resource_has_been_created();
  }

  @Given("^I wait until the dataset status code is either (\\d) or (\\d) less than (\\d+)")
  public void I_wait_until_dataset_status_code_is(int code1, int code2, int secs) throws AuthenticationException {
    Long code = (Long) ((JSONObject) dataset.get("status")).get("code");
    GregorianCalendar start = new GregorianCalendar();
    start.add(Calendar.SECOND, secs);
    Date end = start.getTime();
    while (code.intValue() != code1 && code.intValue() != code2) {
      try {
        Thread.sleep(3);
      } catch (InterruptedException e) {
      }
      assertTrue("Time exceded ", end.after(new Date()));
      I_get_the_dataset((String) dataset.get("resource"));
      code = (Long) ((JSONObject) dataset.get("status")).get("code");
    }
    assertEquals(code.intValue(), code1);
  }

  @Given("^I wait until the dataset is ready less than (\\d+) secs$")
  public void I_wait_until_the_dataset_is_ready_less_than_secs(int secs) throws AuthenticationException {
    I_wait_until_dataset_status_code_is(AbstractResource.FINISHED, AbstractResource.FAULTY, secs);
  }

  @Given("^I get the dataset \"(.*)\"")
  public void I_get_the_dataset(String datasetId) throws AuthenticationException {
    JSONObject resource = BigMLClient.getInstance().getDataset(datasetId);
    Integer code = (Integer) resource.get("code");
    assertEquals(code.intValue(), AbstractResource.HTTP_OK);
    dataset = (JSONObject) resource.get("object");
  }

  // Model steps
  @Given("^I create a model$")
  public void I_create_a_model() throws AuthenticationException {
    String datasetId = (String) dataset.get("resource");
    JSONObject resource = BigMLClient.getInstance().createModel(datasetId, null, 5);
    status = (Integer) resource.get("code");
    location = (String) resource.get("location");
    model = (JSONObject) resource.get("object");
    the_resource_has_been_created();
  }

  @Given("^I wait until the model status code is either (\\d) or (\\d) less than (\\d+)")
  public void I_wait_until_model_status_code_is(int code1, int code2, int secs) throws AuthenticationException {
    Long code = (Long) ((JSONObject) model.get("status")).get("code");
    GregorianCalendar start = new GregorianCalendar();
    start.add(Calendar.SECOND, secs);
    Date end = start.getTime();
    while (code.intValue() != code1 && code.intValue() != code2) {
      try {
        Thread.sleep(3);
      } catch (InterruptedException e) {
      }
      assertTrue("Time exceded ", end.after(new Date()));
      I_get_the_model((String) model.get("resource"));
      code = (Long) ((JSONObject) model.get("status")).get("code");
    }
    assertEquals(code.intValue(), code1);
  }

  @Given("^I wait until the model is ready less than (\\d+) secs$")
  public void I_wait_until_the_model_is_ready_less_than_secs(int secs) throws AuthenticationException {
    I_wait_until_model_status_code_is(AbstractResource.FINISHED, AbstractResource.FAULTY, secs);
  }

  @Given("^I get the model \"(.*)\"")
  public void I_get_the_model(String modelId) throws AuthenticationException {
    JSONObject resource = BigMLClient.getInstance().getModel(modelId);
    Integer code = (Integer) resource.get("code");
    assertEquals(code.intValue(), AbstractResource.HTTP_OK);
    model = (JSONObject) resource.get("object");
  }
  
  // Evaluation steps
  @Given("^I create a evaluation$")
  public void I_create_a_evaluation() throws AuthenticationException {
    String modelId = (String) model.get("resource");
    String datasetId = (String) dataset.get("resource");
    
    JSONObject resource = BigMLClient.getInstance().createEvaluation(modelId, datasetId, null, 5);
    status = (Integer) resource.get("code");
    location = (String) resource.get("location");
    evaluation = (JSONObject) resource.get("object");
    the_resource_has_been_created();
  }
 
  @Given("^I wait until the evaluation status code is either (\\d) or (\\d) less than (\\d+)")
  public void I_wait_until_evaluation_status_code_is(int code1, int code2, int secs) throws AuthenticationException {
    Long code = (Long) ((JSONObject) evaluation.get("status")).get("code");
    GregorianCalendar start = new GregorianCalendar();
    start.add(Calendar.SECOND, secs);
    Date end = start.getTime();
    while (code.intValue() != code1 && code.intValue() != code2) {
      try {
        Thread.sleep(3);
      } catch (InterruptedException e) {
      }
      assertTrue("Time exceded ", end.after(new Date()));
      I_get_the_evaluation((String) evaluation.get("resource"));
      code = (Long) ((JSONObject) evaluation.get("status")).get("code");
    }
    assertEquals(code.intValue(), code1);
  }

  @Given("^I wait until the evaluation is ready less than (\\d+) secs$")
  public void I_wait_until_the_evaluation_is_ready_less_than_secs(int secs) throws AuthenticationException {
    I_wait_until_evaluation_status_code_is(AbstractResource.FINISHED, AbstractResource.FAULTY, secs);
  }
  
  @Given("^I get the evaluation \"(.*)\"")
  public void I_get_the_evaluation(String evaluationId) throws AuthenticationException {
    JSONObject resource = BigMLClient.getInstance().getEvaluation(evaluationId);
    Integer code = (Integer) resource.get("code");
    assertEquals(code.intValue(), AbstractResource.HTTP_OK);
    evaluation = (JSONObject) resource.get("object");
  }

  // Prediction steps
  @When("^I create a prediction for \"(.*)\"$")
  public void I_create_a_prediction_for_petal_length(String args) throws AuthenticationException {
    String modelId = (String) model.get("resource");
    JSONObject resource = BigMLClient.getInstance().createPrediction(modelId, args, 5);
    status = (Integer) resource.get("code");
    location = (String) resource.get("location");
    prediction = (JSONObject) resource.get("object");
    the_resource_has_been_created();
  }

  @Given("^I get the prediction \"(.*)\"")
  public void I_get_the_prediction(String predictionId) throws AuthenticationException {
    JSONObject resource = BigMLClient.getInstance().getPrediction(predictionId);
    Integer code = (Integer) resource.get("code");
    assertEquals(code.intValue(), AbstractResource.HTTP_OK);
    prediction = (JSONObject) resource.get("object");
  }

  @Then("^the prediction for \"([^\"]*)\" is \"([^\"]*)\"$")
  public void the_prediction_for_is(String expected, String pred) {
    JSONObject obj = (JSONObject) prediction.get("prediction");
    String objective = (String) obj.get(expected);
    assertEquals(objective, pred);
  }

  // Listing
  @Then("^test listing$")
  public void test_listing() throws AuthenticationException {
    JSONObject listing = BigMLClient.getInstance().listSources("");
    assertEquals(((Integer) listing.get("code")).intValue(), AbstractResource.HTTP_OK);
    listing = BigMLClient.getInstance().listDatasets("");
    assertEquals(((Integer) listing.get("code")).intValue(), AbstractResource.HTTP_OK);
    listing = BigMLClient.getInstance().listModels("");
    assertEquals(((Integer) listing.get("code")).intValue(), AbstractResource.HTTP_OK);
    listing = BigMLClient.getInstance().listEvaluations("");
    assertEquals(((Integer) listing.get("code")).intValue(), AbstractResource.HTTP_OK);
    listing = BigMLClient.getInstance().listPredictions("");
    assertEquals(((Integer) listing.get("code")).intValue(), AbstractResource.HTTP_OK);
    
  }

  // Delete test data
  @Then("^delete test data$")
  public void delete_test_data() throws AuthenticationException {
    if (prediction != null) {
      BigMLClient.getInstance().deletePrediction((String) prediction.get("resource"));
    }
    if (evaluation != null) {
        BigMLClient.getInstance().deleteEvaluation((String) evaluation.get("resource"));
    }
    if (model != null) {
      BigMLClient.getInstance().deleteModel((String) model.get("resource"));
    }
    if (dataset != null) {
      BigMLClient.getInstance().deleteDataset((String) dataset.get("resource"));
    }
    if (source != null) {
      BigMLClient.getInstance().deleteSource((String) source.get("resource"));
    }
  }
  
}
