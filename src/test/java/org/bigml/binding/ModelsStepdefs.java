package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.*;

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

public class ModelsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ModelsStepdefs.class);

    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    private String sharedHash;
    private String sharedKey;

    @Given("^I create a model with missing splits$")
    public void I_create_a_model_with_missing_splits() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("missing_splits", true);

        JSONObject resource = BigMLClient.getInstance().createModel(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");

        if( context.models == null ) {
            context.models = new JSONArray();
        }
        context.models.add(context.model);

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }


    @Given("^I create a model from a dataset list$")
    public void I_create_a_model_from_a_dataset_list() throws AuthenticationException {
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("missing_splits", false);

        JSONObject resource = BigMLClient.getInstance().createModel(context.datasets,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        if( context.models == null ) {
            context.models = new JSONArray();
        }
        context.models.add(context.model);
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a model with \"(.*)\"$")
    public void I_create_a_model_with_params(String args) throws Throwable {
        String datasetId = (String) context.dataset.get("resource");
        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);

        if( argsJSON != null ) {
            if (argsJSON.containsKey("tags")) {
                ((JSONArray) argsJSON.get("tags")).add("unitTest");
            } else {
                argsJSON.put("tags", Arrays.asList("unitTest"));
            }

            if( !argsJSON.containsKey("missing_splits") ) {
                argsJSON.put("missing_splits", false);
            }
        } else {
            argsJSON = new JSONObject();
            argsJSON.put("tags", Arrays.asList("unitTest"));
            argsJSON.put("missing_splits", false);
        }

        JSONObject resource = BigMLClient.getInstance().createModel(datasetId,
                argsJSON, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");


        if( context.models == null ) {
            context.models = new JSONArray();
        }
        context.models.add(context.model);

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a model$")
    public void I_create_a_model() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("missing_splits", false);

        JSONObject resource = BigMLClient.getInstance().createModel(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        if( context.models == null ) {
            context.models = new JSONArray();
        }
        context.models.add(context.model);
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I wait until the model is ready less than (\\d+) secs and I return it$")
    public JSONObject I_wait_until_the_model_is_ready_less_than_secs_and_return(
            int secs) throws Throwable {
    	commonSteps.I_wait_until_resource_status_code_is(
        		"model",
        		AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
        return context.model;
    }

    @Given("^I retrieve a list of remote models tagged with \"(.*)\"$")
    public void I_retrieve_a_list_of_remote_models_tagged_with(String tag)
            throws Throwable {
        context.models = new JSONArray();
        JSONArray models = (JSONArray) BigMLClient.getInstance()
                .listModels("tags__in=" + tag).get("objects");
        for (int i = 0; i < models.size(); i++) {
            JSONObject modelResource = (JSONObject) models.get(i);
            JSONObject resource = BigMLClient.getInstance().getModel(
                    (String) modelResource.get("resource"));
            context.models.add(resource);
        }
    }

    @Given("^I create a local multi model$")
    public void I_create_a_local_multi_model() throws Exception {
        if( context.models != null ) {
            JSONArray models = new JSONArray();
            for (Object model : context.models) {
                String modelId = (String) ((JSONObject) model).get("resource");
                JSONObject resource = BigMLClient.getInstance().getModel(modelId);
                Integer code = (Integer) resource.get("code");
                assertEquals(AbstractResource.HTTP_OK, code.intValue());
                models.add(resource.get("object"));
            }
            context.multiModel = new MultiModel(models);
        } else {
            List models = new ArrayList();
            models.add(context.model);
            context.multiModel = new MultiModel(models);
        }
        assertTrue("", context.multiModel != null);
    }

    @Then("^the local multi prediction by name=(true|false) for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_multi_prediction_byname_for_is(String by_name,
            String args, String pred) throws Exception {
        Boolean byName = new Boolean(by_name);
        JSONObject inputObj = (JSONObject) JSONValue.parse(args);
        HashMap<Object, Object> prediction = context.multiModel.predict(inputObj,
                byName, null, true);
        assertTrue(
                "",
                prediction != null
                        && ((String) prediction.get("prediction")).equals(pred));
    }
    @Then("^I create a batch multimodel prediction for by name=(true|false) for \"(.*)\" and predictions \"(.*)\"$")
    public void i_create_a_batch_multimodel_prediction_byname_for_and_predictions(String by_name,
            String args, String expectedPredictions) throws Exception {
        Boolean byName = new Boolean(by_name);
        JSONArray inputObjArr = (JSONArray) JSONValue.parse(args);
        List<MultiVote> votes = context.multiModel.batchPredict(inputObjArr, null,
                byName, null, null, null, false, false);

        JSONArray expectedPredictionsArr = new JSONArray();
        if( expectedPredictions != null && expectedPredictions.trim().length() > 0 ) {
            expectedPredictionsArr = (JSONArray) JSONValue.parse(expectedPredictions);
        }

        for (int i = 0; i < expectedPredictionsArr.size(); i++ ) {
            MultiVote vote = votes.get(i);
            for (HashMap<Object, Object> prediction : vote.getPredictions()) {
                if( !prediction.get("prediction").equals(expectedPredictionsArr.get(i)) ) {
                    assertTrue(String.format("Prediction: %s, expected: %s",
                            prediction.get("prediction"), expectedPredictionsArr.get(i)), false);
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // create_prediction_public_model.feature
    // ---------------------------------------------------------------------

    @Given("^I make the model public$")
    public void I_make_the_model_public() throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("private", new Boolean(false));
        changes.put("white_box", new Boolean(true));

        JSONObject resource = BigMLClient.getInstance().updateModel(
                context.model, changes);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Given("^I check the model status using the model's public url$")
    public void I_check_the_model_status_using_the_model_s_public_url()
            throws Throwable {
        String modelId = (String) context.model.get("resource");
        JSONObject resource = BigMLClient.getInstance().getModel(
                "public/" + modelId);

        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = resource;

        Integer code = (Integer) context.model.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
    }

    @Given("^I check the model stems from the original dataset list$")
    public void I_check_the_model_stems_from_the_original_dataset_list()
            throws Throwable {
        if( context.model.containsKey("datasets") &&
                ((JSONArray) context.model.get("datasets")).containsAll(context.datasets) ) {
           assertTrue(true);
        } else {
           assertFalse(String.format("The model contains only %s " +
                   "and the dataset ids are %s",
                   Arrays.toString(((JSONArray) context.model.get("datasets")).toArray()),
                   Arrays.toString((context.datasets.toArray()))), false);
        }
    }

    // ---------------------------------------------------------------------
    // create_prediction_shared_model.feature
    // ---------------------------------------------------------------------

    @Given("^I make the model shared$")
    public void make_the_model_shared() throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("shared", new Boolean(true));

        JSONObject resource = BigMLClient.getInstance().updateModel(
                context.model, changes);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Given("^I get the model sharing info$")
    public void get_sharing_info() throws Throwable {
        sharedHash = (String) context.model.get("shared_hash");
        sharedKey = (String) context.model.get("sharing_key");
    }

    @Given("^I check the model status using the model's shared url$")
    public void model_from_shared_url() throws Throwable {
        JSONObject resource = BigMLClient.getInstance().getModel(
                "shared/model/" + this.sharedHash);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = resource;
        Integer code = (Integer) context.model.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
    }

    @Given("^I check the model status using the model's shared key$")
    public void model_from_shared_key() throws Throwable {
        String apiUser = System.getProperty("BIGML_USERNAME");
        JSONObject resource = BigMLClient.getInstance().getModel(
                "shared/model/" + this.sharedHash, apiUser, this.sharedKey);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = resource;
        Integer code = (Integer) context.model.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());

    }
    
    
    @Then("^I create a model associated to centroid \"(.*)\"$")
    public void I_create_a_model_associated_to_centroid(String centroidId) 
    		throws Throwable {
        String clusterId = (String) context.cluster.get("resource");
        
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("centroid", centroidId);

        JSONObject resource = BigMLClient.getInstance().createModel(
        		clusterId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.model = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    @Then("^the model is associated to the centroid \"([^\"]*)\" of the cluster$")
    public void the_model_is_associated_to_the_centroid_of_the_cluster(String centroid) 
    		throws Throwable {
        
    	BigMLClient.getInstance().getCacheManager().cleanCache();
    	JSONObject resource = BigMLClient.getInstance().getCluster(
            (String) context.cluster.get("resource"));

        context.status = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, context.status);
        
        assertEquals(context.model.get("resource"), 
        	String.format("model/%s",
                Utils.getJSONObject(resource,
                        String.format("object.cluster_models.%s", centroid) )));
    }
}