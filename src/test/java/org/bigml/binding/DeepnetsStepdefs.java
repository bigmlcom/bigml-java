package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class DeepnetsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(DeepnetsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;


    @Given("^I create a deepnet from a dataset$")
    public void I_create_a_deepnet_from_a_dataset() throws Throwable {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createDeepnet(
                datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.deepnet = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);

    }

    @Given("^I get the deepnet \"(.*)\"")
    public void I_get_the_deepnet(String deepnetId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getDeepnet(deepnetId);
        Integer code = (Integer) resource.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
        context.deepnet = (JSONObject) resource.get("object");
    }

    @Given("^I wait until the deepnet status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_deepnet_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.deepnet.get("status"))
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
            I_get_the_deepnet((String) context.deepnet.get("resource"));
            code = (Long) ((JSONObject) context.deepnet.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the deepnet is ready less than (\\d+) secs$")
    public void I_wait_until_the_deepnet_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_deepnet_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I update the deepnet name to \"([^\"]*)\"$")
    public void I_update_the_deepnet_name_to(String newName) throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("name", newName);

        JSONObject resource = BigMLClient.getInstance().updateDeepnet(
            context.deepnet, changes);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.deepnet = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }


    @Then("^the deepnet name is \"([^\"]*)\"$")
    public void the_deepnet_name_is(String newName) throws Throwable {
        assertEquals(newName, context.deepnet.get("name"));
    }


    @When("I delete the deepnet$")
    public void i_delete_the_deepnet() throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().deleteDeepnet(context.deepnet);
        context.status = (Integer) resource.get("code");
        assertTrue(context.status == AbstractResource.HTTP_NO_CONTENT);
        context.deepnet = null;
    }


}