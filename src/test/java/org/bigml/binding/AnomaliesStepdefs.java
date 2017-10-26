package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.junit.Assert.*;

public class AnomaliesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(AnomaliesStepdefs.class);

    LocalAnomaly localAnomaly;
    Double localScore;

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    private String sharedHash;
    private String sharedKey;

    @Given("^I create an anomaly detector from a dataset list$")
    public void I_create_an_anomaly_from_a_dataset_list() throws AuthenticationException {
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
//        args.put("missing_splits", false);

        assertTrue("No datasets found!", context.datasets != null && context.datasets.size() > 0);

        List datasetsIds = new ArrayList();
        for (Object datasetId : context.datasets) {
            datasetsIds.add(datasetId);
        }

        JSONObject resource = BigMLClient.getInstance().createAnomaly(datasetsIds,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create an anomaly detector from a dataset$")
    public void I_create_an_anomaly() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
//        args.put("missing_splits", false);

        JSONObject resource = BigMLClient.getInstance().createAnomaly(datasetId,
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

        JSONObject resource = BigMLClient.getInstance().createAnomaly(datasetId,
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

    @Given("^I wait until the anomaly detector status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_anomaly_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.anomaly.get("status"))
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
            I_get_the_anomaly((String) context.anomaly.get("resource"));
            code = (Long) ((JSONObject) context.anomaly.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the anomaly score status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_anomaly_score_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.anomalyScore.get("status"))
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
            I_get_the_anomaly_score((String) context.anomalyScore.get("resource"));
            code = (Long) ((JSONObject) context.anomalyScore.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the batch anomaly score status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_batch_anomaly_score_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.batchAnomalyScore.get("status"))
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
            I_get_the_batch_anomaly_score((String) context.batchAnomalyScore.get("resource"));
            code = (Long) ((JSONObject) context.batchAnomalyScore.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the anomaly detector is ready less than (\\d+) secs$")
    public void I_wait_until_the_anomaly_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_anomaly_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @When("^I wait until the batch anomaly score is ready less than (\\d+) secs$")
    public void I_wait_until_the_batch_anomaly_score_is_ready_less_than_secs(
            int secs) throws Throwable {
        I_wait_until_batch_anomaly_score_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }



    @Given("^I wait until the anomaly detector is ready less than (\\d+) secs and I return it$")
    public JSONObject I_wait_until_the_anomaly_is_ready_less_than_secs_and_return(
            int secs) throws AuthenticationException {
        I_wait_until_anomaly_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
        return context.anomaly;
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

    @Given("^I get the anomaly detector \"(.*)\"")
    public void I_get_the_anomaly(String anomalyId) throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getAnomaly(anomalyId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.anomaly = (JSONObject) resource.get("object");
    }

    @Given("^I get the anomaly score \"(.*)\"")
    public void I_get_the_anomaly_score(String anomalyScoreId) throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getAnomalyScore(anomalyScoreId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.anomalyScore = (JSONObject) resource.get("object");
    }

    @Given("^I get the batch anomaly score \"(.*)\"")
    public void I_get_the_batch_anomaly_score(String batchAnomalyScoreId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().
                getBatchAnomalyScore(batchAnomalyScoreId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.batchAnomalyScore = (JSONObject) resource.get("object");
    }

    @Given("^I delete the anomaly detector$")
    public void I_delete_the_anomaly() throws AuthenticationException {
        assertNotNull("The anomaly does not exists", context.anomaly);

        String anomalyId = (String) context.anomaly.get("resource");

        JSONObject resource = BigMLClient.getInstance().deleteAnomaly(anomalyId);

        context.status = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_NO_CONTENT, context.status);
    }

    @Given("^I create an anomaly detector with \"(.*)\"$")
    public void I_create_an_anomaly_with_params(String args) throws Throwable {
        String datasetId = (String) context.dataset.get("resource");
        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);

        if( argsJSON != null ) {
            if (argsJSON.containsKey("tags")) {
                ((JSONArray) argsJSON.get("tags")).add("unitTest");
            } else {
                argsJSON.put("tags", Arrays.asList("unitTest"));
            }

//            if( !argsJSON.containsKey("missing_splits") ) {
//                argsJSON.put("missing_splits", false);
//            }
        } else {
            argsJSON = new JSONObject();
            argsJSON.put("tags", Arrays.asList("unitTest"));
//            argsJSON.put("missing_splits", false);
        }

        JSONObject resource = BigMLClient.getInstance().createAnomaly(datasetId,
                argsJSON, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I retrieve a list of remote anomaly detector tagged with \"(.*)\"$")
    public void I_retrieve_a_list_of_remote_anomaly_tagged_with(String tag)
            throws Throwable {
        context.anomalies = new JSONArray();
        JSONArray anomalies = (JSONArray) BigMLClient.getInstance()
                .listAnomalies("tags__in=" + tag).get("objects");
        for (int i = 0; i < anomalies.size(); i++) {
            JSONObject anomalyResource = (JSONObject) anomalies.get(i);
            JSONObject resource = BigMLClient.getInstance().getAnomaly(
                    (String) anomalyResource.get("resource"));
            context.anomalies.add(resource);
        }
    }

    @Given("^I make the anomaly detector public$")
    public void I_make_the_anomaly_public() throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("private", new Boolean(false));
        changes.put("white_box", new Boolean(true));

        JSONObject resource = BigMLClient.getInstance().updateAnomaly(
                context.anomaly, changes);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Given("^I check the anomaly detector status using the anomaly's public url$")
    public void I_check_the_anomaly_status_using_the_anomaly_s_public_url()
            throws Throwable {
        String anomalyId = (String) context.anomaly.get("resource");
        JSONObject resource = BigMLClient.getInstance().getAnomaly(
                "public/" + anomalyId);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = resource;

        Integer code = (Integer) context.anomaly.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
    }

    @Given("^I make the anomaly detector shared$")
    public void make_the_anomaly_shared() throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("shared", new Boolean(true));

        JSONObject resource = BigMLClient.getInstance().updateAnomaly(
                context.model, changes);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Given("^I get the anomaly detector sharing info$")
    public void get_sharing_info() throws Throwable {
        sharedHash = (String) context.anomaly.get("shared_hash");
        sharedKey = (String) context.anomaly.get("sharing_key");
    }

    @Given("^I check the anomaly detector status using the anomaly's shared url$")
    public void anomaly_from_shared_url() throws Throwable {
        JSONObject resource = BigMLClient.getInstance().getAnomaly(
                "shared/anomaly/" + this.sharedHash);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = resource;
        Integer code = (Integer) context.anomaly.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
    }

    @Given("^I check the anomaly status using the anomaly's shared key$")
    public void anomaly_from_shared_key() throws Throwable {
        String apiUser = System.getProperty("BIGML_USERNAME");
        JSONObject resource = BigMLClient.getInstance().getModel(
                "shared/anomaly/" + this.sharedHash, apiUser, this.sharedKey);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomaly = resource;
        Integer code = (Integer) context.anomaly.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
    }

    @When("^I download the created batch anomaly score file to \"([^\"]*)\"$")
    public void I_download_the_created_batch_anomaly_score_file_to(String fileTo)
            throws Throwable {
        BigMLClient.getInstance().downloadBatchAnomalyScore(
                context.batchAnomalyScore, fileTo);

    }

    @When("^I create an anomaly score for \"(.*)\" by name=(true|false)$")
    public void I_create_an_anomaly_score(String data, String byName)
            throws Throwable {
        if( data == null || data.trim().length() == 0 ) {
            data = "{}";
        }

        JSONObject anomaly = context.anomaly;
        JSONObject dataObj = (JSONObject) JSONValue.parse(data);

        JSONObject argsJSON = new JSONObject();
        argsJSON.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createAnomalyScore(anomaly, dataObj, new Boolean(byName), argsJSON,
                5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.anomalyScore = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create a local anomaly score for \"(.*)\" by name=(true|false)$")
    public void I_create_a_local_anomaly_score(String data, String byName)
            throws Throwable {
        if( data == null || data.trim().length() == 0 ) {
            data = "{}";
        }

        JSONObject inputData = (JSONObject) JSONValue.parse(data);
        localScore = localAnomaly.score(inputData, new Boolean(byName));
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
