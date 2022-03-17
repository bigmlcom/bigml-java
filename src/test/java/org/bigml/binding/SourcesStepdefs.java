package org.bigml.binding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;

public class SourcesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(SourcesStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;

    @Given("^I provision a data source from \"([^\"]*)\" file$")
    public void I_provision_a_data_source_from_file(String fileName)
    		throws Throwable {
        
    	JSONObject resource = null;
    	try {
    		JSONArray resources = (JSONArray) context.api
    			.listSources("tags__in=" + fileName).get("objects");
    		if (resources.size() > 0) {
    			resource = (JSONObject) resources.get(0);
    		}
    	} catch (Exception e) {}
    	
    	if (resource != null) {
    		commonSteps.I_get_the_resource(
            	"source", (String) resource.get("resource"));
            
    	} else {
    		JSONObject args = new JSONObject();
    		args.put("tags", new JSONArray());
    		((JSONArray) args.get("tags")).add(fileName);
    		I_create_a_data_source_uploading_a_file(fileName, args);
    	}
    	
    	commonSteps.I_wait_until_the_resource_is_ready_less_than_secs(
    		"source", 100);
    }
    
    
    @Given("^I create a data source uploading a \"([^\"]*)\" file$")
    public void I_create_a_data_source_uploading_a_file(String fileName)
            throws AuthenticationException {
    	I_create_a_data_source_uploading_a_file(fileName, null);
    }
    
    
    public void I_create_a_data_source_uploading_a_file(String fileName, JSONObject args)
            throws AuthenticationException {

    	args = commonSteps.setProject(args);
        
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
    	
    	JSONObject args = commonSteps.setProject(null);
        
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
        
        JSONObject args = commonSteps.setProject(null);

        JSONObject resource = context.api.createInlineSource(
        		inlineData, args);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.source = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a source from the external connector id$")
    public void I_create_a_source_from_the_external_connector_id() throws Throwable {

        JSONObject args = commonSteps.setProject(null);

        JSONObject externalData = new JSONObject();
        externalData.put("source", context.externalConnector.get("source"));
        externalData.put("connection", context.externalConnector.get("connection"));

        JSONObject resource = context.api.createExternalDataSource(
            externalData, args);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.source = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

}