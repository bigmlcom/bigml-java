package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

public class ClustersStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ClustersStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a cluster with options \"(.*)\"$")
    public void I_create_a_cluster_with_options(String options) throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = (JSONObject) JSONValue.parse(options);
        args.put("k", 8);
        args.put("seed", "BigML");
        args.put("cluster_seed", "BigML");
        args.put("tags", Arrays.asList("unitTest"));


        JSONObject resource = context.api.createCluster(
                datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.cluster = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a cluster$")
    public void I_create_a_cluster() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("k", 8);
        args.put("seed", "BigML");
        args.put("cluster_seed", "BigML");
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = context.api.createCluster(
                datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.cluster = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a local cluster$")
    public void I_create_a_local_cluster() throws Exception {
        context.localCluster = new LocalCluster(context.cluster);
    }
    
    @Given("^I get the cluster \"(.*)\"")
    public void I_get_the_cluster(String clusterId)
            throws AuthenticationException {
        JSONObject resource = context.api.getCluster(clusterId);
        
        Integer code = (Integer) resource.get("code");
        assertEquals(code.intValue(), AbstractResource.HTTP_OK);
        context.cluster = (JSONObject) resource.get("object");
    }

    @When("^I create a centroid for \"(.*)\"$")
    public void I_create_a_centroid_for(String inputData)
            throws AuthenticationException {
        String clusterId = (String) context.cluster.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

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
        context.localCentroid = context.localCluster.centroid((JSONObject) JSONValue.parse(inputData),
                Boolean.TRUE);
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
            I_get_the_cluster((String) context.cluster.get("resource"));
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

        BigDecimal centroidDist = new BigDecimal(((Number) context.centroid.get("distance")).doubleValue());
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


    @Then("^the data point in the cluster closest to \"(.*)\" is \"(.*)\"$")
    public void closest_in_cluster(String reference, String closest) throws Throwable {
        JSONObject referencePoint = (JSONObject) JSONValue.parse(reference);
        JSONObject closestObj = (JSONObject) JSONValue.parse(closest);
        
        JSONObject closestInCluster = context.localCluster.closestInCluster(
        		referencePoint, 1, null, true);
        JSONObject result = (JSONObject) (
        		(JSONArray) closestInCluster.get("closest")).get(0);
        
        assertEquals(closestObj, result);
    }
}
