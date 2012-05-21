package org.bigml.binding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.annotation.en.Given;

import static org.junit.Assert.assertTrue;

public class CommonStepdefs {

  // Logging
  Logger logger = LoggerFactory.getLogger(CommonStepdefs.class);

  //Given a instantiated BigML client with "<bigml_username>" and "<bigml_api_key>"
//    @Given("^a instantiated BigML client with \"([^\"]*)\" and \"([^\"]*)\"$")
//    public void a_instantiated_BigML_client_with_and(String bigmlUsername, String bigmlApiKey) {
//    		BigMLClient.getInstance(bigmlUsername, bigmlApiKey);
//    		assertTrue("", BigMLClient.getInstance()!=null);
//    }
  @Given("^a instantiated BigML client$")
  public void a_instantiated_BigML_client() throws InvalidAuthenticationException {
    BigMLClient.getInstance();
    assertTrue("", BigMLClient.getInstance() != null);
  }
}
