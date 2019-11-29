package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.When;

import org.bigml.binding.LocalAssociation;
import org.bigml.binding.localassociation.*;


public class AssociationsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(AssociationsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    LocalAssociation localAssociation;


    @Given("^I create an association with search strategy \"(.*)\" from a dataset$")
    public void I_create_an_association_with_search_strategy_from_a_dataset(String strategy)
    		throws Throwable {
    	
    	JSONObject args = new JSONObject();
        args.put("search_strategy", strategy);
        commonSteps.I_create_a_resource_from_a_dataset_with(
        		"association", args.toString());
    }

    @Given("^I create a local association$")
    public void I_create_a_local_association() throws Exception {
        localAssociation = new LocalAssociation(context.association);
    }


    @When("^I get the rules for \"(.*)\" and the first rule is \"(.*)\"$")
    public void I_get_the_rules_for_and_the_first_rule_is(
    		List itemList, String ruleJson) throws Throwable {
        List<AssociationRule> rules = localAssociation.rules(
            null, null, null, itemList, null);

        if (rules.size() == 0) {
            assertFalse("No rules for association", false);
        }

        AssociationRule rule = rules.get(0);
        
        JSONObject expected = (JSONObject) JSONValue.parse(ruleJson);
        assertEquals(expected, rule.getRule());
    }

}