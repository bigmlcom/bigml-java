package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

import org.bigml.binding.resources.AbstractResource;


public class TopicModelsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(TopicModelsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;


    @Given("^I create topic model from a dataset$")
    public void I_create_topic_model_from_a_dataset()
        throws AuthenticationException {

        String datasetId = (String) context.dataset.get("resource");
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        JSONObject resource = BigMLClient.getInstance().createTopicModel(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.topicModel = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }


    @Given("^I wait until the topic model is ready less than (\\d+) secs$")
    public void I_wait_until_the_topic_model_is_ready_less_than_secs(int secs)
        throws AuthenticationException {

        I_wait_until_topic_model_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I wait until the topic model status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_topic_model_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.topicModel.get("status")).get("code");

        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_topic_model((String) context.topicModel.get("resource"));
            code = (Long) ((JSONObject) context.topicModel.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I get the topic model \"(.*)\"")
    public void I_get_the_topic_model(String topicModelId)
    throws AuthenticationException {

        JSONObject resource =
            BigMLClient.getInstance().getTopicModel(topicModelId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.topicModel = (JSONObject) resource.get("object");
    }

    @Given("^I update the topic model name to \"([^\"]*)\"$")
    public void I_update_the_topic_model_name_to(String topicModelName)
        throws AuthenticationException {

        String topicModelId = (String) context.topicModel.get("resource");

        JSONObject args = new JSONObject();
        args.put("name", topicModelName);

        JSONObject resource = BigMLClient.getInstance().updateTopicModel(
                topicModelId, args.toString());
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.topicModel = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Then("^the topic model name is \"([^\"]*)\"$")
    public void the_topic_model_name_is(String expectedName)
        throws AuthenticationException {

        assertEquals(expectedName, context.topicModel.get("name"));
    }

}