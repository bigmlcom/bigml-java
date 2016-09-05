package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.junit.Assert.*;

public class CorrelationsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(CorrelationsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    private String sharedHash;
    private String sharedKey;

    @Given("^I create a correlation from a dataset$")
    public void I_create_a_correlation_from_a_dataset() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createCorrelation(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.correlation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }


    @Given("^I wait until the correlation is ready less than (\\d+) secs$")
    public void I_wait_until_the_correlation_is_ready_less_than_secs(int secs) throws AuthenticationException {
        I_wait_until_correlation_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I wait until the correlation status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_correlation_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.correlation.get("status"))
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
            I_get_the_correlation((String) context.correlation.get("resource"));
            code = (Long) ((JSONObject) context.correlation.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I get the correlation \"(.*)\"")
    public void I_get_the_correlation(String correlationId) throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getCorrelation(correlationId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.correlation = (JSONObject) resource.get("object");
    }


    @Given("^I update the correlation name to \"([^\"]*)\"$")
    public void I_update_the_correlation_name_to(String correlationName) throws Throwable {
        String correlationId = (String) context.correlation.get("resource");

        JSONObject args = new JSONObject();
        args.put("name", correlationName);

        JSONObject resource = BigMLClient.getInstance().updateCorrelation(
                correlationId, args.toString());
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.correlation = (JSONObject) resource.get("object");
        commonSteps
                .the_resource_has_been_updated_with_status(context.status);
    }

    @Then("^the correlation name is \"([^\"]*)\"$")
    public void the_correlation_name_is(String expectedName) throws Throwable {
        assertEquals(expectedName, context.correlation.get("name"));
    }

}