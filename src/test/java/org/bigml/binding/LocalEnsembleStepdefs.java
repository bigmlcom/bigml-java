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

    LocalEnsemble predictiveEnsemble;

    CommonStepdefs commonSteps = new CommonStepdefs();

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
        predictiveEnsemble = new LocalEnsemble(modelsIds, null);
        assertTrue("", predictiveEnsemble != null);
    }

    @Given("^I create a local ensemble with max models (\\d+)$")
    public void I_create_a_local_ensemble_with_max_models(int maxModels) throws Exception {
        predictiveEnsemble = new LocalEnsemble(context.ensemble, maxModels);
        assertTrue("", predictiveEnsemble != null);
    }

    @Given("^I create a local ensemble$")
    public void I_create_a_local_ensemble() throws Exception {
        predictiveEnsemble = new LocalEnsemble(context.ensemble);
        assertTrue("", predictiveEnsemble != null);
    }

    @Then("^the local ensemble prediction for \"(.*)\" is \"([^\"]*)\" with confidence ([\\d,.]+)$")
    public void the_local_prediction_by_name_for_is_with_confidence(String args, String pred, Double expectedConfidence) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Map<Object, Object> p = predictiveEnsemble
                    .predict(inputObj, true, PredictionMethod.PLURALITY, true);
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
            Map<Object, Object> p = predictiveEnsemble
                    .predict(inputObj, true, PredictionMethod.PLURALITY, true, null, MissingStrategy.LAST_PREDICTION,
                            true, true, true, true);
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
    public void the_local_prediction_by_name_for_is(String args, String pred) {
        try {
            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
            Map<Object, Object> p = predictiveEnsemble
                    .predict(inputObj, true, PredictionMethod.PLURALITY, true);
            String prediction = (String) p.get("prediction");
            assertTrue("", prediction != null && prediction.equals(pred));
        } catch (Exception parseException) {
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

            List<JSONArray> actualFieldImportanceArr = predictiveEnsemble.getFieldImportanceData();
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
            assertTrue("", false);
        }
    }

//    @Then("^the numerical prediction of proportional missing strategy local prediction for \"(.*)\" is ([\\d,.]+)$")
//    public void the_numerical_prediction_of_proportional_missing_strategy_local_predictionfor_is(String args, double expectedPrediction) {
//        try {
//            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
//            HashMap<Object, Object> p = predictiveEnsemble.predict(inputObj, true, false, MissingStrategy.PROPORTIONAL);
//            Double actualPrediction = (Double) p.get("prediction");
//            assertEquals(String.format("%.4g%n", expectedPrediction), String.format("%.4g%n", actualPrediction));
//        } catch (InputDataParseException parseException) {
//            assertTrue("", false);
//        }
//    }
//
//    @Then("^the proportional missing strategy local prediction for \"(.*)\" is \"([^\"]*)\"$")
//    public void the_proportional_missing_strategy_local_prediction_for_is(String args, String pred) {
//        try {
//            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
//            HashMap<Object, Object> p = predictiveEnsemble.predict(inputObj, true, false, MissingStrategy.PROPORTIONAL);
//            String prediction = (String) p.get("prediction");
//            assertTrue("", prediction != null && prediction.equals(pred));
//        } catch (InputDataParseException parseException) {
//            assertTrue("", false);
//        }
//    }
//
//    @Then("^the confidence of the proportional missing strategy local prediction for \"(.*)\" is ([\\d,.]+)$")
//    public void the_confidence_of_the_missing_strategy_local_predictionfor_is(String args, double expectedConfidence) {
//        try {
//            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
//            HashMap<Object, Object> p = predictiveEnsemble.predict(inputObj, true, true, MissingStrategy.PROPORTIONAL);
//            Double actualConfidence = (Double) p.get("confidence");
//            assertEquals(String.format("%.4g%n", expectedConfidence), String.format("%.4g%n", actualConfidence));
//        } catch (InputDataParseException parseException) {
//            assertTrue("", false);
//        }
//    }
//
//    @Then("^the confidence of the local prediction for \"(.*)\" is ([\\d,.]+)$")
//    public void the_confidence_of_the_local_prediction_for_is(String args, double expectedConfidence) {
//        try {
//            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
//            HashMap<Object, Object> p = predictiveEnsemble.predict(inputObj, true, true);
//            Double actualConfidence = (Double) p.get("confidence");
//            assertEquals(String.format("%.4g%n", expectedConfidence), String.format("%.4g%n", actualConfidence));
//        } catch (InputDataParseException parseException) {
//            assertTrue("", false);
//        }
//    }
//
//    @Then("^the local prediction for \"(.*)\" is \"([^\"]*)\"$")
//    public void the_local_prediction_for_is(String args, String pred) {
//        try {
//            JSONObject inputObj = (JSONObject) JSONValue.parse(args);
//            HashMap<Object, Object> p = predictiveEnsemble.predict(inputObj, null);
//            String prediction = (String) p.get("prediction");
//            assertEquals(pred, prediction);
//        } catch (InputDataParseException parseException) {
//            assertTrue("", false);
//        }
//    }
//
//    @Then("^the local prediction by name=(true|false) for \"(.*)\" is \"([^\"]*)\"$")
//    public void the_local_prediction_byname_for_is(String by_name, String args,
//            String pred) {
//        try {
//            Boolean byName = new Boolean(by_name);
//            HashMap<Object, Object> p = predictiveEnsemble.predict( (JSONObject) JSONValue.parse(args),
//                    byName);
//            String prediction = (String) p.get("prediction");
//            assertEquals(pred, prediction);
//        } catch (InputDataParseException parseException) {
//            assertTrue("", false);
//        }
//    }
//
//    @Then("^\"(.*)\" field\'s name is changed to \"(.*)\"$")
//    public void field_name_to_new_name(String fieldId, String newName) {
//        JSONObject field = (JSONObject) Utils.getJSONObject(
//                predictiveEnsemble.fields(), fieldId);
//        if (!field.get("name").equals(newName)) {
//            field.put("name", newName);
//        }
//        assertEquals(newName, field.get("name"));
//    }
//
}
