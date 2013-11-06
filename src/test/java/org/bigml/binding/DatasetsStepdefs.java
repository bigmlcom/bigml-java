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
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.When;


public class DatasetsStepdefs {

	// Logging
	Logger logger = LoggerFactory.getLogger(DatasetsStepdefs.class);

	CommonStepdefs commonSteps = new CommonStepdefs();

  	@Autowired
    private ContextRepository context;


  	@Given("^I create a dataset$")
	public void I_create_a_dataset() throws AuthenticationException {
		String sourceId = (String) context.source.get("resource");
  	    JSONObject resource = BigMLClient.getInstance().createDataset(sourceId, null, 5, null);
  	    context.status = (Integer) resource.get("code");
  	    context.location = (String) resource.get("location");
  	    context.dataset = (JSONObject) resource.get("object");
  	    commonSteps.the_resource_has_been_created_with_status(context.status);
	}


  	@Given("^I create a dataset with \"(.*)\"$")
  	public void I_create_a_dataset_with_options(String args) throws Throwable {
  	    String sourceId = (String) context.source.get("resource");
  	  	JSONObject resource = BigMLClient.getInstance().createDataset(sourceId, args, 5, null);
  	  	context.status = (Integer) resource.get("code");
	    context.location = (String) resource.get("location");
	    context.dataset = (JSONObject) resource.get("object");
	    commonSteps.the_resource_has_been_created_with_status(context.status);
  	}


	@Given("^I wait until the dataset status code is either (\\d) or (\\d) less than (\\d+)")
	public void I_wait_until_dataset_status_code_is(int code1, int code2, int secs) throws AuthenticationException {
  	    Long code = (Long) ((JSONObject) context.dataset.get("status")).get("code");
  	    GregorianCalendar start = new GregorianCalendar();
  	    start.add(Calendar.SECOND, secs);
  	    Date end = start.getTime();
  	    while (code.intValue() != code1 && code.intValue() != code2) {
  	      try {
  	        Thread.sleep(3000);
  	      } catch (InterruptedException e) {
  	      }
  	      assertTrue("Time exceded ", end.after(new Date()));
  	      I_get_the_dataset((String) context.dataset.get("resource"));
  	      code = (Long) ((JSONObject) context.dataset.get("status")).get("code");
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
  	    context.dataset = (JSONObject) resource.get("object");
	}




	// ---------------------------------------------------------------------
    // split_dataset.feature
    // ---------------------------------------------------------------------

	@Given("^I create a dataset extracting a ([\\d,.]+) sample$")
  	public void I_create_a_dataset_extracting_a_sample(double rate) throws Throwable {
  		 String datasetId = (String) context.dataset.get("resource");


  	}

  	/*
    world.origin_dataset = world.dataset
    resource = world.api.create_dataset(world.dataset['resource'], {'sample_rate': float(rate)})
    world.status = resource['code']
    assert world.status == HTTP_CREATED
    world.location = resource['location']
    world.dataset = resource['object']
    world.datasets.append(resource['resource'])
    */


	/*
	@When("^I compare the datasets' instances$")
public void I_compare_the_datasets_instances() throws Throwable {
    // Express the Regexp above with the code you wish you had
    throw new PendingException();
}

@Then("^the proportion of instances between datasets is (\\d+).(\\d+)$")
public void the_proportion_of_instances_between_datasets_is_(int arg1, int arg2) throws Throwable {
    // Express the Regexp above with the code you wish you had
    throw new PendingException();
}
*/


	// ---------------------------------------------------------------------
    // create_public_dataset.feature
    // ---------------------------------------------------------------------

	@Given("^I make the dataset public$")
	public void I_make_the_dataset_public() throws Throwable {
		JSONObject changes = new JSONObject();
		changes.put("private", new Boolean(false));

		JSONObject resource = BigMLClient.getInstance().updateDataset(context.dataset, changes);
		context.status = (Integer) resource.get("code");
	    context.location = (String) resource.get("location");
	    context.dataset = (JSONObject) resource.get("object");
	    commonSteps.the_resource_has_been_updated_with_status(context.status);
	}


	@When("^I get the dataset status using the dataset's public url$")
	public void I_get_the_dataset_status_using_the_dataset_s_public_url() throws Throwable {
		String datasetId = (String) context.dataset.get("resource");
		JSONObject resource = BigMLClient.getInstance().getDataset("public/"+datasetId);
    	context.status = (Integer) resource.get("code");
	    context.location = (String) resource.get("location");
	    context.dataset = (JSONObject) resource.get("object");
	}

	@When("the dataset\'s status is FINISHED$")
	public void dataset_status_finished() {
		Long code = (Long) ((JSONObject) context.dataset.get("status")).get("code");
		assertEquals(code.intValue(), AbstractResource.FINISHED);
	}

}