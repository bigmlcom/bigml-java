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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalEnsembleStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(LocalEnsembleStepdefs.class);

    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a local Ensemble with the last (\\d+) models$")
    public void I_create_local_ensemble_with_list(int lastModels) throws Exception {
        List modelsIds = new ArrayList(lastModels);
        int modelIndex = (context.models.size() >= lastModels ?
                context.models.size() - lastModels : 0);

        for (;modelIndex < context.models.size(); modelIndex++) {
            modelsIds.add(((JSONObject) context.models.get(modelIndex)).get("resource"));
        }
        context.localEnsemble = new LocalEnsemble(modelsIds, null);
        assertTrue("", context.localEnsemble != null);
    }

    @Given("^I create a local ensemble with max models (\\d+)$")
    public void I_create_a_local_ensemble_with_max_models(int maxModels) throws Exception {
    	context.localEnsemble = new LocalEnsemble(context.ensemble, maxModels);
        assertTrue("", context.localEnsemble != null);
    }

    @Given("^I create a local ensemble$")
    public void I_create_a_local_ensemble() throws Exception {
    	context.localEnsemble = new LocalEnsemble(context.ensemble);
        assertTrue("", context.localEnsemble != null);
    }

    @Then("^the local ensemble prediction for \"(.*)\" is \"([^\"]*)\" with confidence ([\\d,.]+)$")
    public void the_local_prediction_for_is_with_confidence(String args, String pred, Double expectedConfidence) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            HashMap<String, Object> p = context.localEnsemble
                    .predict(inputObj, PredictionMethod.PLURALITY, null, null, null, null, null, true);
            
            String prediction = (String) p.get("prediction");
            Double actualConfidence = (Double) p.get("confidence");
            assertTrue("", prediction != null && prediction.equals(pred));
            assertEquals(String.format("%.4g", expectedConfidence), String.format("%.4g", actualConfidence));
        } catch (Exception parseException) {
            assertTrue("", false);
        }
    }


    @Then("^the local ensemble prediction using median with confidence for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_prediction_using_median_with_confidence_for_is(String args, String expectedPrediction) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            HashMap<String, Object> p = context.localEnsemble
                    .predict(inputObj, PredictionMethod.PLURALITY, null, MissingStrategy.LAST_PREDICTION,
                            null, null, true, true);
            
            Object prediction = p.get("prediction");
            if( prediction instanceof Number ) { // Regression
                Double expected = Double.parseDouble(expectedPrediction);
                assertEquals(String.format("%.4g", expected), String.format("%.4g", prediction));
            } else {
                assertTrue("", prediction != null && prediction.equals(expectedPrediction));
            }
        } catch (Exception parseException) {
            assertTrue("", false);
        }
    }

    @Then("^the local ensemble prediction for \"(.*)\" is \"([^\"]*)\"$")
    public void the_local_ensemble_prediction_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            HashMap<String, Object> p = context.localEnsemble
                    .predict(inputObj, PredictionMethod.PLURALITY, null, null, null, null, null, true);
            
            Object prediction = p.get("prediction");
            if( prediction instanceof Number ) { // Regression
                Double expected = Double.parseDouble(pred);
                assertEquals(String.format("%.4g", expected), String.format("%.4g", prediction));
            } else {
                assertTrue("", prediction != null && prediction.equals(pred));
            }
        } catch (Exception parseException) {
        	parseException.printStackTrace();
            assertTrue("", false);
        }
    }
    

    @Then("^the field importance text is \"(.*?)\"$")
    public void the_field_importance_print(String expFieldImportance) {
        try {
            JSONArray expFieldImportanceArr = (JSONArray) JSONValue.parse(expFieldImportance);
            Map<String, Double> expFieldImportanceMap = new HashMap<String, Double>();
            for (Object fieldImportance : expFieldImportanceArr) {
                JSONArray fieldImportanceObj = (JSONArray) fieldImportance;
                expFieldImportanceMap.put((String) fieldImportanceObj.get(0), (Double) fieldImportanceObj.get(1));
            }

            List<JSONArray> actualFieldImportanceArr = context.localEnsemble.getFieldImportanceData();
            Map<String, Double> actFieldImportanceMap = new HashMap<String, Double>();
            for (JSONArray fieldImportance : actualFieldImportanceArr) {
                actFieldImportanceMap.put( (String) fieldImportance.get(0), (Double) fieldImportance.get(1));
            }

            assertEquals(expFieldImportanceArr.size(), actualFieldImportanceArr.size());

            for (String fieldName : actFieldImportanceMap.keySet()) {
                assertTrue(expFieldImportanceMap.containsKey(fieldName));
                assertEquals(String.format("%.12g%n", expFieldImportanceMap.get(fieldName)),
                        String.format("%.12g%n", actFieldImportanceMap.get(fieldName)));
            }
        } catch (Exception parseException) {
        	parseException.printStackTrace();
            assertTrue("", false);
        }
    }
    
    
    
    @When("^I create a proportional missing strategy local prediction with ensemble with \"(.*)\" for \"(.*)\"$")
    public void I_create_a_proportional_missing_strategy_local_prediction_with_ensemble(String options, String args)
            throws AuthenticationException {
        
    	try {
            JSONObject inputData = (JSONObject) JSONValue.parse(args);   
            JSONObject opts = (JSONObject) JSONValue.parse(options);
            
            String operatingKind = null;
            if (opts.get("operating_kind") != null) {
            	operatingKind = (String) opts.get("operating_kind");
            }
            
            HashMap<String, Object> prediction = context.localEnsemble
                    .predict(inputData, null, null, MissingStrategy.PROPORTIONAL, null, 
                    		operatingKind, true, true);
            
            context.localPrediction = prediction;
        } catch (Exception e) {
            assertTrue("", false);
        }
    }
}
