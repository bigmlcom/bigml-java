package org.bigml.binding;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

public class CreatePredictionFromLocalModelStepdefs {

  // Logging
  Logger logger = LoggerFactory.getLogger(CreatePredictionFromLocalModelStepdefs.class);

  JSONObject model = null;
  LocalPredictiveModel predictiveModel;
  CreatePredictionStepdefs createPredictionSteps = new CreatePredictionStepdefs();

  @Given("^I get the model with modelId \"([^\"]*)\"$")
  public void I_get_the_model_with_modelId(String modelId) throws AuthenticationException {
      model = BigMLClient.getInstance().getModel(modelId);
      assertTrue("", model != null);
  }

  @Given("^I create a model from the data source \"([^\"]*)\" waiting less than (\\d+), (\\d+) and (\\d+) secs in each step$")
  public void I_create_a_model_from_the_data_source_waiting_less_than_and_secs_in_each_step(String data, int sourceTime, int dataseTime, int modelTime) throws AuthenticationException {
	  createPredictionSteps.I_create_a_data_source_uploading_a_file(data);
	  createPredictionSteps.I_wait_until_the_source_is_ready_less_than_secs(sourceTime);
	  createPredictionSteps.I_create_a_dataset();
	  createPredictionSteps.I_wait_until_the_dataset_is_ready_less_than_secs(dataseTime);
	  createPredictionSteps.I_create_a_model();
	  JSONObject responseModel = createPredictionSteps.I_wait_until_the_model_is_ready_less_than_secs_and_return(modelTime);
	  I_get_the_model_with_modelId((String) responseModel.get("resource"));
  }

  @Given("^I create the local model$")
  public void I_create_the_local_model() throws Exception {
    predictiveModel = new LocalPredictiveModel(model);
    assertTrue("", predictiveModel != null);
  }

  @Then("^the local prediction by name for \"(.*)\" is \"([^\"]*)\"$")
  public void the_local_prediction_by_name_for_is(String args, String pred) {
    String prediction = (String) predictiveModel.predict(args, false);
    assertTrue("", prediction!=null && prediction.equals(pred));
  }

  @Then("^the local prediction for \"(.*)\" is \"([^\"]*)\"$")
  public void the_local_prediction_for_is(String args, String pred) {
    String prediction = (String) predictiveModel.predict(args, null);
    assertTrue("", prediction!=null && prediction.equals(pred));
  }

}
