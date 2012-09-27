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
  
  @Given("^I get the model with modelId \"([^\"]*)\"$")
  public void I_get_the_model_with_modelId(String modelId) throws AuthenticationException {
      model = BigMLClient.getInstance().getModel(modelId);
      assertTrue("", model != null);
  }
  
  @Given("^I create the local model$")
  public void I_create_the_local_model() throws Exception {
	  predictiveModel = new LocalPredictiveModel(model);
      assertTrue("", predictiveModel != null);
  }
  
  @Then("^the local prediction by name for \"(.*)\" is \"([^\"]*)\"$")
  public void the_local_prediction_by_name_for_is(String args, String pred) {
    String prediction = predictiveModel.predict(args, null);
    assertTrue("", prediction!=null && prediction.equals(pred));
  }
  
  @Then("^the local prediction for \"(.*)\" is \"([^\"]*)\"$")
  public void the_local_prediction_for_is(String args, String pred) {
    String prediction = predictiveModel.predict(args, false);
    assertTrue("", prediction!=null && prediction.equals(pred));
  }
  
}
