package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

public class LogisticRegressionsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(LogisticRegressionsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a logisticregression from a dataset$")
    public void I_create_a_logisticregression_from_a_dataset() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createLogisticRegression(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.logisticregression = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @Given("^I create a logisticregression with objective \"([^\"]*)\" and params \"(.*)\"$")
    public void I_create_a_logisticregression_with_objective_and_params(String objective, String params) 
    		throws Throwable {
        
    	String datasetId = (String) context.dataset.get("resource");

    	JSONObject args = new JSONObject();
    	if (!"".equals(params)) {
    		args = (JSONObject) JSONValue.parse(params);
    	}
        args.put("tags", Arrays.asList("unitTest"));
        args.put("objective_field", objective);
        
        JSONObject resource = BigMLClient.getInstance().createLogisticRegression(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.logisticregression = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status); 	
    }

    @Given("^I create a local logisticregression$")
    public void I_create_a_local_logisticregression() throws Exception {
        context.localLogisticRegression = 
        		new LocalLogisticRegression(context.logisticregression);
    }

    @Given("^I wait until the logisticregression is ready less than (\\d+) secs$")
    public void I_wait_until_the_logisticregression_is_ready_less_than_secs(int secs) throws AuthenticationException {
        I_wait_until_logisticregression_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I wait until the logisticregression status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_logisticregression_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.logisticregression.get("status"))
                .get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_logisticregression((String) context.logisticregression.get("resource"));
            code = (Long) ((JSONObject) context.logisticregression.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I get the logisticregression \"(.*)\"")
    public void I_get_the_logisticregression(String logisticregressionId) throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getLogisticRegression(logisticregressionId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.logisticregression = (JSONObject) resource.get("object");
    }


    @Given("^I update the logisticregression name to \"([^\"]*)\"$")
    public void I_update_the_logisticregression_name_to(String logisticregressionName) throws Throwable {
        String logisticregressionId = (String) context.logisticregression.get("resource");

        JSONObject args = new JSONObject();
        args.put("name", logisticregressionName);

        JSONObject resource = BigMLClient.getInstance().updateLogisticRegression(
                logisticregressionId, args.toString());
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.logisticregression = (JSONObject) resource.get("object");
        commonSteps
                .the_resource_has_been_updated_with_status(context.status);
    }

    @Then("^the logisticregression name is \"([^\"]*)\"$")
    public void the_logisticregression_name_is(String expectedName) throws Throwable {
        assertEquals(expectedName, context.logisticregression.get("name"));
    }

}