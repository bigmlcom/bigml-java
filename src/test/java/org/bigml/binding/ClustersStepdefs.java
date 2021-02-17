package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class ClustersStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ClustersStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    private String downloadedFile;

    @Given("^I create a cluster with options \"(.*)\"$")
    public void I_create_a_cluster_with_options(String options) 
    		throws Throwable {
    	
    	JSONObject args = (JSONObject) JSONValue.parse(options);
        args.put("k", 8);
        args.put("seed", "BigML");
        args.put("cluster_seed", "BigML");
        commonSteps.I_create_a_resource_from_a_dataset_with(
        		"cluster", args.toString());
    }

    @Given("^I create a cluster$")
    public void I_create_a_cluster() throws Throwable {
    	I_create_a_cluster_with_options("{}");
    }

    @Given("^I create a local cluster$")
    public void I_create_a_local_cluster() throws Exception {
        context.localCluster = new LocalCluster(context.cluster);
    }
    
    @When("^I create a centroid for \"(.*)\"$")
    public void I_create_a_centroid_for(String inputData)
            throws AuthenticationException {
        String clusterId = (String) context.cluster.get("resource");
        JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createCentroid(
                clusterId, (JSONObject) JSONValue.parse(inputData),
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.centroid = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create a local centroid for \"(.*)\"$")
    public void I_create_a_local_centroid_for(String inputData)
            throws AuthenticationException {
        context.localCentroid = context.localCluster.centroid(
            (JSONObject) JSONValue.parse(inputData));
    }

    @Given("^I check the centroid is ok$")
    public void I_check_the_centroid_is_ok() throws AuthenticationException {
        int secs = 60;

        Long code = (Long) ((JSONObject) context.centroid.get("status"))
                .get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != AbstractResource.FINISHED
                && code.intValue() != AbstractResource.FAULTY) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            commonSteps.I_get_the_resource(
            		"cluster", (String) context.cluster.get("resource"));
            code = (Long) ((JSONObject) context.cluster.get("status"))
                    .get("code");
        }
        assertEquals(AbstractResource.FINISHED, code.intValue());
    }

    @Then("the centroid is \"([^\"]*)\" with distance (.*)$")
    public void the_centroid_is_with_distance(String result, Double distance) throws AuthenticationException {

        assertEquals(result, context.centroid.get("centroid_name"));

        BigDecimal centroidDist = new BigDecimal(((Number) context.centroid.get("distance")).doubleValue());
        centroidDist = centroidDist.setScale(5, RoundingMode.HALF_EVEN);

        BigDecimal expectedDistance = new BigDecimal(distance);
        expectedDistance = expectedDistance.setScale(5, RoundingMode.HALF_EVEN);

        String confidenceValue = String.format("%.5g", centroidDist);
        assertTrue(confidenceValue.equals(String.format("%.5g", expectedDistance)));
    }

    @Then("the local centroid is \"([^\"]*)\" with distance (.*)$")
    public void the_local_centroid_is_with_distance(String result, Double distance) throws AuthenticationException {
        assertEquals(result, context.localCentroid.get("centroid_name"));

        BigDecimal centroidDist = new BigDecimal(((Number) context.localCentroid.get("distance")).doubleValue());
        centroidDist = centroidDist.setScale(5, RoundingMode.HALF_EVEN);

        BigDecimal expectedDistance = new BigDecimal(distance);
        expectedDistance = expectedDistance.setScale(5, RoundingMode.HALF_EVEN);

        String confidenceValue = String.format("%.5g", centroidDist);
        assertTrue(confidenceValue.equals(String.format("%.5g", expectedDistance)));
    }

    @Then("the centroid is \"([^\"]*)\"$")
    public void the_centroid_is(String result) throws AuthenticationException {
        assertEquals(result, context.centroid.get("centroid_name"));
    }

    @When("^I create a batch centroid for the dataset$")
    public void I_create_a_batch_centroid_for_the_dataset() throws Throwable {
        String clusterId = (String) context.cluster.get("resource");
        String datasetId = (String) context.dataset.get("resource");
        JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createBatchCentroid(
                clusterId, datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.batchCentroid = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @When("^I download the created centroid file to \"([^\"]*)\"$")
    public void I_download_the_created_centroid_file_to(String fileTo)
            throws Throwable {
        downloadedFile = fileTo;

        context.api.downloadBatchCentroid(context.batchCentroid,
                fileTo);
    }

    @Then("^the batch centroid file is like \"([^\"]*)\"$")
    public void the_batch_centroid_file_is_like(String checkFile)
            throws Throwable {
        FileInputStream downloadFis = new FileInputStream(new File(
                downloadedFile));
        FileInputStream checkFis = new FileInputStream(new File(checkFile));

        String localCvs = Utils.inputStreamAsString(downloadFis, "UTF-8");
        String checkCvs = Utils.inputStreamAsString(checkFis, "UTF-8");

        if (!localCvs.equals(checkCvs)) {
            throw new Exception();
        }
    }

    @Then("^the data point in the cluster closest to \"(.*)\" is \"(.*)\"$")
    public void closest_in_cluster(String reference, String closest) throws Throwable {
        JSONObject referencePoint = (JSONObject) JSONValue.parse(reference);
        JSONObject closestObj = (JSONObject) JSONValue.parse(closest);
        
        JSONObject closestInCluster = context.localCluster.closestInCluster(
            referencePoint, 1, null);
        JSONObject result = (JSONObject) (
        		(JSONArray) closestInCluster.get("closest")).get(0);
        
        assertEquals(closestObj, result);
    }

    @Then("^the centroid in the cluster closest to \"(.*)\" is \"(.*)\"$")
    public void centroid_closest_in_cluster(String reference, String closest) throws Throwable {
        JSONObject referencePoint = (JSONObject) JSONValue.parse(reference);

        JSONObject closestInCluster = context.localCluster.closestInCluster(
                referencePoint, 1, null);

        assertEquals(closest, closestInCluster.get("centroid_id"));
    }
}
