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

	private static final Map<String, String> RES_NAMES = new HashMap<String, String>();
	static {
		RES_NAMES.put("anomaly detector", "anomaly");
		RES_NAMES.put("anomaly score", "anomalyScore");
		RES_NAMES.put("batch anomaly score", "batchAnomalyScore");
		RES_NAMES.put("batch prediction", "batchPrediction");
		RES_NAMES.put("batch centroid", "batchCentroid");
		RES_NAMES.put("logisticregression", "logisticRegression");
		RES_NAMES.put("optiml", "optiML");
		RES_NAMES.put("time series", "timeSeries");
		RES_NAMES.put("topic model", "topicModel");
		RES_NAMES.put("statisticaltest", "statisticalTest");

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
			BigMLClient client = BigMLClient.getInstance();
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

	private JSONObject getResource(String resourceName)
			throws IllegalAccessException {
		return (JSONObject) getField(resourceName).get(context);
	}

	private void setResource(String resourceName, JSONObject resource)
			throws IllegalAccessException {
		getField(resourceName).set(context, resource);
	}

	
	
	@Given("^I create a (configuration|project) with \"(.*)\"$")
    public void I_create_a_resource_with_(String resourceName, String args)
        throws AuthenticationException {

        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);
        argsJSON.put("tags", Arrays.asList("unitTest"));
        
        try {
			Method method = getClientMethod("create-args", resourceName);
			
			JSONObject resource = (JSONObject) method.invoke(
					BigMLClient.getInstance(), argsJSON);

			context.status = (Integer) resource.get("code");
			context.location = (String) resource.get("location");
			setResource(resourceName, (JSONObject) resource.get("object"));
			the_resource_has_been_created_with_status(context.status);
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
    }
	
	
	@Given("^I create a[n]* (anomaly detector|association|correlation|deepnet|logisticregression|sample|statisticaltest|time series) from a dataset$")
	public void I_create_a_resource_from_a_dataset(String resourceName)
			throws Throwable {

		String datasetId = (String) ((JSONObject) getResource("dataset"))
				.get("resource");

		JSONObject args = new JSONObject();
		args.put("tags", Arrays.asList("unitTest"));

		try {
			Method method = getClientMethod("create", resourceName);
			JSONObject resource = (JSONObject) method.invoke(
					BigMLClient.getInstance(), datasetId, args, 5, null);
			context.status = (Integer) resource.get("code");
			context.location = (String) resource.get("location");

			setResource(resourceName, (JSONObject) resource.get("object"));
			the_resource_has_been_created_with_status(context.status);
		} catch (Exception e) {
			assertFalse(true);
		}
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
					BigMLClient.getInstance(), getResource(resourceName),
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
						BigMLClient.getInstance(), getResource(resourceName),
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
						BigMLClient.getInstance(), getResource(resourceName),
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
					.invoke(BigMLClient.getInstance(), resourceId);

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
					BigMLClient.getInstance(), getResource(resourceName));
			context.status = (Integer) resource.get("code");
			assertTrue(context.status == AbstractResource.HTTP_NO_CONTENT);
			setResource(resourceName, (JSONObject) null);
		} catch (Exception e) {
			assertFalse(true);
		}

	}

	@Then("^test listing$")
	public void test_listing() throws AuthenticationException {
		JSONObject listing = BigMLClient.getInstance().listSources("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listDatasets("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listModels("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listClusters("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listEnsembles("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listEvaluations("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listPredictions("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listBatchPredictions("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listCentroids("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listBatchCentroids("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());
		listing = BigMLClient.getInstance().listAnomalies("");
		assertEquals(AbstractResource.HTTP_OK,
				((Integer) listing.get("code")).intValue());

	}

	@Then("^delete test data$")
	public void delete_test_data() throws AuthenticationException {
		if (context.batchCentroid != null) {
			BigMLClient.getInstance().deleteBatchCentroid(
					(String) context.batchCentroid.get("resource"));
			context.batchCentroid = null;
		}
		if (context.centroid != null) {
			BigMLClient.getInstance()
					.deleteCentroid((String) context.centroid.get("resource"));
			context.centroid = null;
		}
		if (context.batchPrediction != null) {
			BigMLClient.getInstance().deleteBatchPrediction(
					(String) context.batchPrediction.get("resource"));
			context.batchPrediction = null;
		}
		if (context.prediction != null) {
			BigMLClient.getInstance().deletePrediction(
					(String) context.prediction.get("resource"));
			context.prediction = null;
		}
		if (context.evaluation != null) {
			BigMLClient.getInstance().deleteEvaluation(
					(String) context.evaluation.get("resource"));
			context.evaluation = null;
		}
		if (context.cluster != null) {
			BigMLClient.getInstance()
					.deleteCluster((String) context.cluster.get("resource"));
			context.cluster = null;
		}

		if (context.project != null) {
			BigMLClient.getInstance()
					.deleteProject((String) context.project.get("resource"));
			context.project = null;
		}

		if (context.sample != null) {
			BigMLClient.getInstance()
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

			BigMLClient.getInstance()
					.deleteAnomaly((String) context.anomaly.get("resource"));
			context.anomaly = null;
		}
		if (context.anomalies != null) {
			for (Object anomaly : context.anomalies) {

				BigMLClient.getInstance().deleteAnomaly((String) anomaly);
			}
			context.anomalies = null;
		}
		if (context.anomalyScore != null) {
			BigMLClient.getInstance().deleteAnomalyScore(
					(String) context.anomalyScore.get("resource"));
			context.anomalyScore = null;
		}
		if (context.batchAnomalyScore != null) {
			BigMLClient.getInstance().deleteBatchAnomalyScore(
					(String) context.batchAnomalyScore.get("resource"));
			context.batchAnomalyScore = null;
		}
		if (context.model != null) {

			if (context.models != null) {
				int modelToRemove = -1;
				for (int iModel = 0; iModel < context.models.size(); iModel++) {
					JSONObject modelInList = (JSONObject) context.models
							.get(iModel);
					if (modelInList.get("resource")
							.equals(context.model.get("resource"))) {
						modelToRemove = iModel;
						break;
					}
				}

				if (modelToRemove >= 0) {
					context.models.remove(modelToRemove);
				}
			}

			BigMLClient.getInstance()
					.deleteModel((String) context.model.get("resource"));
			context.model = null;
		}
		if (context.models != null) {
			for (Object model : context.models) {
				BigMLClient.getInstance().deleteModel(
						(String) ((JSONObject) model).get("resource"));
			}
			context.models = null;
		}
		if (context.ensemble != null) {
			BigMLClient.getInstance()
					.deleteEnsemble((String) context.ensemble.get("resource"));
			context.ensemble = null;
		}
		if (context.dataset != null) {
			BigMLClient.getInstance()
					.deleteDataset((String) context.dataset.get("resource"));
			context.dataset = null;
		}
		if (context.datasets != null) {
			for (Object dataset : context.datasets) {
				BigMLClient.getInstance().deleteDataset((String) dataset);
			}
			context.datasets = null;
		}
		if (context.source != null) {
			BigMLClient.getInstance()
					.deleteSource((String) context.source.get("resource"));
			context.source = null;
		}
	}

	@Then("^delete all test data$")
	public void delete_all_test_data() throws Exception {

		BigMLClient.getInstance().getCacheManager().cleanCache();

		// Fusions
		JSONArray fusions = (JSONArray) BigMLClient.getInstance()
				.listFusions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < fusions.size(); i++) {
			JSONObject fusion = (JSONObject) fusions.get(i);
			BigMLClient.getInstance()
					.deleteFusion((String) fusion.get("resource"));
		}

		// OptiMLs
		JSONArray optimls = (JSONArray) BigMLClient.getInstance()
				.listOptiMLs(";tags__in=unitTest").get("objects");
		for (int i = 0; i < optimls.size(); i++) {
			JSONObject optiml = (JSONObject) optimls.get(i);
			BigMLClient.getInstance()
					.deleteOptiML((String) optiml.get("resource"));
		}

		// Deepnets
		JSONArray deepnets = (JSONArray) BigMLClient.getInstance()
				.listDeepnets(";tags__in=unitTest").get("objects");
		for (int i = 0; i < deepnets.size(); i++) {
			JSONObject deepnet = (JSONObject) deepnets.get(i);
			BigMLClient.getInstance()
					.deleteDeepnet((String) deepnet.get("resource"));
		}

		// Forecasts
		JSONArray forecasts = (JSONArray) BigMLClient.getInstance()
				.listForecasts(";tags__in=unitTest").get("objects");
		for (int i = 0; i < forecasts.size(); i++) {
			JSONObject forecast = (JSONObject) forecasts.get(i);
			BigMLClient.getInstance()
					.deleteForecast((String) forecast.get("resource"));
		}

		// TimeSeries
		JSONArray timeSeries = (JSONArray) BigMLClient.getInstance()
				.listTimeSeries(";tags__in=unitTest").get("objects");
		for (int i = 0; i < timeSeries.size(); i++) {
			JSONObject timeSeries_ = (JSONObject) timeSeries.get(i);
			BigMLClient.getInstance()
					.deleteTimeSeries((String) timeSeries_.get("resource"));
		}

		// Configurations
		JSONArray configurations = (JSONArray) BigMLClient.getInstance()
				.listConfigurations(";tags__in=unitTest").get("objects");
		for (int i = 0; i < configurations.size(); i++) {
			JSONObject configuration = (JSONObject) configurations.get(i);
			BigMLClient.getInstance().deleteConfiguration(
					(String) configuration.get("resource"));
		}

		// BatchTopicDistributions
		JSONArray batchTopicDistributions = (JSONArray) BigMLClient
				.getInstance().listBatchTopicDistributions(";tags__in=unitTest")
				.get("objects");
		for (int i = 0; i < batchTopicDistributions.size(); i++) {
			JSONObject batchTopicDistribution = (JSONObject) batchTopicDistributions
					.get(i);
			BigMLClient.getInstance().deleteTopicDistribution(
					(String) batchTopicDistribution.get("resource"));
		}

		// TopicDistributions
		JSONArray topicDistributions = (JSONArray) BigMLClient.getInstance()
				.listTopicDistributions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < topicDistributions.size(); i++) {
			JSONObject topicDistribution = (JSONObject) topicDistributions
					.get(i);
			BigMLClient.getInstance().deleteTopicDistribution(
					(String) topicDistribution.get("resource"));
		}

		// TopicModels
		JSONArray topicModels = (JSONArray) BigMLClient.getInstance()
				.listTopicModels(";tags__in=unitTest").get("objects");
		for (int i = 0; i < topicModels.size(); i++) {
			JSONObject topicModel = (JSONObject) topicModels.get(i);
			BigMLClient.getInstance()
					.deleteTopicModel((String) topicModel.get("resource"));
		}

		/* PLACEHOLDER FOR ASSOCIATIONSET */

		// Associations
		JSONArray associations = (JSONArray) BigMLClient.getInstance()
				.listAssociations(";tags__in=unitTest").get("objects");
		for (int i = 0; i < associations.size(); i++) {
			JSONObject association = (JSONObject) associations.get(i);
			BigMLClient.getInstance()
					.deleteAssociation((String) association.get("resource"));
		}

		// Whizzml Libraries
		JSONArray libraries = (JSONArray) BigMLClient.getInstance()
				.listLibraries(";tags__in=unitTest").get("objects");
		for (int i = 0; i < libraries.size(); i++) {
			JSONObject library = (JSONObject) libraries.get(i);
			BigMLClient.getInstance()
					.deleteLibrary((String) library.get("resource"));
		}

		// Whizzml Scripts
		JSONArray scripts = (JSONArray) BigMLClient.getInstance()
				.listScripts(";tags__in=unitTest").get("objects");
		for (int i = 0; i < scripts.size(); i++) {
			JSONObject script = (JSONObject) scripts.get(i);
			BigMLClient.getInstance()
					.deleteScript((String) script.get("resource"));
		}

		// Whizzml Executions
		JSONArray executions = (JSONArray) BigMLClient.getInstance()
				.listExecutions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < executions.size(); i++) {
			JSONObject execution = (JSONObject) executions.get(i);
			BigMLClient.getInstance()
					.deleteExecution((String) execution.get("resource"));
		}

		// LogisticRegression
		JSONArray logisticRegressions = (JSONArray) BigMLClient.getInstance()
				.listLogisticRegressions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < logisticRegressions.size(); i++) {
			JSONObject logisticRegression = (JSONObject) logisticRegressions
					.get(i);
			BigMLClient.getInstance().deleteLogisticRegression(
					(String) logisticRegression.get("resource"));
		}

		// StatisticalTest
		JSONArray statisticalTests = (JSONArray) BigMLClient.getInstance()
				.listStatisticalTests(";tags__in=unitTest").get("objects");
		for (int i = 0; i < statisticalTests.size(); i++) {
			JSONObject statisticalTest = (JSONObject) statisticalTests.get(i);
			BigMLClient.getInstance().deleteStatisticalTest(
					(String) statisticalTest.get("resource"));
		}

		// Correlations
		JSONArray correlations = (JSONArray) BigMLClient.getInstance()
				.listCorrelations(";tags__in=unitTest").get("objects");
		for (int i = 0; i < correlations.size(); i++) {
			JSONObject correlation = (JSONObject) correlations.get(i);
			BigMLClient.getInstance()
					.deleteCorrelation((String) correlation.get("resource"));
		}

		// BatchCentroids
		JSONArray batchCentroids = (JSONArray) BigMLClient.getInstance()
				.listBatchCentroids(";tags__in=unitTest").get("objects");
		for (int i = 0; i < batchCentroids.size(); i++) {
			JSONObject batchCentroid = (JSONObject) batchCentroids.get(i);
			BigMLClient.getInstance().deleteBatchCentroid(
					(String) batchCentroid.get("resource"));
		}

		// Centroids
		JSONArray centroids = (JSONArray) BigMLClient.getInstance()
				.listCentroids(";tags__in=unitTest").get("objects");
		for (int i = 0; i < centroids.size(); i++) {
			JSONObject centroid = (JSONObject) centroids.get(i);
			BigMLClient.getInstance()
					.deleteCentroid((String) centroid.get("resource"));
		}

		// BatchPredictions
		JSONArray batchPredictions = (JSONArray) BigMLClient.getInstance()
				.listBatchPredictions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < batchPredictions.size(); i++) {
			JSONObject batchPrediction = (JSONObject) batchPredictions.get(i);
			BigMLClient.getInstance().deleteBatchPrediction(
					(String) batchPrediction.get("resource"));
		}

		// Predictions
		JSONArray predictions = (JSONArray) BigMLClient.getInstance()
				.listPredictions(";tags__in=unitTest").get("objects");
		for (int i = 0; i < predictions.size(); i++) {
			JSONObject prediction = (JSONObject) predictions.get(i);
			BigMLClient.getInstance()
					.deletePrediction((String) prediction.get("resource"));
		}

		// Clusters
		JSONArray clusters = (JSONArray) BigMLClient.getInstance()
				.listClusters(";tags__in=unitTest").get("objects");
		for (int i = 0; i < clusters.size(); i++) {
			JSONObject cluster = (JSONObject) clusters.get(i);
			BigMLClient.getInstance()
					.deleteCluster((String) cluster.get("resource"));
		}

		// Evaluations
		JSONArray evaluations = (JSONArray) BigMLClient.getInstance()
				.listEvaluations(";tags__in=unitTest").get("objects");
		for (int i = 0; i < evaluations.size(); i++) {
			JSONObject evaluation = (JSONObject) evaluations.get(i);
			BigMLClient.getInstance()
					.deleteEvaluation((String) evaluation.get("resource"));
		}

		// Ensembles
		JSONArray ensembles = (JSONArray) BigMLClient.getInstance()
				.listEnsembles(";tags__in=unitTest").get("objects");
		for (int i = 0; i < ensembles.size(); i++) {
			JSONObject ensemble = (JSONObject) ensembles.get(i);
			BigMLClient.getInstance()
					.deleteEnsemble((String) ensemble.get("resource"));
		}

		// Anomalies
		JSONArray anomalies = (JSONArray) BigMLClient.getInstance()
				.listAnomalies(";tags__in=unitTest").get("objects");
		for (int i = 0; i < anomalies.size(); i++) {
			JSONObject model = (JSONObject) anomalies.get(i);
			BigMLClient.getInstance()
					.deleteAnomaly((String) model.get("resource"));
		}

		// AnomalyScores
		JSONArray anomalyScores = (JSONArray) BigMLClient.getInstance()
				.listAnomalyScores(";tags__in=unitTest").get("objects");
		for (int i = 0; i < anomalyScores.size(); i++) {
			JSONObject model = (JSONObject) anomalyScores.get(i);
			BigMLClient.getInstance()
					.deleteAnomalyScore((String) model.get("resource"));
		}

		// BatchAnomalyScores
		JSONArray batchAnomalyScores = (JSONArray) BigMLClient.getInstance()
				.listBatchAnomalyScores(";tags__in=unitTest").get("objects");
		for (int i = 0; i < batchAnomalyScores.size(); i++) {
			JSONObject model = (JSONObject) batchAnomalyScores.get(i);
			BigMLClient.getInstance()
					.deleteBatchAnomalyScore((String) model.get("resource"));
		}

		// Models
		JSONArray models = (JSONArray) BigMLClient.getInstance()
				.listModels(";tags__in=unitTest").get("objects");
		for (int i = 0; i < models.size(); i++) {
			JSONObject model = (JSONObject) models.get(i);
			BigMLClient.getInstance()
					.deleteModel((String) model.get("resource"));
		}

		// Datasets
		JSONArray datasets = (JSONArray) BigMLClient.getInstance()
				.listDatasets(";tags__in=unitTest").get("objects");
		for (int i = 0; i < datasets.size(); i++) {
			JSONObject dataset = (JSONObject) datasets.get(i);
			BigMLClient.getInstance()
					.deleteDataset((String) dataset.get("resource"));
		}

		// Sources
		JSONArray sources = (JSONArray) BigMLClient.getInstance()
				.listSources(";tags__in=unitTest").get("objects");
		for (int i = 0; i < sources.size(); i++) {
			JSONObject source = (JSONObject) sources.get(i);
			BigMLClient.getInstance()
					.deleteSource((String) source.get("resource"));
		}

	}
}
