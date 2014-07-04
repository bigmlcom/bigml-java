package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

public class CommonStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(CommonStepdefs.class);

    @Autowired
    private ContextRepository context;

    @Given("^that I use production mode$")
    public void that_I_use_production_mode() throws Throwable {
        BigMLClient.getInstance(false);
        assertTrue("", BigMLClient.getInstance(false) != null);
    }

    @Given("^that I use development mode$")
    public void that_I_use_development_mode() throws Throwable {
        BigMLClient.getInstance(true);
        assertTrue("", BigMLClient.getInstance(true) != null);
    }

    @Then("^test listing$")
    public void test_listing() throws AuthenticationException {
        JSONObject listing = BigMLClient.getInstance().listSources("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listDatasets("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listModels("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listClusters("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listEnsembles("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listEvaluations("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listPredictions("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listBatchPredictions("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listCentroids("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);
        listing = BigMLClient.getInstance().listBatchCentroids("");
        assertEquals(((Integer) listing.get("code")).intValue(),
                AbstractResource.HTTP_OK);

    }

    @Then("^delete test data$")
    public void delete_test_data() throws AuthenticationException {
        if (context.batchCentroid != null) {
            BigMLClient.getInstance().deleteBatchCentroid(
                    (String) context.batchCentroid.get("resource"));
        }
        if (context.centroid != null) {
            BigMLClient.getInstance().deleteCentroid(
                    (String) context.centroid.get("resource"));
        }
        if (context.batchPrediction != null) {
            BigMLClient.getInstance().deleteBatchPrediction(
                    (String) context.batchPrediction.get("resource"));
        }
        if (context.prediction != null) {
            BigMLClient.getInstance().deletePrediction(
                    (String) context.prediction.get("resource"));
        }
        if (context.evaluation != null) {
            BigMLClient.getInstance().deleteEvaluation(
                    (String) context.evaluation.get("resource"));
        }
        if (context.cluster != null) {
            BigMLClient.getInstance().deleteCluster(
                    (String) context.cluster.get("resource"));
        }
        if (context.model != null) {
            BigMLClient.getInstance().deleteModel(
                    (String) context.model.get("resource"));
        }
        if (context.ensemble != null) {
            BigMLClient.getInstance().deleteEnsemble(
                    (String) context.ensemble.get("resource"));
        }
        if (context.dataset != null) {
            BigMLClient.getInstance().deleteDataset(
                    (String) context.dataset.get("resource"));
        }
        if (context.source != null) {
            BigMLClient.getInstance().deleteSource(
                    (String) context.source.get("resource"));
        }
    }

    @Given("^the resource has been created with status (\\d+)$")
    public void the_resource_has_been_created_with_status(int status) {
        assertEquals(AbstractResource.HTTP_CREATED, status);
    }

    @Given("^the resource has been updated with status (\\d+)$")
    public void the_resource_has_been_updated_with_status(int status) {
        assertEquals(AbstractResource.HTTP_ACCEPTED, status);
    }

    @Then("^delete dev data$")
    public void delete_dev_data() throws AuthenticationException {
        // BatchCentroids
        JSONArray batchCentroids = (JSONArray) BigMLClient.getInstance()
                .listBatchCentroids("").get("objects");
        for (int i = 0; i < batchCentroids.size(); i++) {
            JSONObject batchCentroid = (JSONObject) batchCentroids.get(i);
            BigMLClient.getInstance().deleteBatchCentroid(
                    (String) batchCentroid.get("resource"));
        }

        // Centroids
        JSONArray centroids = (JSONArray) BigMLClient.getInstance()
                .listCentroids("").get("objects");
        for (int i = 0; i < centroids.size(); i++) {
            JSONObject centroid = (JSONObject) centroids.get(i);
            BigMLClient.getInstance().deleteCentroid(
                    (String) centroid.get("resource"));
        }

        // Batch predictions
        JSONArray batchPredictions = (JSONArray) BigMLClient.getInstance()
                .listBatchPredictions("").get("objects");
        for (int i = 0; i < batchPredictions.size(); i++) {
            JSONObject batchPrediction = (JSONObject) batchPredictions.get(i);
            BigMLClient.getInstance().deleteBatchPrediction(
                    (String) batchPrediction.get("resource"));
        }

        // Predictions
        JSONArray predictions = (JSONArray) BigMLClient.getInstance()
                .listPredictions("").get("objects");
        for (int i = 0; i < predictions.size(); i++) {
            JSONObject prediction = (JSONObject) predictions.get(i);
            BigMLClient.getInstance().deletePrediction(
                    (String) prediction.get("resource"));
        }

        // Clusters
        JSONArray clusters = (JSONArray) BigMLClient.getInstance()
                .listClusters("").get("objects");
        for (int i = 0; i < clusters.size(); i++) {
            JSONObject cluster = (JSONObject) clusters.get(i);
            BigMLClient.getInstance().deleteCluster(
                    (String) cluster.get("resource"));
        }

        // Evaluations
        JSONArray evaluations = (JSONArray) BigMLClient.getInstance()
                .listEvaluations("").get("objects");
        for (int i = 0; i < evaluations.size(); i++) {
            JSONObject evaluation = (JSONObject) evaluations.get(i);
            BigMLClient.getInstance().deleteEvaluation(
                    (String) evaluation.get("resource"));
        }

        // Ensembles
        JSONArray ensembles = (JSONArray) BigMLClient.getInstance()
                .listEnsembles("").get("objects");
        for (int i = 0; i < ensembles.size(); i++) {
            JSONObject ensemble = (JSONObject) ensembles.get(i);
            BigMLClient.getInstance().deleteEnsemble(
                    (String) ensemble.get("resource"));
        }

        // Models
        JSONArray models = (JSONArray) BigMLClient.getInstance().listModels("")
                .get("objects");
        for (int i = 0; i < models.size(); i++) {
            JSONObject model = (JSONObject) models.get(i);
            BigMLClient.getInstance().deleteModel(
                    (String) model.get("resource"));
        }

        // Datasets
        JSONArray datasets = (JSONArray) BigMLClient.getInstance()
                .listDatasets("").get("objects");
        for (int i = 0; i < datasets.size(); i++) {
            JSONObject dataset = (JSONObject) datasets.get(i);
            BigMLClient.getInstance().deleteDataset(
                    (String) dataset.get("resource"));
        }

        // Sources
        JSONArray sources = (JSONArray) BigMLClient.getInstance()
                .listSources("").get("objects");
        for (int i = 0; i < sources.size(); i++) {
            JSONObject source = (JSONObject) sources.get(i);
            BigMLClient.getInstance().deleteSource(
                    (String) source.get("resource"));
        }

    }
}
