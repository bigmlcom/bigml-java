package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class EvaluationsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(EvaluationsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a evaluation$")
    public void I_create_a_evaluation() throws AuthenticationException {
        String modelId = (String) context.model.get("resource");
        String datasetId = (String) context.dataset.get("resource");
        JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createEvaluation(
                modelId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.evaluation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create an evaluation for the model with the dataset$")
    public void I_create_an_evaluation_for_the_model_with_the_dataset()
            throws Throwable {
        I_create_a_evaluation();
    }

    @When("^I create an evaluation for the ensemble with the dataset$")
    public void I_create_an_evaluation_for_the_ensemble_with_the_dataset()
            throws Throwable {

        I_create_an_evaluation_for_the_ensemble_with_params(null);
    }

    @When("^I create an evaluation for the ensemble with the dataset and \"(.*)\"$")
    public void I_create_an_evaluation_for_the_ensemble_with_params(String params) throws Throwable {
        String ensembleId = (String) context.ensemble.get("resource");
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        if (params!=null && !"".equals(params)) {
            args = (JSONObject) JSONValue.parse(params);
        }
        args = commonSteps.setProject(args);

        JSONObject resource = context.api.createEvaluation(
                ensembleId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.evaluation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }


    @When("^I create an evaluation for the logistic regression with the dataset$")
    public void I_create_an_evaluation_for_the_logistic_regression_with_the_dataset()
            throws Throwable {

        String logisticRegressionId = (String) context.logisticRegression.get("resource");
        String datasetId = (String) context.dataset.get("resource");
        JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createEvaluation(
                logisticRegressionId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.evaluation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    
    @When("^I create an evaluation for the linear regression with the dataset$")
    public void I_create_an_evaluation_for_the_linear_regression_with_the_dataset()
            throws Throwable {

        String linearRegressionId = (String) context.linearRegression.get("resource");
        String datasetId = (String) context.dataset.get("resource");
        JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createEvaluation(
        		linearRegressionId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.evaluation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }
    
    
    @When("^I create an evaluation for the fusion with the dataset$")
    public void I_create_an_evaluation_for_the_fusion_with_the_dataset()
            throws Throwable {

        String fusionId = (String) context.fusion.get("resource");
        String datasetId = (String) context.dataset.get("resource");
        JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createEvaluation(
        		fusionId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.evaluation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create an evaluation for the deepnet with the dataset$")
    public void I_create_an_evaluation_for_the_deepnet_with_the_dataset()
            throws Throwable {

        String deepnetId = (String) context.deepnet.get("resource");
        String datasetId = (String) context.dataset.get("resource");
        JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createEvaluation(
            deepnetId, datasetId, args, 5, 3);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.evaluation = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Then("^the measured \"([^\"]*)\" is (\\d+)$")
    public void the_measured_is(String measure, float value) throws Throwable {
        Long measureLong = ((Number) Utils.getJSONObject(context.evaluation,
                "result.model." + measure)).longValue();
        assertTrue(measureLong.floatValue() == value);
    }

    @Then("^the measured \"([^\"]*)\" is equals to ([\\d,.]+)$")
    public void the_measured_is_equals_to(String measure, double value)
            throws Throwable {
    	
        double measureLong = ((Number) Utils.getJSONObject(context.evaluation,
                "result.model." + measure)).doubleValue();
        
        assertEquals(measureLong, value, 0.00001);
    }

}
