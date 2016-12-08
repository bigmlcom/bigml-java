package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class AssociationsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(AssociationsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;


    @Given("^I create an association from a dataset$")
    public void I_create_an_association_from_a_dataset() throws Throwable {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createAssociation(
                datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.association = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);

    }

    @Given("^I get the association \"(.*)\"")
    public void I_get_the_association(String associationId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getAssociation(associationId);
        Integer code = (Integer) resource.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
        context.association = (JSONObject) resource.get("object");
    }

    @Given("^I wait until the association status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_association_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.association.get("status"))
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
            I_get_the_association((String) context.association.get("resource"));
            code = (Long) ((JSONObject) context.association.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the association is ready less than (\\d+) secs$")
    public void I_wait_until_the_association_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_association_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I update the association name to \"([^\"]*)\"$")
    public void I_update_the_association_name_to(String newName) throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("name", newName);

        JSONObject resource = BigMLClient.getInstance().updateAssociation(
            context.association, changes);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.association = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }


    @Then("^the association name is \"([^\"]*)\"$")
    public void the_association_name_is(String newName) throws Throwable {
        assertEquals(newName, context.association.get("name"));
    }


    @When("I delete the association$")
    public void i_delete_the_association() throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().deleteAssociation(context.association);
        context.status = (Integer) resource.get("code");
        assertTrue(context.status == AbstractResource.HTTP_NO_CONTENT);
        context.association = null;
    }
}