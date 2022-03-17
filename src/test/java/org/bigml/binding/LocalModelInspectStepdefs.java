package org.bigml.binding;

import org.bigml.binding.localmodel.Predicate;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

import io.cucumber.java.en.Given;

public class LocalModelInspectStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(LocalModelInspectStepdefs.class);

    LocalPredictiveModel localModel;
    String output;
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a local model for inspection$")
    public void I_create_a_local_model_for_inspection() throws Exception {
        localModel = new LocalPredictiveModel(context.model);
        assertTrue("", localModel != null);
    }

    @Given("^I translate the tree into IF_THEN rules$")
    public void i_translate_the_tree_into_IF_THEN_rules() {
        this.output = localModel.rules(Predicate.RuleLanguage.PSEUDOCODE);
        assertTrue("", this.output != null && this.output.trim().length() > 0 );
    }

    @Given("^I translate the tree into Java rules$")
    public void i_translate_the_tree_into_Java_rules() {
        this.output = localModel.rules(Predicate.RuleLanguage.JAVA);
        assertTrue("", this.output != null && this.output.trim().length() > 0 );
    }

    @Given("^I check the data distribution with \"(.*)\" file$")
    public void i_check_the_data_distribution(String file) {
        JSONArray distribution = localModel.getDataDistribution();

        StringBuilder distributionStr = new StringBuilder();
        for (Object bin : distribution) {
            JSONArray binObject = (JSONArray) bin;
            distributionStr.append(String.format("[%s,%s]\n", binObject.get(0), binObject.get(1)));
        }

        this.output = distributionStr.toString();

        i_check_if_the_output_is_like_expected_file(file);
    }

    @Given("^I check the predictions distribution with \"(.*)\" file$")
    public void i_check_the_predictions_distribution(String file) {
        JSONArray distribution = localModel.getPredictionDistribution(null);

        StringBuilder distributionStr = new StringBuilder();
        for (Object bin : distribution) {
            JSONArray binObject = (JSONArray) bin;
            distributionStr.append(String.format("[%s,%s]\n", binObject.get(0), binObject.get(1)));
        }

        this.output = distributionStr.toString();

        i_check_if_the_output_is_like_expected_file(file);
    }

    @Given("^I check the model summary with \"(.*)\" file$")
    public void i_check_the_model_summary_with(String file) throws Exception {
        this.output = localModel.summarize(true);

        i_check_if_the_output_is_like_expected_file(file);
    }

    @Given("^I check the output is like \"(.*)\" expected file$")
    public void i_check_if_the_output_is_like_expected_file(String expectedFile) {
        String expectedContent = Utils.readFile(expectedFile);
        assertTrue("", expectedContent.trim().equals(this.output.trim()));
    }

}