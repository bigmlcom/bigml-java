package org.bigml.binding;

import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;


public class LocalModelsStepdefs {

	// Logging
	Logger logger = LoggerFactory.getLogger(LocalModelsStepdefs.class);

	LocalPredictiveModel predictiveModel;
	CommonStepdefs commonSteps = new CommonStepdefs();
	
	
  	@Autowired
    private ContextRepository context;
  	
  	
  	@Given("^I create a local model$")
    public void I_create_a_local_model() throws Exception {
  	  predictiveModel = new LocalPredictiveModel(context.model);
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
    

    @Then("^the local prediction by name=(true|false) for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_byname_for_is(String by_name, String args, String pred) {
      Boolean byName = new Boolean(by_name);
      String prediction = (String) predictiveModel.predict(args, byName);
      assertTrue("", prediction!=null && prediction.equals(pred));
    }
    
}