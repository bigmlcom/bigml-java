package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.ArrayList;
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

public class CompositesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(CompositesStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create an optiml from a dataset$")
    public void I_create_an_optiml_from_a_dataset() throws Throwable {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("max_training_time", 1000);
        args.put("model_types", Arrays.asList("model", "logisticregression"));
        args.put("metric", "max_phi");
        args.put("number_of_model_candidates", 4);

        JSONObject resource = BigMLClient.getInstance().createOptiML(
                datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.optiML = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @Given("^I get the optiml \"(.*)\"")
    public void I_get_the_optiml(String optiMLId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getOptiML(optiMLId);
        Integer code = (Integer) resource.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
        context.optiML = (JSONObject) resource.get("object");
    }
    
    @Given("^I wait until the optiml status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_optiml_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.optiML.get("status"))
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
            I_get_the_optiml((String) context.optiML.get("resource"));
            code = (Long) ((JSONObject) context.optiML.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }
    
    @Given("^I wait until the optiml is ready less than (\\d+) secs$")
    public void I_wait_until_the_optiml_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_optiml_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }
   
    @Given("^I update the optiml name to \"([^\"]*)\"$")
    public void I_update_the_optiml_name_to(String newName) throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("name", newName);

        JSONObject resource = BigMLClient.getInstance().updateOptiML(
            context.optiML, changes);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.optiML = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }
    
    @Then("^the optiml name is \"([^\"]*)\"$")
    public void the_optiml_name_is(String newName) throws Throwable {
        assertEquals(newName, context.optiML.get("name"));
    }
    
    
    @When("I delete the optiml$")
    public void i_delete_the_optiml() throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().deleteOptiML(context.optiML);
        context.status = (Integer) resource.get("code");
        assertTrue(context.status == AbstractResource.HTTP_NO_CONTENT);
        context.optiML = null;
    }
    
    @Given("^I create a fusion from models$")
    public void I_create_a_fusion_from_models() throws Throwable {
        List models = new ArrayList();
        for (int iModel = 0; iModel < context.models.size(); iModel++ ) {
            JSONObject modelInList = (JSONObject) context.models.get(iModel);
            models.add(modelInList.get("resource"));
        }
        
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        
        JSONObject resource = BigMLClient.getInstance().createFusion(
        		models, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.fusion = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @Given("^I get the fusion \"(.*)\"")
    public void I_get_the_fusion(String fusionId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getFusion(fusionId);
        Integer code = (Integer) resource.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
        context.fusion = (JSONObject) resource.get("object");
    }
    
    @Given("^I wait until the fusion status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_fusion_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.fusion.get("status"))
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
            I_get_the_fusion((String) context.fusion.get("resource"));
            code = (Long) ((JSONObject) context.fusion.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }
    
    @Given("^I wait until the fusion is ready less than (\\d+) secs$")
    public void I_wait_until_the_fusion_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_fusion_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }
   
    @Given("^I update the fusion name to \"([^\"]*)\"$")
    public void I_update_the_fusion_name_to(String newName) throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("name", newName);

        JSONObject resource = BigMLClient.getInstance().updateFusion(
            context.fusion, changes);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.fusion = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }
    
    @Then("^the fusion name is \"([^\"]*)\"$")
    public void the_fusion_name_is(String newName) throws Throwable {
        assertEquals(newName, context.fusion.get("name"));
    }
    
    
    @When("I delete the fusion$")
    public void i_delete_the_fusion() throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().deleteFusion(context.fusion);
        context.status = (Integer) resource.get("code");
        assertTrue(context.status == AbstractResource.HTTP_NO_CONTENT);
        context.fusion = null;
    }

}