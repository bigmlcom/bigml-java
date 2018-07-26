package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.junit.Assert.*;

import org.bigml.binding.LocalTopicModel;
import org.bigml.binding.utils.Utils;

public class TopicModelsStepdefs {

	// Logging
	Logger logger = LoggerFactory.getLogger(TopicModelsStepdefs.class);
	
	@Autowired
	CommonStepdefs commonSteps;

	@Autowired
	private ContextRepository context;

	LocalTopicModel localTopicModel;
	JSONArray localTopicDistribution;

	@Given("^I create topic model from a dataset$")
	public void I_create_topic_model_from_a_dataset()
			throws AuthenticationException {

		String datasetId = (String) context.dataset.get("resource");
		JSONObject args = new JSONObject();
		args.put("tags", Arrays.asList("unitTest"));
		args.put("seed", "BigML");
		args.put("topicmodel_seed", "BigML");
		JSONObject resource = context.api
				.createTopicModel(datasetId, args, 5, null);
		context.status = (Integer) resource.get("code");
		context.location = (String) resource.get("location");
		context.topicModel = (JSONObject) resource.get("object");
		commonSteps.the_resource_has_been_created_with_status(context.status);
	}

	@When("^I create a topic distribution for \"(.*)\"$")
	public void I_create_a_topic_distribution_for(String inputData)
			throws AuthenticationException {

		String topicModelId = (String) context.topicModel.get("resource");

		JSONObject args = new JSONObject();
		args.put("tags", Arrays.asList("unitTest"));

		JSONObject resource = context.api.createTopicDistribution(
				topicModelId, (JSONObject) JSONValue.parse(inputData), args, 5,
				null);
		context.status = (Integer) resource.get("code");
		context.location = (String) resource.get("location");
		context.topicDistribution = (JSONObject) resource.get("object");
		commonSteps.the_resource_has_been_created_with_status(context.status);
	}

	@Then("the topic distribution is \"([^\"]*)\"$")
	public void the_topic_distribution_is(String expectedStr)
			throws AuthenticationException {

		JSONArray result = (JSONArray) Utils.getJSONObject(
				context.topicDistribution, "topic_distribution.result");
		JSONArray expected = (JSONArray) JSONValue.parse(expectedStr);
		assertEquals(expected, result);
	}

	@Given("^I create a local topic model$")
	public void I_create_a_local_topic_model() throws Exception {
		localTopicModel = new LocalTopicModel(context.topicModel);
	}

	@When("^I create a local topic distribution for \"(.*)\"$")
	public void I_create_a_local_distribution_for(String inputData)
			throws Throwable {

		localTopicDistribution = localTopicModel
				.distribution((JSONObject) JSONValue.parse(inputData), null);
	}

	@Then("^the local topic distribution is \"([^\"]*)\"$")
	public void the_local_topic_distribution_is(String topicDistribution)
			throws Throwable {

		JSONArray expected = (JSONArray) JSONValue.parse(topicDistribution);
		for (int i = 0; i < localTopicDistribution.size(); i++) {
			JSONObject topicDist = (JSONObject) localTopicDistribution.get(i);
			Double expectedValue = (Double) expected.get(i);
			BigDecimal wasValue = BigDecimal.valueOf(
					(Double) topicDist.get("probability")).setScale(5, RoundingMode.HALF_EVEN);
			assertEquals(expectedValue.doubleValue(), wasValue.doubleValue(), 0);
		}
	}

}