package org.bigml.binding;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.Method;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;

public class ExternalConnectorStepdefs {

	// Logging
	Logger logger = LoggerFactory.getLogger(ExternalConnectorStepdefs.class);

	@Autowired
	CommonStepdefs commonSteps;

	@Autowired
	private ContextRepository context;
	
	@Given("^I create an externalconnector for \"(.*)\" with \"(.*)\"$")
    public void I_create_externalconnector(String source, String connection)
        throws AuthenticationException, Exception {
		
		JSONObject connectionInfo = connection != null ?
            (JSONObject) JSONValue.parse(connection) :
            new JSONObject();
        JSONObject args = commonSteps.setProject(null);

        try {
        	JSONObject resource = context.api.createExternalConnector(
        		source, connectionInfo, args, 5, null);

            context.status = (Integer) resource.get("code");
            context.location = (String) resource.get("location");
            context.externalConnector = (JSONObject) resource.get("object");
            commonSteps.the_resource_has_been_created_with_status(context.status);
        } catch (Exception e) {
			assertFalse(true);
		}

    }

}