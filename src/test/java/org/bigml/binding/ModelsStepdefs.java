package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

public class ModelsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ModelsStepdefs.class);

    MultiModel multiModel;
    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    private String sharedHash;
    private String sharedKey;

    @Given("^I create a model$")
    public void I_create_a_model() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");
        JSONObject resource = BigMLClient.getInstance().createModel(datasetId,
                null, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I wait until the model status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_model_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.model.get("status"))
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
            I_get_the_model((String) context.model.get("resource"));
            code = (Long) ((JSONObject) context.model.get("status"))
                    .get("code");
        }
        assertEquals(code.intValue(), code1);
    }

    @Given("^I wait until the model is ready less than (\\d+) secs$")
    public void I_wait_until_the_model_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_model_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I wait until the model is ready less than (\\d+) secs and I return it$")
    public JSONObject I_wait_until_the_model_is_ready_less_than_secs_and_return(
            int secs) throws AuthenticationException {
        I_wait_until_model_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
        return context.model;
    }

    @Given("^I get the model \"(.*)\"")
    public void I_get_the_model(String modelId) throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getModel(modelId);
        Integer code = (Integer) resource.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
        context.model = (JSONObject) resource.get("object");
    }

    // ---------------------------------------------------------------------
    // create_prediction_multi_model.feature
    // ---------------------------------------------------------------------

    @Given("^I create a model with \"(.*)\"$")
    public void I_create_a_model_with_params(String args) throws Throwable {
        String datasetId = (String) context.dataset.get("resource");
        JSONObject resource = BigMLClient.getInstance().createModel(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I retrieve a list of remote models tagged with \"(.*)\"$")
    public void I_cretrieve_a_list_of_remote_models_tagged_with(String tag)
            throws Throwable {
        context.models = new JSONArray();
        JSONArray models = (JSONArray) BigMLClient.getInstance()
                .listModels("tags__in=" + tag).get("objects");
        for (int i = 0; i < models.size(); i++) {
            JSONObject modelResource = (JSONObject) models.get(i);
            JSONObject resource = BigMLClient.getInstance().getModel(
                    (String) modelResource.get("resource"));
            context.models.add(resource);
        }
    }

    @Given("^I create a local multi model$")
    public void I_create_a_local_multi_model() throws Exception {
        multiModel = new MultiModel(context.models);
        assertTrue("", multiModel != null);
    }

    @Then("^the local multi prediction by name=(true|false) for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_multi_prediction_byname_for_is(String by_name,
            String args, String pred) throws Exception {
        Boolean byName = new Boolean(by_name);
        JSONObject inputObj = (JSONObject) JSONValue.parse(args);
        HashMap<Object, Object> prediction = (HashMap<Object, Object>) multiModel
                .predict(inputObj, byName, null, true);
        assertTrue(
                "",
                prediction != null
                        && ((String) prediction.get("prediction")).equals(pred));
    }

    // ---------------------------------------------------------------------
    // create_prediction_public_model.feature
    // ---------------------------------------------------------------------

    @Given("^I make the model public$")
    public void I_make_the_model_public() throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("private", new Boolean(false));
        changes.put("white_box", new Boolean(true));

        JSONObject resource = BigMLClient.getInstance().updateModel(
                context.model, changes);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Given("^I check the model status using the model's public url$")
    public void I_check_the_model_status_using_the_model_s_public_url()
            throws Throwable {
        String modelId = (String) context.model.get("resource");
        JSONObject resource = BigMLClient.getInstance().getModel(
                "public/" + modelId);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource;

        Integer code = (Integer) context.model.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
    }

    // ---------------------------------------------------------------------
    // create_prediction_shared_model.feature
    // ---------------------------------------------------------------------

    @Given("^I make the model shared$")
    public void make_the_model_shared() throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("shared", new Boolean(true));

        JSONObject resource = BigMLClient.getInstance().updateModel(
                context.model, changes);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Given("^I get the model sharing info$")
    public void get_sharing_info() throws Throwable {
        sharedHash = (String) context.model.get("shared_hash");
        sharedKey = (String) context.model.get("sharing_key");
    }

    @Given("^I check the model status using the model's shared url$")
    public void model_from_shared_url() throws Throwable {
        JSONObject resource = BigMLClient.getInstance().getModel(
                "shared/model/" + this.sharedHash);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource;
        Integer code = (Integer) context.model.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
    }

    @Given("^I check the model status using the model's shared key$")
    public void model_from_shared_key() throws Throwable {
        String apiUser = System.getProperty("BIGML_USERNAME");
        JSONObject resource = BigMLClient.getInstance().getModel(
                "shared/model/" + this.sharedHash, apiUser, this.sharedKey);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource;
        Integer code = (Integer) context.model.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);

    }
}