package org.bigml.binding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;

public class SourcesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(SourcesStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I create a data source uploading a \"([^\"]*)\" file$")
    public void I_create_a_data_source_uploading_a_file(String fileName)
            throws AuthenticationException {
    	
    	JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = context.api.createSource(
        		fileName, "new source", null, args);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.source = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a data source using the url \"([^\"]*)\"$")
    public void I_create_a_data_source_using_the_url(String url)
            throws AuthenticationException {
    	JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        
        JSONObject resource = context.api.createRemoteSource(
        		url, args);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.source = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a data source from inline data slurped from \"([^\"]*)\"$")
    public void I_create_a_data_source_from_inline_data_slurped_from(String data)
            throws AuthenticationException {

        String inlineData = null;
        try {
            FileInputStream downloadFis = new FileInputStream(new File(data));
            inlineData = Utils.inputStreamAsString(downloadFis, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Unable to load the file.", e);
        }
        
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = context.api.createInlineSource(
        		inlineData, args);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.source = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

}