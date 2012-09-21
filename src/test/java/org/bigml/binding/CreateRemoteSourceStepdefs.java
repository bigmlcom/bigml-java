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

public class CreateRemoteSourceStepdefs {

  // Logging
  Logger logger = LoggerFactory.getLogger(CreateRemoteSourceStepdefs.class);
  
  int status;
  String location = null;
  JSONObject source = null;

  // Source steps
  @Given("^I create a remote source from the \"([^\"]*)\" url$")
  public void I_create_a_remote_source_from_the_url(String url) throws AuthenticationException {
    JSONObject resource = BigMLClient.getInstance().createRemoteSource(url, null);

    // update status
    status = (Integer) resource.get("code");
    location = (String) resource.get("location");
    source = (JSONObject) resource.get("object");

    assertEquals(status, AbstractResource.HTTP_CREATED);
  }
  
  @Given("^I wait until the remote resource status code is either (\\d) or (\\d) less than (\\d+)")
  public void I_wait_until_remote_source_status_code_is(int code1, int code2, int secs) throws AuthenticationException {
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
      I_get_the_remote_source((String) source.get("resource"));
      code = (Long) ((JSONObject) source.get("status")).get("code");
    }
    assertEquals(code.intValue(), code1);
  }
  
  @Given("^I wait until the remote source is ready less than (\\d+) secs$")
  public void I_wait_until_the_remote_source_is_ready_less_than_secs(int secs) throws AuthenticationException {
    I_wait_until_remote_source_status_code_is(AbstractResource.FINISHED, AbstractResource.FAULTY, secs);
  }
  
  @Given("^I get the remote source \"(.*)\"")
  public void I_get_the_remote_source(String sourceId) throws AuthenticationException {
    JSONObject resource = BigMLClient.getInstance().getSource(sourceId);
    Integer code = (Integer) resource.get("code");
    assertEquals(code.intValue(), AbstractResource.HTTP_OK);
    source = (JSONObject) resource.get("object");
  }

  // Listing
  @Then("^test listing remote source$")
  public void test_listing_remote_source() throws AuthenticationException {
    JSONObject listing = BigMLClient.getInstance().listSources("");
    assertEquals(((Integer) listing.get("code")).intValue(), AbstractResource.HTTP_OK);
  }

  // Delete test data
  @Then("^delete test remote source data$")
  public void delete_test_remote_source_data() throws AuthenticationException {
    if (source != null) {
      BigMLClient.getInstance().deleteSource((String) source.get("resource"));
    }

  }
}
