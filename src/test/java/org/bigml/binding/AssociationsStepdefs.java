package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.bigml.binding.localassociation.*;


public class AssociationsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(AssociationsStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    LocalAssociation localAssociation;
    List localSet;


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
            String item, String ruleJson) throws Throwable {

        List<String> itemList = new ArrayList<String>();
        itemList.add(item);

        List<AssociationRule> rules = localAssociation.rules(
            null, null, null, itemList, null);

        if (rules.size() == 0) {
            assertFalse("No rules for association", false);
        }

        AssociationRule rule = rules.get(0);
        
        JSONObject expected = (JSONObject) JSONValue.parse(ruleJson);
        assertEquals(expected, rule.getRule());
    }


    @When("^I create a local association set for \"(.*)\"$")
    public void I_create_a_local_association_set(String data)
            throws Throwable {
        if( data == null || data.trim().length() == 0 ) {
            data = "{}";
        }

        JSONObject inputData = (JSONObject) JSONValue.parse(data);
        localSet = localAssociation.associationSet(inputData, null, null);
    }


    @Then("^the local association set is \"(.*)\"$")
    public void the_local_association_set_is(String expectedSet)
            throws Throwable {
        assertEquals(expectedSet, JSONArray.toJSONString(localSet));
    }

}