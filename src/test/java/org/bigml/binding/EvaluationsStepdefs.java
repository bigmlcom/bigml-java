package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;


public class EvaluationsStepdefs {

	// Logging
	Logger logger = LoggerFactory.getLogger(EvaluationsStepdefs.class);

	CommonStepdefs commonSteps = new CommonStepdefs();
	  	
  	@Autowired
    private ContextRepository context;
  	  	
  	@Given("^I create a evaluation$")
    public void I_create_a_evaluation() throws AuthenticationException {
      String modelId = (String) context.model.get("resource");
      String datasetId = (String) context.dataset.get("resource");

      JSONObject resource = BigMLClient.getInstance().createEvaluation(modelId, datasetId, null, 5, 3);
      context.status = (Integer) resource.get("code");
      context.location = (String) resource.get("location");
      context.evaluation = (JSONObject) resource.get("object");
      commonSteps.the_resource_has_been_created_with_status(context.status);
    }
  	
  	
  	@When("^I create an evaluation for the model with the dataset$")
  	public void I_create_an_evaluation_for_the_model_with_the_dataset() throws Throwable {
  	    I_create_a_evaluation();
  	}
  	
  	
  	@When("^I create an evaluation for the ensemble with the dataset$")
    public void I_create_an_evaluation_for_the_ensemble_with_the_dataset() throws Throwable {
  		String ensembleId = (String) context.ensemble.get("resource");
        String datasetId = (String) context.dataset.get("resource");

        JSONObject resource = BigMLClient.getInstance().createEvaluation(ensembleId, datasetId, null, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.evaluation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
  	
  	
    @Given("^I wait until the evaluation status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_evaluation_status_code_is(int code1, int code2, int secs) throws AuthenticationException {
      Long code = (Long) ((JSONObject) context.evaluation.get("status")).get("code");
      GregorianCalendar start = new GregorianCalendar();
      start.add(Calendar.SECOND, secs);
      Date end = start.getTime();
      while (code.intValue() != code1 && code.intValue() != code2) {
        try {
          Thread.sleep(3);
        } catch (InterruptedException e) {
        }
        assertTrue("Time exceded ", end.after(new Date()));
        I_get_the_evaluation((String) context.evaluation.get("resource"));
        code = (Long) ((JSONObject) context.evaluation.get("status")).get("code");
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
      context.evaluation = (JSONObject) resource.get("object");
    }
    
    
    @Then("^the measured \"([^\"]*)\" is (\\d+)$")
    public void the_measured_is(String measure, float value) throws Throwable {
    	Long measureLong = (Long) Utils.getJSONObject(context.evaluation,  "result.model."+measure);
        assertTrue(measureLong.floatValue() == value);
    }
    
    
    @Then("^the measured \"([^\"]*)\" is greater than ([\\d,.]+)$")
    public void the_measured_is_greater_than(String measure, double value) throws Throwable {
    	double measureLong = (Double) Utils.getJSONObject(context.evaluation,  "result.model."+measure);
        assertTrue(measureLong > value);
    }
    
}