package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
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

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class CommonStepdefs {

	public static final Map<String, String> RES_NAMES = new HashMap<String, String>();
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

	protected JSONObject getResource(String resourceName)
			throws IllegalAccessException {
		return (JSONObject) getField(resourceName).get(context);
	}

	protected void setResource(String resourceName, JSONObject resource)
			throws IllegalAccessException {
		getField(resourceName).set(context, resource);
	}


	@Given("^I create a (configuration|project) with \"(.*)\"$")
    public void I_create_a_resource_with_(String resourceName, String args)
        throws AuthenticationException, Exception {

        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);
        argsJSON.put("tags", Arrays.asList("unitTest"));

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
	
	
	@Given("^I create a[n]* (anomaly detector|association|correlation|deepnet|logisticregression|linearregression|sample|statisticaltest|time series|pca) from a dataset with \"(.*)\"$")
	public void I_create_a_resource_from_a_dataset_with(String resourceName, String args)
			throws Throwable {

		String datasetId = (String) ((JSONObject) getResource("dataset"))
				.get("resource");

        JSONObject argsJSON = args != null ?
            (JSONObject) JSONValue.parse(args) :
            new JSONObject();

		argsJSON.put("tags", Arrays.asList("unitTest"));

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
	
	@Then("^delete test data$")
	public void delete_test_data() throws AuthenticationException {
		if (context.models != null) {
			int modelToRemove = -1;
			for (int iModel = 0; iModel < context.models.size(); iModel++) {
				JSONObject modelInList = (JSONObject) context.models
						.get(iModel);
				if (context.model != null && 
						modelInList.get("resource").equals(context.model.get("resource"))) {
					modelToRemove = iModel;
					break;
				}
				if (context.ensemble != null &&
						modelInList.get("resource").equals(context.ensemble.get("resource"))) {
					modelToRemove = iModel;
					break;
				}
				if (context.deepnet != null && 
						modelInList.get("resource").equals(context.deepnet.get("resource"))) {
					modelToRemove = iModel;
					break;
				}
				if (context.logisticRegression != null && 
						modelInList.get("resource").equals(context.logisticRegression.get("resource"))) {
					modelToRemove = iModel;
					break;
				}
				if (context.linearRegression != null && 
						modelInList.get("resource").equals(context.linearRegression.get("resource"))) {
					modelToRemove = iModel;
					break;
				}
				if (context.fusion != null && 
						modelInList.get("resource").equals(context.fusion.get("resource"))) {
					modelToRemove = iModel;
					break;
				}
				if (context.pca != null && 
						modelInList.get("resource").equals(context.pca.get("resource"))) {
					modelToRemove = iModel;
					break;
				}
			}

			if (modelToRemove >= 0) {
				context.models.remove(modelToRemove);
			}
		}
		
		if (context.pca != null) {
			context.api.deletePca(
					(String) context.pca.get("resource"));
			context.pca = null;
		}
		if (context.projection != null) {
			context.api.deleteProjection(
					(String) context.projection.get("resource"));
			context.projection = null;
		}
		if (context.batchProjection != null) {
			context.api.deleteBatchProjection(
					(String) context.batchProjection.get("resource"));
			context.batchProjection = null;
		}
		if (context.fusion != null) {
			context.api.deleteFusion(
					(String) context.fusion.get("resource"));
			context.fusion = null;
		}
		if (context.optiML != null) {
			context.api.deleteOptiML(
					(String) context.optiML.get("resource"));
			context.optiML = null;
		}
		if (context.deepnet != null) {
			context.api
					.deleteDeepnet((String) context.deepnet.get("resource"));
			context.deepnet = null;
		}
		if (context.forecast != null) {
			context.api
					.deleteForecast((String) context.forecast.get("resource"));
			context.forecast = null;
		}
		if (context.timeSeries != null) {
			context.api
					.deleteTimeSeries((String) context.timeSeries.get("resource"));
			context.timeSeries = null;
		}
		if (context.configuration != null) {
			context.api
					.deleteConfiguration((String) context.configuration.get("resource"));
			context.configuration = null;
		}
		if (context.batchTopicDistribution != null) {
			context.api
					.deleteBatchTopicDistribution((String) context.batchTopicDistribution.get("resource"));
			context.batchTopicDistribution = null;
		}
		if (context.topicDistribution != null) {
			context.api
					.deleteTopicDistribution((String) context.topicDistribution.get("resource"));
			context.topicDistribution = null;
		}
		if (context.topicModel != null) {
			context.api
					.deleteTopicModel((String) context.topicModel.get("resource"));
			context.topicModel = null;
		}
		if (context.association != null) {
			context.api
					.deleteAssociation((String) context.association.get("resource"));
			context.association = null;
		}
		if (context.execution != null) {
			context.api
					.deleteExecution((String) context.execution.get("resource"));
			context.execution = null;
		}
		if (context.library != null) {
			context.api
					.deleteLibrary((String) context.library.get("resource"));
			context.library = null;
		}
		if (context.script != null) {
			if (context.scripts != null) {
				int scriptToRemove = -1;
				for (int iScript = 0; iScript < context.anomalies
						.size(); iScript++) {
					JSONObject scriptInList = (JSONObject) context.anomalies
							.get(iScript);
					if (scriptInList.get("resource")
							.equals(context.script.get("resource"))) {
						scriptToRemove = iScript;
						break;
					}
				}

				if (scriptToRemove >= 0) {
					context.scripts.remove(scriptToRemove);
				}
			}

			context.api
					.deleteScript((String) context.script.get("resource"));
			context.script = null;
		}
		if (context.scripts != null) {
			for (Object script : context.scripts) {
				context.api.deleteScript((String) script);
			}
			context.script = null;
		}
		if (context.linearRegression != null) {
			context.api
					.deleteLinearRegression((String) context.linearRegression.get("resource"));
			context.linearRegression = null;
		}
		if (context.logisticRegression != null) {
			context.api
					.deleteLogisticRegression((String) context.logisticRegression.get("resource"));
			context.logisticRegression = null;
		}
		if (context.statisticalTest != null) {
			context.api
					.deleteStatisticalTest((String) context.statisticalTest.get("resource"));
			context.statisticalTest = null;
		}
		if (context.correlation != null) {
			context.api
					.deleteCorrelation((String) context.correlation.get("resource"));
			context.correlation = null;
		}
		if (context.batchCentroid != null) {
			context.api.deleteBatchCentroid(
					(String) context.batchCentroid.get("resource"));
			context.batchCentroid = null;
		}
		if (context.centroid != null) {
			context.api
					.deleteCentroid((String) context.centroid.get("resource"));
			context.centroid = null;
		}
		if (context.batchPrediction != null) {
			context.api.deleteBatchPrediction(
					(String) context.batchPrediction.get("resource"));
			context.batchPrediction = null;
		}
		if (context.prediction != null) {
			context.api.deletePrediction(
					(String) context.prediction.get("resource"));
			context.prediction = null;
		}
		if (context.evaluation != null) {
			context.api.deleteEvaluation(
					(String) context.evaluation.get("resource"));
			context.evaluation = null;
		}
		if (context.cluster != null) {
			context.api
					.deleteCluster((String) context.cluster.get("resource"));
			context.cluster = null;
		}
		if (context.project != null) {
			context.api
					.deleteProject((String) context.project.get("resource"));
			context.project = null;
		}
		if (context.sample != null) {
			context.api
					.deleteSample((String) context.sample.get("resource"));
			context.sample = null;
		}
		if (context.anomaly != null) {
			if (context.anomalies != null) {
				int anomalyToRemove = -1;
				for (int iAnomaly = 0; iAnomaly < context.anomalies
						.size(); iAnomaly++) {
					JSONObject anomalyInList = (JSONObject) context.anomalies
							.get(iAnomaly);
					if (anomalyInList.get("resource")
							.equals(context.anomaly.get("resource"))) {
						anomalyToRemove = iAnomaly;
						break;
					}
				}

				if (anomalyToRemove >= 0) {
					context.anomalies.remove(anomalyToRemove);
				}
			}

			context.api
					.deleteAnomaly((String) context.anomaly.get("resource"));
			context.anomaly = null;
		}
		if (context.anomalies != null) {
			for (Object anomaly : context.anomalies) {
				context.api.deleteAnomaly((String) anomaly);
			}
			context.anomalies = null;
		}
		if (context.anomalyScore != null) {
			context.api.deleteAnomalyScore(
					(String) context.anomalyScore.get("resource"));
			context.anomalyScore = null;
		}
		if (context.batchAnomalyScore != null) {
			context.api.deleteBatchAnomalyScore(
					(String) context.batchAnomalyScore.get("resource"));
			context.batchAnomalyScore = null;
		}
		if (context.model != null) {
			deleteModel((String) context.model.get("resource"));
			context.model = null;
		}
		if (context.models != null) {
			for (Object model : context.models) {
				deleteModel((String) ((JSONObject) model).get("resource"));
			}
			context.models = null;
		}
		if (context.ensemble != null) {
			context.api
					.deleteEnsemble((String) context.ensemble.get("resource"));
			context.ensemble = null;
		}
		if (context.dataset != null) {
			context.api
					.deleteDataset((String) context.dataset.get("resource"));
			context.dataset = null;
		}
		if (context.datasets != null) {
			for (Object dataset : context.datasets) {
				context.api.deleteDataset((String) dataset);
			}
			context.datasets = null;
		}
		if (context.source != null) {
			context.api
					.deleteSource((String) context.source.get("resource"));
			context.source = null;
		}
	}
	
	
	private void deleteModel(String modelId) {
		try {
			if (modelId.startsWith("model/")) {
				context.api.deleteModel(modelId);
			}
			if (modelId.startsWith("ensemble/")) {
				context.api.deleteEnsemble(modelId);
			}
			if (modelId.startsWith("deepnet/")) {
				context.api.deleteDeepnet(modelId);
			}
			if (modelId.startsWith("logisticregression/")) {
				context.api.deleteLogisticRegression(modelId);
			}
			if (modelId.startsWith("linearregression/")) {
				context.api.deleteLinearRegression(modelId);
			}
			if (modelId.startsWith("fusion/")) {
				context.api.deleteFusion(modelId);
			}
			if (modelId.startsWith("pca/")) {
				context.api.deletePca(modelId);
			}
		} catch (Exception e) {}
	}
	

	@Then("^delete all test data$")
	public void delete_all_test_data() throws Exception {

		context.api.getCacheManager().cleanCache();

		// Projects
		JSONArray projects = (JSONArray) context.api
				.listProjects(";tags__in=unitTest").get("objects");
		for (int i = 0; i < projects.size(); i++) {
			JSONObject project = (JSONObject) projects.get(i);
			context.api
					.deleteProject((String) project.get("resource"));
		}
		
		// Samples
		JSONArray samples = (JSONArray) context.api
				.listSamples(";tags__in=unitTest").get("objects");
		for (int i = 0; i < samples.size(); i++) {
			JSONObject sample = (JSONObject) samples.get(i);
			context.api
					.deleteSample((String) sample.get("resource"));
		}
		
		// BatchProjections
		JSONArray batchProjections = (JSONArray) context.api
				.listBatchProjections(";tags__in=unitTest")
				.get("objects");
		for (int i = 0; i < batchProjections.size(); i++) {
			JSONObject batchProjection = (JSONObject) batchProjections
					.get(i);
			context.api.deleteBatchProjection(
					(String) batchProjection.get("resource"));
		}

		// Projections
		JSONArray projections = (JSONArray) context.api
				.listProjections(";tags__in=unitTest").get("objects");
		for (int i = 0; i < projections.size(); i++) {
			JSONObject projection = (JSONObject) projections
					.get(i);
			context.api.deleteProjection(
					(String) projection.get("resource"));
		}

		// Pcas
		JSONArray pcas = (JSONArray) context.api
				.listPcas(";tags__in=unitTest").get("objects");
		for (int i = 0; i < pcas.size(); i++) {
			JSONObject pca = (JSONObject) pcas.get(i);
			context.api
					.deletePca((String) pca.get("resource"));
		}
		
		// Fusions
		JSONArray fusions = (JSONArray) context.api
				.listFusions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < fusions.size(); i++) {
			JSONObject fusion = (JSONObject) fusions.get(i);
			context.api
					.deleteFusion((String) fusion.get("resource"));
		}

		// OptiMLs
		JSONArray optimls = (JSONArray) context.api
				.listOptiMLs(";tags__in=unitTest").get("objects");
		for (int i = 0; i < optimls.size(); i++) {
			JSONObject optiml = (JSONObject) optimls.get(i);
			context.api
					.deleteOptiML((String) optiml.get("resource"));
		}

		// Deepnets
		JSONArray deepnets = (JSONArray) context.api
				.listDeepnets(";tags__in=unitTest").get("objects");
		for (int i = 0; i < deepnets.size(); i++) {
			JSONObject deepnet = (JSONObject) deepnets.get(i);
			context.api
					.deleteDeepnet((String) deepnet.get("resource"));
		}

		// Forecasts
		JSONArray forecasts = (JSONArray) context.api
				.listForecasts(";tags__in=unitTest").get("objects");
		for (int i = 0; i < forecasts.size(); i++) {
			JSONObject forecast = (JSONObject) forecasts.get(i);
			context.api
					.deleteForecast((String) forecast.get("resource"));
		}

		// TimeSeries
		JSONArray timeSeries = (JSONArray) context.api
				.listTimeSeries(";tags__in=unitTest").get("objects");
		for (int i = 0; i < timeSeries.size(); i++) {
			JSONObject timeSeries_ = (JSONObject) timeSeries.get(i);
			context.api
					.deleteTimeSeries((String) timeSeries_.get("resource"));
		}

		// Configurations
		JSONArray configurations = (JSONArray) context.api
				.listConfigurations(";tags__in=unitTest").get("objects");
		for (int i = 0; i < configurations.size(); i++) {
			JSONObject configuration = (JSONObject) configurations.get(i);
			context.api.deleteConfiguration(
					(String) configuration.get("resource"));
		}

		// BatchTopicDistributions
		JSONArray batchTopicDistributions = (JSONArray) context.api
				.listBatchTopicDistributions(";tags__in=unitTest")
				.get("objects");
		for (int i = 0; i < batchTopicDistributions.size(); i++) {
			JSONObject batchTopicDistribution = (JSONObject) batchTopicDistributions
					.get(i);
			context.api.deleteTopicDistribution(
					(String) batchTopicDistribution.get("resource"));
		}

		// TopicDistributions
		JSONArray topicDistributions = (JSONArray) context.api
				.listTopicDistributions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < topicDistributions.size(); i++) {
			JSONObject topicDistribution = (JSONObject) topicDistributions
					.get(i);
			context.api.deleteTopicDistribution(
					(String) topicDistribution.get("resource"));
		}

		// TopicModels
		JSONArray topicModels = (JSONArray) context.api
				.listTopicModels(";tags__in=unitTest").get("objects");
		for (int i = 0; i < topicModels.size(); i++) {
			JSONObject topicModel = (JSONObject) topicModels.get(i);
			context.api
					.deleteTopicModel((String) topicModel.get("resource"));
		}

		// Associations
		JSONArray associations = (JSONArray) context.api
				.listAssociations(";tags__in=unitTest").get("objects");
		for (int i = 0; i < associations.size(); i++) {
			JSONObject association = (JSONObject) associations.get(i);
			context.api
					.deleteAssociation((String) association.get("resource"));
		}

		// Whizzml Libraries
		JSONArray libraries = (JSONArray) context.api
				.listLibraries(";tags__in=unitTest").get("objects");
		for (int i = 0; i < libraries.size(); i++) {
			JSONObject library = (JSONObject) libraries.get(i);
			context.api
					.deleteLibrary((String) library.get("resource"));
		}

		// Whizzml Scripts
		JSONArray scripts = (JSONArray) context.api
				.listScripts(";tags__in=unitTest").get("objects");
		for (int i = 0; i < scripts.size(); i++) {
			JSONObject script = (JSONObject) scripts.get(i);
			context.api
					.deleteScript((String) script.get("resource"));
		}

		// Whizzml Executions
		JSONArray executions = (JSONArray) context.api
				.listExecutions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < executions.size(); i++) {
			JSONObject execution = (JSONObject) executions.get(i);
			context.api
					.deleteExecution((String) execution.get("resource"));
		}
		
		// LinearRegression
		JSONArray linearRegressions = (JSONArray) context.api
				.listLinearRegressions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < linearRegressions.size(); i++) {
			JSONObject linearRegression = (JSONObject) linearRegressions
					.get(i);
			context.api.deleteLinearRegression(
					(String) linearRegression.get("resource"));
		}
				
		// LogisticRegression
		JSONArray logisticRegressions = (JSONArray) context.api
				.listLogisticRegressions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < logisticRegressions.size(); i++) {
			JSONObject logisticRegression = (JSONObject) logisticRegressions
					.get(i);
			context.api.deleteLogisticRegression(
					(String) logisticRegression.get("resource"));
		}

		// StatisticalTest
		JSONArray statisticalTests = (JSONArray) context.api
				.listStatisticalTests(";tags__in=unitTest").get("objects");
		for (int i = 0; i < statisticalTests.size(); i++) {
			JSONObject statisticalTest = (JSONObject) statisticalTests.get(i);
			context.api.deleteStatisticalTest(
					(String) statisticalTest.get("resource"));
		}

		// Correlations
		JSONArray correlations = (JSONArray) context.api
				.listCorrelations(";tags__in=unitTest").get("objects");
		for (int i = 0; i < correlations.size(); i++) {
			JSONObject correlation = (JSONObject) correlations.get(i);
			context.api
					.deleteCorrelation((String) correlation.get("resource"));
		}

		// BatchCentroids
		JSONArray batchCentroids = (JSONArray) context.api
				.listBatchCentroids(";tags__in=unitTest").get("objects");
		for (int i = 0; i < batchCentroids.size(); i++) {
			JSONObject batchCentroid = (JSONObject) batchCentroids.get(i);
			context.api.deleteBatchCentroid(
					(String) batchCentroid.get("resource"));
		}

		// Centroids
		JSONArray centroids = (JSONArray) context.api
				.listCentroids(";tags__in=unitTest").get("objects");
		for (int i = 0; i < centroids.size(); i++) {
			JSONObject centroid = (JSONObject) centroids.get(i);
			context.api
					.deleteCentroid((String) centroid.get("resource"));
		}

		// BatchPredictions
		JSONArray batchPredictions = (JSONArray) context.api
				.listBatchPredictions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < batchPredictions.size(); i++) {
			JSONObject batchPrediction = (JSONObject) batchPredictions.get(i);
			context.api.deleteBatchPrediction(
					(String) batchPrediction.get("resource"));
		}

		// Predictions
		JSONArray predictions = (JSONArray) context.api
				.listPredictions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < predictions.size(); i++) {
			JSONObject prediction = (JSONObject) predictions.get(i);
			context.api
					.deletePrediction((String) prediction.get("resource"));
		}

		// Clusters
		JSONArray clusters = (JSONArray) context.api
				.listClusters(";tags__in=unitTest").get("objects");
		for (int i = 0; i < clusters.size(); i++) {
			JSONObject cluster = (JSONObject) clusters.get(i);
			context.api
					.deleteCluster((String) cluster.get("resource"));
		}

		// Evaluations
		JSONArray evaluations = (JSONArray) context.api
				.listEvaluations(";tags__in=unitTest").get("objects");
		for (int i = 0; i < evaluations.size(); i++) {
			JSONObject evaluation = (JSONObject) evaluations.get(i);
			context.api
					.deleteEvaluation((String) evaluation.get("resource"));
		}

		// Ensembles
		JSONArray ensembles = (JSONArray) context.api
				.listEnsembles(";tags__in=unitTest").get("objects");
		for (int i = 0; i < ensembles.size(); i++) {
			JSONObject ensemble = (JSONObject) ensembles.get(i);
			context.api
					.deleteEnsemble((String) ensemble.get("resource"));
		}

		// Anomalies
		JSONArray anomalies = (JSONArray) context.api
				.listAnomalies(";tags__in=unitTest").get("objects");
		for (int i = 0; i < anomalies.size(); i++) {
			JSONObject model = (JSONObject) anomalies.get(i);
			context.api
					.deleteAnomaly((String) model.get("resource"));
		}

		// AnomalyScores
		JSONArray anomalyScores = (JSONArray) context.api
				.listAnomalyScores(";tags__in=unitTest").get("objects");
		for (int i = 0; i < anomalyScores.size(); i++) {
			JSONObject model = (JSONObject) anomalyScores.get(i);
			context.api
					.deleteAnomalyScore((String) model.get("resource"));
		}

		// BatchAnomalyScores
		JSONArray batchAnomalyScores = (JSONArray) context.api
				.listBatchAnomalyScores(";tags__in=unitTest").get("objects");
		for (int i = 0; i < batchAnomalyScores.size(); i++) {
			JSONObject model = (JSONObject) batchAnomalyScores.get(i);
			context.api
					.deleteBatchAnomalyScore((String) model.get("resource"));
		}

		// Models
		JSONArray models = (JSONArray) context.api
				.listModels(";tags__in=unitTest").get("objects");
		for (int i = 0; i < models.size(); i++) {
			JSONObject model = (JSONObject) models.get(i);
			context.api
					.deleteModel((String) model.get("resource"));
		}

		// Datasets
		JSONArray datasets = (JSONArray) context.api
				.listDatasets(";tags__in=unitTest").get("objects");
		for (int i = 0; i < datasets.size(); i++) {
			JSONObject dataset = (JSONObject) datasets.get(i);
			context.api
					.deleteDataset((String) dataset.get("resource"));
		}

		// Sources
		JSONArray sources = (JSONArray) context.api
				.listSources(";tags__in=unitTest").get("objects");
		for (int i = 0; i < sources.size(); i++) {
			JSONObject source = (JSONObject) sources.get(i);
			context.api
					.deleteSource((String) source.get("resource"));
		}

	}
}
