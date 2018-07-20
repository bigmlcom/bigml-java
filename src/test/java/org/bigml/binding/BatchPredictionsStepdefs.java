package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;


public class BatchPredictionsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(BatchPredictionsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @When("^I create a batch prediction for the dataset with the model$")
    public void I_create_a_batch_prediction_for_the_dataset_with_the_model()
            throws Throwable {
        String modelId = (String) context.model.get("resource");
        I_create_a_batch_prediction_for_the_dataset_with(modelId);
    }

    @When("^I create a batch prediction for the dataset with the ensemble$")
    public void I_create_a_batch_prediction_for_the_dataset_with_the_ensemble()
            throws Throwable {
        String ensembleId = (String) context.ensemble.get("resource");
        I_create_a_batch_prediction_for_the_dataset_with(ensembleId);
    }

    @When("^I create a batch prediction for the dataset with the logistic regression$")
    public void I_create_a_batch_prediction_for_the_dataset_with_the_logistic_regression()
            throws Throwable {
        String logisticRegresionId = (String) context.logisticRegression.get("resource");
        I_create_a_batch_prediction_for_the_dataset_with(logisticRegresionId);
    }
    
    @When("^I create a batch prediction for the dataset with the fusion$")
    public void I_create_a_batch_prediction_for_the_dataset_with_the_fusion()
            throws Throwable {
        String fusionId = (String) context.fusion.get("resource");
        I_create_a_batch_prediction_for_the_dataset_with(fusionId);
    }
    
    
    public void I_create_a_batch_prediction_for_the_dataset_with(String resourceId)
            throws Throwable {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createBatchPrediction(
        		resourceId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.batchPrediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
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


    @When("^I create a batch anomaly score$")
    public void I_create_a_batch_prediction_with_anomaly() throws Throwable {
        assertNotNull("Dataset cannot be null!", context.dataset);
        assertNotNull("Anomaly Detector cannot be null!", context.anomaly);

        String anomalyId = (String) context.anomaly.get("resource");
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createBatchAnomalyScore(
                anomalyId, datasetId, args, 5, null);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.batchAnomalyScore = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }


    @Then("^I create a batch prediction for \"(.*)\" and save it in \"(.*)\"$")
    public void I_create_a_batch_prediction_and_save_it_in(String dataInput,
            String path) throws Throwable {

        if( !new File(path).exists() ) {
            new File(path).mkdirs();
        }

        JSONArray inputDataList = dataInput != null ? (JSONArray) JSONValue.parse(dataInput)
                : null;

        context.multiModel.batchPredict(inputDataList, path);
    }

    @Then("^I create a source from the batch prediction$")
    public void I_create_a_source_from_the_batch_prediction() throws Throwable {

        String batchPredictionId = (String) context.batchPrediction.get("resource");
        assertNotNull("A batch prediction id is needed.", batchPredictionId);

        JSONObject source = BigMLClient.getInstance().createSourceFromBatchPrediction(batchPredictionId,
                new JSONObject());

        Integer code = (Integer) source.get("code");
        assertEquals(AbstractResource.HTTP_CREATED, code.intValue());
        context.location = (String) source.get("location");
        context.source = (JSONObject) source.get("object");
    }

}