package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CommonStepdefs {

	private static final Map<String, String> RES_NAMES = new HashMap<String, String>();
	static {
		RES_NAMES.put("anomaly detector", "anomaly");
		RES_NAMES.put("anomaly score", "anomalyScore");
		RES_NAMES.put("batch anomaly score", "batchAnomalyScore");
		RES_NAMES.put("batch prediction", "batchPrediction");
		RES_NAMES.put("batch centroid", "batchCentroid");
		RES_NAMES.put("logisticregression", "logisticRegression");
		RES_NAMES.put("linearregression", "linearRegression");
		RES_NAMES.put("optiml", "optiML");
		RES_NAMES.put("time series", "timeSeries");
		RES_NAMES.put("topic model", "topicModel");
		RES_NAMES.put("statisticaltest", "statisticalTest");
		RES_NAMES.put("batch projection", "batchProjection");
		RES_NAMES.put("externalconnector", "externalConnector");
	}

	// Logging
	Logger logger = LoggerFactory.getLogger(CommonStepdefs.class);

	@Autowired
	private ContextRepository context;

	private String plural(String name) {
		if (name.endsWith("y")) {
			return name.substring(0, name.length() - 1) + "ies";
		}

		if (name.endsWith("s")) {
			return name;
		}

		return name + "s";
	}

	private Field getField(String resourceName) {
		String name = RES_NAMES.get(resourceName) != null
				? RES_NAMES.get(resourceName)
				: resourceName;
		try {
			return context.getClass().getDeclaredField(name);
		} catch (Exception nme) {
			return null;
		}
	}

	private Method getClientMethod(String operation, String resourceName) {
		String name = RES_NAMES.get(resourceName) != null
				? RES_NAMES.get(resourceName)
				: resourceName;

		if ("list".equals(operation)) {
			name = plural(name);
		}

		if ("create-args".equals(operation)) {
			name = "create" + name.substring(0, 1).toUpperCase()
				+ name.substring(1);
		} else {
			name = operation + name.substring(0, 1).toUpperCase()
					+ name.substring(1);
		}

		Method method = null;
		try {
			BigMLClient client = context.api;
			if ("create-args".equals(operation)) {
				method = client.getClass().getMethod(name, JSONObject.class);
			}
			if ("create".equals(operation)) {
				method = client.getClass().getMethod(
						name, String.class, JSONObject.class,
						Integer.class, Integer.class);
			}
			if ("list".equals(operation)) {
				method = client.getClass().getMethod(name, String.class);
			}
			if ("get".equals(operation)) {
				method = client.getClass().getMethod(name, String.class);
			}
			if ("update".equals(operation)) {
				method = client.getClass().getMethod(name, JSONObject.class,
						JSONObject.class);
			}
			if ("delete".equals(operation)) {
				method = client.getClass().getMethod(name, JSONObject.class);
			}
			return method;
		} catch (Exception nme) {
		}
		return method;
	}

	private JSONObject getResource(String resourceName)
			throws IllegalAccessException {
		return (JSONObject) getField(resourceName).get(context);
	}

	private void setResource(String resourceName, JSONObject resource)
			throws IllegalAccessException {
		getField(resourceName).set(context, resource);
	}


	public JSONObject setProject(JSONObject args) {
		if (args == null) {
			args = new JSONObject();
		}

		if (!args.containsKey("tags")) {
			args.put("tags", new JSONArray());
		}

		((JSONArray) args.get("tags")).add("unitTest");
		args.put("project", context.testProject);

		return args;
	}


	@Given("^I create a (configuration|project) with \"(.*)\"$")
	public void I_create_a_resource_with_(String resourceName, String args)
        throws AuthenticationException, Exception {

        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);
        argsJSON = setProject(argsJSON);

        try {
			Method method = getClientMethod("create-args", resourceName);

			JSONObject resource = (JSONObject) method.invoke(
					context.api, argsJSON);

			context.status = (Integer) resource.get("code");
			context.location = (String) resource.get("location");
			setResource(resourceName, (JSONObject) resource.get("object"));
			the_resource_has_been_created_with_status(context.status);
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
    }


	@Given("^I create a[n]* (anomaly detector|association|correlation|deepnet|ensemble|logisticregression|linearregression|model|sample|statisticaltest|time series|pca) from a dataset with \"(.*)\"$")
	public void I_create_a_resource_from_a_dataset_with(String resourceName, String args)
			throws Throwable {

		String datasetId = (String) ((JSONObject) getResource("dataset"))
				.get("resource");

        JSONObject argsJSON = args != null ?
            (JSONObject) JSONValue.parse(args) :
            new JSONObject();

        argsJSON = setProject(argsJSON);

		try {
			Method method = getClientMethod("create", resourceName);
			JSONObject resource = (JSONObject) method.invoke(
					context.api, datasetId, argsJSON, 5, null);
			context.status = (Integer) resource.get("code");
			context.location = (String) resource.get("location");

            setResource(resourceName, (JSONObject) resource.get("object"));

			the_resource_has_been_created_with_status(context.status);
		} catch (Exception e) {
			assertFalse(true);
		}
	}

	@Given("^I create a[n]* (anomaly detector|association|correlation|deepnet|logisticregression|linearregression|sample|statisticaltest|time series|pca) from a dataset$")
	public void I_create_a_resource_from_a_dataset(String resourceName)
			throws Throwable {

        I_create_a_resource_from_a_dataset_with(resourceName, null);
	}

	public void I_wait_until_resource_status_code_is(String resourceName,
			int code1, int code2, int secs) throws Throwable {

		Long code = (Long) ((JSONObject) getResource(resourceName)
				.get("status")).get("code");
		GregorianCalendar start = new GregorianCalendar();
		start.add(Calendar.SECOND, secs);
		Date end = start.getTime();
		while (code.intValue() != code1 && code.intValue() != code2) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			assertTrue("Time exceded ", end.after(new Date()));
			I_get_the_resource(resourceName,
					(String) getResource(resourceName).get("resource"));
			code = (Long) ((JSONObject) getResource(resourceName).get("status"))
					.get("code");
		}
		assertEquals(code1, code.intValue());
	}

	@Given("^the resource has been created with status (\\d+)$")
	public void the_resource_has_been_created_with_status(int status) {
		assertEquals(AbstractResource.HTTP_CREATED, status);
	}

	@Given("^the resource has been updated with status (\\d+)$")
	public void the_resource_has_been_updated_with_status(int status) {
		assertEquals(AbstractResource.HTTP_ACCEPTED, status);
	}

	@Given("^I update the (.*) name to \"([^\"]*)\"$")
	public void I_update_the_resource_name_to(String resourceName,
			String newName) throws Throwable {

		JSONObject changes = new JSONObject();
		changes.put("name", newName);

		try {
			Method method = getClientMethod("update", resourceName);
			JSONObject resource = (JSONObject) method.invoke(
					context.api, getResource(resourceName),
					changes);
			context.status = (Integer) resource.get("code");
			context.location = (String) resource.get("location");

			setResource(resourceName, (JSONObject) resource.get("object"));
			the_resource_has_been_updated_with_status(context.status);
		} catch (Exception e) {
			assertFalse(true);
		}
	}

	@Given("^I update the (.*) with \"(.*)\"$")
	public void I_update_the_resource_with(String resourceName, String args)
			throws Throwable {

		if (args.equals("{}")) {
			assertTrue("No update params. Continue", true);
		} else {
			try {
				JSONObject changes = (JSONObject) JSONValue.parse(args);

				Method method = getClientMethod("update", resourceName);
				JSONObject resource = (JSONObject) method.invoke(
						context.api, getResource(resourceName),
						changes);
				context.status = (Integer) resource.get("code");
				context.location = (String) resource.get("location");
				setResource(resourceName, (JSONObject) resource.get("object"));
				the_resource_has_been_updated_with_status(context.status);
			} catch (Exception e) {
				assertFalse(true);
			}
		}
	}

	@Given("^I update the (.*) with \"(.*)\" waiting less than (\\d+) secs$")
	public void I_update_the_resource_with(String resourceName, String args,
			int secs) throws Throwable {
		if (args.equals("{}")) {
			assertTrue("No update params. Continue", true);
		} else {
			try {
				JSONObject changes = (JSONObject) JSONValue.parse(args);

				Method method = getClientMethod("update", resourceName);
				JSONObject resource = (JSONObject) method.invoke(
						context.api, getResource(resourceName),
						changes);
				context.status = (Integer) resource.get("code");
				context.location = (String) resource.get("location");
				setResource(resourceName, (JSONObject) resource.get("object"));
				the_resource_has_been_updated_with_status(context.status);
				I_wait_until_the_resource_is_ready_less_than_secs(resourceName,
						secs);
			} catch (Exception e) {
				assertFalse(true);
			}
		}
	}

	@Given("^I wait until the (.*) is ready less than (\\d+) secs$")
	public void I_wait_until_the_resource_is_ready_less_than_secs(
			String resourceName, int secs) throws Throwable {
		I_wait_until_resource_status_code_is(resourceName,
				AbstractResource.FINISHED, AbstractResource.FAULTY, secs);
	}

	@Then("^the (.*) name is \"([^\"]*)\"$")
	public void the_resource_name_is(String resourceName, String newName)
			throws Throwable {
		assertEquals(newName, getResource(resourceName).get("name"));
	}

	public void I_get_the_resource(String resourceName, String resourceId)
			throws AuthenticationException {

		try {
			Method method = getClientMethod("get", resourceName);
			JSONObject resource = (JSONObject) method
					.invoke(context.api, resourceId);

			Integer code = (Integer) resource.get("code");
			assertEquals(AbstractResource.HTTP_OK, code.intValue());
			setResource(resourceName, (JSONObject) resource.get("object"));
		} catch (Exception e) {
			assertFalse(true);
		}
	}


	@When("I delete the (.*)$")
	public void i_delete_the_resource(String resourceName) throws Throwable {

		try {
			Method method = getClientMethod("delete", resourceName);
			JSONObject resource = (JSONObject) method.invoke(
					context.api, getResource(resourceName));
			context.status = (Integer) resource.get("code");
			assertTrue(context.status == AbstractResource.HTTP_NO_CONTENT);
			setResource(resourceName, (JSONObject) null);
		} catch (Exception e) {
			assertFalse(true);
		}

	}


	@Then("^delete all test data$")
	public void delete_all_test_data() throws Exception {

		context.api.getCacheManager().cleanCache();

		JSONObject listProjects = (JSONObject) context.api.listProjects(
	        	"&tags__in=unitTestProject");
		JSONArray projects = (JSONArray) listProjects.get("objects");
		for (int i = 0; i < projects.size(); i++) {
			JSONObject project = (JSONObject) projects.get(i);
			context.api.deleteProject(project);
		}
	}
}
