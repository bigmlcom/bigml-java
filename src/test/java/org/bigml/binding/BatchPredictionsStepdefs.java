package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;


public class BatchPredictionsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchPredictionsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @When("^I create a batch prediction for the dataset with the model$")
    public void I_create_a_batch_prediction_for_the_dataset_with_the_model()
            throws Throwable {
        String modelId = (String) context.model.get("resource");
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createBatchPrediction(
                modelId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.batchPrediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create a batch prediction for the dataset with the ensemble$")
    public void I_create_a_batch_prediction_for_the_dataset_with_the_ensemble()
            throws Throwable {
        String ensembleId = (String) context.ensemble.get("resource");
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createBatchPrediction(
                ensembleId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.batchPrediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I wait until the batchprediction status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_batch_prediction_status_code_is(int code1,
            int code2, int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.batchPrediction.get("status"))
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
            I_get_the_batch_prediction((String) context.batchPrediction
                    .get("resource"));
            code = (Long) ((JSONObject) context.batchPrediction.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @When("^I wait until the batch prediction is ready less than (\\d+) secs$")
    public void I_wait_until_the_batch_prediction_is_ready_less_than_secs(
            int secs) throws Throwable {
        I_wait_until_batch_prediction_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I get the batch prediction \"(.*)\"")
    public void I_get_the_batch_prediction(String batchPredictionId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getBatchPrediction(
                batchPredictionId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.batchPrediction = (JSONObject) resource.get("object");
    }

    @When("^I download the created predictions file to \"([^\"]*)\"$")
    public void I_download_the_created_predictions_file_to(String fileTo)
            throws Throwable {
        BigMLClient.getInstance().downloadBatchPrediction(
                context.batchPrediction, fileTo);

    }

    @Then("^the batch prediction file \"([^\"]*)\" is like \"([^\"]*)\"$")
    public void the_batch_prediction_file_is_like(String downloadedFile,
            String checkFile) throws Throwable {

        FileInputStream downloadFis = new FileInputStream(new File(
                downloadedFile));
        FileInputStream checkFis = new FileInputStream(new File(checkFile));

        String localCvs = Utils.inputStreamAsString(downloadFis, "UTF-8");
        String checkCvs = Utils.inputStreamAsString(checkFis, "UTF-8");

        if (!localCvs.equals(checkCvs)) {
            throw new Exception();
        }

    }

}