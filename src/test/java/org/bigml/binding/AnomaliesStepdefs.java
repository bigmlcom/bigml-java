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

import java.util.*;

import static org.junit.Assert.*;

public class AnomaliesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(AnomaliesStepdefs.class);

    LocalAnomaly localAnomaly;
    Double localScore;
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    
    @Given("^I create an anomaly detector from a dataset list$")
    public void I_create_an_anomaly_from_a_dataset_list() throws AuthenticationException {
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        
        assertTrue("No datasets found!", context.datasets != null && context.datasets.size() > 0);

        List datasetsIds = new ArrayList();
        for (Object datasetId : context.datasets) {
            datasetsIds.add(datasetId);
        }

        JSONObject resource = context.api.createAnomaly(datasetsIds,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Then("^I create an anomaly detector of (\\d+) anomalies from a dataset$")
    public void i_create_an_anomaly_with_top_n_from_dataset(int topN)
        throws AuthenticationException {

        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("top_n", topN);

        JSONObject resource = context.api.createAnomaly(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a local anomaly detector$")
    public void I_create_a_local_anomaly_detector() throws Exception {
        localAnomaly = new LocalAnomaly(context.anomaly);
    }
    
    @Given("^I check the anomaly detector stems from the original dataset list$")
    public void i_check_anomaly_dataset_and_datasets_list () throws AuthenticationException {
        String[] datasetIds = (String[]) context.datasets.toArray(new String[context.datasets.size()]);

        JSONArray anomalyDatasetsJSONArr = (JSONArray) context.anomaly.get("datasets");

        String[] anomalyDatasetIds = (anomalyDatasetsJSONArr != null ?
                (String[]) anomalyDatasetsJSONArr.toArray(new String[anomalyDatasetsJSONArr.size()]) : null);

        assertArrayEquals(datasetIds, anomalyDatasetIds);
    }

    @Given("^I check the anomaly detector stems from the original dataset$")
    public void i_check_anomaly_dataset_and_datasets_ids () throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");
        String anomalyDatasetId = (context.anomaly.containsKey("dataset") ?
                                        (String) context.anomaly.get("dataset") : null);

        assertEquals(datasetId, anomalyDatasetId);
    }
    
    @When("^I create an anomaly score for \"(.*)\"$")
    public void I_create_an_anomaly_score(String data)
            throws Throwable {
        if( data == null || data.trim().length() == 0 ) {
            data = "{}";
        }

        JSONObject anomaly = context.anomaly;
        JSONObject dataObj = (JSONObject) JSONValue.parse(data);

        JSONObject argsJSON = new JSONObject();
        argsJSON.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = context.api.createAnomalyScore(
            anomaly, dataObj, argsJSON, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomalyScore = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create a local anomaly score for \"(.*)\"$")
    public void I_create_a_local_anomaly_score(String data)
            throws Throwable {
        if( data == null || data.trim().length() == 0 ) {
            data = "{}";
        }

        JSONObject inputData = (JSONObject) JSONValue.parse(data);
        localScore = localAnomaly.score(inputData);
    }

    @Then("^the anomaly score is \"(.*)\"$")
    public void the_anomaly_score_is(String data)
            throws Throwable {

        assertEquals(data, context.anomalyScore.get("score").toString());
    }

    @Then("^the local anomaly score is (.*)$")
    public void the_local_anomaly_score_is(Double expectedScore)
            throws Throwable {

        assertEquals(String.format("%.5g", expectedScore), String.format("%.5g", localScore));
    }
}
